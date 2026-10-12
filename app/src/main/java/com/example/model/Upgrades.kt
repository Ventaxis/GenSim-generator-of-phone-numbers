package com.example.model

enum class UpgradeType(
    val id: String,
    val title: String,
    val description: String,
    val iconName: String,
    val basePrice: Long,
    val priceMultiplier: Double,
    val maxLevel: Int
) {
    PASSIVE_INCOME(
        id = "passive_income",
        title = "Серверная АТС",
        description = "Генерирует стабильный доход за счет аренды телеком-линий (+25 ₽/сек за уровень)",
        iconName = "cell_tower",
        basePrice = 800,
        priceMultiplier = 1.8,
        maxLevel = 10
    ),
    MVD_INSIDER(
        id = "mvd_insider",
        title = "Инсайдер в МВД и Спецсвязи",
        description = "Разблокирует и повышает шанс перехвата ведомственных номеров МВД Петровка 38, Лубянки и Кремля",
        iconName = "shield",
        basePrice = 4_500,
        priceMultiplier = 2.4,
        maxLevel = 5
    ),
    GOLDEN_DATABASE(
        id = "golden_db",
        title = "VIP-Архив операторов",
        description = "Повышает вероятность выпадения Золотых, Платиновых и Бриллиантовых серий",
        iconName = "star",
        basePrice = 2_200,
        priceMultiplier = 2.0,
        maxLevel = 5
    ),
    TURBO_ROLL(
        id = "turbo_roll",
        title = "Турбо-переборщик 5G",
        description = "Ускоряет автокрутку номеров до молниеносной скорости (-20мс за уровень)",
        iconName = "bolt",
        basePrice = 1_500,
        priceMultiplier = 2.1,
        maxLevel = 5
    ),
    VAULT_EXPANSION(
        id = "vault_expansion",
        title = "Сейф для SIM-карт",
        description = "Увеличивает вместимость хранилища на +4 слота за каждый уровень",
        iconName = "inventory",
        basePrice = 1_200,
        priceMultiplier = 1.7,
        maxLevel = 8
    );

    fun getPrice(currentLevel: Int): Long {
        if (currentLevel >= maxLevel) return Long.MAX_VALUE
        var price = basePrice.toDouble()
        for (i in 0 until currentLevel) {
            price *= priceMultiplier
        }
        return price.toLong()
    }
}
