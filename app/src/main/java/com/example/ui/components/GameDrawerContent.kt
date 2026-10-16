package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.PrimaryActionBlue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.GameViewModel

enum class DrawerDestination {
    NONE,
    INVENTORY,
    VAULT,
    UPGRADES,
    QUESTS,
    AUTO_SELL_SETTINGS,
    STATS,
    LEVEL_PERKS,
    RARITY_CATALOG,
    OTA_UPDATES,
    SELECT_COUNTRY
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
    Surface(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(),
        color = DarkBg
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header: ← Меню + баланс
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(DarkSurface)
                        .border(1.dp, GlassBorder, CircleShape)
                        .clickable { onCloseDrawer() }
                        .testTag("close_drawer_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Меню",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.weight(1f))

                // Small balance chip in drawer header
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurface)
                        .border(1.dp, GlassBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 9.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = GameViewModel.formatMoney(balance),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }
            }

            // Scrollable Bento Grid
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1. Полноширинный баннер: «ГЕНЕРАТОР SIM»
                BentoBannerCard(
                    title = "ГЕНЕРАТОР SIM",
                    subtitle = "Станция выпуска красивых номеров",
                    numberText = "+7 (999) 777-77-77",
                    onClick = {
                        onCloseDrawer()
                        onSelectDestination(DrawerDestination.NONE)
                    }
                )

                // 2. Полноширинный баннер: «ЕЖЕДНЕВНЫЕ ЗАДАНИЯ»
                BentoQuestBannerCard(
                    title = "ЕЖЕДНЕВНЫЕ ЗАДАНИЯ",
                    badge = if (unclaimedQuestsCount > 0) "+$unclaimedQuestsCount НАГРАД" else "✓ 777-**-**",
                    hasUnclaimed = unclaimedQuestsCount > 0,
                    modifier = Modifier.testTag("drawer_item_quests"),
                    onClick = { onSelectDestination(DrawerDestination.QUESTS) }
                )

                // 3. Двухколоночная компактная сетка Bento Grid (высота 74.dp)
                // Row 1: КОЛЛЕКЦИЯ + СЕЙФ
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BentoSquareCard(
                        title = "КОЛЛЕКЦИЯ",
                        countText = if (inventoryCount > 0) "$inventoryCount шт" else null,
                        icon = Icons.Default.Inventory2,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("drawer_item_inventory"),
                        onClick = { onSelectDestination(DrawerDestination.INVENTORY) }
                    )
                    BentoSquareCard(
                        title = "СЕЙФ",
                        countText = "$vaultCount/$vaultCapacity",
                        icon = Icons.Default.Archive,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("drawer_item_vault"),
                        onClick = { onSelectDestination(DrawerDestination.VAULT) }
                    )
                }

                // Row 2: УЛУЧШЕНИЯ + АВТОПРОДАЖА
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BentoSquareCard(
                        title = "УЛУЧШЕНИЯ",
                        countText = null,
                        icon = Icons.Default.TrendingUp,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("drawer_item_upgrades"),
                        onClick = { onSelectDestination(DrawerDestination.UPGRADES) }
                    )
                    BentoSquareCard(
                        title = "АВТОПРОДАЖА",
                        countText = null,
                        icon = Icons.Default.Speed,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("drawer_item_auto_sell_settings"),
                        onClick = { onSelectDestination(DrawerDestination.AUTO_SELL_SETTINGS) }
                    )
                }

                // Row 3: СТАТИСТИКА + КАТАЛОГ РЕДКОСТЕЙ
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BentoSquareCard(
                        title = "СТАТИСТИКА",
                        countText = null,
                        icon = Icons.Default.BarChart,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("drawer_item_stats"),
                        onClick = { onSelectDestination(DrawerDestination.STATS) }
                    )
                    BentoSquareCard(
                        title = "КАТАЛОГ",
                        countText = "8 рангов",
                        icon = Icons.Default.Category,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("drawer_item_rarity_catalog"),
                        onClick = { onSelectDestination(DrawerDestination.RARITY_CATALOG) }
                    )
                }

                // Row 4: РАНГИ И ОПЫТ + ОБНОВЛЕНИЯ
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BentoSquareCard(
                        title = "РАНГИ И ОПЫТ",
                        countText = "Ур. ${levelInfo.level}",
                        icon = Icons.Default.MilitaryTech,
                        isHighlighted = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("drawer_player_level_card"),
                        onClick = { onSelectDestination(DrawerDestination.LEVEL_PERKS) }
                    )
                    BentoSquareCard(
                        title = "ОБНОВЛЕНИЯ",
                        countText = "v${com.example.BuildConfig.VERSION_NAME}",
                        icon = Icons.Default.SystemUpdate,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("drawer_item_ota_updates"),
                        onClick = { onSelectDestination(DrawerDestination.OTA_UPDATES) }
                    )
                }

                // Row 5: ВЫБОР СТРАНЫ НОМЕРОВ
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BentoSquareCard(
                        title = "ВЫБОР СТРАНЫ",
                        countText = "8 регионов",
                        icon = Icons.Default.Public,
                        isHighlighted = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("drawer_item_select_country"),
                        onClick = { onSelectDestination(DrawerDestination.SELECT_COUNTRY) }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
            }
        }
    }
}

@Composable
private fun BentoBannerCard(
    title: String,
    subtitle: String,
    numberText: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mini SIM card preview
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(DarkSurfaceElevated)
                        .border(1.dp, GlassBorder, RoundedCornerShape(6.dp))
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        RussianFlag(modifier = Modifier.size(width = 11.dp, height = 7.dp))
                        Text(
                            text = numberText,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }

                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    color = TextSecondary
                )
            }

            Text(
                text = title,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryActionBlue,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
private fun BentoQuestBannerCard(
    title: String,
    badge: String,
    hasUnclaimed: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(DarkSurfaceElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✓",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = PrimaryActionBlue
                    )
                }
                Column {
                    Text(
                        text = title,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Серия заданий активна",
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (hasUnclaimed) PrimaryActionBlue else DarkSurfaceElevated)
                    .border(
                        1.dp,
                        if (hasUnclaimed) PrimaryActionBlue else GlassBorder,
                        RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 7.dp, vertical = 3.dp)
            ) {
                Text(
                    text = badge,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun BentoSquareCard(
    title: String,
    countText: String?,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    isHighlighted: Boolean = false,
    onClick: () -> Unit
) {
    val bg = if (isHighlighted) Color(0xFF192336) else DarkSurface
    val borderCol = if (isHighlighted) PrimaryActionBlue.copy(alpha = 0.6f) else GlassBorder

    Box(
        modifier = modifier
            .height(74.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(1.dp, borderCol, RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isHighlighted) PrimaryActionBlue else Color(0xFF60A5FA),
                    modifier = Modifier.size(20.dp)
                )

                if (countText != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(DarkSurfaceElevated)
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = countText,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                    }
                }
            }

            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isHighlighted) Color.White else Color(0xFFE2E8F0),
                letterSpacing = 0.4.sp,
                maxLines = 1
            )
        }
    }
}
