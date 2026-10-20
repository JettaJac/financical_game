package ru.finni.financialpetgame.lct.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.finni.financialpetgame.lct.R
import ru.finni.financialpetgame.lct.data.GameSnapshot
import ru.finni.financialpetgame.lct.domain.BudgetPeriodResult
import ru.finni.financialpetgame.lct.ui.theme.NunitoFontFamily

private val ResultPurple = Color(0xFF8138D0)
private val ResultPurpleDark = Color(0xFF54206F)
private val ResultBackground = Color(0xFFF9EEFF)
private val ResultTotalBackground = Color(0xFFF0D9FC)
private val ResultSecondaryButton = Color(0xFFE2B5FA)

@Composable
internal fun BudgetPeriodResultScreen(
    @DrawableRes petImageRes: Int,
    state: GameSnapshot,
    result: BudgetPeriodResult,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showSuccess by rememberSaveable { mutableStateOf(false) }
    var expandedSection by rememberSaveable { mutableIntStateOf(0) }

    if (showSuccess) {
        BudgetSuccessScreen(
            petImageRes = petImageRes,
            onCollect = onComplete,
            onReturn = {
                showSuccess = false
                expandedSection = 0
            },
            modifier = modifier,
        )
    } else {
        BudgetComparisonScreen(
            petImageRes = petImageRes,
            state = state,
            result = result,
            expandedSection = expandedSection,
            onSectionSelected = { expandedSection = it },
            onContinue = {
                when {
                    expandedSection < 2 -> expandedSection += 1
                    result.isSuccessful -> showSuccess = true
                    else -> onComplete()
                }
            },
            modifier = modifier,
        )
    }
}

@Composable
private fun BudgetComparisonScreen(
    @DrawableRes petImageRes: Int,
    state: GameSnapshot,
    result: BudgetPeriodResult,
    expandedSection: Int,
    onSectionSelected: (Int) -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFE7BEFA), ResultBackground, Color.White),
                ),
            )
            .safeDrawingPadding(),
    ) {
        val compact = maxHeight < 700.dp
        val horizontalPadding = if (maxWidth < 360.dp) 12.dp else 16.dp

        Column(Modifier.fillMaxSize()) {
            Text(
                text = stringResource(R.string.budget_period_results),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = if (compact) 12.dp else 22.dp),
                color = ResultPurpleDark,
                fontFamily = NunitoFontFamily,
                fontSize = if (compact) 20.sp else 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )

            ResultHeader(
                petImageRes = petImageRes,
                message = resultMessage(expandedSection, state, result),
                compact = compact,
                modifier = Modifier.padding(
                    start = horizontalPadding,
                    end = horizontalPadding,
                    top = if (compact) 8.dp else 14.dp,
                    bottom = if (compact) 4.dp else 8.dp,
                ),
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = horizontalPadding),
            ) {
                ResultAccordionHeader(
                    text = stringResource(R.string.budget_income),
                    expanded = expandedSection == 0,
                    onClick = { onSectionSelected(0) },
                )
                if (expandedSection == 0) {
                    IncomeResultContent(state, result, compact)
                }

                ResultAccordionHeader(
                    text = stringResource(R.string.budget_expenses),
                    expanded = expandedSection == 1,
                    onClick = { onSectionSelected(1) },
                )
                if (expandedSection == 1) {
                    ExpensesResultContent(state, result, compact)
                }

                ResultAccordionHeader(
                    text = stringResource(R.string.deposit_title),
                    expanded = expandedSection == 2,
                    onClick = { onSectionSelected(2) },
                )
                if (expandedSection == 2) {
                    DepositResultContent(state)
                }
                Spacer(Modifier.height(12.dp))
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shape = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp),
            ) {
                ResultPrimaryButton(
                    text = stringResource(R.string.budget_period_next),
                    onClick = onContinue,
                    modifier = Modifier.padding(
                        horizontal = horizontalPadding,
                        vertical = if (compact) 10.dp else 16.dp,
                    ),
                )
            }
        }
    }
}

@Composable
private fun resultMessage(
    section: Int,
    state: GameSnapshot,
    result: BudgetPeriodResult,
): String = when (section) {
    0 -> stringResource(
        if (state.actualAdditionalIncome > 0) {
            R.string.budget_period_income_bonus_summary
        } else {
            R.string.budget_period_income_summary
        },
    )
    1 -> stringResource(
        if (result.actualExpenses > result.plannedExpenses) {
            R.string.budget_period_expense_overrun_summary
        } else {
            R.string.budget_period_expense_success_summary
        },
    )
    else -> stringResource(
        if (result.isSuccessful) {
            R.string.budget_period_success_summary
        } else {
            R.string.budget_period_failed_summary
        },
    )
}

@Composable
private fun ResultHeader(
    @DrawableRes petImageRes: Int,
    message: String,
    compact: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ResultSpeechBubble(
            text = message,
            modifier = Modifier.weight(1f),
        )
        Image(
            painter = painterResource(petImageRes),
            contentDescription = stringResource(R.string.pet),
            modifier = Modifier.size(if (compact) 88.dp else 104.dp),
            contentScale = ContentScale.Fit,
        )
    }
}

@Composable
private fun IncomeResultContent(
    state: GameSnapshot,
    result: BudgetPeriodResult,
    compact: Boolean,
) {
    Column(verticalArrangement = Arrangement.spacedBy(if (compact) 7.dp else 10.dp)) {
        ResultComparisonRow(
            title = stringResource(R.string.budget_recurring),
            planned = result.plannedIncome,
            actual = result.plannedIncome,
            compact = compact,
        )
        ResultComparisonRow(
            title = stringResource(R.string.budget_additional_income),
            planned = 0,
            actual = state.actualAdditionalIncome,
            compact = compact,
        )
        ResultComparisonRow(
            title = stringResource(R.string.budget_period_income_total),
            planned = result.plannedIncome,
            actual = result.actualIncome,
            compact = compact,
            highlighted = true,
        )
    }
}

@Composable
private fun ExpensesResultContent(
    state: GameSnapshot,
    result: BudgetPeriodResult,
    compact: Boolean,
) {
    Column(verticalArrangement = Arrangement.spacedBy(if (compact) 7.dp else 10.dp)) {
        ResultComparisonRow(
            title = stringResource(R.string.budget_recurring_expenses),
            planned = state.budgetPlanRecurringExpense,
            actual = state.budgetPlanRecurringExpense,
            compact = compact,
        )
        ResultComparisonRow(
            title = stringResource(R.string.budget_optional_short),
            planned = state.plannedOptionalExpenses,
            actual = state.actualOptionalExpenses,
            compact = compact,
        )
        ResultComparisonRow(
            title = stringResource(R.string.budget_unplanned),
            planned = 0,
            actual = state.actualUnplannedExpenses,
            compact = compact,
        )
        ResultComparisonRow(
            title = stringResource(R.string.budget_goal_purchase),
            planned = state.plannedGoalExpenses,
            actual = state.actualGoalExpenses,
            compact = compact,
        )
        ResultComparisonRow(
            title = stringResource(R.string.budget_period_expense_total),
            planned = result.plannedExpenses,
            actual = result.actualExpenses,
            compact = compact,
            highlighted = true,
        )
    }
}

@Composable
private fun DepositResultContent(state: GameSnapshot) {
    val deposit = state.activeDeposit
    Column(
        modifier = Modifier.padding(top = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (deposit == null) {
            Text(
                text = stringResource(R.string.budget_no_active_deposit),
                color = ResultPurpleDark,
                fontFamily = NunitoFontFamily,
                fontSize = 15.sp,
                lineHeight = 19.sp,
            )
        } else {
            ResultAmountLine(
                title = stringResource(R.string.budget_deposit_amount),
                amount = deposit.amount,
            )
            Text(
                text = stringResource(
                    R.string.budget_deposit_remaining,
                    deposit.remainingCycles(state.currentPeriod),
                ),
                color = ResultPurple,
                fontFamily = NunitoFontFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.height(8.dp))
        ResultAmountLine(
            title = stringResource(R.string.budget_balance),
            amount = state.money,
            titleSize = 22,
        )
    }
}

@Composable
private fun ResultAccordionHeader(text: String, expanded: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(top = 7.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            color = ResultPurpleDark,
            fontFamily = NunitoFontFamily,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
        )
        Text(
            text = if (expanded) "⌃" else "⌄",
            color = ResultPurpleDark,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
        )
    }
    Box(Modifier.fillMaxWidth().height(1.dp).background(ResultPurpleDark))
}

@Composable
private fun ResultComparisonRow(
    title: String,
    planned: Int,
    actual: Int,
    compact: Boolean,
    highlighted: Boolean = false,
) {
    Column(verticalArrangement = Arrangement.spacedBy(if (compact) 3.dp else 5.dp)) {
        Text(
            text = title,
            color = ResultPurpleDark,
            fontFamily = NunitoFontFamily,
            fontSize = if (highlighted) 17.sp else 16.sp,
            lineHeight = 19.sp,
            fontWeight = FontWeight.Bold,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ResultValueCard(
                amount = planned,
                caption = stringResource(R.string.budget_planned),
                highlighted = highlighted,
                compact = compact,
                modifier = Modifier.weight(1f),
            )
            ResultValueCard(
                amount = actual,
                caption = stringResource(R.string.budget_actual),
                highlighted = highlighted,
                compact = compact,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ResultValueCard(
    amount: Int,
    caption: String,
    highlighted: Boolean,
    compact: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(
                color = if (highlighted) ResultTotalBackground else Color.Transparent,
                shape = RoundedCornerShape(9.dp),
            )
            .border(1.2.dp, ResultPurpleDark, RoundedCornerShape(9.dp))
            .padding(
                horizontal = 5.dp,
                vertical = if (compact) 5.dp else 8.dp,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = amount.toString(),
                color = ResultPurpleDark,
                fontFamily = NunitoFontFamily,
                fontSize = if (compact) 18.sp else 20.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            Image(
                painter = painterResource(R.drawable.coin_money),
                contentDescription = null,
                modifier = Modifier.size(if (compact) 19.dp else 21.dp),
            )
        }
        Text(
            text = caption,
            color = ResultPurpleDark,
            fontFamily = NunitoFontFamily,
            fontSize = if (compact) 10.sp else 11.sp,
            lineHeight = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ResultAmountLine(title: String, amount: Int, titleSize: Int = 16) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            color = ResultPurpleDark,
            fontFamily = NunitoFontFamily,
            fontSize = titleSize.sp,
            fontWeight = FontWeight.ExtraBold,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = amount.toString(),
                color = ResultPurpleDark,
                fontFamily = NunitoFontFamily,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            Image(
                painter = painterResource(R.drawable.coin_money),
                contentDescription = null,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Composable
private fun BudgetSuccessScreen(
    @DrawableRes petImageRes: Int,
    onCollect: () -> Unit,
    onReturn: () -> Unit,
    modifier: Modifier,
) {
    BoxWithConstraints(modifier.fillMaxSize().background(ResultBackground)) {
        val compact = maxHeight < 700.dp
        Image(
            painter = painterResource(R.drawable.home_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(Color(0xFFD99AEE).copy(alpha = 0.42f)),
        )
        Box(Modifier.fillMaxSize().safeDrawingPadding()) {
            ResultSpeechBubble(
                text = stringResource(R.string.budget_success_message),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth(0.68f)
                    .padding(top = if (compact) 34.dp else 72.dp),
            )
            Image(
                painter = painterResource(petImageRes),
                contentDescription = stringResource(R.string.pet),
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(if (compact) 0.67f else 0.82f)
                    .padding(top = if (compact) 28.dp else 48.dp, bottom = 86.dp),
                contentScale = ContentScale.Fit,
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ResultPrimaryButton(
                    text = stringResource(R.string.budget_collect_reward),
                    onClick = onCollect,
                )
                Button(
                    onClick = onReturn,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ResultSecondaryButton),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Text(
                        text = stringResource(R.string.budget_return_to_results),
                        color = ResultPurpleDark,
                        fontFamily = NunitoFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultSpeechBubble(text: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, color = Color.White, shape = RoundedCornerShape(8.dp)) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp),
            color = ResultPurpleDark,
            fontFamily = NunitoFontFamily,
            fontSize = 14.sp,
            lineHeight = 17.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ResultPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(52.dp),
        colors = ButtonDefaults.buttonColors(containerColor = ResultPurple),
        shape = RoundedCornerShape(10.dp),
    ) {
        Text(
            text = text,
            fontFamily = NunitoFontFamily,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}
