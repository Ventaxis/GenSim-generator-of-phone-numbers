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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Archive
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Public
import com.example.generator.PhoneGenerator
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import com.example.model.PlayerProgression
import kotlin.math.abs
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
import com.example.model.PhoneNumber
import com.example.model.RarityTier
import com.example.model.UpdateState
import com.example.ui.components.AutoRollSettingsDialog
import com.example.ui.components.CountryFlag
import com.example.ui.components.CountrySelectionDialog
import com.example.ui.components.DrawerDestination
import com.example.ui.components.GameDrawerContent
import com.example.ui.components.LevelPerksDialog
import com.example.ui.components.NumberDetailDialog
import com.example.ui.components.RarityCatalogDialog
import com.example.ui.components.SimCardView
import com.example.ui.components.UpdatePromptDialog
import com.example.ui.tabs.InventoryTab
import com.example.ui.tabs.OtaUpdateScreen
import com.example.ui.tabs.QuestsTab
import com.example.ui.tabs.StatsTab
import com.example.ui.tabs.UpgradesTab
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DeepObsidian
import com.example.ui.theme.EmeraldProfit
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.LiquidGoldMid
import com.example.ui.theme.LiquidGoldStart
import com.example.ui.theme.PrimaryActionBlue
import com.example.ui.theme.TextMuted
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
    val luckPityRolls by viewModel.luckPityRolls.collectAsState()
    val isLuckBoostActive by viewModel.isLuckBoostActive.collectAsState()
    val selectedCountryIso by viewModel.selectedCountryIso.collectAsState()
    val updateState by viewModel.updateState.collectAsState()
    val showUpdateDialog by viewModel.showUpdateDialog.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var currentSubScreen by remember { mutableStateOf(DrawerDestination.NONE) }
    var showAutoSellSettingsModal by remember { mutableStateOf(false) }
    var showLevelPerksModal by remember { mutableStateOf(false) }
    var showRarityCatalogModal by remember { mutableStateOf(false) }
    var showCountrySelectionModal by remember { mutableStateOf(false) }
    var alertEvent by remember { mutableStateOf<GameEvent.Alert?>(null) }
    var celebrationEvent by remember { mutableStateOf<GameEvent.Celebration?>(null) }
    var levelUpEvent by remember { mutableStateOf<GameEvent.LevelUp?>(null) }
    var selectedNumberForDetail by remember { mutableStateOf<PhoneNumber?>(null) }

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

    if (showUpdateDialog && updateState is UpdateState.Available) {
        val availableInfo = (updateState as UpdateState.Available).info
        UpdatePromptDialog(
            updateInfo = availableInfo,
            onDismiss = { viewModel.dismissUpdateDialog(availableInfo.tagName) },
            onConfirmUpdate = {
                viewModel.dismissUpdateDialog()
                viewModel.startDownloadUpdate(availableInfo)
                currentSubScreen = DrawerDestination.OTA_UPDATES
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

    if (showRarityCatalogModal) {
        RarityCatalogDialog(
            onDismissRequest = { showRarityCatalogModal = false }
        )
    }

    if (showCountrySelectionModal) {
        CountrySelectionDialog(
            selectedIso = selectedCountryIso,
            onSelectCountry = { viewModel.selectCountry(it) },
            onDismissRequest = { showCountrySelectionModal = false }
        )
    }

    selectedNumberForDetail?.let { detailSim ->
        NumberDetailDialog(
            number = detailSim,
            effectiveSellPrice = viewModel.getEffectiveSellPrice(detailSim),
            isActiveSim = activeSim?.id == detailSim.id,
            canVault = viewModel.vaultCount < viewModel.vaultCapacity || detailSim.isInVault,
            onDismiss = { selectedNumberForDetail = null },
            onToggleVault = {
                viewModel.toggleVaultStatus(detailSim)
                selectedNumberForDetail = null
            },
            onSetActiveSim = {
                viewModel.setActiveSim(detailSim)
                selectedNumberForDetail = null
            },
            onSell = {
                if (currentNumber?.id == detailSim.id) {
                    viewModel.sellCurrent()
                } else {
                    viewModel.sellFromInventory(detailSim)
                }
                selectedNumberForDetail = null
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = DarkBg,
                modifier = Modifier.width(290.dp)
            ) {
                GameDrawerContent(
                    balance = balance,
                    levelInfo = levelInfo,
                    passiveIncome = viewModel.passiveIncomePerSec,
                    activeSim = activeSim,
                    vaultCount = viewModel.vaultCount,
                    vaultCapacity = viewModel.vaultCapacity,
                    inventoryCount = inventory.count { !it.isInVault },
                    unclaimedQuestsCount = unclaimedQuests,
                    onSelectDestination = { dest ->
                        scope.launch { drawerState.close() }
                        if (dest == DrawerDestination.AUTO_SELL_SETTINGS) {
                            showAutoSellSettingsModal = true
                        } else if (dest == DrawerDestination.LEVEL_PERKS) {
                            showLevelPerksModal = true
                        } else if (dest == DrawerDestination.RARITY_CATALOG) {
                            showRarityCatalogModal = true
                        } else if (dest == DrawerDestination.SELECT_COUNTRY) {
                            showCountrySelectionModal = true
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
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // 1. Top Header: Hamburger + Avatar & Level Pill | Large Balance
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Menu button with notification badge for unclaimed quests
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(DarkSurface)
                                .border(1.dp, GlassBorder, RoundedCornerShape(10.dp))
                                .clickable { scope.launch { drawerState.open() } }
                                .testTag("menu_hamburger_btn"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Меню",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            if (unclaimedQuests > 0) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = 2.dp, y = (-2).dp)
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFF43F5E))
                                )
                            }
                        }

                        // Avatar + Level pill
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(18.dp))
                                .background(DarkSurface)
                                .border(1.dp, GlassBorder, RoundedCornerShape(18.dp))
                                .clickable { showLevelPerksModal = true }
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                                .testTag("top_level_pill"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryActionBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                            Text(
                                text = "Ур. ${levelInfo.level}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF22C55E))
                            )
                        }

                        // Country Selector Pill
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(18.dp))
                                .background(DarkSurface)
                                .border(1.dp, GlassBorder, RoundedCornerShape(18.dp))
                                .clickable { showCountrySelectionModal = true }
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                                .testTag("top_country_pill"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (selectedCountryIso == "AUTO") {
                                Text("🌐", fontSize = 11.sp)
                                Text("Авто", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = PrimaryActionBlue)
                            } else {
                                CountryFlag(countryIso = selectedCountryIso, modifier = Modifier.size(width = 14.dp, height = 10.dp))
                                Text(selectedCountryIso, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }

                    // Balance
                    Text(
                        text = GameViewModel.formatMoney(balance),
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                        color = TextPrimary,
                        modifier = Modifier
                            .clickable { showLevelPerksModal = true }
                            .testTag("balance_pill")
                    )
                }

                // 2. Quest / Promo banner below top bar
                val activeQuest = quests.firstOrNull { !it.isCompleted }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 3.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurface)
                        .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                        .clickable { currentSubScreen = DrawerDestination.QUESTS }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkSurfaceElevated),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = Color(0xFFFBBF24),
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = activeQuest?.title ?: "Квесты оператора",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    softWrap = false
                                )
                                Text(
                                    text = activeQuest?.let { "+${GameViewModel.formatMoney(it.rewardAmount)} ₽" } ?: "Выполняйте задания для бонусов",
                                    fontSize = 10.sp,
                                    color = TextSecondary,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (unclaimedQuests > 0) Color(0xFFF59E0B) else PrimaryActionBlue)
                                .clickable { currentSubScreen = DrawerDestination.QUESTS }
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (unclaimedQuests > 0) "Забрать" else "Открыть",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // 3. Center Generator Area (Authentic White SIM Card + Actions)
                val current = currentNumber
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
                                viewModel.rollSingle()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (notification != null) {
                        Surface(
                            color = Color(0xF2161922),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF252A36)),
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = PrimaryActionBlue,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = notification ?: "",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White
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
                    ) {
                        // Sleek Dark SIM Card
                        SimCardView(
                            phoneNumber = current,
                            isAutoRolling = autoSellConfig.isAutoRolling
                        )

                        // Under SIM card info line: Редкий · оценка 17 165 · 1/22 · +10 XP
                        if (current != null) {
                            val seriesIndex = remember(current.id) { (abs(current.digits.hashCode()) % 22) + 1 }
                            val xpReward = remember(current.id) { PlayerProgression.getXpRewardForNumber(current) }

                            Row(
                                modifier = Modifier
                                    .padding(top = 10.dp, bottom = 8.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = current.rarity.title,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = current.rarity.primaryColor,
                                    maxLines = 1,
                                    softWrap = false
                                )
                                Text(
                                    text = " • ${GameViewModel.formatMoney(current.value)} ₽",
                                    fontSize = 11.5.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    softWrap = false
                                )
                                Text(
                                    text = " • $seriesIndex/22",
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF8A94A6),
                                    maxLines = 1,
                                    softWrap = false
                                )
                                Text(
                                    text = " • +$xpReward XP",
                                    fontSize = 11.5.sp,
                                    color = Color(0xFFFBBF24),
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }

                            // 5 Action Pills Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val isVaulted = isCurrentSaved || current.isInVault
                                // Pill 1: В сейф / В сейфе 🔒
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(if (isVaulted) Color(0x1F10B981) else DarkSurface)
                                        .border(1.dp, if (isVaulted) EmeraldProfit else GlassBorder, RoundedCornerShape(18.dp))
                                        .clickable { viewModel.saveCurrentToVault() }
                                        .padding(horizontal = 12.dp, vertical = 7.dp)
                                        .testTag("quick_save_btn"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (isVaulted) "В сейфе 🔒" else "В сейф",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isVaulted) EmeraldProfit else Color.White
                                    )
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                // Pill 2: Продать
                                val canQuickSell = !isVaulted
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(DarkSurface)
                                        .border(1.dp, GlassBorder, RoundedCornerShape(18.dp))
                                        .clickable(enabled = canQuickSell) { viewModel.sellCurrent() }
                                        .padding(horizontal = 12.dp, vertical = 7.dp)
                                        .testTag("quick_sell_btn"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Продать",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (canQuickSell) Color.White else TextMuted
                                    )
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                // Pill 3: Страна 🌍
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(DarkSurface)
                                        .border(1.dp, GlassBorder, RoundedCornerShape(18.dp))
                                        .clickable { showCountrySelectionModal = true }
                                        .padding(horizontal = 10.dp, vertical = 7.dp)
                                        .testTag("action_country_btn"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        if (selectedCountryIso == "AUTO") {
                                            Text("🌐", fontSize = 12.sp)
                                            Text(
                                                text = "Авто",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = PrimaryActionBlue
                                            )
                                        } else {
                                            CountryFlag(countryIso = selectedCountryIso, modifier = Modifier.size(width = 14.dp, height = 10.dp))
                                            Text(
                                                text = selectedCountryIso,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                // Pill 4: Level perks ✨
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(DarkSurface)
                                        .border(1.dp, GlassBorder, CircleShape)
                                        .clickable { showLevelPerksModal = true },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "Перки",
                                        tint = Color.White,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                // Pill 4: Details dialog ↗
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(DarkSurface)
                                        .border(1.dp, GlassBorder, CircleShape)
                                        .clickable { selectedNumberForDetail = current },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NorthEast,
                                        contentDescription = "Подробнее",
                                        tint = Color.White,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. Luck Boost Progress Bar (Pity System)
                val rollsTillBoost = (100 - luckPityRolls).coerceAtLeast(0)
                val boostProgress = (luckPityRolls / 100f).coerceIn(0f, 1f)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isLuckBoostActive) Color(0x26F59E0B) else DarkSurface)
                        .border(
                            1.dp,
                            if (isLuckBoostActive) LiquidGoldMid else GlassBorder,
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (isLuckBoostActive) Icons.Default.Bolt else Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = if (isLuckBoostActive) LiquidGoldMid else EmeraldProfit,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = if (isLuckBoostActive) "5X УДАЧА АКТИВНА!" else "До 5X удачи: $rollsTillBoost ${pluralizeRolls(rollsTillBoost)}",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isLuckBoostActive) LiquidGoldMid else Color.White
                                )
                            }

                            Text(
                                text = if (isLuckBoostActive) "ГОТОВО (100%)" else "$luckPityRolls / 100",
                                fontSize = 11.sp,
                                fontWeight = if (isLuckBoostActive) FontWeight.Black else FontWeight.Normal,
                                color = if (isLuckBoostActive) LiquidGoldMid else TextSecondary
                            )
                        }

                        LinearProgressIndicator(
                            progress = { if (isLuckBoostActive) 1f else boostProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = if (isLuckBoostActive) LiquidGoldMid else EmeraldProfit,
                            trackColor = DarkSurfaceElevated
                        )

                        if (isLuckBoostActive) {
                            Text(
                                text = "✨ Следующий выпуск гарантирует 5-кратную удачу и редкие номера!",
                                fontSize = 11.sp,
                                color = Color(0xFFFFD54F),
                                lineHeight = 13.sp
                            )
                        }
                    }
                }

                // 6. Bottom Roll Controls: Auto + Cog + Primary Roll Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isAuto = autoSellConfig.isAutoRolling

                    // Multiplier / Auto-roll pill
                    Box(
                        modifier = Modifier
                            .height(46.dp)
                            .widthIn(min = 64.dp)
                            .clip(RoundedCornerShape(23.dp))
                            .background(if (isAuto) Color(0xFFE11D48) else DarkSurface)
                            .border(
                                1.dp,
                                if (isAuto) Color(0xFFF43F5E) else GlassBorder,
                                RoundedCornerShape(23.dp)
                            )
                            .clickable { viewModel.toggleAutoRoll() }
                            .padding(horizontal = 12.dp)
                            .testTag("bottom_autoroll_pill"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isAuto) "СТОП" else "АВТО",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Auto-sell settings cog
                    Box(
                        modifier = Modifier
                            .size(width = 42.dp, height = 46.dp)
                            .clip(RoundedCornerShape(23.dp))
                            .background(DarkSurface)
                            .border(1.dp, GlassBorder, RoundedCornerShape(23.dp))
                            .clickable { showAutoSellSettingsModal = true }
                            .testTag("auto_sell_settings_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Настройки",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Country Selection button
                    Box(
                        modifier = Modifier
                            .size(width = 44.dp, height = 46.dp)
                            .clip(RoundedCornerShape(23.dp))
                            .background(DarkSurface)
                            .border(1.dp, GlassBorder, RoundedCornerShape(23.dp))
                            .clickable { showCountrySelectionModal = true }
                            .testTag("bottom_country_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedCountryIso == "AUTO") {
                            Text("🌐", fontSize = 17.sp)
                        } else {
                            CountryFlag(
                                countryIso = selectedCountryIso,
                                modifier = Modifier.size(width = 20.dp, height = 14.dp)
                            )
                        }
                    }

                    // Primary Roll Button: Electric Blue in normal mode, Gold in 5X Luck mode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .clip(RoundedCornerShape(23.dp))
                            .background(if (isLuckBoostActive) LiquidGoldMid else PrimaryActionBlue)
                            .border(
                                width = 1.dp,
                                color = if (isLuckBoostActive) Color(0xFFFFE082) else Color(0xFF60A5FA).copy(alpha = 0.4f),
                                shape = RoundedCornerShape(23.dp)
                            )
                            .clickable {
                                if (!isAuto) {
                                    viewModel.rollSingle()
                                }
                            }
                            .padding(horizontal = 16.dp)
                            .testTag("primary_roll_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (isLuckBoostActive) {
                                    Icon(
                                        imageVector = Icons.Default.Bolt,
                                        contentDescription = null,
                                        tint = Color(0xFF0F172A),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = if (isLuckBoostActive) "ВЫПУСТИТЬ (5X)" else "Выпустить ещё",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isLuckBoostActive) Color(0xFF0F172A) else Color.White
                                )
                            }

                            Text(
                                text = GameViewModel.formatMoney(viewModel.rollCost),
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isLuckBoostActive) Color(0xFF0F172A) else Color.White
                            )
                        }
                    }
                }

                // 4. Authentic Game Bottom Navigation Dock
                Surface(
                    color = DarkSurface,
                    modifier = Modifier.fillMaxWidth(),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. Inventory
                        BottomNavTabItem(
                            icon = Icons.Default.Inventory2,
                            label = "Инвентарь",
                            badgeText = "${inventory.count { !it.isInVault }}",
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
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(DarkSurface)
                                    .border(1.dp, GlassBorder, CircleShape)
                                    .clickable { currentSubScreen = DrawerDestination.NONE }
                                    .testTag("subscreen_back_btn"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Назад",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = when (currentSubScreen) {
                                    DrawerDestination.QUESTS -> "Задания и Награды"
                                    DrawerDestination.INVENTORY -> "Коллекция номеров"
                                    DrawerDestination.VAULT -> "Сейф"
                                    DrawerDestination.UPGRADES -> "Магазин и Улучшения"
                                    DrawerDestination.STATS -> "Досье и Справочник"
                                    DrawerDestination.OTA_UPDATES -> "Обновления игры (OTA)"
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
                                onClaimReward = { viewModel.claimQuestReward(it) },
                                onRefreshQuests = { viewModel.refreshQuests() }
                            )
                            DrawerDestination.INVENTORY -> InventoryTab(
                                inventory = inventory,
                                activeSim = activeSim,
                                vaultCount = viewModel.vaultCount,
                                vaultCapacity = viewModel.vaultCapacity,
                                inventoryCapacity = viewModel.inventoryCapacity,
                                initialVaultSection = false,
                                getEffectiveSellPrice = { viewModel.getEffectiveSellPrice(it) },
                                onSetActiveSim = { viewModel.setActiveSim(it) },
                                onToggleVault = { viewModel.toggleVaultStatus(it) },
                                onSellSim = { viewModel.sellFromInventory(it) },
                                onSellAllCommon = { viewModel.sellAllCommonFromInventory() },
                                onSellAllInventory = { viewModel.sellAllFromInventory() }
                            )
                            DrawerDestination.VAULT -> InventoryTab(
                                inventory = inventory,
                                activeSim = activeSim,
                                vaultCount = viewModel.vaultCount,
                                vaultCapacity = viewModel.vaultCapacity,
                                inventoryCapacity = viewModel.inventoryCapacity,
                                initialVaultSection = true,
                                getEffectiveSellPrice = { viewModel.getEffectiveSellPrice(it) },
                                onSetActiveSim = { viewModel.setActiveSim(it) },
                                onToggleVault = { viewModel.toggleVaultStatus(it) },
                                onSellSim = { viewModel.sellFromInventory(it) },
                                onSellAllCommon = { viewModel.sellAllCommonFromInventory() },
                                onSellAllInventory = { viewModel.sellAllFromInventory() }
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
                            DrawerDestination.OTA_UPDATES -> OtaUpdateScreen(
                                viewModel = viewModel
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
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier.padding(top = 2.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isBadgeHighlight) EmeraldProfit else TextSecondary,
                modifier = Modifier
                    .padding(end = if (badgeText != null && isBadgeHighlight) 4.dp else 0.dp)
                    .size(19.dp)
            )
            if (badgeText != null && isBadgeHighlight) {
                Box(
                    modifier = Modifier
                        .offset(x = 8.dp, y = (-4).dp)
                        .sizeIn(minWidth = 16.dp, minHeight = 16.dp)
                        .clip(CircleShape)
                        .background(EmeraldProfit)
                        .padding(horizontal = 3.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Black,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        style = androidx.compose.material3.LocalTextStyle.current.copy(
                            platformStyle = androidx.compose.ui.text.PlatformTextStyle(
                                includeFontPadding = false
                            ),
                            lineHeight = 9.sp
                        ),
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(3.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isBadgeHighlight) EmeraldProfit else TextPrimary,
                maxLines = 1,
                softWrap = false,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            if (badgeText != null && !isBadgeHighlight) {
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = badgeText,
                    fontSize = 8.5.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

private fun pluralizeRolls(count: Int): String {
    val mod10 = count % 10
    val mod100 = count % 100
    return when {
        mod100 in 11..19 -> "номеров"
        mod10 == 1 -> "номер"
        mod10 in 2..4 -> "номера"
        else -> "номеров"
    }
}

