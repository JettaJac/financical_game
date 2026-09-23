package com.example.financial_game.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.financial_game.domain.GameDefaults
import com.example.financial_game.domain.GameRepository
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

@Singleton
class GameStore @Inject constructor(@ApplicationContext private val context: Context) : GameRepository {
    private val money = intPreferencesKey("money")
    private val health = intPreferencesKey("health")
    private val happiness = intPreferencesKey("happiness")
    private val energy = intPreferencesKey("energy")
    private val goalTitle = stringPreferencesKey("goal_title")
    private val goalTarget = intPreferencesKey("goal_target")
    private val income = intPreferencesKey("income")
    private val expense = intPreferencesKey("expense")
    private val level = intPreferencesKey("level")
    private val currentPeriod = intPreferencesKey("current_period")




    override val snapshot: Flow<GameSnapshot> = flow {
        emitAll(context.gameDataStore.data.map { preferences ->
            GameSnapshot(
                money = checkNotNull(preferences[money]),
                health = (checkNotNull(preferences[health])).coerceIn(0, 100),
                happiness = (checkNotNull(preferences[happiness])).coerceIn(0, 100),
                energy = (checkNotNull(preferences[energy])).coerceIn(0, 100),
                goalTitle = checkNotNull(preferences[goalTitle]),
                income = checkNotNull(preferences[income]),
                expense = checkNotNull(preferences[expense]),
                goalTarget = checkNotNull(preferences[goalTarget]),
                level = checkNotNull(preferences[level]),
                currentPeriod = checkNotNull(preferences[currentPeriod]),
            )
        })
    }

    override suspend fun initialize() {
        context.gameDataStore.edit { preferences ->
            if (preferences[money] == null) preferences[money] = GameDefaults.MONEY
            if (preferences[health] == null) preferences[health] = GameDefaults.HEALTH
            if (preferences[happiness] == null) preferences[happiness] = GameDefaults.HAPPINESS
            if (preferences[energy] == null) preferences[energy] = GameDefaults.ENERGY
            if (preferences[goalTitle] == null) preferences[goalTitle] = GameDefaults.GOAL
            if (preferences[income] == null) preferences[income] = GameDefaults.INCOME
            if (preferences[expense] == null) preferences[expense] = GameDefaults.EXPENSE
            if (preferences[goalTarget] == null) preferences[goalTarget] = GameDefaults.GOAL_TARGET
            if (preferences[level] == null) preferences[level] = GameDefaults.LEVEL
            if (preferences[currentPeriod] == null) preferences[currentPeriod] = GameDefaults.CURRENT_PERIOD
        }
    }

    override suspend fun reset() {
        context.gameDataStore.edit { preferences ->
            preferences[money] = GameDefaults.MONEY
            preferences[health] = GameDefaults.HEALTH
            preferences[happiness] = GameDefaults.HAPPINESS
            preferences[energy] = GameDefaults.ENERGY
            preferences[goalTitle] = GameDefaults.GOAL
            preferences[goalTarget] = GameDefaults.GOAL_TARGET
            preferences[income] = GameDefaults.INCOME
            preferences[expense] = GameDefaults.EXPENSE
            preferences[level] = GameDefaults.LEVEL
            preferences[currentPeriod] = GameDefaults.CURRENT_PERIOD
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
                val h = checkNotNull(preferences[health])
                preferences[health] = h + 40
            }
        }
    }

    private companion object {
        const val COLLAR_PRICE = 100
    }
}
