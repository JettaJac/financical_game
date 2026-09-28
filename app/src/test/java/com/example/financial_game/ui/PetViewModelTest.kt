package com.example.financial_game.ui

import com.example.financial_game.R
import com.example.financial_game.data.GameSnapshot
import com.example.financial_game.domain.moneyAfterCompletedCycles
import com.example.financial_game.domain.moneyAfterPurchase
import com.example.financial_game.domain.levelAfterGoalPurchase
import com.example.financial_game.domain.cooldownUnlockCycle
import com.example.financial_game.domain.cooldownSecondsRemaining
import com.example.financial_game.domain.cooldownRemaining
import com.example.financial_game.domain.currentCyclePosition
import com.example.financial_game.domain.Cooldown
import com.example.financial_game.domain.CooldownRemaining
import com.example.financial_game.domain.Effect
import com.example.financial_game.domain.GameDefaults
import com.example.financial_game.domain.GameEvent
import com.example.financial_game.domain.Resource
import com.example.financial_game.domain.ShopItem
import com.example.financial_game.domain.TaskItem
import com.example.financial_game.domain.Goals
import com.example.financial_game.domain.canApplyEffects
import com.example.financial_game.domain.canDeclineEvent
import com.example.financial_game.domain.cycleSecondsRemaining
import com.example.financial_game.domain.hasCycleExpired
import com.example.financial_game.domain.nextCycleEnd
import com.example.financial_game.domain.isAvailable
import com.example.financial_game.domain.MandatoryBudget
import com.example.financial_game.domain.budgetPeriodResult
import com.example.financial_game.domain.needsBudgetPlanning
import com.example.financial_game.domain.needsBudgetReview
import com.example.financial_game.domain.events.EventCatalogData
import com.example.financial_game.domain.events.EventDef
import com.example.financial_game.domain.events.EventDeltas
import com.example.financial_game.domain.events.EventFrequency
import com.example.financial_game.domain.events.EventKind
import com.example.financial_game.domain.events.EventScheduler
import com.example.financial_game.domain.events.ScenarioStep
import com.example.financial_game.domain.events.ScheduledEvent
import com.example.financial_game.domain.events.canResolveScheduledEvent
import org.junit.Assert.assertEquals
import org.junit.Test

class PetViewModelTest {
    @Test
    fun unaffordable_event_cannot_be_accepted_and_can_always_be_declined() {
        val forcedExpense = GameEvent(
            descriptionRes = R.string.app_name,
            illustrationRes = R.drawable.goal_pillow,
            acceptEffects = listOf(Effect(Resource.Money, -50)),
            canDecline = false,
        )

        assertEquals(false, canApplyEffects(money = 40, forcedExpense.acceptEffects))
        assertEquals(true, canDeclineEvent(money = 40, forcedExpense))
        assertEquals(false, canDeclineEvent(money = 50, forcedExpense))
        assertEquals(
            true,
            canDeclineEvent(money = 50, forcedExpense.copy(canDecline = true)),
        )
    }

    @Test
    fun charity_can_be_declined_without_charging_money() {
        val charity = ScheduledEvent(
            definition = EventDef(
                id = "charity",
                title = "Charity",
                kind = EventKind.Positive,
                deltas = EventDeltas(money = -50),
                canDecline = true,
                frequency = EventFrequency.Once,
                minLevel = 1,
                requiresFlags = emptySet(),
                setsFlags = setOf("charityAccepted"),
                unlocksJobId = null,
                hideRewardUntilAccept = false,
                moneyFromCard = -50,
                moneyFromScript = null,
            ),
            scenarioStepId = "w2d1_charity",
            isScripted = true,
            appliedDeltas = EventDeltas(money = -50),
        )

        assertEquals(true, canResolveScheduledEvent(charity, accepted = false, currentMoney = 0))
        assertEquals(false, canResolveScheduledEvent(charity, accepted = true, currentMoney = 0))
        assertEquals(true, canResolveScheduledEvent(charity, accepted = true, currentMoney = 50))
    }

    @Test
    fun scheduler_prioritizes_script_and_uses_script_money_override() {
        val event = EventDef(
            id = "training",
            title = "Training",
            kind = EventKind.Training,
            deltas = EventDeltas(money = 20),
            canDecline = true,
            frequency = EventFrequency.Once,
            minLevel = 1,
            requiresFlags = emptySet(),
            setsFlags = setOf("trained"),
            unlocksJobId = null,
            hideRewardUntilAccept = false,
            moneyFromCard = 20,
            moneyFromScript = -20,
            poolEligible = true,
        )
        val scheduler = EventScheduler(
            EventCatalogData(
                events = listOf(event),
                jobs = emptyList(),
                scenario = listOf(
                    ScenarioStep(
                        id = "w1d2_training",
                        week = 1,
                        day = 2,
                        eventId = event.id,
                        jobActionId = null,
                        moneyOverride = null,
                        requiresFlags = emptySet(),
                        minBalanceExclusive = null,
                    ),
                ),
            ),
        )

        val scheduled = scheduler.next(
            GameSnapshot(
                onboardingCompleted = true,
                currentPeriod = 2,
                budgetPlanWeek = 1,
            ),
        )

        assertEquals("w1d2_training", scheduled?.scenarioStepId)
        assertEquals(-20, scheduled?.appliedDeltas?.money)
        assertEquals(true, scheduled?.isScripted)
    }

    @Test
    fun scheduler_does_not_repeat_pool_event_in_the_same_cycle() {
        val poolEvent = EventDef(
            id = "weather",
            title = "Weather",
            kind = EventKind.Positive,
            deltas = EventDeltas(happiness = 10),
            canDecline = false,
            frequency = EventFrequency.PerPeriod,
            minLevel = 1,
            requiresFlags = emptySet(),
            setsFlags = emptySet(),
            unlocksJobId = null,
            hideRewardUntilAccept = false,
            moneyFromCard = 0,
            moneyFromScript = null,
            poolEligible = true,
        )
        val scheduler = EventScheduler(EventCatalogData(listOf(poolEvent), emptyList(), emptyList()))
        val snapshot = GameSnapshot(
            onboardingCompleted = true,
            currentPeriod = 16,
            budgetPlanWeek = 3,
        )

        assertEquals("weather", scheduler.next(snapshot)?.definition?.id)
        assertEquals(
            null,
            scheduler.next(snapshot.copy(eventPoolHandledCycles = setOf("16"))),
        )
    }

    @Test
    fun completed_budget_week_compares_plan_with_actual_values() {
        val successfulSnapshot = GameSnapshot(
            onboardingCompleted = true,
            currentPeriod = 8,
            budgetPlanWeek = 1,
            plannedOptionalExpenses = 20,
            actualOptionalExpenses = 15,
            actualAdditionalIncome = 25,
        )

        assertEquals(true, needsBudgetReview(successfulSnapshot))
        assertEquals(true, budgetPeriodResult(successfulSnapshot).isSuccessful)
        assertEquals(110, budgetPeriodResult(successfulSnapshot).actualIncome)
        assertEquals(70, budgetPeriodResult(successfulSnapshot).actualExpenses)
        assertEquals(
            false,
            budgetPeriodResult(
                successfulSnapshot.copy(actualOptionalExpenses = 21),
            ).isSuccessful,
        )
        assertEquals(
            false,
            needsBudgetReview(successfulSnapshot.copy(lastReviewedBudgetWeek = 1)),
        )
    }

    @Test
    fun budget_planning_is_required_at_the_start_of_each_week() {
        assertEquals(85, MandatoryBudget.income)
        assertEquals(55, MandatoryBudget.expense)
        assertEquals(30, MandatoryBudget.maximumOptionalExpense)
        assertEquals(
            true,
            needsBudgetPlanning(
                GameSnapshot(
                    onboardingCompleted = true,
                    currentPeriod = 1,
                    budgetPlanWeek = 0,
                ),
            ),
        )
        assertEquals(
            false,
            needsBudgetPlanning(
                GameSnapshot(
                    onboardingCompleted = true,
                    currentPeriod = 1,
                    budgetPlanWeek = 1,
                ),
            ),
        )
        assertEquals(
            true,
            needsBudgetPlanning(
                GameSnapshot(
                    onboardingCompleted = true,
                    currentPeriod = 8,
                    budgetPlanWeek = 1,
                    budgetTutorialCompleted = true,
                ),
            ),
        )
    }

    @Test
    fun task_items_keep_product_order() {
        assertEquals(
            listOf(
                TaskItem.GetReady,
                TaskItem.Lessons,
                TaskItem.Cleaning,
                TaskItem.BeadCrafts,
                TaskItem.DeliverNewspapers,
                TaskItem.HandOutFlyers,
                TaskItem.RecyclePaper,
                TaskItem.FeedNeighboursCat,
                TaskItem.WalkNeighboursDog,
                TaskItem.HelpGrandfather,
                TaskItem.WaterPlants,
            ),
            TaskItem.entries,
        )
    }

    @Test
    fun task_cycle_and_usage_requirements_are_applied() {
        assertEquals(
            true,
            TaskItem.Lessons.isAvailable(5, 0, 0, -1, emptyMap(), false),
        )
        assertEquals(
            false,
            TaskItem.Lessons.isAvailable(6, 0, 0, -1, emptyMap(), false),
        )
        assertEquals(
            true,
            TaskItem.Cleaning.isAvailable(6, 0, 0, -1, emptyMap(), false),
        )
        assertEquals(
            false,
            TaskItem.BeadCrafts.isAvailable(
                currentPeriod = 1,
                totalUses = 3,
                weeklyUses = 3,
                usageWeek = 0,
                completedTaskCounts = mapOf(TaskItem.GetReady.storageId to 1),
                eventUnlocked = false,
            ),
        )
        assertEquals(
            false,
            TaskItem.DeliverNewspapers.isAvailable(2, 0, 0, -1, emptyMap(), false),
        )
        assertEquals(
            true,
            TaskItem.DeliverNewspapers.isAvailable(2, 0, 0, -1, emptyMap(), true),
        )
    }

    @Test
    fun every_task_has_one_cycle_cooldown() {
        TaskItem.entries.forEach { task ->
            assertEquals(Cooldown.Cycles(1.0), task.cooldown)
        }
    }

    @Test
    fun shop_items_match_prices_levels_and_effects() {
        assertEquals(15, ShopItem.FlowerPot.price)
        assertEquals(10, ShopItem.FavouriteMug.price)
        assertEquals(15, ShopItem.FloorLamp.price)
        assertEquals(2, ShopItem.FloorLamp.level)
        assertEquals(15, ShopItem.SoftRug.price)
        assertEquals(25, ShopItem.SoftArmchair.price)
        assertEquals(35, ShopItem.StylishScarf.price)
        assertEquals(3, ShopItem.StylishScarf.level)
        assertEquals(35, ShopItem.GlowingOrb.price)
        assertEquals(ShopItem.StylishScarf.careEffects, ShopItem.GlowingOrb.careEffects)
        assertEquals(Cooldown.None, ShopItem.GlowingOrb.cooldown)
    }

    @Test
    fun countdown_is_formatted_as_minutes_and_seconds() {
        assertEquals("20:00", formatCountdown(TIMER_SECONDS))
        assertEquals("00:00", formatCountdown(0))
    }

    @Test
    fun cycle_time_is_rounded_up_to_minutes() {
        assertEquals(20, roundedMinutesRemaining(1_200))
        assertEquals(20, roundedMinutesRemaining(1_199))
        assertEquals(1, roundedMinutesRemaining(60))
        assertEquals(1, roundedMinutesRemaining(1))
        assertEquals(0, roundedMinutesRemaining(0))
    }

    @Test
    fun recurring_income_and_expenses_are_applied_only_at_week_end() {
        assertEquals(85, GameDefaults.POCKET_MONEY_INCOME)
        assertEquals(30, GameDefaults.SCHOOL_LUNCH_EXPENSE)
        assertEquals(10, GameDefaults.MOBILE_SERVICE_EXPENSE)
        assertEquals(5, GameDefaults.VITAMINS_EXPENSE)
        assertEquals(10, GameDefaults.SPORTS_SECTION_EXPENSE)
        assertEquals(55, GameDefaults.EXPENSE)

        assertEquals(
            100,
            moneyAfterCompletedCycles(
                money = 100,
                income = 85,
                expense = 55,
                currentPeriod = 1,
                completedCycles = 6,
            ),
        )
        assertEquals(
            130,
            moneyAfterCompletedCycles(
                money = 100,
                income = 85,
                expense = 55,
                currentPeriod = 7,
                completedCycles = 1,
            ),
        )
        assertEquals(
            160,
            moneyAfterCompletedCycles(
                money = 100,
                income = 85,
                expense = 55,
                currentPeriod = 1,
                completedCycles = 14,
            ),
        )
    }

    @Test
    fun persisted_cycle_restores_remaining_time() {
        assertEquals(
            61,
            cycleSecondsRemaining(cycleEndsAtMillis = 62_000L, nowMillis = 1_000L),
        )
        assertEquals(
            0,
            cycleSecondsRemaining(cycleEndsAtMillis = 1_000L, nowMillis = 1_000L),
        )
    }

    @Test
    fun expired_deadline_completes_only_one_cycle_and_starts_a_fresh_one() {
        val oldDeadline = 10_000L
        val muchLater = oldDeadline + 10_000_000L

        assertEquals(true, hasCycleExpired(oldDeadline, muchLater))
        assertEquals(
            muchLater + 1_200_000L,
            nextCycleEnd(muchLater),
        )
    }

    @Test
    fun purchase_only_charges_when_money_is_enough() {
        assertEquals(0, moneyAfterPurchase(money = 100, price = 100))
        assertEquals(99, moneyAfterPurchase(money = 99, price = 100))
    }

    @Test
    fun cooldown_unlock_is_calculated_in_cycles() {
        assertEquals(4.3, cooldownUnlockCycle(currentCyclePosition = 4.0, cooldownCycles = 0.3), 0.0)
    }

    @Test
    fun cooldown_seconds_scale_with_cycle_duration() {
        assertEquals(
            360,
            cooldownSecondsRemaining(
                unlockCycle = 4.3,
                currentCyclePosition = 4.0,
                cycleDurationSeconds = 1_200,
            ),
        )
        assertEquals(
            180,
            cooldownSecondsRemaining(
                unlockCycle = 4.3,
                currentCyclePosition = 4.0,
                cycleDurationSeconds = 600,
            ),
        )
    }

    @Test
    fun current_cycle_position_includes_elapsed_fraction() {
        assertEquals(
            4.5,
            currentCyclePosition(
                currentPeriod = 4,
                cycleSecondsRemaining = 600,
                cycleDurationSeconds = 1_200,
            ),
            0.0,
        )
    }

    @Test
    fun cooldown_remaining_is_shown_in_seconds() {
        assertEquals(
            CooldownRemaining.Time(360),
            cooldownRemaining(
                cooldown = Cooldown.Cycles(0.3),
                unlockCycle = 1.3,
                currentPeriod = 1,
                cycleSecondsRemaining = 1_200,
            ),
        )
    }

    @Test
    fun event_with_money_cost_cannot_be_accepted_without_enough_money() {
        val effects = listOf(Effect(Resource.Money, -30), Effect(Resource.Energy, 10))

        assertEquals(false, canApplyEffects(money = 29, effects = effects))
        assertEquals(true, canApplyEffects(money = 30, effects = effects))
    }

    @Test
    fun reaching_goal_creates_purchase_event_with_current_goal_data() {
        val resources = GameSnapshot(
            goalId = Goals.Ball.name,
            goalTarget = 100,
            money = 100,
            onboardingCompleted = true,
        )

        val event = goalPurchaseEvent(resources)

        assertEquals(Goals.Ball.titleRes, event.descriptionRes)
        assertEquals(Goals.Ball.illustrationRes, event.illustrationRes)
        assertEquals(Goals.Ball.goalEffects + Effect(Resource.Money, -100), event.acceptEffects)
        assertEquals(R.string.buy, event.acceptButtonTextRes)
        assertEquals(true, event.showCloseButton)
        assertEquals(Goals.Ball, event.goalPurchase?.goal)
        assertEquals(100, event.goalPurchase?.price)
    }

    @Test
    fun goal_event_is_emitted_only_when_threshold_is_crossed() {
        val before = GameSnapshot(
            goalId = Goals.Pillow.name,
            goalTarget = 100,
            money = 99,
            onboardingCompleted = true,
        )
        val reached = before.copy(money = 100)

        assertEquals(true, hasJustReachedGoal(before, reached))
        assertEquals(false, hasJustReachedGoal(reached, reached.copy(money = 120)))
        assertEquals(
            false,
            hasJustReachedGoal(
                before,
                reached.copy(purchasedGoalIds = setOf(Goals.Pillow.name)),
            ),
        )
    }

    @Test
    fun buying_goal_of_current_level_increases_character_level() {
        assertEquals(2, levelAfterGoalPurchase(currentLevel = 1, goalLevel = 1))
        assertEquals(3, levelAfterGoalPurchase(currentLevel = 3, goalLevel = 2))
    }
}
