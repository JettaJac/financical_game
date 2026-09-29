package ru.finni.financialpetgame.lct.domain

import androidx.annotation.StringRes
import ru.finni.financialpetgame.lct.R
import ru.finni.financialpetgame.lct.data.GameSnapshot

enum class BudgetCategory { Income, Expense }

enum class BudgetFrequency { Recurring, Irregular, Savings }

data class BudgetEntry(
    val id: String,
    @param:StringRes val titleRes: Int,
    val amount: Int,
    val category: BudgetCategory,
    val frequency: BudgetFrequency,
)

object MandatoryBudget {
    val entries = listOf(
        BudgetEntry(
            id = "pocket_money",
            titleRes = R.string.budget_pocket_money,
            amount = GameDefaults.POCKET_MONEY_INCOME,
            category = BudgetCategory.Income,
            frequency = BudgetFrequency.Recurring,
        ),
        BudgetEntry(
            id = "school_lunch",
            titleRes = R.string.budget_school_lunch,
            amount = GameDefaults.SCHOOL_LUNCH_EXPENSE,
            category = BudgetCategory.Expense,
            frequency = BudgetFrequency.Recurring,
        ),
        BudgetEntry(
            id = "mobile_service",
            titleRes = R.string.budget_mobile_service,
            amount = GameDefaults.MOBILE_SERVICE_EXPENSE,
            category = BudgetCategory.Expense,
            frequency = BudgetFrequency.Recurring,
        ),
        BudgetEntry(
            id = "vitamins",
            titleRes = R.string.budget_vitamins,
            amount = GameDefaults.VITAMINS_EXPENSE,
            category = BudgetCategory.Expense,
            frequency = BudgetFrequency.Recurring,
        ),
        BudgetEntry(
            id = "sports_section",
            titleRes = R.string.budget_sports_section,
            amount = GameDefaults.SPORTS_SECTION_EXPENSE,
            category = BudgetCategory.Expense,
            frequency = BudgetFrequency.Recurring,
        ),
    )

    val income: Int = entries.filter { it.category == BudgetCategory.Income }.sumOf { it.amount }
    val expense: Int = entries.filter { it.category == BudgetCategory.Expense }.sumOf { it.amount }
    val maximumOptionalExpense: Int = (income - expense).coerceAtLeast(0)
}

data class BudgetPeriodResult(
    val plannedIncome: Int,
    val actualIncome: Int,
    val plannedExpenses: Int,
    val actualExpenses: Int,
) {
    val isSuccessful: Boolean
        get() = actualIncome >= plannedIncome && actualExpenses <= plannedExpenses
}

internal fun budgetWeekForPeriod(currentPeriod: Int): Int = weekForPeriod(currentPeriod) + 1

internal fun budgetPeriodResult(snapshot: GameSnapshot): BudgetPeriodResult = BudgetPeriodResult(
    plannedIncome = MandatoryBudget.income,
    actualIncome = MandatoryBudget.income + snapshot.actualAdditionalIncome,
    plannedExpenses = MandatoryBudget.expense + snapshot.plannedOptionalExpenses,
    actualExpenses = MandatoryBudget.expense + snapshot.actualOptionalExpenses,
)

internal fun needsBudgetReview(snapshot: GameSnapshot): Boolean {
    if (!snapshot.onboardingCompleted) return false
    val completedWeek = budgetWeekForPeriod(snapshot.currentPeriod) - 1
    return completedWeek > 0 &&
        snapshot.budgetPlanWeek == completedWeek &&
        snapshot.lastReviewedBudgetWeek < completedWeek
}

internal fun needsBudgetPlanning(snapshot: GameSnapshot): Boolean =
    snapshot.onboardingCompleted &&
        snapshot.budgetPlanWeek < budgetWeekForPeriod(snapshot.currentPeriod)
