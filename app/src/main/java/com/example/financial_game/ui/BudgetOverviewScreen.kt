package com.example.financial_game.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financial_game.R
import com.example.financial_game.data.GameSnapshot
import com.example.financial_game.domain.BudgetCategory
import com.example.financial_game.domain.BudgetEntry
import com.example.financial_game.domain.MandatoryBudget
import com.example.financial_game.ui.theme.NunitoFontFamily

private val OverviewPurple = Color(0xFF8138D0)
private val OverviewPurpleDark = Color(0xFF54206F)
private val OverviewLight = Color(0xFFF1D9FC)

private enum class BudgetView { Current, Planned }

@Composable
internal fun BudgetOverviewScreen(
    state: GameSnapshot,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var view by rememberSaveable { mutableStateOf(BudgetView.Current) }
    var category by rememberSaveable { mutableStateOf(BudgetCategory.Income) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0f to Color(0xFFF5E4FC),
                    0.45f to Color.White,
                    1f to Color.White,
                ),
            )
            .safeDrawingPadding()
            .padding(horizontal = 20.dp),
    ) {
        Box(Modifier.fillMaxWidth().height(64.dp)) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                Text("‹", color = OverviewPurpleDark, fontSize = 34.sp)
            }
            Text(
                text = stringResource(
                    if (view == BudgetView.Current) R.string.budget_current
                    else R.string.budget_planned_title,
                ),
                modifier = Modifier.align(Alignment.Center),
                color = OverviewPurpleDark,
                fontFamily = NunitoFontFamily,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
            )
        }

        Spacer(Modifier.height(20.dp))
        Text(
            text = state.money.toString(),
            modifier = Modifier.align(Alignment.CenterHorizontally),
            color = OverviewPurpleDark,
            fontFamily = NunitoFontFamily,
            fontSize = 48.sp,
            fontWeight = FontWeight.ExtraBold,
        )
        Text(
            text = stringResource(R.string.budget_coins),
            modifier = Modifier.align(Alignment.CenterHorizontally),
            color = OverviewPurpleDark,
            fontFamily = NunitoFontFamily,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
        )
        Spacer(Modifier.height(30.dp))
        Button(
            onClick = {
                view = if (view == BudgetView.Current) BudgetView.Planned else BudgetView.Current
            },
            modifier = Modifier.align(Alignment.CenterHorizontally),
            colors = ButtonDefaults.buttonColors(containerColor = OverviewPurple),
            shape = RoundedCornerShape(9.dp),
        ) {
            Text(
                stringResource(
                    if (view == BudgetView.Current) R.string.budget_show_planned
                    else R.string.budget_show_current,
                ),
                fontFamily = NunitoFontFamily,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.height(34.dp))
        BudgetCategoryTabs(category = category, onSelected = { category = it })
        Spacer(Modifier.height(16.dp))
        BudgetDetails(
            view = view,
            category = category,
            state = state,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun BudgetCategoryTabs(
    category: BudgetCategory,
    onSelected: (BudgetCategory) -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().height(48.dp).clip(RoundedCornerShape(9.dp)).background(OverviewLight),
    ) {
        BudgetCategory.entries.forEach { item ->
            val selected = item == category
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (selected) OverviewPurple else Color.Transparent)
                    .clickable { onSelected(item) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(
                        if (item == BudgetCategory.Income) R.string.budget_income
                        else R.string.budget_expenses,
                    ),
                    color = if (selected) Color.White else OverviewPurpleDark,
                    fontFamily = NunitoFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
        }
    }
}

@Composable
private fun BudgetDetails(
    view: BudgetView,
    category: BudgetCategory,
    state: GameSnapshot,
    modifier: Modifier,
) {
    val recurring = MandatoryBudget.entries.filter { it.category == category }
    val irregularAmount = when {
        category == BudgetCategory.Income && view == BudgetView.Current -> state.actualAdditionalIncome
        category == BudgetCategory.Expense && view == BudgetView.Current -> state.actualOptionalExpenses
        category == BudgetCategory.Expense -> state.plannedOptionalExpenses
        else -> 0
    }

    Column(modifier.verticalScroll(rememberScrollState())) {
        BudgetGroup(
            title = stringResource(
                if (category == BudgetCategory.Expense) R.string.budget_mandatory
                else R.string.budget_recurring,
            ),
            initiallyExpanded = true,
        ) {
            recurring.forEach { BudgetOverviewEntry(it) }
        }
        BudgetGroup(
            title = stringResource(
                if (category == BudgetCategory.Expense) R.string.budget_optional_short
                else R.string.budget_irregular,
            ),
            initiallyExpanded = irregularAmount > 0,
        ) {
            if (irregularAmount > 0) {
                BudgetAmountRow(
                    title = stringResource(
                        if (category == BudgetCategory.Income) R.string.budget_additional_income
                        else R.string.budget_optional_expenses,
                    ),
                    amount = irregularAmount,
                )
            } else {
                Text(
                    text = stringResource(R.string.budget_no_operations),
                    color = OverviewPurpleDark.copy(alpha = 0.6f),
                    fontFamily = NunitoFontFamily,
                    modifier = Modifier.padding(vertical = 10.dp),
                )
            }
        }
        BudgetGroup(title = stringResource(R.string.budget_unplanned), initiallyExpanded = false) {
            Text(
                text = stringResource(R.string.budget_no_operations),
                color = OverviewPurpleDark.copy(alpha = 0.6f),
                fontFamily = NunitoFontFamily,
                modifier = Modifier.padding(vertical = 10.dp),
            )
        }
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun BudgetGroup(
    title: String,
    initiallyExpanded: Boolean,
    content: @Composable () -> Unit,
) {
    var expanded by rememberSaveable(title) { mutableStateOf(initiallyExpanded) }
    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                color = OverviewPurpleDark,
                fontFamily = NunitoFontFamily,
                fontSize = 18.sp,
            )
            Text(if (expanded) "⌃" else "⌄", color = OverviewPurpleDark, fontSize = 20.sp)
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(OverviewPurpleDark))
        if (expanded) content()
    }
}

@Composable
private fun BudgetOverviewEntry(entry: BudgetEntry) {
    BudgetAmountRow(title = stringResource(entry.titleRes), amount = entry.amount)
}

@Composable
private fun BudgetAmountRow(title: String, amount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            color = OverviewPurpleDark,
            fontFamily = NunitoFontFamily,
            fontSize = 16.sp,
        )
        Text(
            text = amount.toString(),
            color = OverviewPurpleDark,
            fontFamily = NunitoFontFamily,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )
        Image(
            painter = painterResource(R.drawable.coin_money),
            contentDescription = null,
            modifier = Modifier.padding(start = 4.dp).size(20.dp),
        )
    }
}
