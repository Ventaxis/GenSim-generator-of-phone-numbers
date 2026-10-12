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
import com.example.model.PhoneNumber
import com.example.model.PlayerLevelInfo
import com.example.model.PlayerProgression
import com.example.model.Quest
import com.example.model.QuestPeriod
import com.example.model.RarityTier
import com.example.model.UpgradeType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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

    val rollCost: Long = 50

    private val db = AppDatabase.getInstance(application)
    private val simDao = db.savedSimDao()
    private val gameStatsDao = db.gameStatsDao()
    private val prefs = GamePreferences(application)

    private val _balance = MutableStateFlow(prefs.loadBalance(2_500L))
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

    private val _quests = MutableStateFlow<List<Quest>>(loadQuestsWithSavedProgress())
    val quests: StateFlow<List<Quest>> = _quests.asStateFlow()

    private val _notification = MutableStateFlow<String?>(null)
    val notification: StateFlow<String?> = _notification.asStateFlow()

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
            return 10 + (level * 4) + playerLevelBonus
        }

    val passiveIncomePerSec: Long
        get() {
            val incomeLevel = _upgrades.value[UpgradeType.PASSIVE_INCOME.id] ?: 0
            val baseIncome = incomeLevel * 25L
            val activeBonus = _activeSim.value?.let {
                (it.value / 30_000L).coerceAtLeast(3L)
            } ?: 0L
            val levelMultiplier = 1.0 + (_levelInfo.value.passiveIncomeBonusPercent / 100.0)
            return ((baseIncome + activeBonus) * levelMultiplier).toLong()
        }

    val unclaimedQuestsCount: Int
        get() = _quests.value.count { it.isCompleted && !it.isClaimed }

    private fun persistStats() {
        val s = _stats.value
        prefs.saveStats(s.totalRolls, s.totalEarned, s.bestNumber, s.rarityCounts)
        viewModelScope.launch(Dispatchers.IO) {
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
                addDiagnosticLog("Room DB & Prefs: сохранено bestNumber=${s.bestNumber?.formatted ?: "null"}, rarityCounts=${s.rarityCounts.size} категорий")
                refreshDiagnosticData()
            } catch (e: Exception) {
                addDiagnosticLog("Ошибка записи статистики в Room DB: ${e.message}")
            }
        }
    }

    private fun trimInventoryToLimit(list: MutableList<PhoneNumber>) {
        val deletedIds = mutableListOf<String>()
        while (list.size > MAX_INVENTORY_SIZE) {
            val candidateIndex = list.indexOfLast { !it.isInVault && it.id != _activeSim.value?.id }
            if (candidateIndex != -1) {
                val removed = list.removeAt(candidateIndex)
                deletedIds.add(removed.id)
            } else {
                break
            }
        }
        if (deletedIds.isNotEmpty()) {
            viewModelScope.launch {
                try {
                    deletedIds.forEach { simDao.deleteSimById(it) }
                } catch (_: Exception) {}
            }
        }
    }

    private fun checkPassiveIncomeQuest() {
        updateQuestProgress("monthly_passive_income", passiveIncomePerSec, isAbsolute = true)
    }

    init {
        // Collect saved SIMs from Room Database and ensure bidirectional synchronization with preferences/backup
        viewModelScope.launch {
            simDao.getAllSavedSims().collect { entities ->
                val backupSims = prefs.loadInventoryBackup()
                if (entities.isNotEmpty()) {
                    isRoomInitialized = true
                    val domainList = entities.map { it.toDomain() }
                    _inventory.value = domainList
                    prefs.saveInventoryBackup(domainList)

                    // If active SIM is not set, pick the first saved number
                    if (_activeSim.value == null) {
                        val first = domainList.firstOrNull()
                        if (first != null) {
                            _activeSim.value = first
                            prefs.saveActiveSim(first)
                            checkPassiveIncomeQuest()
                        }
                    }

                    // Check if backup had any additional SIMs from older version not yet in Room
                    val entityIds = domainList.map { it.id }.toSet()
                    val missingFromRoom = backupSims.filter { it.id !in entityIds }
                    if (missingFromRoom.isNotEmpty()) {
                        missingFromRoom.forEach { missingSim ->
                            try {
                                simDao.insertSim(SavedSimEntity.fromDomain(missingSim))
                            } catch (_: Exception) {}
                        }
                    }
                } else if (!isRoomInitialized && backupSims.isNotEmpty()) {
                    // Populate Room from backup if Room was freshly created/cleared during migration
                    isRoomInitialized = true
                    _inventory.value = backupSims
                    backupSims.forEach { sim ->
                        try {
                            simDao.insertSim(SavedSimEntity.fromDomain(sim))
                        } catch (_: Exception) {}
                    }
                    if (_activeSim.value == null) {
                        val first = backupSims.firstOrNull()
                        if (first != null) {
                            _activeSim.value = first
                            prefs.saveActiveSim(first)
                            checkPassiveIncomeQuest()
                        }
                    }
                } else if (isRoomInitialized) {
                    _inventory.value = emptyList()
                    prefs.saveInventoryBackup(emptyList())
                }
            }
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

        addDiagnosticLog("Инициализация приложения: Room БД sim_tycoon.db подключена.")
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

        startPassiveIncomeLoop()
    }

    private fun startPassiveIncomeLoop() {
        viewModelScope.launch {
            while (true) {
                delay(1000)
                val income = passiveIncomePerSec
                if (income > 0) {
                    _balance.update { it + income }
                    prefs.saveBalance(_balance.value)
                    _stats.update { it.copy(totalEarned = it.totalEarned + income) }
                    persistStats()
                }
                checkPassiveIncomeQuest()
            }
        }
    }

    fun rollSingle() {
        if (_balance.value < rollCost) {
            showNotification("Недостаточно средств (нужно $rollCost ₽)")
            return
        }

        _balance.update { it - rollCost }
        prefs.saveBalance(_balance.value)

        val mvdLvl = _upgrades.value[UpgradeType.MVD_INSIDER.id] ?: 0
        val goldLvl = _upgrades.value[UpgradeType.GOLDEN_DATABASE.id] ?: 0
        val generated = PhoneGenerator.generate(mvdLvl, goldLvl, _levelInfo.value.level)

        _currentNumber.value = generated
        _isCurrentSaved.value = false
        prefs.saveCurrentNumber(generated)

        // Add to inventory ensuring no duplicates and respecting limit
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

        recordStats(generated)
        updateRollQuests(generated)
        addExperienceForRoll(generated)

        viewModelScope.launch {
            if (generated.rarity.isGovOrSecret || generated.rarity == RarityTier.DIAMOND) {
                _events.emit(GameEvent.Haptic(true))
                _events.emit(
                    GameEvent.Alert(
                        title = "🚨 ЭКСКЛЮЗИВНЫЙ ПЕРЕХВАТ!",
                        message = "Номер: ${generated.formatted}\nКатегория: ${generated.categoryDescription}\nОценка: ${formatMoney(generated.value)}"
                    )
                )
            } else if (generated.rarity == RarityTier.GOLD || generated.rarity == RarityTier.PLATINUM) {
                _events.emit(GameEvent.Haptic(true))
            } else {
                _events.emit(GameEvent.Haptic(false))
            }
        }
    }

    private fun addExperienceForRoll(number: PhoneNumber) {
        val xpGain = PlayerProgression.getXpRewardForNumber(number)
        _lastXpGained.value = xpGain

        val oldLevel = _levelInfo.value.level
        val newXp = _playerXp.value + xpGain
        _playerXp.value = newXp
        prefs.savePlayerXp(newXp)

        val newInfo = PlayerProgression.getLevelInfo(newXp)
        _levelInfo.value = newInfo

        viewModelScope.launch {
            _events.emit(GameEvent.XpGained(xpGain, number.value))
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

        _balance.update { it + current.value }
        prefs.saveBalance(_balance.value)
        _stats.update { it.copy(totalEarned = it.totalEarned + current.value) }
        persistStats()
        showNotification("Продано: +${formatMoney(current.value)}")
        updateQuestProgress("daily_earn_3k", current.value)

        val updated = _inventory.value.filter { it.id != current.id }
        _inventory.value = updated
        prefs.saveInventoryBackup(updated)
        viewModelScope.launch {
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

        _currentNumber.value = null
        _isCurrentSaved.value = true
        prefs.saveCurrentNumber(null)
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
        prefs.saveInventoryBackup(updated)

        viewModelScope.launch {
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
            prefs.saveInventoryBackup(updated)
            viewModelScope.launch {
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
            prefs.saveInventoryBackup(updated)
            viewModelScope.launch {
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
        prefs.saveInventoryBackup(updated)

        viewModelScope.launch {
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
            _currentNumber.value = null
            prefs.saveCurrentNumber(null)
        }
        _balance.update { it + number.value }
        prefs.saveBalance(_balance.value)
        _stats.update { it.copy(totalEarned = it.totalEarned + number.value) }
        persistStats()
        showNotification("SIM продана за ${formatMoney(number.value)}")
        updateQuestProgress("daily_earn_3k", number.value)
    }

    fun sellAllCommonFromInventory() {
        val itemsToSell = _inventory.value.filter {
            it.rarity == RarityTier.COMMON && !it.isInVault && it.id != _activeSim.value?.id
        }
        if (itemsToSell.isEmpty()) {
            showNotification("Нет обычных незапертых номеров для продажи")
            return
        }
        val totalGain = itemsToSell.sumOf { it.value }
        val toSellIds = itemsToSell.map { it.id }.toSet()
        val updated = _inventory.value.filter { it.id !in toSellIds }
        _inventory.value = updated
        prefs.saveInventoryBackup(updated)

        viewModelScope.launch {
            try {
                toSellIds.forEach { simDao.deleteSimById(it) }
            } catch (_: Exception) {}
        }
        if (_currentNumber.value?.id in toSellIds) {
            _currentNumber.value = null
            prefs.saveCurrentNumber(null)
        }
        _balance.update { it + totalGain }
        prefs.saveBalance(_balance.value)
        _stats.update { it.copy(totalEarned = it.totalEarned + totalGain) }
        persistStats()
        showNotification("Продано ${itemsToSell.size} обычных номеров (+${formatMoney(totalGain)})")
        updateQuestProgress("daily_earn_3k", totalGain)
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
                val interval = (130L - (turboLvl * 18L)).coerceAtLeast(40L)

                while (_autoSellConfig.value.isAutoRolling) {
                    if (_balance.value < rollCost) {
                        showNotification("Автокрутка: закончились средства")
                        break
                    }

                    _balance.update { it - rollCost }
                    prefs.saveBalance(_balance.value)

                    val mvdLvl = _upgrades.value[UpgradeType.MVD_INSIDER.id] ?: 0
                    val goldLvl = _upgrades.value[UpgradeType.GOLDEN_DATABASE.id] ?: 0
                    val generated = PhoneGenerator.generate(mvdLvl, goldLvl, _levelInfo.value.level)

                    _currentNumber.value = generated
                    prefs.saveCurrentNumber(generated)

                    recordStats(generated)
                    updateRollQuests(generated)
                    addExperienceForRoll(generated)

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
                        } else {
                            if (_autoSellConfig.value.autoSaveKeptToInventory && vaultCount >= vaultCapacity) {
                                showNotification("Сейф полон! В инвентаре: ${generated.formatted}")
                            } else {
                                showNotification("В инвентаре: ${generated.formatted}")
                            }
                        }
                    } else {
                        _balance.update { it + generated.value }
                        prefs.saveBalance(_balance.value)
                        _stats.update { it.copy(totalEarned = it.totalEarned + generated.value) }
                        persistStats()
                        updateQuestProgress("daily_earn_3k", generated.value)
                    }

                    delay(interval)
                }
            } finally {
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
        updateQuestProgress("weekly_upgrade_2", 1)
        checkPassiveIncomeQuest()

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
        updateQuestProgress("daily_rolls_25", 1)
        updateQuestProgress("weekly_rolls_200", 1)
        updateQuestProgress("monthly_rolls_1000", 1)

        if (number.rarity.ordinal >= RarityTier.BRONZE.ordinal) {
            updateQuestProgress("daily_bronze_2", 1)
        }
        if (number.rarity.ordinal >= RarityTier.SILVER.ordinal) {
            updateQuestProgress("weekly_silver_5", 1)
        }
        if (number.rarity.ordinal >= RarityTier.GOLD.ordinal) {
            updateQuestProgress("weekly_gold_1", 1)
        }
        if (number.rarity.isGovOrSecret || number.rarity == RarityTier.DIAMOND || number.rarity == RarityTier.PLATINUM) {
            updateQuestProgress("monthly_gov_or_plat", 1)
        }
    }

    private fun updateQuestProgress(questId: String, delta: Long, isAbsolute: Boolean = false) {
        _quests.update { list ->
            list.map { q ->
                if (q.id == questId && !q.isClaimed) {
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
                    id = "daily_rolls_25",
                    period = QuestPeriod.DAILY,
                    title = "Быстрый перебор",
                    description = "Сделайте 25 круток телефонных номеров",
                    targetProgress = 25,
                    rewardAmount = 750,
                    categoryIcon = "roll"
                ),
                Quest(
                    id = "daily_bronze_2",
                    period = QuestPeriod.DAILY,
                    title = "Первый улов",
                    description = "Выбейте 2 номера ранга Бронза или выше",
                    targetProgress = 2,
                    rewardAmount = 1_200,
                    categoryIcon = "bronze"
                ),
                Quest(
                    id = "daily_vault_1",
                    period = QuestPeriod.DAILY,
                    title = "В коллекцию",
                    description = "Сохраните 1 номер в сейф",
                    targetProgress = 1,
                    rewardAmount = 600,
                    categoryIcon = "vault"
                ),
                Quest(
                    id = "daily_earn_3k",
                    period = QuestPeriod.DAILY,
                    title = "Дневная выручка",
                    description = "Заработайте 3 000 ₽ на продаже номеров",
                    targetProgress = 3_000,
                    rewardAmount = 1_500,
                    categoryIcon = "money"
                ),
                Quest(
                    id = "weekly_rolls_200",
                    period = QuestPeriod.WEEKLY,
                    title = "Телеком-стахановец",
                    description = "Сделайте 200 круток номеров за неделю",
                    targetProgress = 200,
                    rewardAmount = 10_000,
                    categoryIcon = "roll"
                ),
                Quest(
                    id = "weekly_silver_5",
                    period = QuestPeriod.WEEKLY,
                    title = "Серебряный резерв",
                    description = "Выбейте 5 номеров Серебро или выше",
                    targetProgress = 5,
                    rewardAmount = 15_000,
                    categoryIcon = "silver"
                ),
                Quest(
                    id = "weekly_gold_1",
                    period = QuestPeriod.WEEKLY,
                    title = "Золотая лихорадка",
                    description = "Выбейте хотя бы 1 Золотой VIP-номер",
                    targetProgress = 1,
                    rewardAmount = 25_000,
                    categoryIcon = "gold"
                ),
                Quest(
                    id = "weekly_upgrade_2",
                    period = QuestPeriod.WEEKLY,
                    title = "Модернизация сети",
                    description = "Купите любые 2 улучшения в магазине",
                    targetProgress = 2,
                    rewardAmount = 9_000,
                    categoryIcon = "upgrade"
                ),
                Quest(
                    id = "monthly_rolls_1000",
                    period = QuestPeriod.MONTHLY,
                    title = "Легенда связи РФ",
                    description = "Сделайте 1 000 круток в текущем сезоне",
                    targetProgress = 1_000,
                    rewardAmount = 60_000,
                    categoryIcon = "roll"
                ),
                Quest(
                    id = "monthly_gov_or_plat",
                    period = QuestPeriod.MONTHLY,
                    title = "Спецсвязь и Элита",
                    description = "Выбейте номер Платина, Бриллиант или МВД/ФСБ",
                    targetProgress = 1,
                    rewardAmount = 150_000,
                    categoryIcon = "gov"
                ),
                Quest(
                    id = "monthly_vault_8",
                    period = QuestPeriod.MONTHLY,
                    title = "Золотой архив",
                    description = "Соберите коллекцию из 8 номеров в сейфе",
                    targetProgress = 8,
                    rewardAmount = 40_000,
                    categoryIcon = "vault"
                ),
                Quest(
                    id = "monthly_passive_income",
                    period = QuestPeriod.MONTHLY,
                    title = "Телеком-магнат",
                    description = "Соберите 50 ₽ пассивного дохода за секунду",
                    targetProgress = 50,
                    rewardAmount = 50_000,
                    categoryIcon = "money"
                )
            )
        }
    }
}
