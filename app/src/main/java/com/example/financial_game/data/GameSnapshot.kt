package com.example.financial_game.data

import com.example.financial_game.domain.GameDefaults

data class GameSnapshot(
    val health: Int = GameDefaults.HEALTH,
    val happiness: Int = GameDefaults.HAPPINESS,
    val energy: Int = GameDefaults.ENERGY,
    val money: Int = GameDefaults.MONEY,
    val income: Int = GameDefaults.INCOME,
    val expense: Int = GameDefaults.EXPENSE,

    val goalTitle: String = "GOAL",
    val goalTarget: Int = 1,
    val level: Int = 1,

    val currentPeriod: Int = 0,
    val onboardingCompleted: Boolean = false,

//    val inventory: List<String> TODO: store inventory only in the roomDB
//    val income: List<String>    TODO: different incomes should be took from roomDB ?
)
