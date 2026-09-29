package com.example.financial_game.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financial_game.R
import com.example.financial_game.data.GameSnapshot
import com.example.financial_game.data.EventCatalog
import com.example.financial_game.domain.CardItem
import com.example.financial_game.domain.Effect
import com.example.financial_game.domain.DepositTerm
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
import com.example.financial_game.domain.needsBudgetPlanning
import com.example.financial_game.domain.needsBudgetReview
import com.example.financial_game.domain.withCharacteristicsAt
import com.example.financial_game.domain.events.EventScheduler
import com.example.financial_game.domain.events.ScheduledEvent
import com.example.financial_game.domain.events.canResolveScheduledEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal const val TIMER_SECONDS = GameDefaults.CYCLE_DURATION_SECONDS
internal const val DEMO_MODE_PASSWORD = "1234"
internal const val PARENT_MODE_PASSWORD = "1234"
internal const val HOME_TUTORIAL_STEP_COUNT = 7
private val HOME_TUTORIAL_EVENT_STEPS = setOf(
    "w1d1_login_bonus",
    "w1d1_budget_difference",
)

internal fun formatCountdown(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}

internal fun eventElapsedCycleSeconds(remainingSeconds: Int, isTestMode: Boolean): Int =
    if (isTestMode) {
        GameDefaults.CYCLE_DURATION_SECONDS
    } else {
        (GameDefaults.CYCLE_DURATION_SECONDS - remainingSeconds).coerceAtLeast(0)
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

internal fun needsGoalSelection(snapshot: GameSnapshot): Boolean =
    snapshot.onboardingCompleted && snapshot.goalId in snapshot.purchasedGoalIds

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

enum class HomeOverlay { Menu, Budget, Deposit, Goal, GoalSelection, PersonalAccount, Parent }

enum class HomeSection { Food, Happiness, Energy, Shop, Tasks }

data class PetState(
    val resources: GameSnapshot = GameSnapshot(),
    val secondsRemaining: Int = TIMER_SECONDS,
    val selectedSection: HomeSection = HomeSection.Food,
    val overlay: HomeOverlay? = null,
    val activeEvent: GameEvent? = null,
    val exitRequested: Boolean = false,
    val isInitialized: Boolean = false,
    val isTestMode: Boolean = false,
    val demoModePasswordError: Boolean = false,
    val parentModePasswordError: Boolean = false,
    val homeTutorialStep: Int? = null,
    val demoModeHintVisible: Boolean = false,
    val nowMillis: Long = System.currentTimeMillis(),
)

sealed interface PetAction {
    data class SelectSection(val section: HomeSection) : PetAction
    data object OpenMenu : PetAction
    data object OpenBudget : PetAction
    data object OpenDeposit : PetAction
    data object OpenGoal : PetAction
    data object OpenPersonalAccount : PetAction
    data class OpenParent(val password: String) : PetAction
    data object ClearParentModePasswordError : PetAction
    data object CloseOverlay : PetAction
    data class ShowEvent(val event: GameEvent) : PetAction
    data object AcceptEvent : PetAction
    data object DeclineEvent : PetAction
    data object SkipEvent : PetAction
    data class BuyCareItem(val item: CardItem) : PetAction
    data class BuyGoal(val goal: Goals, val price: Int) : PetAction
    data class SelectGoal(val goal: Goals) : PetAction
    data object SelectCustomGoal : PetAction
    data class AddCustomGoal(val title: String, val target: Int, val illustrationRes: Int) : PetAction
    data object ForceNextCycle : PetAction
    data class EnableTestMode(val password: String) : PetAction
    data object ClearDemoModePasswordError : PetAction
    data object DisableTestMode : PetAction
    data object DismissDemoModeHint : PetAction
    data object Restart : PetAction
    data class CompleteOnboarding(val setup: PetSetup) : PetAction
    data class ApplyPetAppearance(val appearance: PetAppearance) : PetAction
    data class CompleteBudgetPlanning(val optionalExpenses: Int) : PetAction
    data object CompleteBudgetReview : PetAction
    data object AdvanceHomeTutorial : PetAction
    data class OpenDepositAccount(val term: DepositTerm, val amount: Int) : PetAction
    data object CloseDepositAccount : PetAction

    data object Exit : PetAction
}

@HiltViewModel
class PetViewModel @Inject constructor(
    private val repository: GameRepository,
    eventCatalog: EventCatalog,
) : ViewModel() {
    private val _state = MutableStateFlow(PetState())
    val state = _state.asStateFlow()
    private var goalPurchaseInProgress = false
    private var appearanceUpdateInProgress = false
    private val eventScheduler = EventScheduler(eventCatalog.data)

    init {
        viewModelScope.launch {
            repository.initialize()

            launch { runTimer() }

            launch {
                repository.snapshot.collect { resources ->
                    val previous = _state.value.resources.takeIf { _state.value.isInitialized }
                    val scheduledEvent = if (
                        !needsBudgetReview(resources) && !needsBudgetPlanning(resources)
                    ) {
                        val remainingSeconds = cycleSecondsRemaining(
                            resources.cycleEndsAtMillis,
                            System.currentTimeMillis(),
                        )
                        eventScheduler.next(
                            snapshot = resources,
                            elapsedCycleSeconds = eventElapsedCycleSeconds(
                                remainingSeconds = remainingSeconds,
                                isTestMode = _state.value.isTestMode,
                            ),
                        )?.asGameEvent()
                    } else {
                        null
                    }
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
                            isTestMode = resources.demoModeEnabled,
                            nowMillis = nowMillis,
                            overlay = if (scheduledEvent != null || reachedGoalEvent != null) {
                                null
                            } else if (needsGoalSelection(resources)) {
                                HomeOverlay.GoalSelection
                            } else {
                                it.overlay
                            },
                            activeEvent = it.activeEvent ?: scheduledEvent ?: reachedGoalEvent,
                            homeTutorialStep = when {
                                resources.homeTutorialCompleted -> null
                                it.homeTutorialStep != null -> it.homeTutorialStep
                                HOME_TUTORIAL_EVENT_STEPS.all(
                                    resources.handledScenarioEntryIds::contains,
                                ) -> 0
                                else -> null
                            },
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
            if (currentState.activeEvent != null) continue
            if (needsBudgetReview(currentState.resources)) continue
            if (needsBudgetPlanning(currentState.resources)) continue

            val secondsRemaining = cycleSecondsRemaining(
                currentState.resources.cycleEndsAtMillis,
                nowMillis,
            )
            _state.update {
                it.copy(
                    nowMillis = nowMillis,
                    secondsRemaining = secondsRemaining,
                    resources = it.resources.withCharacteristicsAt(secondsRemaining),
                )
            }

            val refreshedState = _state.value
            val scheduledEvent = eventScheduler.next(
                snapshot = refreshedState.resources,
                elapsedCycleSeconds = eventElapsedCycleSeconds(
                    remainingSeconds = secondsRemaining,
                    isTestMode = refreshedState.isTestMode,
                ),
            )?.asGameEvent()
            if (scheduledEvent != null) {
                _state.update {
                    if (it.activeEvent == null) {
                        it.copy(activeEvent = scheduledEvent, overlay = null)
                    } else {
                        it
                    }
                }
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
                    isTestMode = false,
                    demoModePasswordError = false,
                    parentModePasswordError = false,
                    homeTutorialStep = null,
                    demoModeHintVisible = false,
                )
            }
        }
    }

    private fun nextCycle() {
        val currentState = _state.value
        val pendingEvent = eventScheduler.next(
            snapshot = currentState.resources,
            elapsedCycleSeconds = GameDefaults.CYCLE_DURATION_SECONDS,
        )?.asGameEvent()
        if (pendingEvent != null) {
            _state.update { it.copy(activeEvent = pendingEvent, overlay = null) }
            return
        }

        viewModelScope.launch {
            repository.completeTimerCycle()
        }
    }

    fun onAction(action: PetAction) {
        when (action) {
            is PetAction.SelectSection -> _state.update { it.copy(selectedSection = action.section) }
            PetAction.OpenMenu -> _state.update { it.copy(overlay = HomeOverlay.Menu) }
            PetAction.OpenBudget -> _state.update { it.copy(overlay = HomeOverlay.Budget) }
            PetAction.OpenDeposit -> _state.update { it.copy(overlay = HomeOverlay.Deposit) }
            PetAction.OpenGoal -> _state.update {
                it.copy(
                    overlay = if (needsGoalSelection(it.resources)) {
                        HomeOverlay.GoalSelection
                    } else {
                        HomeOverlay.Goal
                    },
                )
            }
            PetAction.OpenPersonalAccount -> _state.update {
                it.copy(overlay = HomeOverlay.PersonalAccount)
            }
            is PetAction.OpenParent -> _state.update {
                if (action.password == PARENT_MODE_PASSWORD) {
                    it.copy(overlay = HomeOverlay.Parent, parentModePasswordError = false)
                } else {
                    it.copy(parentModePasswordError = true)
                }
            }
            PetAction.ClearParentModePasswordError -> _state.update {
                it.copy(parentModePasswordError = false)
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
            is PetAction.BuyCareItem -> viewModelScope.launch { repository.buyCareItem(action.item) }
            is PetAction.BuyGoal -> purchaseGoal(GoalPurchase(action.goal, action.price))
            is PetAction.SelectGoal -> viewModelScope.launch {
                repository.selectGoal(action.goal)
                _state.update { it.copy(overlay = null) }
            }
            PetAction.SelectCustomGoal -> viewModelScope.launch {
                repository.selectCustomGoal()
                _state.update { it.copy(overlay = null) }
            }
            is PetAction.AddCustomGoal -> viewModelScope.launch {
                repository.addCustomGoal(action.title, action.target, action.illustrationRes)
            }
            PetAction.ForceNextCycle -> if (_state.value.isTestMode) {
                nextCycle()
            }
            is PetAction.EnableTestMode -> {
                if (action.password == DEMO_MODE_PASSWORD) {
                    viewModelScope.launch {
                        repository.setDemoMode(true)
                        _state.update {
                            it.copy(
                                demoModePasswordError = false,
                                demoModeHintVisible = true,
                                overlay = null,
                            )
                        }
                    }
                } else {
                    _state.update { it.copy(demoModePasswordError = true) }
                }
            }
            PetAction.DisableTestMode -> viewModelScope.launch {
                repository.setDemoMode(false)
                _state.update {
                    it.copy(
                        isTestMode = false,
                        demoModeHintVisible = false,
                        overlay = null,
                    )
                }
            }
            PetAction.DismissDemoModeHint -> _state.update {
                it.copy(demoModeHintVisible = false)
            }
            PetAction.ClearDemoModePasswordError -> _state.update {
                it.copy(demoModePasswordError = false)
            }
            PetAction.Restart -> restart()
            is PetAction.CompleteOnboarding -> viewModelScope.launch {
                repository.completeOnboarding(action.setup)
            }
            is PetAction.ApplyPetAppearance -> updatePetAppearance(action.appearance)
            is PetAction.CompleteBudgetPlanning -> viewModelScope.launch {
                repository.saveBudgetPlan(action.optionalExpenses)
            }
            PetAction.CompleteBudgetReview -> viewModelScope.launch {
                repository.completeBudgetReview()
            }
            PetAction.AdvanceHomeTutorial -> advanceHomeTutorial()
            is PetAction.OpenDepositAccount -> viewModelScope.launch {
                repository.openDeposit(action.term, action.amount)
            }
            PetAction.CloseDepositAccount -> viewModelScope.launch {
                if (repository.closeDeposit()) {
                    _state.update { it.copy(overlay = null) }
                }
            }
        }
    }

    private fun advanceHomeTutorial() {
        _state.update { current ->
            val step = current.homeTutorialStep ?: return@update current
            if (step + 1 < HOME_TUTORIAL_STEP_COUNT) {
                current.copy(homeTutorialStep = step + 1)
            } else {
                viewModelScope.launch { repository.completeHomeTutorial() }
                current.copy(homeTutorialStep = null)
            }
        }
    }

    private fun resolveEvent(accept: Boolean) {
        val currentState = _state.value
        val event = currentState.activeEvent ?: return
        event.scheduledEvent?.let { scheduled ->
            if (!canResolveScheduledEvent(scheduled, accept, currentState.resources.money)) return
            _state.update { it.copy(activeEvent = null) }
            viewModelScope.launch {
                repository.resolveScheduledEvent(
                    event = scheduled,
                    unlockedJob = eventScheduler.jobFor(scheduled),
                    accepted = accept,
                )
            }
            return
        }
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

private fun ScheduledEvent.asGameEvent(): GameEvent = GameEvent(
    descriptionRes = R.string.app_name,
    descriptionText = definition.title,
    illustrationRes = definition.illustrationRes,
    acceptEffects = acceptEffects,
    canDecline = definition.canDecline,
    hideRewardUntilAccept = definition.hideRewardUntilAccept,
    scheduledEvent = this,
)
