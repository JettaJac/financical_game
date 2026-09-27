package com.example.financial_game.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financial_game.R
import com.example.financial_game.data.GameSnapshot
import com.example.financial_game.domain.CardItem
import com.example.financial_game.domain.Effect
import com.example.financial_game.domain.GameEvent
import com.example.financial_game.domain.GameDefaults
import com.example.financial_game.domain.GameRepository
import com.example.financial_game.domain.GoalPurchase
import com.example.financial_game.domain.Goals
import com.example.financial_game.domain.PetAppearance
import com.example.financial_game.domain.PetSetup
import com.example.financial_game.domain.Resource
import com.example.financial_game.domain.canApplyEffects
import com.example.financial_game.domain.cycleSecondsRemaining
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal const val TIMER_SECONDS = GameDefaults.CYCLE_DURATION_SECONDS
internal const val COLLAR_PRICE = 100

internal fun formatCountdown(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

internal fun hasJustReachedGoal(previous: GameSnapshot?, current: GameSnapshot): Boolean {
    if (!current.onboardingCompleted || current.goalTarget <= 0) return false
    if (current.goalId in current.purchasedGoalIds) return false
    if (current.money < current.goalTarget) return false
    if (previous == null || !previous.onboardingCompleted || previous.goalId != current.goalId) {
        return true
    }
    return previous.money < previous.goalTarget
}

internal fun goalPurchaseEvent(resources: GameSnapshot): GameEvent {
    val goal = Goals.fromStorageId(resources.goalId)
    return GameEvent(
        descriptionRes = goal.titleRes,
        illustrationRes = goal.illustrationRes,
        acceptEffects = goal.goalEffects + Effect(Resource.Money, -resources.goalTarget),
        showCloseButton = true,
        acceptButtonTextRes = R.string.buy,
        goalPurchase = GoalPurchase(goal = goal, price = resources.goalTarget),
    )
}

enum class HomeOverlay { Menu, Shop, Goal, GoalSelection, PersonalAccount }

enum class HomeSection { Food, Happiness, Energy, Shop, Tasks }

data class PetState(
    val resources: GameSnapshot = GameSnapshot(),
    val secondsRemaining: Int = TIMER_SECONDS,
    val selectedSection: HomeSection = HomeSection.Food,
    val overlay: HomeOverlay? = null,
    val activeEvent: GameEvent? = null,
    val exitRequested: Boolean = false,
    val isInitialized: Boolean = false,
    val nowMillis: Long = System.currentTimeMillis(),
)

sealed interface PetAction {
    data class SelectSection(val section: HomeSection) : PetAction
    data object OpenMenu : PetAction
    data object OpenShop : PetAction
    data object OpenGoal : PetAction
    data object OpenPersonalAccount : PetAction
    data object CloseOverlay : PetAction
    data class ShowEvent(val event: GameEvent) : PetAction
    data object AcceptEvent : PetAction
    data object DeclineEvent : PetAction
    data object SkipEvent : PetAction
    data object BuyCollar : PetAction
    data class BuyCareItem(val item: CardItem) : PetAction
    data class BuyGoal(val goal: Goals, val price: Int) : PetAction
    data class SelectGoal(val goal: Goals) : PetAction
    data object ForceNextCycle : PetAction
    data object Restart : PetAction
    data class CompleteOnboarding(val setup: PetSetup) : PetAction
    data class ApplyPetAppearance(val appearance: PetAppearance) : PetAction

    data object Exit : PetAction
}

@HiltViewModel
class PetViewModel @Inject constructor(private val repository: GameRepository) : ViewModel() {
    private val _state = MutableStateFlow(PetState())
    val state = _state.asStateFlow()
    private var goalPurchaseInProgress = false
    private var appearanceUpdateInProgress = false

    init {
        viewModelScope.launch {
            repository.initialize()

            launch { runTimer() }

            launch {
                repository.snapshot.collect { resources ->
                    val previous = _state.value.resources.takeIf { _state.value.isInitialized }
                    val reachedGoalEvent = if (hasJustReachedGoal(previous, resources)) {
                        goalPurchaseEvent(resources)
                    } else {
                        null
                    }
                    _state.update {
                        val nowMillis = System.currentTimeMillis()
                        it.copy(
                            resources = resources,
                            secondsRemaining = cycleSecondsRemaining(
                                resources.cycleEndsAtMillis,
                                nowMillis,
                            ),
                            isInitialized = true,
                            nowMillis = nowMillis,
                            overlay = if (reachedGoalEvent != null) null else it.overlay,
                            activeEvent = reachedGoalEvent ?: it.activeEvent,
                        )
                    }
                }
            }
        }
    }

    private suspend fun runTimer() {
        while (true) {
            delay(1_000)
            val nowMillis = System.currentTimeMillis()
            val currentState = _state.value
            if (!currentState.resources.onboardingCompleted) continue

            val secondsRemaining = cycleSecondsRemaining(
                currentState.resources.cycleEndsAtMillis,
                nowMillis,
            )
            _state.update {
                it.copy(
                    nowMillis = nowMillis,
                    secondsRemaining = secondsRemaining,
                )
            }

            if (secondsRemaining == 0) repository.advanceExpiredCycles(nowMillis)
        }
    }

    private fun restart() {
        viewModelScope.launch {
            repository.reset()

            _state.update {
                it.copy(
                    secondsRemaining = TIMER_SECONDS,
                    selectedSection = HomeSection.Food,
                    overlay = null,
                    activeEvent = null,
                    exitRequested = false,
                )
            }
        }
    }

    private fun nextCycle() {
        viewModelScope.launch {
            repository.completeTimerCycle()
        }
    }

    fun onAction(action: PetAction) {
        when (action) {
            is PetAction.SelectSection -> _state.update { it.copy(selectedSection = action.section) }
            PetAction.OpenMenu -> _state.update { it.copy(overlay = HomeOverlay.Menu) }
            PetAction.OpenShop -> _state.update { it.copy(overlay = HomeOverlay.Shop) }
            PetAction.OpenGoal -> _state.update { it.copy(overlay = HomeOverlay.Goal) }
            PetAction.OpenPersonalAccount -> _state.update {
                it.copy(overlay = HomeOverlay.PersonalAccount)
            }
            PetAction.CloseOverlay -> _state.update { it.copy(overlay = null) }
            is PetAction.ShowEvent -> _state.update {
                it.copy(overlay = null, activeEvent = action.event)
            }
            PetAction.AcceptEvent -> resolveEvent(accept = true)
            PetAction.DeclineEvent -> resolveEvent(accept = false)
            PetAction.SkipEvent -> _state.update {
                if (it.activeEvent?.showCloseButton == true) it.copy(activeEvent = null) else it
            }
            PetAction.Exit -> _state.update { it.copy(exitRequested = true) }
            PetAction.BuyCollar -> viewModelScope.launch { repository.buyCollar() }
            is PetAction.BuyCareItem -> viewModelScope.launch { repository.buyCareItem(action.item) }
            is PetAction.BuyGoal -> purchaseGoal(GoalPurchase(action.goal, action.price))
            is PetAction.SelectGoal -> viewModelScope.launch {
                repository.selectGoal(action.goal)
                _state.update { it.copy(overlay = null) }
            }
            PetAction.ForceNextCycle -> nextCycle()
            PetAction.Restart -> restart()
            is PetAction.CompleteOnboarding -> viewModelScope.launch {
                repository.completeOnboarding(action.setup)
            }
            is PetAction.ApplyPetAppearance -> updatePetAppearance(action.appearance)
        }
    }

    private fun resolveEvent(accept: Boolean) {
        val currentState = _state.value
        val event = currentState.activeEvent ?: return
        if (accept && event.goalPurchase != null) {
            purchaseGoal(event.goalPurchase)
            return
        }
        val effects = if (accept) event.acceptEffects else event.declineEffects
        if (!canApplyEffects(currentState.resources.money, effects)) return

        _state.update { it.copy(activeEvent = null) }
        viewModelScope.launch { repository.applyEffects(effects) }
    }

    private fun purchaseGoal(purchase: GoalPurchase) {
        if (goalPurchaseInProgress) return
        goalPurchaseInProgress = true
        viewModelScope.launch {
            try {
                if (repository.buyGoal(purchase.goal, purchase.price)) {
                    _state.update {
                        it.copy(
                            activeEvent = null,
                            overlay = HomeOverlay.GoalSelection,
                        )
                    }
                }
            } finally {
                goalPurchaseInProgress = false
            }
        }
    }

    private fun updatePetAppearance(appearance: PetAppearance) {
        if (appearanceUpdateInProgress) return
        appearanceUpdateInProgress = true
        viewModelScope.launch {
            try {
                if (repository.updatePetAppearance(appearance)) {
                    _state.update { it.copy(overlay = null) }
                }
            } finally {
                appearanceUpdateInProgress = false
            }
        }
    }
}
