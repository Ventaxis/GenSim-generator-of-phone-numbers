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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.LiquidGoldMid
import com.example.ui.theme.LiquidGoldStart
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

    val dialogShape = RoundedCornerShape(26.dp)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xF50D111A),
        shape = dialogShape,
        modifier = Modifier.border(1.dp, GlassBorder, dialogShape),
        title = {
            Text(
                text = "⚙️ Настройки автопродажи",
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                color = LiquidGoldStart
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Оставлять номера не ниже ранга:",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.SemiBold
                )

                val rarityOptions = listOf(
                    RarityTier.BRONZE,
                    RarityTier.SILVER,
                    RarityTier.GOLD,
                    RarityTier.PLATINUM
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0x401E293B))
                        .border(1.dp, Color.White.copy(0.08f), RoundedCornerShape(16.dp))
                        .padding(6.dp)
                ) {
                    rarityOptions.forEach { tier ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { selectedRarity = tier }
                                .padding(vertical = 4.dp, horizontal = 6.dp)
                        ) {
                            RadioButton(
                                selected = selectedRarity == tier,
                                onClick = { selectedRarity = tier },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = LiquidGoldMid,
                                    unselectedColor = TextSecondary
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = tier.title,
                                color = tier.primaryColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = "от ${GameViewModel.formatMoney(tier.minPrice)}",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Text(
                    text = "Минимальная цена сохранения:",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.SemiBold
                )

                val pricePresets = listOf(10_000L, 50_000L, 100_000L, 500_000L)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    pricePresets.forEach { preset ->
                        val isSelected = minPrice == preset
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) LiquidGoldMid else Color(0x331E293B))
                                .border(
                                    1.dp,
                                    if (isSelected) LiquidGoldMid else Color.White.copy(0.1f),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { minPrice = preset }
                                .padding(vertical = 9.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (preset >= 1_000_000) "${preset / 1_000_000}M" else "${preset / 1_000}k",
                                color = if (isSelected) Color.Black else TextPrimary,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

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
                            text = "Подходящие номера отправлять в коллекцию",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                    Switch(
                        checked = autoSave,
                        onCheckedChange = { autoSave = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = LiquidGoldMid)
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
                            text = "Пауза на спецсериях и алмазах",
                            fontSize = 13.sp,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Остановить автокрутку при выпадении МВД/ФСБ",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                    Switch(
                        checked = stopOnUltra,
                        onCheckedChange = { stopOnUltra = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = LiquidGoldMid)
                    )
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
                colors = ButtonDefaults.buttonColors(containerColor = LiquidGoldMid, contentColor = Color.Black),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.testTag("save_autosell_btn")
            ) {
                Text("Сохранить", fontWeight = FontWeight.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", color = TextSecondary)
            }
        }
    )
}
