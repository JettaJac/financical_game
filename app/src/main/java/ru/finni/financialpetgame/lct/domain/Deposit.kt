package ru.finni.financialpetgame.lct.domain

data class ActiveDeposit(
    val amount: Int,
    val openedAtPeriod: Int,
    val durationCycles: Int,
    val interestPercent: Int,
    val earlyClosePenaltyPercent: Int,
) {
    val maturityPeriod: Int get() = openedAtPeriod + durationCycles

    fun remainingCycles(currentPeriod: Int): Int =
        (maturityPeriod - currentPeriod).coerceAtLeast(0)

    fun isMature(currentPeriod: Int): Boolean = currentPeriod >= maturityPeriod

    val maturityPayout: Int
        get() = amount + amount * interestPercent / 100

    val earlyClosePayout: Int
        get() = amount - amount * earlyClosePenaltyPercent / 100

    val profit: Int
        get() = (maturityPayout - amount).coerceAtLeast(0)

    val earlyCloseLoss: Int
        get() = (amount - earlyClosePayout).coerceAtLeast(0)
}

data class DepositTerm(
    val durationCycles: Int,
    val interestPercent: Int,
    val earlyClosePenaltyPercent: Int,
)

val DepositTerms = listOf(
    DepositTerm(durationCycles = 3, interestPercent = 7, earlyClosePenaltyPercent = 4),
    DepositTerm(durationCycles = 6, interestPercent = 40, earlyClosePenaltyPercent = 20),
    DepositTerm(durationCycles = 12, interestPercent = 50, earlyClosePenaltyPercent = 25),
)
