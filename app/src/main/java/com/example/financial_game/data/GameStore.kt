package com.example.financial_game.data

import android.content.Context
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
import com.example.financial_game.domain.cycleSecondsRemaining
import com.example.financial_game.domain.hasCycleExpired
import com.example.financial_game.domain.nextCycleEnd
import com.example.financial_game.domain.levelAfterGoalPurchase
import com.example.financial_game.domain.moneyAfterPurchase
import com.example.financial_game.domain.moneyAfterCompletedCycles
import com.example.financial_game.domain.isAvailable
import com.example.financial_game.domain.weekForPeriod
import com.example.financial_game.domain.budgetWeekForPeriod
import com.example.financial_game.domain.dayForPeriod
import com.example.financial_game.domain.MandatoryBudget
import com.example.financial_game.domain.BudgetPeriodResult
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

private val Context.gameDataStore by preferencesDataStore("game")

private const val COOLDOWN_UNLOCK_CYCLE_PREFIX = "cooldown_unlock_cycle_"
private const val LEGACY_COOLDOWN_EXPIRE_PREFIX = "cooldown_expire_"
private const val LEGACY_COOLDOWN_PERIOD_PREFIX = "cooldown_period_"
private const val TASK_USE_COUNT_PREFIX = "task_use_count_"
private const val TASK_WEEKLY_USE_COUNT_PREFIX = "task_weekly_use_count_"
private const val TASK_USE_WEEK_PREFIX = "task_use_week_"

private fun cooldownUnlockCycleKey(storageId: String) =
    doublePreferencesKey("$COOLDOWN_UNLOCK_CYCLE_PREFIX$storageId")

private fun cooldownUnlockCycleKey(item: CardItem) = cooldownUnlockCycleKey(item.storageId)

private fun taskUseCountKey(item: TaskItem) =
    intPreferencesKey("$TASK_USE_COUNT_PREFIX${item.storageId}")

private fun taskWeeklyUseCountKey(item: TaskItem) =
    intPreferencesKey("$TASK_WEEKLY_USE_COUNT_PREFIX${item.storageId}")

private fun taskUseWeekKey(item: TaskItem) =
    intPreferencesKey("$TASK_USE_WEEK_PREFIX${item.storageId}")

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
    private val onboardingCompleted = booleanPreferencesKey("onboarding_completed")
    private val eventUnlockedTaskIds = stringSetPreferencesKey("event_unlocked_task_ids")
    private val budgetPlanWeek = intPreferencesKey("budget_plan_week")
    private val plannedOptionalExpenses = intPreferencesKey("planned_optional_expenses")
    private val actualOptionalExpenses = intPreferencesKey("actual_optional_expenses")
    private val actualAdditionalIncome = intPreferencesKey("actual_additional_income")
    private val lastReviewedBudgetWeek = intPreferencesKey("last_reviewed_budget_week")
    private val budgetTutorialCompleted = booleanPreferencesKey("budget_tutorial_completed")

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

            GameSnapshot(
                name = checkNotNull(preferences[petName]),
                hairColour = HairColour.entries.findByName(preferences[hairColour], HairColour.Beige),
                eyeColour = EyeColour.entries.findByName(preferences[eyeColour], EyeColour.Violet),
                hairStyle = HairStyle.entries.findByName(preferences[hairStyle], HairStyle.Default),
                money = checkNotNull(preferences[money]),
                health = (checkNotNull(preferences[health])).coerceIn(0, 100),
                happiness = (checkNotNull(preferences[happiness])).coerceIn(0, 100),
                energy = (checkNotNull(preferences[energy])).coerceIn(0, 100),
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
                cooldownUnlockCycles = cooldownUnlockCycles,
                taskUseCounts = taskUseCounts,
                taskWeeklyUseCounts = taskWeeklyUseCounts,
                taskUseWeeks = taskUseWeeks,
                eventUnlockedTaskIds = checkNotNull(preferences[eventUnlockedTaskIds]),
            )
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
            }
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
                        it.name.startsWith(TASK_USE_WEEK_PREFIX)
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
            preferences[goalTitle] = GameDefaults.GOAL
            preferences[goalTarget] = GameDefaults.GOAL_TARGET
            preferences[goalId] = Goals.Pillow.name
            preferences[income] = GameDefaults.INCOME
            preferences[expense] = GameDefaults.EXPENSE
            preferences[level] = GameDefaults.LEVEL
            preferences[purchasedGoalIds] = emptySet()
            preferences[purchasedShopItemIds] = emptySet()
            preferences[eventUnlockedTaskIds] = emptySet()
            preferences[currentPeriod] = GameDefaults.CURRENT_PERIOD
            preferences[budgetPlanWeek] = 0
            preferences[plannedOptionalExpenses] = 0
            preferences[actualOptionalExpenses] = 0
            preferences[actualAdditionalIncome] = 0
            preferences[lastReviewedBudgetWeek] = 0
            preferences[budgetTutorialCompleted] = false
            preferences[cycleEndsAtMillis] =
                System.currentTimeMillis() + GameDefaults.CYCLE_DURATION_MILLIS
            preferences[onboardingCompleted] = false
        }
    }

    override suspend fun completeTimerCycle() {
        context.gameDataStore.edit { preferences ->
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

    override suspend fun buyCollar() {
        context.gameDataStore.edit { preferences ->
            val currentMoney = checkNotNull(preferences[money])
            if (currentMoney >= COLLAR_PRICE) {
                preferences[money] = moneyAfterPurchase(currentMoney, COLLAR_PRICE)
                recordOptionalExpense(preferences, COLLAR_PRICE)
            }
        }
    }

    override suspend fun buyCareItem(item: CardItem) {
        context.gameDataStore.edit { preferences ->
            val currentCycle = checkNotNull(preferences[currentPeriod])
            val currentCyclePosition = currentCyclePosition(
                currentPeriod = currentCycle,
                cycleSecondsRemaining = cycleSecondsRemaining(
                    cycleEndsAtMillis = checkNotNull(preferences[cycleEndsAtMillis]),
                    nowMillis = System.currentTimeMillis(),
                ),
            )
            val purchasedShopItems = checkNotNull(preferences[purchasedShopItemIds])
            if (item is ShopItem && item.storageId in purchasedShopItems) return@edit
            if (item is TaskItem && !item.isAvailable(
                    currentPeriod = currentCycle,
                    totalUses = preferences[taskUseCountKey(item)] ?: 0,
                    weeklyUses = preferences[taskWeeklyUseCountKey(item)] ?: 0,
                    usageWeek = preferences[taskUseWeekKey(item)] ?: -1,
                    completedTaskCounts = preferences.intMap(TASK_USE_COUNT_PREFIX),
                    eventUnlocked = item.storageId in checkNotNull(preferences[eventUnlockedTaskIds]),
                )
            ) return@edit

            when (item.cooldown) {
                Cooldown.None -> Unit
                is Cooldown.Cycles -> {
                    val unlockCycle = preferences[cooldownUnlockCycleKey(item)] ?: 0.0
                    if (unlockCycle > currentCyclePosition) return@edit
                }
            }

            val currentLevel = checkNotNull(preferences[level])
            if (currentLevel < item.level) return@edit

            val currentMoney = checkNotNull(preferences[money])
            if (currentMoney < item.price) return@edit

            preferences[money] = moneyAfterPurchase(currentMoney, item.price)
            recordOptionalExpense(preferences, item.price)

            for (effect in item.careEffects) {
                when (effect.resource) {
                    Resource.Health -> preferences[health] =
                        (checkNotNull(preferences[health]) + effect.increase).coerceIn(0, 100)
                    Resource.Happiness -> preferences[happiness] =
                        (checkNotNull(preferences[happiness]) + effect.increase).coerceIn(0, 100)
                    Resource.Energy -> preferences[energy] =
                        (checkNotNull(preferences[energy]) + effect.increase).coerceIn(0, 100)
                    Resource.Money -> preferences[money] =
                        checkNotNull(preferences[money]) + effect.increase
                }
            }
            recordAdditionalIncome(
                preferences,
                item.careEffects
                    .filter { it.resource == Resource.Money }
                    .sumOf(Effect::increase)
                    .coerceAtLeast(0),
            )

            when (val cooldown = item.cooldown) {
                Cooldown.None -> Unit
                is Cooldown.Cycles -> {
                    preferences[cooldownUnlockCycleKey(item)] = cooldownUnlockCycle(
                        currentCyclePosition = currentCyclePosition,
                        cooldownCycles = cooldown.value,
                    )
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
            }
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
                    Resource.Health -> preferences[health] =
                        (checkNotNull(preferences[health]) + effect.increase).coerceIn(0, 100)
                    Resource.Happiness -> preferences[happiness] =
                        (checkNotNull(preferences[happiness]) + effect.increase).coerceIn(0, 100)
                    Resource.Energy -> preferences[energy] =
                        (checkNotNull(preferences[energy]) + effect.increase).coerceIn(0, 100)
                    Resource.Money -> preferences[money] =
                        checkNotNull(preferences[money]) + effect.increase
                }
            }

            val currentLevel = checkNotNull(preferences[level])
            preferences[level] = levelAfterGoalPurchase(currentLevel, goal.level)
            preferences[purchasedGoalIds] = purchasedGoals + goal.name
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
                    Resource.Health -> preferences[health] =
                        (checkNotNull(preferences[health]) + change).coerceIn(0, 100)
                    Resource.Happiness -> preferences[happiness] =
                        (checkNotNull(preferences[happiness]) + change).coerceIn(0, 100)
                    Resource.Energy -> preferences[energy] =
                        (checkNotNull(preferences[energy]) + change).coerceIn(0, 100)
                    Resource.Money -> preferences[money] = currentMoney + change
                }
            }
            recordOptionalExpense(preferences, (-moneyChange).coerceAtLeast(0))
            recordAdditionalIncome(preferences, moneyChange.coerceAtLeast(0))
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
            preferences[onboardingCompleted] = true
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
        }
    }

    private fun advanceExpiredCycles(preferences: MutablePreferences, nowMillis: Long) {
        val currentCycleEnd = checkNotNull(preferences[cycleEndsAtMillis])
        if (!hasCycleExpired(currentCycleEnd, nowMillis)) return

        applyCompletedCycles(preferences, count = 1)
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

        val resourceDecrease = 20 * count
        preferences[energy] =
            (checkNotNull(preferences[energy]) - resourceDecrease).coerceAtLeast(0)
        preferences[happiness] =
            (checkNotNull(preferences[happiness]) - resourceDecrease).coerceAtLeast(0)
        preferences[health] =
            (checkNotNull(preferences[health]) - resourceDecrease).coerceAtLeast(0)
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

    private companion object {
        const val COLLAR_PRICE = 100
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
