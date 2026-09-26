package com.example.financial_game.ui

import com.example.financial_game.R
import com.example.financial_game.data.GameSnapshot
import com.example.financial_game.domain.moneyAfterCycle
import com.example.financial_game.domain.moneyAfterPurchase
import com.example.financial_game.domain.levelAfterGoalPurchase
import com.example.financial_game.domain.cooldownExpireAt
import com.example.financial_game.domain.cooldownCyclesRemaining
import com.example.financial_game.domain.cooldownSecondsRemaining
import com.example.financial_game.domain.cooldownUnlockPeriod
import com.example.financial_game.domain.cooldownRemaining
import com.example.financial_game.domain.Cooldown
import com.example.financial_game.domain.CooldownRemaining
import com.example.financial_game.domain.Effect
import com.example.financial_game.domain.Resource
import com.example.financial_game.domain.Goals
import com.example.financial_game.domain.canApplyEffects
import com.example.financial_game.domain.cycleSecondsRemaining
import com.example.financial_game.domain.hasCycleExpired
import com.example.financial_game.domain.nextCycleEnd
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
    fun persisted_cycle_restores_remaining_time() {
        assertEquals(
            61,
            cycleSecondsRemaining(cycleEndsAtMillis = 62_000L, nowMillis = 1_000L),
        )
        assertEquals(
            0,
            cycleSecondsRemaining(cycleEndsAtMillis = 1_000L, nowMillis = 1_000L),
        )
    }

    @Test
    fun expired_deadline_completes_only_one_cycle_and_starts_a_fresh_one() {
        val oldDeadline = 10_000L
        val muchLater = oldDeadline + 10_000_000L

        assertEquals(true, hasCycleExpired(oldDeadline, muchLater))
        assertEquals(
            muchLater + 1_200_000L,
            nextCycleEnd(muchLater),
        )
    }

    @Test
    fun purchase_only_charges_when_money_is_enough() {
        assertEquals(0, moneyAfterPurchase(money = 100, price = 100))
        assertEquals(99, moneyAfterPurchase(money = 99, price = 100))
    }

    @Test
    fun time_cooldown_expire_is_calculated_from_minutes() {
        assertEquals(121_000L, cooldownExpireAt(nowMillis = 1_000L, cooldownMinutes = 2))
    }

    @Test
    fun cooldown_rounds_up_until_expire_time_is_reached() {
        assertEquals(2, cooldownSecondsRemaining(expireAtMillis = 2_001L, nowMillis = 1_000L))
        assertEquals(1, cooldownSecondsRemaining(expireAtMillis = 2_000L, nowMillis = 1_001L))
        assertEquals(0, cooldownSecondsRemaining(expireAtMillis = 2_000L, nowMillis = 2_000L))
        assertEquals(0, cooldownSecondsRemaining(expireAtMillis = 2_000L, nowMillis = 3_000L))
    }

    @Test
    fun cycle_cooldown_unlocks_after_configured_number_of_days() {
        val unlockPeriod = cooldownUnlockPeriod(currentPeriod = 4, cooldownDays = 3)

        assertEquals(7, unlockPeriod)
        assertEquals(3, cooldownCyclesRemaining(unlockPeriod, currentPeriod = 4))
        assertEquals(1, cooldownCyclesRemaining(unlockPeriod, currentPeriod = 6))
        assertEquals(0, cooldownCyclesRemaining(unlockPeriod, currentPeriod = 7))
    }

    @Test
    fun cooldown_remaining_supports_time_and_cycle_types() {
        assertEquals(
            CooldownRemaining.Time(60),
            cooldownRemaining(
                cooldown = Cooldown.Time(minutes = 1),
                expireAtMillis = 61_000L,
                unlockPeriod = 0,
                nowMillis = 1_000L,
                currentPeriod = 1,
            ),
        )
        assertEquals(
            CooldownRemaining.Cycles(2),
            cooldownRemaining(
                cooldown = Cooldown.ByCycle(days = 2),
                expireAtMillis = 0L,
                unlockPeriod = 5,
                nowMillis = 1_000L,
                currentPeriod = 3,
            ),
        )
    }

    @Test
    fun event_with_money_cost_cannot_be_accepted_without_enough_money() {
        val effects = listOf(Effect(Resource.Money, -30), Effect(Resource.Energy, 10))

        assertEquals(false, canApplyEffects(money = 29, effects = effects))
        assertEquals(true, canApplyEffects(money = 30, effects = effects))
    }

    @Test
    fun reaching_goal_creates_purchase_event_with_current_goal_data() {
        val resources = GameSnapshot(
            goalId = Goals.Ball.name,
            goalTarget = 100,
            money = 100,
            onboardingCompleted = true,
        )

        val event = goalPurchaseEvent(resources)

        assertEquals(Goals.Ball.titleRes, event.descriptionRes)
        assertEquals(Goals.Ball.illustrationRes, event.illustrationRes)
        assertEquals(Goals.Ball.goalEffects + Effect(Resource.Money, -100), event.acceptEffects)
        assertEquals(R.string.buy, event.acceptButtonTextRes)
        assertEquals(true, event.showCloseButton)
        assertEquals(Goals.Ball, event.goalPurchase?.goal)
        assertEquals(100, event.goalPurchase?.price)
    }

    @Test
    fun goal_event_is_emitted_only_when_threshold_is_crossed() {
        val before = GameSnapshot(
            goalId = Goals.Pillow.name,
            goalTarget = 100,
            money = 99,
            onboardingCompleted = true,
        )
        val reached = before.copy(money = 100)

        assertEquals(true, hasJustReachedGoal(before, reached))
        assertEquals(false, hasJustReachedGoal(reached, reached.copy(money = 120)))
        assertEquals(
            false,
            hasJustReachedGoal(
                before,
                reached.copy(purchasedGoalIds = setOf(Goals.Pillow.name)),
            ),
        )
    }

    @Test
    fun buying_goal_of_current_level_increases_character_level() {
        assertEquals(2, levelAfterGoalPurchase(currentLevel = 1, goalLevel = 1))
        assertEquals(3, levelAfterGoalPurchase(currentLevel = 3, goalLevel = 2))
    }
}
