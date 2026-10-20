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
    private val baseEntries = listOf(
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
    )

    private val levelTwoEntries = listOf(
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

    val entries: List<BudgetEntry> get() = baseEntries
    fun entries(includeLevelTwoExpenses: Boolean, recurringExpense: Int): List<BudgetEntry> {
        val source = baseEntries + if (includeLevelTwoExpenses) levelTwoEntries else emptyList()
        val knownExpense = source.filter { it.category == BudgetCategory.Expense }.sumOf { it.amount }
        return source.map { entry ->
            if (entry.id == "school_lunch") entry.copy(amount = entry.amount + recurringExpense - knownExpense)
            else entry
        }
    }

    val income: Int = baseEntries.filter { it.category == BudgetCategory.Income }.sumOf { it.amount }
    val expense: Int = baseEntries.filter { it.category == BudgetCategory.Expense }.sumOf { it.amount }
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
    plannedIncome = snapshot.income,
    actualIncome = snapshot.income + snapshot.actualAdditionalIncome,
    plannedExpenses = snapshot.budgetPlanRecurringExpense + snapshot.plannedOptionalExpenses +
        snapshot.plannedGoalExpenses,
    actualExpenses = snapshot.budgetPlanRecurringExpense + snapshot.actualOptionalExpenses +
        snapshot.actualUnplannedExpenses + snapshot.actualGoalExpenses,
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
