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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PhoneNumber
import com.example.model.RarityTier
import com.example.ui.components.NumberDetailDialog
import com.example.ui.components.OperatorLogo
import com.example.ui.components.SegmentedRarityBar
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmeraldProfit
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.PrimaryActionBlue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.GameViewModel
import kotlinx.coroutines.launch

enum class StorageSection(val label: String) {
    INVENTORY("Коллекция"),
    VAULT("Сейф")
}

enum class InventorySort(val label: String) {
    PRICE("Цена"),
    DATE("Дата"),
    RARITY("Редкость")
}

enum class InventoryFilter(val label: String) {
    ALL("Все"),
    COMMON("Обычные"),
    BRONZE("Необычные"),
    SILVER("Редкие"),
    GOLD("Эпические"),
    PLATINUM("Платина"),
    GOV("Госсвязь/VIP")
}

@Composable
fun InventoryTab(
    inventory: List<PhoneNumber>,
    activeSim: PhoneNumber?,
    vaultCount: Int,
    vaultCapacity: Int,
    inventoryCapacity: Int = 100,
    balance: Long = 0L,
    initialVaultSection: Boolean = false,
    getEffectiveSellPrice: (PhoneNumber) -> Long = { it.value },
    onSetActiveSim: (PhoneNumber) -> Unit,
    onToggleVault: (PhoneNumber) -> Unit,
    onSellSim: (PhoneNumber) -> Unit,
    onSellAllCommon: () -> Unit,
    onSellAllInventory: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var storageSection by remember(initialVaultSection) {
        mutableStateOf(if (initialVaultSection) StorageSection.VAULT else StorageSection.INVENTORY)
    }
    LaunchedEffect(initialVaultSection) {
        storageSection = if (initialVaultSection) StorageSection.VAULT else StorageSection.INVENTORY
    }
    var currentSort by remember { mutableStateOf(InventorySort.DATE) }
    var sortDescending by remember { mutableStateOf(true) }
    var currentFilter by remember { mutableStateOf(InventoryFilter.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedNumberForDetail by remember { mutableStateOf<PhoneNumber?>(null) }

    val listState = rememberLazyListState()

    // Base list strictly separated: Vault numbers do not appear in Inventory!
    val poolList = remember(inventory, storageSection) {
        if (storageSection == StorageSection.INVENTORY) {
            inventory.filter { !it.isInVault }
        } else {
            inventory.filter { it.isInVault }
        }
    }

    // Filter logic
    val filteredList = remember(poolList, currentFilter) {
        when (currentFilter) {
            InventoryFilter.ALL -> poolList
            InventoryFilter.COMMON -> poolList.filter { it.rarity == RarityTier.COMMON }
            InventoryFilter.BRONZE -> poolList.filter { it.rarity == RarityTier.BRONZE }
            InventoryFilter.SILVER -> poolList.filter { it.rarity == RarityTier.SILVER }
            InventoryFilter.GOLD -> poolList.filter { it.rarity == RarityTier.GOLD }
            InventoryFilter.PLATINUM -> poolList.filter { it.rarity == RarityTier.PLATINUM }
            InventoryFilter.GOV -> poolList.filter { it.rarity.isGovOrSecret || it.rarity == RarityTier.DIAMOND }
        }
    }

    // Sort logic: by price, by date/recency, by rarity
    val sortedList = remember(filteredList, currentSort, sortDescending, searchQuery) {
        val comparator = when (currentSort) {
            InventorySort.PRICE -> compareBy<PhoneNumber> { it.value }
            InventorySort.DATE -> compareBy<PhoneNumber> { it.timestamp }
            InventorySort.RARITY -> compareBy<PhoneNumber> { it.rarity.ordinal }
        }
        val sorted = if (sortDescending) {
            filteredList.sortedWith(comparator.reversed())
        } else {
            filteredList.sortedWith(comparator)
        }

        if (searchQuery.isBlank()) {
            sorted
        } else {
            val q = searchQuery.trim().lowercase()
            sorted.filter {
                it.formatted.lowercase().contains(q) ||
                it.digits.contains(q) ||
                it.prefix.contains(q) ||
                it.categoryDescription.lowercase().contains(q) ||
                it.operatorName.lowercase().contains(q)
            }
        }
    }

    LaunchedEffect(currentSort, sortDescending, currentFilter, storageSection, searchQuery) {
        listState.scrollToItem(0)
    }

    val unsavedCommonCount = remember(inventory, activeSim) {
        inventory.count { it.rarity == RarityTier.COMMON && !it.isInVault && it.id != activeSim?.id }
    }
    val nonVaultCount = remember(inventory) { inventory.count { !it.isInVault } }

    val totalSectionValue = remember(poolList) {
        poolList.sumOf { getEffectiveSellPrice(it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 1. Шапка и переключатель (Коллекция / Сейф) + 3-строчная сводка справа
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Переключатель вкладок
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurface)
                    .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Tab: Коллекция
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (storageSection == StorageSection.INVENTORY) PrimaryActionBlue else Color.Transparent)
                        .clickable {
                            storageSection = StorageSection.INVENTORY
                            coroutineScope.launch { listState.scrollToItem(0) }
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("storage_tab_inventory"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Коллекция",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (storageSection == StorageSection.INVENTORY) Color.White else TextSecondary
                    )
                }

                // Tab: Сейф
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (storageSection == StorageSection.VAULT) PrimaryActionBlue else Color.Transparent)
                        .clickable {
                            storageSection = StorageSection.VAULT
                            coroutineScope.launch { listState.scrollToItem(0) }
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("storage_tab_vault"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Сейф",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (storageSection == StorageSection.VAULT) Color.White else TextSecondary
                    )
                }
            }

            // Сводка справа (3 компактные строки с выравниванием по правому краю)
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = GameViewModel.formatMoney(balance),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = if (storageSection == StorageSection.INVENTORY) {
                        "$nonVaultCount / $inventoryCapacity"
                    } else {
                        "$vaultCount / $vaultCapacity"
                    },
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Text(
                    text = "Стоимость: ${GameViewModel.formatMoney(totalSectionValue)} ₽",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        }

        // 2. Панель сортировки (Компактные кнопки-капсулы height 34.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            InventorySort.values().forEach { sort ->
                val isSelected = currentSort == sort
                Box(
                    modifier = Modifier
                        .height(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) PrimaryActionBlue else Color(0xFF1A1E29))
                        .border(
                            1.dp,
                            if (isSelected) PrimaryActionBlue else Color(0xFF282E3D),
                            RoundedCornerShape(10.dp)
                        )
                        .clickable {
                            if (isSelected) {
                                sortDescending = !sortDescending
                            } else {
                                currentSort = sort
                                sortDescending = true
                            }
                            coroutineScope.launch { listState.scrollToItem(0) }
                        }
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = sort.label,
                            fontSize = 11.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else TextSecondary
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (isSelected && !sortDescending) "▲" else "▼",
                            fontSize = 10.sp,
                            color = if (isSelected) Color.White else TextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Чипсы фильтрации
            InventoryFilter.values().forEach { filter ->
                val isSelected = currentFilter == filter
                Box(
                    modifier = Modifier
                        .height(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) PrimaryActionBlue else Color(0xFF1A1E29))
                        .border(
                            1.dp,
                            if (isSelected) PrimaryActionBlue else Color(0xFF282E3D),
                            RoundedCornerShape(10.dp)
                        )
                        .clickable {
                            currentFilter = filter
                            coroutineScope.launch { listState.scrollToItem(0) }
                        }
                        .padding(horizontal = 12.dp)
                        .testTag("filter_${filter.name.lowercase()}_chip"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = filter.label,
                        fontSize = 11.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else TextSecondary
                    )
                }
            }
        }

        // 3. НОВОЕ ПОЛЕ ПОИСКА («Поиск номера»)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(DarkSurface)
                .border(1.dp, GlassBorder, RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.weight(1f)) {
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = "Поиск номера (цифры, оператор, категория)...",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        cursorBrush = SolidColor(PrimaryActionBlue),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (searchQuery.isNotEmpty()) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Очистить",
                        tint = TextSecondary,
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { searchQuery = "" }
                    )
                }
            }
        }

        // 4. Список номеров
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
                        imageVector = if (storageSection == StorageSection.VAULT) Icons.Default.Lock else Icons.Default.Archive,
                        contentDescription = null,
                        tint = Color(0xFF475569),
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        text = if (searchQuery.isNotEmpty()) {
                            "Ничего не найдено по запросу «$searchQuery»"
                        } else if (storageSection == StorageSection.VAULT) {
                            if (poolList.isEmpty()) "Сейф пуст. Положите сюда ценные номера!" else "Нет номеров в сейфе по данному фильтру"
                        } else {
                            if (poolList.isEmpty()) "Инвентарь пуст. Крутите барабан на главном экране!" else "Нет номеров по данному фильтру"
                        },
                        color = TextSecondary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 6.dp)
            ) {
                items(sortedList, key = { "${storageSection.name}_${it.id}" }) { item ->
                    val isActive = activeSim?.id == item.id
                    val effectivePrice = getEffectiveSellPrice(item)
                    InventoryNumberRow(
                        item = item,
                        isActive = isActive,
                        effectiveSellPrice = effectivePrice,
                        onCardClick = { selectedNumberForDetail = item },
                        onSetActiveSim = { onSetActiveSim(item) },
                        onToggleVault = { onToggleVault(item) },
                        onSellSim = { onSellSim(item) }
                    )
                }
            }
        }

        // 5. Нижняя панель массовых действий: Доступна ТОЛЬКО для инвентаря (в сейфе продавать нельзя!)
        if (storageSection == StorageSection.INVENTORY && poolList.isNotEmpty()) {
            val sellableCount = poolList.size
            val commonCount = poolList.count { it.rarity == RarityTier.COMMON }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Кнопка 1: Продать всё из инвентаря
                Box(
                    modifier = Modifier
                        .weight(1.1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (sellableCount > 0) Color(0xFF881337).copy(alpha = 0.45f) else Color(0xFF1E2433))
                        .border(
                            1.dp,
                            if (sellableCount > 0) Color(0xFFF43F5E).copy(alpha = 0.6f) else GlassBorder,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            if (sellableCount > 0) {
                                onSellAllInventory()
                            } else {
                                Toast.makeText(context, "В коллекции нет номеров для продажи", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .testTag("sell_all_inventory_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sell,
                            contentDescription = null,
                            tint = if (sellableCount > 0) Color(0xFFF43F5E) else TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Продать всё ($sellableCount)",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (sellableCount > 0) Color(0xFFF43F5E) else TextMuted,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                // Кнопка 2: Продать обычные (если есть)
                if (commonCount > 0) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF181C26))
                            .border(1.dp, Color(0xFF262C3A), RoundedCornerShape(12.dp))
                            .clickable { onSellAllCommon() }
                            .testTag("sell_all_common_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = null,
                                tint = Color(0xFFF87171),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Обычные ($commonCount)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF87171),
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }
        } else if (storageSection == StorageSection.VAULT && poolList.isNotEmpty()) {
            // Подсказка для сейфа, что продать из сейфа нельзя
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF141824))
                    .border(1.dp, GlassBorder, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = PrimaryActionBlue,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "В сейфе продать нельзя. Сначала переместите в инвентарь.",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }

    // Detail Modal Dialog for chosen number
    selectedNumberForDetail?.let { detailItem ->
        NumberDetailDialog(
            number = detailItem,
            effectiveSellPrice = getEffectiveSellPrice(detailItem),
            isActiveSim = activeSim?.id == detailItem.id,
            canVault = vaultCount < vaultCapacity || detailItem.isInVault,
            onDismiss = { selectedNumberForDetail = null },
            onToggleVault = {
                onToggleVault(detailItem)
                selectedNumberForDetail = null
            },
            onSetActiveSim = {
                onSetActiveSim(detailItem)
                selectedNumberForDetail = null
            },
            onSell = {
                onSellSim(detailItem)
                selectedNumberForDetail = null
            }
        )
    }
}

@Composable
fun InventoryNumberRow(
    item: PhoneNumber,
    isActive: Boolean,
    effectiveSellPrice: Long,
    onCardClick: () -> Unit,
    onSetActiveSim: () -> Unit,
    onToggleVault: () -> Unit,
    onSellSim: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val rarity = item.rarity

    val cardBackground = Brush.horizontalGradient(
        listOf(
            Color(0xFF141720),
            Color(0xFF141720),
            rarity.primaryColor.copy(alpha = 0.22f)
        )
    )

    Surface(
        color = Color.Transparent,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isActive) PrimaryActionBlue else rarity.borderColor.copy(alpha = 0.35f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(cardBackground)
            .clickable { onCardClick() }
            .testTag("inventory_item_${item.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Основной ряд: Слева компактная SIM-карта с номером и оператором, справа информация и цена
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Слева: Компактный бейдж SIM-карты с номером телефона и логотипом оператора
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurfaceElevated)
                        .border(1.dp, GlassBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 9.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OperatorLogo(operatorName = item.operatorName, isMini = true)
                        Text(
                            text = item.formatted,
                            fontFamily = FontFamily.Default,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.5.sp,
                            color = Color.White,
                            letterSpacing = 0.3.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Справа: Колонка с Alignment.End
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "${item.operatorName} · ${item.categoryDescription}",
                        fontSize = 10.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    SegmentedRarityBar(rarity = item.rarity)
                    Text(
                        text = "${formatPrice(effectiveSellPrice)} ₽",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }

            // Ряд аккуратных кнопок действий в сдержанном стиле
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Button 1: Equip
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isActive) PrimaryActionBlue.copy(alpha = 0.22f) else Color(0xFF181C26))
                        .border(
                            1.dp,
                            if (isActive) PrimaryActionBlue else GlassBorder,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onSetActiveSim() }
                        .testTag("equip_sim_${item.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isActive) Icons.Default.Check else Icons.Default.PhoneAndroid,
                            contentDescription = null,
                            tint = if (isActive) PrimaryActionBlue else TextSecondary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isActive) "В сети" else "В телефон",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isActive) PrimaryActionBlue else TextSecondary,
                            maxLines = 1
                        )
                    }
                }

                // Button 2: Vault toggle
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (item.isInVault) Color(0xFF1E283A) else Color(0xFF181C26))
                        .border(
                            1.dp,
                            if (item.isInVault) PrimaryActionBlue else GlassBorder,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { onToggleVault() }
                        .testTag("toggle_vault_${item.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (item.isInVault) Icons.Default.Lock else Icons.Default.Archive,
                            contentDescription = null,
                            tint = if (item.isInVault) PrimaryActionBlue else TextSecondary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (item.isInVault) "Из сейфа" else "В сейф",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (item.isInVault) PrimaryActionBlue else TextSecondary,
                            maxLines = 1
                        )
                    }
                }

                // Button 3: Sell
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (item.isInVault) Color(0xFF13161F) else Color(0xFF181C26))
                        .border(
                            1.dp,
                            if (item.isInVault) Color(0xFF222838) else GlassBorder,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            if (item.isInVault) {
                                Toast.makeText(context, "В сейфе продать нельзя! Сначала нажмите «Из сейфа».", Toast.LENGTH_SHORT).show()
                            } else {
                                onSellSim()
                            }
                        }
                        .testTag("sell_sim_${item.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (item.isInVault) Icons.Default.Lock else Icons.Default.Sell,
                            contentDescription = null,
                            tint = if (item.isInVault) TextMuted else EmeraldProfit,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (item.isInVault) "В сейфе" else "Продать",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (item.isInVault) TextMuted else EmeraldProfit,
                            maxLines = 1
                        )
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
