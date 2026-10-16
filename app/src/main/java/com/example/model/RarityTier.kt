package com.example.model

import androidx.compose.ui.graphics.Color

enum class RarityTier(
    val title: String,
    val badgeLabel: String,
    val primaryColor: Color,
    val borderColor: Color,
    val minPrice: Long,
    val maxPrice: Long,
    val prestigeStars: Int,
    val segmentsCount: Int
) {
    COMMON(
        title = "Обычный",
        badgeLabel = "БАЗОВЫЙ",
        primaryColor = Color(0xFF7A8291),
        borderColor = Color(0x33FFFFFF),
        minPrice = 300,
        maxPrice = 1_500,
        prestigeStars = 0,
        segmentsCount = 1
    ),
    BRONZE(
        title = "Необычный",
        badgeLabel = "BRONZE VIP",
        primaryColor = Color(0xFF2DBE60),
        borderColor = Color(0xFF1E8240),
        minPrice = 6_000,
        maxPrice = 40_000,
        prestigeStars = 1,
        segmentsCount = 2
    ),
    SILVER(
        title = "Редкий",
        badgeLabel = "SILVER VIP",
        primaryColor = Color(0xFF3880FF),
        borderColor = Color(0xFF1E56B3),
        minPrice = 70_000,
        maxPrice = 400_000,
        prestigeStars = 2,
        segmentsCount = 3
    ),
    GOLD(
        title = "Эпический",
        badgeLabel = "GOLD VIP",
        primaryColor = Color(0xFF9B51E0),
        borderColor = Color(0xFF6B32A0),
        minPrice = 600_000,
        maxPrice = 4_000_000,
        prestigeStars = 3,
        segmentsCount = 4
    ),
    PLATINUM(
        title = "Платиновый",
        badgeLabel = "PLATINUM",
        primaryColor = Color(0xFFE5A93C),
        borderColor = Color(0xFFB37E22),
        minPrice = 5_000_000,
        maxPrice = 35_000_000,
        prestigeStars = 4,
        segmentsCount = 5
    ),
    DIAMOND(
        title = "Легендарный",
        badgeLabel = "DIAMOND ELITE",
        primaryColor = Color(0xFFF59E0B),
        borderColor = Color(0xFFB45309),
        minPrice = 45_000_000,
        maxPrice = 300_000_000,
        prestigeStars = 5,
        segmentsCount = 5
    ),
    POLICE_MVD(
        title = "МВД РФ",
        badgeLabel = "СПЕЦСВЯЗЬ МВД",
        primaryColor = Color(0xFF3B82F6),
        borderColor = Color(0xFFE11D48),
        minPrice = 90_000_000,
        maxPrice = 500_000_000,
        prestigeStars = 5,
        segmentsCount = 5
    ),
    FSB_KREMLIN(
        title = "АТС-1 / Кремль / ФСБ",
        badgeLabel = "ВЕРТУШКА АТС-1",
        primaryColor = Color(0xFFF43F5E),
        borderColor = Color(0xFFE5A93C),
        minPrice = 300_000_000,
        maxPrice = 2_000_000_000,
        prestigeStars = 6,
        segmentsCount = 5
    );

    val isGovOrSecret: Boolean
        get() = this == POLICE_MVD || this == FSB_KREMLIN

    val isRareOrAbove: Boolean
        get() = this != COMMON
}
