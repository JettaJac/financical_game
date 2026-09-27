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
    val cooldownUnlockCycles: Map<String, Double> = emptyMap(),
)
