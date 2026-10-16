package com.example.generator

import com.example.model.MultiplierDetail
import com.example.model.PhoneNumber
import com.example.model.RarityTier
import kotlin.random.Random

object PhoneGenerator {

    data class CountryConfig(
        val countryCode: String,
        val countryName: String,
        val countryIso: String,
        val standardPrefixes: List<String>,
        val vipPrefixes: List<String>,
        val operators: List<String>,
        val vipOperators: List<String>
    )

    private val COUNTRIES = listOf(
        CountryConfig(
            countryCode = "+7",
            countryName = "Россия",
            countryIso = "RU",
            standardPrefixes = listOf("903", "905", "916", "925", "926", "967", "985", "909", "977"),
            vipPrefixes = listOf("999", "777", "888", "900", "495", "499"),
            operators = listOf("МТС", "МегаФон", "Билайн", "Т-Мобайл"),
            vipOperators = listOf("YOTA VIP", "МЕГАФОН ELITE", "МТС ПРЕСТИЖ", "СБЕР МОБАЙЛ")
        ),
        CountryConfig(
            countryCode = "+380",
            countryName = "Украина",
            countryIso = "UA",
            standardPrefixes = listOf("50", "67", "93", "73", "97", "63", "68"),
            vipPrefixes = listOf("88", "99", "77", "50", "44"),
            operators = listOf("Kyivstar", "Vodafone UA", "lifecell"),
            vipOperators = listOf("Kyivstar VIP", "Vodafone Black", "lifecell Platinum")
        ),
        CountryConfig(
            countryCode = "+375",
            countryName = "Беларусь",
            countryIso = "BY",
            standardPrefixes = listOf("29", "44", "33", "25"),
            vipPrefixes = listOf("29", "44", "17"),
            operators = listOf("A1 Беларусь", "МТС Беларусь", "life:) BY"),
            vipOperators = listOf("A1 Prime VIP", "МТС Platinum BY")
        ),
        CountryConfig(
            countryCode = "+7",
            countryName = "Казахстан",
            countryIso = "KZ",
            standardPrefixes = listOf("701", "702", "705", "707", "747", "708"),
            vipPrefixes = listOf("777", "700", "701", "7172"),
            operators = listOf("Kcell", "Beeline KZ", "Tele2 KZ", "Altel"),
            vipOperators = listOf("Kcell Elite", "Beeline VIP KZ")
        ),
        CountryConfig(
            countryCode = "+971",
            countryName = "ОАЭ",
            countryIso = "AE",
            standardPrefixes = listOf("50", "52", "54", "55", "56", "58"),
            vipPrefixes = listOf("50", "55", "58", "4"),
            operators = listOf("e& (Etisalat)", "du", "Virgin Mobile UAE"),
            vipOperators = listOf("e& Royal VIP", "du Diamond VIP")
        ),
        CountryConfig(
            countryCode = "+1",
            countryName = "США",
            countryIso = "US",
            standardPrefixes = listOf("212", "310", "415", "702", "305", "312"),
            vipPrefixes = listOf("888", "999", "202", "777"),
            operators = listOf("AT&T", "Verizon", "T-Mobile US"),
            vipOperators = listOf("AT&T Elite VIP", "Verizon Black Card")
        ),
        CountryConfig(
            countryCode = "+49",
            countryName = "Германия",
            countryIso = "DE",
            standardPrefixes = listOf("170", "171", "172", "175", "176", "179"),
            vipPrefixes = listOf("170", "171", "30"),
            operators = listOf("Telekom DE", "Vodafone DE", "O2 DE"),
            vipOperators = listOf("Telekom Magenta VIP", "Vodafone Black DE")
        )
    )

    data class SpecialDept(
        val countryCode: String,
        val countryName: String,
        val countryIso: String,
        val prefix: String,
        val head3: String,
        val title: String,
        val operator: String,
        val isKremlin: Boolean,
        val deptMultiplier: Double
    )

    private val SPECIAL_DEPTS = listOf(
        // Россия
        SpecialDept("+7", "Россия", "RU", "495", "606", "Администрация Президента (Вертушка АТС-1)", "Кремль АТС-1", true, 550.0),
        SpecialDept("+7", "Россия", "RU", "495", "624", "ФСБ России (Лубянка)", "Спецсвязь ФСБ", true, 320.0),
        SpecialDept("+7", "Россия", "RU", "999", "001", "Спецкортеж ФСО России", "Спецсвязь ФСО", true, 380.0),
        SpecialDept("+7", "Россия", "RU", "495", "694", "ГУ МВД по Москве (Петровка, 38)", "Спецсвязь МВД", false, 180.0),
        SpecialDept("+7", "Россия", "RU", "495", "667", "Центральный Аппарат МВД РФ", "Спецсвязь МВД", false, 150.0),
        SpecialDept("+7", "Россия", "RU", "495", "608", "Генеральная Прокуратура РФ", "Госсвязь", false, 140.0),
        SpecialDept("+7", "Россия", "RU", "495", "925", "Следственный Комитет РФ", "Спецсвязь СК", false, 130.0),
        SpecialDept("+7", "Россия", "RU", "495", "112", "Центр управления МЧС РФ", "МЧС Центроспас", false, 110.0),
        // Украина
        SpecialDept("+380", "Украина", "UA", "44", "255", "Офіс Президента (Урядовий спецзв'язок)", "Урядовий зв'язок", true, 490.0),
        SpecialDept("+380", "Украина", "UA", "44", "235", "Служба Безпеки України (СБУ)", "Спецзв'язок СБУ", true, 310.0),
        SpecialDept("+380", "Украина", "UA", "44", "256", "Кабінет Міністрів України", "Госсвязь Украины", false, 160.0),
        // Беларусь
        SpecialDept("+375", "Беларусь", "BY", "17", "222", "Администрация Президента РБ", "Спецсвязь РБ", true, 480.0),
        SpecialDept("+375", "Беларусь", "BY", "17", "219", "КГБ Республики Беларусь", "Связь КГБ", true, 310.0),
        SpecialDept("+375", "Беларусь", "BY", "17", "218", "МВД Республики Беларусь", "Спецсвязь МВД РБ", false, 160.0),
        // Казахстан
        SpecialDept("+7", "Казахстан", "KZ", "7172", "740", "Резиденция Президента Акорда", "Спецсвязь Акорда", true, 500.0),
        SpecialDept("+7", "Казахстан", "KZ", "7172", "761", "КНБ Республики Казахстан", "Спецсвязь КНБ", true, 300.0),
        SpecialDept("+7", "Казахстан", "KZ", "7172", "715", "МВД Республики Казахстан", "МВД РК", false, 150.0),
        // ОАЭ
        SpecialDept("+971", "ОАЭ", "AE", "4", "353", "Royal Court of Dubai (Дворец Эмира)", "Royal Comms Dubai", true, 600.0),
        SpecialDept("+971", "ОАЭ", "AE", "2", "665", "Presidential Court Abu Dhabi", "State VIP UAE", true, 550.0),
        SpecialDept("+971", "ОАЭ", "AE", "4", "229", "Dubai Police HQ (Суперкары полиции)", "Dubai Police VIP", false, 200.0),
        // США
        SpecialDept("+1", "США", "US", "202", "456", "The White House (Белый Дом)", "US Secret Service", true, 580.0),
        SpecialDept("+1", "США", "US", "703", "697", "The Pentagon (Минобороны США)", "DoD Top Secret", true, 350.0),
        SpecialDept("+1", "США", "US", "202", "324", "FBI Headquarters (Штаб ФБР)", "FBI Comms", false, 190.0),
        // Германия
        SpecialDept("+49", "Германия", "DE", "30", "400", "Bundeskanzleramt (Канцелярия ФРГ)", "Regierungsnetz DE", true, 450.0),
        SpecialDept("+49", "Германия", "DE", "30", "227", "Deutscher Bundestag (Бундестаг)", "Bundestag VIP", false, 170.0)
    )

    data class CountryOption(
        val iso: String,
        val name: String,
        val flag: String,
        val code: String,
        val desc: String
    )

    val AVAILABLE_COUNTRIES = listOf(
        CountryOption("AUTO", "Все страны (Авто)", "🌐", "Авто", "Случайный выбор из всех регионов"),
        CountryOption("RU", "Россия", "🇷🇺", "+7", "МТС, МегаФон, Кремль АТС-1, ФСБ"),
        CountryOption("KZ", "Казахстан", "🇰🇿", "+7", "Kcell, Beeline KZ, Акорда, КНБ"),
        CountryOption("UA", "Украина", "🇺🇦", "+380", "Kyivstar, Vodafone, Офіс Президента"),
        CountryOption("BY", "Беларусь", "🇧🇾", "+375", "A1, МТС BY, Администрация Президента"),
        CountryOption("AE", "ОАЭ", "🇦🇪", "+971", "e& (Etisalat), du, Royal Court Dubai"),
        CountryOption("US", "США", "🇺🇸", "+1", "AT&T, Verizon, The White House, Pentagon"),
        CountryOption("DE", "Германия", "🇩🇪", "+49", "Telekom DE, Vodafone, Бундестаг")
    )

    fun getTierBasePrice(rarity: RarityTier): Long {
        return when (rarity) {
            RarityTier.COMMON -> 450L
            RarityTier.BRONZE -> 9_000L
            RarityTier.SILVER -> 90_000L
            RarityTier.GOLD -> 850_000L
            RarityTier.PLATINUM -> 7_000_000L
            RarityTier.DIAMOND -> 60_000_000L
            RarityTier.POLICE_MVD -> 120_000_000L
            RarityTier.FSB_KREMLIN -> 400_000_000L
        }
    }

    private fun selectCountry(): CountryConfig {
        val dice = Random.nextInt(0, 100)
        return when {
            dice < 65 -> COUNTRIES[0] // Россия (65%)
            dice < 75 -> COUNTRIES[3] // Казахстан (10%)
            dice < 83 -> COUNTRIES[1] // Украина (8%)
            dice < 90 -> COUNTRIES[2] // Беларусь (7%)
            dice < 94 -> COUNTRIES[4] // ОАЭ (4%)
            dice < 97 -> COUNTRIES[5] // США (3%)
            else -> COUNTRIES[6]      // Германия (3%)
        }
    }

    fun resolveCountry(targetIso: String?): CountryConfig {
        if (!targetIso.isNullOrBlank() && targetIso != "AUTO") {
            val found = COUNTRIES.find { it.countryIso.equals(targetIso, ignoreCase = true) }
            if (found != null) return found
        }
        return selectCountry()
    }

    fun generate(
        mvdInsiderLevel: Int = 0,
        goldenDbLevel: Int = 0,
        playerLevel: Int = 1,
        quantumScannerLevel: Int = 0,
        presidentialHotlineLevel: Int = 0,
        neuralClusterLevel: Int = 0,
        luckMultiplier: Double = 1.0,
        targetCountryIso: String = "AUTO"
    ): PhoneNumber {
        val roll = Random.nextDouble(100.0)

        // Player level bonus is subtle and balanced
        val levelGovBonus = (playerLevel - 1) * 0.0005
        val levelVipBonus = (playerLevel - 1) * 0.002
        val presBonus = presidentialHotlineLevel * 0.004
        val neuralBonus = neuralClusterLevel * 0.003

        // 1. Department / Police chance (Extremely rare: ~0.008% base chance)
        val mvdBase = 0.008 + (mvdInsiderLevel * 0.005) + levelGovBonus + presBonus + (neuralBonus * 0.3)
        val mvdChance = (mvdBase * luckMultiplier).coerceAtMost(35.0)
        if (roll < mvdChance) {
            val gov = generateGovNumber(quantumScannerLevel, presidentialHotlineLevel, targetCountryIso)
            return if (luckMultiplier > 1.0) applyLuckBoostBonus(gov, luckMultiplier) else gov
        }

        // 2. Curated VIP drop chance (~0.08% base chance)
        val vipBase = 0.08 + (goldenDbLevel * 0.04) + levelVipBonus + (presidentialHotlineLevel * 0.02) + neuralBonus
        val vipChance = (vipBase * luckMultiplier).coerceAtMost(55.0)
        if (roll < (mvdChance + vipChance)) {
            val vip = generateCuratedVipNumber(quantumScannerLevel, targetCountryIso)
            return if (luckMultiplier > 1.0) applyLuckBoostBonus(vip, luckMultiplier) else vip
        }

        // 3. Natural generation with country selection
        val country = resolveCountry(targetCountryIso)
        val vipPrefixBase = 3.0 + (goldenDbLevel * 0.3) + (playerLevel - 1) * 0.02 + (presidentialHotlineLevel * 0.5) + (neuralClusterLevel * 0.5)
        val vipPrefixChance = (vipPrefixBase * luckMultiplier).coerceAtMost(75.0)
        val isVipPrefix = Random.nextDouble(100.0) < vipPrefixChance
        val prefix = if (isVipPrefix) country.vipPrefixes.random() else country.standardPrefixes.random()

        // When 5X luck is active, guarantee 65% chance of rolling rare digit patterns
        val digits = if (luckMultiplier > 1.0 && Random.nextDouble(100.0) < 65.0) {
            generateLuckyPatternDigits()
        } else {
            val d1 = Random.nextInt(0, 10)
            val d2 = Random.nextInt(0, 10)
            val d3 = Random.nextInt(0, 10)
            val d4 = Random.nextInt(0, 10)
            val d5 = Random.nextInt(0, 10)
            val d6 = Random.nextInt(0, 10)
            val d7 = Random.nextInt(0, 10)
            "$d1$d2$d3$d4$d5$d6$d7"
        }

        val natural = analyzeAndBuild(country, prefix, digits, isVipPrefix, quantumScannerLevel)
        return if (luckMultiplier > 1.0) applyLuckBoostBonus(natural, luckMultiplier) else natural
    }

    private fun generateLuckyPatternDigits(): String {
        return when (Random.nextInt(7)) {
            0 -> {
                val d = listOf(9, 8, 7, 5, 0, 1).random()
                "$d$d$d$d$d$d$d"
            }
            1 -> {
                val d = listOf(7, 9, 8, 5, 0, 1).random()
                val tail = String.format("%03d", Random.nextInt(0, 1000))
                if (Random.nextBoolean()) "$d$d$d$d$tail" else "$tail$d$d$d$d"
            }
            2 -> {
                if (Random.nextBoolean()) "1234567" else "9876543"
            }
            3 -> {
                val d = listOf(7, 9, 8, 0, 3).random()
                val mid = Random.nextInt(0, 10)
                "$d$d$d$mid$d$d$d"
            }
            4 -> {
                val d = listOf(7, 9, 8, 5, 0).random()
                val p1 = Random.nextInt(0, 10)
                val p2 = Random.nextInt(0, 10)
                "$d$d$d$p1$p1$p2$p2"
            }
            5 -> {
                val d = Random.nextInt(1, 10)
                if (Random.nextBoolean()) "${d}000000" else "$d$d$d${0}${0}${0}${0}"
            }
            else -> {
                val a = Random.nextInt(0, 10)
                val b = Random.nextInt(0, 10)
                val c = Random.nextInt(0, 10)
                "$a$a$b$b$c$c$a"
            }
        }
    }

    private fun applyLuckBoostBonus(phone: PhoneNumber, luckMultiplier: Double): PhoneNumber {
        val boostFactor = 1.5
        val updatedMultipliers = phone.multipliers.toMutableList()
        updatedMultipliers.add(
            0,
            MultiplierDetail(
                title = "Бонус 5X Удачи (Pity)",
                factor = boostFactor,
                isHighlighted = true
            )
        )
        val newTotalMult = phone.totalMultiplier * boostFactor
        val boostedValue = (phone.value * boostFactor).toLong()
            .coerceIn(phone.rarity.minPrice, (phone.rarity.maxPrice * 1.5).toLong())
        return phone.copy(
            multipliers = updatedMultipliers,
            totalMultiplier = newTotalMult,
            value = boostedValue
        )
    }

    private fun getPrefixMultiplier(country: CountryConfig, prefix: String): Pair<Double, String> {
        return when (prefix) {
            "777" -> Pair(2.4, "Префикс 777 (VIP)")
            "888" -> Pair(2.2, "Префикс 888 (Elite)")
            "999" -> Pair(2.0, "Префикс 999 (VIP)")
            "495" -> Pair(2.0, "Код +7 (495) МСК")
            "900" -> Pair(1.6, "Банковский (900)")
            "499" -> Pair(1.4, "Код +7 (499) МСК")
            "88" -> Pair(2.0, "Префикс 88 (VIP)")
            "99" -> Pair(1.9, "Префикс 99 (Elite)")
            "7172" -> Pair(1.8, "Код Астаны (7172)")
            "202" -> Pair(2.0, "Код Вашингтон DC")
            "212" -> Pair(1.9, "Код Нью-Йорк (212)")
            else -> Pair(1.0, "${country.countryName} ($prefix)")
        }
    }

    private fun generateGovNumber(
        quantumScannerLevel: Int = 0,
        presidentialHotlineLevel: Int = 0,
        targetCountryIso: String = "AUTO"
    ): PhoneNumber {
        val pool = if (targetCountryIso != "AUTO") {
            SPECIAL_DEPTS.filter { it.countryIso.equals(targetCountryIso, ignoreCase = true) }.ifEmpty { SPECIAL_DEPTS }
        } else {
            SPECIAL_DEPTS
        }
        val dept = pool.random()
        val tailPool = listOf("0000", "0101", "0001", "7777", "8888", "9999", "1234", "0707")
        val tail = if (Random.nextInt(100) < 30) tailPool.random() else String.format("%04d", Random.nextInt(0, 10000))
        val digits = dept.head3 + tail
        val rarity = if (dept.isKremlin) RarityTier.FSB_KREMLIN else RarityTier.POLICE_MVD
        val basePrice = getTierBasePrice(rarity)

        val country = COUNTRIES.find { it.countryIso == dept.countryIso } ?: COUNTRIES[0]
        val (prefixFactor, prefixLabel) = getPrefixMultiplier(country, dept.prefix)
        var (comboFactor, comboLabel) = analyzeComboMultiplier(digits)
        var deptFactor = dept.deptMultiplier

        val multipliers = mutableListOf<MultiplierDetail>()
        multipliers.add(MultiplierDetail(prefixLabel, prefixFactor, isHighlighted = prefixFactor > 1.0))
        if (comboFactor > 1.0) {
            if (quantumScannerLevel > 0) {
                comboFactor *= (1.0 + (quantumScannerLevel * 0.20))
            }
            multipliers.add(MultiplierDetail(comboLabel, comboFactor, isHighlighted = comboFactor >= 10.0))
        }
        if (presidentialHotlineLevel > 0 && dept.isKremlin) {
            deptFactor *= (1.0 + (presidentialHotlineLevel * 0.25))
            multipliers.add(MultiplierDetail("${dept.title} (+${presidentialHotlineLevel * 25}% спецканал)", deptFactor, isHighlighted = true))
        } else {
            multipliers.add(MultiplierDetail(dept.title, deptFactor, isHighlighted = true))
        }

        val totalMult = prefixFactor * comboFactor * deptFactor
        val rawPrice = (basePrice * totalMult * (1.0 + Random.nextDouble(-0.03, 0.03))).toLong()
        val finalPrice = rawPrice.coerceIn(rarity.minPrice, rarity.maxPrice)

        return PhoneNumber(
            countryCode = dept.countryCode,
            countryName = dept.countryName,
            countryIso = dept.countryIso,
            prefix = dept.prefix,
            digits = digits,
            formatted = formatNumber(dept.countryCode, dept.prefix, digits),
            operatorName = dept.operator,
            rarity = rarity,
            categoryDescription = dept.title,
            basePrice = basePrice,
            multipliers = multipliers,
            totalMultiplier = totalMult,
            value = finalPrice
        )
    }

    private fun generateCuratedVipNumber(quantumScannerLevel: Int = 0, targetCountryIso: String = "AUTO"): PhoneNumber {
        val country = resolveCountry(targetCountryIso)
        val prefix = country.vipPrefixes.random()
        val operator = country.vipOperators.random()
        val dice = Random.nextInt(0, 100)

        // 4% Diamond, 16% Platinum, 80% Gold
        val (digits, category, rarity, comboFactorRaw) = when {
            dice < 4 -> {
                // Absolute repeated beautiful monochrome (+7 999 999 99 99, 888 888 88 88, 777 777 77 77)
                val d = listOf(9, 8, 7, 0).random()
                val str = "$d$d$d$d$d$d$d"
                val catName = if (d == 9) "Легендарные Девятки ($d$d$d $d$d$d $d$d $d$d)"
                              else if (d == 8) "Золотая Бесконечность ($d$d$d $d$d$d $d$d $d$d)"
                              else "Абсолютный монохром ($d$d$d-$d$d-$d$d)"
                Quad(str, catName, RarityTier.DIAMOND, 45.0)
            }
            dice < 10 -> {
                val isAsc = Random.nextBoolean()
                val str = if (isAsc) "1234567" else "9876543"
                Quad(str, "Идеальная лесенка", RarityTier.PLATINUM, 22.0)
            }
            dice < 18 -> {
                val d = listOf(9, 8, 7, 5, 0).random()
                val diff = (d + Random.nextInt(1, 9)) % 10
                val str = if (Random.nextBoolean()) "$diff$d$d$d$d$d$d" else "$d$d$d$d$d$d$diff"
                Quad(str, "Шесть одинаковых подряд ($d)", RarityTier.PLATINUM, 19.0)
            }
            dice < 26 -> {
                val d = Random.nextInt(1, 10)
                val str = "${d}000000"
                Quad(str, "Круглый миллионник ($d 000 000)", RarityTier.PLATINUM, 16.0)
            }
            dice < 60 -> {
                val d = listOf(9, 8, 7, 5, 0).random()
                val mid = Random.nextInt(0, 10)
                val str = "$d$d$d$mid$d$d$d"
                Quad(str, "Симметричные тройки ($d$d$d-$mid-$d$d$d)", RarityTier.GOLD, 10.5)
            }
            else -> {
                val d = listOf(9, 8, 7, 5, 0).random()
                val p = String.format("%02d", Random.nextInt(10, 99))
                val str = "$d$d$d$p$p"
                Quad(str, "Три топора и дубль ($d$d$d-$p-$p)", RarityTier.GOLD, 8.5)
            }
        }

        val comboFactor = if (quantumScannerLevel > 0) comboFactorRaw * (1.0 + quantumScannerLevel * 0.20) else comboFactorRaw
        val basePrice = getTierBasePrice(rarity)
        val (prefixFactor, prefixLabel) = getPrefixMultiplier(country, prefix)
        val multipliers = mutableListOf(
            MultiplierDetail(prefixLabel, prefixFactor, isHighlighted = prefixFactor > 1.0),
            MultiplierDetail(category, comboFactor, isHighlighted = comboFactor >= 8.0)
        )

        var totalMult = prefixFactor * comboFactor

        if ((prefix == "777" || prefix == "999" || prefix == "888") && digits.contains(prefix)) {
            val synergy = 1.40
            multipliers.add(MultiplierDetail("Синергия префикса и цифр ($prefix+$prefix)", synergy, isHighlighted = true))
            totalMult *= synergy
        }

        val rawPrice = (basePrice * totalMult * (1.0 + Random.nextDouble(-0.02, 0.04))).toLong()
        val (finalRarity, finalPrice) = adjustRarityAndClampPrice(rarity, rawPrice)

        return PhoneNumber(
            countryCode = country.countryCode,
            countryName = country.countryName,
            countryIso = country.countryIso,
            prefix = prefix,
            digits = digits,
            formatted = formatNumber(country.countryCode, prefix, digits),
            operatorName = operator,
            rarity = finalRarity,
            categoryDescription = category,
            basePrice = basePrice,
            multipliers = multipliers,
            totalMultiplier = totalMult,
            value = finalPrice
        )
    }

    private data class Quad(val digits: String, val category: String, val rarity: RarityTier, val comboMultiplier: Double)

    private fun analyzeAndBuild(
        country: CountryConfig,
        prefix: String,
        digits: String,
        isVipPrefix: Boolean,
        quantumScannerLevel: Int = 0
    ): PhoneNumber {
        val operator = if (isVipPrefix) country.vipOperators.random() else country.operators.random()
        val (prefixFactor, prefixLabel) = getPrefixMultiplier(country, prefix)

        var (rarity, category, comboFactor) = evaluateRarityAndMultiplier(digits)
        if (quantumScannerLevel > 0 && comboFactor > 1.0) {
            comboFactor *= (1.0 + quantumScannerLevel * 0.20)
        }

        val basePrice = getTierBasePrice(rarity)
        val multipliers = mutableListOf<MultiplierDetail>()
        multipliers.add(MultiplierDetail(prefixLabel, prefixFactor, isHighlighted = prefixFactor > 1.0))

        if (comboFactor > 1.0) {
            multipliers.add(MultiplierDetail(category, comboFactor, isHighlighted = comboFactor >= 5.0))
        }

        // Synergies: matching prefix and first digits (e.g., 999 with 999)
        if (prefix.length == 3 && digits.startsWith(prefix)) {
            val synergy = 1.35
            multipliers.add(MultiplierDetail("Синергия префикса ($prefix)", synergy, isHighlighted = true))
        }

        if (rarity == RarityTier.COMMON) {
            val minorBonus = detectMinorCommonBonus(digits)
            if (minorBonus != null) {
                multipliers.add(minorBonus)
            }
        }

        var totalMult = 1.0
        for (m in multipliers) {
            totalMult *= m.factor
        }

        val rawPrice = (basePrice * totalMult * (1.0 + Random.nextDouble(-0.02, 0.04))).toLong()
        val (finalRarity, finalPrice) = adjustRarityAndClampPrice(rarity, rawPrice)

        return PhoneNumber(
            countryCode = country.countryCode,
            countryName = country.countryName,
            countryIso = country.countryIso,
            prefix = prefix,
            digits = digits,
            formatted = formatNumber(country.countryCode, prefix, digits),
            operatorName = operator,
            rarity = finalRarity,
            categoryDescription = category,
            basePrice = basePrice,
            multipliers = multipliers,
            totalMultiplier = totalMult,
            value = finalPrice
        )
    }

    private fun adjustRarityAndClampPrice(initialRarity: RarityTier, rawPrice: Long): Pair<RarityTier, Long> {
        val clampedPrice = rawPrice.coerceIn(initialRarity.minPrice, initialRarity.maxPrice)
        return Pair(initialRarity, clampedPrice)
    }

    private fun detectMinorCommonBonus(digits: String): MultiplierDetail? {
        val last2 = digits.takeLast(2)
        val first2 = digits.take(2)
        return when {
            last2[0] == last2[1] -> MultiplierDetail("Пара на конце (${last2})", 1.3, isHighlighted = false)
            first2[0] == first2[1] -> MultiplierDetail("Пара в начале (${first2})", 1.2, isHighlighted = false)
            digits.count { it == '7' } >= 3 -> MultiplierDetail("Счастливые семерки", 1.4, isHighlighted = false)
            digits.count { it == '8' } >= 3 -> MultiplierDetail("Счастливые восьмерки", 1.4, isHighlighted = false)
            digits.count { it == '9' } >= 3 -> MultiplierDetail("Красивые девятки", 1.4, isHighlighted = false)
            else -> null
        }
    }

    private fun analyzeComboMultiplier(digits: String): Pair<Double, String> {
        val (_, label, factor) = evaluateRarityAndMultiplier(digits)
        return Pair(factor, label)
    }

    private fun evaluateRarityAndMultiplier(digits: String): Triple<RarityTier, String, Double> {
        if (digits.length != 7) {
            return Triple(RarityTier.COMMON, "Случайный номер", 1.0)
        }

        // 1. DIAMOND: 7 identical digits (0000000 - 9999999)
        if (digits.all { it == digits[0] }) {
            val d = digits[0]
            val title = if (d == '9') "Легендарные Девятки (999 999 99 99)"
                        else if (d == '8') "Восьмерка Удачи (888 888 88 88)"
                        else if (d == '7') "Семь топоров (777 777 77 77)"
                        else "Абсолютный монохром ($d$d$d-$d$d-$d$d)"
            return Triple(RarityTier.DIAMOND, title, 50.0)
        }

        // 2. PLATINUM: 6 identical digits
        var maxConsecutive = 1
        var curConsecutive = 1
        for (i in 1 until digits.length) {
            if (digits[i] == digits[i - 1]) {
                curConsecutive++
                if (curConsecutive > maxConsecutive) maxConsecutive = curConsecutive
            } else {
                curConsecutive = 1
            }
        }
        if (maxConsecutive == 6) {
            return Triple(RarityTier.PLATINUM, "Шесть одинаковых подряд", 20.0)
        }

        // 3. PLATINUM: Straight ladder 1234567 or 9876543
        val isAscLadder = "0123456789".contains(digits)
        val isDescLadder = "9876543210".contains(digits)
        if (isAscLadder || isDescLadder) {
            return Triple(RarityTier.PLATINUM, "Идеальная лесенка (${if (isAscLadder) "1234567" else "9876543"})", 22.0)
        }

        // 4. PLATINUM: Round million (e.g. 7000000, 5000000)
        if (digits.substring(1) == "000000") {
            return Triple(RarityTier.PLATINUM, "Круглый миллионник (${digits[0]} 000 000)", 18.0)
        }

        // 5. GOLD: 5 consecutive identical digits
        if (maxConsecutive == 5) {
            return Triple(RarityTier.GOLD, "Пятерка подряд", 12.0)
        }

        // 6. GOLD: Double triplets AAABBBX or XAAABBB
        val t1 = digits.substring(0, 3)
        val t2 = digits.substring(3, 6)
        if (t1[0] == t1[1] && t1[1] == t1[2] && t2[0] == t2[1] && t2[1] == t2[2] && t1[0] != t2[0]) {
            return Triple(RarityTier.GOLD, "Двойная тройка ($t1-$t2)", 14.0)
        }

        // 7. GOLD: Quadruple + Triple XXXXYYY
        val q1 = digits.substring(0, 4)
        val q2 = digits.substring(4, 7)
        if (q1[0] == q1[1] && q1[1] == q1[2] && q1[2] == q1[3] && q2[0] == q2[1] && q2[1] == q2[2]) {
            return Triple(RarityTier.GOLD, "Каре и тройка ($q1-$q2)", 15.0)
        }

        // 8. GOLD: Symmetrical triplets (e.g. 777-X-777, 888-X-888)
        val part1 = digits.substring(0, 3)
        val part2 = digits.substring(4, 7)
        if (part1 == part2 && part1[0] == part1[1] && part1[1] == part1[2]) {
            return Triple(RarityTier.GOLD, "Симметричные тройки ($part1)", 9.5)
        }

        // 9. GOLD: Round 100k
        if (digits.substring(2) == "00000") {
            return Triple(RarityTier.GOLD, "Круглые сотни тысяч (${digits.take(2)} 00000)", 8.0)
        }

        // 10. SILVER or BRONZE: 3 consecutive identical digits
        if (maxConsecutive == 3) {
            val d = digits.windowed(3).first { it[0] == it[1] && it[1] == it[2] }[0]
            if (d == '7' || d == '0' || d == '9' || d == '8') {
                return Triple(RarityTier.SILVER, "Тройка элитных цифр ($d$d$d)", 4.5)
            } else {
                return Triple(RarityTier.BRONZE, "Тройка подряд ($d$d$d)", 2.4)
            }
        }

        // 11. SILVER: Double repeating tail pair XX-YY-YY
        val p2 = digits.substring(3, 5)
        val p3 = digits.substring(5, 7)
        if (p2 == p3 && p2[0] != p2[1]) {
            return Triple(RarityTier.SILVER, "Дубль-пара ($p2-$p3)", 3.2)
        }

        // 12. SILVER: Round 10k
        if (digits.substring(3) == "0000") {
            return Triple(RarityTier.SILVER, "Десятитысячник (${digits.take(3)} 0000)", 3.0)
        }

        // 13. BRONZE: Premium double endings (00, 77, 99, 88)
        if (digits.endsWith("00") || digits.endsWith("77") || digits.endsWith("99") || digits.endsWith("88")) {
            return Triple(RarityTier.BRONZE, "Круглый финал (${digits.takeLast(2)})", 2.0)
        }

        // 14. BRONZE: Precise mini-mirror endings (707, 070, 101, 808, 909)
        val tail3 = digits.takeLast(3)
        if (tail3 == "707" || tail3 == "070" || tail3 == "101" || tail3 == "808" || tail3 == "909") {
            return Triple(RarityTier.BRONZE, "Симметричный хвост ($tail3)", 1.8)
        }

        // 15. COMMON: Normal random phone number
        return Triple(RarityTier.COMMON, "Случайный номер", 1.0)
    }

    fun formatNumber(countryCode: String, prefix: String, digits: String): String {
        val cleanDigits = digits.filter { it.isDigit() }.padEnd(7, '0').take(7)
        val safePrefix = prefix.filter { it.isDigit() }.ifEmpty { "999" }
        return when (countryCode) {
            "+380" -> {
                val p1 = cleanDigits.take(3)
                val p2 = cleanDigits.substring(3, 5)
                val p3 = cleanDigits.takeLast(2)
                if (safePrefix == "88" && cleanDigits == "8888888") {
                    "+380 (88) 888-88-88"
                } else if (safePrefix == "99" && cleanDigits == "9999999") {
                    "+380 (99) 999-99-99"
                } else {
                    "+380 ($safePrefix) $p1-$p2-$p3"
                }
            }
            "+375" -> {
                val p1 = cleanDigits.take(3)
                val p2 = cleanDigits.substring(3, 5)
                val p3 = cleanDigits.takeLast(2)
                "+375 ($safePrefix) $p1-$p2-$p3"
            }
            "+971" -> {
                val p1 = cleanDigits.take(3)
                val p2 = cleanDigits.takeLast(4)
                "+971 ($safePrefix) $p1-$p2"
            }
            "+1" -> {
                val p1 = cleanDigits.take(3)
                val p2 = cleanDigits.takeLast(4)
                "+1 ($safePrefix) $p1-$p2"
            }
            "+49" -> {
                val p1 = cleanDigits.take(3)
                val p2 = cleanDigits.substring(3, 5)
                val p3 = cleanDigits.takeLast(2)
                "+49 ($safePrefix) $p1-$p2-$p3"
            }
            else -> {
                val p1 = cleanDigits.take(3)
                val p2 = cleanDigits.substring(3, 5)
                val p3 = cleanDigits.takeLast(2)
                if (safePrefix == "999" && cleanDigits == "9999999") {
                    "+7 999 999 99 99"
                } else if (safePrefix == "888" && cleanDigits == "8888888") {
                    "+7 888 888 88 88"
                } else if (safePrefix == "777" && cleanDigits == "7777777") {
                    "+7 777 777 77 77"
                } else {
                    "+7 ($safePrefix) $p1-$p2-$p3"
                }
            }
        }
    }

    fun formatNumber(prefix: String, digits: String): String {
        return formatNumber("+7", prefix, digits)
    }
}
