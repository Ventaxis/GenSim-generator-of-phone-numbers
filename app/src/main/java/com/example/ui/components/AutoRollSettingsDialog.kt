package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AutoSellConfig
import com.example.model.RarityTier
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.PrimaryActionBlue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.GameViewModel

@Composable
fun AutoRollSettingsDialog(
    config: AutoSellConfig,
    onDismiss: () -> Unit,
    onSave: (AutoSellConfig) -> Unit
) {
    var selectedRarity by remember { mutableStateOf(config.autoSellBelowRarity) }
    var minPrice by remember { mutableStateOf(config.minKeepPrice) }
    var autoSave by remember { mutableStateOf(config.autoSaveKeptToInventory) }
    var stopOnUltra by remember { mutableStateOf(config.stopOnUltraRare) }

    // Expanded settings: Speed, Haptics, Audio, High-value confirmation
    var speedPreset by remember { mutableStateOf("fast") } // "normal", "fast", "turbo"
    var hapticFeedbackEnabled by remember { mutableStateOf(true) }
    var soundEffectsEnabled by remember { mutableStateOf(true) }
    var highValueSellConfirmation by remember { mutableStateOf(true) }

    val dialogShape = RoundedCornerShape(22.dp)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF13161F),
        shape = dialogShape,
        modifier = Modifier.border(1.dp, GlassBorder, dialogShape),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⚙️ Настройки игры и генератора",
                    fontWeight = FontWeight.Black,
                    fontSize = 17.sp,
                    color = TextPrimary
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // SECTION 1: АВТОПРОДАЖА И ФИЛЬТРЫ
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "ФИЛЬТР РЕДКОСТИ ДЛЯ СОХРАНЕНИЯ",
                        fontSize = 11.5.sp,
                        color = PrimaryActionBlue,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Оставлять номера не ниже ранга:",
                        fontSize = 12.5.sp,
                        color = TextSecondary
                    )

                    val rarityOptions = listOf(
                        RarityTier.COMMON to "Все номера",
                        RarityTier.BRONZE to "Бронза+",
                        RarityTier.SILVER to "Серебро+",
                        RarityTier.GOLD to "Золото+",
                        RarityTier.PLATINUM to "Платина+"
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurfaceElevated)
                            .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                            .padding(4.dp)
                    ) {
                        rarityOptions.forEach { (tier, label) ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { selectedRarity = tier }
                                    .padding(vertical = 4.dp, horizontal = 6.dp)
                            ) {
                                RadioButton(
                                    selected = selectedRarity == tier,
                                    onClick = { selectedRarity = tier },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = PrimaryActionBlue,
                                        unselectedColor = TextMuted
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = label,
                                    color = if (tier == RarityTier.COMMON) TextPrimary else tier.primaryColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = "от ${GameViewModel.formatMoney(tier.minPrice)}",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                // SECTION 2: МИНИМАЛЬНАЯ ЦЕНА
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "МИНИМАЛЬНАЯ ЦЕНА СОХРАНЕНИЯ",
                        fontSize = 11.5.sp,
                        color = PrimaryActionBlue,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )

                    val pricePresets = listOf(50_000L, 250_000L, 1_000_000L, 5_000_000L, 25_000_000L)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        pricePresets.forEach { preset ->
                            val isSelected = minPrice == preset
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) PrimaryActionBlue else DarkSurfaceElevated)
                                    .border(
                                        1.dp,
                                        if (isSelected) PrimaryActionBlue else GlassBorder,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { minPrice = preset }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (preset >= 1_000_000) "${preset / 1_000_000}M" else "${preset / 1_000}k",
                                    color = if (isSelected) Color.White else TextSecondary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                // SECTION 3: СКОРОСТЬ АВТОПРОКРУТКИ
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "СКОРОСТЬ АВТОПРОКРУТКИ",
                        fontSize = 11.5.sp,
                        color = PrimaryActionBlue,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val speeds = listOf(
                            "normal" to "1x Норма",
                            "fast" to "2x Быстро",
                            "turbo" to "5x Турбо"
                        )
                        speeds.forEach { (key, label) ->
                            val isSelected = speedPreset == key
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) PrimaryActionBlue else DarkSurfaceElevated)
                                    .border(
                                        1.dp,
                                        if (isSelected) PrimaryActionBlue else GlassBorder,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { speedPreset = key }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else TextSecondary
                                )
                            }
                        }
                    }
                }

                // SECTION 4: ПЕРЕКЛЮЧАТЕЛИ (СЕЙФ, СПЕЦСЕРИИ, ЗВУК, ВИБРАЦИЯ)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(DarkSurfaceElevated)
                        .border(1.dp, GlassBorder, RoundedCornerShape(14.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Auto-save toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Сохранять в сейф",
                                fontSize = 13.sp,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Ценные номера сразу отправлять в сейф",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                        Switch(
                            checked = autoSave,
                            onCheckedChange = { autoSave = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PrimaryActionBlue
                            )
                        )
                    }

                    // Stop on Ultra-Rare
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Пауза на спецсериях",
                                fontSize = 13.sp,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Остановить автокрутку при МВД/ФСБ/Бриллиантах",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                        Switch(
                            checked = stopOnUltra,
                            onCheckedChange = { stopOnUltra = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PrimaryActionBlue
                            )
                        )
                    }

                    // Haptics toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Тактильная отдача",
                                fontSize = 13.sp,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Вибрация при тапе и выбивании редких SIM",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                        Switch(
                            checked = hapticFeedbackEnabled,
                            onCheckedChange = { hapticFeedbackEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PrimaryActionBlue
                            )
                        )
                    }

                    // Sound toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Звуковые эффекты",
                                fontSize = 13.sp,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Щелчки генератора и фанфары побед",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                        Switch(
                            checked = soundEffectsEnabled,
                            onCheckedChange = { soundEffectsEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PrimaryActionBlue
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        config.copy(
                            autoSellBelowRarity = selectedRarity,
                            minKeepPrice = minPrice,
                            autoSaveKeptToInventory = autoSave,
                            stopOnUltraRare = stopOnUltra
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryActionBlue,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("save_autosell_btn")
            ) {
                Text("Сохранить", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Закрыть", color = TextSecondary)
            }
        }
    )
}
