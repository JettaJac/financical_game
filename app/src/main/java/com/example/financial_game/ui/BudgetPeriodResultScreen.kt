package com.example.financial_game.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financial_game.R
import com.example.financial_game.domain.BudgetPeriodResult
import com.example.financial_game.domain.MandatoryBudget
import com.example.financial_game.ui.theme.NunitoFontFamily

private val ResultPurple = Color(0xFF8138D0)
private val ResultPurpleDark = Color(0xFF54206F)
private val ResultBackground = Color(0xFFF8EDFF)

@Composable
internal fun BudgetPeriodResultScreen(
    @DrawableRes petImageRes: Int,
    result: BudgetPeriodResult,
    plannedOptionalExpenses: Int,
    actualOptionalExpenses: Int,
    actualAdditionalIncome: Int,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showSuccess by rememberSaveable { mutableStateOf(false) }

    if (showSuccess) {
        BudgetSuccessScreen(
            petImageRes = petImageRes,
            onCollect = onComplete,
            modifier = modifier,
        )
    } else {
        BudgetComparisonScreen(
            petImageRes = petImageRes,
            result = result,
            plannedOptionalExpenses = plannedOptionalExpenses,
            actualOptionalExpenses = actualOptionalExpenses,
            actualAdditionalIncome = actualAdditionalIncome,
            onContinue = {
                if (result.isSuccessful) showSuccess = true else onComplete()
            },
            modifier = modifier,
        )
    }
}

@Composable
private fun BudgetComparisonScreen(
    @DrawableRes petImageRes: Int,
    result: BudgetPeriodResult,
    plannedOptionalExpenses: Int,
    actualOptionalExpenses: Int,
    actualAdditionalIncome: Int,
    onContinue: () -> Unit,
    modifier: Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFE5B9FA), ResultBackground, Color.White),
                ),
            ),
    ) {
        Column(Modifier.fillMaxSize().safeDrawingPadding()) {
            Text(
                text = stringResource(R.string.budget_period_results),
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                color = ResultPurpleDark,
                fontFamily = NunitoFontFamily,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ResultSpeechBubble(
                    text = stringResource(
                        if (result.isSuccessful) {
                            R.string.budget_period_success_summary
                        } else {
                            R.string.budget_period_failed_summary
                        },
                    ),
                    modifier = Modifier.weight(1f),
                )
                Image(
                    painter = painterResource(petImageRes),
                    contentDescription = stringResource(R.string.pet),
                    modifier = Modifier.size(112.dp),
                    contentScale = ContentScale.Crop,
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ResultSectionTitle(stringResource(R.string.budget_income))
                ResultComparisonRow(
                    title = stringResource(R.string.budget_recurring),
                    planned = MandatoryBudget.income,
                    actual = MandatoryBudget.income,
                )
                ResultComparisonRow(
                    title = stringResource(R.string.budget_additional_income),
                    planned = 0,
                    actual = actualAdditionalIncome,
                )
                ResultTotalRow(
                    title = stringResource(R.string.budget_period_income_total),
                    planned = result.plannedIncome,
                    actual = result.actualIncome,
                )
                Spacer(Modifier.height(10.dp))
                ResultSectionTitle(stringResource(R.string.budget_expenses))
                ResultComparisonRow(
                    title = stringResource(R.string.budget_recurring_expenses),
                    planned = MandatoryBudget.expense,
                    actual = MandatoryBudget.expense,
                )
                ResultComparisonRow(
                    title = stringResource(R.string.budget_optional_expenses),
                    planned = plannedOptionalExpenses,
                    actual = actualOptionalExpenses,
                )
                ResultTotalRow(
                    title = stringResource(R.string.budget_period_expense_total),
                    planned = result.plannedExpenses,
                    actual = result.actualExpenses,
                )
                Spacer(Modifier.height(12.dp))
            }
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp),
            ) {
                ResultPrimaryButton(
                    text = stringResource(R.string.budget_period_next),
                    onClick = onContinue,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
    }
}

@Composable
private fun ResultComparisonRow(title: String, planned: Int, actual: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            color = ResultPurpleDark,
            fontFamily = NunitoFontFamily,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ResultValueCard(
                amount = planned,
                caption = stringResource(R.string.budget_planned),
                modifier = Modifier.weight(1f),
            )
            ResultValueCard(
                amount = actual,
                caption = stringResource(R.string.budget_actual),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ResultValueCard(amount: Int, caption: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .border(1.5.dp, ResultPurpleDark, RoundedCornerShape(10.dp))
            .padding(vertical = 11.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = amount.toString(),
                color = ResultPurpleDark,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            Image(
                painter = painterResource(R.drawable.coin_money),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
        }
        Text(text = caption, color = ResultPurpleDark, fontSize = 11.sp)
    }
}

@Composable
private fun ResultTotalRow(title: String, planned: Int, actual: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = title, color = ResultPurpleDark, fontWeight = FontWeight.ExtraBold)
        Text(
            text = "$planned / $actual",
            color = ResultPurpleDark,
            fontWeight = FontWeight.ExtraBold,
        )
    }
}

@Composable
private fun ResultSectionTitle(text: String) {
    Text(
        text = text,
        color = ResultPurpleDark,
        fontFamily = NunitoFontFamily,
        fontSize = 24.sp,
        fontWeight = FontWeight.ExtraBold,
    )
}

@Composable
private fun BudgetSuccessScreen(
    @DrawableRes petImageRes: Int,
    onCollect: () -> Unit,
    modifier: Modifier,
) {
    Box(modifier.fillMaxSize().background(ResultBackground)) {
        Image(
            painter = painterResource(R.drawable.home_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(Color(0xFFD99AEE).copy(alpha = 0.38f)),
        )
        Box(Modifier.fillMaxSize().safeDrawingPadding()) {
            ResultSpeechBubble(
                text = stringResource(R.string.budget_success_message),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 96.dp, start = 32.dp, end = 32.dp),
            )
            Image(
                painter = painterResource(petImageRes),
                contentDescription = stringResource(R.string.pet),
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(0.82f),
                contentScale = ContentScale.Fit,
            )
            ResultPrimaryButton(
                text = stringResource(R.string.budget_collect_reward),
                onClick = onCollect,
                modifier = Modifier.align(Alignment.BottomCenter).padding(20.dp),
            )
        }
    }
}

@Composable
private fun ResultSpeechBubble(text: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, color = Color.White, shape = RoundedCornerShape(9.dp)) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
            color = ResultPurpleDark,
            fontFamily = NunitoFontFamily,
            fontSize = 14.sp,
            lineHeight = 18.sp,
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
        Text(text = text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}
