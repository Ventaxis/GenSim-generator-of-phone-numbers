package com.example.ui.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UpgradeType
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.EmeraldProfit
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.LiquidGoldMid
import com.example.ui.theme.LiquidGoldStart
import com.example.ui.theme.PoliceBlueEnd
import com.example.ui.theme.PoliceBlueStart
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.GameViewModel

@Composable
fun UpgradesTab(
    balance: Long,
    upgrades: Map<String, Int>,
    onBuyUpgrade: (UpgradeType) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "МАГАЗИН И СВЯЗИ",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = LiquidGoldStart
                )
                Text(
                    text = "Прокачивайте серверную АТС, расширяйте сейф и получайте доступ к закрытым правительственным спецбазам РФ.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )
            }
        }

        items(UpgradeType.values(), key = { it.id }) { upgrade ->
            val currLevel = upgrades[upgrade.id] ?: 0
            val isMax = currLevel >= upgrade.maxLevel
            val price = upgrade.getPrice(currLevel)
            val canAfford = balance >= price && !isMax

            val icon: ImageVector = when (upgrade) {
                UpgradeType.INVENTORY_CAPACITY -> Icons.Default.Inventory
                UpgradeType.MVD_INSIDER -> Icons.Default.Security
                UpgradeType.GOLDEN_DATABASE -> Icons.Default.Star
                UpgradeType.TURBO_ROLL -> Icons.Default.Bolt
                UpgradeType.VAULT_EXPANSION -> Icons.Default.Inventory
                UpgradeType.ELITE_BROKER -> Icons.Default.AttachMoney
                UpgradeType.QUANTUM_SCANNER -> Icons.Default.AutoAwesome
                UpgradeType.SATELLITE_ORBIT -> Icons.Default.TrendingUp
                UpgradeType.OPERATOR_CARTEL -> Icons.Default.AccountBalance
                UpgradeType.PRESIDENTIAL_HOTLINE -> Icons.Default.MilitaryTech
                UpgradeType.NEURAL_CLUSTER -> Icons.Default.Bolt
                UpgradeType.OFFSHORE_SYNDICATE -> Icons.Default.TrendingUp
                UpgradeType.OLIGARCH_MONOPOLY -> Icons.Default.Star
                UpgradeType.KREMLIN_VERTUSHKA -> Icons.Default.MilitaryTech
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(DarkSurface)
                    .border(
                        1.2.dp,
                        if (upgrade == UpgradeType.MVD_INSIDER && currLevel > 0) PoliceBlueStart.copy(alpha = 0.6f)
                        else GlassBorder,
                        RoundedCornerShape(22.dp)
                    )
                    .padding(16.dp)
                    .testTag("upgrade_${upgrade.id}")
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    if (upgrade == UpgradeType.MVD_INSIDER) PoliceBlueStart.copy(alpha = 0.2f)
                                    else LiquidGoldMid.copy(alpha = 0.15f)
                                )
                                .border(
                                    1.dp,
                                    if (upgrade == UpgradeType.MVD_INSIDER) PoliceBlueStart.copy(alpha = 0.4f)
                                    else LiquidGoldMid.copy(alpha = 0.3f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (upgrade == UpgradeType.MVD_INSIDER) PoliceBlueStart else LiquidGoldStart,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = upgrade.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "Уровень $currLevel / ${upgrade.maxLevel}",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Text(
                        text = upgrade.description,
                        fontSize = 12.sp,
                        color = Color(0xFFCBD5E1),
                        lineHeight = 16.sp
                    )

                    // Rounded Progress Bar
                    LinearProgressIndicator(
                        progress = { currLevel.toFloat() / upgrade.maxLevel.toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (upgrade == UpgradeType.MVD_INSIDER) PoliceBlueStart else LiquidGoldMid,
                        trackColor = Color(0x331E293B)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isMax) "Максимальный уровень" else "Цена: ${GameViewModel.formatMoney(price)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isMax) LiquidGoldStart else if (canAfford) EmeraldProfit else Color(0xFFFB7185)
                        )

                        Button(
                            onClick = { onBuyUpgrade(upgrade) },
                            enabled = canAfford,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (upgrade == UpgradeType.MVD_INSIDER) PoliceBlueStart else LiquidGoldMid,
                                contentColor = if (upgrade == UpgradeType.MVD_INSIDER) Color.White else Color.Black,
                                disabledContainerColor = Color(0x331E293B),
                                disabledContentColor = Color(0xFF64748B)
                            ),
                            modifier = Modifier.testTag("buy_upgrade_${upgrade.id}")
                        ) {
                            if (isMax) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("МАКС.", fontSize = 11.sp, fontWeight = FontWeight.Black)
                            } else {
                                Text("ПРОКАЧАТЬ", fontSize = 11.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }
    }
}
