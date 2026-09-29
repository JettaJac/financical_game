package ru.finni.financialpetgame.lct.domain

import ru.finni.financialpetgame.lct.data.GameSnapshot

internal fun characteristicValue(
    baseValue: Int,
    permanentModifier: Int,
    cycleAdjustment: Int,
    secondsRemaining: Int,
    cycleDurationSeconds: Int = GameDefaults.CYCLE_DURATION_SECONDS,
): Int {
    if (cycleDurationSeconds <= 0) return (baseValue + permanentModifier + cycleAdjustment)
        .coerceIn(0, 100)
    val elapsedSeconds = (cycleDurationSeconds - secondsRemaining)
        .coerceIn(0, cycleDurationSeconds)
    val decrease = (GameDefaults.CHARACTERISTIC_DECREASE_PER_CYCLE.toLong() * elapsedSeconds /
        cycleDurationSeconds).toInt()
    return (baseValue + permanentModifier + cycleAdjustment - decrease).coerceIn(0, 100)
}

internal fun GameSnapshot.withCharacteristicsAt(secondsRemaining: Int): GameSnapshot = copy(
    health = characteristicValue(
        GameDefaults.HEALTH, healthModifier, healthCycleAdjustment, secondsRemaining,
    ),
    happiness = characteristicValue(
        GameDefaults.HAPPINESS, happinessModifier, happinessCycleAdjustment, secondsRemaining,
    ),
    energy = characteristicValue(
        GameDefaults.ENERGY, energyModifier, energyCycleAdjustment, secondsRemaining,
    ),
)
