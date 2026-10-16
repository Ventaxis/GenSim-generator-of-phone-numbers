package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.model.RarityTier

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun SegmentedRarityBar(
    rarity: RarityTier,
    modifier: Modifier = Modifier
) {
    val inactiveColor = Color(0xFF2A303D)
    val activeColor = rarity.primaryColor
    val count = rarity.segmentsCount.coerceIn(1, 5)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        for (i in 1..5) {
            val isFilled = i <= count
            Box(
                modifier = Modifier
                    .size(width = 12.dp, height = 3.5.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (isFilled) activeColor else inactiveColor)
            )
        }
    }
}

@Composable
fun MinimalRarityBadge(
    rarity: RarityTier,
    isMini: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(rarity.primaryColor.copy(alpha = 0.12f))
            .border(1.dp, rarity.primaryColor.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
            .padding(horizontal = if (isMini) 6.dp else 10.dp, vertical = if (isMini) 2.dp else 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(rarity.primaryColor)
            )
            Text(
                text = rarity.title,
                fontSize = if (isMini) 10.sp else 12.sp,
                fontWeight = FontWeight.Bold,
                color = rarity.primaryColor
            )
        }
    }
}
