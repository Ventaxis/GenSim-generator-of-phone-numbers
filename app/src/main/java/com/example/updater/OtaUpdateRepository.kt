package com.example.updater

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import com.example.BuildConfig
import com.example.model.AppUpdateInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.concurrent.TimeUnit

class OtaUpdateRepository(private val context: Context) {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    suspend fun checkLatestRelease(repo: String): Result<AppUpdateInfo> = withContext(Dispatchers.IO) {
        try {
            val cleanRepo = repo.trim().removePrefix("https://github.com/").removeSuffix("/")
            if (!cleanRepo.contains("/")) {
                return@withContext Result.failure(
                    IllegalArgumentException("Неверный формат репозитория. Используйте: владелец/репозиторий")
                )
            }

            val url = "https://api.github.com/repos/$cleanRepo/releases/latest"
            val request = Request.Builder()
                .url(url)
                .header("Accept", "application/vnd.github.v3+json")
                .header("User-Agent", "SimCardTycoon-OTA-App")
                .get()
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                val code = response.code
                val errBody = response.body?.string() ?: ""
                return@withContext Result.failure(
                    when (code) {
                        404 -> Exception("Релизы в репозитории '$cleanRepo' не найдены. Убедитесь, что создан Release и прикреплен файл .apk")
                        403 -> Exception("Превышен лимит запросов GitHub API. Попробуйте чуть позже.")
                        else -> Exception("Ошибка сервера GitHub (код $code): ${errBody.take(120)}")
                    }
                )
            }

            val bodyString = response.body?.string() ?: return@withContext Result.failure(
                Exception("Пустой ответ от GitHub API")
            )

            val json = JSONObject(bodyString)
            val tagName = json.optString("tag_name", "v1.0")
            val releaseTitle = json.optString("name", tagName)
            val changelog = json.optString("body", "Новые функции, оптимизация и исправление ошибок.")
            val publishedAt = json.optString("published_at", "")

            val assets = json.optJSONArray("assets")
            var apkDownloadUrl = ""
            var apkFileName = ""
            var apkSize = 0L

            if (assets != null) {
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    val name = asset.optString("name", "")
                    if (name.endsWith(".apk", ignoreCase = true)) {
                        apkDownloadUrl = asset.optString("browser_download_url", "")
                        apkFileName = name
                        apkSize = asset.optLong("size", 0L)
                        break
                    }
                }
            }

            if (apkDownloadUrl.isEmpty()) {
                return@withContext Result.failure(
                    Exception("В последнем релизе ($tagName) не найден прикрепленный .apk файл. Загрузите APK в Assets релиза на GitHub.")
                )
            }

            val currentVerName = BuildConfig.VERSION_NAME
            val currentVerCode = BuildConfig.VERSION_CODE
            val remoteVerName = tagName.trimStart('v', 'V')
            val isNewer = isRemoteVersionNewer(remoteVerName, currentVerName, currentVerCode)

            val updateInfo = AppUpdateInfo(
                tagName = tagName,
                versionName = remoteVerName,
                versionCode = 0,
                releaseTitle = if (releaseTitle.isBlank()) tagName else releaseTitle,
                changelog = changelog,
                downloadUrl = apkDownloadUrl,
                apkFileName = apkFileName,
                apkSize = apkSize,
                publishedAt = publishedAt,
                isNewerThanCurrent = isNewer
            )

            Result.success(updateInfo)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun downloadApk(
        downloadUrl: String,
        targetFileName: String,
        onProgress: (percent: Int, downloaded: Long, total: Long) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        var inputStream: InputStream? = null
        var outputStream: FileOutputStream? = null
        try {
            val updatesDir = File(context.cacheDir, "updates")
            if (!updatesDir.exists()) {
                updatesDir.mkdirs()
            }

            val cleanName = if (targetFileName.isNotBlank()) targetFileName else "update.apk"
            val targetFile = File(updatesDir, cleanName)
            if (targetFile.exists()) {
                targetFile.delete()
            }

            val request = Request.Builder()
                .url(downloadUrl)
                .header("User-Agent", "SimCardTycoon-OTA-App")
                .get()
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(
                    Exception("Не удалось скачать APK (HTTP ${response.code})")
                )
            }

            val responseBody = response.body ?: return@withContext Result.failure(
                Exception("Тело ответа при скачивании пустое")
            )

            val totalBytes = responseBody.contentLength()
            inputStream = responseBody.byteStream()
            outputStream = FileOutputStream(targetFile)

            val buffer = ByteArray(8 * 1024)
            var bytesRead: Int
            var totalRead = 0L
            var lastReportedPercent = -1

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                totalRead += bytesRead
                val percent = if (totalBytes > 0) {
                    ((totalRead * 100) / totalBytes).toInt().coerceIn(0, 100)
                } else {
                    0
                }
                if (percent != lastReportedPercent) {
                    lastReportedPercent = percent
                    onProgress(percent, totalRead, totalBytes)
                }
            }
            outputStream.flush()
            onProgress(100, totalRead, totalBytes)

            Result.success(targetFile)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            try { inputStream?.close() } catch (_: Exception) {}
            try { outputStream?.close() } catch (_: Exception) {}
        }
    }

    fun canInstallUnknownApps(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true
        }
    }

    fun openInstallPermissionSettings(activityContext: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                data = Uri.parse("package:${activityContext.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            activityContext.startActivity(intent)
        }
    }

    fun installApk(activityContext: Context, apkFile: File): Result<Unit> {
        return try {
            if (!apkFile.exists()) {
                return Result.failure(Exception("Файл обновления не найден на устройстве"))
            }

            val apkUri: Uri = FileProvider.getUriForFile(
                activityContext,
                "${activityContext.packageName}.fileprovider",
                apkFile
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            activityContext.startActivity(installIntent)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun isRemoteVersionNewer(remoteVer: String, currentVer: String, currentCode: Int): Boolean {
        // Strip suffixes like -beta, -alpha
        val remoteClean = remoteVer.split("-")[0].trim()
        val currentClean = currentVer.split("-")[0].trim()

        val remoteParts = remoteClean.split(".").mapNotNull { it.toIntOrNull() }
        val currentParts = currentClean.split(".").mapNotNull { it.toIntOrNull() }

        val maxLen = maxOf(remoteParts.size, currentParts.size)
        for (i in 0 until maxLen) {
            val r = remoteParts.getOrElse(i) { 0 }
            val c = currentParts.getOrElse(i) { 0 }
            if (r > c) return true
            if (r < c) return false
        }

        // If versions are equal, fallback to false
        return false
    }
}
