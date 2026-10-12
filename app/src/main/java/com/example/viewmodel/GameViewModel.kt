package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.GamePreferences
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

class GameViewModel(application: Application) : AndroidViewModel(application) {

    val rollCost: Long = 50

    private val db = AppDatabase.getInstance(application)
    private val simDao = db.savedSimDao()
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
        AutoSellConfig(
            minKeepPrice = 1_800,
            autoSellBelowRarity = RarityTier.SILVER
        )
    )
    val autoSellConfig: StateFlow<AutoSellConfig> = _autoSellConfig.asStateFlow()

    private val savedStats = prefs.loadStats()
    private val _stats = MutableStateFlow(
        GameStats(
            totalRolls = savedStats.first,
            totalEarned = savedStats.second
        )
    )
    val stats: StateFlow<GameStats> = _stats.asStateFlow()

    private val _quests = MutableStateFlow<List<Quest>>(loadQuestsWithSavedProgress())
    val quests: StateFlow<List<Quest>> = _quests.asStateFlow()

    private val _notification = MutableStateFlow<String?>(null)
    val notification: StateFlow<String?> = _notification.asStateFlow()

    private val _events = MutableSharedFlow<GameEvent>()
    val events: SharedFlow<GameEvent> = _events.asSharedFlow()

    private var autoRollJob: Job? = null

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

    init {
        // Collect saved SIMs from Room Database and ensure bidirectional synchronization
        viewModelScope.launch {
            simDao.getAllSavedSims().collect { entities ->
                if (entities.isNotEmpty()) {
                    val domainList = entities.map { it.toDomain() }
                    _inventory.value = domainList
                    prefs.saveInventoryBackup(domainList)
                    if (_activeSim.value == null) {
                        _activeSim.value = domainList.first()
                        prefs.saveActiveSim(domainList.first())
                    }
                } else if (_inventory.value.isNotEmpty()) {
                    // Populate Room from backup if Room was wiped or is cold
                    _inventory.value.forEach { sim ->
                        simDao.insertSim(SavedSimEntity.fromDomain(sim))
                    }
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
                    prefs.saveStats(_stats.value.totalRolls, _stats.value.totalEarned)
                    updateQuestProgress("monthly_passive_income", income.toInt())
                }
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
        prefs.saveStats(_stats.value.totalRolls, _stats.value.totalEarned)

        val mvdLvl = _upgrades.value[UpgradeType.MVD_INSIDER.id] ?: 0
        val goldLvl = _upgrades.value[UpgradeType.GOLDEN_DATABASE.id] ?: 0
        val generated = PhoneGenerator.generate(mvdLvl, goldLvl, _levelInfo.value.level)

        _currentNumber.value = generated
        _isCurrentSaved.value = false
        prefs.saveCurrentNumber(generated)

        // Add to inventory so player can see it in Inventory anytime
        val currentList = _inventory.value.toMutableList()
        currentList.add(0, generated)
        if (currentList.size > 200) {
            val oldestUnsavedIndex = currentList.indexOfLast { !it.isInVault && it.id != _activeSim.value?.id }
            if (oldestUnsavedIndex != -1) {
                val removed = currentList.removeAt(oldestUnsavedIndex)
                viewModelScope.launch { simDao.deleteSimById(removed.id) }
            }
        }
        _inventory.value = currentList
        prefs.saveInventoryBackup(currentList)
        viewModelScope.launch {
            simDao.insertSim(SavedSimEntity.fromDomain(generated))
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
        _balance.update { it + current.value }
        prefs.saveBalance(_balance.value)
        _stats.update { it.copy(totalEarned = it.totalEarned + current.value) }
        prefs.saveStats(_stats.value.totalRolls, _stats.value.totalEarned)
        showNotification("Продано: +${formatMoney(current.value)}")
        updateQuestProgress("daily_earn_3k", current.value.toInt())

        val updated = _inventory.value.filter { it.id != current.id }
        _inventory.value = updated
        prefs.saveInventoryBackup(updated)
        viewModelScope.launch {
            simDao.deleteSimById(current.id)
        }
        _currentNumber.value = null
        _isCurrentSaved.value = true
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
        val idx = updated.indexOfFirst { it.id == current.id }
        if (idx != -1) {
            updated[idx] = updatedCurrent
        } else {
            updated.add(0, updatedCurrent)
        }
        _inventory.value = updated
        prefs.saveInventoryBackup(updated)

        viewModelScope.launch {
            simDao.insertSim(SavedSimEntity.fromDomain(updatedCurrent))
        }

        showNotification("Сохранено в сейф! 🔒")
        updateQuestProgress("daily_vault_1", 1)
        updateQuestProgress("monthly_vault_8", vaultCount, isAbsolute = true)
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
                simDao.updateVaultStatus(number.id, true)
            }
            if (_currentNumber.value?.id == number.id) {
                _currentNumber.value = _currentNumber.value?.copy(isInVault = true)
                _isCurrentSaved.value = true
            }
            showNotification("Номер добавлен в сейф! 🔒")
            updateQuestProgress("daily_vault_1", 1)
            updateQuestProgress("monthly_vault_8", vaultCount, isAbsolute = true)
        } else {
            val updated = _inventory.value.map {
                if (it.id == number.id) it.copy(isInVault = false) else it
            }
            _inventory.value = updated
            prefs.saveInventoryBackup(updated)
            viewModelScope.launch {
                simDao.updateVaultStatus(number.id, false)
            }
            if (_currentNumber.value?.id == number.id) {
                _currentNumber.value = _currentNumber.value?.copy(isInVault = false)
            }
            showNotification("Номер извлечён из сейфа")
        }
    }

    fun sellFromInventory(number: PhoneNumber) {
        val updated = _inventory.value.filter { it.id != number.id }
        _inventory.value = updated
        prefs.saveInventoryBackup(updated)

        viewModelScope.launch {
            simDao.deleteSimById(number.id)
        }
        if (_activeSim.value?.id == number.id) {
            val nextSim = _inventory.value.firstOrNull { it.id != number.id } ?: _currentNumber.value
            _activeSim.value = nextSim
            prefs.saveActiveSim(nextSim)
        }
        if (_currentNumber.value?.id == number.id) {
            _currentNumber.value = null
        }
        _balance.update { it + number.value }
        prefs.saveBalance(_balance.value)
        _stats.update { it.copy(totalEarned = it.totalEarned + number.value) }
        prefs.saveStats(_stats.value.totalRolls, _stats.value.totalEarned)
        showNotification("SIM продана за ${formatMoney(number.value)}")
        updateQuestProgress("daily_earn_3k", number.value.toInt())
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
            toSellIds.forEach { simDao.deleteSimById(it) }
        }
        if (_currentNumber.value?.id in toSellIds) {
            _currentNumber.value = null
        }
        _balance.update { it + totalGain }
        prefs.saveBalance(_balance.value)
        _stats.update { it.copy(totalEarned = it.totalEarned + totalGain) }
        prefs.saveStats(_stats.value.totalRolls, _stats.value.totalEarned)
        showNotification("Продано ${itemsToSell.size} обычных номеров (+${formatMoney(totalGain)})")
        updateQuestProgress("daily_earn_3k", totalGain.toInt())
    }

    fun setActiveSim(number: PhoneNumber) {
        _activeSim.value = number
        prefs.saveActiveSim(number)
        showNotification("Установлена как основная SIM в телефон!")
        viewModelScope.launch {
            _events.emit(GameEvent.Haptic(true))
        }
    }

    fun toggleAutoRoll() {
        val newState = !_autoSellConfig.value.isAutoRolling
        _autoSellConfig.update { it.copy(isAutoRolling = newState) }

        if (newState) {
            startAutoRoll()
        } else {
            stopAutoRoll()
        }
    }

    fun updateAutoSellConfig(config: AutoSellConfig) {
        _autoSellConfig.value = config
    }

    private fun startAutoRoll() {
        autoRollJob?.cancel()
        autoRollJob = viewModelScope.launch {
            val turboLvl = _upgrades.value[UpgradeType.TURBO_ROLL.id] ?: 0
            val interval = (130L - (turboLvl * 18L)).coerceAtLeast(40L)

            while (_autoSellConfig.value.isAutoRolling) {
                if (_balance.value < rollCost) {
                    showNotification("Автокрутка: закончились средства")
                    stopAutoRoll()
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
                    saveCurrentToVault()
                    stopAutoRoll()
                    break
                }

                val keepCondition = generated.rarity.ordinal >= _autoSellConfig.value.autoSellBelowRarity.ordinal &&
                        generated.value >= _autoSellConfig.value.minKeepPrice

                if (keepCondition) {
                    val shouldVault = _autoSellConfig.value.autoSaveKeptToInventory && vaultCount < vaultCapacity
                    val simToKeep = if (shouldVault) generated.copy(isInVault = true) else generated
                    simDao.insertSim(SavedSimEntity.fromDomain(simToKeep))
                    val updated = _inventory.value.toMutableList()
                    if (!updated.any { it.id == simToKeep.id }) {
                        updated.add(0, simToKeep)
                        _inventory.value = updated
                        prefs.saveInventoryBackup(updated)
                    }
                    if (shouldVault) {
                        showNotification("Сохранен в сейф: ${generated.formatted}")
                        updateQuestProgress("daily_vault_1", 1)
                        updateQuestProgress("monthly_vault_8", vaultCount, isAbsolute = true)
                    } else {
                        showNotification("В инвентаре: ${generated.formatted}")
                    }
                } else {
                    _balance.update { it + generated.value }
                    prefs.saveBalance(_balance.value)
                    _stats.update { it.copy(totalEarned = it.totalEarned + generated.value) }
                    updateQuestProgress("daily_earn_3k", generated.value.toInt())
                }

                delay(interval)
            }
        }
    }

    private fun stopAutoRoll() {
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

        viewModelScope.launch {
            _events.emit(GameEvent.Haptic(false))
        }
    }

    fun claimQuestReward(questId: String) {
        val quest = _quests.value.find { it.id == questId } ?: return
        if (!quest.isCompleted || quest.isClaimed) return

        _quests.update { list ->
            list.map { if (it.id == questId) it.copy(isClaimed = true) else it }
        }
        prefs.saveQuests(_quests.value)

        _balance.update { it + quest.rewardAmount }
        prefs.saveBalance(_balance.value)
        _stats.update { it.copy(totalEarned = it.totalEarned + quest.rewardAmount) }
        prefs.saveStats(_stats.value.totalRolls, _stats.value.totalEarned)

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

    private fun updateQuestProgress(questId: String, delta: Int, isAbsolute: Boolean = false) {
        _quests.update { list ->
            list.map { q ->
                if (q.id == questId && !q.isClaimed) {
                    val newProg = if (isAbsolute) delta else (q.currentProgress + delta)
                    q.copy(currentProgress = newProg.coerceAtMost(q.targetProgress))
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
        prefs.saveStats(_stats.value.totalRolls, _stats.value.totalEarned)
    }

    private fun loadQuestsWithSavedProgress(): List<Quest> {
        val initial = createInitialQuests()
        val saved = prefs.loadQuestsProgress()
        if (saved.isEmpty()) return initial

        return initial.map { q ->
            val savedProgress = saved[q.id]
            if (savedProgress != null) {
                q.copy(
                    currentProgress = savedProgress.first,
                    isClaimed = savedProgress.second
                )
            } else q
        }
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
