package ru.finni.financialpetgame.lct.ui

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
    @DrawableRes budgetPetImageRes: Int = R.drawable.pet_main,
    @DrawableRes depositPetImageRes: Int = R.drawable.pet_main,
) {
    if (topic == FinancialEducationTopic.Deposit) {
        DepositReferenceEducationOverlay(
            amount = depositAmount,
            term = depositTerm,
            petImageRes = depositPetImageRes,
            onDismiss = onDismiss,
        )
        return
    }

    val pageCount = 3
    var page by rememberSaveable(topic) { mutableIntStateOf(0) }
    var categoryRevealStep by rememberSaveable(topic) { mutableIntStateOf(0) }
    val scrollState = rememberScrollState()
    LaunchedEffect(topic, page) { scrollState.scrollTo(0) }
    BackHandler {
        if (page > 0) page -= 1 else onDismiss()
    }

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
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (page > 0) {
                    IconButton(onClick = { page -= 1 }) {
                        Text("‹", color = EducationDark, fontSize = 34.sp)
                    }
                } else {
                    Spacer(Modifier.size(48.dp))
                }
                Text(
                    text = stringResource(R.string.education_budget_title),
                    modifier = Modifier.weight(1f),
                    color = EducationDark,
                    fontFamily = NunitoFontFamily,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                )
                IconButton(onClick = onDismiss) {
                    Text("×", color = EducationDark, fontSize = 34.sp)
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .then(
                        if (
                            page == 1 &&
                            categoryRevealStep < 3
                        ) {
                            Modifier.clickable { categoryRevealStep += 1 }
                        } else {
                            Modifier
                        },
                    )
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                BudgetEducationPage(
                    page = page,
                    recurringIncome = recurringIncome,
                    recurringExpense = recurringExpense,
                    plannedOptionalExpenses = plannedOptionalExpenses,
                    categoryRevealStep = categoryRevealStep,
                    petImageRes = budgetPetImageRes,
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Button(
                    onClick = {
                        if (
                            page == 1 &&
                            categoryRevealStep < 3
                        ) {
                            categoryRevealStep += 1
                        } else if (page == pageCount - 1) {
                            onDismiss()
                        } else {
                            page += 1
                        }
                    },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
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
private fun DepositReferenceEducationOverlay(
    amount: Int,
    term: DepositTerm?,
    @DrawableRes petImageRes: Int,
    onDismiss: () -> Unit,
) {
    var page by rememberSaveable { mutableIntStateOf(0) }
    val scrollState = rememberScrollState()
    val safeAmount = amount.takeIf { it > 0 } ?: 100
    val safeTerm = term ?: DepositTerm(6, 40, 20)
    val loss = safeAmount * safeTerm.earlyClosePenaltyPercent / 100
    val earlyPayout = safeAmount - loss

    LaunchedEffect(page) { scrollState.scrollTo(0) }
    BackHandler {
        if (page > 0) page -= 1 else onDismiss()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFBF1FF))
            .safeDrawingPadding(),
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 48.dp, end = 10.dp, top = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.education_deposit_title),
                    modifier = Modifier.weight(1f),
                    color = EducationDark,
                    fontFamily = NunitoFontFamily,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                IconButton(onClick = onDismiss) {
                    Text("×", color = EducationDark, fontSize = 29.sp)
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                DepositEducationGuide(
                    text = stringResource(
                        if (page == 0) {
                            R.string.education_deposit_week
                        } else {
                            R.string.education_deposit_early_body
                        },
                        *if (page == 0) {
                            arrayOf(gameCycleDurationText(GameDefaults.CYCLE_DURATION_SECONDS))
                        } else {
                            emptyArray()
                        },
                    ),
                    petImageRes = petImageRes,
                )

                if (page == 0) {
                    DepositEducationStep(
                        number = "1",
                        text = stringResource(R.string.education_deposit_reference_put),
                    )
                    DepositEducationStep(
                        number = "2",
                        text = stringResource(R.string.education_deposit_reference_wait),
                    )
                    DepositEducationStep(
                        number = "3",
                        text = stringResource(R.string.education_deposit_reference_receive),
                    )
                } else {
                    DepositEducationExample(
                        icon = R.drawable.change_money,
                        text = stringResource(
                            R.string.education_deposit_reference_example,
                            safeAmount,
                        ),
                    )
                    DepositEducationExample(
                        icon = R.drawable.money_fell,
                        text = stringResource(
                            R.string.education_deposit_reference_result,
                            loss,
                            earlyPayout,
                        ),
                    )
                    Text(
                        text = stringResource(R.string.education_deposit_game_rule),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(EducationLavender, RoundedCornerShape(10.dp))
                            .padding(16.dp),
                        color = EducationDark,
                        fontSize = 15.sp,
                        lineHeight = 18.sp,
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                    .padding(horizontal = 16.dp, vertical = 22.dp),
            ) {
                Button(
                    onClick = {
                        if (page == 0) page = 1 else onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 53.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EducationPurple),
                    shape = RoundedCornerShape(11.dp),
                ) {
                    Text(
                        text = stringResource(R.string.onboarding_next),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun DepositEducationGuide(text: String, @DrawableRes petImageRes: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 112.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            modifier = Modifier
                .weight(1f)
                .background(Color.White, RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 12.dp),
            color = EducationDark,
            fontSize = 13.sp,
            lineHeight = 17.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.width(5.dp))
        Box(Modifier.width(132.dp).height(108.dp)) {
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
private fun DepositEducationStep(number: String, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 90.dp)
            .background(Color.White, RoundedCornerShape(10.dp))
            .border(1.5.dp, EducationDark, RoundedCornerShape(10.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = number,
            color = Color(0xFFD9A2F5),
            fontSize = 49.sp,
            fontWeight = FontWeight.ExtraBold,
        )
        Text(
            text = text,
            modifier = Modifier.weight(1f).padding(start = 14.dp),
            color = EducationDark,
            fontSize = 15.sp,
            lineHeight = 19.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun DepositEducationExample(@DrawableRes icon: Int, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 90.dp)
            .background(Color.White, RoundedCornerShape(10.dp))
            .border(1.5.dp, EducationDark, RoundedCornerShape(10.dp))
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(icon),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(52.dp),
        )
        Text(
            text = text,
            modifier = Modifier.weight(1f).padding(start = 14.dp),
            color = EducationDark,
            fontSize = 15.sp,
            lineHeight = 19.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun BudgetEducationPage(
    page: Int,
    recurringIncome: Int,
    recurringExpense: Int,
    plannedOptionalExpenses: Int,
    categoryRevealStep: Int,
    @DrawableRes petImageRes: Int,
) {
    when (page) {
        0 -> {
            BudgetEducationGuide(
                text = stringResource(
                    R.string.education_budget_game_day,
                    gameCycleDurationText(GameDefaults.CYCLE_DURATION_SECONDS),
                ),
                petImageRes = petImageRes,
            )
            Spacer(Modifier.height(18.dp))
            NumberedBudgetStep("1", stringResource(R.string.education_budget_step_plan))
            Spacer(Modifier.height(10.dp))
            NumberedBudgetStep("2", stringResource(R.string.education_budget_step_play))
            Spacer(Modifier.height(10.dp))
            NumberedBudgetStep("3", stringResource(R.string.education_budget_step_compare))
        }
        1 -> {
            BudgetEducationGuide(
                text = stringResource(
                    when (categoryRevealStep) {
                        0 -> R.string.education_budget_income_explanation
                        1 -> R.string.education_budget_income_groups
                        2 -> R.string.education_budget_expense_explanation
                        else -> R.string.education_budget_expense_groups
                    },
                ),
                petImageRes = petImageRes,
            )
            Text(
                text = stringResource(R.string.education_budget_categories_title),
                color = EducationDark,
                fontSize = 21.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(
                text = stringResource(R.string.education_budget_categories_body),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(EducationLavender, RoundedCornerShape(9.dp))
                    .padding(14.dp),
                color = EducationDark,
                fontSize = 14.sp,
                lineHeight = 18.sp,
            )
            Text(
                text = stringResource(R.string.budget_income),
                color = EducationDark,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            if (categoryRevealStep >= 1) {
                OutlinedCategoryList(
                    listOf(
                        stringResource(R.string.budget_recurring),
                        stringResource(R.string.budget_irregular),
                    ),
                )
            }
            if (categoryRevealStep >= 2) {
                Text(
                    text = stringResource(R.string.budget_expenses),
                    color = EducationDark,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
            if (categoryRevealStep >= 3) {
                OutlinedCategoryList(
                    listOf(
                        stringResource(R.string.budget_mandatory),
                        stringResource(R.string.budget_optional_short),
                    ),
                )
            }
        }
        else -> {
            BudgetEducationGuide(
                text = stringResource(R.string.education_budget_needs_speech),
                petImageRes = petImageRes,
            )
            Text(
                text = stringResource(R.string.education_budget_needs_title),
                color = EducationDark,
                fontSize = 21.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(
                text = stringResource(R.string.education_budget_needs_body),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(EducationLavender, RoundedCornerShape(9.dp))
                    .padding(14.dp),
                color = EducationDark,
                fontSize = 14.sp,
                lineHeight = 18.sp,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(10.dp))
                    .border(1.4.dp, EducationDark, RoundedCornerShape(10.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(R.drawable.lamp),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    contentScale = ContentScale.Fit,
                )
                Text(
                    text = stringResource(R.string.education_budget_needs_tip),
                    modifier = Modifier.weight(1f).padding(start = 12.dp),
                    color = EducationDark,
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                )
            }
        }
    }
}

@Composable
private fun BudgetEducationGuide(text: String, @DrawableRes petImageRes: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 102.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            modifier = Modifier
                .weight(1f)
                .background(Color.White, RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 14.dp),
            color = EducationDark,
            fontSize = 13.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
        )
        Image(
            painter = painterResource(petImageRes),
            contentDescription = stringResource(R.string.pet),
            modifier = Modifier.width(130.dp).height(102.dp),
            contentScale = ContentScale.Crop,
            alignment = Alignment.TopCenter,
        )
    }
}

@Composable
private fun NumberedBudgetStep(number: String, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 84.dp)
            .background(Color.White, RoundedCornerShape(10.dp))
            .border(1.4.dp, EducationDark, RoundedCornerShape(10.dp))
            .padding(horizontal = 15.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = number,
            color = Color(0xFFD9A2F5),
            fontSize = 46.sp,
            fontWeight = FontWeight.ExtraBold,
        )
        Text(
            text = text,
            modifier = Modifier.weight(1f).padding(start = 14.dp),
            color = EducationDark,
            fontSize = 14.sp,
            lineHeight = 18.sp,
        )
    }
}

@Composable
private fun OutlinedCategoryList(lines: List<String>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(10.dp))
            .border(1.4.dp, EducationDark, RoundedCornerShape(10.dp))
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        lines.forEach { line ->
            Text("•  $line", color = EducationDark, fontSize = 14.sp, lineHeight = 18.sp)
        }
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
