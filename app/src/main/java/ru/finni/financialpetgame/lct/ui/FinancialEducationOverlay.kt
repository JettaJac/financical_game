package ru.finni.financialpetgame.lct.ui

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.finni.financialpetgame.lct.R
import ru.finni.financialpetgame.lct.domain.DepositTerm
import ru.finni.financialpetgame.lct.domain.GameDefaults
import ru.finni.financialpetgame.lct.ui.theme.NunitoFontFamily

internal enum class FinancialEducationTopic { Budget, Deposit }

private val EducationPurple = Color(0xFF8138D0)
private val EducationDark = Color(0xFF4B2163)
private val EducationLavender = Color(0xFFF1DDFB)
private val EducationGreen = Color(0xFFE1F6E8)
private val EducationOrange = Color(0xFFFFE9D8)
private val EducationBlue = Color(0xFFE2F2FF)

@Composable
internal fun FinancialEducationOverlay(
    topic: FinancialEducationTopic,
    onDismiss: () -> Unit,
    recurringIncome: Int = 0,
    recurringExpense: Int = 0,
    plannedOptionalExpenses: Int = 0,
    depositAmount: Int = 0,
    depositTerm: DepositTerm? = null,
) {
    val pageCount = if (topic == FinancialEducationTopic.Budget) 3 else 2
    var page by rememberSaveable(topic) { mutableIntStateOf(0) }
    val scrollState = rememberScrollState()
    LaunchedEffect(topic, page) { scrollState.scrollTo(0) }
    BackHandler(onBack = onDismiss)

    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFE7C3FA), Color(0xFFFCF7FF), Color.White),
                ),
            )
            .safeDrawingPadding(),
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 22.dp, end = 10.dp, top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(
                        if (topic == FinancialEducationTopic.Budget) {
                            R.string.education_budget_title
                        } else {
                            R.string.education_deposit_title
                        },
                    ),
                    modifier = Modifier.weight(1f),
                    color = EducationDark,
                    fontFamily = NunitoFontFamily,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
                IconButton(onClick = onDismiss) {
                    Text("×", color = EducationDark, fontSize = 34.sp)
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                if (topic == FinancialEducationTopic.Budget) {
                    BudgetEducationPage(
                        page = page,
                        recurringIncome = recurringIncome,
                        recurringExpense = recurringExpense,
                        plannedOptionalExpenses = plannedOptionalExpenses,
                    )
                } else {
                    DepositEducationPage(
                        page = page,
                        amount = depositAmount,
                        term = depositTerm,
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    repeat(pageCount) { index ->
                        Box(
                            Modifier
                                .size(if (index == page) 10.dp else 8.dp)
                                .background(
                                    if (index == page) EducationPurple else EducationLavender,
                                    CircleShape,
                                ),
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = {
                        if (page == pageCount - 1) onDismiss() else page += 1
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EducationPurple),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(
                        text = stringResource(
                            if (page == pageCount - 1) R.string.education_understood
                            else R.string.onboarding_next,
                        ),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun BudgetEducationPage(
    page: Int,
    recurringIncome: Int,
    recurringExpense: Int,
    plannedOptionalExpenses: Int,
) {
    when (page) {
        0 -> {
            EducationLead(
                title = stringResource(R.string.education_budget_week_title),
                body = stringResource(
                    R.string.education_budget_game_day,
                    gameCycleDurationText(GameDefaults.CYCLE_DURATION_SECONDS),
                ),
            )
            FlowStep(R.drawable.ic_tasks, stringResource(R.string.education_budget_step_plan), EducationBlue)
            FlowArrow()
            FlowStep(R.drawable.ic_timer_cycle, stringResource(R.string.education_budget_step_play), EducationLavender)
            FlowArrow()
            FlowStep(R.drawable.carbon_piggy_bank, stringResource(R.string.education_budget_step_compare), EducationGreen)
        }
        1 -> {
            EducationLead(
                title = stringResource(R.string.education_budget_categories_title),
                body = stringResource(R.string.education_budget_categories_body),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CategoryCard(
                    title = stringResource(R.string.budget_income),
                    lines = listOf(
                        stringResource(R.string.education_income_recurring),
                        stringResource(R.string.education_income_additional),
                    ),
                    color = EducationGreen,
                    modifier = Modifier.weight(1f),
                )
                CategoryCard(
                    title = stringResource(R.string.budget_expenses),
                    lines = listOf(
                        stringResource(R.string.education_expense_mandatory),
                        stringResource(R.string.education_expense_optional),
                        stringResource(R.string.education_expense_unplanned),
                    ),
                    color = EducationOrange,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        else -> {
            val safeOptional = plannedOptionalExpenses.coerceAtLeast(0)
            val balance = recurringIncome - recurringExpense - safeOptional
            EducationLead(
                title = stringResource(R.string.education_budget_formula_title),
                body = stringResource(R.string.education_budget_formula_body),
            )
            FormulaValue(
                label = stringResource(R.string.education_formula_income),
                amount = recurringIncome,
                color = EducationGreen,
            )
            FormulaOperator("−")
            FormulaValue(
                label = stringResource(R.string.education_formula_mandatory),
                amount = recurringExpense,
                color = EducationOrange,
            )
            FormulaOperator("−")
            FormulaValue(
                label = stringResource(R.string.education_formula_optional),
                amount = safeOptional,
                color = EducationOrange,
            )
            FormulaOperator("=")
            FormulaValue(
                label = stringResource(R.string.education_formula_free),
                amount = balance,
                color = EducationBlue,
                emphasized = true,
            )
            Text(
                text = stringResource(R.string.education_budget_free_money),
                color = EducationDark,
                fontSize = 15.sp,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun DepositEducationPage(page: Int, amount: Int, term: DepositTerm?) {
    val safeAmount = amount.coerceAtLeast(0)
    val safeTerm = term ?: DepositTerm(3, 7, 4)
    val profit = safeAmount * safeTerm.interestPercent / 100
    val payout = safeAmount + profit
    val loss = safeAmount * safeTerm.earlyClosePenaltyPercent / 100
    val earlyPayout = safeAmount - loss

    if (page == 0) {
        EducationLead(
            title = stringResource(R.string.education_deposit_flow_title),
            body = stringResource(
                R.string.education_deposit_game_day,
                gameCycleDurationText(GameDefaults.CYCLE_DURATION_SECONDS),
            ),
        )
        FlowStep(
            R.drawable.coin_money,
            stringResource(R.string.education_deposit_put, safeAmount),
            EducationOrange,
        )
        FlowArrow()
        FlowStep(
            R.drawable.ic_deposit_wallet,
            stringResource(R.string.education_deposit_wait, safeTerm.durationCycles),
            EducationBlue,
        )
        FlowArrow()
        FlowStep(
            R.drawable.carbon_piggy_bank,
            stringResource(R.string.education_deposit_receive, safeAmount, profit, payout),
            EducationGreen,
        )
    } else {
        EducationLead(
            title = stringResource(R.string.education_deposit_early_title),
            body = stringResource(R.string.education_deposit_early_body),
        )
        FlowStep(
            R.drawable.ic_deposit_wallet,
            stringResource(R.string.education_deposit_early_amount, safeAmount),
            EducationOrange,
        )
        FlowArrow()
        FlowStep(
            R.drawable.coin_money,
            stringResource(R.string.education_deposit_early_result, loss, earlyPayout),
            Color(0xFFFFDFDF),
        )
        Text(
            text = stringResource(R.string.education_deposit_budget_link),
            color = EducationDark,
            fontSize = 15.sp,
            lineHeight = 20.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun EducationLead(title: String, body: String) {
    Text(
        text = title,
        modifier = Modifier.fillMaxWidth(),
        color = EducationDark,
        fontFamily = NunitoFontFamily,
        fontSize = 23.sp,
        fontWeight = FontWeight.ExtraBold,
        textAlign = TextAlign.Center,
    )
    Text(
        text = body,
        modifier = Modifier.fillMaxWidth(),
        color = EducationDark,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun FlowStep(@DrawableRes icon: Int, text: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color, RoundedCornerShape(18.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(icon),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(48.dp),
        )
        Text(
            text = text,
            modifier = Modifier.weight(1f).padding(start = 14.dp),
            color = EducationDark,
            fontSize = 16.sp,
            lineHeight = 21.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun FlowArrow() {
    Text(
        text = "↓",
        modifier = Modifier.fillMaxWidth(),
        color = EducationPurple,
        fontSize = 28.sp,
        fontWeight = FontWeight.ExtraBold,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun CategoryCard(title: String, lines: List<String>, color: Color, modifier: Modifier) {
    Surface(modifier = modifier, color = color, shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = title,
                color = EducationDark,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            lines.forEach { line ->
                Text(
                    text = "• $line",
                    color = EducationDark,
                    fontSize = 13.sp,
                    lineHeight = 17.sp,
                )
            }
        }
    }
}

@Composable
private fun FormulaValue(
    label: String,
    amount: Int,
    color: Color,
    emphasized: Boolean = false,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color, RoundedCornerShape(16.dp))
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            color = EducationDark,
            fontSize = if (emphasized) 17.sp else 15.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = amount.toString(),
            color = EducationDark,
            fontSize = if (emphasized) 25.sp else 21.sp,
            fontWeight = FontWeight.ExtraBold,
        )
        Image(
            painter = painterResource(R.drawable.coin_money),
            contentDescription = null,
            modifier = Modifier.padding(start = 5.dp).size(22.dp),
        )
    }
}

@Composable
private fun FormulaOperator(operator: String) {
    Text(
        text = operator,
        modifier = Modifier.fillMaxWidth(),
        color = EducationPurple,
        fontSize = 26.sp,
        fontWeight = FontWeight.ExtraBold,
        textAlign = TextAlign.Center,
    )
}

@Composable
internal fun gameCycleDurationText(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return when {
        minutes > 0 && seconds > 0 -> {
            stringResource(
                R.string.cycle_duration_minutes_seconds,
                pluralStringResource(R.plurals.cycle_duration_minutes, minutes, minutes),
                pluralStringResource(R.plurals.cycle_duration_seconds, seconds, seconds),
            )
        }
        minutes > 0 -> pluralStringResource(R.plurals.cycle_duration_minutes, minutes, minutes)
        else -> pluralStringResource(R.plurals.cycle_duration_seconds, seconds, seconds)
    }
}
