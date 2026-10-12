package com.example.ui.tabs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PhoneNumber
import com.example.model.RarityTier
import com.example.ui.theme.EmeraldProfit
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.LiquidGoldMid
import com.example.ui.theme.LiquidGoldStart
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

enum class InventorySort(val label: String) {
    PRICE("По цене"),
    DATE("По давности"),
    RARITY("По редкости")
}

enum class InventoryFilter(val label: String) {
    ALL("Все"),
    VAULT("В сейфе"),
    COMMON("Обычные"),
    BRONZE("Бронза"),
    SILVER("Серебро"),
    GOLD("Золото"),
    PLATINUM("Платина"),
    GOV("МВД/Гос")
}

@Composable
fun InventoryTab(
    inventory: List<PhoneNumber>,
    activeSim: PhoneNumber?,
    vaultCount: Int,
    vaultCapacity: Int,
    onSetActiveSim: (PhoneNumber) -> Unit,
    onToggleVault: (PhoneNumber) -> Unit,
    onSellSim: (PhoneNumber) -> Unit,
    onSellAllCommon: () -> Unit
) {
    val context = LocalContext.current
    var currentSort by remember { mutableStateOf(InventorySort.DATE) }
    var sortDescending by remember { mutableStateOf(true) }
    var currentFilter by remember { mutableStateOf(InventoryFilter.ALL) }

    // Filter logic
    val filteredList = remember(inventory, currentFilter) {
        when (currentFilter) {
            InventoryFilter.ALL -> inventory
            InventoryFilter.VAULT -> inventory.filter { it.isInVault }
            InventoryFilter.COMMON -> inventory.filter { it.rarity == RarityTier.COMMON }
            InventoryFilter.BRONZE -> inventory.filter { it.rarity == RarityTier.BRONZE }
            InventoryFilter.SILVER -> inventory.filter { it.rarity == RarityTier.SILVER }
            InventoryFilter.GOLD -> inventory.filter { it.rarity == RarityTier.GOLD }
            InventoryFilter.PLATINUM -> inventory.filter { it.rarity == RarityTier.PLATINUM }
            InventoryFilter.GOV -> inventory.filter { it.rarity.isGovOrSecret || it.rarity == RarityTier.DIAMOND }
        }
    }

    // Sort logic: by price, by date/recency, by rarity
    val sortedList = remember(filteredList, currentSort, sortDescending) {
        val comparator = when (currentSort) {
            InventorySort.PRICE -> compareBy<PhoneNumber> { it.value }
            InventorySort.DATE -> compareBy<PhoneNumber> { it.timestamp }
            InventorySort.RARITY -> compareBy<PhoneNumber> { it.rarity.ordinal }
        }
        if (sortDescending) {
            filteredList.sortedWith(comparator.reversed())
        } else {
            filteredList.sortedWith(comparator)
        }
    }

    val unsavedCommonCount = remember(inventory, activeSim) {
        inventory.count { it.rarity == RarityTier.COMMON && !it.isInVault && it.id != activeSim?.id }
    }
    val unsavedCommonValue = remember(inventory, activeSim) {
        inventory.filter { it.rarity == RarityTier.COMMON && !it.isInVault && it.id != activeSim?.id }.sumOf { it.value }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 1. Compact Summary & Stats Bar
        Surface(
            color = Color(0xD9101724),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Всего: ${inventory.size}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "•",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = LiquidGoldMid,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Сейф: $vaultCount / $vaultCapacity",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (vaultCount >= vaultCapacity) Color(0xFFF87171) else LiquidGoldStart
                        )
                    }
                }

                if (unsavedCommonCount > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x33EF4444))
                            .border(1.dp, Color(0x66EF4444), RoundedCornerShape(8.dp))
                            .clickable { onSellAllCommon() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("sell_all_common_btn")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = null,
                                tint = Color(0xFFFCA5A5),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Слить обычные ($unsavedCommonCount)",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFCA5A5)
                            )
                        }
                    }
                }
            }
        }

        // 2. Sorting Control Row (По цене, По давности, По редкости)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Сортировка:",
                fontSize = 11.sp,
                color = TextSecondary,
                fontWeight = FontWeight.SemiBold
            )

            InventorySort.values().forEach { sort ->
                val isSelected = currentSort == sort
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) LiquidGoldMid.copy(alpha = 0.2f) else Color(0x221E293B))
                        .border(
                            1.dp,
                            if (isSelected) LiquidGoldMid else Color.White.copy(alpha = 0.1f),
                            RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            if (isSelected) {
                                sortDescending = !sortDescending
                            } else {
                                currentSort = sort
                                sortDescending = true
                            }
                        }
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                        .testTag("sort_${sort.name.lowercase()}_btn")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = sort.label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) LiquidGoldStart else TextSecondary
                        )
                        if (isSelected) {
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(
                                imageVector = if (sortDescending) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                contentDescription = null,
                                tint = LiquidGoldStart,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }

        // 3. Category Filter Chips (Horizontal Scroll)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            InventoryFilter.values().forEach { filter ->
                val isSelected = currentFilter == filter
                val label = when (filter) {
                    InventoryFilter.ALL -> "Все (${inventory.size})"
                    InventoryFilter.VAULT -> "🔒 Сейф ($vaultCount)"
                    InventoryFilter.COMMON -> "Обычные"
                    InventoryFilter.BRONZE -> "Бронза"
                    InventoryFilter.SILVER -> "Серебро"
                    InventoryFilter.GOLD -> "Золото"
                    InventoryFilter.PLATINUM -> "Платина"
                    InventoryFilter.GOV -> "МВД/Гос"
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) LiquidGoldMid else Color(0x331E293B))
                        .border(
                            1.dp,
                            if (isSelected) LiquidGoldMid else Color.White.copy(alpha = 0.1f),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { currentFilter = filter }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                        .testTag("filter_${filter.name.lowercase()}_chip")
                ) {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color(0xFF0F172A) else TextSecondary
                    )
                }
            }
        }

        // 4. Numbers List
        if (sortedList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Archive,
                        contentDescription = null,
                        tint = Color(0xFF475569),
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        text = if (inventory.isEmpty()) "Инвентарь пуст" else "Нет номеров по данному фильтру",
                        color = TextSecondary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Крутите барабан на главном экране!",
                        color = Color(0xFF64748B),
                        fontSize = 11.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(bottom = 12.dp)
            ) {
                items(sortedList, key = { it.id }) { item ->
                    val isActive = activeSim?.id == item.id
                    InventoryNumberRow(
                        item = item,
                        isActive = isActive,
                        onSetActiveSim = { onSetActiveSim(item) },
                        onToggleVault = { onToggleVault(item) },
                        onSellSim = { onSellSim(item) },
                        onCopy = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Phone Number", item.formatted))
                            Toast.makeText(context, "Скопировано: ${item.formatted}", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun InventoryNumberRow(
    item: PhoneNumber,
    isActive: Boolean,
    onSetActiveSim: () -> Unit,
    onToggleVault: () -> Unit,
    onSellSim: () -> Unit,
    onCopy: () -> Unit
) {
    val tierColor = when (item.rarity) {
        RarityTier.COMMON -> Color(0xFF94A3B8)
        RarityTier.BRONZE -> Color(0xFFCD7F32)
        RarityTier.SILVER -> Color(0xFFCBD5E1)
        RarityTier.GOLD -> LiquidGoldStart
        RarityTier.PLATINUM -> Color(0xFF38BDF8)
        RarityTier.DIAMOND -> Color(0xFFF472B6)
        RarityTier.POLICE_MVD -> Color(0xFF60A5FA)
        RarityTier.FSB_KREMLIN -> Color(0xFFF87171)
    }

    Surface(
        color = Color(0xE6111827),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isActive) LiquidGoldMid else if (item.isInVault) LiquidGoldMid.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.08f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("inventory_item_${item.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Number info
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onCopy() },
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Badges row: Operator, Rarity, Vault, Phone
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Operator pill
                    Text(
                        text = item.operatorName,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )

                    // Rarity pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(tierColor.copy(alpha = 0.18f))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = item.rarity.badgeLabel.uppercase(),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = tierColor
                        )
                    }

                    if (item.isInVault) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(LiquidGoldMid.copy(alpha = 0.25f))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "🔒 СЕЙФ",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = LiquidGoldStart
                            )
                        }
                    }

                    if (isActive) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(EmeraldProfit.copy(alpha = 0.25f))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "В СЕТИ",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldProfit
                            )
                        }
                    }
                }

                // Formatted phone number
                Text(
                    text = item.formatted,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    color = TextPrimary,
                    letterSpacing = 0.3.sp
                )

                // Subtitle: Category & Multiplier
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = item.categoryDescription.ifBlank { "Базовый номер" },
                        fontSize = 10.sp,
                        color = Color(0xFF64748B)
                    )
                    if (item.totalMultiplier > 1.0) {
                        Text(
                            text = "×${String.format(java.util.Locale.US, "%.1f", item.totalMultiplier)}",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = tierColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Right: Price and Action Buttons
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Price
                Text(
                    text = "+${formatPrice(item.value)} ₽",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = EmeraldProfit
                )

                // Actions: Vault, Sell, Equip
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Equip to phone SIM
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isActive) EmeraldProfit.copy(alpha = 0.2f) else Color(0x331E293B))
                            .border(
                                1.dp,
                                if (isActive) EmeraldProfit else Color.White.copy(alpha = 0.1f),
                                RoundedCornerShape(6.dp)
                            )
                            .clickable { onSetActiveSim() }
                            .testTag("equip_sim_${item.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isActive) Icons.Default.Check else Icons.Default.PhoneAndroid,
                            contentDescription = "В телефон",
                            tint = if (isActive) EmeraldProfit else TextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    // Vault toggle button
                    Box(
                        modifier = Modifier
                            .height(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (item.isInVault) LiquidGoldMid.copy(alpha = 0.2f) else Color(0x331E293B))
                            .border(
                                1.dp,
                                if (item.isInVault) LiquidGoldMid else Color.White.copy(alpha = 0.1f),
                                RoundedCornerShape(6.dp)
                            )
                            .clickable { onToggleVault() }
                            .padding(horizontal = 6.dp)
                            .testTag("toggle_vault_${item.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (item.isInVault) Icons.Default.Lock else Icons.Default.Archive,
                                contentDescription = if (item.isInVault) "Из сейфа" else "В сейф",
                                tint = if (item.isInVault) LiquidGoldStart else TextSecondary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (item.isInVault) "Сейф" else "+Сейф",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (item.isInVault) LiquidGoldStart else TextSecondary
                            )
                        }
                    }

                    // Sell button
                    Box(
                        modifier = Modifier
                            .height(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (item.isInVault) Color(0x1A64748B) else Color(0x3310B981))
                            .border(
                                1.dp,
                                if (item.isInVault) Color(0x3364748B) else Color(0x6610B981),
                                RoundedCornerShape(6.dp)
                            )
                            .clickable { onSellSim() }
                            .padding(horizontal = 6.dp)
                            .testTag("sell_sim_${item.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (item.isInVault) Icons.Default.Lock else Icons.Default.Sell,
                                contentDescription = if (item.isInVault) "В сейфе" else "Продать",
                                tint = if (item.isInVault) TextSecondary else EmeraldProfit,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (item.isInVault) "В сейфе" else "Продать",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (item.isInVault) TextSecondary else EmeraldProfit
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatPrice(price: Long): String {
    val symbols = java.text.DecimalFormatSymbols().apply { groupingSeparator = ' ' }
    val formatter = java.text.DecimalFormat("#,###", symbols)
    return formatter.format(price)
}
