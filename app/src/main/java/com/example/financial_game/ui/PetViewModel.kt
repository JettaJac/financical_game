package com.example.financial_game.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financial_game.data.GameSnapshot
import com.example.financial_game.domain.CardItem
import com.example.financial_game.domain.GameEvent
import com.example.financial_game.domain.GameRepository
import com.example.financial_game.domain.PetSetup
import com.example.financial_game.domain.canApplyEffects
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal const val TIMER_SECONDS = 20 * 60
internal const val COLLAR_PRICE = 100

internal fun formatCountdown(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

enum class HomeOverlay { Menu, Shop, Goal }

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
    data object CloseOverlay : PetAction
    data class ShowEvent(val event: GameEvent) : PetAction
    data object AcceptEvent : PetAction
    data object DeclineEvent : PetAction
    data object SkipEvent : PetAction
    data object BuyCollar : PetAction
    data class BuyCareItem(val item: CardItem) : PetAction
    data object ForceNextCycle : PetAction
    data object Restart : PetAction
    data class CompleteOnboarding(val setup: PetSetup) : PetAction

    data object Exit : PetAction
}

@HiltViewModel
class PetViewModel @Inject constructor(private val repository: GameRepository) : ViewModel() {
    private val _state = MutableStateFlow(PetState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initialize()

            launch { runTimer() }

            launch {
                repository.snapshot.collect { resources ->
                    _state.update { it.copy(
                        resources = resources,
                        isInitialized = true,
                    ) }
                }
            }
        }
    }

    private suspend fun runTimer() {
            while (true) {
                delay(1_000)
                _state.update { it.copy(nowMillis = System.currentTimeMillis()) }
                if (!_state.value.resources.onboardingCompleted) continue
                if (_state.value.secondsRemaining > 1) {
                    _state.update { it.copy(secondsRemaining = it.secondsRemaining - 1) }
                } else {
                    _state.update { it.copy(secondsRemaining = 0) }
                    repository.completeTimerCycle()
                    _state.update { it.copy(secondsRemaining = TIMER_SECONDS) }
                }
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
            PetAction.ForceNextCycle -> nextCycle()
            PetAction.Restart -> restart()
            is PetAction.CompleteOnboarding -> viewModelScope.launch {
                repository.completeOnboarding(action.setup)
            }
        }
    }

    private fun resolveEvent(accept: Boolean) {
        val currentState = _state.value
        val event = currentState.activeEvent ?: return
        val effects = if (accept) event.acceptEffects else event.declineEffects
        if (!canApplyEffects(currentState.resources.money, effects)) return

        _state.update { it.copy(activeEvent = null) }
        viewModelScope.launch { repository.applyEffects(effects) }
    }
}
