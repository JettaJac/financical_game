package ru.finni.financialpetgame.lct.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PetTapRewardTest {
    @Test
    fun onlyFirstThreeTapsInCycleCanBeRewarded() {
        assertEquals(5, PET_TAP_HAPPINESS_REWARD)
        assertTrue(canRewardPetTap(0))
        assertTrue(canRewardPetTap(1))
        assertTrue(canRewardPetTap(2))
        assertFalse(canRewardPetTap(3))
        assertFalse(canRewardPetTap(4))
    }
}
