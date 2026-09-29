package ru.finni.financialpetgame.lct.ui

import ru.finni.financialpetgame.lct.R
import ru.finni.financialpetgame.lct.data.GameSnapshot
import ru.finni.financialpetgame.lct.domain.moneyAfterCompletedCycles
import ru.finni.financialpetgame.lct.domain.moneyAfterPurchase
import ru.finni.financialpetgame.lct.domain.levelAfterGoalPurchase
import ru.finni.financialpetgame.lct.domain.cooldownUnlockCycle
import ru.finni.financialpetgame.lct.domain.cooldownSecondsRemaining
import ru.finni.financialpetgame.lct.domain.cooldownRemaining
import ru.finni.financialpetgame.lct.domain.currentCyclePosition
import ru.finni.financialpetgame.lct.domain.Cooldown
import ru.finni.financialpetgame.lct.domain.CooldownRemaining
import ru.finni.financialpetgame.lct.domain.Effect
import ru.finni.financialpetgame.lct.domain.GameDefaults
import ru.finni.financialpetgame.lct.domain.GameEvent
import ru.finni.financialpetgame.lct.domain.Resource
import ru.finni.financialpetgame.lct.domain.ShopItem
import ru.finni.financialpetgame.lct.domain.TaskItem
import ru.finni.financialpetgame.lct.domain.Goals
import ru.finni.financialpetgame.lct.domain.HairColour
import ru.finni.financialpetgame.lct.domain.HairStyle
import ru.finni.financialpetgame.lct.domain.canApplyEffects
import ru.finni.financialpetgame.lct.domain.canDeclineEvent
import ru.finni.financialpetgame.lct.domain.characteristicValue
import ru.finni.financialpetgame.lct.domain.cycleSecondsRemaining
import ru.finni.financialpetgame.lct.domain.hasCycleExpired
import ru.finni.financialpetgame.lct.domain.nextCycleEnd
import ru.finni.financialpetgame.lct.domain.migratedCycleEnd
import ru.finni.financialpetgame.lct.domain.isAvailable
import ru.finni.financialpetgame.lct.domain.MandatoryBudget
import ru.finni.financialpetgame.lct.domain.budgetPeriodResult
import ru.finni.financialpetgame.lct.domain.needsBudgetPlanning
import ru.finni.financialpetgame.lct.domain.needsBudgetReview
import ru.finni.financialpetgame.lct.domain.events.EventCatalogData
import ru.finni.financialpetgame.lct.domain.events.EventDef
import ru.finni.financialpetgame.lct.domain.events.EventDeltas
import ru.finni.financialpetgame.lct.domain.events.EventFrequency
import ru.finni.financialpetgame.lct.domain.events.EventKind
import ru.finni.financialpetgame.lct.domain.events.EventScheduler
import ru.finni.financialpetgame.lct.domain.events.ScenarioStep
import ru.finni.financialpetgame.lct.domain.events.ScheduledEvent
import ru.finni.financialpetgame.lct.domain.events.canResolveScheduledEvent
import ru.finni.financialpetgame.lct.domain.events.eventTriggerSecond
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.roundToInt

class PetViewModelTest {
    @Test
    fun pet_images_cover_every_appearance_and_use_level_three_above_level_three() {
        val images = buildSet {
            (1..3).forEach { level ->
                HairColour.entries.forEach { colour ->
                    HairStyle.entries.forEach { style ->
                        add(petImageResource(level, colour, style))
                    }
                }
            }
        }

        assertEquals(27, images.size)
        assertEquals(
            petImageResource(3, HairColour.Orange, HairStyle.Curly),
            petImageResource(10, HairColour.Orange, HairStyle.Curly),
        )
    }

    @Test
    fun characteristics_decrease_by_fifty_smoothly_during_cycle() {
        val duration = GameDefaults.CYCLE_DURATION_SECONDS

        assertEquals(60, characteristicValue(60, 0, 0, duration, duration))
        assertEquals(35, characteristicValue(60, 0, 0, duration / 2, duration))
        assertEquals(10, characteristicValue(60, 0, 0, 0, duration))
    }

    @Test
    fun permanent_modifier_changes_each_cycle_starting_value() {
        val duration = GameDefaults.CYCLE_DURATION_SECONDS

        assertEquals(70, characteristicValue(60, 10, 0, duration, duration))
        assertEquals(20, characteristicValue(60, 10, 0, 0, duration))
    }

    @Test
    fun three_cycle_events_are_distributed_by_cycle_fractions() {
        val duration = GameDefaults.CYCLE_DURATION_SECONDS

        assertEquals((duration * 0.08).roundToInt(), eventTriggerSecond(0, 3, duration))
        assertEquals((duration * 0.50).roundToInt(), eventTriggerSecond(1, 3, duration))
        assertEquals((duration * 0.92).roundToInt(), eventTriggerSecond(2, 3, duration))
    }

    @Test
    fun one_cycle_event_is_scheduled_in_the_middle() {
        assertEquals(
            (GameDefaults.CYCLE_DURATION_SECONDS * 0.5).roundToInt(),
            eventTriggerSecond(index = 0, eventCount = 1),
        )
    }

    @Test
    fun demo_mode_makes_all_events_in_the_current_cycle_due_immediately() {
        assertEquals(
            GameDefaults.CYCLE_DURATION_SECONDS,
            eventElapsedCycleSeconds(
                remainingSeconds = GameDefaults.CYCLE_DURATION_SECONDS,
                isTestMode = true,
            ),
        )
        assertEquals(
            0,
            eventElapsedCycleSeconds(
                remainingSeconds = GameDefaults.CYCLE_DURATION_SECONDS,
                isTestMode = false,
            ),
        )
    }

    @Test
    fun event_slots_scale_as_cycle_parts_when_duration_changes() {
        val doubledCycle = 26 * 60

        assertEquals((doubledCycle * 0.08).roundToInt(), eventTriggerSecond(0, 3, doubledCycle))
        assertEquals((doubledCycle * 0.50).roundToInt(), eventTriggerSecond(1, 3, doubledCycle))
        assertEquals((doubledCycle * 0.92).roundToInt(), eventTriggerSecond(2, 3, doubledCycle))
    }

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
    fun immediate_tutorial_event_is_available_at_cycle_start() {
        val event = EventDef(
            id = "login_bonus",
            title = "Login bonus",
            kind = EventKind.Training,
            deltas = EventDeltas(money = 10),
            canDecline = false,
            frequency = EventFrequency.Once,
            minLevel = 1,
            requiresFlags = emptySet(),
            setsFlags = emptySet(),
            unlocksJobId = null,
            hideRewardUntilAccept = false,
            moneyFromCard = 10,
            moneyFromScript = null,
        )
        val scheduler = EventScheduler(
            EventCatalogData(
                events = listOf(event),
                jobs = emptyList(),
                scenario = listOf(
                    ScenarioStep(
                        id = "w1d1_login_bonus",
                        week = 1,
                        day = 1,
                        eventId = event.id,
                        jobActionId = null,
                        moneyOverride = null,
                        requiresFlags = emptySet(),
                        minBalanceExclusive = null,
                        triggerImmediately = true,
                    ),
                ),
            ),
        )

        val scheduled = scheduler.next(
            snapshot = GameSnapshot(onboardingCompleted = true, currentPeriod = 1),
            elapsedCycleSeconds = 0,
        )

        assertEquals("w1d1_login_bonus", scheduled?.scenarioStepId)
        assertEquals(10, scheduled?.appliedDeltas?.money)
    }

    @Test
    fun scheduler_restarts_script_after_game_event_progress_is_reset() {
        val event = EventDef(
            id = "story",
            title = "Story",
            kind = EventKind.Story,
            deltas = EventDeltas(),
            canDecline = true,
            frequency = EventFrequency.Once,
            minLevel = 1,
            requiresFlags = emptySet(),
            setsFlags = emptySet(),
            unlocksJobId = null,
            hideRewardUntilAccept = false,
            moneyFromCard = 0,
            moneyFromScript = null,
            poolEligible = false,
        )
        val step = ScenarioStep(
            id = "w1d1_story",
            week = 1,
            day = 1,
            eventId = event.id,
            jobActionId = null,
            moneyOverride = null,
            requiresFlags = emptySet(),
            minBalanceExclusive = null,
        )
        val scheduler = EventScheduler(
            EventCatalogData(listOf(event), emptyList(), listOf(step)),
        )
        val completedGame = GameSnapshot(
            onboardingCompleted = true,
            currentPeriod = 1,
            budgetPlanWeek = 1,
            completedEventIds = setOf(event.id),
            handledScenarioEntryIds = setOf(step.id),
        )

        assertEquals(null, scheduler.next(completedGame))

        val restartedGame = completedGame.copy(
            completedEventIds = emptySet(),
            eventPeriodOccurrences = emptySet(),
            handledScenarioEntryIds = emptySet(),
            eventPoolHandledCycles = emptySet(),
        )

        assertEquals(step.id, scheduler.next(restartedGame)?.scenarioStepId)
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
        assertEquals("12:00", formatCountdown(TIMER_SECONDS))
        assertEquals("00:00", formatCountdown(0))
    }

    @Test
    fun cycle_time_is_rounded_up_to_minutes() {
        assertEquals(12, roundedMinutesRemaining(TIMER_SECONDS))
        assertEquals(12, roundedMinutesRemaining(TIMER_SECONDS - 1))
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
            muchLater + GameDefaults.CYCLE_DURATION_MILLIS,
            nextCycleEnd(muchLater),
        )
    }

    @Test
    fun active_cycle_progress_is_preserved_when_duration_changes() {
        val now = 1_000L
        assertEquals(
            now + 360_000L,
            migratedCycleEnd(
                cycleEndsAtMillis = now + 600_000L,
                nowMillis = now,
                previousDurationSeconds = 1_200,
                currentDurationSeconds = 720,
            ),
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
            CooldownRemaining.Time(
                (GameDefaults.CYCLE_DURATION_SECONDS * 0.3).roundToInt(),
            ),
            cooldownRemaining(
                cooldown = Cooldown.Cycles(0.3),
                unlockCycle = 1.3,
                currentPeriod = 1,
                cycleSecondsRemaining = GameDefaults.CYCLE_DURATION_SECONDS,
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
    fun purchased_current_goal_requires_selecting_a_new_goal_after_state_restoration() {
        val purchasedCurrentGoal = GameSnapshot(
            goalId = Goals.Pillow.name,
            purchasedGoalIds = setOf(Goals.Pillow.name),
            onboardingCompleted = true,
        )

        assertEquals(true, needsGoalSelection(purchasedCurrentGoal))
        assertEquals(
            false,
            needsGoalSelection(purchasedCurrentGoal.copy(goalId = Goals.Ball.name)),
        )
        assertEquals(
            false,
            needsGoalSelection(purchasedCurrentGoal.copy(onboardingCompleted = false)),
        )
    }

    @Test
    fun buying_goal_of_current_level_increases_character_level() {
        assertEquals(2, levelAfterGoalPurchase(currentLevel = 1, goalLevel = 1))
        assertEquals(3, levelAfterGoalPurchase(currentLevel = 3, goalLevel = 2))
    }
}
