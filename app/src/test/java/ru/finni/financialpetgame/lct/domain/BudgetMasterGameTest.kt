package ru.finni.financialpetgame.lct.domain

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BudgetMasterGameTest {
    @Test
    fun `pool contains every supplied card in the expected categories`() {
        assertEquals(63, BudgetMasterCards.size)
        assertEquals(22, BudgetMasterCards.count { it.category == BudgetMasterCategory.RecurringIncome })
        assertEquals(19, BudgetMasterCards.count { it.category == BudgetMasterCategory.IrregularIncome })
        assertEquals(9, BudgetMasterCards.count { it.category == BudgetMasterCategory.MandatoryExpense })
        assertEquals(13, BudgetMasterCards.count { it.category == BudgetMasterCategory.OptionalExpense })
        assertEquals(BudgetMasterCards.size, BudgetMasterCards.map { it.id }.distinct().size)
    }

    @Test
    fun `direction and category validation require the exact answer`() {
        val card = BudgetMasterCards.first { it.category == BudgetMasterCategory.RecurringIncome }

        assertTrue(isCorrectBudgetDirection(card, BudgetDirection.Income))
        assertFalse(isCorrectBudgetDirection(card, BudgetDirection.Expense))
        assertTrue(isCorrectBudgetCategory(card, BudgetMasterCategory.RecurringIncome))
        assertFalse(isCorrectBudgetCategory(card, BudgetMasterCategory.IrregularIncome))
    }

    @Test
    fun `random selection avoids used cards while alternatives remain`() {
        val retained = BudgetMasterCards.last()
        val selected = randomBudgetMasterCard(
            excludedIds = BudgetMasterCards.dropLast(1).mapTo(mutableSetOf()) { it.id },
            random = Random(1),
        )

        assertEquals(retained, selected)
    }
}
