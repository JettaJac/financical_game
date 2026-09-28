package com.example.financial_game.domain

import com.example.financial_game.data.GameSnapshot
import kotlinx.coroutines.flow.Flow
import com.example.financial_game.domain.events.JobDef
import com.example.financial_game.domain.events.ScheduledEvent

interface GameRepository {
    val snapshot: Flow<GameSnapshot>

    suspend fun initialize()

    suspend fun completeTimerCycle()

    suspend fun advanceExpiredCycles(nowMillis: Long)

    suspend fun buyCareItem(item: CardItem)

    suspend fun buyGoal(goal: Goals, price: Int): Boolean

    suspend fun selectGoal(goal: Goals)

    suspend fun applyEffects(effects: List<Effect>)

    suspend fun completeOnboarding(setup: PetSetup)

    suspend fun updatePetAppearance(appearance: PetAppearance): Boolean

    suspend fun saveBudgetPlan(optionalExpenses: Int)

    suspend fun completeBudgetReview()

    suspend fun resolveScheduledEvent(
        event: ScheduledEvent,
        unlockedJob: JobDef?,
        accepted: Boolean,
    )

    suspend fun reset()
}

internal fun completedWeeks(currentPeriod: Int, completedCycles: Int): Int {
    if (completedCycles <= 0) return 0
    val safePeriod = currentPeriod.coerceAtLeast(1)
    return (safePeriod + completedCycles - 1) / 7 - (safePeriod - 1) / 7
}

internal fun moneyAfterCompletedCycles(
    money: Int,
    income: Int,
    expense: Int,
    currentPeriod: Int,
    completedCycles: Int,
): Int = money + (income - expense) * completedWeeks(currentPeriod, completedCycles)

internal fun hasCycleExpired(cycleEndsAtMillis: Long, nowMillis: Long): Boolean =
    cycleEndsAtMillis > 0L && nowMillis >= cycleEndsAtMillis

internal fun nextCycleEnd(nowMillis: Long): Long =
    nowMillis + GameDefaults.CYCLE_DURATION_MILLIS

internal fun cycleSecondsRemaining(cycleEndsAtMillis: Long, nowMillis: Long): Int {
    val remainingMillis = (cycleEndsAtMillis - nowMillis).coerceAtLeast(0L)
    return ((remainingMillis + 999L) / 1_000L)
        .coerceAtMost(Int.MAX_VALUE.toLong())
        .toInt()
}

internal fun moneyAfterPurchase(money: Int, price: Int): Int =
    if (money >= price) money - price else money

internal fun levelAfterGoalPurchase(currentLevel: Int, goalLevel: Int): Int =
    if (goalLevel == currentLevel) currentLevel + 1 else currentLevel
