package ru.finni.financialpetgame.lct.ui

import android.graphics.ImageDecoder
import android.graphics.drawable.Animatable2
import android.graphics.drawable.AnimatedImageDrawable
import android.os.Build
import android.os.SystemClock
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import ru.finni.financialpetgame.lct.R
import ru.finni.financialpetgame.lct.domain.CardItem
import ru.finni.financialpetgame.lct.domain.CooldownRemaining
import ru.finni.financialpetgame.lct.domain.Effect
import ru.finni.financialpetgame.lct.domain.FoodItem
import ru.finni.financialpetgame.lct.domain.Resource
import ru.finni.financialpetgame.lct.domain.ShopItem
import ru.finni.financialpetgame.lct.domain.TaskItem
import ru.finni.financialpetgame.lct.domain.TaskUsageLimit
import ru.finni.financialpetgame.lct.domain.EnergyItem
import ru.finni.financialpetgame.lct.domain.GameEvent
import ru.finni.financialpetgame.lct.domain.GameEvents
import ru.finni.financialpetgame.lct.domain.GameDefaults
import ru.finni.financialpetgame.lct.domain.HairColour
import ru.finni.financialpetgame.lct.domain.HappinessItem
import ru.finni.financialpetgame.lct.domain.Goals
import ru.finni.financialpetgame.lct.domain.cooldownRemaining
import ru.finni.financialpetgame.lct.domain.dayForPeriod
import ru.finni.financialpetgame.lct.domain.isAvailable
import ru.finni.financialpetgame.lct.domain.isUnlockedAtPeriod
import ru.finni.financialpetgame.lct.domain.weekForPeriod
import ru.finni.financialpetgame.lct.domain.WorkPreparationBlock
import ru.finni.financialpetgame.lct.domain.workPreparationBlock
import ru.finni.financialpetgame.lct.domain.currentCyclePosition
import ru.finni.financialpetgame.lct.domain.needsBudgetPlanning
import ru.finni.financialpetgame.lct.domain.needsBudgetReview
import ru.finni.financialpetgame.lct.domain.budgetPeriodResult
import ru.finni.financialpetgame.lct.domain.canApplyEffects
import ru.finni.financialpetgame.lct.domain.canDeclineEvent
import ru.finni.financialpetgame.lct.ui.theme.NunitoFontFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private val HomePurple = Color(0xFF8743D3)
private val HomePurpleDark = Color(0xFF4B2163)
private val HomeSurface = Color(0xFFFCF7FF)
private val HomeTrack = Color(0xFFE6C1FA)

private enum class HomePetAnimation { Sleepy, Brushing, Lessons }

private fun HomePetAnimation.assetPath(colour: HairColour): String = when (this) {
    HomePetAnimation.Sleepy -> when (colour) {
        HairColour.Beige -> "webm/beige_sleepy_animated.webp"
        HairColour.Violet -> "webm/violet_sleepy_animated.webp"
        HairColour.Orange -> "webm/orange_sleepy_animated.webp"
    }
    HomePetAnimation.Brushing -> when (colour) {
        HairColour.Beige -> "webm/beige_brushing_animated.webp"
        HairColour.Violet -> "webm/violet_brushing_animated.webp"
        HairColour.Orange -> "webm/orange_brushing_animated.webp"
    }
    HomePetAnimation.Lessons -> "webm/lessons_animated.webp"
}

internal fun roundedMinutesRemaining(seconds: Int): Int =
    ((seconds.coerceAtLeast(0).toLong() + 59L) / 60L).toInt()

private data class HomeLayoutMetrics(
    val horizontalPadding: Dp,
    val headerHeight: Dp,
    val backdropHeight: Dp,
    val backdropTopInset: Dp,
    val topFadeHeight: Dp,
    val bottomFadeHeight: Dp,
    val quickActionsTop: Dp,
    val quickActionSize: Dp,
    val petWidth: Dp,
    val bottomPanelHeight: Dp,
    val tabHeight: Dp,
    val itemGap: Dp,
    val headerValueWidth: Dp,
)

private fun homeLayoutMetrics(width: Dp, height: Dp): HomeLayoutMetrics {
    val horizontalPadding = (width * 0.041f).coerceIn(12.dp, 20.dp)
    val headerHeight = (height * 0.156f).coerceIn(106.dp, 138.dp)
    val bottomPanelHeight = (height * 0.28f).coerceIn(200.dp, 255.dp)
    val tabHeight = bottomPanelHeight * (68f / 226f)

    return HomeLayoutMetrics(
        horizontalPadding = horizontalPadding,
        headerHeight = headerHeight,
        backdropHeight = height - bottomPanelHeight + tabHeight,
        backdropTopInset = headerHeight * (116f / 126f),
        topFadeHeight = headerHeight * 0.7f,
        bottomFadeHeight = bottomPanelHeight * 0.45f,
        quickActionsTop = headerHeight * (138f / 126f),
        quickActionSize = (width * 0.129f).coerceIn(44.dp, 58.dp),
        petWidth = (width * 0.71f).coerceIn(225.dp, 330.dp),
        bottomPanelHeight = bottomPanelHeight,
        tabHeight = tabHeight,
        itemGap = (width * 0.02f).coerceIn(6.dp, 10.dp),
        headerValueWidth = (width * 0.14f).coerceIn(44.dp, 64.dp),
    )
}

private data class CardItemUI(
    val item: CardItem,
    @DrawableRes val illustration: Int,
    @param:StringRes val title: Int,
)

private val FoodItems = listOf(
    CardItemUI(FoodItem.SchoolLunch, R.drawable.food_item1, R.string.school_lunch),
    CardItemUI(FoodItem.Soda, R.drawable.food_item2, R.string.soda),
    CardItemUI(FoodItem.MashedPotatoes, R.drawable.food_item3, R.string.mashed_potatoes),
    CardItemUI(FoodItem.IceCream, R.drawable.food_item4, R.string.ice_cream),
    CardItemUI(FoodItem.HamburgerWithCola, R.drawable.food_item5, R.string.hamburger_cola),
    CardItemUI(FoodItem.Pasta, R.drawable.food_item6, R.string.pasta),
    CardItemUI(FoodItem.SetRolls, R.drawable.food_item7, R.string.set_rolls),
    CardItemUI(FoodItem.Pizza, R.drawable.food_item8, R.string.pizza),
)

private val HappinessItems = listOf(
    CardItemUI(HappinessItem.BudgetMaster, R.drawable.budget_master, R.string.master_budget),
    CardItemUI(HappinessItem.CatchMoney, R.drawable.catch_money, R.string.catch_money),
    CardItemUI(HappinessItem.ChangeMoney, R.drawable.change_money, R.string.change_money),
    CardItemUI(HappinessItem.PlayWithBall, R.drawable.play_ball, R.string.ball_game),
    CardItemUI(HappinessItem.BoardGame, R.drawable.board_game, R.string.board_game),
    CardItemUI(HappinessItem.MeetingWithFriends, R.drawable.meet_the_friends, R.string.meeting_with_friends),
    CardItemUI(HappinessItem.Trip, R.drawable.trip_to_city, R.string.trip),
    CardItemUI(HappinessItem.Zoo, R.drawable.zoo, R.string.zoo),
)

private val EnergyItems = listOf(
    CardItemUI(EnergyItem.TakeASeat, R.drawable.to_seat, R.string.seat),
    CardItemUI(EnergyItem.TakeANap, R.drawable.take_a_nap, R.string.take_a_nap),
    CardItemUI(EnergyItem.ListenMusic, R.drawable.listen_music, R.string.listen_music),
    CardItemUI(EnergyItem.TakeAMassage, R.drawable.massage_coach, R.string.take_a_massage),
    CardItemUI(EnergyItem.HotBath, R.drawable.bath, R.string.bath),
    CardItemUI(EnergyItem.SPA, R.drawable.spa, R.string.spa),
    CardItemUI(EnergyItem.BodyMassage, R.drawable.body_massage, R.string.body_massage),
    CardItemUI(EnergyItem.Yoga, R.drawable.yoga, R.string.yoga),
)

private val ShopItems = listOf(
    CardItemUI(ShopItem.FlowerPot, R.drawable.flower, R.string.shop_flower_pot),
    CardItemUI(ShopItem.FavouriteMug, R.drawable.cup, R.string.shop_favourite_mug),
    CardItemUI(ShopItem.FloorLamp, R.drawable.lamp, R.string.shop_floor_lamp),
    CardItemUI(ShopItem.SoftRug, R.drawable.soft_beaty, R.string.shop_soft_rug),
    CardItemUI(ShopItem.SoftArmchair, R.drawable.soft_coach, R.string.shop_soft_armchair),
    CardItemUI(ShopItem.StylishScarf, R.drawable.scarf, R.string.shop_stylish_scarf),
    CardItemUI(ShopItem.GlowingOrb, R.drawable.lightning_ball, R.string.shop_glowing_orb),
    CardItemUI(
        ShopItem.MorningAutomation,
        R.drawable.automation_morning,
        R.string.shop_morning_automation,
    ),
)

private val TaskItems = listOf(
    CardItemUI(TaskItem.GetReady, R.drawable.take_care_of_yourself, R.string.task_get_ready),
    CardItemUI(TaskItem.Lessons, R.drawable.homework, R.string.task_lessons),
    CardItemUI(TaskItem.Cleaning, R.drawable.mopping, R.string.task_cleaning),
    CardItemUI(TaskItem.BeadCrafts, R.drawable.pearls, R.string.task_bead_crafts),
    CardItemUI(TaskItem.DeliverNewspapers, R.drawable.newspapers, R.string.task_deliver_newspapers),
    CardItemUI(TaskItem.HandOutFlyers, R.drawable.promote, R.string.task_hand_out_flyers),
    CardItemUI(TaskItem.RecyclePaper, R.drawable.recycle, R.string.task_recycle_paper),
    CardItemUI(TaskItem.FeedNeighboursCat, R.drawable.feed_cat, R.string.task_feed_neighbours_cat),
    CardItemUI(TaskItem.WalkNeighboursDog, R.drawable.walking_dog, R.string.task_walk_neighbours_dog),
    CardItemUI(TaskItem.HelpGrandfather, R.drawable.grandpa, R.string.task_help_grandfather),
    CardItemUI(TaskItem.WaterPlants, R.drawable.feed_flower, R.string.task_water_plants),
)


@Composable
fun PetScreen(state: PetState, onAction: (PetAction) -> Unit) {
    val context = LocalContext.current
    val backMessage = stringResource(R.string.press_back_again_to_exit)
    var lastBackPressAt by remember { mutableLongStateOf(0L) }
    val closeOverlay = {
        lastBackPressAt = 0L
        onAction(PetAction.CloseOverlay)
    }
    val handleSystemBack = {
        if (state.budgetMasterGame != null) {
            onAction(PetAction.PauseBudgetMaster)
        } else if (state.changeMoneyGame != null) {
            onAction(PetAction.PauseChangeMoney)
        } else {
            val now = SystemClock.elapsedRealtime()
            when (
                backPressAction(
                    previousPressAt = lastBackPressAt,
                    currentPressAt = now,
                    hasDemoHint = state.demoModeHintVisible,
                    hasActiveEvent = state.activeEvent != null,
                    activeEventCanClose = state.activeEvent?.showCloseButton == true,
                    hasOverlay = state.overlay != null,
                )
            ) {
                BackPressAction.DismissDemoHint -> {
                    lastBackPressAt = 0L
                    onAction(PetAction.DismissDemoModeHint)
                }
                BackPressAction.SkipEvent -> {
                    lastBackPressAt = 0L
                    onAction(PetAction.SkipEvent)
                }
                BackPressAction.Ignore -> lastBackPressAt = 0L
                BackPressAction.CloseOverlay -> closeOverlay()
                BackPressAction.Exit -> {
                    lastBackPressAt = 0L
                    onAction(PetAction.Exit)
                }
                BackPressAction.PromptExit -> {
                    lastBackPressAt = now
                    Toast.makeText(context, backMessage, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    BackHandler(onBack = handleSystemBack)

    if (!state.isInitialized) {
        Box(Modifier.fillMaxSize().background(HomeSurface))
        return
    }

    if (!state.resources.onboardingCompleted) {
        OnboardingScreen(
            initialState = state.resources,
            onComplete = { onAction(PetAction.CompleteOnboarding(it)) },
        )
        return
    }

    state.budgetMasterGame?.let { game ->
        BudgetMasterGameScreen(
            game = game,
            bestScore = state.resources.budgetMasterBestScore,
            onAction = onAction,
        )
        return
    }

    state.changeMoneyGame?.let { game ->
        ChangeMoneyGameScreen(
            game = game,
            bestTimeMillis = state.resources.changeMoneyBestTimesMillis.values.minOrNull(),
            onAction = onAction,
        )
        return
    }

    if (needsBudgetReview(state.resources)) {
        BudgetPeriodResultScreen(
            petImageRes = petImageResource(
                state.resources.level, state.resources.hairColour, state.resources.hairStyle,
            ),
            state = state.resources,
            result = budgetPeriodResult(state.resources),
            onComplete = { onAction(PetAction.CompleteBudgetReview) },
        )
        return
    }

    if (needsBudgetPlanning(state.resources)) {
        val activatesLevelTwoExpenses =
            state.resources.level >= 2 && !state.resources.levelTwoBudgetExpensesEnabled
        val nextRecurringExpense = state.resources.expense + if (activatesLevelTwoExpenses) {
            GameDefaults.VITAMINS_EXPENSE + GameDefaults.SPORTS_SECTION_EXPENSE
        } else {
            0
        }
        BudgetPlanningScreen(
            petImageRes = petImageResource(
                state.resources.level, state.resources.hairColour, state.resources.hairStyle,
            ),
            firstPlanning = !state.resources.budgetTutorialCompleted,
            secondaryPlanning = state.resources.budgetTutorialCompleted &&
                state.resources.budgetPlanWeek == 1,
            previousOptionalExpenses = state.resources.actualOptionalExpenses,
            previousOptionalExpenseRecords = state.resources.optionalExpenseRecords,
            recurringIncome = state.resources.income,
            recurringExpense = nextRecurringExpense,
            onComplete = { optional ->
                onAction(PetAction.CompleteBudgetPlanning(optional))
            },
        )
        return
    }

    Box(Modifier.fillMaxSize().background(HomeSurface)) {
        HomePage(state, onAction)
    }

//    EventOverlay(GameEvents.SportsSection, acceptButtonText = "Принять")

    if (state.overlay != null) {
        Box(
            Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            event.changes.forEach { change -> change.consume() }
                        }
                    }
                },
        )
    }

    when (state.overlay) {
        HomeOverlay.Menu -> MenuOverlay(state, onAction, closeOverlay)
        HomeOverlay.Handbook -> HandbookScreen(onBack = closeOverlay)
        HomeOverlay.Budget -> BudgetOverviewScreen(
            state = state.resources,
            onBack = closeOverlay,
            onCloseDeposit = { onAction(PetAction.CloseDepositAccount) },
        )
        HomeOverlay.Deposit -> DepositScreen(
            state = state.resources,
            onBack = closeOverlay,
            onOpen = { term, amount -> onAction(PetAction.OpenDepositAccount(term, amount)) },
            onClose = { onAction(PetAction.CloseDepositAccount) },
            showEducationInitially = !state.resources.depositTutorialCompleted,
            onEducationSeen = { onAction(PetAction.CompleteDepositTutorial) },
        )
        HomeOverlay.Goal -> {
            val isCustomGoal = state.resources.goalId == "custom"
            val goal = if (isCustomGoal) null else Goals.fromStorageId(state.resources.goalId)
            GoalOverlay(
                money = state.resources.money,
                target = state.resources.goalTarget,
                goal = goal,
                customGoal = state.resources.customGoal.takeIf { isCustomGoal },
                currentLevel = state.resources.level,
                alreadyPurchased = state.resources.goalId in state.resources.purchasedGoalIds,
                onBuy = {
                    if (goal != null) {
                        onAction(PetAction.BuyGoal(goal, state.resources.goalTarget))
                    } else {
                        onAction(PetAction.BuyCustomGoal(state.resources.goalTarget))
                    }
                },
                onDismiss = closeOverlay,
            )
        }
        HomeOverlay.GoalSelection -> {
            val availableGoals = Goals.entries
                .filter {
                    it.level <= state.resources.level &&
                        it.name !in state.resources.purchasedGoalIds
                }
            if (availableGoals.isEmpty() && state.resources.customGoal == null) {
                LaunchedEffect(Unit) { closeOverlay() }
            } else {
                GoalSelectionScreen(
                    goals = availableGoals,
                    onGoalSelected = { onAction(PetAction.SelectGoal(it)) },
                    customGoal = state.resources.customGoal.takeUnless {
                        "custom" in state.resources.purchasedGoalIds
                    },
                    onCustomGoalSelected = { onAction(PetAction.SelectCustomGoal) },
                )
            }
        }
        HomeOverlay.PersonalAccount -> PersonalAccountScreen(
            initialState = state.resources,
            onBack = closeOverlay,
            onApply = { onAction(PetAction.ApplyPetAppearance(it)) },
        )
        HomeOverlay.Parent -> ParentScreen(
            state = state.resources,
            onBack = closeOverlay,
            onAddGoal = { title, target, image ->
                onAction(PetAction.AddCustomGoal(title, target, image))
            },
        )
        null -> Unit
    }

    state.activeEvent?.let { event ->
        val canAcceptEvent = canApplyEffects(
            health = state.resources.health,
            happiness = state.resources.happiness,
            energy = state.resources.energy,
            money = state.resources.money,
            effects = event.acceptEffects,
        )
        EventOverlay(
            event = event,
            acceptButtonText = stringResource(event.acceptButtonTextRes),
            declineButtonText = if (canDeclineEvent(state.resources, event)) {
                stringResource(event.declineButtonTextRes)
            } else {
                null
            },
            canClose = event.showCloseButton,
            canAccept = canAcceptEvent,
            onAccept = { onAction(PetAction.AcceptEvent) },
            onDecline = { onAction(PetAction.DeclineEvent) },
            onClose = { onAction(PetAction.SkipEvent) },
        )
    }

    state.customMessage?.takeIf {
        state.activeEvent == null && state.overlay == null
    }?.let { message ->
        LaunchedEffect(message) {
            delay(4_500)
            onAction(PetAction.DismissMessage)
        }
        ItemAvailabilityAnnotation(
            message = message,
            onDismiss = { onAction(PetAction.DismissMessage) },
        )
    }

    val finnyMessage = state.finnyPrompt?.let { prompt ->
        when (prompt) {
            FinnyPrompt.LowHealth -> stringResource(R.string.low_health_hint)
            FinnyPrompt.LowHappiness -> stringResource(R.string.low_happiness_hint)
            FinnyPrompt.LowEnergy -> stringResource(R.string.low_energy_hint)
            FinnyPrompt.CycleEnergyBlocked -> stringResource(
                R.string.cycle_energy_blocked,
                state.resources.name,
            )
            FinnyPrompt.DepositClosed -> stringResource(R.string.deposit_closed_by_event)
            FinnyPrompt.RoutineReminder -> stringResource(R.string.routine_reminder)
            FinnyPrompt.RoutineReminderWeekend ->
                stringResource(R.string.routine_reminder_weekend)
        }
    }
    finnyMessage?.takeIf {
        state.activeEvent == null && state.overlay == null && state.homeTutorialStep == null
            && state.morningRoutineTutorialStep == null
    }?.let { message ->
        FinnySpeechOverlay(
            message = message,
            onDismiss = { onAction(PetAction.DismissMessage) },
        )
    }
}

@Composable
private fun FinnySpeechOverlay(
    message: String,
    onDismiss: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = onDismiss),
    ) {
        PetSpeechBubble(
            text = message,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .safeDrawingPadding()
                .padding(top = 150.dp, start = 24.dp, end = 24.dp)
                .widthIn(max = 320.dp),
        )
    }
}

@Composable
private fun ItemAvailabilityAnnotation(
    message: String,
    onDismiss: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss,
            )
            .safeDrawingPadding()
            .padding(horizontal = 20.dp)
            .padding(bottom = 218.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onDismiss),
            color = Color.White,
            shape = RoundedCornerShape(14.dp),
            shadowElevation = 8.dp,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.tip_pic),
                    contentDescription = null,
                    modifier = Modifier.size(42.dp),
                    contentScale = ContentScale.Fit,
                )
                Text(
                    text = message,
                    modifier = Modifier.weight(1f),
                    color = HomePurpleDark,
                    fontFamily = NunitoFontFamily,
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "×",
                    color = HomePurple,
                    fontSize = 22.sp,
                    lineHeight = 22.sp,
                )
            }
        }
    }
}

private const val BACK_EXIT_INTERVAL_MILLIS = 2_000L

internal enum class BackPressAction {
    DismissDemoHint,
    SkipEvent,
    Ignore,
    CloseOverlay,
    Exit,
    PromptExit,
}

internal fun isSecondBackPress(previousPressAt: Long, currentPressAt: Long): Boolean =
    previousPressAt > 0L &&
        currentPressAt >= previousPressAt &&
        currentPressAt - previousPressAt <= BACK_EXIT_INTERVAL_MILLIS

internal fun backPressAction(
    previousPressAt: Long,
    currentPressAt: Long,
    hasDemoHint: Boolean,
    hasActiveEvent: Boolean,
    activeEventCanClose: Boolean,
    hasOverlay: Boolean,
): BackPressAction = when {
    hasDemoHint -> BackPressAction.DismissDemoHint
    hasActiveEvent && activeEventCanClose -> BackPressAction.SkipEvent
    hasActiveEvent -> BackPressAction.Ignore
    hasOverlay -> BackPressAction.CloseOverlay
    isSecondBackPress(previousPressAt, currentPressAt) -> BackPressAction.Exit
    else -> BackPressAction.PromptExit
}

@Composable
private fun HomePage(state: PetState, onAction: (PetAction) -> Unit) {
    var oneShotPetAnimation by remember(state.resources.currentPeriod) {
        mutableStateOf<HomePetAnimation?>(null)
    }
    var routinePromptShown by remember(state.resources.currentPeriod) {
        mutableStateOf(false)
    }
    val morningRoutineAutomated =
        ShopItem.MorningAutomation.storageId in state.resources.purchasedShopItemIds
    val cyclePosition = currentCyclePosition(
        currentPeriod = state.resources.currentPeriod,
        cycleSecondsRemaining = state.secondsRemaining,
    )
    val getReadyCompleted =
        (state.resources.cooldownUnlockCycles[TaskItem.GetReady.storageId] ?: 0.0) >
            cyclePosition
    val lessonsCompleted =
        dayForPeriod(state.resources.currentPeriod) in 6..7 ||
            (state.resources.cooldownUnlockCycles[TaskItem.Lessons.storageId] ?: 0.0) >
            cyclePosition
    val morningRoutineCompleted = getReadyCompleted && lessonsCompleted

    LaunchedEffect(
        state.resources.currentPeriod,
        morningRoutineAutomated,
        state.activeEvent,
        state.homeTutorialStep,
        state.morningRoutineTutorialStep,
        morningRoutineCompleted,
    ) {
        if (
            !routinePromptShown &&
            state.resources.currentPeriod > 1 &&
            !morningRoutineAutomated &&
            !morningRoutineCompleted &&
            state.activeEvent == null &&
            state.homeTutorialStep == null &&
            state.morningRoutineTutorialStep == null
        ) {
            routinePromptShown = true
            onAction(PetAction.ShowRoutineReminder)
        }
    }

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(HomeSurface)
            .safeDrawingPadding(),
    ) {
        val metrics = homeLayoutMetrics(maxWidth, maxHeight)

        HomeBackground(metrics, state.resources.roomDirtLevel)

        HomeHeader(
            state = state,
            onMenuClick = { onAction(PetAction.OpenMenu) },
            onGoalClick = { onAction(PetAction.OpenGoal) },
            tutorialHighlighted = state.homeTutorialStep == 0,
            metrics = metrics,
            modifier = Modifier.align(Alignment.TopCenter),
        )

        Text(
            text = stringResource(R.string.goal_level, state.resources.level),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = metrics.headerHeight + 2.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable { onAction(PetAction.OpenGoal) }
                .padding(horizontal = 18.dp, vertical = 6.dp),
            color = HomePurpleDark,
            fontFamily = NunitoFontFamily,
            fontSize = 15.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.Bold,
        )

        QuickActions(
            secondsRemaining = state.secondsRemaining,
            highlightCycle = state.demoModeHintVisible || state.homeTutorialStep == 1,
            highlightBudget = state.homeTutorialStep == 2,
            highlightDeposit = state.homeTutorialStep == 3,
            onBudgetClick = { onAction(PetAction.OpenBudget) },
            onDepositClick = { onAction(PetAction.OpenDeposit) },
            onSkipCycle = { onAction(PetAction.ForceNextCycle )},
            tileSize = metrics.quickActionSize,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = metrics.quickActionsTop),
        )

        val petImageRes = petImageResource(
            level = state.resources.level,
            colour = state.resources.hairColour,
            style = state.resources.hairStyle,
        )
        val teethBrushed =
            (state.resources.cooldownUnlockCycles[TaskItem.GetReady.storageId] ?: 0.0) >
                cyclePosition
        val petAnimation = oneShotPetAnimation ?: HomePetAnimation.Sleepy.takeIf {
            !morningRoutineAutomated && !teethBrushed
        }
        val petModifier = Modifier
            .align(Alignment.Center)
            .width(metrics.petWidth)
            .aspectRatio(6f / 7f)

        if (petAnimation == null) {
            InteractivePetImage(
                imageRes = petImageRes,
                reactionNonce = state.petTapReactionNonce,
                onTap = { onAction(PetAction.PetTapped) },
                modifier = petModifier,
            )
        } else {
            AnimatedPetAsset(
                assetPath = petAnimation.assetPath(state.resources.hairColour),
                fallbackImageRes = petImageRes,
                loop = petAnimation == HomePetAnimation.Sleepy,
                onFinished = {
                    oneShotPetAnimation = null
                },
                modifier = petModifier,
            )
        }

        HomeBottomPanel(
            state = state,
            tutorialHighlightedSections = tutorialHighlightedSections(state.homeTutorialStep) +
                if (state.morningRoutineTutorialStep != null) {
                    setOf(HomeSection.Tasks)
                } else {
                    emptySet()
                },
            onSectionClick = { onAction(PetAction.SelectSection(it)) },
            onBuyClick = { item ->
                oneShotPetAnimation = when (item) {
                    TaskItem.GetReady -> HomePetAnimation.Brushing
                    TaskItem.Lessons -> HomePetAnimation.Lessons
                    else -> oneShotPetAnimation
                }
                onAction(PetAction.BuyCareItem(item))
            },
            onUnavailableClick = { message, emphasizeMoney ->
                onAction(PetAction.ShowMessage(message, emphasizeMoney))
            },
            metrics = metrics,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .height(metrics.bottomPanelHeight),
        )
    }

    state.homeTutorialStep?.let { step ->
        HomeTutorialOverlay(
            step = step,
            onNext = { onAction(PetAction.AdvanceHomeTutorial) },
        )
    }
    state.morningRoutineTutorialStep?.let { step ->
        MorningRoutineTutorialOverlay(
            step = step,
            onNext = { onAction(PetAction.AdvanceMorningRoutineTutorial) },
        )
    }
    if (state.demoModeHintVisible) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onAction(PetAction.DismissDemoModeHint) },
        ) {
            PetSpeechBubble(
                text = stringResource(R.string.demo_mode_hint),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 170.dp, start = 24.dp, end = 24.dp)
                    .widthIn(max = 320.dp),
            )
        }
    }
}

@Composable
private fun AnimatedPetAsset(
    assetPath: String,
    @DrawableRes fallbackImageRes: Int,
    loop: Boolean,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val currentOnFinished by rememberUpdatedState(onFinished)
    val drawable = remember(context, assetPath) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeDrawable(ImageDecoder.createSource(context.assets, assetPath))
        } else {
            null
        }
    }

    if (drawable == null) {
        Image(
            painter = painterResource(fallbackImageRes),
            contentDescription = stringResource(R.string.pet),
            contentScale = ContentScale.Fit,
            modifier = modifier,
        )
        LaunchedEffect(assetPath, loop) {
            if (!loop) currentOnFinished()
        }
        return
    }

    DisposableEffect(drawable, loop) {
        val animatedDrawable = drawable as? AnimatedImageDrawable
        val callback = if (!loop && animatedDrawable != null) {
            object : Animatable2.AnimationCallback() {
                override fun onAnimationEnd(endedDrawable: android.graphics.drawable.Drawable?) {
                    currentOnFinished()
                }
            }
        } else {
            null
        }
        animatedDrawable?.repeatCount = if (loop) {
            AnimatedImageDrawable.REPEAT_INFINITE
        } else {
            0
        }
        if (callback != null && animatedDrawable != null) {
            animatedDrawable.registerAnimationCallback(callback)
        }
        animatedDrawable?.start()
        onDispose {
            if (callback != null && animatedDrawable != null) {
                animatedDrawable.unregisterAnimationCallback(callback)
            }
            animatedDrawable?.stop()
        }
    }

    AndroidView(
        factory = { viewContext ->
            ImageView(viewContext).apply {
                scaleType = ImageView.ScaleType.FIT_CENTER
                setImageDrawable(drawable)
                contentDescription = viewContext.getString(R.string.pet)
            }
        },
        update = { imageView -> imageView.setImageDrawable(drawable) },
        modifier = modifier,
    )
}

private const val PET_MAX_STRETCH = 1.05f

internal fun petScaleForDrag(
    dragOffsetPx: Float,
    fullStretchDistancePx: Float,
): Float {
    if (fullStretchDistancePx <= 0f) return 1f
    val stretchProgress = (kotlin.math.abs(dragOffsetPx) / fullStretchDistancePx).coerceIn(0f, 1f)
    return 1f + stretchProgress * (PET_MAX_STRETCH - 1f)
}

@Composable
private fun InteractivePetImage(
    @DrawableRes imageRes: Int,
    reactionNonce: Int,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val animationScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val fullStretchDistancePx = with(density) { 220.dp.toPx() }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    val tapScale = remember { Animatable(1f) }
    val tapRotation = remember { Animatable(0f) }
    val heartProgress = remember { Animatable(0f) }
    var showHeart by remember { mutableStateOf(false) }
    var heartJob by remember { mutableStateOf<Job?>(null) }
    val interactionSource = remember { MutableInteractionSource() }

    val animateTap = {
        animationScope.launch {
            tapScale.snapTo(0.94f)
            tapScale.animateTo(1.05f, tween(110))
            tapScale.animateTo(
                1f,
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium,
                ),
            )
        }
        animationScope.launch {
            tapRotation.snapTo(-2.5f)
            tapRotation.animateTo(2.5f, tween(130))
            tapRotation.animateTo(
                0f,
                spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium,
                ),
            )
        }
    }

    val showRewardHeart = {
        heartJob?.cancel()
        heartJob = animationScope.launch {
            showHeart = true
            heartProgress.snapTo(0f)
            heartProgress.animateTo(1f, tween(1_350))
            showHeart = false
        }
    }

    LaunchedEffect(reactionNonce) {
        if (reactionNonce > 0) showRewardHeart()
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = petScaleForDrag(dragOffset.x, fullStretchDistancePx)
                scaleY = petScaleForDrag(dragOffset.y, fullStretchDistancePx)
                transformOrigin = TransformOrigin(
                    pivotFractionX = when {
                        dragOffset.x > 0f -> 0f
                        dragOffset.x < 0f -> 1f
                        else -> 0.5f
                    },
                    pivotFractionY = when {
                        dragOffset.y > 0f -> 0f
                        dragOffset.y < 0f -> 1f
                        else -> 0.5f
                    },
                )
            }
            .pointerInput(fullStretchDistancePx) {
                var reboundJob: Job? = null
                val rebound = {
                    reboundJob?.cancel()
                    val startOffset = dragOffset
                    reboundJob = animationScope.launch {
                        Animatable(1f).animateTo(
                            0f,
                            spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow,
                            ),
                        ) {
                            dragOffset = startOffset * value
                        }
                    }
                }
                detectDragGestures(
                    onDragStart = {
                        reboundJob?.cancel()
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragOffset = Offset(
                            x = (dragOffset.x + dragAmount.x).coerceIn(
                                -fullStretchDistancePx,
                                fullStretchDistancePx,
                            ),
                            y = (dragOffset.y + dragAmount.y).coerceIn(
                                -fullStretchDistancePx,
                                fullStretchDistancePx,
                            ),
                        )
                    },
                    onDragEnd = rebound,
                    onDragCancel = rebound,
                )
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    animateTap()
                    onTap()
                },
            ),
        contentAlignment = Alignment.TopCenter,
    ) {
        Image(
            painter = painterResource(imageRes),
            contentDescription = stringResource(R.string.pet),
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = tapScale.value
                    scaleY = tapScale.value
                    rotationZ = tapRotation.value
                },
        )
        if (showHeart) {
            val heart = stringResource(R.string.pet_tap_reaction)
            listOf(
                floatArrayOf(-30f, 0f, 0f, 0.82f),
                floatArrayOf(0f, -10f, 0.1f, 1.08f),
                floatArrayOf(30f, 2f, 0.2f, 0.94f),
            ).forEach { (xOffset, yOffset, delayFraction, sizeFactor) ->
                val progress = ((heartProgress.value - delayFraction) /
                    (1f - delayFraction)).coerceIn(0f, 1f)
                val heartAlpha = when {
                    progress < 0.16f -> progress / 0.16f
                    else -> ((1f - progress) / 0.84f).coerceIn(0f, 1f)
                }
                Text(
                    text = heart,
                    modifier = Modifier
                        .padding(top = 18.dp)
                        .offset(
                            x = xOffset.dp,
                            y = (yOffset - 66f * progress).dp,
                        )
                        .graphicsLayer {
                            alpha = heartAlpha
                            scaleX = sizeFactor * (0.7f + progress * 0.3f)
                            scaleY = sizeFactor * (0.7f + progress * 0.3f)
                        },
                    color = Color(0xFFFF6C9D),
                    fontSize = 27.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
            }
        }
    }
}

private fun tutorialHighlightedSections(step: Int?): Set<HomeSection> = when (step) {
    4, 5 -> setOf(HomeSection.Food)
    6 -> setOf(HomeSection.Happiness, HomeSection.Energy)
    7 -> setOf(HomeSection.Food, HomeSection.Happiness, HomeSection.Energy)
    8 -> setOf(HomeSection.Shop)
    9, 10 -> setOf(HomeSection.Tasks)
    else -> emptySet()
}

@Composable
private fun HomeTutorialOverlay(step: Int, onNext: () -> Unit) {
    val text = if (step == 1) {
        stringResource(
            R.string.home_tutorial_cycle,
            gameCycleDurationText(GameDefaults.CYCLE_DURATION_SECONDS),
        )
    } else {
        stringResource(
            when (step) {
            0 -> R.string.home_tutorial_goal
            2 -> R.string.home_tutorial_budget
            3 -> R.string.home_tutorial_deposit
            4 -> R.string.home_tutorial_food
            5 -> R.string.home_tutorial_food_options
            6 -> R.string.home_tutorial_play_rest
            7 -> R.string.home_tutorial_stats
            8 -> R.string.home_tutorial_shop
            9 -> R.string.home_tutorial_tasks
            else -> R.string.home_tutorial_earnings
            },
        )
    }
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = onNext),
    ) {
        val bottomPanelHeight = (maxHeight * 0.28f).coerceIn(200.dp, 255.dp)
        PetSpeechBubble(
            text = text,
            smallerText = true,
            tailAtTopCenter = true,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(
                    start = 24.dp,
                    end = 24.dp,
                    bottom = bottomPanelHeight + 12.dp,
                )
                .widthIn(max = 320.dp),
        )
    }
}

@Composable
private fun MorningRoutineTutorialOverlay(step: Int, onNext: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(if (step == 0) Modifier.clickable(onClick = onNext) else Modifier),
    ) {
        PetSpeechBubble(
            text = stringResource(
                when (step) {
                    0 -> R.string.morning_tutorial_intro
                    1 -> R.string.morning_tutorial_get_ready
                    else -> R.string.morning_tutorial_lessons
                },
            ),
            smallerText = true,
            tailAtTopCenter = true,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 24.dp)
                .offset(y = (-72).dp)
                .widthIn(max = 320.dp),
        )
    }
}

@Composable
private fun HomeBackground(metrics: HomeLayoutMetrics, roomDirtLevel: Int) {
    Box(Modifier.fillMaxWidth().height(metrics.backdropHeight)) {
        Image(
            painter = painterResource(roomBackgroundResource(roomDirtLevel)),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().padding(top = metrics.backdropTopInset),
        )
        Box(
            Modifier
                .align(Alignment.TopCenter)
                .offset(y = metrics.backdropTopInset)
                .fillMaxWidth()
                .height(metrics.topFadeHeight)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(HomeSurface, HomeSurface.copy(alpha = 0.72f), Color.Transparent),
                    ),
                ),
        )
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(metrics.bottomFadeHeight)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, HomeSurface.copy(alpha = 0.78f), HomeSurface),
                    ),
                ),
        )
    }
}

@DrawableRes
internal fun roomBackgroundResource(roomDirtLevel: Int): Int = when (
    roomDirtLevel.coerceIn(1, 5)
) {
    1 -> R.drawable.background_1
    2 -> R.drawable.background_2
    3 -> R.drawable.background_3
    4 -> R.drawable.background_4
    else -> R.drawable.background_5
}

@Composable
private fun HomeHeader(
    state: PetState,
    onMenuClick: () -> Unit,
    onGoalClick: () -> Unit,
    tutorialHighlighted: Boolean,
    metrics: HomeLayoutMetrics,
    modifier: Modifier = Modifier,
) {
    val goalTarget = state.resources.goalTarget.coerceAtLeast(1)
    val progress = (state.resources.money.toFloat() / goalTarget).coerceIn(0f, 1f)

    Box(
        modifier
            .fillMaxWidth()
            .height(metrics.headerHeight)
            .padding(horizontal = metrics.horizontalPadding)
            .then(
                if (tutorialHighlighted) {
                    Modifier
                        .border(3.dp, Color(0xFFFFC928), RoundedCornerShape(12.dp))
                        .graphicsLayer {
                            scaleX = 1.03f
                            scaleY = 1.03f
                        }
                } else {
                    Modifier
                },
            ),
    ) {
        if (state.isTestMode) {
            Surface(
                color = HomePurple.copy(alpha = 0.10f),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 2.dp),
            ) {
                Text(
                    text = stringResource(R.string.demo_mode_label),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    color = HomePurpleDark.copy(alpha = 0.72f),
                    fontSize = 8.sp,
                    lineHeight = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = NunitoFontFamily,
                    maxLines = 1,
                )
            }
        }
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth(0.74f)
                .padding(top = metrics.headerHeight * (27f / 126f))
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onGoalClick)
                .padding(horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val day = dayForPeriod(state.resources.currentPeriod)
            val weekday = stringArrayResource(R.array.weekdays_short)[day - 1]
            Text(
                text = stringResource(
                    R.string.period_value,
                    day,
                    weekday,
                    weekForPeriod(state.resources.currentPeriod) + 1,
                ),
                color = HomePurple,
                fontSize = 16.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = NunitoFontFamily,
            )
            Text(
                text = state.resources.goalTitle,
                modifier = Modifier.fillMaxWidth(),
                color = HomePurpleDark,
                autoSize = TextAutoSize.StepBased(
                    minFontSize = 11.sp,
                    maxFontSize = 24.sp,
                    stepSize = 0.5.sp,
                ),
                lineHeight = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = NunitoFontFamily,
                textAlign = TextAlign.Center,
                maxLines = 1,
                softWrap = false,
            )
        }

        IconButton(
            onClick = onMenuClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = metrics.headerHeight * (8f / 126f)),
        ) {
            Image(
                painter = painterResource(R.drawable.ic_menu),
                contentDescription = stringResource(R.string.open_menu),
                modifier = Modifier.size(27.dp),
            )
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onGoalClick),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HeaderValue(
                icon = R.drawable.coin_money,
                value = state.resources.money.toString(),
                observedValue = state.resources.money * 31 + state.moneyAttentionNonce,
                width = metrics.headerValueWidth,
            )
            LinearProgressIndicator(
                progress = { progress },
                color = HomePurple,
                trackColor = HomeTrack,
                gapSize = 0.dp,
                drawStopIndicator = {},
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 9.dp)
                    .height(8.dp)
                    .clip(CircleShape),
            )
            HeaderValue(
                icon = R.drawable.goal_target,
                value = goalTarget.toString(),
                width = metrics.headerValueWidth,
            )
        }
    }
}

@Composable
private fun HeaderValue(
    @DrawableRes icon: Int,
    value: String,
    observedValue: Int? = null,
    width: Dp,
) {
    val iconScale = rememberChangePulseScale(observedValue)
    Column(
        modifier = Modifier.width(width),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier
                .size(24.dp)
                .graphicsLayer {
                    scaleX = iconScale
                    scaleY = iconScale
                },
        )
        Text(
            text = value,
            modifier = Modifier.fillMaxWidth(),
            color = HomePurpleDark,
            autoSize = TextAutoSize.StepBased(
                minFontSize = 8.sp,
                maxFontSize = 14.sp,
                stepSize = 0.5.sp,
            ),
            lineHeight = 16.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            softWrap = false,
        )
    }
}

@Composable
private fun QuickActions(
    secondsRemaining: Int,
    highlightCycle: Boolean,
    highlightBudget: Boolean,
    highlightDeposit: Boolean,
    onBudgetClick: () -> Unit,
    onDepositClick: () -> Unit,
    onSkipCycle: () -> Unit,
    tileSize: Dp,
    modifier: Modifier = Modifier,
) {
    val timerProgress =
        secondsRemaining.coerceIn(0, TIMER_SECONDS).toFloat() / TIMER_SECONDS

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = tileSize * (17f / 53f)),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        ActionTile(size = tileSize, onClick = onSkipCycle, highlighted = highlightCycle) {
            CircularProgressIndicator(
                progress = { timerProgress },
                modifier = Modifier.size(38.dp),
                color = Color.White,
                trackColor = Color(0xFFBB82EF),
                strokeWidth = 2.dp,
                gapSize = 0.dp,
                strokeCap = StrokeCap.Round,
            )
            Text(
                text = roundedMinutesRemaining(secondsRemaining).toString(),
                color = Color.White,
                fontSize = 15.sp,
                modifier = Modifier.align(Alignment.Center),
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            ActionTile(
                size = tileSize,
                onClick = onBudgetClick,
                highlighted = highlightBudget,
            ) {
                Image(
                    painter = painterResource(R.drawable.carbon_piggy_bank),
                    contentDescription = stringResource(R.string.budget_current),
                    modifier = Modifier.align(Alignment.Center).size(33.dp),
                )
            }
            ActionTile(
                size = tileSize,
                onClick = onDepositClick,
                highlighted = highlightDeposit,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_deposit_wallet),
                    contentDescription = stringResource(R.string.deposit_title),
                    modifier = Modifier.align(Alignment.Center).size(33.dp),
                )
            }
        }
    }
}

@Composable
private fun ActionTile(
    size: Dp,
    onClick: (() -> Unit)? = null,
    highlighted: Boolean = false,
    content: @Composable BoxScope.() -> Unit,
) {
    val interactionModifier = if (onClick == null) Modifier else Modifier.clickable(onClick = onClick)
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(10.dp))
            .background(HomePurple)
            .then(
                if (highlighted) {
                    Modifier.border(3.dp, Color(0xFFFFC928), RoundedCornerShape(10.dp))
                } else {
                    Modifier
                },
            )
            .graphicsLayer {
                val scale = if (highlighted) 1.12f else 1f
                scaleX = scale
                scaleY = scale
            }
            .then(interactionModifier),
        contentAlignment = Alignment.Center,
        content = content,
    )
}

@Composable
private fun HomeBottomPanel(
    state: PetState,
    tutorialHighlightedSections: Set<HomeSection>,
    onSectionClick: (HomeSection) -> Unit,
    onBuyClick: (CardItem) -> Unit,
    onUnavailableClick: (String, Boolean) -> Unit,
    metrics: HomeLayoutMetrics,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth()) {
        HomeTabs(
            state = state,
            tutorialHighlightedSections = tutorialHighlightedSections,
            onSectionClick = onSectionClick,
            horizontalPadding = metrics.horizontalPadding,
            itemGap = metrics.itemGap,
            modifier = Modifier.fillMaxWidth().height(metrics.tabHeight),
        )
        Column(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(HomeSurface),
        ) {
            when (state.selectedSection) {
                HomeSection.Food -> CardRow(
                    items = FoodItems,
                    money = state.resources.money,
                    currentHealth = state.resources.health,
                    currentHappiness = state.resources.happiness,
                    currentEnergy = state.resources.energy,
                    level = state.resources.level,
                    cooldownUnlockCycles = state.resources.cooldownUnlockCycles,
                    currentPeriod = state.resources.currentPeriod,
                    cycleSecondsRemaining = state.secondsRemaining,
                    onBuyClick = onBuyClick,
                    horizontalPadding = metrics.horizontalPadding,
                    itemGap = metrics.itemGap,
                    onUnavailableClick = onUnavailableClick,
                )
                HomeSection.Happiness -> CardRow(
                    items = HappinessItems,
                    money = state.resources.money,
                    currentHealth = state.resources.health,
                    currentHappiness = state.resources.happiness,
                    currentEnergy = state.resources.energy,
                    level = state.resources.level,
                    cooldownUnlockCycles = state.resources.cooldownUnlockCycles,
                    currentPeriod = state.resources.currentPeriod,
                    cycleSecondsRemaining = state.secondsRemaining,
                    onBuyClick = onBuyClick,
                    horizontalPadding = metrics.horizontalPadding,
                    itemGap = metrics.itemGap,
                    onUnavailableClick = onUnavailableClick,
                )
                HomeSection.Energy -> CardRow(
                    items = EnergyItems,
                    money = state.resources.money,
                    currentHealth = state.resources.health,
                    currentHappiness = state.resources.happiness,
                    currentEnergy = state.resources.energy,
                    level = state.resources.level,
                    cooldownUnlockCycles = state.resources.cooldownUnlockCycles,
                    currentPeriod = state.resources.currentPeriod,
                    cycleSecondsRemaining = state.secondsRemaining,
                    onBuyClick = onBuyClick,
                    horizontalPadding = metrics.horizontalPadding,
                    itemGap = metrics.itemGap,
                    onUnavailableClick = onUnavailableClick,
                )
                HomeSection.Shop -> CardRow(
                    items = ShopItems,
                    money = state.resources.money,
                    currentHealth = state.resources.health,
                    currentHappiness = state.resources.happiness,
                    currentEnergy = state.resources.energy,
                    level = state.resources.level,
                    cooldownUnlockCycles = state.resources.cooldownUnlockCycles,
                    currentPeriod = state.resources.currentPeriod,
                    cycleSecondsRemaining = state.secondsRemaining,
                    onBuyClick = onBuyClick,
                    horizontalPadding = metrics.horizontalPadding,
                    itemGap = metrics.itemGap,
                    onUnavailableClick = onUnavailableClick,
                    purchasedItemIds = state.resources.purchasedShopItemIds,
                    nextPurchaseHalfPrice =
                        "nextPurchaseHalfPrice" in state.resources.eventFlags,
                )
                HomeSection.Tasks -> CardRow(
                    items = TaskItems,
                    money = state.resources.money,
                    currentHealth = state.resources.health,
                    currentHappiness = state.resources.happiness,
                    currentEnergy = state.resources.energy,
                    level = state.resources.level,
                    cooldownUnlockCycles = state.resources.cooldownUnlockCycles,
                    currentPeriod = state.resources.currentPeriod,
                    cycleSecondsRemaining = state.secondsRemaining,
                    onBuyClick = onBuyClick,
                    horizontalPadding = metrics.horizontalPadding,
                    itemGap = metrics.itemGap,
                    taskUseCounts = state.resources.taskUseCounts,
                    taskWeeklyUseCounts = state.resources.taskWeeklyUseCounts,
                    taskUseWeeks = state.resources.taskUseWeeks,
                    eventUnlockedTaskIds = state.resources.eventUnlockedTaskIds,
                    jobRemainingActions = state.resources.jobRemainingActions,
                    jobForTaskIds = state.resources.jobForTaskIds,
                    jobEffectsForTaskIds = state.resources.jobEffectsForTaskIds,
                    purchasedItemIds = state.resources.purchasedShopItemIds,
                    tutorialTargetItemId = when (state.morningRoutineTutorialStep) {
                        1 -> TaskItem.GetReady.storageId
                        2 -> TaskItem.Lessons.storageId
                        else -> null
                    },
                    onUnavailableClick = onUnavailableClick,
                )
            }
        }
    }
}

@Composable
private fun HomeTabs(
    state: PetState,
    tutorialHighlightedSections: Set<HomeSection>,
    onSectionClick: (HomeSection) -> Unit,
    horizontalPadding: Dp,
    itemGap: Dp,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(horizontal = horizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(itemGap),
        verticalAlignment = Alignment.Bottom,
    ) {
        HomeTab(
            icon = R.drawable.ic_food,
            label = stringResource(R.string.food),
            selected = state.selectedSection == HomeSection.Food,
            progress = state.resources.health / 100f,
            observedValue = state.resources.health,
            tutorialHighlighted = HomeSection.Food in tutorialHighlightedSections,
            modifier = Modifier.weight(1f),
            onClick = { onSectionClick(HomeSection.Food) },
        )
        HomeTab(
            icon = R.drawable.ic_happy,
            label = stringResource(R.string.happiness),
            selected = state.selectedSection == HomeSection.Happiness,
            progress = state.resources.happiness / 100f,
            observedValue = state.resources.happiness,
            tutorialHighlighted = HomeSection.Happiness in tutorialHighlightedSections,
            modifier = Modifier.weight(1f),
            onClick = { onSectionClick(HomeSection.Happiness) },
        )
        HomeTab(
            icon = R.drawable.ic_energy,
            label = stringResource(R.string.energy),
            selected = state.selectedSection == HomeSection.Energy,
            progress = state.resources.energy / 100f,
            observedValue = state.resources.energy,
            tutorialHighlighted = HomeSection.Energy in tutorialHighlightedSections,
            modifier = Modifier.weight(1f),
            onClick = { onSectionClick(HomeSection.Energy) },
        )
        HomeTab(
            icon = R.drawable.ic_shop,
            label = stringResource(R.string.shop),
            selected = state.selectedSection == HomeSection.Shop,
            progress = null,
            observedValue = null,
            tutorialHighlighted = HomeSection.Shop in tutorialHighlightedSections,
            modifier = Modifier.weight(1f),
            onClick = { onSectionClick(HomeSection.Shop) },
        )
        HomeTab(
            icon = R.drawable.ic_tasks,
            label = stringResource(R.string.tasks),
            selected = state.selectedSection == HomeSection.Tasks,
            progress = null,
            observedValue = null,
            tutorialHighlighted = HomeSection.Tasks in tutorialHighlightedSections,
            modifier = Modifier.weight(1f),
            onClick = { onSectionClick(HomeSection.Tasks) },
        )
    }
}

@Composable
private fun HomeTab(
    @DrawableRes icon: Int,
    label: String,
    selected: Boolean,
    progress: Float?,
    observedValue: Int?,
    tutorialHighlighted: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val background = if (selected || tutorialHighlighted) HomeSurface else HomePurple
    val iconScale = rememberChangePulseScale(observedValue)
    Box(
        modifier = modifier
            .fillMaxHeight()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.Tab,
            ),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 8.dp)
                .clip(RoundedCornerShape(topStart = 11.dp, topEnd = 11.dp))
                .background(background)
                .then(
                    if (tutorialHighlighted) {
                        Modifier.border(3.dp, Color(0xFFFFC928), RoundedCornerShape(11.dp))
                    } else {
                        Modifier
                    },
                )
                .padding(top = 13.dp, bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Image(
                painter = painterResource(icon),
                contentDescription = label,
                colorFilter = ColorFilter.tint(
                    if (selected || tutorialHighlighted) HomePurple else Color.White,
                ),
                modifier = Modifier
                    .size(if (tutorialHighlighted) 36.dp else 27.dp)
                    .graphicsLayer {
                        scaleX = iconScale
                        scaleY = iconScale
                    },
            )
            if (progress != null) {
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    color = if (selected) HomePurple else Color.White,
                    trackColor = if (selected) HomeTrack else Color(0xFFBB82EF),
                    modifier = Modifier.width(38.dp).height(4.dp).clip(CircleShape),
                    gapSize = 0.dp,
                    drawStopIndicator = {},
                )
            } else {
                Spacer(Modifier.height(4.dp))
            }
        }
        if (observedValue != null) {
            Surface(
                color = Color(0xFFE8C5FA),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-4).dp)
                    .height(18.dp)
                    .graphicsLayer {
                        scaleX = iconScale
                        scaleY = iconScale
                    },
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 7.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = observedValue.toString(),
                        color = HomePurple,
                        fontFamily = NunitoFontFamily,
                        fontSize = 10.sp,
                        lineHeight = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun rememberChangePulseScale(value: Int?): Float {
    val scale = remember { Animatable(1f) }
    var previousValue by remember { mutableStateOf(value) }

    LaunchedEffect(value) {
        if (value != null && previousValue != null && value != previousValue) {
            previousValue = value
            scale.snapTo(1f)
            scale.animateTo(1.28f, animationSpec = tween(durationMillis = 120))
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(dampingRatio = 0.45f, stiffness = 650f),
            )
        } else {
            previousValue = value
        }
    }

    return scale.value
}

@Composable
private fun SectionPlaceholder(
    section: HomeSection,
    horizontalPadding: Dp,
    verticalPadding: Dp,
) {
    val sectionName = when (section) {
        HomeSection.Food -> stringResource(R.string.food)
        HomeSection.Happiness -> stringResource(R.string.happiness)
        HomeSection.Energy -> stringResource(R.string.energy)
        HomeSection.Shop -> stringResource(R.string.shop)
        HomeSection.Tasks -> stringResource(R.string.tasks)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = horizontalPadding, vertical = verticalPadding)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, HomePurpleDark, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = sectionName,
                color = HomePurpleDark,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(5.dp))
            Text(
                text = stringResource(R.string.section_placeholder),
                color = HomePurpleDark.copy(alpha = 0.62f),
                fontSize = 13.sp,
            )
        }
    }
}

@Composable
private fun CardRow(
    items: List<CardItemUI>,
    money: Int,
    currentHealth: Int,
    currentHappiness: Int,
    currentEnergy: Int,
    level: Int,
    cooldownUnlockCycles: Map<String, Double>,
    currentPeriod: Int,
    cycleSecondsRemaining: Int,
    onBuyClick: (CardItem) -> Unit,
    horizontalPadding: Dp,
    itemGap: Dp,
    purchasedItemIds: Set<String> = emptySet(),
    taskUseCounts: Map<String, Int> = emptyMap(),
    taskWeeklyUseCounts: Map<String, Int> = emptyMap(),
    taskUseWeeks: Map<String, Int> = emptyMap(),
    eventUnlockedTaskIds: Set<String> = emptySet(),
    jobRemainingActions: Map<String, Int> = emptyMap(),
    jobForTaskIds: Map<String, String> = emptyMap(),
    jobEffectsForTaskIds: Map<String, List<Effect>> = emptyMap(),
    nextPurchaseHalfPrice: Boolean = false,
    tutorialTargetItemId: String? = null,
    onUnavailableClick: (String, Boolean) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
) {
    if (items.isEmpty()) return

    val pages = items.chunked(PRODUCTS_PER_PAGE)
    val pagerState = rememberPagerState(pageCount = { pages.size })

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = horizontalPadding, vertical = itemGap),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth().weight(1f),
            pageSpacing = itemGap,
            userScrollEnabled = pages.size > 1,
        ) { page ->
            val pageItems = pages[page]

            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(itemGap),
            ) {
                pageItems.forEach { product ->
                    val alreadyPurchased = product.item.storageId in purchasedItemIds
                    val taskAvailable = (product.item as? TaskItem)?.let { task ->
                        task.isAvailable(
                            currentPeriod = currentPeriod,
                            totalUses = taskUseCounts[task.storageId] ?: 0,
                            weeklyUses = taskWeeklyUseCounts[task.storageId] ?: 0,
                            usageWeek = taskUseWeeks[task.storageId] ?: -1,
                            completedTaskCounts = taskUseCounts,
                            eventUnlocked = task.storageId in eventUnlockedTaskIds,
                        )
                    } ?: true
                    val weeklyTaskLimitReached = (product.item as? TaskItem)?.let { task ->
                        val weeklyLimit = task.usageLimit as? TaskUsageLimit.PerWeek
                        val currentWeek = weekForPeriod(currentPeriod)
                        weeklyLimit != null &&
                            taskUseWeeks[task.storageId] == currentWeek &&
                            (taskWeeklyUseCounts[task.storageId] ?: 0) >= weeklyLimit.count
                    } ?: false
                    val cycleRecoveryAction = cycleSecondsRemaining == 0 &&
                        currentEnergy == 0 &&
                        (product.item == EnergyItem.TakeASeat ||
                            product.item == EnergyItem.TakeANap)
                    val remainingCooldown = if (cycleRecoveryAction) null else cooldownRemaining(
                        cooldown = product.item.cooldown,
                        unlockCycle = cooldownUnlockCycles[product.item.storageId] ?: 0.0,
                        currentPeriod = currentPeriod,
                        cycleSecondsRemaining = cycleSecondsRemaining,
                    )
                    val effectivePrice = if (
                        nextPurchaseHalfPrice && product.item is ShopItem
                    ) {
                        product.item.price / 2
                    } else {
                        product.item.price
                    }
                    val task = product.item as? TaskItem
                    val appliedEffects = task
                        ?.let { jobEffectsForTaskIds[it.storageId] }
                        ?: product.item.careEffects
                    val hasEnoughResources = canApplyEffects(
                        health = currentHealth,
                        happiness = currentHappiness,
                        energy = currentEnergy,
                        money = money,
                        effects = appliedEffects + Effect(Resource.Money, -effectivePrice),
                    )
                    val shopPeriodUnlocked = (product.item as? ShopItem)
                        ?.isUnlockedAtPeriod(currentPeriod) ?: true
                    val cyclePosition = currentCyclePosition(currentPeriod, cycleSecondsRemaining)
                    val preparationBlock = if (task?.earnings?.let { it > 0 } == true) {
                        workPreparationBlock(
                            currentPeriod = currentPeriod,
                            currentCyclePosition = cyclePosition,
                            getReadyUnlockCycle =
                                cooldownUnlockCycles[TaskItem.GetReady.storageId] ?: 0.0,
                            lessonsUnlockCycle =
                                cooldownUnlockCycles[TaskItem.Lessons.storageId] ?: 0.0,
                            purchasedShopItemIds = purchasedItemIds,
                        )
                    } else {
                        null
                    }
                    val unavailableReason = when {
                        alreadyPurchased -> stringResource(R.string.item_already_purchased)
                        !shopPeriodUnlocked ->
                            stringResource(R.string.shop_morning_automation_day_locked)
                        product.item is TaskItem && product.item.requiresEvent &&
                            product.item.storageId !in eventUnlockedTaskIds ->
                            stringResource(R.string.item_needs_event)
                        preparationBlock == WorkPreparationBlock.GetReady ->
                            stringResource(R.string.work_get_ready)
                        preparationBlock == WorkPreparationBlock.Lessons ->
                            stringResource(R.string.work_lessons)
                        money < effectivePrice -> stringResource(R.string.item_not_enough_money)
                        !hasEnoughResources -> stringResource(R.string.item_not_enough_resources)
                        level < product.item.level ->
                            stringResource(R.string.item_level_locked, product.item.level)
                        remainingCooldown is CooldownRemaining.Time -> stringResource(
                            R.string.item_on_cooldown,
                            formatCountdown(remainingCooldown.seconds),
                        )
                        weeklyTaskLimitReached ->
                            stringResource(R.string.task_available_next_week)
                        product.item is TaskItem &&
                            dayForPeriod(currentPeriod) !in product.item.activeDays -> stringResource(
                                if (product.item.activeDays.last <= 5) {
                                    R.string.item_weekdays_only
                                } else {
                                    R.string.item_weekends_only
                                },
                            )
                        product.item is TaskItem &&
                            product.item.usageLimit is TaskUsageLimit.Total ->
                            stringResource(R.string.item_limit_reached)
                        else -> stringResource(R.string.task_unavailable)
                    }
                    val canBuy = money >= effectivePrice &&
                        remainingCooldown == null &&
                        (level >= product.item.level ||
                            (product.item is TaskItem &&
                                product.item.storageId in eventUnlockedTaskIds)) &&
                        taskAvailable &&
                        preparationBlock == null &&
                        hasEnoughResources &&
                        shopPeriodUnlocked &&
                        !alreadyPurchased
                    ProductCard(
                        product = product,
                        canBuy = canBuy,
                        alreadyPurchased = alreadyPurchased,
                        remainingCooldown = remainingCooldown,
                        weeklyTaskLimitReached = weeklyTaskLimitReached,
                        effectivePrice = effectivePrice,
                        remainingActions = (product.item as? TaskItem)
                            ?.let { jobForTaskIds[it.storageId] }
                            ?.let(jobRemainingActions::get)
                            ?.takeIf { it >= 0 },
                        tutorialHighlighted = product.item.storageId == tutorialTargetItemId,
                        onBuyClick = {
                            if (canBuy) onBuyClick(product.item)
                            else onUnavailableClick(
                                unavailableReason,
                                money < effectivePrice,
                            )
                        },
                        modifier = Modifier.weight(1f),
                    )
                }

                repeat(PRODUCTS_PER_PAGE - pageItems.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }

        Spacer(Modifier.height(4.dp))
        ProductPageIndicator(
            pageCount = pages.size,
            selectedPage = pagerState.currentPage,
        )
    }
}

private const val PRODUCTS_PER_PAGE = 3

@Composable
private fun ProductPageIndicator(
    pageCount: Int,
    selectedPage: Int,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(pageCount) { page ->
            Box(
                Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(
                        if (page == selectedPage) HomePurple
                        else HomePurple.copy(alpha = 0.25f),
                    ),
            )
        }
    }
}

@Composable
private fun ProductCard(
    product: CardItemUI,
    canBuy: Boolean,
    alreadyPurchased: Boolean,
    remainingCooldown: CooldownRemaining?,
    weeklyTaskLimitReached: Boolean,
    effectivePrice: Int,
    remainingActions: Int?,
    tutorialHighlighted: Boolean,
    onBuyClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val showsMorningAutomationDetails = product.item == ShopItem.MorningAutomation
    var showMorningAutomationDetails by rememberSaveable(product.item.storageId) {
        mutableStateOf(false)
    }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White)
            .border(
                if (tutorialHighlighted) 3.dp else 1.dp,
                if (tutorialHighlighted) Color(0xFFFFC928) else HomePurpleDark,
                RoundedCornerShape(10.dp),
            )
            .then(
                if (showsMorningAutomationDetails) {
                    Modifier.clickable { showMorningAutomationDetails = true }
                } else {
                    Modifier
                },
            )
            .padding(horizontal = 8.dp, vertical = 5.dp),
    ) {
      Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Image(
            painter = painterResource(product.illustration),
            contentDescription = stringResource(product.title),
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentScale = ContentScale.Fit,
        )
        Text(
            text = stringResource(product.title),
            modifier = Modifier.fillMaxWidth(),
            color = HomePurpleDark,
            autoSize = TextAutoSize.StepBased(
                minFontSize = 8.sp,
                maxFontSize = 11.sp,
                stepSize = 0.5.sp,
            ),
            lineHeight = 12.sp,
            textAlign = TextAlign.Center,
            maxLines = if (
                product.item is TaskItem || product.item == ShopItem.MorningAutomation
            ) 2 else 1,
        )
        if (!showsMorningAutomationDetails) {
            CareStats(product.item)
        }
        Button(
            onClick = {
                if (showsMorningAutomationDetails) {
                    showMorningAutomationDetails = true
                } else {
                    onBuyClick()
                }
            },
            enabled = true,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (canBuy) HomePurple else HomePurple.copy(alpha = 0.22f),
                disabledContainerColor = HomePurple.copy(alpha = 0.22f),
                disabledContentColor = HomePurpleDark.copy(alpha = 0.62f),
            ),
            contentPadding = PaddingValues(horizontal = 1.dp, vertical = 0.dp),
            modifier = Modifier.fillMaxWidth().heightIn(min = 28.dp, max = 32.dp),
            shape = RoundedCornerShape(27)
        ) {
            when {
                alreadyPurchased -> Text(
                    text = stringResource(R.string.goal_purchased),
                    autoSize = TextAutoSize.StepBased(
                        minFontSize = 7.sp,
                        maxFontSize = 10.sp,
                        stepSize = 0.5.sp,
                    ),
                    maxLines = 1,
                )
                remainingCooldown is CooldownRemaining.Time -> Text(
                    text = stringResource(
                        R.string.cooldown_remaining,
                        formatCountdown(remainingCooldown.seconds),
                    ),
                    autoSize = TextAutoSize.StepBased(
                        minFontSize = 7.sp,
                        maxFontSize = 10.sp,
                        stepSize = 0.5.sp,
                    ),
                    maxLines = 1,
                )
                product.item is TaskItem && !canBuy -> Text(
                    text = stringResource(
                        if (weeklyTaskLimitReached) {
                            R.string.task_available_next_week
                        } else {
                            R.string.task_unavailable
                        },
                    ),
                    autoSize = TextAutoSize.StepBased(
                        minFontSize = 7.sp,
                        maxFontSize = 10.sp,
                        stepSize = 0.5.sp,
                    ),
                    maxLines = 1,
                )
                product.item is TaskItem -> {
                    Text(
                        text = if (product.item.earnings > 0) {
                            stringResource(R.string.task_do_with_reward, product.item.earnings)
                        } else {
                            stringResource(R.string.task_do)
                        },
                        autoSize = TextAutoSize.StepBased(
                            minFontSize = 7.sp,
                            maxFontSize = 10.sp,
                            stepSize = 0.5.sp,
                        ),
                        maxLines = 1,
                    )
                }
                else -> {
                    Text(
                        text = stringResource(product.item.actionLabelRes()),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                    )
                    Spacer(Modifier.width(3.dp))
                    Text(
                        text = effectivePrice.toString(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                    )
                    Spacer(Modifier.width(2.dp))
                    Image(
                        painter = painterResource(R.drawable.coin_money),
                        contentDescription = stringResource(R.string.coins),
                        modifier = Modifier.size(13.dp),
                    )
                }
            }
        }
      }
      if (product.item is HappinessItem) {
          Image(
              painter = painterResource(R.drawable.cube),
              contentDescription = null,
              modifier = Modifier
                  .align(Alignment.TopEnd)
                  .size(30.dp),
          )
      }
      remainingActions?.let { remaining ->
          Surface(
              modifier = Modifier.align(Alignment.TopEnd),
              color = HomePurple,
              shape = CircleShape,
          ) {
              Text(
                  text = remaining.toString(),
                  modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                  color = Color.White,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
              )
          }
      }
    }

    if (showMorningAutomationDetails) {
        AlertDialog(
            onDismissRequest = { showMorningAutomationDetails = false },
            title = { Text(stringResource(R.string.shop_morning_automation)) },
            text = { Text(stringResource(R.string.shop_morning_automation_description)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showMorningAutomationDetails = false
                        if (!alreadyPurchased) onBuyClick()
                    },
                ) {
                    Text(
                        stringResource(
                            if (alreadyPurchased) R.string.goal_purchased else R.string.buy,
                        ),
                    )
                }
            },
            dismissButton = if (alreadyPurchased) {
                null
            } else {
                {
                    TextButton(onClick = { showMorningAutomationDetails = false }) {
                        Text(stringResource(R.string.cancel_action))
                    }
                }
            },
        )
    }
}

@StringRes
private fun CardItem.actionLabelRes(): Int = when (this) {
    is FoodItem -> R.string.action_eat
    HappinessItem.BudgetMaster,
    HappinessItem.ChangeMoney,
    HappinessItem.CatchMoney,
    HappinessItem.PlayWithBall,
    HappinessItem.BoardGame,
    -> R.string.action_play
    HappinessItem.MeetingWithFriends -> R.string.action_meet
    HappinessItem.Trip -> R.string.action_go
    HappinessItem.Zoo -> R.string.action_visit
    EnergyItem.TakeASeat -> R.string.action_rest
    EnergyItem.TakeANap -> R.string.action_sleep
    EnergyItem.ListenMusic -> R.string.action_listen
    EnergyItem.HotBath -> R.string.action_take
    EnergyItem.TakeAMassage,
    EnergyItem.SPA,
    EnergyItem.BodyMassage,
    EnergyItem.Yoga,
    -> R.string.action_do
    is ShopItem -> R.string.buy
    is TaskItem -> R.string.task_do
    else -> R.string.buy
}

@Composable
private fun CareStats(item: CardItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Resource.entries
            .filterNot { it == Resource.Money }
            .forEach { resource ->
            val increaseLevel = item.careEffects
                .firstOrNull { it.resource == resource }
                ?.increase
                ?: 0

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Image(
                    painter = painterResource(resource.iconResource()),
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    colorFilter = ColorFilter.tint(HomePurple),
                )
                Text(
                    text = increaseLevel.toString(),
                    color = HomePurple,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
            }
        }
    }
}

@DrawableRes
private fun Resource.iconResource(): Int = when (this) {
    Resource.Health -> R.drawable.ic_food
    Resource.Happiness -> R.drawable.ic_happy
    Resource.Energy -> R.drawable.ic_energy
    Resource.Money -> R.drawable.ic_coin
}

@Composable
private fun PageDots(isVisible: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().height(20.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isVisible) {
            Box(Modifier.size(9.dp).background(HomePurple, CircleShape))
            Spacer(Modifier.width(4.dp))
            Box(Modifier.size(9.dp).background(Color(0xFFC68BED), CircleShape))
            Spacer(Modifier.width(4.dp))
            Box(Modifier.size(9.dp).background(Color(0xFFE0B5F7), CircleShape))
        }
    }
}

@Composable
private fun MenuOverlay(
    state: PetState,
    onAction: (PetAction) -> Unit,
    onDismiss: () -> Unit,
) {
    var showRestartConfirmation by rememberSaveable { mutableStateOf(false) }
    var showDemoModePassword by rememberSaveable { mutableStateOf(false) }
    var demoModePassword by rememberSaveable { mutableStateOf("") }
    var showParentModePassword by rememberSaveable { mutableStateOf(false) }
    var parentModePassword by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(state.isTestMode) {
        if (state.isTestMode) {
            showDemoModePassword = false
            demoModePassword = ""
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(24.dp), tonalElevation = 8.dp) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    stringResource(R.string.menu),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(27)
                ) {
                    Text(stringResource(R.string.continue_action))
                }
                TextButton(
                    onClick = { showRestartConfirmation = true },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.restart_action))
                }
                TextButton(
                    onClick = { onAction(PetAction.OpenPersonalAccount) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.change_character_action))
                }
                TextButton(
                    onClick = {
                        onAction(PetAction.ClearParentModePasswordError)
                        showParentModePassword = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.parent_area_action))
                }
                TextButton(
                    onClick = {
                        if (state.isTestMode) {
                            onAction(PetAction.DisableTestMode)
                        } else {
                            onAction(PetAction.ClearDemoModePasswordError)
                            showDemoModePassword = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        stringResource(
                            if (state.isTestMode) {
                                R.string.demo_mode_disable
                            } else {
                                R.string.demo_mode_action
                            },
                        ),
                    )
                }
                TextButton(
                    onClick = { onAction(PetAction.OpenHandbook) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.handbook_title))
                }
                TextButton(
                    onClick = { onAction(PetAction.Exit) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.exit_action))
                }
            }
        }
    }

    if (showRestartConfirmation) {
        AlertDialog(
            onDismissRequest = { showRestartConfirmation = false },
            title = { Text(stringResource(R.string.restart_confirmation)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRestartConfirmation = false
                        onAction(PetAction.Restart)
                    },
                ) {
                    Text(stringResource(R.string.restart_action))
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestartConfirmation = false }) {
                    Text(stringResource(R.string.cancel_action))
                }
            },
        )
    }

    if (showDemoModePassword) {
        AlertDialog(
            onDismissRequest = {
                showDemoModePassword = false
                demoModePassword = ""
                onAction(PetAction.ClearDemoModePasswordError)
            },
            title = { Text(stringResource(R.string.demo_mode_password_title)) },
            text = {
                OutlinedTextField(
                    value = demoModePassword,
                    onValueChange = {
                        demoModePassword = it
                        if (state.demoModePasswordError) {
                            onAction(PetAction.ClearDemoModePasswordError)
                        }
                    },
                    label = { Text(stringResource(R.string.demo_mode_password_label)) },
                    isError = state.demoModePasswordError,
                    supportingText = if (state.demoModePasswordError) {
                        { Text(stringResource(R.string.demo_mode_password_error)) }
                    } else {
                        null
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { onAction(PetAction.EnableTestMode(demoModePassword)) },
                    enabled = demoModePassword.isNotEmpty(),
                ) {
                    Text(stringResource(R.string.demo_mode_enable))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDemoModePassword = false
                        demoModePassword = ""
                        onAction(PetAction.ClearDemoModePasswordError)
                    },
                ) {
                    Text(stringResource(R.string.cancel_action))
                }
            },
        )
    }

    if (showParentModePassword) {
        AlertDialog(
            onDismissRequest = {
                showParentModePassword = false
                parentModePassword = ""
                onAction(PetAction.ClearParentModePasswordError)
            },
            title = { Text(stringResource(R.string.parent_mode_password_title)) },
            text = {
                OutlinedTextField(
                    value = parentModePassword,
                    onValueChange = {
                        parentModePassword = it
                        if (state.parentModePasswordError) {
                            onAction(PetAction.ClearParentModePasswordError)
                        }
                    },
                    label = { Text(stringResource(R.string.demo_mode_password_label)) },
                    isError = state.parentModePasswordError,
                    supportingText = if (state.parentModePasswordError) {
                        { Text(stringResource(R.string.demo_mode_password_error)) }
                    } else {
                        null
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { onAction(PetAction.OpenParent(parentModePassword)) },
                    enabled = parentModePassword.isNotEmpty(),
                ) {
                    Text(stringResource(R.string.parent_mode_enter))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showParentModePassword = false
                        parentModePassword = ""
                        onAction(PetAction.ClearParentModePasswordError)
                    },
                ) {
                    Text(stringResource(R.string.cancel_action))
                }
            },
        )
    }
}

@Composable
private fun EmptyPage() {
    Box(
        Modifier.fillMaxSize().background(HomeSurface).safeDrawingPadding(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            stringResource(R.string.soon),
            color = HomePurpleDark.copy(alpha = 0.55f),
            style = MaterialTheme.typography.headlineSmall,
        )
    }
}


private const val PAGE_COUNT = 3
private const val HOME_PAGE = 1
