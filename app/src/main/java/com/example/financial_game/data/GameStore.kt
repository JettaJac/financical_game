package com.example.financial_game.data

import android.content.Context
import com.example.financial_game.R
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
import com.example.financial_game.domain.GameDefaults
import com.example.financial_game.domain.GameRepository
import com.example.financial_game.domain.CardItem
import com.example.financial_game.domain.Cooldown
import com.example.financial_game.domain.EyeColour
import com.example.financial_game.domain.Effect
import com.example.financial_game.domain.Goals
import com.example.financial_game.domain.HairColour
import com.example.financial_game.domain.HairStyle
import com.example.financial_game.domain.PetAppearance
import com.example.financial_game.domain.PET_APPEARANCE_CHANGE_PRICE
import com.example.financial_game.domain.PetSetup
import com.example.financial_game.domain.Resource
import com.example.financial_game.domain.ShopItem
import com.example.financial_game.domain.TaskItem
import com.example.financial_game.domain.cooldownUnlockCycle
import com.example.financial_game.domain.currentCyclePosition
import com.example.financial_game.domain.characteristicValue
import com.example.financial_game.domain.cycleSecondsRemaining
import com.example.financial_game.domain.hasCycleExpired
import com.example.financial_game.domain.nextCycleEnd
import com.example.financial_game.domain.levelAfterGoalPurchase
import com.example.financial_game.domain.moneyAfterPurchase
import com.example.financial_game.domain.moneyAfterCompletedCycles
import com.example.financial_game.domain.migratedCycleEnd
import com.example.financial_game.domain.isAvailable
import com.example.financial_game.domain.weekForPeriod
import com.example.financial_game.domain.withCharacteristicsAt
import com.example.financial_game.domain.budgetWeekForPeriod
import com.example.financial_game.domain.dayForPeriod
import com.example.financial_game.domain.MandatoryBudget
import com.example.financial_game.domain.BudgetPeriodResult
import com.example.financial_game.domain.ActiveDeposit
import com.example.financial_game.domain.DepositTerm
import com.example.financial_game.domain.events.EventFrequency
import com.example.financial_game.domain.events.JobDef
import com.example.financial_game.domain.events.ScheduledEvent
import com.example.financial_game.domain.events.canResolveScheduledEvent
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
private const val NEXT_PURCHASE_DISCOUNT_FLAG = "nextPurchaseHalfPrice"
private const val BRAIDED_BRACELETS_FLAG = "braidedBracelets"
private const val LEGACY_CYCLE_DURATION_SECONDS = 20 * 60
private const val MAX_ACTION_HISTORY_SIZE = 500

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
    private val plannedOptionalExpenses = intPreferencesKey("planned_optional_expenses")
    private val actualOptionalExpenses = intPreferencesKey("actual_optional_expenses")
    private val actualAdditionalIncome = intPreferencesKey("actual_additional_income")
    private val lastReviewedBudgetWeek = intPreferencesKey("last_reviewed_budget_week")
    private val budgetTutorialCompleted = booleanPreferencesKey("budget_tutorial_completed")
    private val homeTutorialCompleted = booleanPreferencesKey("home_tutorial_completed")
    private val demoModeEnabled = booleanPreferencesKey("demo_mode_enabled")
    private val eventFlags = stringSetPreferencesKey("event_flags")
    private val completedEventIds = stringSetPreferencesKey("completed_event_ids")
    private val eventPeriodOccurrences = stringSetPreferencesKey("event_period_occurrences")
    private val handledScenarioEntryIds = stringSetPreferencesKey("handled_scenario_entry_ids")
    private val eventPoolHandledCycles = stringSetPreferencesKey("event_pool_handled_cycles")
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
                plannedOptionalExpenses = checkNotNull(preferences[plannedOptionalExpenses]),
                actualOptionalExpenses = checkNotNull(preferences[actualOptionalExpenses]),
                actualAdditionalIncome = checkNotNull(preferences[actualAdditionalIncome]),
                lastReviewedBudgetWeek = checkNotNull(preferences[lastReviewedBudgetWeek]),
                budgetTutorialCompleted = checkNotNull(preferences[budgetTutorialCompleted]),
                homeTutorialCompleted = checkNotNull(preferences[homeTutorialCompleted]),
                demoModeEnabled = checkNotNull(preferences[demoModeEnabled]),
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
                activeJobIds = checkNotNull(preferences[activeJobIds]),
                jobRemainingActions = jobRemainingActions,
                actionHistory = decodeActionHistory(preferences[actionHistory]),
                customGoal = preferences[customGoalTitle]?.let { title ->
                    com.example.financial_game.data.CustomGoal(
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
            if (preferences[actualOptionalExpenses] == null) preferences[actualOptionalExpenses] = 0
            if (preferences[actualAdditionalIncome] == null) preferences[actualAdditionalIncome] = 0
            if (preferences[lastReviewedBudgetWeek] == null) {
                preferences[lastReviewedBudgetWeek] = 0
            }
            if (preferences[budgetTutorialCompleted] == null) {
                preferences[budgetTutorialCompleted] = false
            }
            if (preferences[homeTutorialCompleted] == null) {
                preferences[homeTutorialCompleted] = false
            }
            if (preferences[demoModeEnabled] == null) preferences[demoModeEnabled] = false
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
            if (preferences[activeJobIds] == null) preferences[activeJobIds] = emptySet()
            if (preferences[actionHistory] == null) preferences[actionHistory] = "[]"
            if (preferences[depositAmount] == null) preferences[depositAmount] = 0

            if (preferences[goalTitle] == LEGACY_GOAL_TITLE &&
                preferences[goalTarget] == LEGACY_GOAL_TARGET
            ) {
                preferences[goalTitle] = GameDefaults.GOAL
                preferences[goalTarget] = GameDefaults.GOAL_TARGET
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
                        it.name.startsWith(JOB_PERIOD_LIMIT_PREFIX)
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
            preferences[activeJobIds] = emptySet()
            preferences[actionHistory] = "[]"
            preferences[currentPeriod] = GameDefaults.CURRENT_PERIOD
            preferences[budgetPlanWeek] = 0
            preferences[plannedOptionalExpenses] = 0
            preferences[actualOptionalExpenses] = 0
            preferences[actualAdditionalIncome] = 0
            preferences[lastReviewedBudgetWeek] = 0
            preferences[budgetTutorialCompleted] = false
            preferences[homeTutorialCompleted] = false
            preferences[demoModeEnabled] = false
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
            recordAdditionalIncome(preferences, (payout - amount).coerceAtLeast(0))
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
            val purchasedShopItems = checkNotNull(preferences[purchasedShopItemIds])
            if (item is ShopItem && item.storageId in purchasedShopItems) return@edit
            val instanceJobId = if (item is TaskItem) {
                preferences[jobForTaskKey(item.storageId)]
            } else {
                null
            }
            if (item is TaskItem && characteristicValue(
                    baseValue = GameDefaults.ENERGY,
                    permanentModifier = checkNotNull(preferences[energyModifier]),
                    cycleAdjustment = checkNotNull(preferences[energyCycleAdjustment]),
                    secondsRemaining = secondsRemaining,
                ) <= 10
            ) return@edit
            if (item is TaskItem && !item.isAvailable(
                    currentPeriod = currentCycle,
                    totalUses = preferences[taskUseCountKey(item)] ?: 0,
                    weeklyUses = preferences[taskWeeklyUseCountKey(item)] ?: 0,
                    usageWeek = preferences[taskUseWeekKey(item)] ?: -1,
                    completedTaskCounts = preferences.intMap(TASK_USE_COUNT_PREFIX),
                    eventUnlocked = item.storageId in checkNotNull(preferences[eventUnlockedTaskIds]),
                )
            ) return@edit

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
            } else if (instanceJobId == null) {
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

            preferences[money] = moneyAfterPurchase(currentMoney, effectivePrice)
            recordOptionalExpense(preferences, effectivePrice)
            if (discounted) consumeFlag(preferences, NEXT_PURCHASE_DISCOUNT_FLAG)

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
                // A task used at any point in this cycle unlocks exactly at the next cycle boundary.
                preferences[cooldownUnlockCycleKey(item)] = currentCycle + 1.0
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
            appendAction(preferences, "Куплена цель «${context.getString(goal.titleRes)}»", -safePrice)
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
            val moneyChange = effects
                .filter { it.resource == Resource.Money }
                .sumOf(Effect::increase)
            val currentMoney = checkNotNull(preferences[money])
            if (currentMoney + moneyChange < 0) return@edit

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
            recordOptionalExpense(preferences, (-moneyChange).coerceAtLeast(0))
            recordAdditionalIncome(preferences, moneyChange.coerceAtLeast(0))
            appendAction(preferences, "Применён результат события", moneyChange)
        }
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
            recordOptionalExpense(preferences, PET_APPEARANCE_CHANGE_PRICE)
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
            preferences[budgetPlanWeek] = budgetWeekForPeriod(period)
            preferences[plannedOptionalExpenses] = optionalExpenses.coerceIn(
                minimumValue = 0,
                maximumValue = MandatoryBudget.maximumOptionalExpense,
            )
            preferences[actualOptionalExpenses] = 0
            preferences[actualAdditionalIncome] = 0
            preferences[budgetTutorialCompleted] = true
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
                plannedIncome = MandatoryBudget.income,
                actualIncome = MandatoryBudget.income +
                    (preferences[actualAdditionalIncome] ?: 0),
                plannedExpenses = MandatoryBudget.expense +
                    checkNotNull(preferences[plannedOptionalExpenses]),
                actualExpenses = MandatoryBudget.expense +
                    checkNotNull(preferences[actualOptionalExpenses]),
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
    ) {
        context.gameDataStore.edit { preferences ->
            val definition = event.definition
            val currentWeek = weekForPeriod(checkNotNull(preferences[currentPeriod])) + 1
            val occurrence = "${definition.id}@$currentWeek"
            val alreadyHandled = when (definition.frequency) {
                EventFrequency.Once,
                EventFrequency.PermanentModifier,
                -> definition.id in checkNotNull(preferences[completedEventIds])
                EventFrequency.PerPeriod -> occurrence in
                    checkNotNull(preferences[eventPeriodOccurrences])
            }
            if (alreadyHandled) return@edit
            if (!canResolveScheduledEvent(event, accepted, checkNotNull(preferences[money]))) {
                return@edit
            }
            if (accepted) {
                applyEventDeltas(preferences, event)
                preferences[eventFlags] =
                    checkNotNull(preferences[eventFlags]) + definition.setsFlags
                if (definition.permanentExpenseDelta != 0) {
                    preferences[expense] = checkNotNull(preferences[expense]) +
                        definition.permanentExpenseDelta
                }
                unlockedJob?.let { createJobInstance(preferences, it) }
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
                EventFrequency.Once,
                EventFrequency.PermanentModifier,
                -> preferences[completedEventIds] =
                    checkNotNull(preferences[completedEventIds]) + definition.id
                EventFrequency.PerPeriod -> preferences[eventPeriodOccurrences] =
                    checkNotNull(preferences[eventPeriodOccurrences]) + occurrence
            }
            appendAction(
                preferences,
                "Событие «${definition.title}»: ${if (accepted) "согласился" else "отказался"}",
                if (accepted) event.appliedDeltas.money else 0,
            )
        }
    }

    private fun advanceExpiredCycles(preferences: MutablePreferences, nowMillis: Long) {
        val currentCycleEnd = checkNotNull(preferences[cycleEndsAtMillis])
        if (!hasCycleExpired(currentCycleEnd, nowMillis)) return

        applyCompletedCycles(preferences, count = 1)
        appendAction(preferences, "Завершён цикл")
        preferences[cycleEndsAtMillis] = nextCycleEnd(nowMillis)
    }

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
        val nextCycle = checkNotNull(preferences[currentPeriod]) + 1.0
        TaskItem.entries.forEach { task ->
            val key = cooldownUnlockCycleKey(task)
            val savedUnlockCycle = preferences[key] ?: return@forEach
            if (savedUnlockCycle > nextCycle) preferences[key] = nextCycle
        }
    }

    private fun applyCharacteristicEffect(
        preferences: MutablePreferences,
        resource: Resource,
        change: Int,
        permanent: Boolean,
    ) {
        if (change == 0) return
        val key = when (resource) {
            Resource.Health -> if (permanent) healthModifier else healthCycleAdjustment
            Resource.Happiness -> if (permanent) happinessModifier else happinessCycleAdjustment
            Resource.Energy -> if (permanent) energyModifier else energyCycleAdjustment
            Resource.Money -> return
        }
        preferences[key] = (preferences[key] ?: 0) + change
    }

    private fun resetCycleAdjustments(preferences: MutablePreferences) {
        preferences[healthCycleAdjustment] = 0
        preferences[happinessCycleAdjustment] = 0
        preferences[energyCycleAdjustment] = 0
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
        resetCycleAdjustments(preferences)
    }

    private fun recordOptionalExpense(preferences: MutablePreferences, amount: Int) {
        if (amount <= 0) return
        val period = checkNotNull(preferences[currentPeriod])
        if (preferences[budgetPlanWeek] != budgetWeekForPeriod(period)) return
        preferences[actualOptionalExpenses] =
            (preferences[actualOptionalExpenses] ?: 0) + amount
    }

    private fun recordAdditionalIncome(preferences: MutablePreferences, amount: Int) {
        if (amount <= 0) return
        val period = checkNotNull(preferences[currentPeriod])
        if (preferences[budgetPlanWeek] != budgetWeekForPeriod(period)) return
        preferences[actualAdditionalIncome] =
            (preferences[actualAdditionalIncome] ?: 0) + amount
    }

    private fun applyEventDeltas(preferences: MutablePreferences, event: ScheduledEvent) {
        val deltas = event.appliedDeltas
        val permanent = event.definition.frequency == EventFrequency.PermanentModifier
        applyCharacteristicEffect(preferences, Resource.Health, deltas.health, permanent)
        applyCharacteristicEffect(
            preferences, Resource.Happiness, deltas.happiness, permanent,
        )
        applyCharacteristicEffect(preferences, Resource.Energy, deltas.energy, permanent)
        preferences[money] = checkNotNull(preferences[money]) + deltas.money
        recordOptionalExpense(preferences, (-deltas.money).coerceAtLeast(0))
        recordAdditionalIncome(preferences, deltas.money.coerceAtLeast(0))
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
