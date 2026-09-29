package ru.finni.financialpetgame.lct.domain

object GameDefaults {
    const val CYCLE_DURATION_SECONDS = 7 * 60
    const val CYCLE_DURATION_MILLIS = CYCLE_DURATION_SECONDS * 1_000L
    const val NAME = "Финник"
    const val MONEY = 30
    const val HEALTH = 60
    const val HAPPINESS = 60
    const val ENERGY = 60
    const val CHARACTERISTIC_DECREASE_PER_CYCLE = 50
    const val POCKET_MONEY_INCOME = 85
    const val SCHOOL_LUNCH_EXPENSE = 30
    const val MOBILE_SERVICE_EXPENSE = 10
    const val VITAMINS_EXPENSE = 5
    const val SPORTS_SECTION_EXPENSE = 10
    const val INCOME = POCKET_MONEY_INCOME
    const val EXPENSE = SCHOOL_LUNCH_EXPENSE + MOBILE_SERVICE_EXPENSE +
        VITAMINS_EXPENSE + SPORTS_SECTION_EXPENSE
    const val LEVEL = 1
    const val CURRENT_PERIOD = 1
    const val GOAL = "Подушка"
    const val GOAL_TARGET = 120
}
