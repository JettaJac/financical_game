package com.example.financial_game.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financial_game.data.GameSnapshot
import com.example.financial_game.domain.CareItem
import com.example.financial_game.domain.GameRepository
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

enum class HomeOverlay { Menu, Shop }

enum class HomeSection { Food, Happiness, Energy, Shop, Tasks }

data class PetState(
    val resources: GameSnapshot = GameSnapshot(),
    val secondsRemaining: Int = TIMER_SECONDS,
    val selectedSection: HomeSection = HomeSection.Food,
    val overlay: HomeOverlay? = null,
    val exitRequested: Boolean = false,
    val isInitialized: Boolean = false,
)

sealed interface PetAction {
    data class SelectSection(val section: HomeSection) : PetAction
    data object OpenMenu : PetAction
    data object OpenShop : PetAction
    data object CloseOverlay : PetAction
    data object BuyCollar : PetAction
    data class BuyCareItem(val item: CareItem) : PetAction
    data object ForceNextCycle : PetAction
    data object Restart : PetAction

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
            PetAction.CloseOverlay -> _state.update { it.copy(overlay = null) }
            PetAction.Exit -> _state.update { it.copy(exitRequested = true) }
            PetAction.BuyCollar -> viewModelScope.launch { repository.buyCollar() }
            is PetAction.BuyCareItem -> viewModelScope.launch { repository.buyCareItem(action.item) }
            PetAction.ForceNextCycle -> nextCycle()
            PetAction.Restart -> restart()
        }
    }
}
