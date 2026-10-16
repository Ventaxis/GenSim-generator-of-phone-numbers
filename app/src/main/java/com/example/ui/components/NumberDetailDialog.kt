package com.example.ui.components

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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PriceCheck
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.PhoneNumber
import com.example.model.RarityTier
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.EmeraldProfit
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.LiquidGoldMid
import com.example.ui.theme.LiquidGoldStart
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.GameViewModel

@Composable
fun NumberDetailDialog(
    number: PhoneNumber,
    effectiveSellPrice: Long,
    isActiveSim: Boolean,
    canVault: Boolean,
    onDismiss: () -> Unit,
    onToggleVault: () -> Unit,
    onSetActiveSim: () -> Unit,
    onSell: () -> Unit
) {
    val context = LocalContext.current
    val rarity = number.rarity
    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .border(
                    width = 1.2.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            rarity.borderColor.copy(alpha = 0.8f),
                            Color.White.copy(alpha = 0.15f),
                            rarity.primaryColor.copy(alpha = 0.3f)
                        )
                    ),
                    shape = RoundedCornerShape(24.dp)
                )
                .testTag("number_detail_dialog"),
            color = DarkSurface,
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header: Operator + Close button
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
                                .background(rarity.primaryColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (rarity.isGovOrSecret) Icons.Default.Security else Icons.Default.SimCard,
                                contentDescription = null,
                                tint = rarity.primaryColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = number.operatorName.uppercase(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = rarity.primaryColor,
                            letterSpacing = 1.sp
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp).testTag("close_detail_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Закрыть",
                            tint = TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Hero Card: Big Phone Number with Monospace font
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF141A28),
                                    Color(0xFF0C101A)
                                )
                            )
                        )
                        .border(
                            width = 1.dp,
                            color = rarity.borderColor.copy(alpha = 0.45f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(vertical = 16.dp, horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = number.formatted,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp,
                            letterSpacing = 0.8.sp,
                            color = when (rarity) {
                                RarityTier.FSB_KREMLIN -> LiquidGoldStart
                                RarityTier.POLICE_MVD -> Color(0xFF93C5FD)
                                RarityTier.DIAMOND -> Color(0xFF00E5FF)
                                RarityTier.GOLD -> LiquidGoldMid
                                else -> TextPrimary
                            },
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Category Description
                        Text(
                            text = number.categoryDescription.ifBlank { "Базовый номер" },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Rarity & Status Badges
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Rarity pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(rarity.primaryColor.copy(alpha = 0.2f))
                            .border(1.dp, rarity.borderColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = rarity.badgeLabel,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = rarity.primaryColor
                            )
                            if (rarity.prestigeStars > 0) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "★".repeat(rarity.prestigeStars),
                                    fontSize = 11.sp,
                                    color = rarity.primaryColor
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    if (number.isInVault) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(LiquidGoldMid.copy(alpha = 0.25f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "🔒 В СЕЙФЕ",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = LiquidGoldStart
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    if (isActiveSim) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(EmeraldProfit.copy(alpha = 0.25f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "🟢 В СЕТИ",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldProfit
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                HorizontalDivider(color = GlassBorder)

                Spacer(modifier = Modifier.height(14.dp))

                // Multipliers Breakdown Section
                Text(
                    text = "ДЕТАЛИЗАЦИЯ И МНОЖИТЕЛИ",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 1.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0x331E293B))
                        .border(1.dp, GlassBorder, RoundedCornerShape(14.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Base Price row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Базовая цена (${rarity.title})",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "${GameViewModel.formatMoney(number.basePrice)} ₽",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    // Each Multiplier
                    if (number.multipliers.isNotEmpty()) {
                        number.multipliers.forEach { mult ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f, fill = false)
                                ) {
                                    if (mult.isHighlighted) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = LiquidGoldStart,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = mult.title,
                                        fontSize = 11.5.sp,
                                        color = if (mult.isHighlighted) LiquidGoldMid else TextSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "×${String.format(java.util.Locale.US, "%.1f", mult.factor)}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (mult.isHighlighted) LiquidGoldStart else TextPrimary
                                )
                            }
                        }
                    }

                    // Total Multiplier row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Итоговый множитель карты",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                        Text(
                            text = "×${String.format(java.util.Locale.US, "%.2f", number.totalMultiplier)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = LiquidGoldStart
                        )
                    }

                    // Broker / Upgrades bonus
                    if (effectiveSellPrice > number.value) {
                        val brokerBonus = effectiveSellPrice - number.value
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Надбавка Биржи и Синдиката",
                                fontSize = 12.sp,
                                color = EmeraldProfit
                            )
                            Text(
                                text = "+${GameViewModel.formatMoney(brokerBonus)} ₽",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldProfit
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Price Summary Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF0F291E),
                                    Color(0xFF081C14)
                                )
                            )
                        )
                        .border(1.dp, EmeraldProfit.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "ЦЕНА ПРОДАЖИ",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldProfit.copy(alpha = 0.8f),
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Чистый доход",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }

                        Text(
                            text = "+${GameViewModel.formatMoney(effectiveSellPrice)} ₽",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = EmeraldProfit
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Row with Vault & Active SIM buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Toggle Vault button
                        OutlinedButton(
                            onClick = onToggleVault,
                            modifier = Modifier.weight(1f).testTag("dialog_vault_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = if (number.isInVault) Color.White else LiquidGoldStart
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (number.isInVault) Color(0x55FFFFFF) else LiquidGoldMid
                            )
                        ) {
                            Icon(
                                imageVector = if (number.isInVault) Icons.Default.LockOpen else Icons.Default.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (number.isInVault) "Из сейфа" else "В сейф",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }

                        // Set Active SIM button
                        OutlinedButton(
                            onClick = onSetActiveSim,
                            modifier = Modifier.weight(1f).testTag("dialog_active_sim_button"),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isActiveSim,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = EmeraldProfit
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isActiveSim) Color(0x3310B981) else EmeraldProfit
                            )
                        ) {
                            Icon(
                                imageVector = if (isActiveSim) Icons.Default.Check else Icons.Default.PhoneAndroid,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isActiveSim) "Активна" else "В телефон",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }

                    // Sell Button (disabled if in vault)
                    Button(
                        onClick = onSell,
                        enabled = !number.isInVault,
                        modifier = Modifier.fillMaxWidth().testTag("dialog_sell_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldProfit,
                            disabledContainerColor = Color(0x331E293B),
                            disabledContentColor = TextMuted
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.PriceCheck,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (number.isInVault) "Заблокировано (в сейфе)" else "Продать за ${GameViewModel.formatMoney(effectiveSellPrice)} ₽",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    // Copy Number Button
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Номер телефона", number.formatted)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Номер скопирован: ${number.formatted}", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth().testTag("dialog_copy_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = TextSecondary
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Скопировать номер",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
