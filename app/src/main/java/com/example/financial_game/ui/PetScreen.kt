package com.example.financial_game.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
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
import androidx.compose.ui.window.Dialog
import com.example.financial_game.R
import com.example.financial_game.domain.CardItem
import com.example.financial_game.domain.CooldownRemaining
import com.example.financial_game.domain.FoodItem
import com.example.financial_game.domain.Resource
import com.example.financial_game.domain.ShopItem
import com.example.financial_game.domain.TaskItem
import com.example.financial_game.domain.EnergyItem
import com.example.financial_game.domain.GameEvent
import com.example.financial_game.domain.GameEvents
import com.example.financial_game.domain.HappinessItem
import com.example.financial_game.domain.Goals
import com.example.financial_game.domain.cooldownRemaining
import com.example.financial_game.domain.dayForPeriod
import com.example.financial_game.domain.isAvailable
import com.example.financial_game.domain.weekForPeriod
import com.example.financial_game.domain.needsBudgetPlanning
import com.example.financial_game.domain.needsBudgetReview
import com.example.financial_game.domain.budgetPeriodResult
import com.example.financial_game.domain.canApplyEffects
import com.example.financial_game.domain.canDeclineEvent
import com.example.financial_game.ui.theme.NunitoFontFamily

private val HomePurple = Color(0xFF8743D3)
private val HomePurpleDark = Color(0xFF4B2163)
private val HomeSurface = Color(0xFFFCF7FF)
private val HomeTrack = Color(0xFFE6C1FA)

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

    if (needsBudgetReview(state.resources)) {
        BudgetPeriodResultScreen(
            petImageRes = petImageResource(
                state.resources.level, state.resources.hairColour, state.resources.hairStyle,
            ),
            result = budgetPeriodResult(state.resources),
            plannedOptionalExpenses = state.resources.plannedOptionalExpenses,
            actualOptionalExpenses = state.resources.actualOptionalExpenses,
            actualAdditionalIncome = state.resources.actualAdditionalIncome,
            onComplete = { onAction(PetAction.CompleteBudgetReview) },
        )
        return
    }

    if (needsBudgetPlanning(state.resources)) {
        BudgetPlanningScreen(
            petImageRes = petImageResource(
                state.resources.level, state.resources.hairColour, state.resources.hairStyle,
            ),
            firstPlanning = !state.resources.budgetTutorialCompleted,
            previousOptionalExpenses = state.resources.plannedOptionalExpenses,
            onComplete = { onAction(PetAction.CompleteBudgetPlanning(it)) },
        )
        return
    }

    Box(Modifier.fillMaxSize().background(HomeSurface)) {
                HomePage(state, onAction)
    }

//    EventOverlay(GameEvents.SportsSection, acceptButtonText = "Принять")

    when (state.overlay) {
        HomeOverlay.Menu -> MenuOverlay(state, onAction)
        HomeOverlay.Budget -> BudgetOverviewScreen(
            state = state.resources,
            onBack = { onAction(PetAction.CloseOverlay) },
        )
        HomeOverlay.Deposit -> DepositScreen(
            state = state.resources,
            onBack = { onAction(PetAction.CloseOverlay) },
            onOpen = { term, amount -> onAction(PetAction.OpenDepositAccount(term, amount)) },
            onClose = { onAction(PetAction.CloseDepositAccount) },
        )
        HomeOverlay.Goal -> {
            val goal = Goals.fromStorageId(state.resources.goalId)
            GoalOverlay(
                money = state.resources.money,
                target = state.resources.goalTarget,
                goal = goal,
                alreadyPurchased = goal.name in state.resources.purchasedGoalIds,
                onBuy = {
                    onAction(PetAction.BuyGoal(goal, state.resources.goalTarget))
                },
                onDismiss = { onAction(PetAction.CloseOverlay) },
            )
        }
        HomeOverlay.GoalSelection -> {
            val availableGoals = Goals.entries
                .filter {
                    it.level <= state.resources.level &&
                        it.name !in state.resources.purchasedGoalIds
                }
            if (availableGoals.isEmpty() && state.resources.customGoal == null) {
                LaunchedEffect(Unit) { onAction(PetAction.CloseOverlay) }
            } else {
                GoalSelectionScreen(
                    goals = availableGoals,
                    onGoalSelected = { onAction(PetAction.SelectGoal(it)) },
                    customGoal = state.resources.customGoal,
                    onCustomGoalSelected = { onAction(PetAction.SelectCustomGoal) },
                )
            }
        }
        HomeOverlay.PersonalAccount -> PersonalAccountScreen(
            initialState = state.resources,
            onBack = { onAction(PetAction.CloseOverlay) },
            onApply = { onAction(PetAction.ApplyPetAppearance(it)) },
        )
        HomeOverlay.Parent -> ParentScreen(
            state = state.resources,
            onBack = { onAction(PetAction.CloseOverlay) },
            onAddGoal = { title, target, image ->
                onAction(PetAction.AddCustomGoal(title, target, image))
            },
        )
        null -> Unit
    }

    state.activeEvent?.let { event ->
        val canAcceptEvent = canApplyEffects(state.resources.money, event.acceptEffects)
        EventOverlay(
            event = event,
            acceptButtonText = stringResource(event.acceptButtonTextRes),
            declineButtonText = if (canDeclineEvent(state.resources.money, event)) {
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
}

@Composable
private fun HomePage(state: PetState, onAction: (PetAction) -> Unit) {
    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(HomeSurface)
            .safeDrawingPadding(),
    ) {
        val metrics = homeLayoutMetrics(maxWidth, maxHeight)

        HomeBackground(metrics)

        HomeHeader(
            state = state,
            onMenuClick = { onAction(PetAction.OpenMenu) },
            onGoalClick = { onAction(PetAction.OpenGoal) },
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
            highlightCycle = state.demoModeHintVisible,
            onBudgetClick = { onAction(PetAction.OpenBudget) },
            onDepositClick = { onAction(PetAction.OpenDeposit) },
            onSkipCycle = { onAction(PetAction.ForceNextCycle )},
            tileSize = metrics.quickActionSize,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = metrics.quickActionsTop),
        )

        Image(
            painter = painterResource(
                petImageResource(
                    level = state.resources.level,
                    colour = state.resources.hairColour,
                    style = state.resources.hairStyle,
                ),
            ),
            contentDescription = stringResource(R.string.pet),
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .align(Alignment.Center)
                .width(metrics.petWidth)
                .aspectRatio(292f / 360f),
        )

        HomeBottomPanel(
            state = state,
            tutorialHighlightedSections = tutorialHighlightedSections(state.homeTutorialStep),
            onSectionClick = { onAction(PetAction.SelectSection(it)) },
            onBuyClick = { onAction(PetAction.BuyCareItem(it)) },
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

private fun tutorialHighlightedSections(step: Int?): Set<HomeSection> = when (step) {
    0, 1 -> setOf(HomeSection.Food)
    2 -> setOf(HomeSection.Happiness, HomeSection.Energy)
    3 -> setOf(HomeSection.Food, HomeSection.Happiness, HomeSection.Energy)
    4 -> setOf(HomeSection.Shop)
    5, 6 -> setOf(HomeSection.Tasks)
    else -> emptySet()
}

@Composable
private fun HomeTutorialOverlay(step: Int, onNext: () -> Unit) {
    val text = stringResource(
        when (step) {
            0 -> R.string.home_tutorial_food
            1 -> R.string.home_tutorial_food_options
            2 -> R.string.home_tutorial_play_rest
            3 -> R.string.home_tutorial_stats
            4 -> R.string.home_tutorial_shop
            5 -> R.string.home_tutorial_tasks
            else -> R.string.home_tutorial_earnings
        },
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = onNext),
    ) {
        PetSpeechBubble(
            text = text,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 170.dp, start = 24.dp, end = 24.dp)
                .widthIn(max = 320.dp),
        )
    }
}

@Composable
private fun HomeBackground(metrics: HomeLayoutMetrics) {
    Box(Modifier.fillMaxWidth().height(metrics.backdropHeight)) {
        Image(
            painter = painterResource(R.drawable.home_background),
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

@Composable
private fun HomeHeader(
    state: PetState,
    onMenuClick: () -> Unit,
    onGoalClick: () -> Unit,
    metrics: HomeLayoutMetrics,
    modifier: Modifier = Modifier,
) {
    val goalTarget = state.resources.goalTarget.coerceAtLeast(1)
    val progress = (state.resources.money.toFloat() / goalTarget).coerceIn(0f, 1f)

    Box(
        modifier
            .fillMaxWidth()
            .height(metrics.headerHeight)
            .padding(horizontal = metrics.horizontalPadding),
    ) {
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
            if (state.isTestMode) {
                Text(
                    text = stringResource(R.string.demo_mode_label),
                    color = HomePurpleDark.copy(alpha = 0.72f),
                    fontSize = 9.sp,
                    lineHeight = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = NunitoFontFamily,
                )
            }
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
                observedValue = state.resources.money,
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
            ActionTile(size = tileSize, onClick = onBudgetClick) {
                Image(
                    painter = painterResource(R.drawable.carbon_piggy_bank),
                    contentDescription = stringResource(R.string.budget_current),
                    modifier = Modifier.align(Alignment.Center).size(33.dp),
                )
            }
            ActionTile(size = tileSize, onClick = onDepositClick) {
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
                    level = state.resources.level,
                    cooldownUnlockCycles = state.resources.cooldownUnlockCycles,
                    currentPeriod = state.resources.currentPeriod,
                    cycleSecondsRemaining = state.secondsRemaining,
                    onBuyClick = onBuyClick,
                    horizontalPadding = metrics.horizontalPadding,
                    itemGap = metrics.itemGap,
                )
                HomeSection.Happiness -> CardRow(
                    items = HappinessItems,
                    money = state.resources.money,
                    level = state.resources.level,
                    cooldownUnlockCycles = state.resources.cooldownUnlockCycles,
                    currentPeriod = state.resources.currentPeriod,
                    cycleSecondsRemaining = state.secondsRemaining,
                    onBuyClick = onBuyClick,
                    horizontalPadding = metrics.horizontalPadding,
                    itemGap = metrics.itemGap,
                )
                HomeSection.Energy -> CardRow(
                    items = EnergyItems,
                    money = state.resources.money,
                    level = state.resources.level,
                    cooldownUnlockCycles = state.resources.cooldownUnlockCycles,
                    currentPeriod = state.resources.currentPeriod,
                    cycleSecondsRemaining = state.secondsRemaining,
                    onBuyClick = onBuyClick,
                    horizontalPadding = metrics.horizontalPadding,
                    itemGap = metrics.itemGap,
                )
                HomeSection.Shop -> CardRow(
                    items = ShopItems,
                    money = state.resources.money,
                    level = state.resources.level,
                    cooldownUnlockCycles = state.resources.cooldownUnlockCycles,
                    currentPeriod = state.resources.currentPeriod,
                    cycleSecondsRemaining = state.secondsRemaining,
                    onBuyClick = onBuyClick,
                    horizontalPadding = metrics.horizontalPadding,
                    itemGap = metrics.itemGap,
                    purchasedItemIds = state.resources.purchasedShopItemIds,
                    nextPurchaseHalfPrice =
                        "nextPurchaseHalfPrice" in state.resources.eventFlags,
                )
                HomeSection.Tasks -> CardRow(
                    items = TaskItems,
                    money = state.resources.money,
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
                    currentEnergy = state.resources.energy,
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
        if (selected && observedValue != null) {
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
    nextPurchaseHalfPrice: Boolean = false,
    currentEnergy: Int = 100,
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
                    val remainingCooldown = cooldownRemaining(
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
                    ProductCard(
                        product = product,
                        canBuy = money >= effectivePrice &&
                            remainingCooldown == null &&
                            (level >= product.item.level ||
                                (product.item is TaskItem &&
                                    product.item.storageId in eventUnlockedTaskIds)) &&
                            taskAvailable &&
                            (product.item !is TaskItem || currentEnergy > 10) &&
                            !alreadyPurchased,
                        alreadyPurchased = alreadyPurchased,
                        remainingCooldown = remainingCooldown,
                        onBuyClick = { onBuyClick(product.item) },
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
    onBuyClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White)
            .border(1.dp, HomePurpleDark, RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 5.dp),
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
            maxLines = if (product.item is TaskItem) 2 else 1,
        )
        CareStats(product.item)
        Button(
            onClick = onBuyClick,
            enabled = canBuy,
            colors = ButtonDefaults.buttonColors(
                containerColor = HomePurple,
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
                    text = stringResource(R.string.task_unavailable),
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
                        text = product.item.price.toString(),
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
}

@StringRes
private fun CardItem.actionLabelRes(): Int = when (this) {
    is FoodItem -> R.string.action_eat
    HappinessItem.BudgetMaster,
    HappinessItem.CatchMoney,
    HappinessItem.ChangeMoney,
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
private fun MenuOverlay(state: PetState, onAction: (PetAction) -> Unit) {
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

    Dialog(onDismissRequest = { onAction(PetAction.CloseOverlay) }) {
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
                    onClick = { onAction(PetAction.CloseOverlay) },
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
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.support_action))
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
