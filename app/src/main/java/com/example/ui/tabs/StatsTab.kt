package com.example.ui.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PlayerLevelInfo
import com.example.model.RarityTier
import com.example.ui.components.MinimalRarityBadge
import com.example.ui.theme.EmeraldProfit
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.LiquidGoldMid
import com.example.ui.theme.LiquidGoldStart
import com.example.ui.theme.PoliceBlueStart
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.GameStats
import com.example.viewmodel.GameViewModel

@Composable
fun StatsTab(
    stats: GameStats,
    levelInfo: PlayerLevelInfo,
    onOpenDiagnostics: (() -> Unit)? = null
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "ДОСЬЕ И СТАТИСТИКА",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = LiquidGoldStart
                )
                Text(
                    text = "История рекордов, ваш служебный ранг и спецсвязь РФ.",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        if (onOpenDiagnostics != null) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0x22F59E0B))
                        .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .clickable { onOpenDiagnostics() }
                        .padding(12.dp)
                        .testTag("open_diagnostic_log_btn")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Storage,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f, fill = false)) {
                                Text(
                                    text = "ДИАГНОСТИКА ROOM DB",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF59E0B),
                                    maxLines = 1,
                                    softWrap = false
                                )
                                Text(
                                    text = "Контроль персистентности: bestNumber и rarityCounts",
                                    fontSize = 10.sp,
                                    color = TextSecondary,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                        }
                        Text(
                            text = "ОТКРЫТЬ →",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFF59E0B),
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }

        // Player Level Dossier Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xD9121A2A))
                    .border(1.2.dp, LiquidGoldMid.copy(alpha = 0.5f), RoundedCornerShape(22.dp))
                    .padding(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(LiquidGoldStart.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MilitaryTech,
                                    contentDescription = null,
                                    tint = LiquidGoldStart,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "СЛУЖЕБНЫЙ РАНГ ${levelInfo.level}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LiquidGoldMid,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "«${levelInfo.title}»",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Black,
                                    color = TextPrimary
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x3310B981))
                                .border(1.dp, EmeraldProfit.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "УР. ${levelInfo.level}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = EmeraldProfit
                            )
                        }
                    }

                    // Progress bar
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Прогресс до повышения:",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = "${levelInfo.currentLevelXp} / ${levelInfo.nextLevelXp} XP (${(levelInfo.progressFraction * 100).toInt()}%)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = LiquidGoldStart
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0x401E293B))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(levelInfo.progressFraction)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(LiquidGoldStart)
                            )
                        }
                    }

                    // Active perks breakdown
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x331E293B))
                                .padding(8.dp)
                        ) {
                            Column {
                                Text("СЕЙФ", fontSize = 9.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                                Text("+${levelInfo.bonusVaultSlots} слотов", fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x331E293B))
                                .padding(8.dp)
                        ) {
                            Column {
                                Text("ПАССИВКА", fontSize = 9.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                                Text("+${levelInfo.passiveIncomeBonusPercent}%", fontSize = 12.sp, color = EmeraldProfit, fontWeight = FontWeight.Bold)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x331E293B))
                                .padding(8.dp)
                        ) {
                            Column {
                                Text("ПРЕМИЯ", fontSize = 9.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                                Text("+${GameViewModel.formatMoney(levelInfo.nextLevelBonusCash)}", fontSize = 11.sp, color = LiquidGoldStart, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Total Rolls Glass Pill
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xD9121722))
                        .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text("ВСЕГО КРУТОК", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${stats.totalRolls}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                    }
                }

                // Total Profit Glass Pill
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xD9121722))
                        .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text("ОБЩИЙ ДОХОД", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = GameViewModel.formatMoney(stats.totalEarned),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            color = EmeraldProfit
                        )
                    }
                }
            }
        }

        // Best Record Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xD9121722))
                    .border(1.2.dp, LiquidGoldMid.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                    .padding(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(LiquidGoldMid.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MilitaryTech,
                                    contentDescription = null,
                                    tint = LiquidGoldStart,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "РЕКОРД СТОИМОСТИ",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = LiquidGoldStart
                            )
                        }

                        if (stats.bestNumber != null) {
                            MinimalRarityBadge(rarity = stats.bestNumber.rarity, isMini = true)
                        }
                    }

                    if (stats.bestNumber != null) {
                        Text(
                            text = stats.bestNumber.formatted,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = TextPrimary,
                            maxLines = 1,
                            softWrap = false
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stats.bestNumber.categoryDescription,
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = GameViewModel.formatMoney(stats.bestNumber.value),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = EmeraldProfit
                            )
                        }
                    } else {
                        Text(
                            text = "Еще не сгенерировано номеров",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Rarity Distribution Table
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xD9121722))
                    .border(1.dp, GlassBorder, RoundedCornerShape(24.dp))
                    .padding(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = null,
                            tint = LiquidGoldStart,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "РАСПРЕДЕЛЕНИЕ ПО РАНГАМ",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                    }

                    RarityTier.values().forEach { tier ->
                        val count = stats.rarityCounts[tier] ?: 0
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = tier.title,
                                fontSize = 13.sp,
                                color = tier.primaryColor,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$count шт.",
                                fontSize = 13.sp,
                                color = if (count > 0) TextPrimary else TextSecondary,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }

        // Special Department Encyclopedia
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xD9121722))
                    .border(1.dp, PoliceBlueStart.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                    .padding(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalPolice,
                            contentDescription = null,
                            tint = PoliceBlueStart,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "СПРАВОЧНИК ВЕДОМСТВЕННЫХ СЕРИЙ",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                    }

                    val guideItems = listOf(
                        GuideEntry(
                            prefix = "+7 (495) 694-XX-XX",
                            title = "ГУ МВД России по Москве (Петровка, 38)",
                            desc = "Легендарная серия столичного главка МВД. Номера с префиксом 694 закреплены за оперативными службами Петровки."
                        ),
                        GuideEntry(
                            prefix = "+7 (495) 667-XX-XX",
                            title = "Центральный Аппарат МВД РФ (Житная)",
                            desc = "Прямые правительственные линии руководства МВД России."
                        ),
                        GuideEntry(
                            prefix = "+7 (495) 624-XX-XX",
                            title = "ФСБ России (Лубянская площадь)",
                            desc = "Спецлинии центрального аппарата госбезопасности."
                        ),
                        GuideEntry(
                            prefix = "+7 (495) 606-XX-XX",
                            title = "Кремль / Вертушка АТС-1",
                            desc = "Высшая президентская спецсвязь Администрации Президента РФ (Старая площадь). Редчайший джекпот."
                        ),
                        GuideEntry(
                            prefix = "+7 (999) 777-77-77",
                            title = "Абсолютный монохром (Бриллиант)",
                            desc = "Совпадение всех 7 цифр мобильного номера. Коллекционный эксклюзив."
                        ),
                        GuideEntry(
                            prefix = "+7 (XXX) 123-45-67",
                            title = "Полная восходящая лесенка",
                            desc = "Идеальный порядок чисел подряд от 1 до 7."
                        )
                    )

                    guideItems.forEach { entry ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0x331E293B))
                                .border(1.dp, Color.White.copy(0.06f), RoundedCornerShape(14.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = entry.prefix,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                color = LiquidGoldStart,
                                fontSize = 13.sp
                            )
                            Text(
                                text = entry.title,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 12.sp
                            )
                            Text(
                                text = entry.desc,
                                color = TextSecondary,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class GuideEntry(
    val prefix: String,
    val title: String,
    val desc: String
)
