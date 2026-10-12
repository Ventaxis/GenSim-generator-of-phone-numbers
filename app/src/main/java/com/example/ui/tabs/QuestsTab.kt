package com.example.ui.tabs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Upgrade
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Quest
import com.example.model.QuestPeriod
import com.example.ui.theme.EmeraldProfit
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.LiquidGoldEnd
import com.example.ui.theme.LiquidGoldMid
import com.example.ui.theme.LiquidGoldStart
import com.example.ui.theme.PoliceBlueStart
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.GameViewModel

@Composable
fun QuestsTab(
    quests: List<Quest>,
    onClaimReward: (String) -> Unit
) {
    var selectedPeriod by remember { mutableStateOf(QuestPeriod.DAILY) }

    val filteredQuests = remember(quests, selectedPeriod) {
        quests.filter { it.period == selectedPeriod }
    }

    val totalInPeriod = filteredQuests.size
    val completedInPeriod = filteredQuests.count { it.isCompleted }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "ЗАДАНИЯ И НАГРАДЫ",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = LiquidGoldStart
                )
                Text(
                    text = "Выполняйте цели дня, недели и месяца, получайте крупные денежные премии и развивайте свою телеком-империю.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )
            }
        }

        // Period Switcher Pills (Daily, Weekly, Monthly)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0x331E293B))
                    .border(1.dp, GlassBorder, RoundedCornerShape(18.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                QuestPeriod.values().forEach { period ->
                    val isSelected = selectedPeriod == period
                    val unclaimedCount = quests.count { it.period == period && it.isCompleted && !it.isClaimed }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isSelected) LiquidGoldMid else Color.Transparent
                            )
                            .clickable { selectedPeriod = period }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = period.title,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                color = if (isSelected) Color.Black else TextSecondary
                            )
                            if (unclaimedCount > 0) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .sizeIn(minWidth = 18.dp, minHeight = 18.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) Color.Black else EmeraldProfit)
                                        .padding(horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$unclaimedCount",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (isSelected) LiquidGoldStart else Color.Black,
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
            }
        }

        // Completion status card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xD9121722))
                    .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Прогресс периода: ${selectedPeriod.title}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "$completedInPeriod / $totalInPeriod выполнено",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = if (completedInPeriod == totalInPeriod) EmeraldProfit else LiquidGoldStart
                        )
                    }

                    LinearProgressIndicator(
                        progress = { if (totalInPeriod > 0) completedInPeriod.toFloat() / totalInPeriod.toFloat() else 0f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = LiquidGoldMid,
                        trackColor = Color(0x331E293B)
                    )
                }
            }
        }

        // Quests List
        items(filteredQuests, key = { it.id }) { quest ->
            val icon = getQuestIcon(quest.categoryIcon)
            val isClaimable = quest.isCompleted && !quest.isClaimed

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xD9121722))
                    .border(
                        1.2.dp,
                        if (isClaimable) EmeraldProfit.copy(alpha = 0.8f) else GlassBorder,
                        RoundedCornerShape(22.dp)
                    )
                    .padding(16.dp)
                    .testTag("quest_item_${quest.id}")
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Header: Icon + Title + Reward Tag
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isClaimable) EmeraldProfit.copy(alpha = 0.2f)
                                    else LiquidGoldMid.copy(alpha = 0.15f)
                                )
                                .border(
                                    1.dp,
                                    if (isClaimable) EmeraldProfit else LiquidGoldMid.copy(alpha = 0.3f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isClaimable) EmeraldProfit else LiquidGoldStart,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = quest.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = quest.description,
                                fontSize = 11.sp,
                                color = TextSecondary,
                                lineHeight = 15.sp
                            )
                        }
                    }

                    // Progress bar
                    LinearProgressIndicator(
                        progress = { quest.progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (quest.isCompleted) EmeraldProfit else LiquidGoldMid,
                        trackColor = Color(0x331E293B)
                    )

                    // Bottom Row: Progress Count & Claim Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${quest.currentProgress} / ${quest.targetProgress}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (quest.isCompleted) EmeraldProfit else TextSecondary
                        )

                        when {
                            quest.isClaimed -> {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = EmeraldProfit,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "ПОЛУЧЕНО",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = EmeraldProfit
                                    )
                                }
                            }
                            isClaimable -> {
                                Button(
                                    onClick = { onClaimReward(quest.id) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = EmeraldProfit,
                                        contentColor = Color.Black
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("claim_quest_${quest.id}")
                                ) {
                                    Text(
                                        text = "ЗАБРАТЬ +${GameViewModel.formatMoney(quest.rewardAmount)}",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                            else -> {
                                Text(
                                    text = "Награда: ${GameViewModel.formatMoney(quest.rewardAmount)}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = LiquidGoldStart
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun getQuestIcon(iconType: String): ImageVector {
    return when (iconType) {
        "roll" -> Icons.Default.Casino
        "vault" -> Icons.Default.Archive
        "money" -> Icons.Default.AttachMoney
        "gov" -> Icons.Default.Security
        "upgrade" -> Icons.Default.Upgrade
        else -> Icons.Default.Star
    }
}
