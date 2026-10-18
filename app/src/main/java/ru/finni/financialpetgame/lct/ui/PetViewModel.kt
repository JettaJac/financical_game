package ru.finni.financialpetgame.lct.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ru.finni.financialpetgame.lct.R
import ru.finni.financialpetgame.lct.data.GameSnapshot
import ru.finni.financialpetgame.lct.data.EventCatalog
import ru.finni.financialpetgame.lct.domain.CardItem
import ru.finni.financialpetgame.lct.domain.BUDGET_MASTER_ROUND_SECONDS
import ru.finni.financialpetgame.lct.domain.BUDGET_MASTER_SUCCESS_SCORE
import ru.finni.financialpetgame.lct.domain.BudgetDirection
import ru.finni.financialpetgame.lct.domain.BudgetMasterCard
import ru.finni.financialpetgame.lct.domain.BudgetMasterCategory
import ru.finni.financialpetgame.lct.domain.ChangeMoneyChallenge
import ru.finni.financialpetgame.lct.domain.ChangeMoneyValidation
import ru.finni.financialpetgame.lct.domain.Effect
import ru.finni.financialpetgame.lct.domain.DepositTerm
import ru.finni.financialpetgame.lct.domain.GameEvent
import ru.finni.financialpetgame.lct.domain.GameDefaults
import ru.finni.financialpetgame.lct.domain.GameRepository
import ru.finni.financialpetgame.lct.domain.GoalPurchase
import ru.finni.financialpetgame.lct.domain.Goals
import ru.finni.financialpetgame.lct.domain.HappinessItem
import ru.finni.financialpetgame.lct.domain.PetAppearance
import ru.finni.financialpetgame.lct.domain.PetSetup
import ru.finni.financialpetgame.lct.domain.Resource
import ru.finni.financialpetgame.lct.domain.TaskItem
import ru.finni.financialpetgame.lct.domain.canApplyEffects
import ru.finni.financialpetgame.lct.domain.cycleSecondsRemaining
import ru.finni.financialpetgame.lct.domain.needsBudgetPlanning
import ru.finni.financialpetgame.lct.domain.needsBudgetReview
import ru.finni.financialpetgame.lct.domain.randomChangeMoneyChallenge
import ru.finni.financialpetgame.lct.domain.randomBudgetMasterCard
import ru.finni.financialpetgame.lct.domain.isCorrectBudgetCategory
import ru.finni.financialpetgame.lct.domain.isCorrectBudgetDirection
import ru.finni.financialpetgame.lct.domain.validateChangeMoneySelection
import ru.finni.financialpetgame.lct.domain.withCharacteristicsAt
import ru.finni.financialpetgame.lct.domain.weekForPeriod
import ru.finni.financialpetgame.lct.domain.dayForPeriod
import ru.finni.financialpetgame.lct.domain.events.EventScheduler
import ru.finni.financialpetgame.lct.domain.events.EventFrequency
import ru.finni.financialpetgame.lct.domain.events.ScheduledEvent
import ru.finni.financialpetgame.lct.domain.events.canResolveScheduledEvent
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
internal const val HOME_TUTORIAL_STEP_COUNT = 11
internal const val CYCLE_ENERGY_PROMPT_REPEAT_MILLIS = 15_000L
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

internal fun isScheduledEventHandled(event: ScheduledEvent, snapshot: GameSnapshot): Boolean {
    event.scenarioStepId?.let { return it in snapshot.handledScenarioEntryIds }
    if (!event.isScripted) {
        return snapshot.currentPeriod.toString() in snapshot.eventPoolHandledCycles
    }
    if (event.definition.id in snapshot.completedEventIds) return true
    if (
        event.definition.frequency == EventFrequency.PerPeriod &&
        "${event.definition.id}@${weekForPeriod(snapshot.currentPeriod) + 1}" in
            snapshot.eventPeriodOccurrences
    ) {
        return true
    }
    return snapshot.eventRetryPeriods[event.definition.id]?.let { retryPeriod ->
        retryPeriod > snapshot.currentPeriod
    } == true
}

internal fun goalPurchaseEvent(resources: GameSnapshot): GameEvent {
    if (resources.goalId == "custom") {
        val customGoal = resources.customGoal
        return GameEvent(
            descriptionRes = R.string.app_name,
            descriptionText = customGoal?.title ?: resources.goalTitle,
            illustrationRes = customGoal?.illustrationRes ?: R.drawable.goal_pillow,
            acceptEffects = listOf(Effect(Resource.Money, -resources.goalTarget)),
            showCloseButton = true,
            acceptButtonTextRes = R.string.buy,
            customGoalPurchase = true,
        )
    }
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

enum class ChangeMoneyStage { Intro, Rules, Ready, Playing, Success, Result }

enum class BudgetMasterStage { Intro, Rules, Playing, Result }

enum class BudgetMasterStep { Direction, Category }

data class BudgetMasterGameState(
    val card: BudgetMasterCard,
    val stage: BudgetMasterStage,
    val step: BudgetMasterStep = BudgetMasterStep.Direction,
    val score: Int = 0,
    val secondsRemaining: Int = BUDGET_MASTER_ROUND_SECONDS,
    val usedCardIds: Set<String> = emptySet(),
    val errorCategory: BudgetMasterCategory? = null,
    val pauseDialogVisible: Boolean = false,
)

data class ChangeMoneyGameState(
    val challenge: ChangeMoneyChallenge,
    val stage: ChangeMoneyStage,
    val selectedCounts: Map<Int, Int> = emptyMap(),
    val validation: ChangeMoneyValidation? = null,
    val elapsedMillis: Long = 0L,
    val pauseDialogVisible: Boolean = false,
)

enum class FinnyPrompt {
    LowHealth,
    LowHappiness,
    LowEnergy,
    CycleEnergyBlocked,
    DepositClosed,
    RoutineReminder,
    RoutineReminderWeekend,
}

data class PetState(
    val resources: GameSnapshot = GameSnapshot(),
    val secondsRemaining: Int = TIMER_SECONDS,
    val selectedSection: HomeSection = HomeSection.Food,
    val overlay: HomeOverlay? = null,
    val activeEvent: GameEvent? = null,
    val pendingEvents: List<GameEvent> = emptyList(),
    val exitRequested: Boolean = false,
    val isInitialized: Boolean = false,
    val isTestMode: Boolean = false,
    val demoModePasswordError: Boolean = false,
    val parentModePasswordError: Boolean = false,
    val homeTutorialStep: Int? = null,
    val morningRoutineTutorialStep: Int? = null,
    val demoModeHintVisible: Boolean = false,
    val nowMillis: Long = System.currentTimeMillis(),
    val finnyPrompt: FinnyPrompt? = null,
    val customMessage: String? = null,
    val moneyAttentionNonce: Int = 0,
    val petTapReactionNonce: Int = 0,
    val budgetMasterGame: BudgetMasterGameState? = null,
    val changeMoneyGame: ChangeMoneyGameState? = null,
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
    data object AdvanceBudgetMaster : PetAction
    data object StartBudgetMasterRound : PetAction
    data class SelectBudgetDirection(val direction: BudgetDirection) : PetAction
    data class SelectBudgetCategory(val category: BudgetMasterCategory) : PetAction
    data object TickBudgetMaster : PetAction
    data object FinishBudgetMaster : PetAction
    data object PauseBudgetMaster : PetAction
    data object ResumeBudgetMaster : PetAction
    data object ExitBudgetMaster : PetAction
    data object AdvanceChangeMoney : PetAction
    data object StartChangeMoneyRound : PetAction
    data class ChangeMoneyNoteCount(val denomination: Int, val delta: Int) : PetAction
    data class CheckChangeMoney(val elapsedMillis: Long) : PetAction
    data object ShowChangeMoneyResult : PetAction
    data object PauseChangeMoney : PetAction
    data object ResumeChangeMoney : PetAction
    data object ExitChangeMoney : PetAction
    data object PetTapped : PetAction
    data object ShowRoutineReminder : PetAction
    data class ShowMessage(val message: String, val emphasizeMoney: Boolean = false) : PetAction
    data object DismissMessage : PetAction
    data class BuyGoal(val goal: Goals, val price: Int) : PetAction
    data class BuyCustomGoal(val price: Int) : PetAction
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
    data class CompleteBudgetPlanning(
        val optionalExpenses: Int,
        val goalExpenses: Int,
    ) : PetAction
    data object CompleteBudgetReview : PetAction
    data object AdvanceHomeTutorial : PetAction
    data object AdvanceMorningRoutineTutorial : PetAction
    data class OpenDepositAccount(val term: DepositTerm, val amount: Int) : PetAction
    data object CloseDepositAccount : PetAction
    data object CompleteDepositTutorial : PetAction

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
    private var eventResolutionInProgress = false
    private var budgetMasterStartInProgress = false
    private var budgetMasterCompletionInProgress = false
    private var changeMoneyStartInProgress = false
    private var changeMoneyCompletionInProgress = false
    private var lastCycleEnergyPromptAtMillis = 0L
    private val shownLowPrompts = mutableSetOf<String>()
    private val eventScheduler = EventScheduler(eventCatalog.data)

    init {
        viewModelScope.launch {
            repository.initialize()

            launch { runTimer() }

            launch {
                repository.snapshot.collect { resources ->
                    val previous = _state.value.resources.takeIf { _state.value.isInitialized }
                    val scheduledEvents = if (
                        !needsBudgetReview(resources) && !needsBudgetPlanning(resources)
                    ) {
                        val remainingSeconds = cycleSecondsRemaining(
                            resources.cycleEndsAtMillis,
                            System.currentTimeMillis(),
                        )
                        eventScheduler.due(
                            snapshot = resources,
                            elapsedCycleSeconds = eventElapsedCycleSeconds(
                                remainingSeconds = remainingSeconds,
                                isTestMode = _state.value.isTestMode,
                            ),
                        ).map(ScheduledEvent::asGameEvent)
                    } else {
                        emptyList()
                    }
                    val reachedGoalEvent = if (hasJustReachedGoal(previous, resources)) {
                        goalPurchaseEvent(resources)
                    } else {
                        null
                    }
                    _state.update {
                        val nowMillis = System.currentTimeMillis()
                        val unresolvedActiveEvent = it.activeEvent?.takeUnless { active ->
                            active.scheduledEvent?.let { scheduled ->
                                isScheduledEventHandled(scheduled, resources)
                            } ?: false
                        }
                        val nextOverlay = if (needsGoalSelection(resources)) {
                            HomeOverlay.GoalSelection
                        } else {
                            it.overlay
                        }
                        val nextTutorialStep = when {
                            resources.homeTutorialCompleted -> null
                            it.homeTutorialStep != null -> it.homeTutorialStep
                            HOME_TUTORIAL_EVENT_STEPS.all(
                                resources.handledScenarioEntryIds::contains,
                            ) -> 0
                            else -> null
                        }
                        val remainingSeconds = cycleSecondsRemaining(
                            resources.cycleEndsAtMillis,
                            nowMillis,
                        )
                        val getReadyCompleted =
                            (resources.cooldownUnlockCycles[TaskItem.GetReady.storageId] ?: 0.0) >
                                resources.currentPeriod
                        val lessonsCompleted =
                            (resources.cooldownUnlockCycles[TaskItem.Lessons.storageId] ?: 0.0) >
                                resources.currentPeriod
                        val nextMorningTutorialStep = when {
                            resources.morningRoutineTutorialCompleted -> null
                            resources.currentPeriod != 2 -> null
                            !resources.homeTutorialCompleted -> null
                            it.morningRoutineTutorialStep == null -> 0
                            it.morningRoutineTutorialStep == 1 && getReadyCompleted -> 2
                            it.morningRoutineTutorialStep == 2 && lessonsCompleted -> null
                            else -> it.morningRoutineTutorialStep
                        }
                        if (
                            it.morningRoutineTutorialStep == 2 &&
                            lessonsCompleted &&
                            !resources.morningRoutineTutorialCompleted
                        ) {
                            viewModelScope.launch { repository.completeMorningRoutineTutorial() }
                        }
                        val cycleEnergyBlocked = cycleSecondsRemaining(
                            resources.cycleEndsAtMillis,
                            nowMillis,
                        ) == 0 && resources.energy == 0
                        val canShowLowPrompt = !cycleEnergyBlocked &&
                            unresolvedActiveEvent == null &&
                            scheduledEvents.isEmpty() &&
                            reachedGoalEvent == null &&
                            nextOverlay == null &&
                            nextTutorialStep == null &&
                            nextMorningTutorialStep == null &&
                            it.finnyPrompt == null &&
                            it.customMessage == null
                        val lowPrompt = if (canShowLowPrompt) {
                            lowCharacteristicPrompts(resources).firstOrNull { prompt ->
                                "${resources.currentPeriod}:$prompt" !in shownLowPrompts
                            }
                        } else {
                            null
                        }
                        lowPrompt?.let { prompt ->
                            shownLowPrompts.add("${resources.currentPeriod}:$prompt")
                        }
                        val baseState = it.copy(
                            resources = resources,
                            secondsRemaining = remainingSeconds,
                            isInitialized = true,
                            isTestMode = resources.demoModeEnabled,
                            nowMillis = nowMillis,
                            overlay = nextOverlay,
                            activeEvent = unresolvedActiveEvent,
                            homeTutorialStep = nextTutorialStep,
                            morningRoutineTutorialStep = nextMorningTutorialStep,
                            finnyPrompt = it.finnyPrompt ?: lowPrompt,
                        )
                        enqueueEvents(
                            state = baseState,
                            candidates = scheduledEvents + listOfNotNull(reachedGoalEvent),
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
            if (currentState.morningRoutineTutorialStep != null) continue
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
            val cycleEnergyBlocked = secondsRemaining == 0 &&
                refreshedState.resources.energy == 0
            if (!cycleEnergyBlocked) lastCycleEnergyPromptAtMillis = 0L

            if (
                cycleEnergyBlocked &&
                shouldRepeatCycleEnergyPrompt(
                    nowMillis = nowMillis,
                    lastShownAtMillis = lastCycleEnergyPromptAtMillis,
                ) &&
                refreshedState.overlay == null &&
                refreshedState.homeTutorialStep == null &&
                refreshedState.morningRoutineTutorialStep == null &&
                refreshedState.finnyPrompt == null &&
                refreshedState.customMessage == null
            ) {
                _state.update {
                    if (
                        it.overlay == null &&
                        it.activeEvent == null &&
                        it.homeTutorialStep == null &&
                        it.morningRoutineTutorialStep == null &&
                        it.finnyPrompt == null &&
                        it.customMessage == null
                    ) {
                        it.copy(finnyPrompt = FinnyPrompt.CycleEnergyBlocked)
                    } else {
                        it
                    }
                }
                if (_state.value.finnyPrompt == FinnyPrompt.CycleEnergyBlocked) {
                    lastCycleEnergyPromptAtMillis = nowMillis
                }
            }

            val promptState = _state.value
            val lowPrompt = if (
                !cycleEnergyBlocked &&
                promptState.overlay == null &&
                promptState.homeTutorialStep == null &&
                promptState.morningRoutineTutorialStep == null &&
                promptState.finnyPrompt == null &&
                promptState.customMessage == null
            ) {
                lowCharacteristicPrompts(promptState.resources).firstOrNull { prompt ->
                    "${promptState.resources.currentPeriod}:$prompt" !in shownLowPrompts
                }
            } else {
                null
            }
            if (lowPrompt != null) {
                shownLowPrompts.add("${promptState.resources.currentPeriod}:$lowPrompt")
                _state.update { it.copy(finnyPrompt = lowPrompt) }
            }
            val scheduledEvents = eventScheduler.due(
                snapshot = refreshedState.resources,
                elapsedCycleSeconds = eventElapsedCycleSeconds(
                    remainingSeconds = secondsRemaining,
                    isTestMode = refreshedState.isTestMode,
                ),
            ).map(ScheduledEvent::asGameEvent)
            if (scheduledEvents.isNotEmpty()) {
                _state.update { enqueueEvents(it, scheduledEvents) }
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
                    pendingEvents = emptyList(),
                    exitRequested = false,
                    isTestMode = false,
                    demoModePasswordError = false,
                    parentModePasswordError = false,
                    homeTutorialStep = null,
                    morningRoutineTutorialStep = null,
                    budgetMasterGame = null,
                    changeMoneyGame = null,
                    demoModeHintVisible = false,
                )
            }
        }
    }

    private fun nextCycle() {
        val currentState = _state.value
        val pendingEvents = eventScheduler.due(
            snapshot = currentState.resources,
            elapsedCycleSeconds = GameDefaults.CYCLE_DURATION_SECONDS,
        ).map(ScheduledEvent::asGameEvent)
        if (pendingEvents.isNotEmpty()) {
            _state.update { enqueueEvents(it, pendingEvents) }
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
            PetAction.CloseOverlay -> _state.update {
                promotePendingEvent(it.copy(overlay = null))
            }
            is PetAction.ShowEvent -> _state.update {
                it.copy(overlay = null, activeEvent = action.event)
            }
            PetAction.AcceptEvent -> resolveEvent(accept = true)
            PetAction.DeclineEvent -> resolveEvent(accept = false)
            PetAction.SkipEvent -> _state.update {
                if (it.activeEvent?.showCloseButton == true) {
                    promotePendingEvent(it.copy(activeEvent = null))
                } else {
                    it
                }
            }
            PetAction.Exit -> _state.update { it.copy(exitRequested = true) }
            is PetAction.BuyCareItem -> when (action.item) {
                HappinessItem.BudgetMaster -> startBudgetMasterGame()
                HappinessItem.ChangeMoney -> startChangeMoneyGame()
                else -> viewModelScope.launch { repository.buyCareItem(action.item) }
            }
            PetAction.AdvanceBudgetMaster -> _state.update { current ->
                val game = current.budgetMasterGame ?: return@update current
                current.copy(
                    budgetMasterGame = game.copy(
                        stage = when (game.stage) {
                            BudgetMasterStage.Intro -> BudgetMasterStage.Rules
                            else -> game.stage
                        },
                    ),
                )
            }
            PetAction.StartBudgetMasterRound -> _state.update { current ->
                val game = current.budgetMasterGame ?: return@update current
                current.copy(
                    budgetMasterGame = game.copy(
                        stage = BudgetMasterStage.Playing,
                        step = BudgetMasterStep.Direction,
                        secondsRemaining = BUDGET_MASTER_ROUND_SECONDS,
                        score = 0,
                        errorCategory = null,
                    ),
                )
            }
            is PetAction.SelectBudgetDirection -> selectBudgetDirection(action.direction)
            is PetAction.SelectBudgetCategory -> selectBudgetCategory(action.category)
            PetAction.TickBudgetMaster -> tickBudgetMaster()
            PetAction.FinishBudgetMaster -> finishBudgetMaster()
            PetAction.PauseBudgetMaster -> _state.update { current ->
                val game = current.budgetMasterGame ?: return@update current
                if (game.stage == BudgetMasterStage.Result) {
                    promotePendingEvent(current.copy(budgetMasterGame = null))
                } else {
                    current.copy(budgetMasterGame = game.copy(pauseDialogVisible = true))
                }
            }
            PetAction.ResumeBudgetMaster -> _state.update { current ->
                val game = current.budgetMasterGame ?: return@update current
                current.copy(budgetMasterGame = game.copy(pauseDialogVisible = false))
            }
            PetAction.ExitBudgetMaster -> _state.update {
                promotePendingEvent(it.copy(budgetMasterGame = null))
            }
            PetAction.AdvanceChangeMoney -> _state.update { current ->
                val game = current.changeMoneyGame ?: return@update current
                current.copy(
                    changeMoneyGame = game.copy(
                        stage = when (game.stage) {
                            ChangeMoneyStage.Intro -> ChangeMoneyStage.Rules
                            ChangeMoneyStage.Rules -> ChangeMoneyStage.Ready
                            else -> game.stage
                        },
                    ),
                )
            }
            PetAction.StartChangeMoneyRound -> _state.update { current ->
                val game = current.changeMoneyGame ?: return@update current
                current.copy(
                    changeMoneyGame = game.copy(
                        stage = ChangeMoneyStage.Playing,
                        selectedCounts = emptyMap(),
                        validation = null,
                        elapsedMillis = 0L,
                    ),
                )
            }
            is PetAction.ChangeMoneyNoteCount -> _state.update { current ->
                val game = current.changeMoneyGame ?: return@update current
                if (game.stage != ChangeMoneyStage.Playing) return@update current
                val nextCount = ((game.selectedCounts[action.denomination] ?: 0) + action.delta)
                    .coerceIn(0, game.challenge.requiredNotes)
                current.copy(
                    changeMoneyGame = game.copy(
                        selectedCounts = game.selectedCounts + (action.denomination to nextCount),
                        validation = null,
                    ),
                )
            }
            is PetAction.CheckChangeMoney -> checkChangeMoney(action.elapsedMillis)
            PetAction.ShowChangeMoneyResult -> _state.update { current ->
                val game = current.changeMoneyGame ?: return@update current
                current.copy(changeMoneyGame = game.copy(stage = ChangeMoneyStage.Result))
            }
            PetAction.PauseChangeMoney -> _state.update { current ->
                val game = current.changeMoneyGame ?: return@update current
                if (game.stage == ChangeMoneyStage.Result) {
                    promotePendingEvent(current.copy(changeMoneyGame = null))
                } else {
                    current.copy(changeMoneyGame = game.copy(pauseDialogVisible = true))
                }
            }
            PetAction.ResumeChangeMoney -> _state.update { current ->
                val game = current.changeMoneyGame ?: return@update current
                current.copy(changeMoneyGame = game.copy(pauseDialogVisible = false))
            }
            PetAction.ExitChangeMoney -> _state.update {
                promotePendingEvent(it.copy(changeMoneyGame = null))
            }
            PetAction.PetTapped -> viewModelScope.launch {
                if (repository.rewardPetTap()) {
                    _state.update { it.copy(petTapReactionNonce = it.petTapReactionNonce + 1) }
                }
            }
            PetAction.ShowRoutineReminder -> _state.update {
                it.copy(
                    finnyPrompt = if (dayForPeriod(it.resources.currentPeriod) in 6..7) {
                        FinnyPrompt.RoutineReminderWeekend
                    } else {
                        FinnyPrompt.RoutineReminder
                    },
                    customMessage = null,
                )
            }
            is PetAction.ShowMessage -> _state.update {
                it.copy(
                    customMessage = action.message,
                    finnyPrompt = null,
                    moneyAttentionNonce = it.moneyAttentionNonce +
                        if (action.emphasizeMoney) 1 else 0,
                )
            }
            PetAction.DismissMessage -> _state.update {
                it.copy(customMessage = null, finnyPrompt = null)
            }
            is PetAction.BuyGoal -> purchaseGoal(GoalPurchase(action.goal, action.price))
            is PetAction.BuyCustomGoal -> purchaseCustomGoal(action.price)
            is PetAction.SelectGoal -> viewModelScope.launch {
                repository.selectGoal(action.goal)
                _state.update { promotePendingEvent(it.copy(overlay = null)) }
            }
            PetAction.SelectCustomGoal -> viewModelScope.launch {
                repository.selectCustomGoal()
                _state.update { promotePendingEvent(it.copy(overlay = null)) }
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
                            promotePendingEvent(
                                it.copy(
                                    demoModePasswordError = false,
                                    demoModeHintVisible = true,
                                    overlay = null,
                                ),
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
                    promotePendingEvent(
                        it.copy(
                            isTestMode = false,
                            demoModeHintVisible = false,
                            overlay = null,
                        ),
                    )
                }
            }
            PetAction.DismissDemoModeHint -> _state.update {
                promotePendingEvent(it.copy(demoModeHintVisible = false))
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
                repository.saveBudgetPlan(action.optionalExpenses, action.goalExpenses)
            }
            PetAction.CompleteBudgetReview -> viewModelScope.launch {
                repository.completeBudgetReview()
            }
            PetAction.AdvanceHomeTutorial -> advanceHomeTutorial()
            PetAction.AdvanceMorningRoutineTutorial -> _state.update { current ->
                if (current.morningRoutineTutorialStep == 0) {
                    current.copy(
                        selectedSection = HomeSection.Tasks,
                        morningRoutineTutorialStep = 1,
                    )
                } else {
                    current
                }
            }
            is PetAction.OpenDepositAccount -> viewModelScope.launch {
                repository.openDeposit(action.term, action.amount)
            }
            PetAction.CloseDepositAccount -> viewModelScope.launch {
                if (repository.closeDeposit()) {
                    _state.update { promotePendingEvent(it.copy(overlay = null)) }
                }
            }
            PetAction.CompleteDepositTutorial -> viewModelScope.launch {
                repository.completeDepositTutorial()
            }
        }
    }

    private fun startBudgetMasterGame() {
        if (_state.value.budgetMasterGame != null || budgetMasterStartInProgress) return
        budgetMasterStartInProgress = true
        val showTutorial = !_state.value.resources.budgetMasterTutorialCompleted
        viewModelScope.launch {
            try {
                if (!repository.startBudgetMasterAttempt()) return@launch
                val firstCard = randomBudgetMasterCard()
                _state.update { current ->
                    current.copy(
                        overlay = null,
                        finnyPrompt = null,
                        customMessage = null,
                        budgetMasterGame = BudgetMasterGameState(
                            card = firstCard,
                            stage = if (showTutorial) {
                                BudgetMasterStage.Intro
                            } else {
                                BudgetMasterStage.Playing
                            },
                            usedCardIds = setOf(firstCard.id),
                        ),
                    )
                }
            } finally {
                budgetMasterStartInProgress = false
            }
        }
    }

    private fun selectBudgetDirection(direction: BudgetDirection) {
        val game = _state.value.budgetMasterGame ?: return
        if (
            game.stage != BudgetMasterStage.Playing ||
            game.step != BudgetMasterStep.Direction ||
            game.errorCategory != null
        ) return
        if (isCorrectBudgetDirection(game.card, direction)) {
            _state.update { current ->
                current.copy(
                    budgetMasterGame = current.budgetMasterGame?.copy(
                        step = BudgetMasterStep.Category,
                    ),
                )
            }
        } else {
            showBudgetMasterError(game)
        }
    }

    private fun selectBudgetCategory(category: BudgetMasterCategory) {
        val game = _state.value.budgetMasterGame ?: return
        if (
            game.stage != BudgetMasterStage.Playing ||
            game.step != BudgetMasterStep.Category ||
            game.errorCategory != null
        ) return
        if (isCorrectBudgetCategory(game.card, category)) {
            _state.update { current ->
                val activeGame = current.budgetMasterGame ?: return@update current
                current.copy(budgetMasterGame = activeGame.nextBudgetMasterCard(scoreDelta = 1))
            }
        } else {
            showBudgetMasterError(game)
        }
    }

    private fun showBudgetMasterError(game: BudgetMasterGameState) {
        _state.update { current ->
            val activeGame = current.budgetMasterGame ?: return@update current
            if (activeGame.card.id != game.card.id) return@update current
            current.copy(
                budgetMasterGame = activeGame.copy(errorCategory = activeGame.card.category),
            )
        }
        viewModelScope.launch {
            delay(2_200L)
            _state.update { current ->
                val activeGame = current.budgetMasterGame ?: return@update current
                if (
                    activeGame.stage != BudgetMasterStage.Playing ||
                    activeGame.card.id != game.card.id ||
                    activeGame.errorCategory == null
                ) return@update current
                current.copy(budgetMasterGame = activeGame.nextBudgetMasterCard())
            }
        }
    }

    private fun tickBudgetMaster() {
        val game = _state.value.budgetMasterGame ?: return
        if (
            game.stage != BudgetMasterStage.Playing ||
            game.pauseDialogVisible ||
            budgetMasterCompletionInProgress
        ) return
        if (game.secondsRemaining <= 1) {
            _state.update { current ->
                current.copy(
                    budgetMasterGame = current.budgetMasterGame?.copy(secondsRemaining = 0),
                )
            }
            finishBudgetMaster()
        } else {
            _state.update { current ->
                current.copy(
                    budgetMasterGame = current.budgetMasterGame?.copy(
                        secondsRemaining = game.secondsRemaining - 1,
                    ),
                )
            }
        }
    }

    private fun finishBudgetMaster() {
        if (budgetMasterCompletionInProgress) return
        val game = _state.value.budgetMasterGame ?: return
        if (game.stage != BudgetMasterStage.Playing) return
        budgetMasterCompletionInProgress = true
        viewModelScope.launch {
            try {
                repository.completeBudgetMasterAttempt(
                    score = game.score,
                    successful = game.score >= BUDGET_MASTER_SUCCESS_SCORE,
                )
                _state.update { current ->
                    current.copy(
                        budgetMasterGame = current.budgetMasterGame?.copy(
                            stage = BudgetMasterStage.Result,
                            pauseDialogVisible = false,
                            errorCategory = null,
                        ),
                    )
                }
            } finally {
                budgetMasterCompletionInProgress = false
            }
        }
    }

    private fun BudgetMasterGameState.nextBudgetMasterCard(
        scoreDelta: Int = 0,
    ): BudgetMasterGameState {
        val next = randomBudgetMasterCard(excludedIds = usedCardIds)
        return copy(
            card = next,
            step = BudgetMasterStep.Direction,
            score = score + scoreDelta,
            usedCardIds = usedCardIds + next.id,
            errorCategory = null,
        )
    }

    private fun startChangeMoneyGame() {
        if (_state.value.changeMoneyGame != null || changeMoneyStartInProgress) return
        changeMoneyStartInProgress = true
        val showTutorial = !_state.value.resources.changeMoneyTutorialCompleted
        viewModelScope.launch {
            try {
                if (!repository.startChangeMoneyAttempt()) return@launch
                _state.update { current ->
                    current.copy(
                        overlay = null,
                        finnyPrompt = null,
                        customMessage = null,
                        changeMoneyGame = ChangeMoneyGameState(
                            challenge = randomChangeMoneyChallenge(),
                            stage = if (showTutorial) {
                                ChangeMoneyStage.Intro
                            } else {
                                ChangeMoneyStage.Ready
                            },
                        ),
                    )
                }
            } finally {
                changeMoneyStartInProgress = false
            }
        }
    }

    private fun checkChangeMoney(elapsedMillis: Long) {
        if (changeMoneyCompletionInProgress) return
        val game = _state.value.changeMoneyGame ?: return
        if (game.stage != ChangeMoneyStage.Playing) return
        val validation = validateChangeMoneySelection(game.challenge, game.selectedCounts)
        if (validation != ChangeMoneyValidation.Success) {
            _state.update { current ->
                current.copy(changeMoneyGame = current.changeMoneyGame?.copy(validation = validation))
            }
            return
        }
        changeMoneyCompletionInProgress = true
        viewModelScope.launch {
            try {
                repository.completeChangeMoneyAttempt(game.challenge.id, elapsedMillis)
                _state.update { current ->
                    current.copy(
                        changeMoneyGame = current.changeMoneyGame?.copy(
                            stage = ChangeMoneyStage.Success,
                            validation = null,
                            elapsedMillis = elapsedMillis,
                        ),
                    )
                }
            } finally {
                changeMoneyCompletionInProgress = false
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
        if (eventResolutionInProgress) return
        val currentState = _state.value
        val event = currentState.activeEvent ?: return
        event.scheduledEvent?.let { scheduled ->
            val canDeclineAtZero = currentState.resources.activeDeposit == null &&
                currentState.resources.health == 0 &&
                currentState.resources.happiness == 0 &&
                currentState.resources.energy == 0
            if (!canResolveScheduledEvent(
                    scheduled,
                    accept,
                    currentState.resources.money,
                    canDeclineAtZero,
                )
            ) return
            eventResolutionInProgress = true
            viewModelScope.launch {
                try {
                    val result = repository.resolveScheduledEvent(
                        event = scheduled,
                        unlockedJob = eventScheduler.jobFor(scheduled),
                        accepted = accept,
                    )
                    _state.update { state ->
                        if (state.activeEvent?.scheduledEvent == scheduled) {
                            state.copy(activeEvent = null)
                        } else {
                            state
                        }
                    }
                    if (result.depositClosedWithPenalty) {
                        _state.update { it.copy(finnyPrompt = FinnyPrompt.DepositClosed) }
                    }
                    if (opensDepositAfterEvent(scheduled.definition.id, accept)) {
                        _state.update { it.copy(overlay = HomeOverlay.Deposit) }
                    } else {
                        _state.update(::promotePendingEvent)
                    }
                } finally {
                    eventResolutionInProgress = false
                }
            }
            return
        }
        if (accept && event.goalPurchase != null) {
            purchaseGoal(event.goalPurchase)
            return
        }
        if (accept && event.customGoalPurchase) {
            purchaseCustomGoal(currentState.resources.goalTarget)
            return
        }
        val effects = if (accept) event.acceptEffects else event.declineEffects
        if (!canApplyEffects(currentState.resources.money, effects)) return

        _state.update { promotePendingEvent(it.copy(activeEvent = null)) }
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

    private fun purchaseCustomGoal(price: Int) {
        if (goalPurchaseInProgress) return
        goalPurchaseInProgress = true
        viewModelScope.launch {
            try {
                if (repository.buyCustomGoal(price)) {
                    _state.update {
                        it.copy(activeEvent = null, overlay = HomeOverlay.GoalSelection)
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
                    _state.update { promotePendingEvent(it.copy(overlay = null)) }
                }
            } finally {
                appearanceUpdateInProgress = false
            }
        }
    }
}

private fun GameEvent.queueKey(): String = scheduledEvent?.let { scheduled ->
    scheduled.scenarioStepId ?: "event:${scheduled.definition.id}"
} ?: goalPurchase?.let { "goal:${it.goal.name}" }
    ?: if (customGoalPurchase) "goal:custom" else "event:${descriptionText}:${descriptionRes}"

internal fun canPresentQueuedEvent(state: PetState): Boolean =
    state.resources.onboardingCompleted &&
        !needsBudgetReview(state.resources) &&
        !needsBudgetPlanning(state.resources) &&
        state.overlay == null &&
        !state.demoModeHintVisible &&
        state.budgetMasterGame == null &&
        state.changeMoneyGame == null &&
        state.homeTutorialStep == null &&
        state.morningRoutineTutorialStep == null

internal fun enqueueEvents(state: PetState, candidates: List<GameEvent>): PetState {
    val activeKey = state.activeEvent?.queueKey()
    val retainedPending = state.pendingEvents.filterNot { event ->
        event.scheduledEvent?.let { isScheduledEventHandled(it, state.resources) } ?: false
    }
    val knownKeys = buildSet {
        activeKey?.let(::add)
        retainedPending.forEach { add(it.queueKey()) }
    }
    val additions = candidates.filter { it.queueKey() !in knownKeys }.distinctBy(GameEvent::queueKey)
    return promotePendingEvent(state.copy(pendingEvents = retainedPending + additions))
}

internal fun promotePendingEvent(state: PetState): PetState {
    if (state.activeEvent != null || !canPresentQueuedEvent(state)) return state
    val next = state.pendingEvents.firstOrNull() ?: return state
    return state.copy(activeEvent = next, pendingEvents = state.pendingEvents.drop(1))
}

internal fun lowCharacteristicPrompts(resources: GameSnapshot): List<FinnyPrompt> = buildList {
    if (resources.health < 10) add(FinnyPrompt.LowHealth)
    if (resources.energy < 10) add(FinnyPrompt.LowEnergy)
    if (resources.happiness < 10) add(FinnyPrompt.LowHappiness)
}

internal fun shouldRepeatCycleEnergyPrompt(
    nowMillis: Long,
    lastShownAtMillis: Long,
): Boolean = lastShownAtMillis == 0L ||
    nowMillis - lastShownAtMillis >= CYCLE_ENERGY_PROMPT_REPEAT_MILLIS

private val DEPOSIT_OFFER_EVENT_IDS = setOf(
    "deposit_offer",
    "forced_deposit_offer",
)

internal fun opensDepositAfterEvent(eventId: String, accepted: Boolean): Boolean =
    accepted && eventId in DEPOSIT_OFFER_EVENT_IDS

private fun ScheduledEvent.asGameEvent(): GameEvent = GameEvent(
    descriptionRes = R.string.app_name,
    descriptionText = definition.title,
    illustrationRes = definition.illustrationRes,
    acceptEffects = acceptEffects,
    canDecline = definition.canDecline,
    hideRewardUntilAccept = definition.hideRewardUntilAccept,
    scheduledEvent = this,
)
