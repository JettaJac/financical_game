package com.example.financial_game.domain

import com.example.financial_game.data.GameSnapshot
import kotlinx.coroutines.flow.Flow

interface GameRepository {
    val snapshot: Flow<GameSnapshot>

    suspend fun initialize()

    suspend fun completeTimerCycle()

    suspend fun buyCollar()

    suspend fun buyCareItem(item: CardItem)

    suspend fun buyGoal(goal: Goals, price: Int): Boolean

    suspend fun selectGoal(goal: Goals)

    suspend fun applyEffects(effects: List<Effect>)

    suspend fun completeOnboarding(setup: PetSetup)

    suspend fun reset()
}

internal fun moneyAfterCycle(money: Int, income: Int, expense: Int): Int =
    money + income - expense

internal fun moneyAfterPurchase(money: Int, price: Int): Int =
    if (money >= price) money - price else money

internal fun levelAfterGoalPurchase(currentLevel: Int, goalLevel: Int): Int =
    if (goalLevel == currentLevel) currentLevel + 1 else currentLevel
