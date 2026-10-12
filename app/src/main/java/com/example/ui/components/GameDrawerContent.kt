package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PhoneNumber
import com.example.model.PlayerLevelInfo
import com.example.ui.theme.EmeraldProfit
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.LiquidGoldMid
import com.example.ui.theme.LiquidGoldStart
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.GameViewModel

enum class DrawerDestination {
    NONE,
    INVENTORY,
    UPGRADES,
    QUESTS,
    AUTO_SELL_SETTINGS,
    STATS,
    LEVEL_PERKS,
    DIAGNOSTICS
}

@Composable
fun GameDrawerContent(
    balance: Long,
    levelInfo: PlayerLevelInfo,
    passiveIncome: Long,
    activeSim: PhoneNumber?,
    vaultCount: Int,
    vaultCapacity: Int,
    inventoryCount: Int = 0,
    unclaimedQuestsCount: Int,
    onSelectDestination: (DrawerDestination) -> Unit,
    onCloseDrawer: () -> Unit
) {
    val animatedProgress by animateFloatAsState(
        targetValue = levelInfo.progressFraction,
        label = "drawer_xp_progress"
    )

    Surface(
        modifier = Modifier
            .fillMaxHeight()
            .width(320.dp),
        color = Color(0xF80A0E17),
        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .statusBarsPadding()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header with Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "СИМ-МАГНАТ",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Black,
                            color = LiquidGoldStart,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Блатные Номера РФ",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0x331E293B))
                            .border(1.dp, GlassBorder, CircleShape)
                            .clickable { onCloseDrawer() }
                            .testTag("close_drawer_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Закрыть",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Player Experience & Level Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xE6131B2A))
                        .border(1.dp, LiquidGoldMid.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                        .clickable { onSelectDestination(DrawerDestination.LEVEL_PERKS) }
                        .padding(14.dp)
                        .testTag("drawer_player_level_card")
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(LiquidGoldStart.copy(alpha = 0.2f)),
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
                                Column {
                                    Text(
                                        text = "УРОВЕНЬ ${levelInfo.level}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black,
                                        color = LiquidGoldStart
                                    )
                                    Text(
                                        text = "«${levelInfo.title}»",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0x3310B981))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "Привилегии",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldProfit
                                )
                            }
                        }

                        // XP Progress Bar
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Опыт (XP):",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "${levelInfo.currentLevelXp} / ${levelInfo.nextLevelXp} XP (${(levelInfo.progressFraction * 100).toInt()}%)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LiquidGoldMid
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color(0x331E293B))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(animatedProgress)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(LiquidGoldStart)
                                )
                            }
                        }

                        // Active Perks Summary
                        Text(
                            text = "+${levelInfo.bonusVaultSlots} к сейфу • +${levelInfo.passiveIncomeBonusPercent}% доход",
                            fontSize = 10.sp,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Active SIM Profile Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xD9121724))
                        .border(1.dp, GlassBorder, RoundedCornerShape(18.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PhoneAndroid,
                                contentDescription = null,
                                tint = LiquidGoldMid,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ВАШ НОМЕР В ТЕЛЕФОНЕ:",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                        }

                        Text(
                            text = activeSim?.formatted ?: "+7 (999) 000-00-00",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = activeSim?.operatorName ?: "Связь",
                                fontSize = 11.sp,
                                color = LiquidGoldStart,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "+$passiveIncome ₽/с",
                                fontSize = 11.sp,
                                color = EmeraldProfit,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                Spacer(modifier = Modifier.height(10.dp))

                // Menu items
                val menuItems = listOf(
                    MenuItemData(
                        id = DrawerDestination.INVENTORY,
                        title = "Инвентарь",
                        subtitle = "Все номера ($inventoryCount) • Сейф $vaultCount/$vaultCapacity",
                        icon = Icons.Default.Inventory2,
                        accentColor = LiquidGoldMid
                    ),
                    MenuItemData(
                        id = DrawerDestination.QUESTS,
                        title = "Задания и Награды",
                        subtitle = if (unclaimedQuestsCount > 0) "Есть готовые награды!" else "День, неделя, месяц",
                        icon = Icons.Default.EmojiEvents,
                        accentColor = EmeraldProfit,
                        badgeCount = unclaimedQuestsCount
                    ),
                    MenuItemData(
                        id = DrawerDestination.UPGRADES,
                        title = "Магазин и Связи",
                        subtitle = "МВД, АТС, VIP-база",
                        icon = Icons.Default.ShoppingBag,
                        accentColor = Color(0xFF60A5FA)
                    ),
                    MenuItemData(
                        id = DrawerDestination.STATS,
                        title = "Досье и Спецсерии",
                        subtitle = "Ранг, рекорды, каталог МВД",
                        icon = Icons.Default.BarChart,
                        accentColor = Color(0xFF38BDF8)
                    ),
                    MenuItemData(
                        id = DrawerDestination.AUTO_SELL_SETTINGS,
                        title = "Автопродажа",
                        subtitle = "Фильтры рангов и цен",
                        icon = Icons.Default.Settings,
                        accentColor = Color(0xFFA78BFA)
                    ),
                    MenuItemData(
                        id = DrawerDestination.DIAGNOSTICS,
                        title = "Диагностика Room DB",
                        subtitle = "bestNumber, rarityCounts, сырой дамп",
                        icon = Icons.Default.Storage,
                        accentColor = Color(0xFFF59E0B)
                    )
                )

                menuItems.forEach { item ->
                    DrawerItem(
                        item = item,
                        onClick = { onSelectDestination(item.id) }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Balance
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0x330C261B))
                    .border(1.dp, Color(0x6610B981), RoundedCornerShape(18.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "БАЛАНС",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                        Text(
                            text = GameViewModel.formatMoney(balance),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            color = EmeraldProfit
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(EmeraldProfit.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = EmeraldProfit,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

private data class MenuItemData(
    val id: DrawerDestination,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val accentColor: Color,
    val badgeCount: Int = 0
)

@Composable
private fun DrawerItem(
    item: MenuItemData,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 7.dp)
            .testTag("drawer_item_${item.id.name.lowercase()}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(item.accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = item.accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = TextPrimary
                )
                Text(
                    text = item.subtitle,
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }

            if (item.badgeCount > 0) {
                Box(
                    modifier = Modifier
                        .sizeIn(minWidth = 20.dp, minHeight = 20.dp)
                        .clip(CircleShape)
                        .background(EmeraldProfit)
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${item.badgeCount}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Black,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        style = androidx.compose.material3.LocalTextStyle.current.copy(
                            platformStyle = androidx.compose.ui.text.PlatformTextStyle(
                                includeFontPadding = false
                            ),
                            lineHeight = 10.sp
                        ),
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}
