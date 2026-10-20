package ru.finni.financialpetgame.lct.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.finni.financialpetgame.lct.R
import ru.finni.financialpetgame.lct.data.GameSnapshot
import ru.finni.financialpetgame.lct.domain.ActiveDeposit
import ru.finni.financialpetgame.lct.domain.DepositTerm
import ru.finni.financialpetgame.lct.domain.DepositTerms
import ru.finni.financialpetgame.lct.ui.theme.NunitoFontFamily

private val DepositBackground = Color(0xFFFBF1FF)
private val DepositPurple = Color(0xFF843AD1)
private val DepositDark = Color(0xFF4B2163)
private val DepositLight = Color(0xFFE3B1FA)

@Composable
internal fun DepositScreen(
    state: GameSnapshot,
    onBack: () -> Unit,
    onOpen: (DepositTerm, Int) -> Unit,
    onClose: () -> Unit,
    showEducationInitially: Boolean,
    onEducationSeen: () -> Unit,
) {
    val deposit = state.activeDeposit
    var showEducation by rememberSaveable { mutableStateOf(showEducationInitially) }
    if (showEducation) {
        val educationTerm = deposit?.let {
            DepositTerm(it.durationCycles, it.interestPercent, it.earlyClosePenaltyPercent)
        } ?: DepositTerms[1]
        FinancialEducationOverlay(
            topic = FinancialEducationTopic.Deposit,
            depositAmount = deposit?.amount ?: state.money.coerceAtMost(100).coerceAtLeast(0),
            depositTerm = educationTerm,
            depositPetImageRes = petImageResource(state.level, state.hairColour, state.hairStyle),
            onDismiss = {
                showEducation = false
                if (showEducationInitially) onEducationSeen()
            },
        )
        return
    }

    when {
        deposit == null -> DepositOpeningScreen(state, onBack, onOpen)
        deposit.isMature(state.currentPeriod) -> {
            DepositSuccessfulScreen(state, deposit, onBack, onClose)
        }
        else -> DepositOpenedScreen(state, deposit, onBack, onClose)
    }
}

@Composable
private fun DepositOpeningScreen(
    state: GameSnapshot,
    onBack: () -> Unit,
    onOpen: (DepositTerm, Int) -> Unit,
) {
    var selectedTermIndex by rememberSaveable { mutableStateOf(1) }
    val selectedTerm = DepositTerms[selectedTermIndex]
    var amountText by rememberSaveable {
        mutableStateOf(state.money.coerceAtMost(100).takeIf { it > 0 }?.toString().orEmpty())
    }
    val amount = amountText.toIntOrNull() ?: 0
    val canOpen = amount > 0 && amount <= state.money
    val profit = amount * selectedTerm.interestPercent / 100
    val earlyLoss = amount * selectedTerm.earlyClosePenaltyPercent / 100

    DepositPage(
        onBack = onBack,
        bottomContent = {
            DepositOutcomePanel(
                duration = selectedTerm.durationCycles,
                maturityAmount = amount + profit,
                earlyAmount = amount - earlyLoss,
                buttonText = stringResource(R.string.deposit_open),
                buttonEnabled = canOpen,
                onButtonClick = { onOpen(selectedTerm, amount) },
            )
        },
    ) {
        DepositGuideRow(
            text = stringResource(R.string.deposit_intro),
            petImageRes = petImageResource(state.level, state.hairColour, state.hairStyle),
        )
        DepositHeading(stringResource(R.string.deposit_terms))
        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
            DepositTerms.forEachIndexed { index, term ->
                DepositTermRow(
                    term = term,
                    selected = selectedTermIndex == index,
                    onClick = { selectedTermIndex = index },
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        DepositHeading(stringResource(R.string.deposit_enter_amount))
        OutlinedTextField(
            value = amountText,
            onValueChange = { value -> amountText = value.filter(Char::isDigit).take(7) },
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            singleLine = true,
            isError = amountText.isNotEmpty() && !canOpen,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            textStyle = androidx.compose.ui.text.TextStyle(
                color = DepositDark,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = DepositPurple,
                unfocusedBorderColor = DepositDark,
                focusedTextColor = DepositDark,
                unfocusedTextColor = DepositDark,
                errorBorderColor = Color(0xFFC62828),
            ),
            shape = RoundedCornerShape(10.dp),
        )
        if (amount > state.money) {
            Text(
                text = stringResource(R.string.deposit_not_enough_money, state.money),
                color = Color(0xFFC62828),
                fontSize = 12.sp,
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.deposit_current_balance),
                color = DepositDark,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.width(6.dp))
            MoneyAmount(state.money, fontSize = 17, coinSize = 22)
        }
    }
}

@Composable
private fun DepositOpenedScreen(
    state: GameSnapshot,
    deposit: ActiveDeposit,
    onBack: () -> Unit,
    onClose: () -> Unit,
) {
    var confirming by rememberSaveable { mutableStateOf(false) }
    if (confirming) {
        DepositEarlyClosingScreen(
            state = state,
            deposit = deposit,
            onBack = { confirming = false },
            onClose = onClose,
        )
        return
    }

    DepositPage(onBack = onBack) {
        DepositHeading(stringResource(R.string.deposit_open_deposits), fontSize = 24)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, DepositDark, RoundedCornerShape(12.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                MoneyAmount(deposit.amount, fontSize = 30, coinSize = 29)
                Text(
                    text = stringResource(
                        R.string.deposit_rate_for_days,
                        deposit.interestPercent,
                        deposit.durationCycles,
                    ),
                    color = DepositDark,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text = stringResource(R.string.deposit_until_end),
                color = DepositPurple,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(
                    R.string.deposit_game_days,
                    deposit.remainingCycles(state.currentPeriod),
                ),
                color = DepositPurple,
                fontSize = 21.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.deposit_maturity_caption_short),
                    modifier = Modifier.weight(1f),
                    color = DepositDark,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                )
                MoneyAmount(deposit.maturityPayout, fontSize = 14, coinSize = 15)
            }
            DepositButton(
                text = stringResource(R.string.deposit_close_account),
                enabled = true,
                light = true,
                onClick = { confirming = true },
            )
        }
    }
}

@Composable
private fun DepositEarlyClosingScreen(
    state: GameSnapshot,
    deposit: ActiveDeposit,
    onBack: () -> Unit,
    onClose: () -> Unit,
) {
    DepositPage(
        onBack = onBack,
        scrollable = false,
        bottomContent = {
            DepositOutcomePanel(
                duration = deposit.durationCycles,
                maturityAmount = deposit.maturityPayout,
                earlyAmount = deposit.earlyClosePayout,
                buttonText = stringResource(R.string.deposit_close_account),
                buttonEnabled = true,
                onButtonClick = onClose,
            )
        },
    ) {
        OutlinedSpeechBubble(stringResource(R.string.deposit_close_confirmation))
        Image(
            painter = painterResource(
                petImageResource(state.level, state.hairColour, state.hairStyle),
            ),
            contentDescription = stringResource(R.string.pet),
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxWidth().weight(1f),
        )
    }
}

@Composable
private fun DepositSuccessfulScreen(
    state: GameSnapshot,
    deposit: ActiveDeposit,
    onBack: () -> Unit,
    onCollect: () -> Unit,
) {
    DepositPage(
        onBack = onBack,
        scrollable = false,
        bottomContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                    .padding(horizontal = 16.dp, vertical = 22.dp),
            ) {
                DepositButton(stringResource(R.string.deposit_collect), true, onClick = onCollect)
            }
        },
    ) {
        OutlinedSpeechBubble(stringResource(R.string.deposit_success, deposit.maturityPayout))
        Image(
            painter = painterResource(
                petImageResource(state.level, state.hairColour, state.hairStyle),
            ),
            contentDescription = stringResource(R.string.pet),
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxWidth().weight(1f),
        )
    }
}

@Composable
private fun DepositPage(
    onBack: () -> Unit,
    scrollable: Boolean = true,
    contentBottomPadding: Dp = 22.dp,
    bottomContent: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    var bottomHeightPx by remember { mutableIntStateOf(0) }
    val bottomHeight = with(LocalDensity.current) { bottomHeightPx.toDp() }
    val effectiveBottomPadding = maxOf(contentBottomPadding, bottomHeight)

    Box(
        Modifier
            .fillMaxSize()
            .background(DepositBackground)
            .safeDrawingPadding(),
    ) {
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 16.dp, top = 78.dp, bottom = effectiveBottomPadding)
        Column(
            modifier = if (scrollable) {
                contentModifier.verticalScroll(rememberScrollState())
            } else {
                contentModifier
            },
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = content,
        )
        Text(
            text = "‹",
            modifier = Modifier
                .align(Alignment.TopStart)
                .clickable(onClick = onBack)
                .padding(start = 18.dp, end = 20.dp, top = 7.dp, bottom = 8.dp),
            color = DepositDark,
            fontSize = 38.sp,
            lineHeight = 38.sp,
        )
        Text(
            text = stringResource(R.string.deposit_title),
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 17.dp),
            color = DepositDark,
            fontFamily = NunitoFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 21.sp,
        )
        bottomContent?.let {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .onSizeChanged { size -> bottomHeightPx = size.height },
            ) {
                it()
            }
        }
    }
}

@Composable
private fun DepositGuideRow(text: String, petImageRes: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 132.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            modifier = Modifier
                .weight(1f)
                .background(Color.White, RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            color = DepositDark,
            fontSize = 13.sp,
            lineHeight = 17.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.width(3.dp))
        Box(Modifier.width(137.dp).height(118.dp)) {
            Image(
                painter = painterResource(petImageRes),
                contentDescription = stringResource(R.string.pet),
                contentScale = ContentScale.Crop,
                alignment = Alignment.TopCenter,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun DepositHeading(text: String, fontSize: Int = 21) {
    Text(
        text = text,
        color = DepositDark,
        fontSize = fontSize.sp,
        lineHeight = (fontSize + 3).sp,
        fontWeight = FontWeight.ExtraBold,
        fontFamily = NunitoFontFamily,
    )
}

@Composable
private fun DepositTermRow(term: DepositTerm, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(17.dp)
                .border(1.5.dp, if (selected) DepositPurple else DepositDark, CircleShape)
                .padding(3.dp),
        ) {
            if (selected) Box(Modifier.fillMaxSize().background(DepositPurple, CircleShape))
        }
        Spacer(Modifier.width(7.dp))
        Text(
            text = stringResource(
                R.string.deposit_term_format,
                term.durationCycles,
                term.interestPercent,
                term.earlyClosePenaltyPercent,
            ),
            color = DepositDark,
            fontSize = 14.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.ExtraBold,
        )
    }
}

@Composable
private fun DepositOutcomePanel(
    duration: Int,
    maturityAmount: Int,
    earlyAmount: Int,
    buttonText: String,
    buttonEnabled: Boolean,
    onButtonClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        DepositOutcomeRow(
            label = stringResource(R.string.deposit_maturity_caption, duration),
            amount = maturityAmount,
        )
        DepositOutcomeRow(
            label = stringResource(R.string.deposit_early_caption, duration),
            amount = earlyAmount,
        )
        DepositButton(buttonText, buttonEnabled, onClick = onButtonClick)
    }
}

@Composable
private fun DepositOutcomeRow(label: String, amount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            color = DepositPurple,
            fontSize = 15.sp,
            lineHeight = 19.sp,
            fontWeight = FontWeight.ExtraBold,
        )
        MoneyAmount(amount, fontSize = 26, coinSize = 24)
    }
}

@Composable
private fun OutlinedSpeechBubble(text: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.76f)
                .border(1.5.dp, DepositDark, RoundedCornerShape(7.dp))
                .background(Color.White, RoundedCornerShape(7.dp))
                .padding(horizontal = 14.dp, vertical = 27.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                color = DepositDark,
                fontSize = 19.sp,
                lineHeight = 23.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
            )
        }
        Canvas(Modifier.size(46.dp, 18.dp).offset(y = (-1).dp)) {
            val triangle = Path().apply {
                moveTo(0f, 0f)
                lineTo(size.width / 2f, size.height)
                lineTo(size.width, 0f)
                close()
            }
            drawPath(triangle, color = Color.White)
            drawLine(
                color = DepositDark,
                start = Offset(0f, 0f),
                end = Offset(size.width / 2f, size.height),
                strokeWidth = 1.5.dp.toPx(),
            )
            drawLine(
                color = DepositDark,
                start = Offset(size.width / 2f, size.height),
                end = Offset(size.width, 0f),
                strokeWidth = 1.5.dp.toPx(),
            )
        }
    }
}

@Composable
private fun MoneyAmount(value: Int, fontSize: Int, coinSize: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = value.toString(),
            color = DepositDark,
            fontSize = fontSize.sp,
            lineHeight = (fontSize + 2).sp,
            fontWeight = FontWeight.ExtraBold,
        )
        Spacer(Modifier.width(4.dp))
        Image(
            painter = painterResource(R.drawable.coin_money),
            contentDescription = null,
            modifier = Modifier.size(coinSize.dp),
        )
    }
}

@Composable
private fun DepositButton(
    text: String,
    enabled: Boolean,
    light: Boolean = false,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().heightIn(min = 53.dp),
        shape = RoundedCornerShape(11.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (light) DepositLight else DepositPurple,
            contentColor = if (light) DepositDark else Color.White,
            disabledContainerColor = DepositLight.copy(alpha = 0.55f),
            disabledContentColor = DepositDark.copy(alpha = 0.55f),
        ),
    ) {
        Text(text, fontSize = 17.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
    }
}
