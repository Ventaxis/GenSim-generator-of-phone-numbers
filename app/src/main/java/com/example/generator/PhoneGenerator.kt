package com.example.generator

import com.example.model.MultiplierDetail
import com.example.model.PhoneNumber
import com.example.model.RarityTier
import kotlin.random.Random

object PhoneGenerator {

    private val STANDARD_PREFIXES = listOf("903", "905", "916", "925", "926", "967", "985", "909", "977")
    private val VIP_PREFIXES = listOf("999", "777", "900", "495", "499")

    data class SpecialDept(
        val prefix: String,
        val head3: String,
        val title: String,
        val operator: String,
        val isKremlin: Boolean,
        val deptMultiplier: Double
    )

    private val SPECIAL_DEPTS = listOf(
        SpecialDept("495", "694", "ГУ МВД по Москве (Петровка, 38)", "Спецсвязь МВД", false, 180.0),
        SpecialDept("495", "667", "Центральный Аппарат МВД РФ", "Спецсвязь МВД", false, 150.0),
        SpecialDept("495", "608", "Генеральная Прокуратура РФ", "Госсвязь", false, 140.0),
        SpecialDept("495", "925", "Следственный Комитет РФ", "Спецсвязь СК", false, 130.0),
        SpecialDept("495", "624", "ФСБ России (Лубянка)", "Спецсвязь ФСБ", true, 320.0),
        SpecialDept("495", "606", "Администрация Президента (Вертушка АТС-1)", "Кремль АТС-1", true, 550.0),
        SpecialDept("999", "001", "Спецкортеж ФСО России", "Спецсвязь ФСО", true, 380.0),
        SpecialDept("495", "112", "Центр управления МЧС РФ", "МЧС Центроспас", false, 110.0)
    )

    fun generate(mvdInsiderLevel: Int = 0, goldenDbLevel: Int = 0, playerLevel: Int = 1): PhoneNumber {
        val basePrice = 50L
        val roll = Random.nextDouble(100.0)

        // Player level bonus is subtle and balanced
        val levelGovBonus = (playerLevel - 1) * 0.002
        val levelVipBonus = (playerLevel - 1) * 0.010

        // 1. Department / Police chance (Genuinely Rare! Base 0.02%, scales with upgrade up to ~0.095%)
        val mvdChance = 0.02 + (mvdInsiderLevel * 0.015) + levelGovBonus
        if (roll < mvdChance) {
            return generateGovNumber(basePrice)
        }

        // 2. Curated VIP drop chance (Base 0.25%, scales up to ~0.85% at max upgrade)
        val vipChance = 0.25 + (goldenDbLevel * 0.12) + levelVipBonus
        if (roll < (mvdChance + vipChance)) {
            return generateCuratedVipNumber(basePrice)
        }

        // 3. Natural generation: 97% standard prefixes, 3% chance of VIP prefix
        val vipPrefixChance = 3.0 + (goldenDbLevel * 0.5) + (playerLevel - 1) * 0.05
        val prefix = if (Random.nextDouble(100.0) < vipPrefixChance) {
            VIP_PREFIXES.random()
        } else {
            STANDARD_PREFIXES.random()
        }

        val d1 = Random.nextInt(0, 10)
        val d2 = Random.nextInt(0, 10)
        val d3 = Random.nextInt(0, 10)
        val d4 = Random.nextInt(0, 10)
        val d5 = Random.nextInt(0, 10)
        val d6 = Random.nextInt(0, 10)
        val d7 = Random.nextInt(0, 10)
        val digits = "$d1$d2$d3$d4$d5$d6$d7"

        return analyzeAndBuild(prefix, digits, basePrice)
    }

    private fun getPrefixMultiplier(prefix: String): Pair<Double, String> {
        return when (prefix) {
            "777" -> Pair(2.8, "Префикс 777")
            "495" -> Pair(2.2, "Регион +7 (495) МСК")
            "999" -> Pair(1.8, "YOTA VIP (999)")
            "900" -> Pair(1.6, "Банковский (900)")
            "499" -> Pair(1.4, "Регион +7 (499)")
            else -> Pair(1.0, "Федеральный ($prefix)")
        }
    }

    private fun generateGovNumber(basePrice: Long): PhoneNumber {
        val dept = SPECIAL_DEPTS.random()
        val tailPool = listOf("0000", "0101", "0001", "7777", "1111", "9999", "1234", "0707")
        val tail = if (Random.nextInt(100) < 30) tailPool.random() else String.format("%04d", Random.nextInt(0, 10000))
        val digits = dept.head3 + tail
        val rarity = if (dept.isKremlin) RarityTier.FSB_KREMLIN else RarityTier.POLICE_MVD

        val (prefixFactor, prefixLabel) = getPrefixMultiplier(dept.prefix)
        val (comboFactor, comboLabel) = analyzeComboMultiplier(digits)
        val deptFactor = dept.deptMultiplier

        val multipliers = mutableListOf<MultiplierDetail>()
        multipliers.add(MultiplierDetail(prefixLabel, prefixFactor, isHighlighted = prefixFactor > 1.0))
        if (comboFactor > 1.0) {
            multipliers.add(MultiplierDetail(comboLabel, comboFactor, isHighlighted = comboFactor >= 10.0))
        }
        multipliers.add(MultiplierDetail(dept.title, deptFactor, isHighlighted = true))

        val totalMult = prefixFactor * comboFactor * deptFactor
        val rawPrice = (basePrice * totalMult * (1.0 + Random.nextDouble(-0.04, 0.04))).toLong()
        val finalPrice = rawPrice.coerceIn(rarity.minPrice, rarity.maxPrice)

        return PhoneNumber(
            prefix = dept.prefix,
            digits = digits,
            formatted = formatNumber(dept.prefix, digits),
            operatorName = dept.operator,
            rarity = rarity,
            categoryDescription = dept.title,
            basePrice = basePrice,
            multipliers = multipliers,
            totalMultiplier = totalMult,
            value = finalPrice
        )
    }

    private fun generateCuratedVipNumber(basePrice: Long): PhoneNumber {
        val prefix = VIP_PREFIXES.random()
        val operator = getOperatorForPrefix(prefix)
        val dice = Random.nextInt(0, 100)

        // 3% Diamond, 15% Platinum, 82% Gold
        val (digits, category, rarity, comboFactor) = when {
            dice < 3 -> {
                val d = Random.nextInt(0, 10)
                val str = "$d$d$d$d$d$d$d"
                Quad(str, "Абсолютный монохром ($d$d$d-$d$d-$d$d)", RarityTier.DIAMOND, 140.0)
            }
            dice < 10 -> {
                val isAsc = Random.nextBoolean()
                val str = if (isAsc) "1234567" else "9876543"
                Quad(str, "Идеальная лесенка", RarityTier.PLATINUM, 45.0)
            }
            dice < 18 -> {
                val d = Random.nextInt(0, 10)
                val diff = (d + Random.nextInt(1, 9)) % 10
                val str = if (Random.nextBoolean()) "$diff$d$d$d$d$d$d" else "$d$d$d$d$d$d$diff"
                Quad(str, "Шесть одинаковых подряд", RarityTier.PLATINUM, 50.0)
            }
            dice < 40 -> {
                val d = Random.nextInt(1, 10)
                val str = "${d}000000"
                Quad(str, "Круглый миллионник ($d 000 000)", RarityTier.GOLD, 18.0)
            }
            dice < 70 -> {
                val d = Random.nextInt(0, 10)
                val mid = Random.nextInt(0, 10)
                val str = "$d$d$d$mid$d$d$d"
                Quad(str, "Симметричные тройки ($d$d$d-$mid-$d$d$d)", RarityTier.GOLD, 14.0)
            }
            else -> {
                val d = listOf(7, 9, 8, 5, 0).random()
                val p = String.format("%02d", Random.nextInt(10, 99))
                val str = "$d$d$d$p$p"
                Quad(str, "Три топора и дубль ($d$d$d-$p-$p)", RarityTier.GOLD, 12.0)
            }
        }

        val (prefixFactor, prefixLabel) = getPrefixMultiplier(prefix)
        val multipliers = listOf(
            MultiplierDetail(prefixLabel, prefixFactor, isHighlighted = prefixFactor > 1.0),
            MultiplierDetail(category, comboFactor, isHighlighted = comboFactor >= 10.0)
        )
        val totalMult = prefixFactor * comboFactor
        val rawPrice = (basePrice * totalMult * (1.0 + Random.nextDouble(-0.04, 0.04))).toLong()
        val finalPrice = rawPrice.coerceIn(rarity.minPrice, rarity.maxPrice)

        return PhoneNumber(
            prefix = prefix,
            digits = digits,
            formatted = formatNumber(prefix, digits),
            operatorName = "$operator VIP",
            rarity = rarity,
            categoryDescription = category,
            basePrice = basePrice,
            multipliers = multipliers,
            totalMultiplier = totalMult,
            value = finalPrice
        )
    }

    private data class Quad(val digits: String, val category: String, val rarity: RarityTier, val comboMultiplier: Double)

    private fun analyzeAndBuild(prefix: String, digits: String, basePrice: Long): PhoneNumber {
        val operator = getOperatorForPrefix(prefix)
        val (prefixFactor, prefixLabel) = getPrefixMultiplier(prefix)

        // Rare natural special department match
        if (prefix == "495" && Random.nextInt(100) < 5) {
            val head3 = digits.substring(0, 3)
            val dept = SPECIAL_DEPTS.find { it.prefix == prefix && it.head3 == head3 }
            if (dept != null) {
                val rarity = if (dept.isKremlin) RarityTier.FSB_KREMLIN else RarityTier.POLICE_MVD
                val (comboFactor, comboLabel) = analyzeComboMultiplier(digits)
                val deptFactor = dept.deptMultiplier

                val multipliers = mutableListOf<MultiplierDetail>()
                multipliers.add(MultiplierDetail(prefixLabel, prefixFactor, isHighlighted = true))
                if (comboFactor > 1.0) {
                    multipliers.add(MultiplierDetail(comboLabel, comboFactor, isHighlighted = true))
                }
                multipliers.add(MultiplierDetail(dept.title, deptFactor, isHighlighted = true))

                val totalMult = prefixFactor * comboFactor * deptFactor
                val rawPrice = (basePrice * totalMult * (1.0 + Random.nextDouble(-0.04, 0.04))).toLong()
                val finalPrice = rawPrice.coerceIn(rarity.minPrice, rarity.maxPrice)

                return PhoneNumber(
                    prefix = prefix,
                    digits = digits,
                    formatted = formatNumber(prefix, digits),
                    operatorName = dept.operator,
                    rarity = rarity,
                    categoryDescription = dept.title,
                    basePrice = basePrice,
                    multipliers = multipliers,
                    totalMultiplier = totalMult,
                    value = finalPrice
                )
            }
        }

        val (rarity, category, comboFactor) = evaluateRarityAndMultiplier(digits)
        val multipliers = mutableListOf<MultiplierDetail>()
        if (prefixFactor > 1.0) {
            multipliers.add(MultiplierDetail(prefixLabel, prefixFactor, isHighlighted = true))
        } else {
            multipliers.add(MultiplierDetail(prefixLabel, prefixFactor, isHighlighted = false))
        }

        if (comboFactor > 1.0) {
            multipliers.add(MultiplierDetail(category, comboFactor, isHighlighted = comboFactor >= 4.0))
        } else {
            multipliers.add(MultiplierDetail("Обычный набор цифр", 1.0, isHighlighted = false))
        }

        val totalMult = prefixFactor * comboFactor
        val rawPrice = (basePrice * totalMult * (1.0 + Random.nextDouble(-0.04, 0.04))).toLong()
        val finalPrice = rawPrice.coerceIn(rarity.minPrice, rarity.maxPrice)

        return PhoneNumber(
            prefix = prefix,
            digits = digits,
            formatted = formatNumber(prefix, digits),
            operatorName = operator,
            rarity = rarity,
            categoryDescription = category,
            basePrice = basePrice,
            multipliers = multipliers,
            totalMultiplier = totalMult,
            value = finalPrice
        )
    }

    private fun analyzeComboMultiplier(digits: String): Pair<Double, String> {
        val (_, cat, factor) = evaluateRarityAndMultiplier(digits)
        return Pair(factor, cat)
    }

    private fun evaluateRarityAndMultiplier(digits: String): Triple<RarityTier, String, Double> {
        // 1. DIAMOND: 7 identical digits (1 in 1,000,000)
        if (digits.all { it == digits[0] }) {
            return Triple(RarityTier.DIAMOND, "7 одинаковых цифр подряд", 120.0)
        }

        // Consecutive repetition count
        var maxConsecutive = 1
        var currConsecutive = 1
        for (i in 1 until digits.length) {
            if (digits[i] == digits[i - 1]) {
                currConsecutive++
                if (currConsecutive > maxConsecutive) maxConsecutive = currConsecutive
            } else {
                currConsecutive = 1
            }
        }

        // 2. DIAMOND: 6 identical digits in a row
        if (maxConsecutive >= 6) {
            return Triple(RarityTier.DIAMOND, "6 одинаковых цифр подряд", 80.0)
        }

        // 3. PLATINUM: 5 identical digits in a row
        if (maxConsecutive == 5) {
            return Triple(RarityTier.PLATINUM, "5 одинаковых цифр подряд", 38.0)
        }

        // 4. PLATINUM: Straight ladders
        if (digits == "1234567" || digits == "2345678" || digits == "3456789" ||
            digits == "7654321" || digits == "8765432" || digits == "9876543"
        ) {
            return Triple(RarityTier.PLATINUM, "Идеальная лесенка", 35.0)
        }

        // 5. PLATINUM: Round million
        if (digits.substring(1) == "000000") {
            return Triple(RarityTier.PLATINUM, "Круглый миллионник", 32.0)
        }

        // 6. GOLD: 4 identical digits in a row
        if (maxConsecutive == 4) {
            val d = digits.windowed(4).first { it[0] == it[1] && it[1] == it[2] && it[2] == it[3] }[0]
            return Triple(RarityTier.GOLD, "Четверка подряд ($d$d$d$d)", 14.0)
        }

        // 7. GOLD: Full 7-digit palindrome
        if (digits[0] == digits[6] && digits[1] == digits[5] && digits[2] == digits[4]) {
            return Triple(RarityTier.GOLD, "Зеркало-палиндром", 12.0)
        }

        // 8. GOLD: Symmetrical 777-X-777 or identical triplets
        val part1 = digits.substring(0, 3)
        val part2 = digits.substring(4, 7)
        if (part1 == part2 && part1[0] == part1[1] && part1[1] == part1[2]) {
            return Triple(RarityTier.GOLD, "Симметричные тройки ($part1)", 12.0)
        }

        // 9. GOLD: Round 100k
        if (digits.substring(2) == "00000") {
            return Triple(RarityTier.GOLD, "Круглые сотни тысяч", 10.0)
        }

        // 10. SILVER: 3 consecutive identical digits (e.g. 777, 000, 999)
        if (maxConsecutive == 3) {
            val d = digits.windowed(3).first { it[0] == it[1] && it[1] == it[2] }[0]
            return Triple(RarityTier.SILVER, "Тройка подряд ($d$d$d)", 4.5)
        }

        // 11. SILVER: Double repeating tail pair XX-YY-YY
        val p2 = digits.substring(3, 5)
        val p3 = digits.substring(5, 7)
        if (p2 == p3 && p2[0] != p2[1]) {
            return Triple(RarityTier.SILVER, "Дубль-пара ($p2-$p3)", 3.8)
        }

        // 12. SILVER: Round 10k
        if (digits.substring(3) == "0000") {
            return Triple(RarityTier.SILVER, "Десятитысячник", 3.5)
        }

        // 13. BRONZE: Premium double endings (00, 77, 88, 99)
        if (digits.endsWith("00") || digits.endsWith("77") || digits.endsWith("88") || digits.endsWith("99")) {
            return Triple(RarityTier.BRONZE, "Круглый финал (${digits.takeLast(2)})", 2.2)
        }

        // 14. BRONZE: Mini-mirror tail (e.g. 707, 101, 808)
        if (digits[4] == digits[6] && digits[4] != digits[5]) {
            return Triple(RarityTier.BRONZE, "Симметричный хвост (${digits.substring(4)})", 1.8)
        }

        // 15. COMMON: Normal random phone number
        return Triple(RarityTier.COMMON, "Случайный номер", 1.0)
    }

    fun formatNumber(prefix: String, digits: String): String {
        val safeDigits = if (digits.length == 7) digits else digits.padEnd(7, '0').take(7)
        val p1 = safeDigits.substring(0, 3)
        val p2 = safeDigits.substring(3, 5)
        val p3 = safeDigits.substring(5, 7)
        return "+7 ($prefix) $p1-$p2-$p3"
    }

    private fun getOperatorForPrefix(prefix: String): String {
        return when (prefix) {
            "999" -> "YOTA VIP"
            "777" -> "МЕГАФОН ELITE"
            "900" -> "СБЕР МОБАЙЛ"
            "495" -> "МОСКВА ДИРЕКТ"
            "499" -> "МОСКВА ЦЕНТР"
            "916", "985" -> "МТС ПРЕСТИЖ"
            "925", "926" -> "МЕГАФОН"
            "903", "905", "909" -> "БИЛАЙН"
            "977" -> "Т-МОБАЙЛ"
            else -> "СОТОВЫЙ РФ"
        }
    }
}
