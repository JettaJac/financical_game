package com.example.financial_game.data

import com.example.financial_game.domain.GameDefaults
import com.example.financial_game.domain.EyeColour
import com.example.financial_game.domain.Goals
import com.example.financial_game.domain.HairColour
import com.example.financial_game.domain.HairStyle

data class GameSnapshot(
    val name: String = GameDefaults.NAME,
    val hairColour: HairColour = HairColour.Beige,
    val eyeColour: EyeColour = EyeColour.Violet,
    val hairStyle: HairStyle = HairStyle.Default,

    val health: Int = GameDefaults.HEALTH,
    val happiness: Int = GameDefaults.HAPPINESS,
    val energy: Int = GameDefaults.ENERGY,
    val healthModifier: Int = 0,
    val happinessModifier: Int = 0,
    val energyModifier: Int = 0,
    val healthCycleAdjustment: Int = 0,
    val happinessCycleAdjustment: Int = 0,
    val energyCycleAdjustment: Int = 0,
    val money: Int = GameDefaults.MONEY,
    val income: Int = GameDefaults.INCOME,
    val expense: Int = GameDefaults.EXPENSE,

    val goalId: String = Goals.Pillow.name,
    val goalTitle: String = GameDefaults.GOAL,
    val goalTarget: Int = GameDefaults.GOAL_TARGET,
    val level: Int = GameDefaults.LEVEL,
    val purchasedGoalIds: Set<String> = emptySet(),
    val purchasedShopItemIds: Set<String> = emptySet(),

    val currentPeriod: Int = GameDefaults.CURRENT_PERIOD,
    val cycleEndsAtMillis: Long = 0L,
    val onboardingCompleted: Boolean = false,
    val budgetPlanWeek: Int = 0,
    val plannedOptionalExpenses: Int = 0,
    val actualOptionalExpenses: Int = 0,
    val actualAdditionalIncome: Int = 0,
    val lastReviewedBudgetWeek: Int = 0,
    val budgetTutorialCompleted: Boolean = false,
    val cooldownUnlockCycles: Map<String, Double> = emptyMap(),
    val taskUseCounts: Map<String, Int> = emptyMap(),
    val taskWeeklyUseCounts: Map<String, Int> = emptyMap(),
    val taskUseWeeks: Map<String, Int> = emptyMap(),
    val eventUnlockedTaskIds: Set<String> = emptySet(),
    val eventFlags: Set<String> = emptySet(),
    val completedEventIds: Set<String> = emptySet(),
    val eventPeriodOccurrences: Set<String> = emptySet(),
    val handledScenarioEntryIds: Set<String> = emptySet(),
    val eventPoolHandledCycles: Set<String> = emptySet(),
    val activeJobIds: Set<String> = emptySet(),
    val jobRemainingActions: Map<String, Int> = emptyMap(),
    val actionHistory: List<GameActionRecord> = emptyList(),
    val customGoal: CustomGoal? = null,
)

data class GameActionRecord(
    val timestamp: Long,
    val cycle: Int,
    val description: String,
    val moneyDelta: Int = 0,
)

data class CustomGoal(
    val title: String,
    val target: Int,
    val illustrationRes: Int,
)
