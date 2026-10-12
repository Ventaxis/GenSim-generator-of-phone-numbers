package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.DiamondCyanStart
import com.example.ui.theme.LiquidGoldMid
import com.example.ui.theme.LiquidGoldStart
import com.example.ui.theme.PlatinumSilverStart
import com.example.ui.theme.PoliceBlueStart
import com.example.ui.theme.PoliceRedEnd
import com.example.ui.theme.TextMuted

enum class RarityTier(
    val title: String,
    val badgeLabel: String,
    val primaryColor: Color,
    val borderColor: Color,
    val minPrice: Long,
    val maxPrice: Long,
    val prestigeStars: Int
) {
    COMMON(
        title = "Обычный",
        badgeLabel = "БАЗОВЫЙ",
        primaryColor = TextMuted,
        borderColor = Color(0x33FFFFFF),
        minPrice = 40,
        maxPrice = 300,
        prestigeStars = 0
    ),
    BRONZE(
        title = "Бронза",
        badgeLabel = "BRONZE VIP",
        primaryColor = Color(0xFFCD7F32),
        borderColor = Color(0xFF8C5523),
        minPrice = 350,
        maxPrice = 2_800,
        prestigeStars = 1
    ),
    SILVER(
        title = "Серебро",
        badgeLabel = "SILVER VIP",
        primaryColor = PlatinumSilverStart,
        borderColor = Color(0xFF94A3B8),
        minPrice = 3_000,
        maxPrice = 16_000,
        prestigeStars = 2
    ),
    GOLD(
        title = "Золотой",
        badgeLabel = "GOLD VIP",
        primaryColor = LiquidGoldMid,
        borderColor = LiquidGoldStart,
        minPrice = 18_000,
        maxPrice = 95_000,
        prestigeStars = 3
    ),
    PLATINUM(
        title = "Платиновый",
        badgeLabel = "PLATINUM",
        primaryColor = Color(0xFF38BDF8),
        borderColor = Color(0xFF7DD3FC),
        minPrice = 100_000,
        maxPrice = 480_000,
        prestigeStars = 4
    ),
    DIAMOND(
        title = "Бриллиантовый",
        badgeLabel = "DIAMOND ELITE",
        primaryColor = DiamondCyanStart,
        borderColor = Color(0xFF00E5FF),
        minPrice = 500_000,
        maxPrice = 2_500_000,
        prestigeStars = 5
    ),
    POLICE_MVD(
        title = "МВД РФ",
        badgeLabel = "СПЕЦСВЯЗЬ МВД",
        primaryColor = PoliceBlueStart,
        borderColor = PoliceRedEnd,
        minPrice = 1_500_000,
        maxPrice = 8_500_000,
        prestigeStars = 5
    ),
    FSB_KREMLIN(
        title = "АТС-1 / Кремль / ФСБ",
        badgeLabel = "ВЕРТУШКА АТС-1",
        primaryColor = Color(0xFFF43F5E),
        borderColor = LiquidGoldStart,
        minPrice = 10_000_000,
        maxPrice = 55_000_000,
        prestigeStars = 6
    );

    val isGovOrSecret: Boolean
        get() = this == POLICE_MVD || this == FSB_KREMLIN

    val isRareOrAbove: Boolean
        get() = this != COMMON
}
