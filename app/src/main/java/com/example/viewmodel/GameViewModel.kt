package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.GamePreferences
import com.example.data.GameStatsEntity
import com.example.data.SavedSimEntity
import com.example.generator.PhoneGenerator
import com.example.model.AutoSellConfig
import com.example.model.AppUpdateInfo
import com.example.model.UpdateState
import com.example.model.PhoneNumber
import com.example.model.PlayerLevelInfo
import com.example.model.PlayerProgression
import com.example.model.Quest
import com.example.model.QuestPeriod
import com.example.model.RarityTier
import com.example.model.UpgradeType
import com.example.updater.OtaUpdateRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class GameEvent {
    data class Haptic(val isHeavy: Boolean = false) : GameEvent()
    data class Alert(val title: String, val message: String) : GameEvent()
    data class Celebration(val reward: Long, val title: String) : GameEvent()
    data class LevelUp(val newLevel: Int, val title: String, val bonusReward: Long) : GameEvent()
    data class XpGained(val amount: Long, val numberValue: Long) : GameEvent()
}

data class GameStats(
    val totalRolls: Long = 0,
    val totalEarned: Long = 0,
    val bestNumber: PhoneNumber? = null,
    val rarityCounts: Map<RarityTier, Int> = emptyMap()
)

data class RoomDiagnosticData(
    val dbName: String = "sim_tycoon.db",
    val dbVersion: Int = 3,
    val savedSimsCount: Int = 0,
    val savedSimsRawRows: List<Map<String, Any?>> = emptyList(),
    val gameStatsRawRow: Map<String, Any?>? = null,
    val memoryBestNumber: PhoneNumber? = null,
    val memoryRarityCounts: Map<RarityTier, Int> = emptyMap(),
    val memoryTotalRolls: Long = 0,
    val memoryTotalEarned: Long = 0,
    val prefsBestNumber: PhoneNumber? = null,
    val prefsRarityCounts: Map<RarityTier, Int> = emptyMap(),
    val prefsTotalRolls: Long = 0,
    val prefsTotalEarned: Long = 0,
    val roomBestNumberFormatted: String? = null,
    val roomBestNumberValue: Long? = null,
    val roomBestNumberRarity: String? = null,
    val roomRarityCounts: Map<RarityTier, Int> = emptyMap(),
    val roomTotalRolls: Long = 0,
    val roomTotalEarned: Long = 0,
    val isBestNumberPersisted: Boolean = false,
    val isRarityCountsPersisted: Boolean = false,
    val isOverallPersistenceOk: Boolean = false,
    val eventLogs: List<String> = emptyList(),
    val lastCheckedTimestamp: Long = System.currentTimeMillis()
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    val rollCost: Long = 1000L

    private val db = AppDatabase.getInstance(application)
    private val simDao = db.savedSimDao()
    private val gameStatsDao = db.gameStatsDao()
    private val prefs = GamePreferences(application)

    private val _balance = MutableStateFlow(prefs.loadBalance(10_000L))
    val balance: StateFlow<Long> = _balance.asStateFlow()

    private val _playerXp = MutableStateFlow(prefs.loadPlayerXp())
    val playerXp: StateFlow<Long> = _playerXp.asStateFlow()

    private val _levelInfo = MutableStateFlow(PlayerProgression.getLevelInfo(_playerXp.value))
    val levelInfo: StateFlow<PlayerLevelInfo> = _levelInfo.asStateFlow()

    private val _lastXpGained = MutableStateFlow<Long?>(null)
    val lastXpGained: StateFlow<Long?> = _lastXpGained.asStateFlow()

    private val _currentNumber = MutableStateFlow<PhoneNumber?>(null)
    val currentNumber: StateFlow<PhoneNumber?> = _currentNumber.asStateFlow()

    private val _isCurrentSaved = MutableStateFlow(false)
    val isCurrentSaved: StateFlow<Boolean> = _isCurrentSaved.asStateFlow()

    // Initialize inventory immediately from backup to prevent blank flash
    private val _inventory = MutableStateFlow<List<PhoneNumber>>(prefs.loadInventoryBackup())
    val inventory: StateFlow<List<PhoneNumber>> = _inventory.asStateFlow()

    private val _activeSim = MutableStateFlow<PhoneNumber?>(prefs.loadActiveSim())
    val activeSim: StateFlow<PhoneNumber?> = _activeSim.asStateFlow()

    private val _upgrades = MutableStateFlow<Map<String, Int>>(
        UpgradeType.values().associate { it.id to 0 }.toMutableMap().apply {
            putAll(prefs.loadUpgrades())
        }
    )
    val upgrades: StateFlow<Map<String, Int>> = _upgrades.asStateFlow()

    private val _autoSellConfig = MutableStateFlow(
        prefs.loadAutoSellConfig(
            AutoSellConfig(
                minKeepPrice = 1_800,
                autoSellBelowRarity = RarityTier.SILVER
            )
        )
    )
    val autoSellConfig: StateFlow<AutoSellConfig> = _autoSellConfig.asStateFlow()

    private val savedStats = prefs.loadStats()
    private val _stats = MutableStateFlow(
        GameStats(
            totalRolls = savedStats.totalRolls,
            totalEarned = savedStats.totalEarned,
            bestNumber = savedStats.bestNumber,
            rarityCounts = savedStats.rarityCounts
        )
    )
    val stats: StateFlow<GameStats> = _stats.asStateFlow()

    private val _luckPityRolls = MutableStateFlow(0)
    val luckPityRolls: StateFlow<Int> = _luckPityRolls.asStateFlow()

    private val _isLuckBoostActive = MutableStateFlow(false)
    val isLuckBoostActive: StateFlow<Boolean> = _isLuckBoostActive.asStateFlow()

    private val otaRepository = OtaUpdateRepository(application.applicationContext)

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    private val _githubRepo = MutableStateFlow(prefs.getGitHubRepo())
    val githubRepo: StateFlow<String> = _githubRepo.asStateFlow()

    private val _showUpdateDialog = MutableStateFlow(false)
    val showUpdateDialog: StateFlow<Boolean> = _showUpdateDialog.asStateFlow()

    private val _quests = MutableStateFlow<List<Quest>>(loadQuestsWithSavedProgress())
    val quests: StateFlow<List<Quest>> = _quests.asStateFlow()

    private val _notification = MutableStateFlow<String?>(null)
    val notification: StateFlow<String?> = _notification.asStateFlow()

    private val _selectedCountryIso = MutableStateFlow(prefs.loadSelectedCountry())
    val selectedCountryIso: StateFlow<String> = _selectedCountryIso.asStateFlow()

    fun selectCountry(iso: String) {
        _selectedCountryIso.value = iso
        prefs.saveSelectedCountry(iso)
        val opt = PhoneGenerator.AVAILABLE_COUNTRIES.find { it.iso == iso }
        val displayName = if (opt != null) "${opt.flag} ${opt.name}" else iso
        showNotification("Регион генерации: $displayName")
        viewModelScope.launch {
            _events.emit(GameEvent.Haptic(true))
        }
    }

    private val _events = MutableSharedFlow<GameEvent>()
    val events: SharedFlow<GameEvent> = _events.asSharedFlow()

    private val _diagnosticData = MutableStateFlow(RoomDiagnosticData())
    val diagnosticData: StateFlow<RoomDiagnosticData> = _diagnosticData.asStateFlow()

    private val _diagnosticLogs = MutableStateFlow<List<String>>(emptyList())
    val diagnosticLogs: StateFlow<List<String>> = _diagnosticLogs.asStateFlow()

    fun addDiagnosticLog(message: String) {
        val time = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
        val entry = "[$time] $message"
        _diagnosticLogs.update { (it + entry).takeLast(100) }
    }

    fun refreshDiagnosticData() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val rawSims = db.getRawTableContent("saved_sims")
                val rawStatsList = db.getRawTableContent("game_stats_log")
                val rawStats = rawStatsList.firstOrNull()

                val memStats = _stats.value
                val loadedPrefs = prefs.loadStats()

                val roomRolls = (rawStats?.get("totalRolls") as? Number)?.toLong() ?: 0L
                val roomEarned = (rawStats?.get("totalEarned") as? Number)?.toLong() ?: 0L
                val roomBestFormatted = rawStats?.get("bestNumberFormatted") as? String
                val roomBestValue = (rawStats?.get("bestNumberValue") as? Number)?.toLong()
                val roomBestRarity = rawStats?.get("bestNumberRarity") as? String
                val roomRarityStr = rawStats?.get("rarityCountsJson") as? String ?: ""
                val roomRarityMap = mutableMapOf<RarityTier, Int>()
                if (roomRarityStr.isNotBlank()) {
                    roomRarityStr.split(";").forEach { item ->
                        val parts = item.split(":")
                        if (parts.size == 2) {
                            try {
                                val tier = RarityTier.valueOf(parts[0])
                                val count = parts[1].toIntOrNull() ?: 0
                                roomRarityMap[tier] = count
                            } catch (_: Exception) {}
                        }
                    }
                }

                val bestPersisted = if (memStats.bestNumber == null) {
                    loadedPrefs.bestNumber == null
                } else {
                    loadedPrefs.bestNumber?.id == memStats.bestNumber.id
                }

                val rarityPersisted = memStats.rarityCounts == loadedPrefs.rarityCounts
                val overallOk = bestPersisted && rarityPersisted

                _diagnosticData.value = RoomDiagnosticData(
                    dbName = "sim_tycoon.db",
                    dbVersion = 3,
                    savedSimsCount = rawSims.size,
                    savedSimsRawRows = rawSims,
                    gameStatsRawRow = rawStats,
                    memoryBestNumber = memStats.bestNumber,
                    memoryRarityCounts = memStats.rarityCounts,
                    memoryTotalRolls = memStats.totalRolls,
                    memoryTotalEarned = memStats.totalEarned,
                    prefsBestNumber = loadedPrefs.bestNumber,
                    prefsRarityCounts = loadedPrefs.rarityCounts,
                    prefsTotalRolls = loadedPrefs.totalRolls,
                    prefsTotalEarned = loadedPrefs.totalEarned,
                    roomBestNumberFormatted = roomBestFormatted,
                    roomBestNumberValue = roomBestValue,
                    roomBestNumberRarity = roomBestRarity,
                    roomRarityCounts = roomRarityMap,
                    roomTotalRolls = roomRolls,
                    roomTotalEarned = roomEarned,
                    isBestNumberPersisted = bestPersisted,
                    isRarityCountsPersisted = rarityPersisted,
                    isOverallPersistenceOk = overallOk,
                    eventLogs = _diagnosticLogs.value,
                    lastCheckedTimestamp = System.currentTimeMillis()
                )
            } catch (e: Exception) {
                addDiagnosticLog("Ошибка опроса Room DB для диагностики: ${e.message}")
            }
        }
    }

    private var autoRollJob: Job? = null
    private var isRoomInitialized = false

    val vaultCount: Int
        get() = _inventory.value.count { it.isInVault }

    val vaultCapacity: Int
        get() {
            val level = _upgrades.value[UpgradeType.VAULT_EXPANSION.id] ?: 0
            val playerLevelBonus = _levelInfo.value.bonusVaultSlots
            return 10 + (level * 6) + playerLevelBonus
        }

    val inventoryCapacity: Int
        get() {
            val lvl = _upgrades.value[UpgradeType.INVENTORY_CAPACITY.id] ?: 0
            return when (lvl) {
                0 -> 100
                1 -> 250
                2 -> 500
                3 -> 800
                4 -> 1_200
                5 -> 1_800
                6 -> 2_500
                7 -> 3_500
                8 -> 5_000
                9 -> 7_000
                else -> 10_000
            }
        }

    val inventoryCount: Int
        get() = _inventory.value.count { !it.isInVault }

    // Passive income neutralized as requested by user
    val passiveIncomePerSec: Long = 0L

    fun getEffectiveSellPrice(number: PhoneNumber): Long {
        val brokerLvl = _upgrades.value[UpgradeType.ELITE_BROKER.id] ?: 0
        val monopolyLvl = _upgrades.value[UpgradeType.OLIGARCH_MONOPOLY.id] ?: 0
        val cartelLvl = _upgrades.value[UpgradeType.OPERATOR_CARTEL.id] ?: 0
        val offshoreLvl = _upgrades.value[UpgradeType.OFFSHORE_SYNDICATE.id] ?: 0
        val vertushkaLvl = _upgrades.value[UpgradeType.KREMLIN_VERTUSHKA.id] ?: 0
        val levelBonus = _levelInfo.value.sellPriceBonusPercent / 100.0

        var bonus = 1.0 + (brokerLvl * 0.15) + (monopolyLvl * 0.25) + (cartelLvl * 0.15) + levelBonus
        if (number.rarity.isGovOrSecret) {
            bonus += (offshoreLvl * 0.30)
        }
        if (vertushkaLvl > 0 && number.rarity.isRareOrAbove) {
            bonus += (vertushkaLvl * 0.50)
        }
        return (number.value * bonus).toLong().coerceAtLeast(number.value)
    }

    val unclaimedQuestsCount: Int
        get() = _quests.value.count { it.isCompleted && !it.isClaimed }

    private var statsPersistJob: Job? = null

    private fun persistStats() {
        statsPersistJob?.cancel()
        statsPersistJob = viewModelScope.launch(Dispatchers.IO) {
            delay(350)
            val s = _stats.value
            prefs.saveStats(s.totalRolls, s.totalEarned, s.bestNumber, s.rarityCounts)
            try {
                gameStatsDao.insertOrUpdate(
                    GameStatsEntity(
                        id = "stats_record",
                        totalRolls = s.totalRolls,
                        totalEarned = s.totalEarned,
                        bestNumberId = s.bestNumber?.id,
                        bestNumberFormatted = s.bestNumber?.formatted,
                        bestNumberValue = s.bestNumber?.value,
                        bestNumberRarity = s.bestNumber?.rarity?.name,
                        bestNumberJson = s.bestNumber?.toJson(),
                        rarityCountsJson = s.rarityCounts.entries.joinToString(";") { "${it.key.name}:${it.value}" },
                        lastUpdatedTimestamp = System.currentTimeMillis()
                    )
                )
                refreshDiagnosticData()
            } catch (_: Exception) {}
        }
    }

    private fun trimInventoryToLimit(list: MutableList<PhoneNumber>) {
        val limit = inventoryCapacity
        val deletedIds = mutableListOf<String>()
        while (list.count { !it.isInVault } > limit) {
            val candidateIndex = list.indexOfLast { !it.isInVault && it.id != _activeSim.value?.id }
            if (candidateIndex != -1) {
                val removed = list.removeAt(candidateIndex)
                deletedIds.add(removed.id)
            } else {
                break
            }
        }
        if (deletedIds.isNotEmpty()) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    simDao.deleteSimsByIds(deletedIds)
                } catch (_: Exception) {}
            }
        }
    }

    private fun checkPassiveIncomeQuest() {
        updateQuestProgress("monthly_passive_income", passiveIncomePerSec, isAbsolute = true)
    }

    init {
        // Load initial SIM inventory from Room once on startup without background flow thrashing
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val entities = simDao.getAllSavedSimsOnce()
                if (entities.isNotEmpty()) {
                    isRoomInitialized = true
                    val domainList = entities.map { it.toDomain() }
                    withContext(Dispatchers.Main) {
                        _inventory.value = domainList
                        if (_activeSim.value == null) {
                            val first = domainList.firstOrNull()
                            if (first != null) {
                                _activeSim.value = first
                                prefs.saveActiveSim(first)
                            }
                        }
                    }
                } else {
                    val backupSims = prefs.loadInventoryBackup()
                    if (backupSims.isNotEmpty()) {
                        isRoomInitialized = true
                        withContext(Dispatchers.Main) {
                            _inventory.value = backupSims
                            if (_activeSim.value == null) {
                                val first = backupSims.firstOrNull()
                                if (first != null) {
                                    _activeSim.value = first
                                    prefs.saveActiveSim(first)
                                    checkPassiveIncomeQuest()
                                }
                            }
                        }
                        try {
                            simDao.insertAll(backupSims.map { SavedSimEntity.fromDomain(it) })
                        } catch (_: Exception) {}
                    }
                }
            } catch (_: Exception) {}
        }

        // Restore last generated number from preferences across app restarts
        val restoredCurrent = prefs.loadCurrentNumber()
        if (restoredCurrent != null) {
            _currentNumber.value = restoredCurrent
        } else {
            val initialNumber = _activeSim.value ?: PhoneGenerator.generate(0, 0, _levelInfo.value.level)
            _currentNumber.value = initialNumber
            prefs.saveCurrentNumber(initialNumber)
        }

        val initialPity = prefs.loadLuckPity(fallback = (savedStats.totalRolls % 100).toInt())
        _luckPityRolls.value = initialPity.coerceIn(0, 100)
        _isLuckBoostActive.value = _luckPityRolls.value >= 100

        addDiagnosticLog("Инициализация приложения: Room БД sim_tycoon.db подключена. Удача pity: ${_luckPityRolls.value}/100")
        addDiagnosticLog("Загрузка статистики: rolls=${savedStats.totalRolls}, earned=${savedStats.totalEarned}, bestNumber=${savedStats.bestNumber?.formatted ?: "null"}, rarityTiers=${savedStats.rarityCounts.size}")

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val existing = gameStatsDao.getStats()
                if (existing == null) {
                    val s = _stats.value
                    gameStatsDao.insertOrUpdate(
                        GameStatsEntity(
                            id = "stats_record",
                            totalRolls = s.totalRolls,
                            totalEarned = s.totalEarned,
                            bestNumberId = s.bestNumber?.id,
                            bestNumberFormatted = s.bestNumber?.formatted,
                            bestNumberValue = s.bestNumber?.value,
                            bestNumberRarity = s.bestNumber?.rarity?.name,
                            bestNumberJson = s.bestNumber?.toJson(),
                            rarityCountsJson = s.rarityCounts.entries.joinToString(";") { "${it.key.name}:${it.value}" },
                            lastUpdatedTimestamp = System.currentTimeMillis()
                        )
                    )
                    addDiagnosticLog("Первичная синхронизация Room DB: создана запись game_stats_log")
                } else {
                    // Bidirectional sync with existing stats from old version/Room DB
                    val currentMem = _stats.value
                    val mergedRolls = maxOf(currentMem.totalRolls, existing.totalRolls)
                    val mergedEarned = maxOf(currentMem.totalEarned, existing.totalEarned)
                    val existingBest = if (!existing.bestNumberJson.isNullOrBlank()) PhoneNumber.fromJson(existing.bestNumberJson) else null
                    val mergedBest = when {
                        currentMem.bestNumber == null -> existingBest
                        existingBest == null -> currentMem.bestNumber
                        existingBest.value > currentMem.bestNumber.value -> existingBest
                        else -> currentMem.bestNumber
                    }
                    val existingRarities = mutableMapOf<RarityTier, Int>()
                    if (existing.rarityCountsJson.isNotBlank()) {
                        existing.rarityCountsJson.split(";").forEach { item ->
                            val parts = item.split(":")
                            if (parts.size == 2) {
                                try {
                                    val tier = RarityTier.valueOf(parts[0])
                                    existingRarities[tier] = parts[1].toIntOrNull() ?: 0
                                } catch (_: Exception) {}
                            }
                        }
                    }
                    val mergedRarities = currentMem.rarityCounts.toMutableMap()
                    existingRarities.forEach { (tier, count) ->
                        mergedRarities[tier] = maxOf(mergedRarities[tier] ?: 0, count)
                    }

                    _stats.value = GameStats(
                        totalRolls = mergedRolls,
                        totalEarned = mergedEarned,
                        bestNumber = mergedBest,
                        rarityCounts = mergedRarities
                    )
                    prefs.saveStats(mergedRolls, mergedEarned, mergedBest, mergedRarities)
                    gameStatsDao.insertOrUpdate(
                        GameStatsEntity(
                            id = "stats_record",
                            totalRolls = mergedRolls,
                            totalEarned = mergedEarned,
                            bestNumberId = mergedBest?.id,
                            bestNumberFormatted = mergedBest?.formatted,
                            bestNumberValue = mergedBest?.value,
                            bestNumberRarity = mergedBest?.rarity?.name,
                            bestNumberJson = mergedBest?.toJson(),
                            rarityCountsJson = mergedRarities.entries.joinToString(";") { "${it.key.name}:${it.value}" },
                            lastUpdatedTimestamp = System.currentTimeMillis()
                        )
                    )
                    addDiagnosticLog("В Room DB синхронизированы сохранённые данные: rolls=$mergedRolls, best=${mergedBest?.formatted ?: "null"}")
                }
                refreshDiagnosticData()
            } catch (e: Exception) {
                addDiagnosticLog("Ошибка инициализации game_stats_log: ${e.message}")
            }
        }

        // Automatic background check for updates on startup
        viewModelScope.launch {
            delay(1500)
            checkForUpdates(isManual = false)
        }
    }

    fun setGitHubRepo(repo: String) {
        val trimmed = repo.trim()
        _githubRepo.value = trimmed
        prefs.setGitHubRepo(trimmed)
        checkForUpdates(isManual = true)
    }

    fun checkForUpdates(isManual: Boolean = false) {
        viewModelScope.launch {
            _updateState.value = UpdateState.Checking
            val result = otaRepository.checkLatestRelease(_githubRepo.value)
            result.onSuccess { info ->
                if (info.isNewerThanCurrent) {
                    _updateState.value = UpdateState.Available(info)
                    val dismissed = prefs.getDismissedUpdateVersion()
                    if (!isManual && dismissed == info.tagName) {
                        _showUpdateDialog.value = false
                    } else {
                        _showUpdateDialog.value = true
                    }
                } else {
                    _updateState.value = UpdateState.UpToDate(
                        checkedAtMillis = System.currentTimeMillis(),
                        currentVersion = com.example.BuildConfig.VERSION_NAME
                    )
                    if (isManual) {
                        showNotification("У вас установлена последняя версия (v${com.example.BuildConfig.VERSION_NAME})")
                    }
                }
            }.onFailure { err ->
                _updateState.value = UpdateState.Error(err.message ?: "Ошибка проверки")
                if (isManual) {
                    showNotification(err.message ?: "Ошибка проверки обновлений")
                }
            }
        }
    }

    fun startDownloadUpdate(info: AppUpdateInfo) {
        viewModelScope.launch {
            _updateState.value = UpdateState.Downloading(
                info = info,
                progressPercent = 0,
                downloadedBytes = 0L,
                totalBytes = info.apkSize
            )
            val result = otaRepository.downloadApk(
                downloadUrl = info.downloadUrl,
                targetFileName = info.apkFileName.ifBlank { "sim_card_tycoon_${info.tagName}.apk" }
            ) { percent, downloaded, total ->
                _updateState.value = UpdateState.Downloading(
                    info = info,
                    progressPercent = percent,
                    downloadedBytes = downloaded,
                    totalBytes = total
                )
            }

            result.onSuccess { file ->
                _updateState.value = UpdateState.ReadyToInstall(info, file)
            }.onFailure { err ->
                _updateState.value = UpdateState.Error("Ошибка скачивания: ${err.message}")
            }
        }
    }

    fun installDownloadedApk(activityContext: android.content.Context, file: java.io.File) {
        if (!otaRepository.canInstallUnknownApps()) {
            showNotification("Разрешите установку из этого источника в настройках Android")
            otaRepository.openInstallPermissionSettings(activityContext)
            return
        }
        val result = otaRepository.installApk(activityContext, file)
        result.onFailure {
            showNotification("Не удалось запустить установку: ${it.message}")
        }
    }

    fun dismissUpdateDialog(tagName: String? = null) {
        if (tagName != null) {
            prefs.setDismissedUpdateVersion(tagName)
        }
        _showUpdateDialog.value = false
    }

    fun openUpdateDialog() {
        _showUpdateDialog.value = true
    }


    private var lastRollUptimeMillis = 0L
    private var lastHapticUptimeMillis = 0L
    private var inventorySaveJob: Job? = null

    fun scheduleInventoryBackup(list: List<PhoneNumber>) {
        inventorySaveJob?.cancel()
        inventorySaveJob = viewModelScope.launch(Dispatchers.IO) {
            delay(300)
            prefs.saveInventoryBackup(list)
        }
    }

    fun rollSingle() {
        val now = android.os.SystemClock.uptimeMillis()
        if (now - lastRollUptimeMillis < 45) return // Throttles rapid auto-clicker taps to avoid ANR and dropped frames
        lastRollUptimeMillis = now

        val satLvl = _upgrades.value[UpgradeType.SATELLITE_ORBIT.id] ?: 0
        val cashback = satLvl * 60L
        val effectiveRollCost = (rollCost - cashback).coerceAtLeast(100L)

        if (_balance.value < effectiveRollCost) {
            showNotification("Недостаточно средств (нужно $effectiveRollCost ₽)")
            return
        }

        _balance.update { it - effectiveRollCost }
        prefs.saveBalance(_balance.value)

        val mvdLvl = _upgrades.value[UpgradeType.MVD_INSIDER.id] ?: 0
        val goldLvl = _upgrades.value[UpgradeType.GOLDEN_DATABASE.id] ?: 0
        val quantumLvl = _upgrades.value[UpgradeType.QUANTUM_SCANNER.id] ?: 0
        val presLvl = _upgrades.value[UpgradeType.PRESIDENTIAL_HOTLINE.id] ?: 0
        val neuralLvl = _upgrades.value[UpgradeType.NEURAL_CLUSTER.id] ?: 0

        val has5xLuck = _isLuckBoostActive.value
        val luckMult = if (has5xLuck) 5.0 else 1.0

        val generated = PhoneGenerator.generate(
            mvdInsiderLevel = mvdLvl,
            goldenDbLevel = goldLvl,
            playerLevel = _levelInfo.value.level,
            quantumScannerLevel = quantumLvl,
            presidentialHotlineLevel = presLvl,
            neuralClusterLevel = neuralLvl,
            luckMultiplier = luckMult,
            targetCountryIso = _selectedCountryIso.value
        )

        if (has5xLuck) {
            _isLuckBoostActive.value = false
            _luckPityRolls.value = 0
            prefs.saveLuckPity(0)
            showNotification("⚡ Сработал 5X бонус удачи! Выбит ${generated.rarity.title} номер!")
        } else {
            val nextPity = (_luckPityRolls.value + 1).coerceAtMost(100)
            _luckPityRolls.value = nextPity
            if (nextPity >= 100) {
                _isLuckBoostActive.value = true
                showNotification("🔥 5X УДАЧА ЗАРЯЖЕНА! Следующий номер гарантирует 5X шанс!")
            }
            prefs.saveLuckPity(nextPity)
        }

        _currentNumber.value = generated
        _isCurrentSaved.value = false
        prefs.saveCurrentNumber(generated)

        // Add to inventory ensuring no duplicates and respecting limit
        val currentList = _inventory.value.toMutableList()
        currentList.removeAll { it.id == generated.id }
        currentList.add(0, generated)
        trimInventoryToLimit(currentList)
        _inventory.value = currentList
        scheduleInventoryBackup(currentList)

        viewModelScope.launch(Dispatchers.IO) {
            try {
                simDao.insertSim(SavedSimEntity.fromDomain(generated))
            } catch (_: Exception) {}
        }

        recordStats(generated)
        updateRollQuests(generated)
        addExperienceForRoll(generated)

        if (now - lastHapticUptimeMillis >= 70) {
            lastHapticUptimeMillis = now
            viewModelScope.launch {
                if (generated.rarity.isGovOrSecret || generated.rarity == RarityTier.DIAMOND) {
                    _events.emit(GameEvent.Haptic(true))
                    _events.emit(
                        GameEvent.Alert(
                            title = "🚨 ЭКСКЛЮЗИВНЫЙ ПЕРЕХВАТ!",
                            message = "Номер: ${generated.formatted}\nСтрана: ${generated.countryName}\nОценка: ${formatMoney(generated.value)}"
                        )
                    )
                } else if (generated.rarity == RarityTier.GOLD || generated.rarity == RarityTier.PLATINUM) {
                    _events.emit(GameEvent.Haptic(true))
                } else {
                    _events.emit(GameEvent.Haptic(false))
                }
            }
        }
    }

    private fun addExperienceForRoll(number: PhoneNumber, isAutoRoll: Boolean = false) {
        val xpGain = PlayerProgression.getXpRewardForNumber(number)
        if (!isAutoRoll) {
            _lastXpGained.value = xpGain
        }

        val oldLevel = _levelInfo.value.level
        val newXp = _playerXp.value + xpGain
        _playerXp.value = newXp
        if (!isAutoRoll) {
            prefs.savePlayerXp(newXp)
        }

        val newInfo = PlayerProgression.getLevelInfo(newXp)
        _levelInfo.value = newInfo

        if (!isAutoRoll) {
            viewModelScope.launch {
                _events.emit(GameEvent.XpGained(xpGain, number.value))
            }
        }

        if (newInfo.level > oldLevel) {
            val bonusCash = newInfo.level * 3_000L
            _balance.update { it + bonusCash }
            prefs.saveBalance(_balance.value)
            viewModelScope.launch {
                _events.emit(GameEvent.Haptic(true))
                _events.emit(
                    GameEvent.LevelUp(
                        newLevel = newInfo.level,
                        title = newInfo.title,
                        bonusReward = bonusCash
                    )
                )
            }
        }
    }

    fun sellCurrent() {
        val current = _currentNumber.value ?: return
        if (current.isInVault || _inventory.value.any { it.id == current.id && it.isInVault }) {
            showNotification("Номер заблокирован в сейфе! Сначала извлеките его.")
            return
        }

        val effectivePrice = getEffectiveSellPrice(current)
        _balance.update { it + effectivePrice }
        prefs.saveBalance(_balance.value)
        _stats.update { it.copy(totalEarned = it.totalEarned + effectivePrice) }
        persistStats()
        showNotification("Продано: +${formatMoney(effectivePrice)}")
        updateQuestsByCondition(effectivePrice) { it.categoryIcon == "money" || it.id.contains("earn") }

        val updated = _inventory.value.filter { it.id != current.id }
        _inventory.value = updated
        scheduleInventoryBackup(updated)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                simDao.deleteSimById(current.id)
            } catch (_: Exception) {}
        }

        if (_activeSim.value?.id == current.id) {
            val nextSim = updated.firstOrNull()
            _activeSim.value = nextSim
            prefs.saveActiveSim(nextSim)
            checkPassiveIncomeQuest()
        }

        val nextNumber = updated.firstOrNull() ?: _activeSim.value ?: PhoneGenerator.generate(0, 0, _levelInfo.value.level)
        _currentNumber.value = nextNumber
        _isCurrentSaved.value = true
        prefs.saveCurrentNumber(nextNumber)
    }

    fun saveCurrentToVault(): Boolean {
        val current = _currentNumber.value ?: return false
        if (current.isInVault) {
            showNotification("Этот номер уже сохранён в сейфе!")
            return true
        }
        if (vaultCount >= vaultCapacity) {
            showNotification("Сейф полон! ($vaultCount/$vaultCapacity). Расширьте в магазине.")
            return false
        }

        val updatedCurrent = current.copy(isInVault = true)
        _currentNumber.value = updatedCurrent
        _isCurrentSaved.value = true
        prefs.saveCurrentNumber(updatedCurrent)

        val updated = _inventory.value.toMutableList()
        updated.removeAll { it.id == current.id }
        updated.add(0, updatedCurrent)
        trimInventoryToLimit(updated)
        _inventory.value = updated
        scheduleInventoryBackup(updated)

        viewModelScope.launch(Dispatchers.IO) {
            try {
                simDao.insertSim(SavedSimEntity.fromDomain(updatedCurrent))
            } catch (_: Exception) {}
        }

        showNotification("Сохранено в сейф! 🔒")
        updateQuestProgress("daily_vault_1", 1)
        updateQuestProgress("monthly_vault_8", vaultCount.toLong(), isAbsolute = true)
        return true
    }

    fun toggleVaultStatus(number: PhoneNumber) {
        if (!number.isInVault) {
            if (vaultCount >= vaultCapacity) {
                showNotification("Сейф полон! ($vaultCount/$vaultCapacity). Расширьте в магазине.")
                return
            }
            val updated = _inventory.value.map {
                if (it.id == number.id) it.copy(isInVault = true) else it
            }
            _inventory.value = updated
            scheduleInventoryBackup(updated)
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    simDao.updateVaultStatus(number.id, true)
                } catch (_: Exception) {}
            }
            if (_currentNumber.value?.id == number.id) {
                _currentNumber.value = _currentNumber.value?.copy(isInVault = true)
                _isCurrentSaved.value = true
            }
            showNotification("Номер добавлен в сейф! 🔒")
            updateQuestProgress("daily_vault_1", 1)
            updateQuestProgress("monthly_vault_8", vaultCount.toLong(), isAbsolute = true)
        } else {
            val updated = _inventory.value.map {
                if (it.id == number.id) it.copy(isInVault = false) else it
            }
            _inventory.value = updated
            scheduleInventoryBackup(updated)
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    simDao.updateVaultStatus(number.id, false)
                } catch (_: Exception) {}
            }
            if (_currentNumber.value?.id == number.id) {
                _currentNumber.value = _currentNumber.value?.copy(isInVault = false)
            }
            showNotification("Номер извлечён из сейфа")
        }
    }

    fun sellFromInventory(number: PhoneNumber) {
        if (number.isInVault || _inventory.value.any { it.id == number.id && it.isInVault }) {
            showNotification("Номер находится в сейфе! Сначала извлеките его.")
            return
        }

        val updated = _inventory.value.filter { it.id != number.id }
        _inventory.value = updated
        scheduleInventoryBackup(updated)

        viewModelScope.launch(Dispatchers.IO) {
            try {
                simDao.deleteSimById(number.id)
            } catch (_: Exception) {}
        }
        if (_activeSim.value?.id == number.id) {
            val nextSim = updated.firstOrNull() ?: if (_currentNumber.value?.id != number.id) _currentNumber.value else null
            _activeSim.value = nextSim
            prefs.saveActiveSim(nextSim)
            checkPassiveIncomeQuest()
        }
        if (_currentNumber.value?.id == number.id) {
            val nextSim = updated.firstOrNull() ?: _activeSim.value ?: PhoneGenerator.generate(0, 0, _levelInfo.value.level)
            _currentNumber.value = nextSim
            prefs.saveCurrentNumber(nextSim)
        }
        val effectivePrice = getEffectiveSellPrice(number)
        _balance.update { it + effectivePrice }
        prefs.saveBalance(_balance.value)
        _stats.update { it.copy(totalEarned = it.totalEarned + effectivePrice) }
        persistStats()
        showNotification("SIM продана за ${formatMoney(effectivePrice)}")
        updateQuestsByCondition(effectivePrice) { it.id.contains("earn") || it.categoryIcon == "money" }
    }

    fun sellAllCommonFromInventory() {
        val itemsToSell = _inventory.value.filter {
            it.rarity == RarityTier.COMMON && !it.isInVault
        }
        if (itemsToSell.isEmpty()) {
            showNotification("Нет обычных незапертых номеров для продажи")
            return
        }
        val totalGain = itemsToSell.sumOf { getEffectiveSellPrice(it) }
        val toSellIds = itemsToSell.map { it.id }.toSet()
        val updated = _inventory.value.filter { it.id !in toSellIds }
        _inventory.value = updated
        scheduleInventoryBackup(updated)

        viewModelScope.launch(Dispatchers.IO) {
            try {
                simDao.deleteSimsByIds(toSellIds.toList())
            } catch (_: Exception) {}
        }
        if (_activeSim.value?.id in toSellIds) {
            val nextSim = updated.firstOrNull() ?: _inventory.value.firstOrNull { it.isInVault }
            _activeSim.value = nextSim
            prefs.saveActiveSim(nextSim)
            checkPassiveIncomeQuest()
        }
        if (_currentNumber.value?.id in toSellIds) {
            val nextSim = updated.firstOrNull() ?: _activeSim.value ?: PhoneGenerator.generate(0, 0, _levelInfo.value.level)
            _currentNumber.value = nextSim
            prefs.saveCurrentNumber(nextSim)
        }
        _balance.update { it + totalGain }
        prefs.saveBalance(_balance.value)
        _stats.update { it.copy(totalEarned = it.totalEarned + totalGain) }
        persistStats()
        showNotification("Продано ${itemsToSell.size} обычных номеров (+${formatMoney(totalGain)})")
        updateQuestsByCondition(totalGain) { it.id.contains("earn") || it.categoryIcon == "money" }
        viewModelScope.launch {
            _events.emit(GameEvent.Haptic(true))
        }
    }

    fun sellAllFromInventory() {
        val itemsToSell = _inventory.value.filter {
            !it.isInVault
        }
        if (itemsToSell.isEmpty()) {
            showNotification("В коллекции нет свободных номеров для продажи (сейф защищён)")
            return
        }
        val totalGain = itemsToSell.sumOf { getEffectiveSellPrice(it) }
        val toSellIds = itemsToSell.map { it.id }.toSet()
        val updated = _inventory.value.filter { it.id !in toSellIds }
        _inventory.value = updated
        scheduleInventoryBackup(updated)

        viewModelScope.launch(Dispatchers.IO) {
            try {
                simDao.deleteSimsByIds(toSellIds.toList())
            } catch (_: Exception) {}
        }
        if (_activeSim.value?.id in toSellIds) {
            val nextSim = updated.firstOrNull() ?: _inventory.value.firstOrNull { it.isInVault }
            _activeSim.value = nextSim
            prefs.saveActiveSim(nextSim)
            checkPassiveIncomeQuest()
        }
        if (_currentNumber.value?.id in toSellIds) {
            val nextSim = updated.firstOrNull() ?: _activeSim.value ?: PhoneGenerator.generate(0, 0, _levelInfo.value.level)
            _currentNumber.value = nextSim
            prefs.saveCurrentNumber(nextSim)
        }
        _balance.update { it + totalGain }
        prefs.saveBalance(_balance.value)
        _stats.update { it.copy(totalEarned = it.totalEarned + totalGain) }
        persistStats()
        showNotification("Продано всё из коллекции: ${itemsToSell.size} шт. (+${formatMoney(totalGain)})")
        updateQuestsByCondition(totalGain) { it.id.contains("earn") || it.categoryIcon == "money" }
        viewModelScope.launch {
            _events.emit(GameEvent.Haptic(true))
        }
    }

    fun setActiveSim(number: PhoneNumber) {
        _activeSim.value = number
        prefs.saveActiveSim(number)
        checkPassiveIncomeQuest()
        showNotification("Установлена как основная SIM в телефон!")
        viewModelScope.launch {
            _events.emit(GameEvent.Haptic(true))
        }
    }

    fun toggleAutoRoll() {
        if (_autoSellConfig.value.isAutoRolling) {
            stopAutoRoll()
        } else {
            startAutoRoll()
        }
    }

    fun updateAutoSellConfig(config: AutoSellConfig) {
        _autoSellConfig.value = config
        prefs.saveAutoSellConfig(config)
    }

    private fun startAutoRoll() {
        stopAutoRoll()
        _autoSellConfig.update { it.copy(isAutoRolling = true) }
        autoRollJob = viewModelScope.launch {
            try {
                val turboLvl = _upgrades.value[UpgradeType.TURBO_ROLL.id] ?: 0
                val interval = (380L - (turboLvl * 15L)).coerceAtLeast(220L)
                val satLvl = _upgrades.value[UpgradeType.SATELLITE_ORBIT.id] ?: 0
                val cashback = satLvl * 60L
                val effectiveCost = (rollCost - cashback).coerceAtLeast(100L)
                var rollCounter = 0

                while (_autoSellConfig.value.isAutoRolling) {
                    if (_balance.value < effectiveCost) {
                        showNotification("Автокрутка: закончились средства (нужно $effectiveCost ₽)")
                        break
                    }

                    _balance.update { it - effectiveCost }

                    val mvdLvl = _upgrades.value[UpgradeType.MVD_INSIDER.id] ?: 0
                    val goldLvl = _upgrades.value[UpgradeType.GOLDEN_DATABASE.id] ?: 0
                    val quantumLvl = _upgrades.value[UpgradeType.QUANTUM_SCANNER.id] ?: 0
                    val presLvl = _upgrades.value[UpgradeType.PRESIDENTIAL_HOTLINE.id] ?: 0
                    val neuralLvl = _upgrades.value[UpgradeType.NEURAL_CLUSTER.id] ?: 0

                    val has5xLuck = _isLuckBoostActive.value
                    val luckMult = if (has5xLuck) 5.0 else 1.0

                    val generated = PhoneGenerator.generate(
                        mvdInsiderLevel = mvdLvl,
                        goldenDbLevel = goldLvl,
                        playerLevel = _levelInfo.value.level,
                        quantumScannerLevel = quantumLvl,
                        presidentialHotlineLevel = presLvl,
                        neuralClusterLevel = neuralLvl,
                        luckMultiplier = luckMult,
                        targetCountryIso = _selectedCountryIso.value
                    )

                    if (has5xLuck) {
                        _isLuckBoostActive.value = false
                        _luckPityRolls.value = 0
                        prefs.saveLuckPity(0)
                        showNotification("⚡ Сработал 5X бонус удачи! Выбит ${generated.rarity.title} номер!")
                    } else {
                        val nextPity = (_luckPityRolls.value + 1).coerceAtMost(100)
                        _luckPityRolls.value = nextPity
                        if (nextPity >= 100) {
                            _isLuckBoostActive.value = true
                        }
                        prefs.saveLuckPity(nextPity)
                    }

                    _currentNumber.value = generated

                    recordStats(generated)
                    updateRollQuests(generated)
                    addExperienceForRoll(generated, isAutoRoll = true)

                    val isUltra = generated.rarity.isGovOrSecret || generated.rarity == RarityTier.DIAMOND
                    if (isUltra && _autoSellConfig.value.stopOnUltraRare) {
                        _events.emit(GameEvent.Haptic(true))
                        _events.emit(
                            GameEvent.Alert(
                                title = "🚨 РЕДКИЙ ПЕРЕХВАТ!",
                                message = "Автокрутка остановлена!\nНомер: ${generated.formatted}\nОценка: ${formatMoney(generated.value)}"
                            )
                        )
                        val vaulted = saveCurrentToVault()
                        if (!vaulted) {
                            val currentList = _inventory.value.toMutableList()
                            currentList.removeAll { it.id == generated.id }
                            currentList.add(0, generated)
                            trimInventoryToLimit(currentList)
                            _inventory.value = currentList
                            prefs.saveInventoryBackup(currentList)
                            viewModelScope.launch {
                                try {
                                    simDao.insertSim(SavedSimEntity.fromDomain(generated))
                                } catch (_: Exception) {}
                            }
                            showNotification("Сейф полон! Номер сохранён в инвентаре")
                        }
                        break
                    }

                    val keepCondition = generated.rarity.ordinal >= _autoSellConfig.value.autoSellBelowRarity.ordinal &&
                            generated.value >= _autoSellConfig.value.minKeepPrice

                    if (keepCondition) {
                        val shouldVault = _autoSellConfig.value.autoSaveKeptToInventory && vaultCount < vaultCapacity
                        val simToKeep = if (shouldVault) generated.copy(isInVault = true) else generated
                        val updated = _inventory.value.toMutableList()
                        updated.removeAll { it.id == simToKeep.id }
                        updated.add(0, simToKeep)
                        trimInventoryToLimit(updated)
                        _inventory.value = updated
                        prefs.saveInventoryBackup(updated)
                        viewModelScope.launch {
                            try {
                                simDao.insertSim(SavedSimEntity.fromDomain(simToKeep))
                            } catch (_: Exception) {}
                        }
                        if (shouldVault) {
                            showNotification("Сохранен в сейф: ${generated.formatted}")
                            updateQuestProgress("daily_vault_1", 1)
                            updateQuestProgress("monthly_vault_8", vaultCount.toLong(), isAbsolute = true)
                        } else if (vaultCount >= vaultCapacity && _autoSellConfig.value.autoSaveKeptToInventory) {
                            showNotification("Сейф полон! В инвентаре: ${generated.formatted}")
                        }
                    } else {
                        val autoPrice = getEffectiveSellPrice(generated)
                        _balance.update { it + autoPrice }
                        _stats.update { it.copy(totalEarned = it.totalEarned + autoPrice) }
                        updateQuestsByCondition(autoPrice) { it.id.contains("earn") || it.categoryIcon == "money" }
                    }

                    rollCounter++
                    if (rollCounter % 10 == 0) {
                        prefs.saveBalance(_balance.value)
                        prefs.saveCurrentNumber(generated)
                        prefs.savePlayerXp(_playerXp.value)
                        persistStats()
                    }

                    delay(interval)
                }
            } finally {
                prefs.saveBalance(_balance.value)
                persistStats()
                stopAutoRoll()
            }
        }
    }

    fun stopAutoRoll() {
        _autoSellConfig.update { it.copy(isAutoRolling = false) }
        autoRollJob?.cancel()
        autoRollJob = null
    }

    fun buyUpgrade(type: UpgradeType) {
        val currLevel = _upgrades.value[type.id] ?: 0
        if (currLevel >= type.maxLevel) {
            showNotification("Максимальный уровень!")
            return
        }

        val price = type.getPrice(currLevel)
        if (_balance.value < price) {
            showNotification("Не хватает средств: ${formatMoney(price)}")
            return
        }

        _balance.update { it - price }
        prefs.saveBalance(_balance.value)

        val newUpgrades = _upgrades.value.toMutableMap().apply { put(type.id, currLevel + 1) }
        _upgrades.value = newUpgrades
        prefs.saveUpgrades(newUpgrades)

        showNotification("${type.title} прокачан до ур. ${currLevel + 1}!")
        updateQuestsByCondition(1) { it.categoryIcon == "upgrade" || it.id.contains("upgrade") }
        checkPassiveIncomeQuest()

        viewModelScope.launch {
            _events.emit(GameEvent.Haptic(false))
        }
    }

    fun refreshQuests(period: QuestPeriod? = null) {
        val current = _quests.value
        val updated = current.map { q ->
            if (period == null || q.period == period) {
                generateDynamicQuest(q.period)
            } else q
        }
        _quests.value = updated
        prefs.saveQuests(updated)
        showNotification("Контракты обновлены! Доступны новые задания.")
        viewModelScope.launch {
            _events.emit(GameEvent.Haptic(false))
        }
    }

    fun claimQuestReward(questId: String) {
        var claimedQuest: Quest? = null
        _quests.update { list ->
            list.map {
                if (it.id == questId && it.isCompleted && !it.isClaimed) {
                    claimedQuest = it
                    it.copy(isClaimed = true)
                } else it
            }
        }
        val quest = claimedQuest ?: return
        prefs.saveQuests(_quests.value)

        _balance.update { it + quest.rewardAmount }
        prefs.saveBalance(_balance.value)
        _stats.update { it.copy(totalEarned = it.totalEarned + quest.rewardAmount) }
        persistStats()

        // Give XP bonus for quest completion!
        addExperienceForQuest(quest.rewardAmount)

        showNotification("Награда получена: +${formatMoney(quest.rewardAmount)}!")

        viewModelScope.launch {
            _events.emit(GameEvent.Haptic(true))
            _events.emit(GameEvent.Celebration(quest.rewardAmount, quest.title))

            // Replace claimed quest with a dynamic fresh contract
            delay(450)
            _quests.update { list ->
                list.map {
                    if (it.id == questId) {
                        generateDynamicQuest(it.period)
                    } else it
                }
            }
            prefs.saveQuests(_quests.value)
        }
    }

    private fun addExperienceForQuest(reward: Long) {
        val questXp = (reward / 10L).coerceIn(50L, 2_500L)
        val oldLevel = _levelInfo.value.level
        val newXp = _playerXp.value + questXp
        _playerXp.value = newXp
        prefs.savePlayerXp(newXp)

        val newInfo = PlayerProgression.getLevelInfo(newXp)
        _levelInfo.value = newInfo

        if (newInfo.level > oldLevel) {
            val bonusCash = newInfo.level * 3_000L
            _balance.update { it + bonusCash }
            prefs.saveBalance(_balance.value)
            viewModelScope.launch {
                _events.emit(GameEvent.LevelUp(newInfo.level, newInfo.title, bonusCash))
            }
        }
    }

    private fun updateRollQuests(number: PhoneNumber) {
        updateQuestsByCondition(1) { it.categoryIcon == "roll" || it.id.contains("rolls") }

        if (number.rarity.ordinal >= RarityTier.BRONZE.ordinal) {
            updateQuestsByCondition(1) { it.categoryIcon == "bronze" || it.id.contains("bronze") }
        }
        if (number.rarity.ordinal >= RarityTier.SILVER.ordinal) {
            updateQuestsByCondition(1) { it.categoryIcon == "silver" || it.id.contains("silver") }
        }
        if (number.rarity.ordinal >= RarityTier.GOLD.ordinal) {
            updateQuestsByCondition(1) { it.categoryIcon == "gold" || it.id.contains("gold") }
        }
        if (number.rarity.isGovOrSecret || number.rarity == RarityTier.DIAMOND || number.rarity == RarityTier.PLATINUM) {
            updateQuestsByCondition(1) { it.categoryIcon == "gov" || it.id.contains("gov") || it.id.contains("plat") || it.id.contains("spec") }
        }
        if (number.digits.contains("777") || number.prefix == "777" || number.digits.contains("999") || number.prefix == "999") {
            updateQuestsByCondition(1) { it.id.contains("777") || it.id.contains("vip_code") }
        }
        if (number.totalMultiplier >= 2.0) {
            updateQuestsByCondition(1) { it.id.contains("combo") || it.categoryIcon == "combo" }
        }
        if (number.operatorName.contains("Билайн", ignoreCase = true) || number.operatorName.contains("Yota", ignoreCase = true) || number.operatorName.contains("Мегафон", ignoreCase = true)) {
            updateQuestsByCondition(1) { it.id.contains("operator") }
        }
        if (number.categoryDescription.contains("зеркал", ignoreCase = true) ||
            number.categoryDescription.contains("дубль", ignoreCase = true) ||
            number.categoryDescription.contains("палиндром", ignoreCase = true) ||
            number.categoryDescription.contains("хвост", ignoreCase = true)
        ) {
            updateQuestsByCondition(1) { it.id.contains("mirror") || it.id.contains("double") }
        }
    }

    fun updateQuestsByCondition(delta: Long = 1, isAbsolute: Boolean = false, predicate: (Quest) -> Boolean) {
        _quests.update { list ->
            list.map { q ->
                if (!q.isClaimed && predicate(q)) {
                    val target = q.targetProgress.toLong()
                    val newProg = if (isAbsolute) {
                        delta.coerceIn(0L, target)
                    } else {
                        (q.currentProgress.toLong() + delta.coerceAtLeast(0L)).coerceIn(0L, target)
                    }
                    q.copy(currentProgress = newProg.toInt())
                } else {
                    q
                }
            }
        }
        prefs.saveQuests(_quests.value)
    }

    private fun updateQuestProgress(questId: String, delta: Long, isAbsolute: Boolean = false) {
        updateQuestsByCondition(delta, isAbsolute) { it.id == questId }
    }

    private fun recordStats(number: PhoneNumber) {
        _stats.update { s ->
            val newCounts = s.rarityCounts.toMutableMap()
            newCounts[number.rarity] = (newCounts[number.rarity] ?: 0) + 1
            val best = if (s.bestNumber == null || number.value > s.bestNumber.value) number else s.bestNumber
            s.copy(
                totalRolls = s.totalRolls + 1,
                bestNumber = best,
                rarityCounts = newCounts
            )
        }
        persistStats()
    }

    private fun loadQuestsWithSavedProgress(): List<Quest> {
        val initial = createInitialQuests()
        val saved = prefs.loadQuestsProgress()
        val resetDates = prefs.loadLastResetDates()

        val cal = java.util.Calendar.getInstance()
        val currentDailyKey = String.format(java.util.Locale.US, "%04d-%02d-%02d", cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH) + 1, cal.get(java.util.Calendar.DAY_OF_MONTH))
        val currentWeeklyKey = String.format(java.util.Locale.US, "%04d-W%02d", cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.WEEK_OF_YEAR))
        val currentMonthlyKey = String.format(java.util.Locale.US, "%04d-M%02d", cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH) + 1)

        val shouldResetDaily = resetDates.lastDaily.isNotEmpty() && resetDates.lastDaily != currentDailyKey
        val shouldResetWeekly = resetDates.lastWeekly.isNotEmpty() && resetDates.lastWeekly != currentWeeklyKey
        val shouldResetMonthly = resetDates.lastMonthly.isNotEmpty() && resetDates.lastMonthly != currentMonthlyKey

        // Persist current calendar reset dates
        prefs.saveLastResetDates(currentDailyKey, currentWeeklyKey, currentMonthlyKey)

        val quests = initial.map { q ->
            val shouldResetThis = when (q.period) {
                QuestPeriod.DAILY -> shouldResetDaily
                QuestPeriod.WEEKLY -> shouldResetWeekly
                QuestPeriod.MONTHLY -> shouldResetMonthly
            }

            if (shouldResetThis) {
                q.copy(currentProgress = 0, isClaimed = false)
            } else {
                val savedProgress = saved[q.id]
                if (savedProgress != null) {
                    q.copy(
                        currentProgress = savedProgress.first.coerceIn(0, q.targetProgress),
                        isClaimed = savedProgress.second
                    )
                } else q
            }
        }
        prefs.saveQuests(quests)
        return quests
    }

    fun showNotification(msg: String) {
        _notification.value = msg
        viewModelScope.launch {
            delay(2200)
            if (_notification.value == msg) {
                _notification.value = null
            }
        }
    }

    fun clearNotification() {
        _notification.value = null
    }

    private var questGenCounter = 0

    private fun generateDynamicQuest(period: QuestPeriod): Quest {
        questGenCounter++
        val uid = "${period.name.lowercase()}_dyn_${System.currentTimeMillis() % 100000}_$questGenCounter"
        return when (period) {
            QuestPeriod.DAILY -> {
                val pool = listOf(
                    Quest(uid, QuestPeriod.DAILY, "Охота за кодом 777", "Выбейте номер с комбинацией или кодом 777", 0, 1, 8_000, false, "gold"),
                    Quest(uid, QuestPeriod.DAILY, "Зеркальный палиндром", "Выбейте номер с зеркальным или парным хвостом", 0, 1, 6_000, false, "silver"),
                    Quest(uid, QuestPeriod.DAILY, "Крипто-нумеролог", "Поймайте красивый номер с множителем x2.0+", 0, 1, 7_500, false, "combo"),
                    Quest(uid, QuestPeriod.DAILY, "Контракт операторов", "Выбейте 2 номера Билайн, Yota или Мегафон", 0, 2, 5_000, false, "operator"),
                    Quest(uid, QuestPeriod.DAILY, "В золотой сейф", "Сохраните 2 ценных номера в сейф", 0, 2, 4_000, false, "vault"),
                    Quest(uid, QuestPeriod.DAILY, "Выручка дилера", "Заработайте 15 000 ₽ на продаже номеров", 0, 15_000, 8_000, false, "money"),
                    Quest(uid, QuestPeriod.DAILY, "Срочный перебор", "Сделайте 35 быстрых круток телефонных номеров", 0, 35, 3_000, false, "roll"),
                    Quest(uid, QuestPeriod.DAILY, "Телеком-серия", "Выбейте 3 номера ранга Бронза или выше", 0, 3, 4_500, false, "bronze"),
                    Quest(uid, QuestPeriod.DAILY, "Серебряная удача", "Выбейте 1 номер ранга Серебро или выше", 0, 1, 6_500, false, "silver"),
                    Quest(uid, QuestPeriod.DAILY, "Модернизация вышки", "Купите 1 любое улучшение в магазине", 0, 1, 4_000, false, "upgrade")
                )
                pool.random()
            }
            QuestPeriod.WEEKLY -> {
                val pool = listOf(
                    Quest(uid, QuestPeriod.WEEKLY, "Спецсвязь и Элита", "Выбейте 45 номеров Платина, Бриллиант или МВД/ФСБ", 0, 45, 25_000_000, false, "gov"),
                    Quest(uid, QuestPeriod.WEEKLY, "Торговый синдикат", "Заработайте 150 000 000 ₽ на продаже номеров", 0, 150_000_000, 45_000_000, false, "money"),
                    Quest(uid, QuestPeriod.WEEKLY, "Счастливые топоры", "Выбейте 60 номеров с комбинацией 777 или 999", 0, 60, 20_000_000, false, "gold"),
                    Quest(uid, QuestPeriod.WEEKLY, "Золотой фонд", "Выбейте 60 Золотых VIP-номеров", 0, 60, 18_000_000, false, "gold"),
                    Quest(uid, QuestPeriod.WEEKLY, "Архивариус спецбазы", "Сохраните 25 ценных номеров в сейфе", 0, 25, 15_000_000, false, "vault"),
                    Quest(uid, QuestPeriod.WEEKLY, "Инфраструктурный скачок", "Купите любые 25 улучшений в магазине", 0, 25, 15_000_000, false, "upgrade"),
                    Quest(uid, QuestPeriod.WEEKLY, "Телеком-стахановец", "Сделайте 5 000 круток номеров за неделю", 0, 5_000, 14_000_000, false, "roll")
                )
                pool.random()
            }
            QuestPeriod.MONTHLY -> {
                val pool = listOf(
                    Quest(uid, QuestPeriod.MONTHLY, "Легенда связи", "Сделайте 40 000 круток в текущем сезоне", 0, 40_000, 150_000_000, false, "roll"),
                    Quest(uid, QuestPeriod.MONTHLY, "Вертушка АТС-1", "Выбейте 30 правительственных номеров спецсвязи или Бриллиант", 0, 30, 300_000_000, false, "gov"),
                    Quest(uid, QuestPeriod.MONTHLY, "Сейф инвестора", "Соберите коллекцию из 50 номеров в сейфе", 0, 50, 120_000_000, false, "vault"),
                    Quest(uid, QuestPeriod.MONTHLY, "Олигарх сезона", "Заработайте 1 000 000 000 ₽ на продаже номеров", 0, 1_000_000_000, 500_000_000, false, "money"),
                    Quest(uid, QuestPeriod.MONTHLY, "Золотая гвардия", "Выбейте 300 номеров ранга Золото или выше", 0, 300, 250_000_000, false, "gold")
                )
                pool.random()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        autoRollJob?.cancel()
    }

    companion object {
        const val MAX_INVENTORY_SIZE = 200

        fun formatMoney(amount: Long): String {
            val s = amount.toString()
            val reversed = s.reversed()
            val chunked = reversed.chunked(3).joinToString(" ")
            return "${chunked.reversed()} ₽"
        }

        private fun createInitialQuests(): List<Quest> {
            return listOf(
                Quest(
                    id = "daily_777_hunt",
                    period = QuestPeriod.DAILY,
                    title = "Охота за кодом 777",
                    description = "Выбейте номер с комбинацией или кодом 777",
                    targetProgress = 1,
                    rewardAmount = 8_000,
                    categoryIcon = "gold"
                ),
                Quest(
                    id = "daily_bronze_3",
                    period = QuestPeriod.DAILY,
                    title = "Телеком-серия",
                    description = "Выбейте 3 номера ранга Бронза или выше",
                    targetProgress = 3,
                    rewardAmount = 4_500,
                    categoryIcon = "bronze"
                ),
                Quest(
                    id = "daily_vault_2",
                    period = QuestPeriod.DAILY,
                    title = "В золотой сейф",
                    description = "Сохраните 2 ценных номера в сейф",
                    targetProgress = 2,
                    rewardAmount = 4_000,
                    categoryIcon = "vault"
                ),
                Quest(
                    id = "daily_earn_15k",
                    period = QuestPeriod.DAILY,
                    title = "Выручка дилера",
                    description = "Заработайте 15 000 ₽ на продаже номеров",
                    targetProgress = 15_000,
                    rewardAmount = 8_000,
                    categoryIcon = "money"
                ),
                Quest(
                    id = "weekly_gov_or_plat",
                    period = QuestPeriod.WEEKLY,
                    title = "Спецсвязь и Элита",
                    description = "Выбейте 45 номеров Платина, Бриллиант или МВД/ФСБ",
                    targetProgress = 45,
                    rewardAmount = 25_000_000,
                    categoryIcon = "gov"
                ),
                Quest(
                    id = "weekly_trade_syndicate",
                    period = QuestPeriod.WEEKLY,
                    title = "Торговый синдикат",
                    description = "Заработайте 150 000 000 ₽ на продаже номеров",
                    targetProgress = 150_000_000,
                    rewardAmount = 45_000_000,
                    categoryIcon = "money"
                ),
                Quest(
                    id = "weekly_gold_2",
                    period = QuestPeriod.WEEKLY,
                    title = "Золотой фонд",
                    description = "Выбейте 60 Золотых VIP-номеров",
                    targetProgress = 60,
                    rewardAmount = 18_000_000,
                    categoryIcon = "gold"
                ),
                Quest(
                    id = "weekly_upgrade_2",
                    period = QuestPeriod.WEEKLY,
                    title = "Инфраструктурный скачок",
                    description = "Купите любые 25 улучшений в магазине",
                    targetProgress = 25,
                    rewardAmount = 15_000_000,
                    categoryIcon = "upgrade"
                ),
                Quest(
                    id = "monthly_rolls_1000",
                    period = QuestPeriod.MONTHLY,
                    title = "Легенда связи",
                    description = "Сделайте 40 000 круток в текущем сезоне",
                    targetProgress = 40_000,
                    rewardAmount = 150_000_000,
                    categoryIcon = "roll"
                ),
                Quest(
                    id = "monthly_kremlin_vertushka",
                    period = QuestPeriod.MONTHLY,
                    title = "Вертушка АТС-1",
                    description = "Выбейте 30 правительственных номеров спецсвязи или Бриллиант",
                    targetProgress = 30,
                    rewardAmount = 300_000_000,
                    categoryIcon = "gov"
                ),
                Quest(
                    id = "monthly_vault_8",
                    period = QuestPeriod.MONTHLY,
                    title = "Сейф инвестора",
                    description = "Соберите коллекцию из 50 номеров в сейфе",
                    targetProgress = 50,
                    rewardAmount = 120_000_000,
                    categoryIcon = "vault"
                ),
                Quest(
                    id = "monthly_passive_income",
                    period = QuestPeriod.MONTHLY,
                    title = "Олигарх сезона",
                    description = "Заработайте 1 000 000 000 ₽ на продаже номеров",
                    targetProgress = 1_000_000_000,
                    rewardAmount = 500_000_000,
                    categoryIcon = "money"
                )
            )
        }
    }
}
