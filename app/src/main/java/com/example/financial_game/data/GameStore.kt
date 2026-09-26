package com.example.financial_game.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.financial_game.domain.GameDefaults
import com.example.financial_game.domain.GameRepository
import com.example.financial_game.domain.CardItem
import com.example.financial_game.domain.EyeColour
import com.example.financial_game.domain.Effect
import com.example.financial_game.domain.Goals
import com.example.financial_game.domain.HairColour
import com.example.financial_game.domain.HairStyle
import com.example.financial_game.domain.PetSetup
import com.example.financial_game.domain.Resource
import com.example.financial_game.domain.cooldownExpireAt
import com.example.financial_game.domain.levelAfterGoalPurchase
import com.example.financial_game.domain.moneyAfterCycle
import com.example.financial_game.domain.moneyAfterPurchase
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

private val Context.gameDataStore by preferencesDataStore("game")

private const val COOLDOWN_EXPIRE_PREFIX = "cooldown_expire_"

private fun cooldownExpireKey(item: CardItem) =
    longPreferencesKey("$COOLDOWN_EXPIRE_PREFIX${item.storageId}")

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
    private val income = intPreferencesKey("income")
    private val expense = intPreferencesKey("expense")
    private val level = intPreferencesKey("level")
    private val currentPeriod = intPreferencesKey("current_period")
    private val onboardingCompleted = booleanPreferencesKey("onboarding_completed")

    override val snapshot: Flow<GameSnapshot> = flow {
        emitAll(context.gameDataStore.data.map { preferences ->
            val cooldownExpires = preferences.asMap()
                .mapNotNull { (key, value) ->
                    if (key.name.startsWith(COOLDOWN_EXPIRE_PREFIX) && value is Long) {
                        key.name.removePrefix(COOLDOWN_EXPIRE_PREFIX) to value
                    } else {
                        null
                    }
                }
                .toMap()

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
                currentPeriod = checkNotNull(preferences[currentPeriod]),
                onboardingCompleted = checkNotNull(preferences[onboardingCompleted]),
                cooldownExpires = cooldownExpires,
            )
        })
    }

    override suspend fun initialize() {
        context.gameDataStore.edit { preferences ->
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
            if (preferences[income] == null) preferences[income] = GameDefaults.INCOME
            if (preferences[expense] == null) preferences[expense] = GameDefaults.EXPENSE
            if (preferences[goalTarget] == null) preferences[goalTarget] = GameDefaults.GOAL_TARGET
            if (preferences[level] == null) preferences[level] = GameDefaults.LEVEL
            if (preferences[currentPeriod] == null) preferences[currentPeriod] = GameDefaults.CURRENT_PERIOD
            if (preferences[onboardingCompleted] == null) preferences[onboardingCompleted] = false

            if (preferences[goalTitle] == LEGACY_GOAL_TITLE &&
                preferences[goalTarget] == LEGACY_GOAL_TARGET
            ) {
                preferences[goalTitle] = GameDefaults.GOAL
                preferences[goalTarget] = GameDefaults.GOAL_TARGET
            }
        }
    }

    override suspend fun reset() {
        context.gameDataStore.edit { preferences ->
            preferences.asMap().keys
                .filter { it.name.startsWith(COOLDOWN_EXPIRE_PREFIX) }
                .forEach { preferences.remove(longPreferencesKey(it.name)) }

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
            preferences[currentPeriod] = GameDefaults.CURRENT_PERIOD
            preferences[onboardingCompleted] = false
        }
    }

    override suspend fun completeTimerCycle() {
        context.gameDataStore.edit { preferences ->
            val currentMoney =  checkNotNull(preferences[money])
            val currentIncome = checkNotNull(preferences[income])
            val currentExpense = checkNotNull(preferences[expense])
            preferences[money] = moneyAfterCycle(currentMoney, currentIncome, currentExpense)

            val cp = checkNotNull(preferences[currentPeriod])
            preferences[currentPeriod] = cp + 1

            val e = checkNotNull(preferences[energy])
            val h = checkNotNull(preferences[happiness])
            val hea = checkNotNull(preferences[health])

            preferences[energy] = (e - 20).coerceAtLeast(0)
            preferences[happiness] = (h - 20).coerceAtLeast(0)
            preferences[health] = (hea - 20).coerceAtLeast(0)
        }
    }

    override suspend fun buyCollar() {
        context.gameDataStore.edit { preferences ->
            val currentMoney = checkNotNull(preferences[money])
            if (currentMoney >= COLLAR_PRICE) {
                preferences[money] = moneyAfterPurchase(currentMoney, COLLAR_PRICE)
            }
        }
    }

    override suspend fun buyCareItem(item: CardItem) {
        context.gameDataStore.edit { preferences ->
            val nowMillis = System.currentTimeMillis()
            val cooldownKey = cooldownExpireKey(item)
            val cooldownExpire = preferences[cooldownKey] ?: 0L
            if (cooldownExpire > nowMillis) return@edit

            val currentMoney = checkNotNull(preferences[money])
            if (currentMoney < item.price) return@edit

            preferences[money] = moneyAfterPurchase(currentMoney, item.price)

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

            preferences[cooldownKey] = cooldownExpireAt(nowMillis, item.coolDownSeconds)
        }
    }

    override suspend fun buyGoal(goal: Goals, price: Int): Boolean {
        val safePrice = price.coerceAtLeast(0)
        var purchased = false
        context.gameDataStore.edit { preferences ->
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
            purchased = true
        }
        return purchased
    }

    override suspend fun selectGoal(goal: Goals) {
        context.gameDataStore.edit { preferences ->
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
            preferences[onboardingCompleted] = true
        }
    }

    private companion object {
        const val COLLAR_PRICE = 100
        const val LEGACY_GOAL_TITLE = "GOAL"
        const val LEGACY_GOAL_TARGET = 1
    }
}

private fun <T : Enum<T>> List<T>.findByName(name: String?, fallback: T): T =
    firstOrNull { it.name == name } ?: fallback
