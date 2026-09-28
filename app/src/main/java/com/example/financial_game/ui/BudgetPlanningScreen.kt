package com.example.financial_game.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financial_game.R
import com.example.financial_game.domain.BudgetCategory
import com.example.financial_game.domain.BudgetEntry
import com.example.financial_game.domain.BudgetFrequency
import com.example.financial_game.domain.MandatoryBudget
import com.example.financial_game.ui.theme.NunitoFontFamily

private const val BUDGET_INTRO = 0
private const val BUDGET_CLASSIFY = 1
private const val BUDGET_SUMMARY = 2
private const val BUDGET_OPTIONAL = 3
private const val BUDGET_REVIEW = 4

private enum class ClassificationStep { Category, Frequency }

private val BudgetPurple = Color(0xFF8138D0)
private val BudgetPurpleDark = Color(0xFF54206F)
private val BudgetLightPurple = Color(0xFFDCAAF7)
private val BudgetBackground = Color(0xFFF8EDFF)

@Composable
internal fun BudgetPlanningScreen(
    firstPlanning: Boolean,
    previousOptionalExpenses: Int,
    onComplete: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var screen by rememberSaveable { mutableIntStateOf(BUDGET_INTRO) }
    var entryIndex by rememberSaveable { mutableIntStateOf(0) }
    var classificationStep by rememberSaveable { mutableStateOf(ClassificationStep.Category) }
    var guideText by rememberSaveable { mutableStateOf<String?>(null) }
    var optionalExpenses by rememberSaveable {
        mutableStateOf(
            (if (previousOptionalExpenses > 0) previousOptionalExpenses else 20).toString(),
        )
    }
    val optionalExpenseAmount = optionalExpenses.toIntOrNull() ?: 0
    val optionalExpenseTooHigh = optionalExpenseAmount > MandatoryBudget.maximumOptionalExpense

    when (screen) {
        BUDGET_INTRO -> BudgetIntroScreen(
            onContinue = {
                screen = if (firstPlanning) BUDGET_CLASSIFY else BUDGET_OPTIONAL
            },
            modifier = modifier,
        )
        BUDGET_CLASSIFY -> {
            val entry = MandatoryBudget.entries[entryIndex]
            BudgetClassificationScreen(
                entry = entry,
                placedEntries = MandatoryBudget.entries.take(entryIndex),
                step = classificationStep,
                guideText = guideText,
                onCategorySelected = { selected ->
                    if (selected == entry.category) {
                        classificationStep = ClassificationStep.Frequency
                        guideText = null
                    } else {
                        guideText = if (entry.category == BudgetCategory.Income) {
                            "Это доход — деньги, которые мы получаем. Попробуй ещё раз!"
                        } else {
                            "Это расход — деньги, которые мы тратим. Попробуй ещё раз!"
                        }
                    }
                },
                onFrequencySelected = { selected ->
                    if (selected == entry.frequency) {
                        if (entryIndex == MandatoryBudget.entries.lastIndex) {
                            screen = BUDGET_SUMMARY
                        } else {
                            entryIndex += 1
                            classificationStep = ClassificationStep.Category
                            guideText = "Верно! Отлично получается — идём дальше."
                        }
                    } else {
                        guideText = "Почти! Этот платёж повторяется каждую неделю."
                    }
                },
                modifier = modifier,
            )
        }
        BUDGET_SUMMARY -> BudgetMandatorySummaryScreen(
            onContinue = { screen = BUDGET_OPTIONAL },
            modifier = modifier,
        )
        BUDGET_OPTIONAL -> BudgetOptionalExpensesScreen(
            value = optionalExpenses,
            onValueChange = { value -> optionalExpenses = value.filter(Char::isDigit).take(6) },
            maximumOptionalExpenses = MandatoryBudget.maximumOptionalExpense,
            showBalanceError = optionalExpenseTooHigh,
            onContinue = {
                if (!optionalExpenseTooHigh) screen = BUDGET_REVIEW
            },
            modifier = modifier,
        )
        BUDGET_REVIEW -> BudgetReviewScreen(
            optionalExpenses = optionalExpenseAmount,
            onComplete = { onComplete(optionalExpenseAmount) },
            modifier = modifier,
        )
    }
}

@Composable
private fun BudgetIntroScreen(onContinue: () -> Unit, modifier: Modifier) {
    Box(
        modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        Image(
            painter = painterResource(R.drawable.home_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.White.copy(alpha = 0.08f),
                        0.72f to Color.White.copy(alpha = 0.15f),
                        1f to Color.White,
                    ),
                ),
        )
        Box(Modifier.fillMaxSize().safeDrawingPadding()) {
            BudgetSpeechBubble(
                text = stringResource(R.string.budget_intro_message),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 72.dp, start = 24.dp, end = 24.dp),
            )
            Image(
                painter = painterResource(R.drawable.pet_main),
                contentDescription = stringResource(R.string.pet),
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(0.78f),
                contentScale = ContentScale.Fit,
            )
            BudgetPrimaryButton(
                text = stringResource(R.string.budget_make_plan),
                onClick = onContinue,
                modifier = Modifier.align(Alignment.BottomCenter).padding(20.dp),
            )
        }
    }
}

@Composable
private fun BudgetClassificationScreen(
    entry: BudgetEntry,
    placedEntries: List<BudgetEntry>,
    step: ClassificationStep,
    guideText: String?,
    onCategorySelected: (BudgetCategory) -> Unit,
    onFrequencySelected: (BudgetFrequency) -> Unit,
    modifier: Modifier,
) {
    BudgetPage(
        guide = guideText ?: stringResource(
            if (step == ClassificationStep.Category) {
                R.string.budget_choose_category_guide
            } else {
                R.string.budget_choose_frequency_guide
            },
        ),
        modifier = modifier,
        bottom = {
            Text(
                text = stringResource(
                    if (step == ClassificationStep.Category) {
                        R.string.budget_choose_category
                    } else {
                        R.string.budget_choose_subcategory
                    },
                ),
                color = BudgetPurpleDark,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            Spacer(Modifier.height(10.dp))
            BudgetEntryRow(entry)
            Spacer(Modifier.height(8.dp))
            if (step == ClassificationStep.Category) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    BudgetChoiceButton(
                        text = stringResource(R.string.budget_income),
                        onClick = { onCategorySelected(BudgetCategory.Income) },
                        modifier = Modifier.weight(1f),
                    )
                    BudgetChoiceButton(
                        text = stringResource(R.string.budget_expenses),
                        onClick = { onCategorySelected(BudgetCategory.Expense) },
                        modifier = Modifier.weight(1f),
                    )
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    BudgetFrequency.entries.forEach { frequency ->
                        BudgetChoiceButton(
                            text = stringResource(frequency.labelRes(entry.category)),
                            onClick = { onFrequencySelected(frequency) },
                            adaptiveText = entry.category == BudgetCategory.Expense,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        },
    ) {
        BudgetSections(entries = placedEntries)
    }
}

@Composable
private fun BudgetMandatorySummaryScreen(onContinue: () -> Unit, modifier: Modifier) {
    BudgetPage(
        guide = stringResource(R.string.budget_mandatory_success),
        modifier = modifier,
        bottom = {
            BudgetPrimaryButton(
                text = stringResource(R.string.onboarding_next),
                onClick = onContinue,
            )
        },
    ) {
        BudgetSections(entries = MandatoryBudget.entries, showFrequency = true)
    }
}

@Composable
private fun BudgetOptionalExpensesScreen(
    value: String,
    onValueChange: (String) -> Unit,
    maximumOptionalExpenses: Int,
    showBalanceError: Boolean,
    onContinue: () -> Unit,
    modifier: Modifier,
) {
    BudgetPage(
        guide = if (showBalanceError) {
            stringResource(R.string.budget_optional_too_high, maximumOptionalExpenses)
        } else {
            stringResource(R.string.budget_optional_guide)
        },
        modifier = modifier,
        bottom = {
            Text(
                text = stringResource(R.string.budget_enter_amount),
                color = BudgetPurpleDark,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = showBalanceError,
                supportingText = {
                    Text(
                        text = stringResource(
                            R.string.budget_optional_maximum,
                            maximumOptionalExpenses,
                        ),
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle = TextStyle(
                    color = BudgetPurpleDark,
                    fontFamily = NunitoFontFamily,
                    fontSize = 18.sp,
                ),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BudgetPurpleDark,
                    unfocusedBorderColor = BudgetPurpleDark,
                    errorBorderColor = Color(0xFFB3261E),
                ),
            )
            Spacer(Modifier.height(12.dp))
            BudgetPrimaryButton(
                text = stringResource(R.string.onboarding_next),
                onClick = onContinue,
            )
        },
    ) {
        BudgetSections(
            entries = MandatoryBudget.entries,
            showFrequency = true,
        )
    }
}

@Composable
private fun BudgetReviewScreen(
    optionalExpenses: Int,
    onComplete: () -> Unit,
    modifier: Modifier,
) {
    val totalExpenses = MandatoryBudget.expense + optionalExpenses
    val balance = MandatoryBudget.income - totalExpenses
    BudgetPage(
        guide = stringResource(R.string.budget_review_guide),
        modifier = modifier,
        bottom = {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.budget_total),
                        color = BudgetPurpleDark,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Text(
                        text = stringResource(R.string.budget_balance_hint),
                        color = BudgetPurple,
                        fontSize = 10.sp,
                    )
                }
                Text(
                    text = balance.toString(),
                    color = BudgetPurpleDark,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
                Spacer(Modifier.width(5.dp))
                Image(
                    painter = painterResource(R.drawable.coin_money),
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(Modifier.height(12.dp))
            BudgetPrimaryButton(
                text = stringResource(R.string.budget_to_game),
                onClick = onComplete,
            )
        },
    ) {
        BudgetSectionTitle(stringResource(R.string.budget_income))
        BudgetEntryRow(MandatoryBudget.entries.first())
        Spacer(Modifier.height(15.dp))
        BudgetSectionTitle(stringResource(R.string.budget_expenses))
        MandatoryBudget.entries
            .filter { it.category == BudgetCategory.Expense }
            .forEach { BudgetEntryRow(it) }
        if (optionalExpenses > 0) {
            BudgetValueRow(
                title = stringResource(R.string.budget_optional_expenses),
                amount = optionalExpenses,
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = stringResource(R.string.budget_total_expenses),
                color = BudgetPurpleDark,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = totalExpenses.toString(),
                color = BudgetPurpleDark,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun BudgetPage(
    guide: String,
    modifier: Modifier,
    bottom: @Composable ColumnScope.() -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        BudgetLightPurple.copy(alpha = 0.72f),
                        BudgetBackground,
                        Color.White,
                    ),
                ),
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .imePadding(),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp),
                verticalAlignment = Alignment.Top,
            ) {
                BudgetSpeechBubble(guide, Modifier.weight(1f))
                Image(
                    painter = painterResource(R.drawable.pet_main),
                    contentDescription = stringResource(R.string.pet),
                    modifier = Modifier.size(112.dp),
                    contentScale = ContentScale.Crop,
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                content = content,
            )
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    content = bottom,
                )
            }
        }
    }
}

@Composable
private fun BudgetSections(entries: List<BudgetEntry>, showFrequency: Boolean = false) {
    BudgetSectionTitle(stringResource(R.string.budget_income))
    if (showFrequency && entries.any { it.category == BudgetCategory.Income }) {
        BudgetSubheading(stringResource(R.string.budget_recurring))
    }
    entries.filter { it.category == BudgetCategory.Income }.forEach { BudgetEntryRow(it) }
    Spacer(Modifier.height(24.dp))
    BudgetSectionTitle(stringResource(R.string.budget_expenses))
    if (showFrequency && entries.any { it.category == BudgetCategory.Expense }) {
        BudgetSubheading(stringResource(R.string.budget_recurring))
    }
    entries.filter { it.category == BudgetCategory.Expense }.forEach { BudgetEntryRow(it) }
}

@Composable
private fun BudgetSectionTitle(text: String) {
    Text(
        text = text,
        color = BudgetPurpleDark,
        fontSize = 23.sp,
        fontWeight = FontWeight.ExtraBold,
        fontFamily = NunitoFontFamily,
    )
}

@Composable
private fun BudgetSubheading(text: String) {
    Text(
        text = text,
        color = BudgetPurpleDark,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun BudgetEntryRow(entry: BudgetEntry) {
    BudgetValueRow(title = stringResource(entry.titleRes), amount = entry.amount)
}

@Composable
private fun BudgetValueRow(title: String, amount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, BudgetPurpleDark, RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = amount.toString(), color = BudgetPurpleDark, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(4.dp))
        Image(
            painter = painterResource(R.drawable.coin_money),
            contentDescription = null,
            modifier = Modifier.size(15.dp),
        )
        Text(text = "  •  $title", color = BudgetPurpleDark, fontSize = 14.sp)
    }
}

@Composable
private fun BudgetSpeechBubble(text: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, color = Color.White, shape = RoundedCornerShape(9.dp)) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            color = BudgetPurpleDark,
            fontFamily = NunitoFontFamily,
            fontSize = 14.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun BudgetChoiceButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    adaptiveText: Boolean = false,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(46.dp),
        colors = ButtonDefaults.buttonColors(containerColor = BudgetPurple),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp),
    ) {
        if (adaptiveText) {
            Text(
                text = text,
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1,
                softWrap = false,
                textAlign = TextAlign.Center,
                autoSize = TextAutoSize.StepBased(
                    minFontSize = 8.sp,
                    maxFontSize = 12.sp,
                    stepSize = 0.5.sp,
                ),
            )
        } else {
            Text(text = text, fontSize = 12.sp, maxLines = 1)
        }
    }
}

@Composable
private fun BudgetPrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(52.dp),
        colors = ButtonDefaults.buttonColors(containerColor = BudgetPurple),
        shape = RoundedCornerShape(10.dp),
    ) {
        Text(text = text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

private fun BudgetFrequency.labelRes(category: BudgetCategory): Int = when (category) {
    BudgetCategory.Income -> when (this) {
        BudgetFrequency.Recurring -> R.string.budget_recurring
        BudgetFrequency.Irregular -> R.string.budget_irregular
        BudgetFrequency.Savings -> R.string.budget_savings
    }
    BudgetCategory.Expense -> when (this) {
        BudgetFrequency.Recurring -> R.string.budget_recurring
        BudgetFrequency.Irregular -> R.string.budget_optional_short
        BudgetFrequency.Savings -> R.string.budget_unplanned
    }
}
