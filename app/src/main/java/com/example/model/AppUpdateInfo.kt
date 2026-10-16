package com.example.model

import java.io.File

data class AppUpdateInfo(
    val tagName: String,
    val versionName: String,
    val versionCode: Int,
    val releaseTitle: String,
    val changelog: String,
    val downloadUrl: String,
    val apkFileName: String,
    val apkSize: Long,
    val publishedAt: String,
    val isNewerThanCurrent: Boolean
)

sealed interface UpdateState {
    data object Idle : UpdateState
    data object Checking : UpdateState
    data class Available(val info: AppUpdateInfo) : UpdateState
    data class Downloading(
        val info: AppUpdateInfo,
        val progressPercent: Int,
        val downloadedBytes: Long,
        val totalBytes: Long
    ) : UpdateState
    data class ReadyToInstall(
        val info: AppUpdateInfo,
        val apkFile: File
    ) : UpdateState
    data class UpToDate(val checkedAtMillis: Long, val currentVersion: String) : UpdateState
    data class Error(val message: String) : UpdateState
}
