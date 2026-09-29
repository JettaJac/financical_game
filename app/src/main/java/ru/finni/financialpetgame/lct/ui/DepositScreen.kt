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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
) {
    val deposit = state.activeDeposit
    when {
        deposit == null -> DepositOpeningScreen(state, onBack, onOpen)
        deposit.isMature(state.currentPeriod) -> DepositSuccessfulScreen(state, deposit, onBack, onClose)
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
    var amountText by rememberSaveable { mutableStateOf("100") }
    val amount = amountText.toIntOrNull() ?: 0
    val canOpen = amount > 0 && amount <= state.money
    val payout = amount + amount * selectedTerm.interestPercent / 100
    val earlyPayout = amount - amount * selectedTerm.earlyClosePenaltyPercent / 100

    DepositPage(onBack) {
        GuideRow(
            text = stringResource(R.string.deposit_intro),
            petImageRes = petImageResource(state.level, state.hairColour, state.hairStyle),
        )
        DepositHeading(stringResource(R.string.deposit_terms))
        DepositTerms.forEach { term ->
            DepositTermRow(
                term = term,
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
        Spacer(Modifier.height(12.dp))
        DepositSummary(
            maturityPayout = payout,
            earlyPayout = earlyPayout,
            duration = selectedTerm.durationCycles,
            buttonText = stringResource(R.string.deposit_open),
            buttonEnabled = canOpen,
            onButtonClick = { onOpen(selectedTerm, amount) },
        )
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
        DepositEarlyClosingScreen(state, deposit, onBack = { confirming = false }, onClose = onClose)
        return
    }

    DepositPage(onBack) {
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
    onClose: () -> Unit,
) {
    DepositPage(onBack) {
        GuideRow(
            text = stringResource(R.string.deposit_close_confirmation),
            petImageRes = petImageResource(state.level, state.hairColour, state.hairStyle),
        )
        Spacer(Modifier.height(30.dp))
        DepositSummary(
            maturityPayout = deposit.maturityPayout,
            earlyPayout = deposit.earlyClosePayout,
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
    onCollect: () -> Unit,
) {
    DepositPage(onBack) {
        Text(
            text = stringResource(R.string.deposit_success, deposit.maturityPayout),
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
private fun DepositPage(onBack: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
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
private fun DepositTermRow(term: DepositTerm, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 5.dp),
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
        Text(
            text = stringResource(
                R.string.deposit_term_format,
                term.durationCycles,
                term.interestPercent,
                term.earlyClosePenaltyPercent,
            ),
            color = DepositDark,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun DepositSummary(
    maturityPayout: Int,
    earlyPayout: Int,
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
        Text(stringResource(R.string.deposit_income), color = DepositPurple, fontWeight = FontWeight.Bold)
        MoneyValue(maturityPayout, fontSize = 32)
        Text(
            stringResource(R.string.deposit_maturity_caption, duration),
            color = DepositPurple,
            fontSize = 13.sp,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.deposit_remaining_amount),
            color = DepositPurple,
            fontWeight = FontWeight.Bold,
        )
        MoneyValue(earlyPayout, fontSize = 25)
        Text(
            stringResource(R.string.deposit_early_caption, duration),
            color = DepositPurple,
            fontSize = 13.sp,
        )
        Spacer(Modifier.height(10.dp))
        DepositButton(buttonText, buttonEnabled, onButtonClick)
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
