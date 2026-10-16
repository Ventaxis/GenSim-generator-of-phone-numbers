package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PhoneNumber
import com.example.model.RarityTier
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.PrimaryActionBlue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Флаг страны (поддержка России, Беларуси, Казахстана, Украины, ОАЭ, США, Германии)
 */
@Composable
fun CountryFlag(
    countryIso: String,
    modifier: Modifier = Modifier
) {
    val boxModifier = modifier
        .size(width = 16.dp, height = 11.dp)
        .clip(RoundedCornerShape(2.dp))
        .border(0.5.dp, Color(0x55FFFFFF), RoundedCornerShape(2.dp))

    when (countryIso.uppercase()) {
        "UA" -> {
            // Украина (сине-жёлтый)
            Box(modifier = boxModifier) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth().background(Color(0xFF0057B7)))
                    Box(modifier = Modifier.weight(1f).fillMaxWidth().background(Color(0xFFFFDD00)))
                }
            }
        }
        "BY" -> {
            // Беларусь (красно-зелёный)
            Box(modifier = boxModifier) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.weight(2f).fillMaxWidth().background(Color(0xFFC8102E)))
                    Box(modifier = Modifier.weight(1f).fillMaxWidth().background(Color(0xFF007A3D)))
                }
            }
        }
        "KZ" -> {
            // Казахстан (бирюзово-голубой с золотым солнцем)
            Box(
                modifier = boxModifier.background(Color(0xFF00AFCA)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFEC50C))
                )
            }
        }
        "AE" -> {
            // ОАЭ (красная полоса слева, зелёная, белая, чёрная горизонтально)
            Box(modifier = boxModifier) {
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.width(4.5.dp).fillMaxHeight().background(Color(0xFFFF0000)))
                    Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth().background(Color(0xFF00732F)))
                        Box(modifier = Modifier.weight(1f).fillMaxWidth().background(Color.White))
                        Box(modifier = Modifier.weight(1f).fillMaxWidth().background(Color(0xFF000000)))
                    }
                }
            }
        }
        "US" -> {
            // США (красно-белые полосы с синим кантоном)
            Box(modifier = boxModifier) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth().background(Color(0xFFB22234)))
                    Box(modifier = Modifier.weight(1f).fillMaxWidth().background(Color.White))
                    Box(modifier = Modifier.weight(1f).fillMaxWidth().background(Color(0xFFB22234)))
                }
                Box(
                    modifier = Modifier
                        .size(width = 6.dp, height = 5.dp)
                        .background(Color(0xFF3C3B6E))
                )
            }
        }
        "DE" -> {
            // Германия (чёрный, красный, золотой)
            Box(modifier = boxModifier) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth().background(Color(0xFF000000)))
                    Box(modifier = Modifier.weight(1f).fillMaxWidth().background(Color(0xFFFF0000)))
                    Box(modifier = Modifier.weight(1f).fillMaxWidth().background(Color(0xFFFFCC00)))
                }
            }
        }
        else -> {
            // Россия (триколор: белый, синий, красный)
            Box(modifier = boxModifier) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth().background(Color.White))
                    Box(modifier = Modifier.weight(1f).fillMaxWidth().background(Color(0xFF1D6BF3)))
                    Box(modifier = Modifier.weight(1f).fillMaxWidth().background(Color(0xFFE11D48)))
                }
            }
        }
    }
}

@Composable
fun RussianFlag(modifier: Modifier = Modifier) {
    CountryFlag(countryIso = "RU", modifier = modifier)
}

/**
 * Фирменный логотип мобильного оператора
 */
@Composable
fun OperatorLogo(
    operatorName: String,
    isMini: Boolean = false,
    modifier: Modifier = Modifier
) {
    val size = if (isMini) 16.dp else 22.dp

    when {
        // Спецсвязь / Кремль / СБУ / КГБ / Белый дом: золотой герб / звезда
        operatorName.contains("Кремль", ignoreCase = true) ||
        operatorName.contains("АТС", ignoreCase = true) ||
        operatorName.contains("МВД", ignoreCase = true) ||
        operatorName.contains("ФСБ", ignoreCase = true) ||
        operatorName.contains("ФСО", ignoreCase = true) ||
        operatorName.contains("СБУ", ignoreCase = true) ||
        operatorName.contains("КГБ", ignoreCase = true) ||
        operatorName.contains("Акорда", ignoreCase = true) ||
        operatorName.contains("White House", ignoreCase = true) ||
        operatorName.contains("Pentagon", ignoreCase = true) ||
        operatorName.contains("FBI", ignoreCase = true) ||
        operatorName.contains("Police", ignoreCase = true) ||
        operatorName.contains("Royal", ignoreCase = true) ||
        operatorName.contains("Bundestag", ignoreCase = true) ||
        operatorName.contains("Kanzler", ignoreCase = true) -> {
            Box(
                modifier = modifier
                    .size(size)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFBE123C)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "★",
                    fontSize = if (isMini) 10.sp else 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFBBF24)
                )
            }
        }
        // Kyivstar: Синий квадрат с жёлтой звездой
        operatorName.contains("Kyivstar", ignoreCase = true) ||
        operatorName.contains("Киевстар", ignoreCase = true) -> {
            Box(
                modifier = modifier
                    .size(size)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF0077FF)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "★",
                    fontSize = if (isMini) 9.sp else 12.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFFD700)
                )
            }
        }
        // Vodafone: Красный круг с белой каплей/точкой
        operatorName.contains("Vodafone", ignoreCase = true) -> {
            Box(
                modifier = modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(Color(0xFFE60000)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(if (isMini) 6.dp else 8.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
        }
        // A1 Беларусь: красный квадрат с "A1"
        operatorName.contains("A1", ignoreCase = true) -> {
            Box(
                modifier = modifier
                    .size(size)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFE2001A)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "A1",
                    fontSize = if (isMini) 8.sp else 10.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        }
        // Kcell: фиолетовый круг с буквой K
        operatorName.contains("Kcell", ignoreCase = true) -> {
            Box(
                modifier = modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(Color(0xFF6A1B9A)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "K",
                    fontSize = if (isMini) 9.sp else 11.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        }
        // AT&T: синий круг с глобусом
        operatorName.contains("AT&T", ignoreCase = true) -> {
            Box(
                modifier = modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(Color(0xFF00A8E0)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "at&t",
                    fontSize = if (isMini) 6.sp else 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
        // Verizon: чёрный квадрат с красной галочкой
        operatorName.contains("Verizon", ignoreCase = true) -> {
            Box(
                modifier = modifier
                    .size(size)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFF000000)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "V",
                    fontSize = if (isMini) 9.sp else 11.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFCD040B)
                )
            }
        }
        // Telekom DE: маджента с буквой T
        operatorName.contains("Telekom", ignoreCase = true) -> {
            Box(
                modifier = modifier
                    .size(size)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFE20074)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "T",
                    fontSize = if (isMini) 9.sp else 12.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        }
        // e& / du / UAE:
        operatorName.contains("e&", ignoreCase = true) || operatorName.contains("du", ignoreCase = true) -> {
            Box(
                modifier = modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(Color(0xFFE11438)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "e&",
                    fontSize = if (isMini) 7.sp else 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
        // Билайн / Belime: полосатый жёлто-чёрный круг
        operatorName.contains("Билайн", ignoreCase = true) ||
        operatorName.contains("Belime", ignoreCase = true) ||
        operatorName.contains("Bee", ignoreCase = true) -> {
            Box(
                modifier = modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(Color(0xFFFBBF24))
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth().background(Color(0xFFFBBF24)))
                    Box(modifier = Modifier.weight(1f).fillMaxWidth().background(Color(0xFF18181B)))
                    Box(modifier = Modifier.weight(1f).fillMaxWidth().background(Color(0xFFFBBF24)))
                    Box(modifier = Modifier.weight(1f).fillMaxWidth().background(Color(0xFF18181B)))
                }
            }
        }
        // МТС: красный квадрат со скруглёнными углами
        operatorName.contains("МТС", ignoreCase = true) ||
        operatorName.contains("MTS", ignoreCase = true) -> {
            Box(
                modifier = modifier
                    .size(size)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFE11D48)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "M",
                    fontSize = if (isMini) 9.sp else 12.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        }
        // МегаФон: зелёный круг с фиолетовой точкой
        operatorName.contains("Мега", ignoreCase = true) -> {
            Box(
                modifier = modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(Color(0xFF10B981)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(if (isMini) 6.dp else 8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF7C3AED))
                )
            }
        }
        // Tele2 / T2: тёмный квадрат с белой надписью
        operatorName.contains("Tele2", ignoreCase = true) ||
        operatorName.contains("Теле2", ignoreCase = true) ||
        operatorName.contains("T2", ignoreCase = true) -> {
            Box(
                modifier = modifier
                    .size(size)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFF0F172A))
                    .border(0.5.dp, Color(0xFF334155), RoundedCornerShape(3.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "T2",
                    fontSize = if (isMini) 8.sp else 10.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        }
        // Yota или другие: бирюзовый круг
        else -> {
            Box(
                modifier = modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(Color(0xFF06B6D4)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Y",
                    fontSize = if (isMini) 9.sp else 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun SimMicrochip(modifier: Modifier = Modifier, isMini: Boolean = false) {
    val chipWidth = if (isMini) 22.dp else 30.dp
    val chipHeight = if (isMini) 16.dp else 22.dp
    Box(
        modifier = modifier
            .size(width = chipWidth, height = chipHeight)
            .clip(RoundedCornerShape(3.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFE5A93C),
                        Color(0xFFC48821),
                        Color(0xFFFCD34D)
                    )
                )
            )
            .border(0.8.dp, Color(0xFF8D6214), RoundedCornerShape(3.dp))
            .padding(1.5.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.6.dp)
                    .background(Color(0xFF78510D))
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.6.dp)
                    .background(Color(0xFF78510D))
            )
        }
    }
}

/**
 * Тёмная матовая карточка SIM-карты в едином стиле интерфейса
 */
@Composable
fun SimCardView(
    phoneNumber: PhoneNumber?,
    modifier: Modifier = Modifier,
    isMini: Boolean = false,
    isAutoRolling: Boolean = false
) {
    if (phoneNumber == null) {
        EmptySimCardView(modifier = modifier)
        return
    }

    val rarity = phoneNumber.rarity

    // Быстрый визуальный отклик на ручной тап
    val cardScale = remember { Animatable(1f) }
    LaunchedEffect(phoneNumber.id) {
        if (!isMini && !isAutoRolling) {
            cardScale.snapTo(0.97f)
            cardScale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 70, easing = FastOutSlowInEasing)
            )
        }
    }

    val sizingModifier = if (isMini) {
        modifier.fillMaxWidth()
    } else {
        modifier
            .widthIn(max = 350.dp)
            .fillMaxWidth()
    }

    // Разбор цифр номера: +7 [prefix] [first3]-[mid2]-[last2]
    val prefix = phoneNumber.prefix
    val digits = phoneNumber.digits
    val first3 = if (digits.length >= 3) digits.substring(0, 3) else digits
    val mid2 = if (digits.length >= 5) digits.substring(3, 5) else ""
    val last2 = if (digits.length >= 7) digits.substring(5, 7) else ""

    // Проверка совпадения паттернов для синего подчёркивания
    val underlinePrefixAndFirst = prefix.length == 3 && first3.length == 3 && prefix == first3
    val underlineMidAndLast = mid2.isNotEmpty() && mid2 == last2
    val underlineFirst3Identical = first3.length == 3 && first3[0] == first3[1] && first3[1] == first3[2]
    val underlinePrefixIdentical = prefix.length == 3 && prefix[0] == prefix[1] && prefix[1] == prefix[2]

    val displayOperatorName = when {
        phoneNumber.operatorName.contains("Билайн", ignoreCase = true) -> "Belime"
        else -> phoneNumber.operatorName
    }

    // Индекс редкости (например, "1 из 22")
    val rarityPoolCount = when (rarity) {
        RarityTier.COMMON -> 10
        RarityTier.BRONZE -> 16
        RarityTier.SILVER -> 22
        RarityTier.GOLD -> 35
        RarityTier.PLATINUM -> 50
        RarityTier.DIAMOND -> 77
        RarityTier.POLICE_MVD -> 15
        RarityTier.FSB_KREMLIN -> 7
    }

    val cardBorderColor = if (rarity.ordinal >= RarityTier.GOLD.ordinal) {
        rarity.borderColor.copy(alpha = 0.55f)
    } else {
        GlassBorder
    }

    // Тёмная карточка SIM-карты с чётким отображением номера
    Box(
        modifier = sizingModifier
            .testTag("sim_card_${phoneNumber.id}")
            .scale(if (!isAutoRolling) cardScale.value else 1f)
            .clip(RoundedCornerShape(if (isMini) 12.dp else 16.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        DarkSurface,
                        Color(0xFF11141C)
                    )
                )
            )
            .border(
                width = 1.2.dp,
                color = cardBorderColor,
                shape = RoundedCornerShape(if (isMini) 12.dp else 16.dp)
            )
            .padding(
                horizontal = if (isMini) 10.dp else 16.dp,
                vertical = if (isMini) 8.dp else 14.dp
            )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Верхняя строка: Логотип оператора + "Россия" слева, Золотой микрочип справа
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Слева: Логотип оператора, имя и мини-флаг России
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(if (isMini) 5.dp else 8.dp)
                    ) {
                        OperatorLogo(operatorName = displayOperatorName, isMini = isMini)
                        Text(
                            text = displayOperatorName,
                            fontSize = if (isMini) 13.sp else 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(if (isMini) 2.dp else 3.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(if (isMini) 4.dp else 6.dp)
                    ) {
                        CountryFlag(
                            countryIso = phoneNumber.countryIso,
                            modifier = Modifier.size(if (isMini) 12.dp else 16.dp, if (isMini) 8.dp else 11.dp)
                        )
                        Text(
                            text = phoneNumber.countryName,
                            fontSize = if (isMini) 9.5.sp else 11.5.sp,
                            fontWeight = FontWeight.Normal,
                            color = TextSecondary,
                            maxLines = 1
                        )
                    }
                }

                // Справа: Золотой чип SIM-карты с вырубкой
                SimMicrochip(isMini = isMini)
            }

            Spacer(modifier = Modifier.height(if (isMini) 8.dp else 14.dp))

            // 2. Номер телефона: яркий, белый, чётко виден, всегда строго в 1 строку!
            val formattedNumber = phoneNumber.formatted
            val isEliteRepeated = formattedNumber.contains("999 999") ||
                                 formattedNumber.contains("888 888") ||
                                 formattedNumber.contains("777 777") ||
                                 formattedNumber.contains("8888") ||
                                 formattedNumber.contains("9999") ||
                                 formattedNumber.contains("7777")

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = formattedNumber,
                    fontSize = if (isMini) {
                        13.sp
                    } else if (formattedNumber.length > 21) {
                        17.5.sp
                    } else if (formattedNumber.length > 18) {
                        19.5.sp
                    } else {
                        22.5.sp
                    },
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Default,
                    color = Color.White,
                    letterSpacing = 0.4.sp,
                    maxLines = 1,
                    softWrap = false
                )
                if (!isMini && isEliteRepeated) {
                    Box(
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .height(2.dp)
                            .fillMaxWidth(0.5f)
                            .clip(RoundedCornerShape(1.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(PrimaryActionBlue, Color(0xFF60A5FA), Color.Transparent)
                                )
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(if (isMini) 6.dp else 10.dp))

            // 3. Нижняя строка: Категория слева + Редкость справа (строго в 1 строку)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = phoneNumber.categoryDescription,
                    fontSize = if (isMini) 9.sp else 11.sp,
                    color = TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "${rarity.title}, 1 из $rarityPoolCount",
                    fontSize = if (isMini) 9.5.sp else 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = rarity.primaryColor,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * Пустая заготовка карты до первого ролла — в едином тёмном стиле
 */
@Composable
fun EmptySimCardView(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .widthIn(max = 350.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        DarkSurface,
                        Color(0xFF11141C)
                    )
                )
            )
            .border(1.2.dp, GlassBorder, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OperatorLogo(operatorName = "Belime")
                        Text(
                            text = "Belime",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        RussianFlag()
                        Text(
                            text = "Россия",
                            fontSize = 11.5.sp,
                            color = TextSecondary
                        )
                    }
                }
                SimMicrochip()
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Чётко видимый заполнитель номера на тёмном фоне
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "+7 (999) •••-••-••",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White.copy(alpha = 0.9f),
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.BottomEnd
            ) {
                Text(
                    text = "Нажмите «Выпустить ещё»",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = PrimaryActionBlue
                )
            }
        }
    }
}
