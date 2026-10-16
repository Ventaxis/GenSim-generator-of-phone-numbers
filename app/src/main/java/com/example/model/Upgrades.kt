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
    INVENTORY_CAPACITY(
        id = "inventory_capacity",
        title = "Вместимость инвентаря",
        description = "Расширяет лимит инвентаря (базово 100 номеров, улучшается до 10 000)",
        iconName = "inventory",
        basePrice = 3_000,
        priceMultiplier = 2.1,
        maxLevel = 10
    ),
    MVD_INSIDER(
        id = "mvd_insider",
        title = "Инсайдер в МВД и Спецсвязи",
        description = "Повышает шанс перехвата спецномеров Петровка 38, Лубянка и Кремль",
        iconName = "shield",
        basePrice = 12_000,
        priceMultiplier = 2.0,
        maxLevel = 10
    ),
    GOLDEN_DATABASE(
        id = "golden_db",
        title = "VIP-Архив операторов",
        description = "Повышает вероятность выпадения Золотых, Платиновых и Бриллиантовых серий",
        iconName = "star",
        basePrice = 8_000,
        priceMultiplier = 1.8,
        maxLevel = 10
    ),
    TURBO_ROLL(
        id = "turbo_roll",
        title = "Турбо-переборщик 5G",
        description = "Ускоряет автокрутку номеров до молниеносной скорости (-25мс за уровень)",
        iconName = "bolt",
        basePrice = 5_000,
        priceMultiplier = 1.8,
        maxLevel = 8
    ),
    VAULT_EXPANSION(
        id = "vault_expansion",
        title = "Сейф для SIM-карт",
        description = "Увеличивает вместимость хранилища на +6 слотов за каждый уровень",
        iconName = "inventory",
        basePrice = 4_000,
        priceMultiplier = 1.6,
        maxLevel = 15
    ),
    ELITE_BROKER(
        id = "elite_broker",
        title = "Биржа Блатных Номеров",
        description = "Скупщики платят на +15% дороже за каждый проданный вами номер",
        iconName = "money",
        basePrice = 20_000,
        priceMultiplier = 1.9,
        maxLevel = 15
    ),
    QUANTUM_SCANNER(
        id = "quantum_scanner",
        title = "Квантовый Дешифратор",
        description = "Усиливает множители редких комбинаций (зеркала, триплеты, лесенки) на +25%",
        iconName = "quantum",
        basePrice = 35_000,
        priceMultiplier = 2.0,
        maxLevel = 15
    ),
    SATELLITE_ORBIT(
        id = "satellite_orbit",
        title = "Спутниковая Сеть «Сфера»",
        description = "Орбитальная группировка связи (+60 ₽ кешбэк со стоимости каждой крутки за уровень)",
        iconName = "satellite",
        basePrice = 75_000,
        priceMultiplier = 1.9,
        maxLevel = 20
    ),
    OPERATOR_CARTEL(
        id = "operator_cartel",
        title = "Телеком-Синдикат РФ",
        description = "Увеличивает выплаты со всех сделок по продаже номеров на +15% за каждый уровень",
        iconName = "cartel",
        basePrice = 150_000,
        priceMultiplier = 2.2,
        maxLevel = 12
    ),
    PRESIDENTIAL_HOTLINE(
        id = "presidential_hotline",
        title = "Спецканал Кремля",
        description = "Открывает доступ к правительственной связи с заоблачными множителями до x150",
        iconName = "presidential",
        basePrice = 500_000,
        priceMultiplier = 2.4,
        maxLevel = 8
    ),
    NEURAL_CLUSTER(
        id = "neural_cluster",
        title = "Нейросеть «Спектр-AI»",
        description = "Нейроанализ радиочастот. Повышает шанс редких серий (777, 999, 495) на +25% за уровень",
        iconName = "neural",
        basePrice = 1_500_000,
        priceMultiplier = 2.3,
        maxLevel = 10
    ),
    OFFSHORE_SYNDICATE(
        id = "offshore_syndicate",
        title = "Офшорный Холдинг Телеком",
        description = "Международные оптоволоконные узлы (+30% к бонусам за продажу редких номеров за уровень)",
        iconName = "offshore",
        basePrice = 5_000_000,
        priceMultiplier = 2.4,
        maxLevel = 10
    ),
    OLIGARCH_MONOPOLY(
        id = "oligarch_monopoly",
        title = "Монополия Олигарха",
        description = "Абсолютный контроль рынка связи. Умножает цену продажи всех номеров на +25% за уровень",
        iconName = "monopoly",
        basePrice = 15_000_000,
        priceMultiplier = 2.5,
        maxLevel = 8
    ),
    KREMLIN_VERTUSHKA(
        id = "kremlin_vertushka",
        title = "Золотая Вертушка АТС-1",
        description = "Высшая связь руководства РФ. Статус Телеком-Магната и +100% к стоимости редких категорий",
        iconName = "vertushka",
        basePrice = 50_000_000,
        priceMultiplier = 3.0,
        maxLevel = 6
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
