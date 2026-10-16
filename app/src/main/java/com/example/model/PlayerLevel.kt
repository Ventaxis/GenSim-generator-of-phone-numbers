package com.example.model

data class PlayerLevelInfo(
    val level: Int,
    val title: String,
    val currentLevelXp: Long,
    val nextLevelXp: Long,
    val progressFraction: Float,
    val totalXp: Long,
    val bonusVaultSlots: Int,
    val sellPriceBonusPercent: Int,
    val luckBonusPercent: Double,
    val nextLevelBonusCash: Long
)

data class LevelPerkPreview(
    val level: Int,
    val title: String,
    val requiredXp: Long,
    val cashReward: Long,
    val bonusVaultSlots: Int,
    val sellPriceBonusPercent: Int,
    val isUnlocked: Boolean
)

object PlayerProgression {

    val TITLES = listOf(
        "Стажёр связи",               // Ур. 1
        "Скупщик симок",              // Ур. 2
        "Оператор АТС",               // Ур. 3
        "Дилер красивых номеров",     // Ур. 4
        "Блатной делец",              // Ур. 5
        "Старший связист",            // Ур. 6
        "Архивариус баз данных",      // Ур. 7
        "Менеджер VIP-линий",         // Ур. 8
        "Специалист Спецсвязи",       // Ур. 9
        "Связист МВД (Петровка)",     // Ур. 10
        "Куратор ведомств",           // Ур. 11
        "Телеком-Магнат",             // Ур. 12
        "Советник Госсвязи",          // Ур. 13
        "Оператор Вертушки АТС-1",    // Ур. 14
        "Легенда Кремлевской АТС",    // Ур. 15
        "Смотритель Госархива",       // Ур. 16
        "Маршал Частот РФ",           // Ур. 17
        "Генерал Спецсвязи",          // Ур. 18
        "Властелин Вышек 5G",         // Ур. 19
        "Абсолютный Телеком-Барон"    // Ур. 20+
    )

    fun getXpThreshold(level: Int): Long {
        if (level <= 1) return 0L
        // Level curve:
        // L1: 0, L2: 300, L3: 800, L4: 1600, L5: 2800, L6: 4500, L7: 6800, etc.
        var total = 0L
        for (lvl in 1 until level) {
            total += (lvl * 200L) + (lvl * lvl * 40L)
        }
        return total
    }

    fun getLevelFromXp(totalXp: Long): Int {
        var lvl = 1
        while (totalXp >= getXpThreshold(lvl + 1)) {
            lvl++
            if (lvl >= 50) break
        }
        return lvl
    }

    fun getLevelInfo(totalXp: Long): PlayerLevelInfo {
        val level = getLevelFromXp(totalXp)
        val titleIdx = (level - 1).coerceIn(0, TITLES.lastIndex)
        val title = TITLES[titleIdx]

        val currentThreshold = getXpThreshold(level)
        val nextThreshold = getXpThreshold(level + 1)
        val needed = (nextThreshold - currentThreshold).coerceAtLeast(1L)
        val progress = (totalXp - currentThreshold).coerceIn(0L, needed)
        val fraction = (progress.toFloat() / needed.toFloat()).coerceIn(0f, 1f)

        val bonusSlots = (level - 1) * 2
        val sellBonus = (level - 1) * 4
        val luckBonus = (level - 1) * 0.2
        val nextCash = (level + 1) * 3_000L

        return PlayerLevelInfo(
            level = level,
            title = title,
            currentLevelXp = progress,
            nextLevelXp = needed,
            progressFraction = fraction,
            totalXp = totalXp,
            bonusVaultSlots = bonusSlots,
            sellPriceBonusPercent = sellBonus,
            luckBonusPercent = luckBonus,
            nextLevelBonusCash = nextCash
        )
    }

    fun getXpRewardForRarity(rarity: RarityTier): Long {
        return when (rarity) {
            RarityTier.COMMON -> 5L
            RarityTier.BRONZE -> 30L
            RarityTier.SILVER -> 120L
            RarityTier.GOLD -> 450L
            RarityTier.PLATINUM -> 1_500L
            RarityTier.DIAMOND -> 4_000L
            RarityTier.POLICE_MVD -> 5_000L
            RarityTier.FSB_KREMLIN -> 10_000L
        }
    }

    /**
     * Calculates rich XP reward for a generated phone number.
     * High-value numbers and rare multipliers yield massive XP chunks!
     */
    fun getXpRewardForNumber(number: PhoneNumber): Long {
        val rarityBase = getXpRewardForRarity(number.rarity)
        // Bonus for high market price: 1 XP per 200 ₽ value
        val valueBonus = (number.value / 200L).coerceIn(0L, 50_000L)
        // Bonus for high multiplier combo:
        val multBonus = if (number.totalMultiplier >= 5.0) {
            (number.totalMultiplier * 15.0).toLong().coerceAtMost(10_000L)
        } else 0L

        return rarityBase + valueBonus + multBonus
    }

    fun getRoadmap(currentLevel: Int): List<LevelPerkPreview> {
        val list = mutableListOf<LevelPerkPreview>()
        for (lvl in 1..20) {
            val titleIdx = (lvl - 1).coerceIn(0, TITLES.lastIndex)
            val title = TITLES[titleIdx]
            val xpReq = getXpThreshold(lvl)
            val cash = lvl * 3_000L
            val slots = (lvl - 1) * 2
            val sellBonus = (lvl - 1) * 4
            list.add(
                LevelPerkPreview(
                    level = lvl,
                    title = title,
                    requiredXp = xpReq,
                    cashReward = cash,
                    bonusVaultSlots = slots,
                    sellPriceBonusPercent = sellBonus,
                    isUnlocked = lvl <= currentLevel
                )
            )
        }
        return list
    }
}
