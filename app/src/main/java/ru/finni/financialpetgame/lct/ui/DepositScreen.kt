package ru.finni.financialpetgame.lct.ui

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
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.finni.financialpetgame.lct.R
import ru.finni.financialpetgame.lct.data.GameSnapshot
import ru.finni.financialpetgame.lct.domain.ActiveDeposit
import ru.finni.financialpetgame.lct.domain.DepositTerm
import ru.finni.financialpetgame.lct.domain.DepositTerms
import ru.finni.financialpetgame.lct.ui.theme.NunitoFontFamily

private val DepositBackground = Color(0xFFFCF7FF)
private val DepositPurple = Color(0xFF8743D3)
private val DepositDark = Color(0xFF4B2163)
private val DepositLight = Color(0xFFF0DCFA)

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
            onDismiss = {
                showEducation = false
                if (showEducationInitially) onEducationSeen()
            },
        )
        return
    }
    val onHelp = { showEducation = true }
    when {
        deposit == null -> DepositOpeningScreen(state, onBack, onHelp, onOpen)
        deposit.isMature(state.currentPeriod) -> {
            DepositSuccessfulScreen(state, deposit, onBack, onHelp, onClose)
        }
        else -> DepositOpenedScreen(state, deposit, onBack, onHelp, onClose)
    }
}

@Composable
private fun DepositOpeningScreen(
    state: GameSnapshot,
    onBack: () -> Unit,
    onHelp: () -> Unit,
    onOpen: (DepositTerm, Int) -> Unit,
) {
    var selectedTermIndex by rememberSaveable { mutableStateOf(1) }
    val selectedTerm = DepositTerms[selectedTermIndex]
    var amountText by rememberSaveable {
        mutableStateOf(state.money.coerceAtMost(100).takeIf { it > 0 }?.toString().orEmpty())
    }
    var confirming by rememberSaveable { mutableStateOf(false) }
    val amount = amountText.toIntOrNull() ?: 0
    val canOpen = amount > 0 && amount <= state.money
    val payout = amount + amount * selectedTerm.interestPercent / 100
    val earlyPayout = amount - amount * selectedTerm.earlyClosePenaltyPercent / 100

    if (confirming) {
        DepositConfirmationScreen(
            state = state,
            term = selectedTerm,
            amount = amount,
            onBack = { confirming = false },
            onHelp = onHelp,
            onConfirm = { onOpen(selectedTerm, amount) },
        )
        return
    }

    DepositPage(onBack, onHelp) {
        GuideRow(
            text = stringResource(R.string.deposit_intro),
            petImageRes = petImageResource(state.level, state.hairColour, state.hairStyle),
        )
        DepositHeading(stringResource(R.string.deposit_terms))
        DepositTerms.forEach { term ->
            DepositTermRow(
                term = term,
                amount = amount,
                selected = selectedTerm == term,
                onClick = { selectedTermIndex = DepositTerms.indexOf(term) },
            )
        }
        DepositHeading(stringResource(R.string.deposit_enter_amount))
        OutlinedTextField(
            value = amountText,
            onValueChange = { value -> amountText = value.filter(Char::isDigit).take(7) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = amountText.isNotEmpty() && !canOpen,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = DepositPurple,
                unfocusedBorderColor = DepositDark,
                focusedTextColor = DepositDark,
                unfocusedTextColor = DepositDark,
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
        Text(
            text = stringResource(R.string.deposit_quick_amount),
            color = DepositPurple,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            listOf(10, 25, 50).forEach { quickAmount ->
                QuickAmountButton(
                    text = quickAmount.toString(),
                    enabled = quickAmount <= state.money,
                    onClick = { amountText = quickAmount.toString() },
                    modifier = Modifier.weight(1f),
                )
            }
            QuickAmountButton(
                text = stringResource(R.string.deposit_all_money),
                enabled = state.money > 0,
                onClick = { amountText = state.money.toString() },
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(12.dp))
        DepositSummary(
            amount = amount,
            profit = payout - amount,
            earlyLoss = amount - earlyPayout,
            duration = selectedTerm.durationCycles,
            buttonText = stringResource(R.string.deposit_open),
            buttonEnabled = canOpen,
            onButtonClick = { confirming = true },
        )
    }
}

@Composable
private fun DepositConfirmationScreen(
    state: GameSnapshot,
    term: DepositTerm,
    amount: Int,
    onBack: () -> Unit,
    onHelp: () -> Unit,
    onConfirm: () -> Unit,
) {
    val profit = amount * term.interestPercent / 100
    DepositPage(onBack, onHelp) {
        DepositHeading(stringResource(R.string.deposit_confirm_title))
        GuideRow(
            text = stringResource(
                R.string.education_deposit_receive,
                amount,
                profit,
                amount + profit,
            ),
            petImageRes = petImageResource(state.level, state.hairColour, state.hairStyle),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(18.dp))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp),
        ) {
            DepositBreakdownRow(stringResource(R.string.deposit_own_money), amount)
            DepositBreakdownRow(stringResource(R.string.deposit_wallet_after), state.money - amount)
            DepositBreakdownRow(stringResource(R.string.deposit_profit), profit, Color(0xFF21864B))
            DepositBreakdownRow(
                stringResource(R.string.deposit_total_return),
                amount + profit,
                DepositPurple,
                emphasized = true,
            )
        }
        DepositButton(stringResource(R.string.deposit_confirm_action), true, onConfirm)
        TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.deposit_back_to_edit),
                color = DepositPurple,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun DepositOpenedScreen(
    state: GameSnapshot,
    deposit: ActiveDeposit,
    onBack: () -> Unit,
    onHelp: () -> Unit,
    onClose: () -> Unit,
) {
    var confirming by rememberSaveable { mutableStateOf(false) }
    if (confirming) {
        DepositEarlyClosingScreen(
            state,
            deposit,
            onBack = { confirming = false },
            onHelp = onHelp,
            onClose = onClose,
        )
        return
    }

    DepositPage(onBack, onHelp) {
        DepositHeading(stringResource(R.string.deposit_open_deposits))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, DepositDark, RoundedCornerShape(14.dp))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MoneyValue(deposit.amount, fontSize = 32)
            Text(
                text = stringResource(
                    R.string.deposit_rate_for_days,
                    deposit.interestPercent,
                    deposit.durationCycles,
                ),
                color = DepositDark,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(6.dp))
            Text(stringResource(R.string.deposit_until_end), color = DepositPurple, fontSize = 14.sp)
            Text(
                text = stringResource(
                    R.string.deposit_game_days,
                    deposit.remainingCycles(state.currentPeriod),
                ),
                color = DepositDark,
                fontSize = 25.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            val completedCycles = (state.currentPeriod - deposit.openedAtPeriod)
                .coerceIn(0, deposit.durationCycles)
            Text(
                text = stringResource(
                    R.string.deposit_progress,
                    completedCycles,
                    deposit.durationCycles,
                ),
                color = DepositPurple,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
            )
            LinearProgressIndicator(
                progress = {
                    completedCycles.toFloat() / deposit.durationCycles.coerceAtLeast(1)
                },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = DepositPurple,
                trackColor = DepositLight,
            )
            Text(
                text = stringResource(R.string.deposit_maturity_caption_short),
                color = DepositPurple,
                fontSize = 14.sp,
            )
            MoneyValue(deposit.maturityPayout, fontSize = 24)
            DepositButton(stringResource(R.string.deposit_close), true) { confirming = true }
        }
    }
}

@Composable
private fun DepositEarlyClosingScreen(
    state: GameSnapshot,
    deposit: ActiveDeposit,
    onBack: () -> Unit,
    onHelp: () -> Unit,
    onClose: () -> Unit,
) {
    DepositPage(onBack, onHelp) {
        GuideRow(
            text = stringResource(R.string.deposit_close_confirmation),
            petImageRes = petImageResource(state.level, state.hairColour, state.hairStyle),
        )
        Spacer(Modifier.height(30.dp))
        Text(
            text = stringResource(
                R.string.deposit_early_loss,
                deposit.earlyCloseLoss,
                deposit.earlyClosePayout,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFFE4E4), RoundedCornerShape(14.dp))
                .padding(16.dp),
            color = Color(0xFF9C2F2F),
            fontSize = 16.sp,
            lineHeight = 21.sp,
            fontWeight = FontWeight.Bold,
        )
        DepositSummary(
            amount = deposit.amount,
            profit = deposit.profit,
            earlyLoss = deposit.earlyCloseLoss,
            duration = deposit.durationCycles,
            buttonText = stringResource(R.string.deposit_close),
            buttonEnabled = true,
            onButtonClick = onClose,
        )
    }
}

@Composable
private fun DepositSuccessfulScreen(
    state: GameSnapshot,
    deposit: ActiveDeposit,
    onBack: () -> Unit,
    onHelp: () -> Unit,
    onCollect: () -> Unit,
) {
    DepositPage(onBack, onHelp) {
        Text(
            text = stringResource(
                R.string.deposit_success_breakdown,
                deposit.maturityPayout,
                deposit.amount,
                deposit.profit,
            ),
            modifier = Modifier
                .fillMaxWidth(0.72f)
                .background(Color.White, RoundedCornerShape(14.dp))
                .border(1.dp, DepositLight, RoundedCornerShape(14.dp))
                .padding(18.dp),
            color = DepositDark,
            fontSize = 20.sp,
            lineHeight = 25.sp,
            fontWeight = FontWeight.Bold,
        )
        Image(
            painter = painterResource(
                petImageResource(state.level, state.hairColour, state.hairStyle),
            ),
            contentDescription = stringResource(R.string.pet),
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxWidth().height(330.dp),
        )
        DepositButton(stringResource(R.string.deposit_collect), true, onCollect)
    }
}

@Composable
private fun DepositPage(
    onBack: () -> Unit,
    onHelp: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(DepositBackground)
            .safeDrawingPadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 22.dp, end = 22.dp, top = 58.dp, bottom = 22.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content,
        )
        Text(
            text = "‹",
            modifier = Modifier
                .align(Alignment.TopStart)
                .clickable(onClick = onBack)
                .padding(horizontal = 20.dp, vertical = 8.dp),
            color = DepositDark,
            fontSize = 42.sp,
            lineHeight = 42.sp,
        )
        Text(
            text = stringResource(R.string.deposit_title),
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 16.dp),
            color = DepositDark,
            fontFamily = NunitoFontFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 22.sp,
        )
        IconButton(
            onClick = onHelp,
            modifier = Modifier.align(Alignment.TopEnd).padding(end = 8.dp, top = 5.dp),
        ) {
            Text("?", color = DepositDark, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun GuideRow(text: String, petImageRes: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = text,
            modifier = Modifier
                .weight(1f)
                .background(Color.White, RoundedCornerShape(14.dp))
                .border(1.dp, DepositLight, RoundedCornerShape(14.dp))
                .padding(14.dp),
            color = DepositDark,
            fontSize = 14.sp,
            lineHeight = 19.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.width(8.dp))
        Image(
            painter = painterResource(petImageRes),
            contentDescription = stringResource(R.string.pet),
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(108.dp),
        )
    }
}

@Composable
private fun DepositHeading(text: String) {
    Text(
        text = text,
        color = DepositDark,
        fontSize = 20.sp,
        fontWeight = FontWeight.ExtraBold,
        fontFamily = NunitoFontFamily,
    )
}

@Composable
private fun DepositTermRow(
    term: DepositTerm,
    amount: Int,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val profit = amount * term.interestPercent / 100
    val loss = amount * term.earlyClosePenaltyPercent / 100
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (selected) DepositLight else Color.White, RoundedCornerShape(12.dp))
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) DepositPurple else DepositLight,
                shape = RoundedCornerShape(12.dp),
            )
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(21.dp)
                .border(2.dp, DepositPurple, CircleShape)
                .padding(4.dp),
        ) {
            if (selected) Box(Modifier.fillMaxSize().background(DepositPurple, CircleShape))
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = stringResource(
                    R.string.deposit_term_format,
                    term.durationCycles,
                    term.interestPercent,
                    term.earlyClosePenaltyPercent,
                ),
                color = DepositDark,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = stringResource(R.string.deposit_term_result, profit, loss),
                color = DepositPurple,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun DepositSummary(
    amount: Int,
    profit: Int,
    earlyLoss: Int,
    duration: Int,
    buttonText: String,
    buttonEnabled: Boolean,
    onButtonClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(22.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            stringResource(R.string.deposit_maturity_caption, duration),
            color = DepositPurple,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
        )
        DepositBreakdownRow(stringResource(R.string.deposit_own_money), amount)
        DepositBreakdownRow(stringResource(R.string.deposit_profit), profit, Color(0xFF21864B))
        DepositBreakdownRow(
            stringResource(R.string.deposit_total_return),
            amount + profit,
            DepositPurple,
            emphasized = true,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.deposit_early_caption, duration),
            color = DepositPurple,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
        )
        DepositBreakdownRow(
            stringResource(R.string.deposit_penalty),
            earlyLoss,
            Color(0xFFC34141),
        )
        DepositBreakdownRow(
            stringResource(R.string.deposit_total_return),
            amount - earlyLoss,
            DepositDark,
        )
        Spacer(Modifier.height(10.dp))
        DepositButton(buttonText, buttonEnabled, onButtonClick)
    }
}

@Composable
private fun DepositBreakdownRow(
    label: String,
    amount: Int,
    valueColor: Color = DepositDark,
    emphasized: Boolean = false,
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            color = DepositDark,
            fontSize = if (emphasized) 16.sp else 14.sp,
            fontWeight = if (emphasized) FontWeight.ExtraBold else FontWeight.SemiBold,
        )
        Text(
            text = amount.toString(),
            color = valueColor,
            fontSize = if (emphasized) 23.sp else 18.sp,
            fontWeight = FontWeight.ExtraBold,
        )
        Image(
            painter = painterResource(R.drawable.coin_money),
            contentDescription = null,
            modifier = Modifier.padding(start = 5.dp).size(if (emphasized) 22.dp else 18.dp),
        )
    }
}

@Composable
private fun QuickAmountButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(42.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = DepositLight,
            contentColor = DepositDark,
            disabledContainerColor = DepositLight.copy(alpha = 0.45f),
        ),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp),
    ) {
        Text(text, fontSize = 12.sp, maxLines = 1, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun MoneyValue(value: Int, fontSize: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            value.toString(),
            color = DepositDark,
            fontSize = fontSize.sp,
            fontWeight = FontWeight.ExtraBold,
        )
        Spacer(Modifier.width(7.dp))
        Image(
            painter = painterResource(R.drawable.coin_money),
            contentDescription = null,
            modifier = Modifier.size((fontSize * 0.75f).dp),
        )
    }
}

@Composable
private fun DepositButton(text: String, enabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = DepositPurple,
            disabledContainerColor = DepositLight,
        ),
    ) {
        Text(text, fontSize = 17.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
    }
}
