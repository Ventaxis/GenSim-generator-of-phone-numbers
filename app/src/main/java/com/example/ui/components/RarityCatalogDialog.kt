package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.generator.PhoneGenerator
import com.example.model.PhoneNumber
import com.example.model.RarityTier
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.PrimaryActionBlue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.GameViewModel

@Composable
fun RarityCatalogDialog(
    onDismissRequest: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = DarkBg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                // Header with back button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(DarkSurface)
                            .border(1.dp, GlassBorder, CircleShape)
                            .clickable { onDismissRequest() }
                            .testTag("close_rarity_catalog_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = "Каталог редкостей",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                // Three decorative filter capsules
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Россия ▾", "VIP и Гос ▾", "Москва ▾").forEach { label ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(DarkSurface)
                                .border(1.dp, GlassBorder, RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // List of tiers
                val catalogItems = listOf(
                    CatalogTierEntry(RarityTier.COMMON, "4829103", "903", "СОТОВЫЙ РФ", "Случайный номер", "~88.0%"),
                    CatalogTierEntry(RarityTier.BRONZE, "2501900", "926", "МЕГАФОН", "Круглый финал (00)", "~8.5%"),
                    CatalogTierEntry(RarityTier.SILVER, "3457771", "999", "YOTA VIP", "Тройка элитных цифр (777)", "~2.5%"),
                    CatalogTierEntry(RarityTier.GOLD, "7770777", "777", "МЕГАФОН ELITE", "Симметричные тройки (777)", "~0.8%"),
                    CatalogTierEntry(RarityTier.PLATINUM, "1234567", "495", "МОСКВА ДИРЕКТ", "Идеальная лесенка", "~0.08%"),
                    CatalogTierEntry(RarityTier.DIAMOND, "7777777", "777", "МЕГАФОН ELITE VIP", "Абсолютный монохром (777)", "~0.01%"),
                    CatalogTierEntry(RarityTier.POLICE_MVD, "6940001", "495", "Спецсвязь МВД", "ГУ МВД по Москве (Петровка, 38)", "~0.008%"),
                    CatalogTierEntry(RarityTier.FSB_KREMLIN, "6060001", "495", "Кремль АТС-1", "Администрация Президента (Вертушка)", "~0.002%")
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(catalogItems, key = { it.tier.name }) { entry ->
                        CatalogTierCard(entry = entry)
                    }
                }
            }
        }
    }
}

private data class CatalogTierEntry(
    val tier: RarityTier,
    val digits: String,
    val prefix: String,
    val operatorName: String,
    val categoryDescription: String,
    val approxChance: String
)

@Composable
private fun CatalogTierCard(entry: CatalogTierEntry) {
    val tier = entry.tier
    val sampleNumber = PhoneNumber(
        id = "catalog_${tier.name}",
        prefix = entry.prefix,
        digits = entry.digits,
        formatted = PhoneGenerator.formatNumber(entry.prefix, entry.digits),
        operatorName = entry.operatorName,
        rarity = tier,
        categoryDescription = entry.categoryDescription,
        basePrice = tier.minPrice,
        multipliers = emptyList(),
        totalMultiplier = 1.0,
        value = tier.minPrice
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        tier.primaryColor.copy(alpha = 0.15f),
                        DarkSurface
                    )
                )
            )
            .border(1.dp, tier.primaryColor.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Preview SIM Card
            SimCardView(
                phoneNumber = sampleNumber,
                isMini = false,
                isAutoRolling = true
            )

            // Bottom info bar of the card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF0F121A))
                    .border(0.8.dp, GlassBorder, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Title + Bar
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            text = tier.title,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = tier.primaryColor
                        )
                        SegmentedRarityBar(rarity = tier)
                    }

                    // Center: Drop chance
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Шанс",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                        Text(
                            text = entry.approxChance,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryActionBlue
                        )
                    }

                    // Right: Price
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Оценка",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                        Text(
                            text = "от ${GameViewModel.formatMoney(tier.minPrice)} ₽",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
