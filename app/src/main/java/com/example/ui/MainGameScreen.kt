package com.example.ui

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AutoSellConfig
import com.example.ui.components.AutoRollSettingsDialog
import com.example.ui.components.DrawerDestination
import com.example.ui.components.GameDrawerContent
import com.example.ui.components.LevelPerksDialog
import com.example.ui.components.SimCardView
import com.example.ui.tabs.InventoryTab
import com.example.ui.tabs.QuestsTab
import com.example.ui.tabs.StatsTab
import com.example.ui.tabs.UpgradesTab
import com.example.ui.theme.DeepObsidian
import com.example.ui.theme.EmeraldProfit
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.LiquidGoldMid
import com.example.ui.theme.LiquidGoldStart
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.GameEvent
import com.example.viewmodel.GameViewModel
import kotlinx.coroutines.launch

@Composable
fun MainGameScreen(viewModel: GameViewModel) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    val balance by viewModel.balance.collectAsState()
    val levelInfo by viewModel.levelInfo.collectAsState()
    val lastXpGained by viewModel.lastXpGained.collectAsState()
    val currentNumber by viewModel.currentNumber.collectAsState()
    val isCurrentSaved by viewModel.isCurrentSaved.collectAsState()
    val inventory by viewModel.inventory.collectAsState()
    val activeSim by viewModel.activeSim.collectAsState()
    val upgrades by viewModel.upgrades.collectAsState()
    val autoSellConfig by viewModel.autoSellConfig.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val quests by viewModel.quests.collectAsState()
    val notification by viewModel.notification.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var currentSubScreen by remember { mutableStateOf(DrawerDestination.NONE) }
    var showAutoSellSettingsModal by remember { mutableStateOf(false) }
    var showLevelPerksModal by remember { mutableStateOf(false) }
    var alertEvent by remember { mutableStateOf<GameEvent.Alert?>(null) }
    var celebrationEvent by remember { mutableStateOf<GameEvent.Celebration?>(null) }
    var levelUpEvent by remember { mutableStateOf<GameEvent.LevelUp?>(null) }

    var isTappedState by remember { mutableStateOf(false) }
    val cardScale by animateFloatAsState(
        targetValue = if (isTappedState) 0.94f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "card_scale"
    )

    val unclaimedQuests = viewModel.unclaimedQuestsCount

    BackHandler(enabled = drawerState.isOpen || currentSubScreen != DrawerDestination.NONE) {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
        } else if (currentSubScreen != DrawerDestination.NONE) {
            currentSubScreen = DrawerDestination.NONE
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is GameEvent.Haptic -> {
                    try {
                        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                        if (vibrator != null && vibrator.hasVibrator()) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                val effect = if (event.isHeavy) {
                                    VibrationEffect.createWaveform(longArrayOf(0, 70, 50, 110), -1)
                                } else {
                                    VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE)
                                }
                                vibrator.vibrate(effect)
                            } else {
                                @Suppress("DEPRECATION")
                                vibrator.vibrate(if (event.isHeavy) 110 else 20)
                            }
                        } else {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                    } catch (_: Exception) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                }
                is GameEvent.Alert -> {
                    alertEvent = event
                }
                is GameEvent.Celebration -> {
                    celebrationEvent = event
                }
                is GameEvent.LevelUp -> {
                    levelUpEvent = event
                }
                is GameEvent.XpGained -> {
                    // Handled automatically via lastXpGained StateFlow
                }
            }
        }
    }

    // Level-up celebration modal
    if (levelUpEvent != null) {
        AlertDialog(
            onDismissRequest = { levelUpEvent = null },
            containerColor = Color(0xF50F1828),
            shape = RoundedCornerShape(24.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MilitaryTech,
                        contentDescription = null,
                        tint = LiquidGoldStart,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "НОВЫЙ УРОВЕНЬ ${levelUpEvent?.newLevel}!",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = LiquidGoldStart
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Ваш новый ранг: «${levelUpEvent?.title}»",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "Премия за повышение: +${GameViewModel.formatMoney(levelUpEvent?.bonusReward ?: 0)}",
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        color = EmeraldProfit
                    )
                    Text(
                        text = "Бонус: +2 слота в сейф и +5% к пассивному доходу!",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { levelUpEvent = null },
                    colors = ButtonDefaults.buttonColors(containerColor = LiquidGoldMid, contentColor = Color.Black),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("ПРИНЯТЬ", fontWeight = FontWeight.Black)
                }
            }
        )
    }

    if (alertEvent != null) {
        AlertDialog(
            onDismissRequest = { alertEvent = null },
            containerColor = Color(0xF5101626),
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = alertEvent?.title ?: "",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = LiquidGoldMid
                )
            },
            text = {
                Text(
                    text = alertEvent?.message ?: "",
                    fontSize = 15.sp,
                    color = TextPrimary,
                    lineHeight = 22.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { alertEvent = null },
                    colors = ButtonDefaults.buttonColors(containerColor = LiquidGoldMid, contentColor = Color.Black),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("В СЕЙФ", fontWeight = FontWeight.Black)
                }
            }
        )
    }

    if (celebrationEvent != null) {
        AlertDialog(
            onDismissRequest = { celebrationEvent = null },
            containerColor = Color(0xF50C241B),
            shape = RoundedCornerShape(24.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = EmeraldProfit,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "НАГРАДА ПОЛУЧЕНА!",
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp,
                        color = EmeraldProfit
                    )
                }
            },
            text = {
                Text(
                    text = "Задание «${celebrationEvent?.title}» выполнено!\nНачислено: +${GameViewModel.formatMoney(celebrationEvent?.reward ?: 0)}",
                    fontSize = 14.sp,
                    color = TextPrimary,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { celebrationEvent = null },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldProfit, contentColor = Color.Black),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("ОТЛИЧНО!", fontWeight = FontWeight.Black)
                }
            }
        )
    }

    if (showAutoSellSettingsModal) {
        AutoRollSettingsDialog(
            config = autoSellConfig,
            onDismiss = { showAutoSellSettingsModal = false },
            onSave = { updated ->
                viewModel.updateAutoSellConfig(updated)
                showAutoSellSettingsModal = false
            }
        )
    }

    if (showLevelPerksModal) {
        LevelPerksDialog(
            levelInfo = levelInfo,
            onDismiss = { showLevelPerksModal = false }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color(0xF50A0E17),
                modifier = Modifier.width(320.dp)
            ) {
                GameDrawerContent(
                    balance = balance,
                    levelInfo = levelInfo,
                    passiveIncome = viewModel.passiveIncomePerSec,
                    activeSim = activeSim,
                    vaultCount = viewModel.vaultCount,
                    vaultCapacity = viewModel.vaultCapacity,
                    inventoryCount = inventory.size,
                    unclaimedQuestsCount = unclaimedQuests,
                    onSelectDestination = { dest ->
                        scope.launch { drawerState.close() }
                        if (dest == DrawerDestination.AUTO_SELL_SETTINGS) {
                            showAutoSellSettingsModal = true
                        } else if (dest == DrawerDestination.LEVEL_PERKS) {
                            showLevelPerksModal = true
                        } else {
                            currentSubScreen = dest
                        }
                    },
                    onCloseDrawer = {
                        scope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DeepObsidian)
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0x281D4ED8), Color.Transparent),
                            center = Offset(size.width * 0.15f, size.height * 0.22f),
                            radius = size.width * 0.65f
                        )
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0x28FFB800), Color.Transparent),
                            center = Offset(size.width * 0.85f, size.height * 0.72f),
                            radius = size.width * 0.7f
                        )
                    )
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // 1. Top Bar: Minimalist & Compact
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Menu button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0x281E293B))
                            .border(0.8.dp, GlassBorder, CircleShape)
                            .clickable { scope.launch { drawerState.open() } }
                            .testTag("menu_hamburger_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Меню",
                            tint = LiquidGoldStart,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Level XP Indicator Capsule
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0x281E293B))
                            .border(0.8.dp, LiquidGoldMid.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                            .clickable { showLevelPerksModal = true }
                            .padding(horizontal = 9.dp, vertical = 5.dp)
                            .testTag("top_level_pill"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = LiquidGoldStart,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Ур. ${levelInfo.level}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = LiquidGoldStart
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${(levelInfo.progressFraction * 100).toInt()}%",
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    // Balance Capsule
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0x3D0C261B))
                            .border(0.8.dp, Color(0x6610B981), RoundedCornerShape(16.dp))
                            .padding(horizontal = 11.dp, vertical = 5.dp)
                            .testTag("balance_pill"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = GameViewModel.formatMoney(balance),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = EmeraldProfit
                        )
                    }
                }

                // 2. Center Generator Area (Compact card, plenty of space, not stretching edge-to-edge)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("tap_area_fullscreen")
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (!autoSellConfig.isAutoRolling) {
                                isTappedState = true
                                viewModel.rollSingle()
                                scope.launch {
                                    kotlinx.coroutines.delay(80)
                                    isTappedState = false
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Floating notification banner overlay (does not shift layout)
                    androidx.compose.animation.AnimatedVisibility(
                        visible = notification != null,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
                        exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 8.dp)
                    ) {
                        Surface(
                            color = Color(0xF21E293B),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(0.8.dp, Color.White.copy(alpha = 0.20f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = LiquidGoldMid,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = notification ?: "",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                            }
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .scale(cardScale)
                    ) {
                        // Subtle floating XP gained indicator
                        AnimatedVisibility(
                            visible = lastXpGained != null,
                            enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
                            exit = fadeOut() + slideOutVertically(targetOffsetY = { -it })
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(bottom = 8.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xE6102B20))
                                    .border(0.8.dp, EmeraldProfit.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = LiquidGoldStart,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "+$lastXpGained XP",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldProfit
                                    )
                                }
                            }
                        }

                        // Compact SIM Card View with smooth drop animations
                        AnimatedContent(
                            targetState = currentNumber,
                            transitionSpec = {
                                (fadeIn(animationSpec = tween(170, easing = LinearOutSlowInEasing)) +
                                 scaleIn(initialScale = 0.88f, animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium)) +
                                 slideInVertically(initialOffsetY = { 24 }, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)))
                                .togetherWith(
                                    fadeOut(animationSpec = tween(100, easing = FastOutLinearInEasing)) +
                                    scaleOut(targetScale = 0.95f, animationSpec = tween(100))
                                )
                            },
                            label = "sim_card_drop_transition",
                            contentAlignment = Alignment.Center
                        ) { animatedSim ->
                            SimCardView(
                                phoneNumber = animatedSim
                            )
                        }

                        // Quick Actions (В сейф / Продать) with smooth fade-in
                        AnimatedVisibility(
                            visible = currentNumber != null && !autoSellConfig.isAutoRolling,
                            enter = fadeIn(animationSpec = tween(180)) + slideInVertically(initialOffsetY = { 16 }),
                            exit = fadeOut(animationSpec = tween(100)) + slideOutVertically(targetOffsetY = { 16 })
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val isVaulted = isCurrentSaved || currentNumber?.isInVault == true
                                    // Save to Vault
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(18.dp))
                                            .background(if (isVaulted) Color(0x3310B981) else Color(0x331E293B))
                                            .border(
                                                0.8.dp,
                                                if (isVaulted) EmeraldProfit.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.15f),
                                                RoundedCornerShape(18.dp)
                                            )
                                            .clickable { viewModel.saveCurrentToVault() }
                                            .padding(horizontal = 14.dp, vertical = 8.dp)
                                            .testTag("quick_save_btn"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Archive,
                                                contentDescription = null,
                                                tint = if (isVaulted) EmeraldProfit else LiquidGoldMid,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(5.dp))
                                            Text(
                                                text = if (isVaulted) "В сейфе ✓" else "В сейф (${viewModel.vaultCount}/${viewModel.vaultCapacity})",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isVaulted) EmeraldProfit else TextPrimary
                                            )
                                        }
                                    }

                                    // Quick Sell
                                    val canQuickSell = !isVaulted
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(18.dp))
                                            .background(if (canQuickSell) Color(0x330E3022) else Color(0x221E293B))
                                            .border(
                                                0.8.dp,
                                                if (canQuickSell) Color(0x6610B981) else Color.White.copy(alpha = 0.15f),
                                                RoundedCornerShape(18.dp)
                                            )
                                            .clickable { viewModel.sellCurrent() }
                                            .padding(horizontal = 14.dp, vertical = 8.dp)
                                            .testTag("quick_sell_btn"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = if (canQuickSell) Icons.Default.AttachMoney else Icons.Default.Lock,
                                                contentDescription = null,
                                                tint = if (canQuickSell) EmeraldProfit else TextSecondary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = if (canQuickSell) "Продать (+${GameViewModel.formatMoney(currentNumber?.value ?: 0)})" else "В сейфе 🔒",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (canQuickSell) EmeraldProfit else TextSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 3. Compact Roll Controls (Primary roll button + Auto toggle + Settings)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isAuto = autoSellConfig.isAutoRolling

                    // Main ROLL button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xE61E293B), Color(0xE60F172A))
                                )
                            )
                            .border(1.dp, LiquidGoldMid.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                            .clickable {
                                if (!isAuto) {
                                    isTappedState = true
                                    viewModel.rollSingle()
                                    scope.launch {
                                        kotlinx.coroutines.delay(80)
                                        isTappedState = false
                                    }
                                }
                            }
                            .testTag("primary_roll_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Casino,
                                contentDescription = null,
                                tint = LiquidGoldStart,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "КРУТИТЬ (-${viewModel.rollCost} ₽)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = LiquidGoldStart,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    // Auto-roll toggle button
                    Box(
                        modifier = Modifier
                            .height(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isAuto) Brush.horizontalGradient(listOf(Color(0xE6E11D48), Color(0xE6BE123C)))
                                else Brush.horizontalGradient(listOf(Color(0x33334155), Color(0x331E293B)))
                            )
                            .border(
                                1.dp,
                                if (isAuto) Color.White.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.15f),
                                RoundedCornerShape(14.dp)
                            )
                            .clickable { viewModel.toggleAutoRoll() }
                            .padding(horizontal = 14.dp)
                            .testTag("bottom_autoroll_pill"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isAuto) Icons.Default.Pause else Icons.Default.FlashOn,
                                contentDescription = null,
                                tint = if (isAuto) Color.White else LiquidGoldMid,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isAuto) "СТОП" else "АВТО",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isAuto) Color.White else TextPrimary
                            )
                        }
                    }

                    // Auto-sell settings cog
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0x221E293B))
                            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
                            .clickable { showAutoSellSettingsModal = true }
                            .testTag("auto_sell_settings_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Настройки автокрутки",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // 4. Authentic Game Bottom Navigation Dock
                Surface(
                    color = Color(0xF50D111A),
                    modifier = Modifier.fillMaxWidth(),
                    border = androidx.compose.foundation.BorderStroke(0.8.dp, Color.White.copy(alpha = 0.08f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. Inventory
                        BottomNavTabItem(
                            icon = Icons.Default.Inventory2,
                            label = "Инвентарь",
                            badgeText = "${inventory.size}",
                            onClick = { currentSubScreen = DrawerDestination.INVENTORY }
                        )

                        // 2. Shop / Upgrades
                        BottomNavTabItem(
                            icon = Icons.Default.ShoppingCart,
                            label = "Магазин",
                            badgeText = null,
                            onClick = { currentSubScreen = DrawerDestination.UPGRADES }
                        )

                        // 3. Quests
                        BottomNavTabItem(
                            icon = Icons.Default.EmojiEvents,
                            label = "Квесты",
                            badgeText = if (unclaimedQuests > 0) "$unclaimedQuests" else null,
                            isBadgeHighlight = unclaimedQuests > 0,
                            onClick = { currentSubScreen = DrawerDestination.QUESTS }
                        )

                        // 4. Stats / Dossier
                        BottomNavTabItem(
                            icon = Icons.Default.MilitaryTech,
                            label = "Досье",
                            badgeText = "ур.${levelInfo.level}",
                            onClick = { currentSubScreen = DrawerDestination.STATS }
                        )
                    }
                }
            }

            // Sub-Screen Overlay (Slide-in)
            AnimatedVisibility(
                visible = currentSubScreen != DrawerDestination.NONE,
                enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
                exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DeepObsidian
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                            .navigationBarsPadding()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color(0x331E293B))
                                    .border(1.dp, GlassBorder, CircleShape)
                                    .clickable { currentSubScreen = DrawerDestination.NONE }
                                    .testTag("subscreen_back_btn"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Назад",
                                    tint = LiquidGoldStart,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = when (currentSubScreen) {
                                    DrawerDestination.QUESTS -> "Задания и Награды"
                                    DrawerDestination.INVENTORY -> "Инвентарь"
                                    DrawerDestination.UPGRADES -> "Магазин и Улучшения"
                                    DrawerDestination.STATS -> "Досье и Справочник"
                                    else -> ""
                                },
                                fontWeight = FontWeight.Black,
                                fontSize = 19.sp,
                                color = TextPrimary
                            )
                        }

                        when (currentSubScreen) {
                            DrawerDestination.QUESTS -> QuestsTab(
                                quests = quests,
                                onClaimReward = { viewModel.claimQuestReward(it) }
                            )
                            DrawerDestination.INVENTORY -> InventoryTab(
                                inventory = inventory,
                                activeSim = activeSim,
                                vaultCount = viewModel.vaultCount,
                                vaultCapacity = viewModel.vaultCapacity,
                                onSetActiveSim = { viewModel.setActiveSim(it) },
                                onToggleVault = { viewModel.toggleVaultStatus(it) },
                                onSellSim = { viewModel.sellFromInventory(it) },
                                onSellAllCommon = { viewModel.sellAllCommonFromInventory() }
                            )
                            DrawerDestination.UPGRADES -> UpgradesTab(
                                balance = balance,
                                upgrades = upgrades,
                                onBuyUpgrade = { viewModel.buyUpgrade(it) }
                            )
                            DrawerDestination.STATS -> StatsTab(
                                stats = stats,
                                levelInfo = levelInfo
                            )
                            else -> {}
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavTabItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    badgeText: String? = null,
    isBadgeHighlight: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isBadgeHighlight) EmeraldProfit else LiquidGoldStart,
                modifier = Modifier.size(19.dp)
            )
            if (isBadgeHighlight) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(EmeraldProfit)
                )
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            if (badgeText != null && !isBadgeHighlight) {
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = badgeText,
                    fontSize = 9.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

