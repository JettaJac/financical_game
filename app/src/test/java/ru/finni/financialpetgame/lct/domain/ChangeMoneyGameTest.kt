package ru.finni.financialpetgame.lct.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class ChangeMoneyGameTest {
    @Test
    fun every_challenge_has_a_valid_solution_without_two_hundred_banknotes() {
        val solutions = mapOf(
            1 to mapOf(50 to 2),
            2 to mapOf(100 to 1, 50 to 2),
            3 to mapOf(50 to 4),
            4 to mapOf(100 to 5),
            5 to mapOf(50 to 10),
            6 to mapOf(100 to 4, 50 to 2),
            7 to mapOf(500 to 4),
            8 to mapOf(500 to 1, 100 to 5),
            9 to mapOf(500 to 3, 100 to 5),
            10 to mapOf(1_000 to 2, 500 to 6),
        )

        assertEquals(10, ChangeMoneyChallenges.size)
        ChangeMoneyChallenges.forEach { challenge ->
            assertEquals(
                "Challenge ${challenge.id}",
                ChangeMoneyValidation.Success,
                validateChangeMoneySelection(challenge, solutions.getValue(challenge.id)),
            )
        }
    }

    @Test
    fun selection_must_match_both_amount_and_note_count() {
        val challenge = ChangeMoneyChallenges.first()

        assertEquals(
            ChangeMoneyValidation.WrongNoteCount,
            validateChangeMoneySelection(challenge, mapOf(100 to 1)),
        )
        assertEquals(
            ChangeMoneyValidation.WrongAmount,
            validateChangeMoneySelection(challenge, mapOf(10 to 2)),
        )
    }

    @Test
    fun challenge_restrictions_are_validated_after_total_and_count() {
        assertEquals(
            ChangeMoneyValidation.ForbiddenDenomination,
            validateChangeMoneySelection(
                ChangeMoneyChallenge(
                    id = 11,
                    amount = 200,
                    requiredNotes = 2,
                    forbiddenDenominations = setOf(100),
                ),
                mapOf(100 to 2),
            ),
        )
        assertEquals(
            ChangeMoneyValidation.DenominationTooSmall,
            validateChangeMoneySelection(
                ChangeMoneyChallenge(
                    id = 12,
                    amount = 100,
                    requiredNotes = 2,
                    minimumDenomination = 50,
                ),
                mapOf(10 to 1, 90 to 1),
            ),
        )
        assertEquals(
            ChangeMoneyValidation.DenominationTooLarge,
            validateChangeMoneySelection(
                ChangeMoneyChallenge(
                    id = 13,
                    amount = 1_000,
                    requiredNotes = 2,
                    maximumDenomination = 100,
                ),
                mapOf(500 to 2),
            ),
        )
    }
}
