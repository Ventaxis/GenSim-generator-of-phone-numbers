package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RarityTier
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.EmeraldProfit
import com.example.ui.theme.LiquidGoldMid
import com.example.ui.theme.LiquidGoldStart
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.GameViewModel

@Composable
fun DiagnosticLogView(
    viewModel: GameViewModel,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val diagnosticData by viewModel.diagnosticData.collectAsState()
    val eventLogs by viewModel.diagnosticLogs.collectAsState()

    var selectedTableTab by remember { mutableStateOf(0) } // 0: game_stats_log, 1: saved_sims, 2: all

    LaunchedEffect(Unit) {
        viewModel.refreshDiagnosticData()
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("diagnostic_log_view"),
        color = Color(0xF80A0E17)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0x281E293B))
                                .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape)
                                .clickable { onClose() }
                                .testTag("close_diagnostic_log_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Назад",
                                tint = TextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Storage,
                                    contentDescription = null,
                                    tint = LiquidGoldStart,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "ДИАГНОСТИКА ROOM DB",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    color = LiquidGoldStart,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                            Text(
                                text = "Отслеживание: bestNumber и rarityCounts",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0x3310B981))
                                .border(1.dp, EmeraldProfit.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                .clickable {
                                    viewModel.refreshDiagnosticData()
                                    viewModel.addDiagnosticLog("Диагностические данные обновлены вручную.")
                                    Toast.makeText(context, "Данные обновлены", Toast.LENGTH_SHORT).show()
                                }
                                .testTag("diagnostic_refresh_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Обновить",
                                tint = EmeraldProfit,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0x281E293B))
                                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                                .clickable {
                                    val dump = buildRawDumpText(diagnosticData)
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Room_DB_Diagnostic_Dump", dump))
                                    Toast.makeText(context, "Дамп скопирован в буфер", Toast.LENGTH_SHORT).show()
                                }
                                .testTag("copy_diagnostic_dump_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Копировать",
                                tint = TextPrimary,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                }
            }

            // 1. Persistence Status Overview Banner
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (diagnosticData.isOverallPersistenceOk) Color(0x2210B981) else Color(0x33F59E0B))
                        .border(
                            1.dp,
                            if (diagnosticData.isOverallPersistenceOk) EmeraldProfit.copy(alpha = 0.6f) else Color(0xFFF59E0B),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(14.dp)
                        .testTag("persistence_status_banner")
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (diagnosticData.isOverallPersistenceOk) EmeraldProfit else Color(0xFFF59E0B),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (diagnosticData.isOverallPersistenceOk) "ПЕРСИСТЕНТНОСТЬ ПОДТВЕРЖДЕНА" else "ПРОВЕРКА СИНХРОНИЗАЦИИ",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (diagnosticData.isOverallPersistenceOk) EmeraldProfit else Color(0xFFF59E0B)
                                )
                            }

                            Text(
                                text = "БД: ${diagnosticData.dbName} (v${diagnosticData.dbVersion})",
                                fontSize = 10.sp,
                                color = TextSecondary,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Text(
                            text = "Room база данных сохраняет состояние между перезапусками. Отслеживаемые переменные 'bestNumber' и 'rarityCounts' синхронизированы в таблице `game_stats_log` и SharedPreferences.",
                            fontSize = 11.sp,
                            color = TextPrimary,
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            // 2. Specific Variable Tracking: 'bestNumber'
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xD9121824))
                        .border(1.dp, DarkCardBorder, RoundedCornerShape(16.dp))
                        .padding(14.dp)
                        .testTag("diagnostic_best_number")
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "ПЕРЕМЕННАЯ: bestNumber (Рекорд стоимости)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = LiquidGoldStart
                                )
                                Text(
                                    text = "Отслеживание рекорда номера в Room DB и Prefs",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (diagnosticData.isBestNumberPersisted) Color(0x3310B981) else Color(0x33F59E0B))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = if (diagnosticData.isBestNumberPersisted) "СОВПАДАЕТ" else "В ОЧЕРЕДИ",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (diagnosticData.isBestNumberPersisted) EmeraldProfit else Color(0xFFF59E0B)
                                )
                            }
                        }

                        HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                        // Memory vs Room vs Prefs comparison
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            ComparisonRow(
                                source = "In-Memory (StateFlow):",
                                value = diagnosticData.memoryBestNumber?.let { "${it.formatted} (${GameViewModel.formatMoney(it.value)}, ${it.rarity.name})" } ?: "null (ещё не выбит)",
                                isHighlighted = true
                            )
                            ComparisonRow(
                                source = "Room DB (game_stats_log):",
                                value = diagnosticData.roomBestNumberFormatted?.let { "$it (${GameViewModel.formatMoney(diagnosticData.roomBestNumberValue ?: 0)}, ${diagnosticData.roomBestNumberRarity})" } ?: "null (первичная инициализация)"
                            )
                            ComparisonRow(
                                source = "SharedPreferences (Backup):",
                                value = diagnosticData.prefsBestNumber?.let { "${it.formatted} (${GameViewModel.formatMoney(it.value)}, ${it.rarity.name})" } ?: "null"
                            )
                        }

                        // Raw JSON inspect
                        diagnosticData.memoryBestNumber?.let { best ->
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Сырой JSON (bestNumber):",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF090D14))
                                        .padding(8.dp)
                                        .horizontalScroll(rememberScrollState())
                                ) {
                                    Text(
                                        text = best.toJson(),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        color = EmeraldProfit
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. Specific Variable Tracking: 'rarityCounts'
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xD9121824))
                        .border(1.dp, DarkCardBorder, RoundedCornerShape(16.dp))
                        .padding(14.dp)
                        .testTag("diagnostic_rarity_counts")
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "ПЕРЕМЕННАЯ: rarityCounts (Статистика выпадений)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = LiquidGoldStart
                                )
                                Text(
                                    text = "Отслеживание счетчиков по всем категориям редкостей",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (diagnosticData.isRarityCountsPersisted) Color(0x3310B981) else Color(0x33F59E0B))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = if (diagnosticData.isRarityCountsPersisted) "СОВПАДАЕТ" else "В ОЧЕРЕДИ",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (diagnosticData.isRarityCountsPersisted) EmeraldProfit else Color(0xFFF59E0B)
                                )
                            }
                        }

                        HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                        // Grid of rarity counts
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Значения счетчиков по категориям редкости:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            RarityTier.values().forEach { tier ->
                                val memCount = diagnosticData.memoryRarityCounts[tier] ?: 0
                                val roomCount = diagnosticData.roomRarityCounts[tier] ?: 0
                                val prefsCount = diagnosticData.prefsRarityCounts[tier] ?: 0

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0x1A1E293B))
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${tier.name} (${tier.title}):",
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = TextSecondary
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Text(
                                            text = "RAM: $memCount",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (memCount > 0) EmeraldProfit else TextSecondary
                                        )
                                        Text(
                                            text = "Room: $roomCount",
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                        Text(
                                            text = "Prefs: $prefsCount",
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }
                        }

                        // Serialized string in Room DB
                        val serializedRarity = diagnosticData.gameStatsRawRow?.get("rarityCountsJson") as? String ?: ""
                        if (serializedRarity.isNotBlank()) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Сырая строка в Room DB (game_stats_log.rarityCountsJson):",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF090D14))
                                        .padding(8.dp)
                                        .horizontalScroll(rememberScrollState())
                                ) {
                                    Text(
                                        text = serializedRarity,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        color = LiquidGoldMid
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. Raw Room Database Content (SQLite Raw Tables)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xD9121824))
                        .border(1.dp, DarkCardBorder, RoundedCornerShape(16.dp))
                        .padding(14.dp)
                        .testTag("diagnostic_room_db_content")
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "СЫРОЕ СОДЕРЖИМОЕ ROOM DATABASE",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = LiquidGoldStart
                                )
                                Text(
                                    text = "Прямой дамп таблиц SQLite через Room openHelper",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        // Table selector tabs
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TableTabButton(
                                title = "game_stats_log (${if (diagnosticData.gameStatsRawRow != null) 1 else 0})",
                                isSelected = selectedTableTab == 0,
                                onClick = { selectedTableTab = 0 }
                            )
                            TableTabButton(
                                title = "saved_sims (${diagnosticData.savedSimsCount})",
                                isSelected = selectedTableTab == 1,
                                onClick = { selectedTableTab = 1 }
                            )
                            TableTabButton(
                                title = "Все таблицы",
                                isSelected = selectedTableTab == 2,
                                onClick = { selectedTableTab = 2 }
                            )
                        }

                        HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

                        when (selectedTableTab) {
                            0 -> {
                                RawTableRowView(
                                    tableName = "game_stats_log",
                                    rowCount = if (diagnosticData.gameStatsRawRow != null) 1 else 0,
                                    rows = listOfNotNull(diagnosticData.gameStatsRawRow)
                                )
                            }
                            1 -> {
                                RawTableRowView(
                                    tableName = "saved_sims",
                                    rowCount = diagnosticData.savedSimsCount,
                                    rows = diagnosticData.savedSimsRawRows
                                )
                            }
                            else -> {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    RawTableRowView(
                                        tableName = "game_stats_log",
                                        rowCount = if (diagnosticData.gameStatsRawRow != null) 1 else 0,
                                        rows = listOfNotNull(diagnosticData.gameStatsRawRow)
                                    )
                                    RawTableRowView(
                                        tableName = "saved_sims",
                                        rowCount = diagnosticData.savedSimsCount,
                                        rows = diagnosticData.savedSimsRawRows
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 5. Diagnostic Event Logs Console
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xD90D111A))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                        .padding(14.dp)
                        .testTag("diagnostic_event_logs")
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Terminal,
                                    contentDescription = null,
                                    tint = EmeraldProfit,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "ЖУРНАЛ СОБЫТИЙ ПЕРСИСТЕНТНОСТИ",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }

                            Text(
                                text = "${eventLogs.size} записей",
                                fontSize = 10.sp,
                                color = TextSecondary,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF06090F))
                                .border(0.8.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            val scrollState = rememberScrollState()
                            LaunchedEffect(eventLogs.size) {
                                scrollState.animateScrollTo(scrollState.maxValue)
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(scrollState),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (eventLogs.isEmpty()) {
                                    Text(
                                        text = "Журнал пуст. Выполните прокрутку или действие для регистрации событий.",
                                        fontSize = 10.sp,
                                        color = TextSecondary,
                                        fontFamily = FontFamily.Monospace
                                    )
                                } else {
                                    eventLogs.forEach { log ->
                                        Text(
                                            text = log,
                                            fontSize = 10.sp,
                                            color = when {
                                                log.contains("Ошибка") -> Color(0xFFEF4444)
                                                log.contains("сохранено") || log.contains("Синхронизация") -> EmeraldProfit
                                                else -> Color(0xFF94A3B8)
                                            },
                                            fontFamily = FontFamily.Monospace,
                                            lineHeight = 13.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Spacing
            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun ComparisonRow(
    source: String,
    value: String,
    isHighlighted: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = source,
            fontSize = 10.sp,
            color = TextSecondary,
            maxLines = 1
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = value,
            fontSize = 10.sp,
            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal,
            color = if (isHighlighted) EmeraldProfit else TextPrimary,
            fontFamily = FontFamily.Monospace,
            maxLines = 1,
            softWrap = false,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun TableTabButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) LiquidGoldStart.copy(alpha = 0.2f) else Color(0x1A1E293B))
            .border(
                1.dp,
                if (isSelected) LiquidGoldMid else Color.White.copy(alpha = 0.1f),
                RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = title,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) LiquidGoldStart else TextSecondary
        )
    }
}

@Composable
private fun RawTableRowView(
    tableName: String,
    rowCount: Int,
    rows: List<Map<String, Any?>>
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Таблица: `$tableName`",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = LiquidGoldMid
            )
            Text(
                text = "Строк: $rowCount",
                fontSize = 11.sp,
                color = TextSecondary,
                fontFamily = FontFamily.Monospace
            )
        }

        if (rows.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF090D14))
                    .padding(10.dp)
            ) {
                Text(
                    text = "Таблица пуста (0 строк)",
                    fontSize = 10.sp,
                    color = TextSecondary,
                    fontFamily = FontFamily.Monospace
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                rows.take(15).forEachIndexed { index, rowMap ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF090D14))
                            .border(0.6.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                            .horizontalScroll(rememberScrollState())
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Row #$index:",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = LiquidGoldStart,
                                fontFamily = FontFamily.Monospace
                            )
                            rowMap.forEach { (col, valObj) ->
                                Text(
                                    text = "  $col: $valObj",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (col.contains("best", ignoreCase = true) || col.contains("rarity", ignoreCase = true)) {
                                        EmeraldProfit
                                    } else {
                                        Color(0xFFCBD5E1)
                                    }
                                )
                            }
                        }
                    }
                }

                if (rows.size > 15) {
                    Text(
                        text = "... ещё ${rows.size - 15} строк скрыто для краткости",
                        fontSize = 9.sp,
                        color = TextSecondary,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

private fun buildRawDumpText(data: com.example.viewmodel.RoomDiagnosticData): String {
    val sb = StringBuilder()
    sb.appendLine("=== ROOM DATABASE DIAGNOSTIC DUMP ===")
    sb.appendLine("Database Name: ${data.dbName} (v${data.dbVersion})")
    sb.appendLine("Timestamp: ${java.util.Date(data.lastCheckedTimestamp)}")
    sb.appendLine("Overall Persistence Verified: ${data.isOverallPersistenceOk}")
    sb.appendLine()
    sb.appendLine("--- TRACKED VARIABLE: bestNumber ---")
    sb.appendLine("Memory: ${data.memoryBestNumber?.formatted} (${data.memoryBestNumber?.value} ₽, ${data.memoryBestNumber?.rarity?.name})")
    sb.appendLine("Room DB: ${data.roomBestNumberFormatted} (${data.roomBestNumberValue} ₽, ${data.roomBestNumberRarity})")
    sb.appendLine("Prefs: ${data.prefsBestNumber?.formatted} (${data.prefsBestNumber?.value} ₽, ${data.prefsBestNumber?.rarity?.name})")
    sb.appendLine("Persisted & Verified: ${data.isBestNumberPersisted}")
    sb.appendLine()
    sb.appendLine("--- TRACKED VARIABLE: rarityCounts ---")
    sb.appendLine("Memory: ${data.memoryRarityCounts}")
    sb.appendLine("Room DB: ${data.roomRarityCounts}")
    sb.appendLine("Prefs: ${data.prefsRarityCounts}")
    sb.appendLine("Persisted & Verified: ${data.isRarityCountsPersisted}")
    sb.appendLine()
    sb.appendLine("--- TABLE: game_stats_log ---")
    sb.appendLine(data.gameStatsRawRow?.toString() ?: "No rows")
    sb.appendLine()
    sb.appendLine("--- TABLE: saved_sims (Count: ${data.savedSimsCount}) ---")
    data.savedSimsRawRows.forEachIndexed { idx, row ->
        sb.appendLine("Row #$idx: $row")
    }
    return sb.toString()
}
