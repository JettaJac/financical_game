package com.example.financial_game.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DepositTest {
    private val deposit = ActiveDeposit(
        amount = 100,
        openedAtPeriod = 3,
        durationCycles = 6,
        interestPercent = 40,
        earlyClosePenaltyPercent = 20,
    )

    @Test
    fun payoutsUseConfiguredPercentages() {
        assertEquals(140, deposit.maturityPayout)
        assertEquals(80, deposit.earlyClosePayout)
    }

    @Test
    fun maturityUsesGamePeriods() {
        assertEquals(6, deposit.remainingCycles(currentPeriod = 3))
        assertEquals(1, deposit.remainingCycles(currentPeriod = 8))
        assertFalse(deposit.isMature(currentPeriod = 8))
        assertTrue(deposit.isMature(currentPeriod = 9))
        assertEquals(0, deposit.remainingCycles(currentPeriod = 12))
    }
}
