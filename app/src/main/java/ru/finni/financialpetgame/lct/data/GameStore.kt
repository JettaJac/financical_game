package ru.finni.financialpetgame.lct.data

import android.content.Context
import ru.finni.financialpetgame.lct.R
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import ru.finni.financialpetgame.lct.domain.GameDefaults
import ru.finni.financialpetgame.lct.domain.GameRepository
import ru.finni.financialpetgame.lct.domain.CardItem
import ru.finni.financialpetgame.lct.domain.Cooldown
import ru.finni.financialpetgame.lct.domain.EyeColour
import ru.finni.financialpetgame.lct.domain.Effect
import ru.finni.financialpetgame.lct.domain.EnergyItem
import ru.finni.financialpetgame.lct.domain.FoodItem
import ru.finni.financialpetgame.lct.domain.Goals
import ru.finni.financialpetgame.lct.domain.HairColour
import ru.finni.financialpetgame.lct.domain.HairStyle
import ru.finni.financialpetgame.lct.domain.HappinessItem
import ru.finni.financialpetgame.lct.domain.PetAppearance
import ru.finni.financialpetgame.lct.domain.PET_APPEARANCE_CHANGE_PRICE
import ru.finni.financialpetgame.lct.domain.PET_TAP_HAPPINESS_REWARD
import ru.finni.financialpetgame.lct.domain.PetSetup
import ru.finni.financialpetgame.lct.domain.Resource
import ru.finni.financialpetgame.lct.domain.ShopItem
import ru.finni.financialpetgame.lct.domain.TaskItem
import ru.finni.financialpetgame.lct.domain.cooldownUnlockCycle
import ru.finni.financialpetgame.lct.domain.currentCyclePosition
import ru.finni.financialpetgame.lct.domain.characteristicValue
import ru.finni.financialpetgame.lct.domain.characteristicCycleAdjustmentForValue
import ru.finni.financialpetgame.lct.domain.cycleSecondsRemaining
import ru.finni.financialpetgame.lct.domain.hasCycleExpired
import ru.finni.financialpetgame.lct.domain.canCompleteCycle
import ru.finni.financialpetgame.lct.domain.canApplyEffects
import ru.finni.financialpetgame.lct.domain.canRewardPetTap
import ru.finni.financialpetgame.lct.domain.nextCycleEnd
import ru.finni.financialpetgame.lct.domain.levelAfterGoalPurchase
import ru.finni.financialpetgame.lct.domain.moneyAfterPurchase
import ru.finni.financialpetgame.lct.domain.moneyAfterCompletedCycles
import ru.finni.financialpetgame.lct.domain.roomDirtAfterCompletedCycles
import ru.finni.financialpetgame.lct.domain.migratedCycleEnd
import ru.finni.financialpetgame.lct.domain.isAvailable
import ru.finni.financialpetgame.lct.domain.isUnlockedAtPeriod
import ru.finni.financialpetgame.lct.domain.weekForPeriod
import ru.finni.financialpetgame.lct.domain.withCharacteristicsAt
import ru.finni.financialpetgame.lct.domain.workPreparationBlock
import ru.finni.financialpetgame.lct.domain.budgetWeekForPeriod
import ru.finni.financialpetgame.lct.domain.dayForPeriod
import ru.finni.financialpetgame.lct.domain.MandatoryBudget
import ru.finni.financialpetgame.lct.domain.MiniGameStartResult
import ru.finni.financialpetgame.lct.domain.ChangeMoneyStartResult
import ru.finni.financialpetgame.lct.domain.nextChangeMoneyChallenge
import ru.finni.financialpetgame.lct.domain.BudgetPeriodResult
import ru.finni.financialpetgame.lct.domain.ActiveDeposit
import ru.finni.financialpetgame.lct.domain.DepositTerm
import ru.finni.financialpetgame.lct.domain.events.EventFrequency
import ru.finni.financialpetgame.lct.domain.events.EventKind
import ru.finni.financialpetgame.lct.domain.events.EventResolutionResult
import ru.finni.financialpetgame.lct.domain.events.JobDef
import ru.finni.financialpetgame.lct.domain.events.ScheduledEvent
import ru.finni.financialpetgame.lct.domain.events.canResolveScheduledEvent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.gameDataStore by preferencesDataStore("game")

private const val COOLDOWN_UNLOCK_CYCLE_PREFIX = "cooldown_unlock_cycle_"
private const val LEGACY_COOLDOWN_EXPIRE_PREFIX = "cooldown_expire_"
private const val LEGACY_COOLDOWN_PERIOD_PREFIX = "cooldown_period_"
private const val TASK_USE_COUNT_PREFIX = "task_use_count_"
private const val TASK_WEEKLY_USE_COUNT_PREFIX = "task_weekly_use_count_"
private const val TASK_USE_WEEK_PREFIX = "task_use_week_"
private const val JOB_REMAINING_ACTIONS_PREFIX = "job_remaining_actions_"
private const val JOB_FOR_TASK_PREFIX = "job_for_task_"
private const val JOB_PAYOUT_PREFIX = "job_payout_"
private const val JOB_HEALTH_PREFIX = "job_health_"
private const val JOB_HAPPINESS_PREFIX = "job_happiness_"
private const val JOB_ENERGY_PREFIX = "job_energy_"
private const val JOB_ALLOWED_CYCLES_PREFIX = "job_allowed_cycles_"
private const val JOB_PERIOD_LIMIT_PREFIX = "job_period_limit_"
private const val EVENT_RETRY_PERIOD_PREFIX = "event_retry_period_"
private const val CHANGE_MONEY_BEST_TIME_PREFIX = "change_money_best_time_"
private const val NEXT_PURCHASE_DISCOUNT_FLAG = "nextPurchaseHalfPrice"
private const val BRAIDED_BRACELETS_FLAG = "braidedBracelets"
private const val LEGACY_CYCLE_DURATION_SECONDS = 20 * 60
private const val MAX_ACTION_HISTORY_SIZE = 500
private const val CURRENT_BUDGET_ONBOARDING_VERSION = 3

private fun permanentBonus(
    resource: Resource,
    purchasedShopItemIds: Set<String>,
    purchasedGoalIds: Set<String>,
): Int = ShopItem.entries
    .filter { it.storageId in purchasedShopItemIds }
    .flatMap(CardItem::careEffects)
    .filter { it.resource == resource }
    .sumOf(Effect::increase) + Goals.entries
    .filter { it.name in purchasedGoalIds }
    .flatMap(Goals::goalEffects)
    .filter { it.resource == resource }
    .sumOf(Effect::increase)

private fun decodeActionHistory(raw: String?): List<GameActionRecord> = runCatching {
    val array = JSONArray(raw ?: "[]")
    List(array.length()) { index ->
        val item = array.getJSONObject(index)
        GameActionRecord(
            timestamp = item.getLong("timestamp"),
            cycle = item.getInt("cycle"),
            description = item.getString("description"),
            moneyDelta = item.optInt("moneyDelta"),
        )
    }
}.getOrDefault(emptyList())

private fun appendAction(
    preferences: MutablePreferences,
    description: String,
    moneyDelta: Int = 0,
) {
    val entries = decodeActionHistory(preferences[stringPreferencesKey("action_history")])
        .takeLast(MAX_ACTION_HISTORY_SIZE - 1) + GameActionRecord(
        timestamp = System.currentTimeMillis(),
        cycle = preferences[intPreferencesKey("current_period")] ?: 1,
        description = description,
        moneyDelta = moneyDelta,
    )
    preferences[stringPreferencesKey("action_history")] = JSONArray().apply {
        entries.forEach { entry ->
            put(JSONObject().apply {
                put("timestamp", entry.timestamp)
                put("cycle", entry.cycle)
                put("description", entry.description)
                put("moneyDelta", entry.moneyDelta)
            })
        }
    }.toString()
}

private fun decodeBudgetExpenseRecords(raw: String?): List<BudgetExpenseRecord> = runCatching {
    val array = JSONArray(raw ?: "[]")
    List(array.length()) { index ->
        val item = array.getJSONObject(index)
        BudgetExpenseRecord(
            title = item.getString("title"),
            amount = item.getInt("amount"),
        )
    }
}.getOrDefault(emptyList())

private fun encodeBudgetExpenseRecords(records: List<BudgetExpenseRecord>): String =
    JSONArray().apply {
        records.forEach { record ->
            put(JSONObject().apply {
                put("title", record.title)
                put("amount", record.amount)
            })
        }
    }.toString()

private fun cooldownUnlockCycleKey(storageId: String) =
    doublePreferencesKey("$COOLDOWN_UNLOCK_CYCLE_PREFIX$storageId")

private fun cooldownUnlockCycleKey(item: CardItem) = cooldownUnlockCycleKey(item.storageId)

private fun taskUseCountKey(item: TaskItem) =
    intPreferencesKey("$TASK_USE_COUNT_PREFIX${item.storageId}")

private fun taskWeeklyUseCountKey(item: TaskItem) =
    intPreferencesKey("$TASK_WEEKLY_USE_COUNT_PREFIX${item.storageId}")

private fun taskUseWeekKey(item: TaskItem) =
    intPreferencesKey("$TASK_USE_WEEK_PREFIX${item.storageId}")

private fun jobRemainingActionsKey(jobId: String) =
    intPreferencesKey("$JOB_REMAINING_ACTIONS_PREFIX$jobId")

private fun jobForTaskKey(taskStorageId: String) =
    stringPreferencesKey("$JOB_FOR_TASK_PREFIX$taskStorageId")

private fun jobPayoutKey(jobId: String) = intPreferencesKey("$JOB_PAYOUT_PREFIX$jobId")
private fun jobHealthKey(jobId: String) = intPreferencesKey("$JOB_HEALTH_PREFIX$jobId")
private fun jobHappinessKey(jobId: String) = intPreferencesKey("$JOB_HAPPINESS_PREFIX$jobId")
private fun jobEnergyKey(jobId: String) = intPreferencesKey("$JOB_ENERGY_PREFIX$jobId")
private fun jobAllowedCyclesKey(jobId: String) =
    stringPreferencesKey("$JOB_ALLOWED_CYCLES_PREFIX$jobId")
private fun jobPeriodLimitKey(jobId: String) =
    intPreferencesKey("$JOB_PERIOD_LIMIT_PREFIX$jobId")
private fun eventRetryPeriodKey(eventId: String) =
    intPreferencesKey("$EVENT_RETRY_PERIOD_PREFIX$eventId")
private fun changeMoneyBestTimeKey(challengeId: Int) =
    intPreferencesKey("$CHANGE_MONEY_BEST_TIME_PREFIX$challengeId")

@Singleton
class GameStore @Inject constructor(@ApplicationContext private val context: Context) : GameRepository {
    private val petName = stringPreferencesKey("pet_name")
    private val hairColour = stringPreferencesKey("hair_colour")
    private val eyeColour = stringPreferencesKey("eye_colour")
    private val hairStyle = stringPreferencesKey("hair_style")
    private val money = intPreferencesKey("money")
    private val health = intPreferencesKey("health")
    private val happiness = intPreferencesKey("happiness")
    private val energy = intPreferencesKey("energy")
    private val healthModifier = intPreferencesKey("health_modifier")
    private val happinessModifier = intPreferencesKey("happiness_modifier")
    private val energyModifier = intPreferencesKey("energy_modifier")
    private val healthCycleAdjustment = intPreferencesKey("health_cycle_adjustment")
    private val happinessCycleAdjustment = intPreferencesKey("happiness_cycle_adjustment")
    private val energyCycleAdjustment = intPreferencesKey("energy_cycle_adjustment")
    private val rewardedPetTapsThisCycle = intPreferencesKey("rewarded_pet_taps_this_cycle")
    private val goalTitle = stringPreferencesKey("goal_title")
    private val goalTarget = intPreferencesKey("goal_target")
    private val goalId = stringPreferencesKey("goal_id")
    private val purchasedGoalIds = stringSetPreferencesKey("purchased_goal_ids")
    private val purchasedShopItemIds = stringSetPreferencesKey("purchased_shop_item_ids")
    private val income = intPreferencesKey("income")
    private val expense = intPreferencesKey("expense")
    private val level = intPreferencesKey("level")
    private val currentPeriod = intPreferencesKey("current_period")
    private val cycleEndsAtMillis = longPreferencesKey("cycle_ends_at_millis")
    private val cycleDurationSeconds = intPreferencesKey("cycle_duration_seconds")
    private val onboardingCompleted = booleanPreferencesKey("onboarding_completed")
    private val eventUnlockedTaskIds = stringSetPreferencesKey("event_unlocked_task_ids")
    private val budgetPlanWeek = intPreferencesKey("budget_plan_week")
    private val budgetPlanRecurringExpense = intPreferencesKey("budget_plan_recurring_expense")
    private val plannedOptionalExpenses = intPreferencesKey("planned_optional_expenses")
    private val plannedGoalExpenses = intPreferencesKey("planned_goal_expenses")
    private val actualOptionalExpenses = intPreferencesKey("actual_optional_expenses")
    private val optionalExpenseRecords = stringPreferencesKey("optional_expense_records")
    private val actualUnplannedExpenses = intPreferencesKey("actual_unplanned_expenses")
    private val actualGoalExpenses = intPreferencesKey("actual_goal_expenses")
    private val unplannedExpenseRecords = stringPreferencesKey("unplanned_expense_records")
    private val actualAdditionalIncome = intPreferencesKey("actual_additional_income")
    private val lastReviewedBudgetWeek = intPreferencesKey("last_reviewed_budget_week")
    private val budgetTutorialCompleted = booleanPreferencesKey("budget_tutorial_completed")
    private val budgetOnboardingVersion = intPreferencesKey("budget_onboarding_version")
    private val budgetOpeningBalance = intPreferencesKey("budget_opening_balance")
    private val levelTwoBudgetExpensesEnabled =
        booleanPreferencesKey("level_two_budget_expenses_enabled")
    private val depositTutorialCompleted = booleanPreferencesKey("deposit_tutorial_completed")
    private val homeTutorialCompleted = booleanPreferencesKey("home_tutorial_completed")
    private val morningRoutineTutorialCompleted =
        booleanPreferencesKey("morning_routine_tutorial_completed")
    private val budgetMasterTutorialCompleted =
        booleanPreferencesKey("budget_master_tutorial_completed")
    private val budgetMasterBestScore = intPreferencesKey("budget_master_best_score")
    private val changeMoneyTutorialCompleted =
        booleanPreferencesKey("change_money_tutorial_completed")
    private val changeMoneyLastChallengeId = intPreferencesKey("change_money_last_challenge_id")
    private val demoModeEnabled = booleanPreferencesKey("demo_mode_enabled")
    private val roomDirtLevel = intPreferencesKey("room_dirt_level")
    private val eventFlags = stringSetPreferencesKey("event_flags")
    private val completedEventIds = stringSetPreferencesKey("completed_event_ids")
    private val eventPeriodOccurrences = stringSetPreferencesKey("event_period_occurrences")
    private val handledScenarioEntryIds = stringSetPreferencesKey("handled_scenario_entry_ids")
    private val eventPoolHandledCycles = stringSetPreferencesKey("event_pool_handled_cycles")
    private val restedAtPeriod = intPreferencesKey("rested_at_period")
    private val activeJobIds = stringSetPreferencesKey("active_job_ids")
    private val actionHistory = stringPreferencesKey("action_history")
    private val customGoalTitle = stringPreferencesKey("custom_goal_title")
    private val customGoalTarget = intPreferencesKey("custom_goal_target")
    private val customGoalIllustration = intPreferencesKey("custom_goal_illustration")
    private val depositAmount = intPreferencesKey("deposit_amount")
    private val depositOpenedAtPeriod = intPreferencesKey("deposit_opened_at_period")
    private val depositDurationCycles = intPreferencesKey("deposit_duration_cycles")
    private val depositInterestPercent = intPreferencesKey("deposit_interest_percent")
    private val depositPenaltyPercent = intPreferencesKey("deposit_penalty_percent")
    private val pendingExpenseDelta = intPreferencesKey("pending_expense_delta")

    override val snapshot: Flow<GameSnapshot> = flow {
        emitAll(context.gameDataStore.data.map { preferences ->
            val cooldownUnlockCycles = preferences.asMap()
                .mapNotNull { (key, value) ->
                    if (key.name.startsWith(COOLDOWN_UNLOCK_CYCLE_PREFIX) && value is Double) {
                        key.name.removePrefix(COOLDOWN_UNLOCK_CYCLE_PREFIX) to value
                    } else {
                        null
                    }
                }
                .toMap()
            val taskUseCounts = preferences.intMap(TASK_USE_COUNT_PREFIX)
            val taskWeeklyUseCounts = preferences.intMap(TASK_WEEKLY_USE_COUNT_PREFIX)
            val taskUseWeeks = preferences.intMap(TASK_USE_WEEK_PREFIX)
            val jobRemainingActions = preferences.intMap(JOB_REMAINING_ACTIONS_PREFIX)
            val jobForTaskIds = preferences.stringMap(JOB_FOR_TASK_PREFIX)
            val jobEffectsForTaskIds = jobForTaskIds.mapValues { (_, jobId) ->
                listOf(
                    Effect(Resource.Health, preferences[jobHealthKey(jobId)] ?: 0),
                    Effect(Resource.Happiness, preferences[jobHappinessKey(jobId)] ?: 0),
                    Effect(Resource.Energy, preferences[jobEnergyKey(jobId)] ?: 0),
                    Effect(Resource.Money, preferences[jobPayoutKey(jobId)] ?: 0),
                )
            }
            val eventRetryPeriods = preferences.intMap(EVENT_RETRY_PERIOD_PREFIX)
            val changeMoneyBestTimes = preferences.intMap(CHANGE_MONEY_BEST_TIME_PREFIX)

            val secondsRemaining = cycleSecondsRemaining(
                cycleEndsAtMillis = checkNotNull(preferences[cycleEndsAtMillis]),
                nowMillis = System.currentTimeMillis(),
            )
            GameSnapshot(
                name = checkNotNull(preferences[petName]),
                hairColour = HairColour.entries.findByName(preferences[hairColour], HairColour.Beige),
                eyeColour = EyeColour.entries.findByName(preferences[eyeColour], EyeColour.Violet),
                hairStyle = HairStyle.entries.findByName(preferences[hairStyle], HairStyle.Default),
                money = checkNotNull(preferences[money]),
                healthModifier = checkNotNull(preferences[healthModifier]),
                happinessModifier = checkNotNull(preferences[happinessModifier]),
                energyModifier = checkNotNull(preferences[energyModifier]),
                healthCycleAdjustment = checkNotNull(preferences[healthCycleAdjustment]),
                happinessCycleAdjustment = checkNotNull(preferences[happinessCycleAdjustment]),
                energyCycleAdjustment = checkNotNull(preferences[energyCycleAdjustment]),
                rewardedPetTapsThisCycle = checkNotNull(preferences[rewardedPetTapsThisCycle]),
                goalId = checkNotNull(preferences[goalId]),
                goalTitle = checkNotNull(preferences[goalTitle]),
                income = checkNotNull(preferences[income]),
                expense = checkNotNull(preferences[expense]),
                goalTarget = checkNotNull(preferences[goalTarget]),
                level = checkNotNull(preferences[level]),
                purchasedGoalIds = checkNotNull(preferences[purchasedGoalIds]),
                purchasedShopItemIds = checkNotNull(preferences[purchasedShopItemIds]),
                currentPeriod = checkNotNull(preferences[currentPeriod]),
                cycleEndsAtMillis = checkNotNull(preferences[cycleEndsAtMillis]),
                onboardingCompleted = checkNotNull(preferences[onboardingCompleted]),
                budgetPlanWeek = checkNotNull(preferences[budgetPlanWeek]),
                budgetPlanRecurringExpense = checkNotNull(preferences[budgetPlanRecurringExpense]),
                plannedOptionalExpenses = checkNotNull(preferences[plannedOptionalExpenses]),
                plannedGoalExpenses = checkNotNull(preferences[plannedGoalExpenses]),
                actualOptionalExpenses = checkNotNull(preferences[actualOptionalExpenses]),
                optionalExpenseRecords = decodeBudgetExpenseRecords(
                    preferences[optionalExpenseRecords],
                ),
                actualUnplannedExpenses = checkNotNull(preferences[actualUnplannedExpenses]),
                actualGoalExpenses = checkNotNull(preferences[actualGoalExpenses]),
                unplannedExpenseRecords = decodeBudgetExpenseRecords(
                    preferences[unplannedExpenseRecords],
                ),
                actualAdditionalIncome = checkNotNull(preferences[actualAdditionalIncome]),
                lastReviewedBudgetWeek = checkNotNull(preferences[lastReviewedBudgetWeek]),
                budgetTutorialCompleted = checkNotNull(preferences[budgetTutorialCompleted]),
                budgetOpeningBalance = checkNotNull(preferences[budgetOpeningBalance]),
                levelTwoBudgetExpensesEnabled = checkNotNull(
                    preferences[levelTwoBudgetExpensesEnabled],
                ),
                depositTutorialCompleted = checkNotNull(preferences[depositTutorialCompleted]),
                homeTutorialCompleted = checkNotNull(preferences[homeTutorialCompleted]),
                morningRoutineTutorialCompleted = checkNotNull(
                    preferences[morningRoutineTutorialCompleted],
                ),
                budgetMasterTutorialCompleted = checkNotNull(
                    preferences[budgetMasterTutorialCompleted],
                ),
                budgetMasterBestScore = checkNotNull(preferences[budgetMasterBestScore]),
                changeMoneyTutorialCompleted = checkNotNull(
                    preferences[changeMoneyTutorialCompleted],
                ),
                changeMoneyBestTimesMillis = changeMoneyBestTimes.mapKeys { (key, _) ->
                    key.toIntOrNull() ?: -1
                }.filterKeys { it > 0 },
                changeMoneyLastChallengeId = checkNotNull(
                    preferences[changeMoneyLastChallengeId],
                ),
                demoModeEnabled = checkNotNull(preferences[demoModeEnabled]),
                roomDirtLevel = checkNotNull(preferences[roomDirtLevel]),
                cooldownUnlockCycles = cooldownUnlockCycles,
                taskUseCounts = taskUseCounts,
                taskWeeklyUseCounts = taskWeeklyUseCounts,
                taskUseWeeks = taskUseWeeks,
                eventUnlockedTaskIds = checkNotNull(preferences[eventUnlockedTaskIds]),
                eventFlags = checkNotNull(preferences[eventFlags]),
                completedEventIds = checkNotNull(preferences[completedEventIds]),
                eventPeriodOccurrences = checkNotNull(preferences[eventPeriodOccurrences]),
                handledScenarioEntryIds = checkNotNull(preferences[handledScenarioEntryIds]),
                eventPoolHandledCycles = checkNotNull(preferences[eventPoolHandledCycles]),
                eventRetryPeriods = eventRetryPeriods,
                activeJobIds = checkNotNull(preferences[activeJobIds]),
                jobRemainingActions = jobRemainingActions,
                jobForTaskIds = jobForTaskIds,
                jobEffectsForTaskIds = jobEffectsForTaskIds,
                actionHistory = decodeActionHistory(preferences[actionHistory]),
                customGoal = preferences[customGoalTitle]?.let { title ->
                    ru.finni.financialpetgame.lct.data.CustomGoal(
                        title = title,
                        target = preferences[customGoalTarget] ?: 0,
                        illustrationRes = preferences[customGoalIllustration] ?: R.drawable.goal_pillow,
                    )
                },
                activeDeposit = (preferences[depositAmount] ?: 0)
                    .takeIf { it > 0 }
                    ?.let { amount ->
                        ActiveDeposit(
                            amount = amount,
                            openedAtPeriod = preferences[depositOpenedAtPeriod] ?: 1,
                            durationCycles = preferences[depositDurationCycles] ?: 3,
                            interestPercent = preferences[depositInterestPercent] ?: 7,
                            earlyClosePenaltyPercent = preferences[depositPenaltyPercent] ?: 4,
                        )
                    },
                pendingExpenseDelta = checkNotNull(preferences[pendingExpenseDelta]),
                restedAtPeriod = checkNotNull(preferences[restedAtPeriod]),
            ).withCharacteristicsAt(secondsRemaining)
        })
    }

    override suspend fun initialize() {
        context.gameDataStore.edit { preferences ->
            val nowMillis = System.currentTimeMillis()
            if (preferences[petName] == null) preferences[petName] = GameDefaults.NAME
            if (preferences[hairColour] == null) preferences[hairColour] = HairColour.Beige.name
            if (preferences[eyeColour] == null) preferences[eyeColour] = EyeColour.Violet.name
            if (preferences[hairStyle] == null) preferences[hairStyle] = HairStyle.Default.name
            if (preferences[money] == null) preferences[money] = GameDefaults.MONEY
            if (preferences[health] == null) preferences[health] = GameDefaults.HEALTH
            if (preferences[happiness] == null) preferences[happiness] = GameDefaults.HAPPINESS
            if (preferences[energy] == null) preferences[energy] = GameDefaults.ENERGY
            if (preferences[healthModifier] == null) {
                val purchasedItems = preferences[purchasedShopItemIds].orEmpty()
                val purchasedGoals = preferences[purchasedGoalIds].orEmpty()
                preferences[healthModifier] = permanentBonus(
                    Resource.Health, purchasedItems, purchasedGoals,
                )
                preferences[happinessModifier] = permanentBonus(
                    Resource.Happiness, purchasedItems, purchasedGoals,
                )
                preferences[energyModifier] = permanentBonus(
                    Resource.Energy, purchasedItems, purchasedGoals,
                )
            }
            if (preferences[healthCycleAdjustment] == null) preferences[healthCycleAdjustment] = 0
            if (preferences[happinessCycleAdjustment] == null) {
                preferences[happinessCycleAdjustment] = 0
            }
            if (preferences[energyCycleAdjustment] == null) preferences[energyCycleAdjustment] = 0
            if (preferences[rewardedPetTapsThisCycle] == null) {
                preferences[rewardedPetTapsThisCycle] = 0
            }
            if (preferences[goalTitle] == null) preferences[goalTitle] = GameDefaults.GOAL
            if (preferences[goalId] == null) preferences[goalId] = Goals.Pillow.name
            val hasLegacyRecurringMoney =
                preferences[income] == LEGACY_INCOME && preferences[expense] == LEGACY_EXPENSE
            if (preferences[income] == null || hasLegacyRecurringMoney) {
                preferences[income] = GameDefaults.INCOME
            }
            if (preferences[expense] == null || hasLegacyRecurringMoney) {
                preferences[expense] = GameDefaults.EXPENSE
            }
            if (preferences[goalTarget] == null) preferences[goalTarget] = GameDefaults.GOAL_TARGET
            if (preferences[level] == null) preferences[level] = GameDefaults.LEVEL
            if (preferences[purchasedGoalIds] == null) preferences[purchasedGoalIds] = emptySet()
            if (preferences[purchasedShopItemIds] == null) {
                preferences[purchasedShopItemIds] = emptySet()
            }
            if (preferences[currentPeriod] == null) preferences[currentPeriod] = GameDefaults.CURRENT_PERIOD
            if (preferences[cycleEndsAtMillis] == null) {
                preferences[cycleEndsAtMillis] = nowMillis + GameDefaults.CYCLE_DURATION_MILLIS
            } else {
                val previousDuration =
                    preferences[cycleDurationSeconds] ?: LEGACY_CYCLE_DURATION_SECONDS
                if (previousDuration != GameDefaults.CYCLE_DURATION_SECONDS) {
                    preferences[cycleEndsAtMillis] = migratedCycleEnd(
                        cycleEndsAtMillis = checkNotNull(preferences[cycleEndsAtMillis]),
                        nowMillis = nowMillis,
                        previousDurationSeconds = previousDuration,
                    )
                }
            }
            preferences[cycleDurationSeconds] = GameDefaults.CYCLE_DURATION_SECONDS
            if (preferences[onboardingCompleted] == null) preferences[onboardingCompleted] = false
            if (preferences[plannedOptionalExpenses] == null) preferences[plannedOptionalExpenses] = 0
            if (preferences[plannedGoalExpenses] == null) preferences[plannedGoalExpenses] = 0
            if (preferences[actualOptionalExpenses] == null) preferences[actualOptionalExpenses] = 0
            if (preferences[optionalExpenseRecords] == null) preferences[optionalExpenseRecords] = "[]"
            if (preferences[actualUnplannedExpenses] == null) {
                preferences[actualUnplannedExpenses] = 0
            }
            if (preferences[actualGoalExpenses] == null) preferences[actualGoalExpenses] = 0
            if (preferences[unplannedExpenseRecords] == null) {
                preferences[unplannedExpenseRecords] = "[]"
            }
            if (preferences[pendingExpenseDelta] == null) preferences[pendingExpenseDelta] = 0
            if (preferences[actualAdditionalIncome] == null) preferences[actualAdditionalIncome] = 0
            if (preferences[lastReviewedBudgetWeek] == null) {
                preferences[lastReviewedBudgetWeek] = 0
            }
            if (preferences[budgetTutorialCompleted] == null) {
                preferences[budgetTutorialCompleted] = false
            }
            if ((preferences[budgetOnboardingVersion] ?: 0) < CURRENT_BUDGET_ONBOARDING_VERSION) {
                // Show the redesigned flow once to existing players without deleting their save.
                preferences[budgetTutorialCompleted] = false
                preferences[budgetOnboardingVersion] = CURRENT_BUDGET_ONBOARDING_VERSION
            }
            if (preferences[budgetOpeningBalance] == null) {
                preferences[budgetOpeningBalance] = preferences[money] ?: GameDefaults.MONEY
            }
            if (preferences[levelTwoBudgetExpensesEnabled] == null) {
                preferences[levelTwoBudgetExpensesEnabled] = false
            }
            if (preferences[depositTutorialCompleted] == null) {
                preferences[depositTutorialCompleted] = false
            }
            if (preferences[homeTutorialCompleted] == null) {
                preferences[homeTutorialCompleted] = false
            }
            if (preferences[morningRoutineTutorialCompleted] == null) {
                preferences[morningRoutineTutorialCompleted] = false
            }
            if (preferences[budgetMasterTutorialCompleted] == null) {
                preferences[budgetMasterTutorialCompleted] = false
            }
            if (preferences[budgetMasterBestScore] == null) {
                preferences[budgetMasterBestScore] = 0
            }
            if (preferences[changeMoneyTutorialCompleted] == null) {
                preferences[changeMoneyTutorialCompleted] = false
            }
            if (preferences[changeMoneyLastChallengeId] == null) {
                preferences[changeMoneyLastChallengeId] = 0
            }
            if (preferences[demoModeEnabled] == null) preferences[demoModeEnabled] = false
            if (preferences[roomDirtLevel] == null) {
                preferences[roomDirtLevel] =
                    checkNotNull(preferences[currentPeriod]).coerceIn(1, 5)
            }
            if (preferences[budgetPlanWeek] == null) {
                val period = checkNotNull(preferences[currentPeriod])
                preferences[budgetPlanWeek] = if (
                    preferences[onboardingCompleted] == true && dayForPeriod(period) != 1
                ) {
                    budgetWeekForPeriod(period)
                } else {
                    0
                }
            }
            if (preferences[budgetPlanRecurringExpense] == null) {
                preferences[budgetPlanRecurringExpense] = checkNotNull(preferences[expense])
            }
            if (preferences[eventUnlockedTaskIds] == null) {
                preferences[eventUnlockedTaskIds] = emptySet()
            }
            if (preferences[eventFlags] == null) preferences[eventFlags] = emptySet()
            if (preferences[completedEventIds] == null) preferences[completedEventIds] = emptySet()
            if (preferences[eventPeriodOccurrences] == null) {
                preferences[eventPeriodOccurrences] = emptySet()
            }
            if (preferences[handledScenarioEntryIds] == null) {
                preferences[handledScenarioEntryIds] = emptySet()
            }
            if (preferences[eventPoolHandledCycles] == null) {
                preferences[eventPoolHandledCycles] = emptySet()
            }
            if (preferences[restedAtPeriod] == null) preferences[restedAtPeriod] = 0
            if (preferences[activeJobIds] == null) preferences[activeJobIds] = emptySet()
            if (preferences[actionHistory] == null) preferences[actionHistory] = "[]"
            if (preferences[depositAmount] == null) preferences[depositAmount] = 0

            if (preferences[goalTitle] == LEGACY_GOAL_TITLE &&
                preferences[goalTarget] == LEGACY_GOAL_TARGET
            ) {
                preferences[goalTitle] = GameDefaults.GOAL
                preferences[goalTarget] = GameDefaults.GOAL_TARGET
            }
            if (preferences[goalId] != "custom") {
                val savedGoal = Goals.fromStorageId(checkNotNull(preferences[goalId]))
                preferences[goalTitle] = context.getString(savedGoal.titleRes)
                preferences[goalTarget] = savedGoal.target
            }
            if (
                preferences[income] == GameDefaults.POCKET_MONEY_INCOME &&
                preferences[expense] == PREVIOUS_EXPENSE
            ) {
                preferences[expense] = GameDefaults.EXPENSE
                if (preferences[budgetPlanRecurringExpense] == PREVIOUS_EXPENSE) {
                    preferences[budgetPlanRecurringExpense] = GameDefaults.EXPENSE
                }
            }

            val period = checkNotNull(preferences[currentPeriod])
            if (
                preferences[onboardingCompleted] == true &&
                checkNotNull(preferences[budgetPlanWeek]) >= budgetWeekForPeriod(period)
            ) {
                advanceExpiredCycles(preferences, nowMillis)
            }
            migrateLegacyCooldowns(preferences, nowMillis)
            normalizeTaskCooldowns(preferences)
            preferences[jobPeriodLimitKey("recycle_paper")] = -1
        }
    }

    override suspend fun reset() {
        context.gameDataStore.edit { preferences ->
            preferences.asMap().keys
                .filter {
                    it.name.startsWith(COOLDOWN_UNLOCK_CYCLE_PREFIX) ||
                        it.name.startsWith(LEGACY_COOLDOWN_EXPIRE_PREFIX) ||
                        it.name.startsWith(LEGACY_COOLDOWN_PERIOD_PREFIX) ||
                        it.name.startsWith(TASK_USE_COUNT_PREFIX) ||
                        it.name.startsWith(TASK_WEEKLY_USE_COUNT_PREFIX) ||
                        it.name.startsWith(TASK_USE_WEEK_PREFIX) ||
                        it.name.startsWith(JOB_REMAINING_ACTIONS_PREFIX) ||
                        it.name.startsWith(JOB_FOR_TASK_PREFIX) ||
                        it.name.startsWith(JOB_PAYOUT_PREFIX) ||
                        it.name.startsWith(JOB_HEALTH_PREFIX) ||
                        it.name.startsWith(JOB_HAPPINESS_PREFIX) ||
                        it.name.startsWith(JOB_ENERGY_PREFIX) ||
                        it.name.startsWith(JOB_ALLOWED_CYCLES_PREFIX) ||
                        it.name.startsWith(JOB_PERIOD_LIMIT_PREFIX) ||
                        it.name.startsWith(EVENT_RETRY_PERIOD_PREFIX) ||
                        it.name.startsWith(CHANGE_MONEY_BEST_TIME_PREFIX)
                }
                .forEach { preferences.remove(it) }

            preferences[petName] = GameDefaults.NAME
            preferences[hairColour] = HairColour.Beige.name
            preferences[eyeColour] = EyeColour.Violet.name
            preferences[hairStyle] = HairStyle.Default.name
            preferences[money] = GameDefaults.MONEY
            preferences[health] = GameDefaults.HEALTH
            preferences[happiness] = GameDefaults.HAPPINESS
            preferences[energy] = GameDefaults.ENERGY
            preferences[healthModifier] = 0
            preferences[happinessModifier] = 0
            preferences[energyModifier] = 0
            resetCycleAdjustments(preferences)
            preferences[goalTitle] = GameDefaults.GOAL
            preferences[goalTarget] = GameDefaults.GOAL_TARGET
            preferences[goalId] = Goals.Pillow.name
            preferences[income] = GameDefaults.INCOME
            preferences[expense] = GameDefaults.EXPENSE
            preferences[level] = GameDefaults.LEVEL
            preferences[purchasedGoalIds] = emptySet()
            preferences[purchasedShopItemIds] = emptySet()
            preferences[eventUnlockedTaskIds] = emptySet()
            preferences[eventFlags] = emptySet()
            preferences[completedEventIds] = emptySet()
            preferences[eventPeriodOccurrences] = emptySet()
            preferences[handledScenarioEntryIds] = emptySet()
            preferences[eventPoolHandledCycles] = emptySet()
            preferences[restedAtPeriod] = 0
            preferences[activeJobIds] = emptySet()
            preferences[actionHistory] = "[]"
            preferences[currentPeriod] = GameDefaults.CURRENT_PERIOD
            preferences[budgetPlanWeek] = 0
            preferences[budgetPlanRecurringExpense] = GameDefaults.EXPENSE
            preferences[plannedOptionalExpenses] = 0
            preferences[plannedGoalExpenses] = 0
            preferences[actualOptionalExpenses] = 0
            preferences[optionalExpenseRecords] = "[]"
            preferences[actualUnplannedExpenses] = 0
            preferences[actualGoalExpenses] = 0
            preferences[unplannedExpenseRecords] = "[]"
            preferences[pendingExpenseDelta] = 0
            preferences[actualAdditionalIncome] = 0
            preferences[lastReviewedBudgetWeek] = 0
            preferences[budgetTutorialCompleted] = false
            preferences[budgetOnboardingVersion] = CURRENT_BUDGET_ONBOARDING_VERSION
            preferences[budgetOpeningBalance] = GameDefaults.MONEY
            preferences[levelTwoBudgetExpensesEnabled] = false
            preferences[depositTutorialCompleted] = false
            preferences[homeTutorialCompleted] = false
            preferences[morningRoutineTutorialCompleted] = false
            preferences[budgetMasterTutorialCompleted] = false
            preferences[budgetMasterBestScore] = 0
            preferences[changeMoneyTutorialCompleted] = false
            preferences[changeMoneyLastChallengeId] = 0
            preferences[demoModeEnabled] = false
            preferences[roomDirtLevel] = 1
            preferences[cycleEndsAtMillis] =
                System.currentTimeMillis() + GameDefaults.CYCLE_DURATION_MILLIS
            preferences[cycleDurationSeconds] = GameDefaults.CYCLE_DURATION_SECONDS
            preferences[onboardingCompleted] = false
            clearDeposit(preferences)
        }
    }

    override suspend fun addCustomGoal(title: String, target: Int, illustrationRes: Int) {
        context.gameDataStore.edit { preferences ->
            preferences[customGoalTitle] = title.trim()
            preferences[customGoalTarget] = target.coerceAtLeast(1)
            preferences[customGoalIllustration] = illustrationRes
            preferences[purchasedGoalIds] = checkNotNull(preferences[purchasedGoalIds]) - "custom"
            appendAction(preferences, "Родитель добавил цель «${title.trim()}»")
        }
    }

    override suspend fun selectCustomGoal() {
        context.gameDataStore.edit { preferences ->
            val title = preferences[customGoalTitle] ?: return@edit
            preferences[goalId] = "custom"
            preferences[goalTitle] = title
            preferences[goalTarget] = preferences[customGoalTarget] ?: return@edit
            appendAction(preferences, "Выбрана цель «$title»")
        }
    }

    override suspend fun recordAction(description: String, moneyDelta: Int) {
        context.gameDataStore.edit { preferences -> appendAction(preferences, description, moneyDelta) }
    }

    override suspend fun openDeposit(term: DepositTerm, amount: Int): Boolean {
        val safeAmount = amount.coerceAtLeast(0)
        var opened = false
        context.gameDataStore.edit { preferences ->
            if ((preferences[depositAmount] ?: 0) > 0) return@edit
            val currentMoney = checkNotNull(preferences[money])
            if (safeAmount <= 0 || safeAmount > currentMoney) return@edit

            preferences[money] = currentMoney - safeAmount
            preferences[depositAmount] = safeAmount
            preferences[depositOpenedAtPeriod] = checkNotNull(preferences[currentPeriod])
            preferences[depositDurationCycles] = term.durationCycles
            preferences[depositInterestPercent] = term.interestPercent
            preferences[depositPenaltyPercent] = term.earlyClosePenaltyPercent
            appendAction(preferences, "Открыт вклад на $safeAmount монет", -safeAmount)
            opened = true
        }
        return opened
    }

    override suspend fun closeDeposit(): Boolean {
        var closed = false
        context.gameDataStore.edit { preferences ->
            val amount = preferences[depositAmount] ?: 0
            if (amount <= 0) return@edit
            val deposit = ActiveDeposit(
                amount = amount,
                openedAtPeriod = preferences[depositOpenedAtPeriod] ?: 1,
                durationCycles = preferences[depositDurationCycles] ?: 3,
                interestPercent = preferences[depositInterestPercent] ?: 7,
                earlyClosePenaltyPercent = preferences[depositPenaltyPercent] ?: 4,
            )
            val mature = deposit.isMature(checkNotNull(preferences[currentPeriod]))
            val payout = if (mature) deposit.maturityPayout else deposit.earlyClosePayout
            preferences[money] = checkNotNull(preferences[money]) + payout
            if (mature) {
                recordAdditionalIncome(preferences, deposit.profit)
            } else {
                recordUnplannedExpense(
                    preferences,
                    deposit.earlyCloseLoss,
                    context.getString(R.string.budget_deposit_penalty),
                )
            }
            appendAction(
                preferences,
                if (mature) "Получен доход по вкладу" else "Вклад закрыт досрочно",
                payout,
            )
            clearDeposit(preferences)
            closed = true
        }
        return closed
    }

    private fun clearDeposit(preferences: MutablePreferences) {
        preferences[depositAmount] = 0
        preferences.remove(depositOpenedAtPeriod)
        preferences.remove(depositDurationCycles)
        preferences.remove(depositInterestPercent)
        preferences.remove(depositPenaltyPercent)
    }

    override suspend fun completeTimerCycle() {
        context.gameDataStore.edit { preferences ->
            if (!canCompleteCycle(currentEnergy(preferences, System.currentTimeMillis()))) return@edit
            appendAction(preferences, "Завершён цикл")
            applyCompletedCycles(preferences, count = 1)
            preferences[cycleEndsAtMillis] =
                System.currentTimeMillis() + GameDefaults.CYCLE_DURATION_MILLIS
        }
    }

    override suspend fun advanceExpiredCycles(nowMillis: Long) {
        context.gameDataStore.edit { preferences ->
            if (preferences[onboardingCompleted] != true) return@edit
            advanceExpiredCycles(preferences, nowMillis)
        }
    }

    override suspend fun buyCareItem(item: CardItem) {
        context.gameDataStore.edit { preferences ->
            val currentCycle = checkNotNull(preferences[currentPeriod])
            val secondsRemaining = cycleSecondsRemaining(
                cycleEndsAtMillis = checkNotNull(preferences[cycleEndsAtMillis]),
                nowMillis = System.currentTimeMillis(),
            )
            val currentCyclePosition = currentCyclePosition(
                currentPeriod = currentCycle,
                cycleSecondsRemaining = secondsRemaining,
            )
            val cycleRecoveryAction = secondsRemaining == 0 &&
                currentEnergy(preferences, System.currentTimeMillis()) == 0 &&
                (item == EnergyItem.TakeASeat || item == EnergyItem.TakeANap)
            val purchasedShopItems = checkNotNull(preferences[purchasedShopItemIds])
            if (item is ShopItem && item.storageId in purchasedShopItems) return@edit
            if (item is ShopItem && !item.isUnlockedAtPeriod(currentCycle)) return@edit
            val instanceJobId = if (item is TaskItem) {
                preferences[jobForTaskKey(item.storageId)]
            } else {
                null
            }
            val appliedEffects = if (instanceJobId == null) {
                item.careEffects
            } else {
                listOf(
                    Effect(Resource.Health, preferences[jobHealthKey(instanceJobId)] ?: 0),
                    Effect(Resource.Happiness, preferences[jobHappinessKey(instanceJobId)] ?: 0),
                    Effect(Resource.Energy, preferences[jobEnergyKey(instanceJobId)] ?: 0),
                    Effect(Resource.Money, preferences[jobPayoutKey(instanceJobId)] ?: 0),
                )
            }
            if (item is TaskItem && !item.isAvailable(
                    currentPeriod = currentCycle,
                    totalUses = preferences[taskUseCountKey(item)] ?: 0,
                    weeklyUses = preferences[taskWeeklyUseCountKey(item)] ?: 0,
                    usageWeek = preferences[taskUseWeekKey(item)] ?: -1,
                    completedTaskCounts = preferences.intMap(TASK_USE_COUNT_PREFIX),
                    eventUnlocked = item.storageId in checkNotNull(preferences[eventUnlockedTaskIds]),
                )
            ) return@edit

            if (item is TaskItem && item.earnings > 0) {
                val preparationBlock = workPreparationBlock(
                    currentPeriod = currentCycle,
                    currentCyclePosition = currentCyclePosition,
                    getReadyUnlockCycle =
                        preferences[cooldownUnlockCycleKey(TaskItem.GetReady)] ?: 0.0,
                    lessonsUnlockCycle =
                        preferences[cooldownUnlockCycleKey(TaskItem.Lessons)] ?: 0.0,
                    purchasedShopItemIds = purchasedShopItems,
                )
                if (preparationBlock != null) return@edit
            }

            if (instanceJobId != null &&
                (preferences[jobRemainingActionsKey(instanceJobId)] ?: -1) == 0
            ) return@edit
            if (instanceJobId != null && item is TaskItem) {
                val day = dayForPeriod(currentCycle)
                val allowed = preferences[jobAllowedCyclesKey(instanceJobId)] ?: "Any"
                if (allowed != "Any" && day !in 6..7) return@edit
                val currentWeek = weekForPeriod(currentCycle)
                val usesThisWeek = if (preferences[taskUseWeekKey(item)] == currentWeek) {
                    preferences[taskWeeklyUseCountKey(item)] ?: 0
                } else {
                    0
                }
                val periodLimit = preferences[jobPeriodLimitKey(instanceJobId)] ?: -1
                if (periodLimit >= 0 && usesThisWeek >= periodLimit) return@edit
            }

            if (item is TaskItem) {
                val unlockCycle = preferences[cooldownUnlockCycleKey(item)] ?: 0.0
                if (unlockCycle > currentCyclePosition) return@edit
            } else if (instanceJobId == null && !cycleRecoveryAction) {
                when (item.cooldown) {
                    Cooldown.None -> Unit
                    is Cooldown.Cycles -> {
                        val unlockCycle = preferences[cooldownUnlockCycleKey(item)] ?: 0.0
                        if (unlockCycle > currentCyclePosition) return@edit
                    }
                }
            }

            val currentLevel = checkNotNull(preferences[level])
            if (instanceJobId == null && currentLevel < item.level) return@edit

            val currentMoney = checkNotNull(preferences[money])
            val discounted = item is ShopItem &&
                NEXT_PURCHASE_DISCOUNT_FLAG in checkNotNull(preferences[eventFlags])
            val effectivePrice = if (discounted) item.price / 2 else item.price
            if (currentMoney < effectivePrice) return@edit
            if (!canApplyEffects(
                    health = currentCharacteristic(preferences, Resource.Health, secondsRemaining),
                    happiness = currentCharacteristic(
                        preferences,
                        Resource.Happiness,
                        secondsRemaining,
                    ),
                    energy = currentCharacteristic(preferences, Resource.Energy, secondsRemaining),
                    money = currentMoney,
                    effects = appliedEffects + Effect(Resource.Money, -effectivePrice),
                )
            ) return@edit

            preferences[money] = moneyAfterPurchase(currentMoney, effectivePrice)
            recordOptionalExpense(preferences, effectivePrice, budgetExpenseTitle(item))
            if (discounted) consumeFlag(preferences, NEXT_PURCHASE_DISCOUNT_FLAG)

            for (effect in appliedEffects) {
                when (effect.resource) {
                    Resource.Health,
                    Resource.Happiness,
                    Resource.Energy,
                    -> applyCharacteristicEffect(
                        preferences = preferences,
                        resource = effect.resource,
                        change = effect.increase,
                        permanent = item is ShopItem,
                    )
                    Resource.Money -> preferences[money] =
                        checkNotNull(preferences[money]) + effect.increase
                }
            }
            recordAdditionalIncome(
                preferences,
                appliedEffects
                    .filter { it.resource == Resource.Money }
                    .sumOf(Effect::increase)
                    .coerceAtLeast(0),
            )

            if (item is TaskItem) {
                preferences[cooldownUnlockCycleKey(item)] = when (val cooldown = item.cooldown) {
                    Cooldown.None -> currentCyclePosition
                    is Cooldown.Cycles -> if (cooldown.value == 1.0) {
                        // A daily task unlocks exactly at the next cycle boundary.
                        currentCycle + 1.0
                    } else {
                        cooldownUnlockCycle(currentCyclePosition, cooldown.value)
                    }
                }
            } else if (instanceJobId == null) {
                when (val cooldown = item.cooldown) {
                    Cooldown.None -> Unit
                    is Cooldown.Cycles -> {
                        preferences[cooldownUnlockCycleKey(item)] = cooldownUnlockCycle(
                            currentCyclePosition = currentCyclePosition,
                            cooldownCycles = cooldown.value,
                        )
                    }
                }
            }

            if (item is ShopItem) {
                preferences[purchasedShopItemIds] = purchasedShopItems + item.storageId
            }
            if (item == EnergyItem.TakeASeat || item == EnergyItem.TakeANap) {
                preferences[restedAtPeriod] = currentCycle
            }
            if (item is TaskItem) {
                val currentWeek = weekForPeriod(currentCycle)
                val previousWeek = preferences[taskUseWeekKey(item)] ?: -1
                val weeklyUses = if (previousWeek == currentWeek) {
                    preferences[taskWeeklyUseCountKey(item)] ?: 0
                } else {
                    0
                }
                preferences[taskUseCountKey(item)] =
                    (preferences[taskUseCountKey(item)] ?: 0) + 1
                preferences[taskWeeklyUseCountKey(item)] = weeklyUses + 1
                preferences[taskUseWeekKey(item)] = currentWeek
                if (item == TaskItem.Cleaning) preferences[roomDirtLevel] = 1
                if (item == TaskItem.BeadCrafts) addFlag(preferences, BRAIDED_BRACELETS_FLAG)
                if (instanceJobId != null) {
                    val remaining = preferences[jobRemainingActionsKey(instanceJobId)] ?: -1
                    if (remaining > 0) {
                        preferences[jobRemainingActionsKey(instanceJobId)] = remaining - 1
                        if (remaining == 1) {
                            preferences[eventUnlockedTaskIds] =
                                checkNotNull(preferences[eventUnlockedTaskIds]) - item.storageId
                            preferences[activeJobIds] =
                                checkNotNull(preferences[activeJobIds]) - instanceJobId
                        }
                    }
                }
            }
            val incomeChange = appliedEffects
                .filter { it.resource == Resource.Money }
                .sumOf(Effect::increase)
            appendAction(
                preferences,
                if (item is TaskItem) "Выполнено задание: ${item.storageId}"
                else "Использовано: ${item.storageId}",
                incomeChange - effectivePrice,
            )
        }
    }

    override suspend fun startBudgetMasterAttempt(): MiniGameStartResult? {
        var result: MiniGameStartResult? = null
        context.gameDataStore.edit { preferences ->
            val item = HappinessItem.BudgetMaster
            val currentCycle = checkNotNull(preferences[currentPeriod])
            val secondsRemaining = cycleSecondsRemaining(
                cycleEndsAtMillis = checkNotNull(preferences[cycleEndsAtMillis]),
                nowMillis = System.currentTimeMillis(),
            )
            val cyclePosition = currentCyclePosition(
                currentPeriod = currentCycle,
                cycleSecondsRemaining = secondsRemaining,
            )
            val unlockCycle = preferences[cooldownUnlockCycleKey(item)] ?: 0.0
            if (unlockCycle > cyclePosition) return@edit
            if (checkNotNull(preferences[level]) < item.level) return@edit

            val currentMoney = checkNotNull(preferences[money])
            if (currentMoney < item.price) return@edit
            if (!canApplyEffects(
                    health = currentCharacteristic(preferences, Resource.Health, secondsRemaining),
                    happiness = currentCharacteristic(
                        preferences,
                        Resource.Happiness,
                        secondsRemaining,
                    ),
                    energy = currentCharacteristic(preferences, Resource.Energy, secondsRemaining),
                    money = currentMoney,
                    effects = item.careEffects + Effect(Resource.Money, -item.price),
                )
            ) return@edit

            preferences[money] = currentMoney - item.price
            recordOptionalExpense(preferences, item.price, "Мастер бюджета")
            preferences[cooldownUnlockCycleKey(item)] = when (val cooldown = item.cooldown) {
                Cooldown.None -> cyclePosition
                is Cooldown.Cycles -> cooldownUnlockCycle(cyclePosition, cooldown.value)
            }
            val showTutorial = !checkNotNull(preferences[budgetMasterTutorialCompleted])
            preferences[budgetMasterTutorialCompleted] = true
            appendAction(preferences, "Начата мини-игра «Мастер Бюджета»", -item.price)
            result = MiniGameStartResult(showTutorial)
        }
        return result
    }

    override suspend fun completeBudgetMasterAttempt(score: Int, successful: Boolean) {
        context.gameDataStore.edit { preferences ->
            val safeScore = score.coerceAtLeast(0)
            if (safeScore > checkNotNull(preferences[budgetMasterBestScore])) {
                preferences[budgetMasterBestScore] = safeScore
            }
            if (successful) {
                HappinessItem.BudgetMaster.careEffects.forEach { effect ->
                    when (effect.resource) {
                        Resource.Health,
                        Resource.Happiness,
                        Resource.Energy,
                        -> applyCharacteristicEffect(
                            preferences = preferences,
                            resource = effect.resource,
                            change = effect.increase,
                            permanent = false,
                        )
                        Resource.Money -> preferences[money] =
                            checkNotNull(preferences[money]) + effect.increase
                    }
                }
            }
            appendAction(
                preferences,
                if (successful) {
                    "Пройдена мини-игра «Мастер Бюджета»: $safeScore карточек"
                } else {
                    "Мини-игра «Мастер Бюджета» не пройдена: $safeScore карточек"
                },
            )
        }
    }

    override suspend fun startChangeMoneyAttempt(): ChangeMoneyStartResult? {
        var result: ChangeMoneyStartResult? = null
        context.gameDataStore.edit { preferences ->
            val item = HappinessItem.ChangeMoney
            val currentCycle = checkNotNull(preferences[currentPeriod])
            val secondsRemaining = cycleSecondsRemaining(
                cycleEndsAtMillis = checkNotNull(preferences[cycleEndsAtMillis]),
                nowMillis = System.currentTimeMillis(),
            )
            val cyclePosition = currentCyclePosition(
                currentPeriod = currentCycle,
                cycleSecondsRemaining = secondsRemaining,
            )
            val unlockCycle = preferences[cooldownUnlockCycleKey(item)] ?: 0.0
            if (unlockCycle > cyclePosition) return@edit
            if (checkNotNull(preferences[level]) < item.level) return@edit

            val currentMoney = checkNotNull(preferences[money])
            if (currentMoney < item.price) return@edit
            if (!canApplyEffects(
                    health = currentCharacteristic(preferences, Resource.Health, secondsRemaining),
                    happiness = currentCharacteristic(
                        preferences,
                        Resource.Happiness,
                        secondsRemaining,
                    ),
                    energy = currentCharacteristic(preferences, Resource.Energy, secondsRemaining),
                    money = currentMoney,
                    effects = item.careEffects + Effect(Resource.Money, -item.price),
                )
            ) return@edit

            preferences[money] = currentMoney - item.price
            recordOptionalExpense(preferences, item.price, "Разменяй!")
            preferences[cooldownUnlockCycleKey(item)] = when (val cooldown = item.cooldown) {
                Cooldown.None -> cyclePosition
                is Cooldown.Cycles -> cooldownUnlockCycle(cyclePosition, cooldown.value)
            }
            val showTutorial = !checkNotNull(preferences[changeMoneyTutorialCompleted])
            val challenge = nextChangeMoneyChallenge(
                checkNotNull(preferences[changeMoneyLastChallengeId]),
            )
            preferences[changeMoneyTutorialCompleted] = true
            preferences[changeMoneyLastChallengeId] = challenge.id
            appendAction(preferences, "Начата мини-игра «Разменяй!»", -item.price)
            result = ChangeMoneyStartResult(showTutorial, challenge)
        }
        return result
    }

    override suspend fun completeChangeMoneyAttempt(challengeId: Int, elapsedMillis: Long) {
        context.gameDataStore.edit { preferences ->
            HappinessItem.ChangeMoney.careEffects.forEach { effect ->
                when (effect.resource) {
                    Resource.Health,
                    Resource.Happiness,
                    Resource.Energy,
                    -> applyCharacteristicEffect(
                        preferences = preferences,
                        resource = effect.resource,
                        change = effect.increase,
                        permanent = false,
                    )
                    Resource.Money -> preferences[money] =
                        checkNotNull(preferences[money]) + effect.increase
                }
            }
            val safeElapsedMillis = elapsedMillis.coerceIn(1L, Int.MAX_VALUE.toLong()).toInt()
            val bestTimeKey = changeMoneyBestTimeKey(challengeId)
            val previousBest = preferences[bestTimeKey]
            if (previousBest == null || safeElapsedMillis < previousBest) {
                preferences[bestTimeKey] = safeElapsedMillis
            }
            appendAction(preferences, "Пройдена мини-игра «Разменяй!»")
        }
    }

    override suspend fun buyGoal(goal: Goals, price: Int): Boolean {
        val safePrice = price.coerceAtLeast(0)
        var purchased = false
        context.gameDataStore.edit { preferences ->
            val purchasedGoals = checkNotNull(preferences[purchasedGoalIds])
            if (goal.name in purchasedGoals) return@edit

            val currentMoney = checkNotNull(preferences[money])
            if (currentMoney < safePrice) return@edit

            preferences[money] = currentMoney - safePrice
            for (effect in goal.goalEffects) {
                when (effect.resource) {
                    Resource.Health,
                    Resource.Happiness,
                    Resource.Energy,
                    -> applyCharacteristicEffect(
                        preferences, effect.resource, effect.increase, permanent = true,
                    )
                    Resource.Money -> preferences[money] =
                        checkNotNull(preferences[money]) + effect.increase
                }
            }

            val currentLevel = checkNotNull(preferences[level])
            preferences[level] = levelAfterGoalPurchase(currentLevel, goal.level)
            preferences[purchasedGoalIds] = purchasedGoals + goal.name
            recordGoalExpense(preferences, safePrice)
            appendAction(preferences, "Куплена цель «${context.getString(goal.titleRes)}»", -safePrice)
            purchased = true
        }
        return purchased
    }

    override suspend fun buyCustomGoal(price: Int): Boolean {
        val safePrice = price.coerceAtLeast(0)
        var purchased = false
        context.gameDataStore.edit { preferences ->
            val title = preferences[customGoalTitle] ?: return@edit
            if ("custom" in checkNotNull(preferences[purchasedGoalIds])) return@edit
            val currentMoney = checkNotNull(preferences[money])
            if (currentMoney < safePrice) return@edit

            preferences[money] = currentMoney - safePrice
            preferences[level] = checkNotNull(preferences[level]) + 1
            preferences[purchasedGoalIds] = checkNotNull(preferences[purchasedGoalIds]) + "custom"
            recordGoalExpense(preferences, safePrice)
            appendAction(preferences, "Куплена цель «$title»", -safePrice)
            purchased = true
        }
        return purchased
    }

    override suspend fun selectGoal(goal: Goals) {
        context.gameDataStore.edit { preferences ->
            if (goal.name in checkNotNull(preferences[purchasedGoalIds])) return@edit
            preferences[goalId] = goal.name
            preferences[goalTitle] = context.getString(goal.titleRes)
            preferences[goalTarget] = goal.target
            appendAction(preferences, "Выбрана цель «${context.getString(goal.titleRes)}»")
        }
    }

    override suspend fun applyEffects(effects: List<Effect>) {
        context.gameDataStore.edit { preferences ->
            val secondsRemaining = cycleSecondsRemaining(
                checkNotNull(preferences[cycleEndsAtMillis]),
                System.currentTimeMillis(),
            )
            val moneyChange = effects
                .filter { it.resource == Resource.Money }
                .sumOf(Effect::increase)
            val currentMoney = checkNotNull(preferences[money])
            if (!canApplyEffects(
                    health = currentCharacteristic(preferences, Resource.Health, secondsRemaining),
                    happiness = currentCharacteristic(
                        preferences,
                        Resource.Happiness,
                        secondsRemaining,
                    ),
                    energy = currentCharacteristic(preferences, Resource.Energy, secondsRemaining),
                    money = currentMoney,
                    effects = effects,
                )
            ) return@edit

            effects.groupBy(Effect::resource).forEach { (resource, resourceEffects) ->
                val change = resourceEffects.sumOf(Effect::increase)
                when (resource) {
                    Resource.Health,
                    Resource.Happiness,
                    Resource.Energy,
                    -> applyCharacteristicEffect(preferences, resource, change, permanent = false)
                    Resource.Money -> preferences[money] = currentMoney + change
                }
            }
            recordOptionalExpense(
                preferences,
                (-moneyChange).coerceAtLeast(0),
                "Другое событие",
            )
            recordAdditionalIncome(preferences, moneyChange.coerceAtLeast(0))
            appendAction(preferences, "Применён результат события", moneyChange)
        }
    }

    override suspend fun rewardPetTap(): Boolean {
        var rewarded = false
        context.gameDataStore.edit { preferences ->
            val tapCount = preferences[rewardedPetTapsThisCycle] ?: 0
            if (!canRewardPetTap(tapCount)) return@edit

            preferences[rewardedPetTapsThisCycle] = tapCount + 1
            applyCharacteristicEffect(
                preferences = preferences,
                resource = Resource.Happiness,
                change = PET_TAP_HAPPINESS_REWARD,
                permanent = false,
            )
            rewarded = true
        }
        return rewarded
    }

    override suspend fun completeOnboarding(setup: PetSetup) {
        context.gameDataStore.edit { preferences ->
            preferences[petName] = setup.name.trim()
            preferences[hairColour] = setup.hairColour.name
            preferences[eyeColour] = setup.eyeColour.name
            preferences[hairStyle] = setup.hairStyle.name
            preferences[goalId] = setup.goal.name
            preferences[goalTitle] = context.getString(setup.goal.titleRes)
            preferences[goalTarget] = setup.goal.target
            preferences[cycleEndsAtMillis] =
                System.currentTimeMillis() + GameDefaults.CYCLE_DURATION_MILLIS
            resetCycleAdjustments(preferences)
            preferences[onboardingCompleted] = true
            appendAction(preferences, "Завершён онбординг")
        }
    }

    override suspend fun updatePetAppearance(appearance: PetAppearance): Boolean {
        var updated = false
        context.gameDataStore.edit { preferences ->
            val currentMoney = checkNotNull(preferences[money])
            if (currentMoney < PET_APPEARANCE_CHANGE_PRICE) return@edit

            preferences[money] = currentMoney - PET_APPEARANCE_CHANGE_PRICE
            recordOptionalExpense(
                preferences,
                PET_APPEARANCE_CHANGE_PRICE,
                "Изменение внешности",
            )
            preferences[petName] = appearance.name.trim()
            preferences[hairColour] = appearance.hairColour.name
            preferences[eyeColour] = appearance.eyeColour.name
            preferences[hairStyle] = appearance.hairStyle.name
            appendAction(preferences, "Изменён персонаж", -PET_APPEARANCE_CHANGE_PRICE)
            updated = true
        }
        return updated
    }

    override suspend fun saveBudgetPlan(optionalExpenses: Int) {
        context.gameDataStore.edit { preferences ->
            val period = checkNotNull(preferences[currentPeriod])
            val planningWeek = budgetWeekForPeriod(period)
            val isRedesignedOnboardingReplay =
                preferences[budgetPlanWeek] == planningWeek &&
                    !checkNotNull(preferences[budgetTutorialCompleted])
            if (isRedesignedOnboardingReplay) {
                preferences[budgetTutorialCompleted] = true
                appendAction(preferences, "Пройден обновлённый онбординг бюджета")
                return@edit
            }
            val levelTwoEnabled = checkNotNull(preferences[levelTwoBudgetExpensesEnabled])
            val activateLevelTwo = !levelTwoEnabled && checkNotNull(preferences[level]) >= 2
            if (activateLevelTwo) {
                preferences[expense] = checkNotNull(preferences[expense]) +
                    GameDefaults.VITAMINS_EXPENSE + GameDefaults.SPORTS_SECTION_EXPENSE
                preferences[levelTwoBudgetExpensesEnabled] = true
            }
            val availableAfterMandatory = (
                checkNotNull(preferences[income]) - checkNotNull(preferences[expense])
            ).coerceAtLeast(0)
            preferences[budgetPlanWeek] = planningWeek
            preferences[budgetPlanRecurringExpense] = checkNotNull(preferences[expense])
            preferences[plannedGoalExpenses] = 0
            preferences[plannedOptionalExpenses] = optionalExpenses.coerceIn(
                minimumValue = 0,
                maximumValue = availableAfterMandatory,
            )
            preferences[actualOptionalExpenses] = 0
            preferences[optionalExpenseRecords] = "[]"
            preferences[actualUnplannedExpenses] = 0
            preferences[actualGoalExpenses] = 0
            preferences[unplannedExpenseRecords] = "[]"
            preferences[actualAdditionalIncome] = 0
            preferences[budgetTutorialCompleted] = true
            preferences[budgetOpeningBalance] = checkNotNull(preferences[money])
            preferences[cycleEndsAtMillis] =
                System.currentTimeMillis() + GameDefaults.CYCLE_DURATION_MILLIS
            resetCycleAdjustments(preferences)
            appendAction(preferences, "Составлен план бюджета")
        }
    }

    override suspend fun completeBudgetReview() {
        context.gameDataStore.edit { preferences ->
            val completedWeek = budgetWeekForPeriod(checkNotNull(preferences[currentPeriod])) - 1
            if (completedWeek <= 0) return@edit
            if (preferences[budgetPlanWeek] != completedWeek) return@edit
            if ((preferences[lastReviewedBudgetWeek] ?: 0) >= completedWeek) return@edit

            val result = BudgetPeriodResult(
                plannedIncome = checkNotNull(preferences[income]),
                actualIncome = checkNotNull(preferences[income]) +
                    (preferences[actualAdditionalIncome] ?: 0),
                plannedExpenses = checkNotNull(preferences[budgetPlanRecurringExpense]) +
                    checkNotNull(preferences[plannedOptionalExpenses]) +
                    checkNotNull(preferences[plannedGoalExpenses]),
                actualExpenses = checkNotNull(preferences[budgetPlanRecurringExpense]) +
                    checkNotNull(preferences[actualOptionalExpenses]) +
                    checkNotNull(preferences[actualUnplannedExpenses]) +
                    checkNotNull(preferences[actualGoalExpenses]),
            )
            if (result.isSuccessful) {
                preferences[money] = checkNotNull(preferences[money]) + BUDGET_SUCCESS_REWARD
            }
            preferences[lastReviewedBudgetWeek] = completedWeek
            appendAction(preferences, "Подведены итоги бюджета")
        }
    }

    override suspend fun completeHomeTutorial() {
        context.gameDataStore.edit { preferences ->
            preferences[homeTutorialCompleted] = true
            appendAction(preferences, "Завершено обучение главного экрана")
        }
    }

    override suspend fun completeMorningRoutineTutorial() {
        context.gameDataStore.edit { preferences ->
            preferences[morningRoutineTutorialCompleted] = true
            appendAction(preferences, "Завершено обучение утреннему ритуалу")
        }
    }

    override suspend fun completeDepositTutorial() {
        context.gameDataStore.edit { preferences ->
            preferences[depositTutorialCompleted] = true
            appendAction(preferences, "Просмотрено обучение вкладам")
        }
    }

    override suspend fun setDemoMode(enabled: Boolean) {
        context.gameDataStore.edit { preferences ->
            preferences[demoModeEnabled] = enabled
            appendAction(
                preferences,
                if (enabled) "Включён демонстрационный режим"
                else "Выключен демонстрационный режим",
            )
        }
    }

    override suspend fun resolveScheduledEvent(
        event: ScheduledEvent,
        unlockedJob: JobDef?,
        accepted: Boolean,
    ): EventResolutionResult {
        var depositClosedWithPenalty = false
        var resolved = false
        context.gameDataStore.edit { preferences ->
            val definition = event.definition
            val currentPeriodValue = checkNotNull(preferences[currentPeriod])
            val currentWeek = weekForPeriod(checkNotNull(preferences[currentPeriod])) + 1
            val occurrence = "${definition.id}@$currentWeek"
            val alreadyHandled = when (definition.frequency) {
                EventFrequency.Once,
                EventFrequency.PermanentModifier,
                -> definition.id in checkNotNull(preferences[completedEventIds])
                EventFrequency.PerPeriod -> occurrence in
                    checkNotNull(preferences[eventPeriodOccurrences])
            }
            if (alreadyHandled) {
                resolved = true
                return@edit
            }
            val secondsRemaining = cycleSecondsRemaining(
                checkNotNull(preferences[cycleEndsAtMillis]),
                System.currentTimeMillis(),
            )
            val currentHealth = currentCharacteristic(
                preferences,
                Resource.Health,
                secondsRemaining,
            )
            val currentHappiness = currentCharacteristic(
                preferences,
                Resource.Happiness,
                secondsRemaining,
            )
            val currentEnergy = currentCharacteristic(
                preferences,
                Resource.Energy,
                secondsRemaining,
            )
            if (!canResolveScheduledEvent(
                    event,
                    accepted,
                    currentHealth = currentHealth,
                    currentHappiness = currentHappiness,
                    currentEnergy = currentEnergy,
                    currentMoney = checkNotNull(preferences[money]),
                )
            ) {
                return@edit
            }
            if (accepted) {
                val depleted = applyEventDeltas(preferences, event, secondsRemaining)
                if (depleted && (preferences[depositAmount] ?: 0) > 0) {
                    closeDepositEarly(preferences)
                    depositClosedWithPenalty = true
                }
                preferences[eventFlags] =
                    checkNotNull(preferences[eventFlags]) + definition.setsFlags
                if (definition.permanentExpenseDelta != 0) {
                    preferences[pendingExpenseDelta] = checkNotNull(preferences[pendingExpenseDelta]) +
                        definition.permanentExpenseDelta
                }
                unlockedJob?.let { createJobInstance(preferences, it) }
                preferences.remove(eventRetryPeriodKey(definition.id))
            } else if (definition.frequency == EventFrequency.Once) {
                preferences[eventRetryPeriodKey(definition.id)] = currentPeriodValue + 2
            }

            event.scenarioStepId?.let { stepId ->
                preferences[handledScenarioEntryIds] =
                    checkNotNull(preferences[handledScenarioEntryIds]) + stepId
            }
            if (!event.isScripted) {
                preferences[eventPoolHandledCycles] =
                    checkNotNull(preferences[eventPoolHandledCycles]) +
                    checkNotNull(preferences[currentPeriod]).toString()
            }
            when (definition.frequency) {
                EventFrequency.Once -> if (accepted) {
                    preferences[completedEventIds] =
                        checkNotNull(preferences[completedEventIds]) + definition.id
                }
                EventFrequency.PermanentModifier -> preferences[completedEventIds] =
                    checkNotNull(preferences[completedEventIds]) + definition.id
                EventFrequency.PerPeriod -> preferences[eventPeriodOccurrences] =
                    checkNotNull(preferences[eventPeriodOccurrences]) + occurrence
            }
            appendAction(
                preferences,
                "Событие «${definition.title}»: ${if (accepted) "согласился" else "отказался"}",
                if (accepted) event.appliedDeltas.money else 0,
            )
            resolved = true
        }
        return EventResolutionResult(
            resolved = resolved,
            depositClosedWithPenalty = depositClosedWithPenalty,
        )
    }

    private fun advanceExpiredCycles(preferences: MutablePreferences, nowMillis: Long) {
        val currentCycleEnd = checkNotNull(preferences[cycleEndsAtMillis])
        if (!hasCycleExpired(currentCycleEnd, nowMillis)) return
        if (!canCompleteCycle(currentEnergy(preferences, nowMillis))) return

        applyCompletedCycles(preferences, count = 1)
        appendAction(preferences, "Завершён цикл")
        preferences[cycleEndsAtMillis] = nextCycleEnd(nowMillis)
    }

    private fun currentEnergy(preferences: Preferences, nowMillis: Long): Int =
        characteristicValue(
            baseValue = GameDefaults.ENERGY,
            permanentModifier = checkNotNull(preferences[energyModifier]),
            cycleAdjustment = checkNotNull(preferences[energyCycleAdjustment]),
            secondsRemaining = cycleSecondsRemaining(
                checkNotNull(preferences[cycleEndsAtMillis]),
                nowMillis,
            ),
        )

    private fun migrateLegacyCooldowns(preferences: MutablePreferences, nowMillis: Long) {
        val legacyEntries = preferences.asMap().entries.filter { (key, _) ->
            key.name.startsWith(LEGACY_COOLDOWN_EXPIRE_PREFIX) ||
                key.name.startsWith(LEGACY_COOLDOWN_PERIOD_PREFIX)
        }
        if (legacyEntries.isEmpty()) return

        val cyclePosition = currentCyclePosition(
            currentPeriod = checkNotNull(preferences[currentPeriod]),
            cycleSecondsRemaining = cycleSecondsRemaining(
                cycleEndsAtMillis = checkNotNull(preferences[cycleEndsAtMillis]),
                nowMillis = nowMillis,
            ),
        )
        legacyEntries.forEach { (key, value) ->
            val unlockCycle = when {
                key.name.startsWith(LEGACY_COOLDOWN_EXPIRE_PREFIX) && value is Long -> {
                    val remainingMillis = (value - nowMillis).coerceAtLeast(0L)
                    cyclePosition +
                        remainingMillis.toDouble() / GameDefaults.CYCLE_DURATION_MILLIS
                }
                key.name.startsWith(LEGACY_COOLDOWN_PERIOD_PREFIX) && value is Int ->
                    value.toDouble()
                else -> null
            }
            val storageId = key.name
                .removePrefix(LEGACY_COOLDOWN_EXPIRE_PREFIX)
                .removePrefix(LEGACY_COOLDOWN_PERIOD_PREFIX)
            val newKey = cooldownUnlockCycleKey(storageId)
            if (unlockCycle != null && preferences[newKey] == null && unlockCycle > cyclePosition) {
                preferences[newKey] = unlockCycle
            }
            preferences.remove(key)
        }
    }

    private fun normalizeTaskCooldowns(preferences: MutablePreferences) {
        val currentCycle = checkNotNull(preferences[currentPeriod])
        TaskItem.entries.forEach { task ->
            val key = cooldownUnlockCycleKey(task)
            val savedUnlockCycle = preferences[key] ?: return@forEach
            val maximumUnlock = when (val cooldown = task.cooldown) {
                Cooldown.None -> currentCycle.toDouble()
                is Cooldown.Cycles -> currentCycle + cooldown.value
            }
            if (savedUnlockCycle > maximumUnlock) preferences[key] = maximumUnlock
        }
    }

    private fun applyCharacteristicEffect(
        preferences: MutablePreferences,
        resource: Resource,
        change: Int,
        permanent: Boolean,
    ) {
        if (change == 0) return
        val modifierKey = when (resource) {
            Resource.Health -> healthModifier
            Resource.Happiness -> happinessModifier
            Resource.Energy -> energyModifier
            Resource.Money -> return
        }
        val adjustmentKey = when (resource) {
            Resource.Health -> healthCycleAdjustment
            Resource.Happiness -> happinessCycleAdjustment
            Resource.Energy -> energyCycleAdjustment
            Resource.Money -> return
        }
        val baseValue = when (resource) {
            Resource.Health -> GameDefaults.HEALTH
            Resource.Happiness -> GameDefaults.HAPPINESS
            Resource.Energy -> GameDefaults.ENERGY
            Resource.Money -> return
        }
        val secondsRemaining = cycleSecondsRemaining(
            checkNotNull(preferences[cycleEndsAtMillis]),
            System.currentTimeMillis(),
        )
        val currentValue = currentCharacteristic(preferences, resource, secondsRemaining)
        val targetValue = (currentValue + change).coerceIn(0, 100)
        val modifier = (preferences[modifierKey] ?: 0) + if (permanent) change else 0
        if (permanent) preferences[modifierKey] = modifier
        preferences[adjustmentKey] = characteristicCycleAdjustmentForValue(
            baseValue = baseValue,
            permanentModifier = modifier,
            targetValue = targetValue,
            secondsRemaining = secondsRemaining,
        )
    }

    private fun resetCycleAdjustments(preferences: MutablePreferences) {
        preferences[healthCycleAdjustment] = 0
        preferences[happinessCycleAdjustment] = 0
        preferences[energyCycleAdjustment] = 0
        preferences[rewardedPetTapsThisCycle] = 0
    }

    private fun applyCompletedCycles(preferences: MutablePreferences, count: Int) {
        if (count <= 0) return

        val currentMoney = checkNotNull(preferences[money])
        val currentIncome = checkNotNull(preferences[income])
        val currentExpense = checkNotNull(preferences[expense])
        val periodBeforeCompletion = checkNotNull(preferences[currentPeriod])
        preferences[money] = moneyAfterCompletedCycles(
            money = currentMoney,
            income = currentIncome,
            expense = currentExpense,
            currentPeriod = periodBeforeCompletion,
            completedCycles = count,
        )
        preferences[currentPeriod] = periodBeforeCompletion + count
        preferences[roomDirtLevel] = roomDirtAfterCompletedCycles(
            currentLevel = checkNotNull(preferences[roomDirtLevel]),
            completedCycles = count,
        )
        if (ru.finni.financialpetgame.lct.domain.completedWeeks(periodBeforeCompletion, count) > 0) {
            val pendingDelta = checkNotNull(preferences[pendingExpenseDelta])
            if (pendingDelta != 0) {
                preferences[expense] = currentExpense + pendingDelta
                preferences[pendingExpenseDelta] = 0
            }
        }
        resetCycleAdjustments(preferences)
    }

    private fun recordOptionalExpense(
        preferences: MutablePreferences,
        amount: Int,
        title: String = "Покупка",
    ) {
        if (amount <= 0) return
        val period = checkNotNull(preferences[currentPeriod])
        if (preferences[budgetPlanWeek] != budgetWeekForPeriod(period)) return
        preferences[actualOptionalExpenses] =
            (preferences[actualOptionalExpenses] ?: 0) + amount
        val records = decodeBudgetExpenseRecords(preferences[optionalExpenseRecords]) +
            BudgetExpenseRecord(title = title, amount = amount)
        preferences[optionalExpenseRecords] = encodeBudgetExpenseRecords(records)
    }

    private fun budgetExpenseTitle(item: CardItem): String = context.getString(
        when (item) {
            FoodItem.SchoolLunch -> R.string.school_lunch
            FoodItem.Soda -> R.string.soda
            FoodItem.MashedPotatoes -> R.string.mashed_potatoes
            FoodItem.IceCream -> R.string.ice_cream
            FoodItem.HamburgerWithCola -> R.string.hamburger_cola
            FoodItem.Pasta -> R.string.pasta
            FoodItem.SetRolls -> R.string.set_rolls
            FoodItem.Pizza -> R.string.pizza
            HappinessItem.BudgetMaster -> R.string.master_budget
            HappinessItem.CatchMoney -> R.string.catch_money
            HappinessItem.ChangeMoney -> R.string.change_money
            HappinessItem.PlayWithBall -> R.string.ball_game
            HappinessItem.BoardGame -> R.string.board_game
            HappinessItem.MeetingWithFriends -> R.string.meeting_with_friends
            HappinessItem.Trip -> R.string.trip
            HappinessItem.Zoo -> R.string.zoo
            EnergyItem.TakeASeat -> R.string.seat
            EnergyItem.TakeANap -> R.string.take_a_nap
            EnergyItem.ListenMusic -> R.string.listen_music
            EnergyItem.TakeAMassage -> R.string.take_a_massage
            EnergyItem.HotBath -> R.string.bath
            EnergyItem.SPA -> R.string.spa
            EnergyItem.BodyMassage -> R.string.body_massage
            EnergyItem.Yoga -> R.string.yoga
            ShopItem.FlowerPot -> R.string.shop_flower_pot
            ShopItem.FavouriteMug -> R.string.shop_favourite_mug
            ShopItem.FloorLamp -> R.string.shop_floor_lamp
            ShopItem.SoftRug -> R.string.shop_soft_rug
            ShopItem.SoftArmchair -> R.string.shop_soft_armchair
            ShopItem.StylishScarf -> R.string.shop_stylish_scarf
            ShopItem.GlowingOrb -> R.string.shop_glowing_orb
            ShopItem.MorningAutomation -> R.string.shop_morning_automation
            else -> R.string.budget_optional_expenses
        },
    )

    private fun recordAdditionalIncome(preferences: MutablePreferences, amount: Int) {
        if (amount <= 0) return
        val period = checkNotNull(preferences[currentPeriod])
        if (preferences[budgetPlanWeek] != budgetWeekForPeriod(period)) return
        preferences[actualAdditionalIncome] =
            (preferences[actualAdditionalIncome] ?: 0) + amount
    }

    private fun recordGoalExpense(preferences: MutablePreferences, amount: Int) {
        if (amount <= 0) return
        val period = checkNotNull(preferences[currentPeriod])
        if (preferences[budgetPlanWeek] != budgetWeekForPeriod(period)) return
        preferences[actualGoalExpenses] = (preferences[actualGoalExpenses] ?: 0) + amount
    }

    private fun recordUnplannedExpense(
        preferences: MutablePreferences,
        amount: Int,
        title: String,
    ) {
        if (amount <= 0) return
        val period = checkNotNull(preferences[currentPeriod])
        if (preferences[budgetPlanWeek] != budgetWeekForPeriod(period)) return
        preferences[actualUnplannedExpenses] =
            (preferences[actualUnplannedExpenses] ?: 0) + amount
        val records = decodeBudgetExpenseRecords(preferences[unplannedExpenseRecords]) +
            BudgetExpenseRecord(title = title, amount = amount)
        preferences[unplannedExpenseRecords] = encodeBudgetExpenseRecords(records)
    }

    private fun applyEventDeltas(
        preferences: MutablePreferences,
        event: ScheduledEvent,
        secondsRemaining: Int,
    ): Boolean {
        val deltas = event.appliedDeltas
        val permanent = event.definition.frequency == EventFrequency.PermanentModifier
        listOf(
            Resource.Health to deltas.health,
            Resource.Happiness to deltas.happiness,
            Resource.Energy to deltas.energy,
        ).forEach { (resource, delta) ->
            val current = currentCharacteristic(preferences, resource, secondsRemaining)
            val appliedDelta = if (event.definition.kind == EventKind.Negative) {
                (current + delta).coerceAtLeast(0) - current
            } else {
                delta
            }
            applyCharacteristicEffect(preferences, resource, appliedDelta, permanent)
        }
        val currentMoney = checkNotNull(preferences[money])
        val resultingMoney = if (event.definition.kind == EventKind.Negative) {
            (currentMoney + deltas.money).coerceAtLeast(0)
        } else {
            currentMoney + deltas.money
        }
        preferences[money] = resultingMoney
        recordUnplannedExpense(
            preferences,
            (currentMoney - resultingMoney).coerceAtLeast(0),
            event.definition.title,
        )
        recordAdditionalIncome(preferences, deltas.money.coerceAtLeast(0))
        return resultingMoney == 0 && listOf(
            Resource.Health,
            Resource.Happiness,
            Resource.Energy,
        ).all { resource ->
            currentCharacteristic(preferences, resource, secondsRemaining) == 0
        }
    }

    private fun currentCharacteristic(
        preferences: Preferences,
        resource: Resource,
        secondsRemaining: Int,
    ): Int {
        if (resource == Resource.Money) return checkNotNull(preferences[money])
        return characteristicValue(
        baseValue = when (resource) {
            Resource.Health -> GameDefaults.HEALTH
            Resource.Happiness -> GameDefaults.HAPPINESS
            Resource.Energy -> GameDefaults.ENERGY
            Resource.Money -> 0
        },
        permanentModifier = when (resource) {
            Resource.Health -> checkNotNull(preferences[healthModifier])
            Resource.Happiness -> checkNotNull(preferences[happinessModifier])
            Resource.Energy -> checkNotNull(preferences[energyModifier])
            Resource.Money -> 0
        },
        cycleAdjustment = when (resource) {
            Resource.Health -> checkNotNull(preferences[healthCycleAdjustment])
            Resource.Happiness -> checkNotNull(preferences[happinessCycleAdjustment])
            Resource.Energy -> checkNotNull(preferences[energyCycleAdjustment])
            Resource.Money -> 0
        },
        secondsRemaining = secondsRemaining,
        )
    }

    private fun closeDepositEarly(preferences: MutablePreferences) {
        val amount = preferences[depositAmount] ?: return
        if (amount <= 0) return
        val deposit = ActiveDeposit(
            amount = amount,
            openedAtPeriod = preferences[depositOpenedAtPeriod] ?: 1,
            durationCycles = preferences[depositDurationCycles] ?: 3,
            interestPercent = preferences[depositInterestPercent] ?: 7,
            earlyClosePenaltyPercent = preferences[depositPenaltyPercent] ?: 4,
        )
        preferences[money] = checkNotNull(preferences[money]) + deposit.earlyClosePayout
        recordUnplannedExpense(
            preferences,
            deposit.earlyCloseLoss,
            context.getString(R.string.budget_deposit_penalty),
        )
        appendAction(preferences, "Вклад закрыт из-за критического события", deposit.earlyClosePayout)
        clearDeposit(preferences)
    }

    private fun createJobInstance(preferences: MutablePreferences, job: JobDef) {
        preferences[activeJobIds] = checkNotNull(preferences[activeJobIds]) + job.id
        preferences[jobRemainingActionsKey(job.id)] = job.remainingActions
        preferences[jobPayoutKey(job.id)] = job.payout
        preferences[jobHealthKey(job.id)] = job.statCost.health
        preferences[jobHappinessKey(job.id)] = job.statCost.happiness
        preferences[jobEnergyKey(job.id)] = job.statCost.energy
        preferences[jobAllowedCyclesKey(job.id)] = job.allowedCycles.name
        preferences[jobPeriodLimitKey(job.id)] = job.periodLimit ?: -1
        job.taskStorageId?.let { taskStorageId ->
            preferences[jobForTaskKey(taskStorageId)] = job.id
            preferences[eventUnlockedTaskIds] =
                checkNotNull(preferences[eventUnlockedTaskIds]) + taskStorageId
        }
    }

    private fun addFlag(preferences: MutablePreferences, flag: String) {
        preferences[eventFlags] = checkNotNull(preferences[eventFlags]) + flag
    }

    private fun consumeFlag(preferences: MutablePreferences, flag: String) {
        preferences[eventFlags] = checkNotNull(preferences[eventFlags]) - flag
    }

    private companion object {
        const val BUDGET_SUCCESS_REWARD = 10
        const val LEGACY_GOAL_TITLE = "GOAL"
        const val LEGACY_GOAL_TARGET = 1
        const val LEGACY_INCOME = 30
        const val LEGACY_EXPENSE = 10
        const val PREVIOUS_EXPENSE = 55
    }
}

private fun <T : Enum<T>> List<T>.findByName(name: String?, fallback: T): T =
    firstOrNull { it.name == name } ?: fallback

private fun Preferences.intMap(prefix: String): Map<String, Int> = asMap()
    .mapNotNull { (key, value) ->
        if (key.name.startsWith(prefix) && value is Int) {
            key.name.removePrefix(prefix) to value
        } else {
            null
        }
    }
    .toMap()

private fun Preferences.stringMap(prefix: String): Map<String, String> = asMap()
    .mapNotNull { (key, value) ->
        if (key.name.startsWith(prefix) && value is String) {
            key.name.removePrefix(prefix) to value
        } else {
            null
        }
    }
    .toMap()
