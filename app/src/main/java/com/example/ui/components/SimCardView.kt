package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PhoneNumber
import com.example.model.RarityTier
import com.example.ui.theme.DiamondCyanStart
import com.example.ui.theme.EmeraldProfit
import com.example.ui.theme.LiquidGoldMid
import com.example.ui.theme.LiquidGoldStart
import com.example.ui.theme.PlatinumSilverStart
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.GameViewModel
import kotlinx.coroutines.launch

@Composable
fun SimCardView(
    phoneNumber: PhoneNumber?,
    modifier: Modifier = Modifier,
    isMini: Boolean = false
) {
    if (phoneNumber == null) {
        EmptySimCardView(modifier = modifier)
        return
    }

    val rarity = phoneNumber.rarity

    // Dynamic drop animation states
    val auraIntensity = remember { Animatable(0f) }
    val sweepAnim = remember { Animatable(0f) }
    val badgeScale = remember { Animatable(1f) }
    val numberScale = remember { Animatable(1f) }
    val priceScale = remember { Animatable(1f) }

    LaunchedEffect(phoneNumber.id) {
        if (!isMini) {
            auraIntensity.snapTo(0.65f)
            badgeScale.snapTo(1.25f)
            numberScale.snapTo(0.93f)
            priceScale.snapTo(1.18f)
            sweepAnim.snapTo(0f)

            launch {
                auraIntensity.animateTo(
                    0.18f,
                    animationSpec = tween(550, easing = LinearOutSlowInEasing)
                )
            }
            launch {
                badgeScale.animateTo(
                    1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            }
            launch {
                numberScale.animateTo(
                    1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            }
            launch {
                priceScale.animateTo(
                    1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            }
            launch {
                sweepAnim.animateTo(
                    1f,
                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                )
            }
        }
    }

    // Clean matte dark titanium gradient (minimalist, modern, no cheap gaudiness)
    val cardBackground = Brush.linearGradient(
        colors = when (rarity) {
            RarityTier.COMMON -> listOf(Color(0xF2121722), Color(0xF20B0F16))
            RarityTier.BRONZE -> listOf(Color(0xF2181412), Color(0xF20F0C0A))
            RarityTier.SILVER -> listOf(Color(0xF2151B26), Color(0xF20E131C))
            RarityTier.GOLD -> listOf(Color(0xF51C170E), Color(0xF5100D06))
            RarityTier.PLATINUM -> listOf(Color(0xF2131C28), Color(0xF20B111A))
            RarityTier.DIAMOND -> listOf(Color(0xF2091F28), Color(0xF2041219))
            RarityTier.POLICE_MVD -> listOf(Color(0xF20C1733), Color(0xF2140A12))
            RarityTier.FSB_KREMLIN -> listOf(Color(0xF2200A10), Color(0xF2160C06))
        }
    )

    val cardShape = RoundedCornerShape(if (isMini) 14.dp else 16.dp)
    val cardBorder = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.18f),
            rarity.borderColor.copy(alpha = if (rarity.isRareOrAbove) 0.55f else 0.35f),
            Color.White.copy(alpha = 0.05f)
        )
    )

    val sizingModifier = if (isMini) {
        modifier.fillMaxWidth()
    } else {
        modifier
            .widthIn(max = 330.dp)
            .fillMaxWidth(0.88f)
    }

    Box(
        modifier = sizingModifier
            .testTag("sim_card_${phoneNumber.id}")
            .drawBehind {
                if (!isMini && auraIntensity.value > 0f) {
                    val glowRadius = size.maxDimension * 0.75f
                    val glowColor = rarity.primaryColor.copy(alpha = auraIntensity.value * if (rarity.isRareOrAbove) 0.75f else 0.35f)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                glowColor,
                                rarity.borderColor.copy(alpha = auraIntensity.value * 0.3f),
                                Color.Transparent
                            ),
                            center = center,
                            radius = glowRadius
                        )
                    )
                }
            }
            .clip(cardShape)
            .background(cardBackground)
            .border(1.dp, cardBorder, cardShape)
            .padding(if (isMini) 10.dp else 13.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(if (isMini) 6.dp else 9.dp)
        ) {
            // 1. Header Row: Operator on left + Minimal Rarity pill on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (rarity.isGovOrSecret) Icons.Default.Security else Icons.Default.SimCard,
                        contentDescription = null,
                        tint = if (rarity.isGovOrSecret) rarity.primaryColor else Color(0xFF64748B),
                        modifier = Modifier.size(if (isMini) 12.dp else 15.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = phoneNumber.operatorName.uppercase(),
                        fontWeight = FontWeight.Bold,
                        fontSize = if (isMini) 10.sp else 11.sp,
                        color = if (rarity.isGovOrSecret) rarity.primaryColor else Color(0xFF94A3B8),
                        letterSpacing = 0.8.sp
                    )
                }

                // Minimalist rarity badge with spring pop
                Box(modifier = Modifier.scale(badgeScale.value)) {
                    MinimalRarityBadge(rarity = rarity, isMini = isMini)
                }
            }

            // 2. Frosted Liquid Glass Number Box (Clean, premium liquid glass reflection)
            val glassBoxShape = RoundedCornerShape(if (isMini) 8.dp else 11.dp)
            val liquidGlassGradient = Brush.verticalGradient(
                colors = listOf(
                    Color(0xB3101726),
                    Color(0xE6080D16)
                )
            )
            val liquidGlassBorder = Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.28f),
                    rarity.primaryColor.copy(alpha = 0.35f),
                    Color.White.copy(alpha = 0.06f)
                ),
                start = Offset(0f, 0f),
                end = Offset(300f, 150f)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(numberScale.value)
                    .clip(glassBoxShape)
                    .background(liquidGlassGradient)
                    .drawBehind {
                        // Liquid Glass Sheen reflection line along the top half
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.08f),
                                    Color.Transparent
                                ),
                                startY = 0f,
                                endY = size.height * 0.45f
                            )
                        )

                        // Smooth light sweep animation on drop
                        if (!isMini && sweepAnim.value > 0f && sweepAnim.value < 1f) {
                            val sweepX = (size.width + 200f) * sweepAnim.value - 100f
                            drawRect(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        rarity.primaryColor.copy(alpha = 0.35f),
                                        Color.White.copy(alpha = 0.65f),
                                        rarity.primaryColor.copy(alpha = 0.35f),
                                        Color.Transparent
                                    ),
                                    start = Offset(sweepX - 70f, 0f),
                                    end = Offset(sweepX + 70f, size.height)
                                )
                            )
                        }
                    }
                    .border(0.9.dp, liquidGlassBorder, glassBoxShape)
                    .padding(vertical = if (isMini) 6.dp else 10.dp, horizontal = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = phoneNumber.formatted,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    fontSize = if (isMini) 15.sp else 20.sp,
                    letterSpacing = 1.2.sp,
                    textAlign = TextAlign.Center,
                    color = when (rarity) {
                        RarityTier.FSB_KREMLIN -> LiquidGoldStart
                        RarityTier.POLICE_MVD -> Color(0xFF93C5FD)
                        RarityTier.DIAMOND -> DiamondCyanStart
                        RarityTier.GOLD -> LiquidGoldMid
                        RarityTier.PLATINUM -> PlatinumSilverStart
                        else -> Color(0xFFF8FAFC)
                    }
                )
            }

            // 3. Ultra-Clean Footer Row: Category on left, Price with Multiplier on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category description
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    if (rarity.isRareOrAbove && !isMini) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = rarity.primaryColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = phoneNumber.categoryDescription,
                        fontSize = if (isMini) 10.sp else 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF94A3B8),
                        maxLines = 1
                    )
                    if (rarity.prestigeStars > 0) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "★".repeat(rarity.prestigeStars),
                            fontSize = 10.sp,
                            color = rarity.primaryColor
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Price (with subtle multiplier pill if > 1.0 and scale pop)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.scale(priceScale.value)
                ) {
                    if (phoneNumber.totalMultiplier > 1.0) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0x24FFB800))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "×${if (phoneNumber.totalMultiplier % 1.0 == 0.0) phoneNumber.totalMultiplier.toInt() else String.format("%.1f", phoneNumber.totalMultiplier)}",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = LiquidGoldStart
                            )
                        }
                        Spacer(modifier = Modifier.width(5.dp))
                    }
                    Text(
                        text = GameViewModel.formatMoney(phoneNumber.value),
                        fontWeight = FontWeight.Black,
                        fontSize = if (isMini) 12.sp else 15.sp,
                        color = EmeraldProfit
                    )
                }
            }
        }
    }
}

@Composable
fun EmptySimCardView(modifier: Modifier = Modifier) {
    val cardShape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .widthIn(max = 330.dp)
            .fillMaxWidth(0.88f)
            .clip(cardShape)
            .background(Color(0xD90E1420))
            .border(1.dp, Color.White.copy(alpha = 0.10f), cardShape)
            .padding(vertical = 22.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.TouchApp,
                contentDescription = null,
                tint = LiquidGoldStart,
                modifier = Modifier.size(26.dp)
            )
            Text(
                text = "Нажмите для генерации",
                color = Color(0xFFE2E8F0),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Ищите редкие серии, красивые цифры и спецномера",
                color = TextSecondary,
                fontSize = 10.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun MinimalRarityBadge(rarity: RarityTier, isMini: Boolean) {
    val badgeBgGradient = when (rarity) {
        RarityTier.COMMON -> Brush.linearGradient(listOf(Color(0x33334155), Color(0x331E293B)))
        RarityTier.BRONZE -> Brush.linearGradient(listOf(Color(0x44CD7F32), Color(0x448C5523)))
        RarityTier.SILVER -> Brush.linearGradient(listOf(Color(0x4494A3B8), Color(0x44475569)))
        RarityTier.GOLD -> Brush.linearGradient(listOf(Color(0x44FFB800), Color(0x44B37400)))
        RarityTier.PLATINUM -> Brush.linearGradient(listOf(Color(0x4438BDF8), Color(0x440284C7)))
        RarityTier.DIAMOND -> Brush.linearGradient(listOf(Color(0x5500E5FF), Color(0x5500B4D8)))
        RarityTier.POLICE_MVD -> Brush.linearGradient(listOf(Color(0x552563EB), Color(0x551D4ED8)))
        RarityTier.FSB_KREMLIN -> Brush.linearGradient(listOf(Color(0x55DC2626), Color(0x55991B1B)))
    }

    val badgeShape = RoundedCornerShape(5.dp)

    Box(
        modifier = Modifier
            .clip(badgeShape)
            .background(badgeBgGradient)
            .border(0.7.dp, rarity.borderColor.copy(alpha = 0.5f), badgeShape)
            .padding(horizontal = if (isMini) 5.dp else 7.dp, vertical = if (isMini) 1.dp else 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = rarity.badgeLabel,
            fontSize = if (isMini) 8.sp else 9.sp,
            fontWeight = FontWeight.Black,
            color = rarity.primaryColor,
            letterSpacing = 0.4.sp
        )
    }
}
