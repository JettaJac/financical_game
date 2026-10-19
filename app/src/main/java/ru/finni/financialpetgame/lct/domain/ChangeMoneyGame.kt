package ru.finni.financialpetgame.lct.domain

internal val ChangeMoneyDenominations = listOf(10, 50, 100, 500, 1_000, 5_000)

data class ChangeMoneyChallenge(
    val id: Int,
    val level: Int = id,
    val amount: Int,
    val requiredNotes: Int,
    val forbiddenDenominations: Set<Int> = emptySet(),
    val minimumDenomination: Int? = null,
    val maximumDenomination: Int? = null,
)

internal val ChangeMoneyChallenges = listOf(
    ChangeMoneyChallenge(id = 1, amount = 100, requiredNotes = 2),
    ChangeMoneyChallenge(id = 2, amount = 200, requiredNotes = 3),
    ChangeMoneyChallenge(id = 3, amount = 200, requiredNotes = 4),
    ChangeMoneyChallenge(id = 4, amount = 500, requiredNotes = 5),
    ChangeMoneyChallenge(
        id = 5,
        amount = 500,
        requiredNotes = 10,
        forbiddenDenominations = setOf(100),
    ),
    ChangeMoneyChallenge(
        id = 6,
        amount = 500,
        requiredNotes = 6,
        maximumDenomination = 100,
    ),
    ChangeMoneyChallenge(
        id = 7,
        amount = 2_000,
        requiredNotes = 4,
        forbiddenDenominations = setOf(100),
    ),
    ChangeMoneyChallenge(
        id = 8,
        amount = 1_000,
        requiredNotes = 6,
        maximumDenomination = 500,
    ),
    ChangeMoneyChallenge(
        id = 9,
        amount = 2_000,
        requiredNotes = 8,
        forbiddenDenominations = setOf(1_000),
    ),
    ChangeMoneyChallenge(
        id = 10,
        amount = 5_000,
        requiredNotes = 8,
        minimumDenomination = 50,
        maximumDenomination = 1_000,
    ),
)

internal fun nextChangeMoneyChallenge(lastChallengeId: Int): ChangeMoneyChallenge {
    val challengesByLevel = ChangeMoneyChallenges.sortedBy(ChangeMoneyChallenge::level)
    val lastIndex = challengesByLevel.indexOfFirst { it.id == lastChallengeId }
    return challengesByLevel[(lastIndex + 1).mod(challengesByLevel.size)]
}

enum class ChangeMoneyValidation {
    Success,
    WrongAmount,
    WrongNoteCount,
    ForbiddenDenomination,
    DenominationTooSmall,
    DenominationTooLarge,
}

internal fun validateChangeMoneySelection(
    challenge: ChangeMoneyChallenge,
    selectedCounts: Map<Int, Int>,
): ChangeMoneyValidation {
    val normalizedCounts = selectedCounts.filterValues { it > 0 }
    val noteCount = normalizedCounts.values.sum()
    val amount = normalizedCounts.entries.sumOf { (denomination, count) -> denomination * count }
    if (noteCount != challenge.requiredNotes) return ChangeMoneyValidation.WrongNoteCount
    if (amount != challenge.amount) return ChangeMoneyValidation.WrongAmount
    if (normalizedCounts.keys.any { it in challenge.forbiddenDenominations }) {
        return ChangeMoneyValidation.ForbiddenDenomination
    }
    if (challenge.minimumDenomination?.let { minimum ->
            normalizedCounts.keys.any { it < minimum }
        } == true
    ) {
        return ChangeMoneyValidation.DenominationTooSmall
    }
    if (challenge.maximumDenomination?.let { maximum ->
            normalizedCounts.keys.any { it > maximum }
        } == true
    ) {
        return ChangeMoneyValidation.DenominationTooLarge
    }
    return ChangeMoneyValidation.Success
}
