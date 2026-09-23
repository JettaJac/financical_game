package com.example.financial_game.ui

import com.example.financial_game.domain.moneyAfterCycle
import com.example.financial_game.domain.moneyAfterPurchase
import org.junit.Assert.assertEquals
import org.junit.Test

class PetViewModelTest {
    @Test
    fun countdown_is_formatted_as_minutes_and_seconds() {
        assertEquals("20:00", formatCountdown(TIMER_SECONDS))
        assertEquals("00:00", formatCountdown(0))
    }

    @Test
    fun collar_price_matches_spec() {
        assertEquals(100, COLLAR_PRICE)
    }

    @Test
    fun timer_cycle_applies_income_minus_expense() {
        assertEquals(125, moneyAfterCycle(money = 100, income = 35, expense = 10))
    }

    @Test
    fun purchase_only_charges_when_money_is_enough() {
        assertEquals(0, moneyAfterPurchase(money = 100, price = 100))
        assertEquals(99, moneyAfterPurchase(money = 99, price = 100))
    }
}
