package com.example.financial_game.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.financial_game.R
import com.example.financial_game.domain.CareItem

private val HomePurple = Color(0xFF8743D3)
private val HomePurpleDark = Color(0xFF4B2163)
private val HomeSurface = Color(0xFFFCF7FF)
private val HomeTrack = Color(0xFFE6C1FA)

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

private data class CareItemUi(
    val item: CareItem,
    val illustration: String,
    @param:StringRes val title: Int,
)

private val FoodItems = listOf(
    CareItemUi(CareItem.SchoolLunch, "🍱", R.string.school_lunch),
    CareItemUi(CareItem.Soda, "🥤", R.string.soda),
    CareItemUi(CareItem.IceCream, "🍦", R.string.ice_cream),
)

private val HappinessItems = listOf(
    CareItemUi(CareItem.ToyMouse, "🐭", R.string.toy_mouse),
    CareItemUi(CareItem.YarnBall, "🧶", R.string.yarn_ball),
    CareItemUi(CareItem.Music, "🎵", R.string.music),
)

private val EnergyItems = listOf(
    CareItemUi(CareItem.Nap, "😴", R.string.nap),
    CareItemUi(CareItem.Cocoa, "☕", R.string.cocoa),
    CareItemUi(CareItem.Pillow, "🛏️", R.string.pillow),
)

@Composable
fun PetScreen(state: PetState, onAction: (PetAction) -> Unit) {
    if (!state.isInitialized) {
        Box(Modifier.fillMaxSize().background(HomeSurface))
        return
    }

    val pagerState = rememberPagerState(initialPage = HOME_PAGE, pageCount = { PAGE_COUNT })

    Box(Modifier.fillMaxSize().background(HomeSurface)) {
                HomePage(state, onAction)
    }

    when (state.overlay) {
        HomeOverlay.Menu -> MenuOverlay(onAction)
        HomeOverlay.Shop -> ShopOverlay(
            canBuy = state.resources.money >= COLLAR_PRICE,
            onAction = onAction,
        )
        null -> Unit
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
            metrics = metrics,
            modifier = Modifier.align(Alignment.TopCenter),
        )

        QuickActions(
            currentPeriod = state.resources.currentPeriod,
            onShopClick = { onAction(PetAction.OpenShop) },
            onSkipCycle = { onAction(PetAction.ForceNextCycle )},
            tileSize = metrics.quickActionSize,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = metrics.quickActionsTop),
        )

        Image(
            painter = painterResource(R.drawable.pet_main),
            contentDescription = stringResource(R.string.pet),
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .align(Alignment.Center)
                .width(metrics.petWidth)
                .aspectRatio(292f / 360f),
        )

        HomeBottomPanel(
            state = state,
            onSectionClick = { onAction(PetAction.SelectSection(it)) },
            onBuyClick = { onAction(PetAction.BuyCareItem(it)) },
            metrics = metrics,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .height(metrics.bottomPanelHeight),
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
                .padding(top = metrics.headerHeight * (27f / 126f)),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(
                    R.string.period_value,
                    (state.resources.currentPeriod % 7),
                    state.resources.currentPeriod / 7,
                ),
                color = HomePurple,
                fontSize = 16.sp,
                lineHeight = 18.sp,
            )
            Text(
                text = state.resources.goalTitle,
                color = HomePurpleDark,
                fontSize = 27.sp,
                lineHeight = 30.sp,
                fontWeight = FontWeight.Bold,
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
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HeaderValue(
                icon = R.drawable.ic_coin,
                value = state.resources.money.toString(),
                width = metrics.headerValueWidth,
            )
            LinearProgressIndicator(
                progress = { progress },
                color = HomePurple,
                trackColor = HomeTrack,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 9.dp)
                    .height(8.dp)
                    .clip(CircleShape),
            )
            HeaderValue(
                icon = R.drawable.ic_money_bag,
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
    width: Dp,
) {
    Column(
        modifier = Modifier.width(width),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
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
    currentPeriod: Int,
    onShopClick: () -> Unit,
    onSkipCycle: () -> Unit,
    tileSize: Dp,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = tileSize * (17f / 53f)),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        ActionTile(size = tileSize, onClick = onSkipCycle) {
            Image(
                painter = painterResource(R.drawable.ic_timer_cycle),
                contentDescription = stringResource(R.string.timer),
                modifier = Modifier.size(39.dp),
            )
            Text(
                text = currentPeriod.toString(),
                color = Color.White,
                fontSize = 17.sp,
                modifier = Modifier.align(Alignment.Center),
            )
        }

        ActionTile(size = tileSize, onClick = onShopClick) {
            Image(
                painter = painterResource(R.drawable.ic_savings),
                contentDescription = stringResource(R.string.shop),
                modifier = Modifier.align(Alignment.Center).size(33.dp),
            )
        }
    }
}

@Composable
private fun ActionTile(
    size: Dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val interactionModifier = if (onClick == null) Modifier else Modifier.clickable(onClick = onClick)
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(10.dp))
            .background(HomePurple)
            .then(interactionModifier),
        contentAlignment = Alignment.Center,
        content = content,
    )
}

@Composable
private fun HomeBottomPanel(
    state: PetState,
    onSectionClick: (HomeSection) -> Unit,
    onBuyClick: (CareItem) -> Unit,
    metrics: HomeLayoutMetrics,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth()) {
        HomeTabs(
            state = state,
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
                HomeSection.Food -> ProductRow(
                    items = FoodItems,
                    money = state.resources.money,
                    onBuyClick = onBuyClick,
                    horizontalPadding = metrics.horizontalPadding,
                    itemGap = metrics.itemGap,
                )
                HomeSection.Happiness -> ProductRow(
                    items = HappinessItems,
                    money = state.resources.money,
                    onBuyClick = onBuyClick,
                    horizontalPadding = metrics.horizontalPadding,
                    itemGap = metrics.itemGap,
                )
                HomeSection.Energy -> ProductRow(
                    items = EnergyItems,
                    money = state.resources.money,
                    onBuyClick = onBuyClick,
                    horizontalPadding = metrics.horizontalPadding,
                    itemGap = metrics.itemGap,
                )
                HomeSection.Shop, HomeSection.Tasks -> SectionPlaceholder(
                    section = state.selectedSection,
                    horizontalPadding = metrics.horizontalPadding,
                    verticalPadding = metrics.itemGap,
                )
            }
        }
    }
}

@Composable
private fun HomeTabs(
    state: PetState,
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
            modifier = Modifier.weight(1f),
            onClick = { onSectionClick(HomeSection.Food) },
        )
        HomeTab(
            icon = R.drawable.ic_happy,
            label = stringResource(R.string.happiness),
            selected = state.selectedSection == HomeSection.Happiness,
            progress = state.resources.happiness / 100f,
            modifier = Modifier.weight(1f),
            onClick = { onSectionClick(HomeSection.Happiness) },
        )
        HomeTab(
            icon = R.drawable.ic_energy,
            label = stringResource(R.string.energy),
            selected = state.selectedSection == HomeSection.Energy,
            progress = state.resources.energy / 100f,
            modifier = Modifier.weight(1f),
            onClick = { onSectionClick(HomeSection.Energy) },
        )
        HomeTab(
            icon = R.drawable.ic_shop,
            label = stringResource(R.string.shop),
            selected = state.selectedSection == HomeSection.Shop,
            progress = null,
            modifier = Modifier.weight(1f),
            onClick = { onSectionClick(HomeSection.Shop) },
        )
        HomeTab(
            icon = R.drawable.ic_tasks,
            label = stringResource(R.string.tasks),
            selected = state.selectedSection == HomeSection.Tasks,
            progress = null,
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
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val background = if (selected) HomeSurface else HomePurple
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(topStart = 11.dp, topEnd = 11.dp))
            .background(background)
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.Tab,
            )
            .padding(top = 13.dp, bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Image(
            painter = painterResource(icon),
            contentDescription = label,
            colorFilter = ColorFilter.tint(if (selected) HomePurple else Color.White),
            modifier = Modifier.size(27.dp),
        )
        if (progress != null) {
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                color = if (selected) HomePurple else Color.White,
                trackColor = if (selected) HomeTrack else Color(0xFFBB82EF),
                modifier = Modifier.width(38.dp).height(4.dp).clip(CircleShape),
            )
        } else {
            Spacer(Modifier.height(4.dp))
        }
    }
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
private fun ProductRow(
    items: List<CareItemUi>,
    money: Int,
    onBuyClick: (CareItem) -> Unit,
    horizontalPadding: Dp,
    itemGap: Dp,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = horizontalPadding, vertical = itemGap),
        horizontalArrangement = Arrangement.spacedBy(itemGap),
    ) {
        items.forEach { product ->
            ProductCard(
                product = product,
                canBuy = money >= product.item.price,
                onBuyClick = { onBuyClick(product.item) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ProductCard(
    product: CareItemUi,
    canBuy: Boolean,
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
        Text(text = product.illustration, fontSize = 39.sp, lineHeight = 47.sp)
        Spacer(Modifier.weight(1f))
        Text(
            text = stringResource(product.title),
            modifier = Modifier.fillMaxWidth(),
            color = HomePurpleDark,
            autoSize = TextAutoSize.StepBased(
                minFontSize = 8.sp,
                maxFontSize = 11.sp,
                stepSize = 0.5.sp,
            ),
            lineHeight = 14.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
        Spacer(Modifier.height(5.dp))
        Button(
            onClick = onBuyClick,
            enabled = canBuy,
            colors = ButtonDefaults.buttonColors(
                containerColor = HomePurple,
                disabledContainerColor = HomePurple.copy(alpha = 0.22f),
                disabledContentColor = HomePurpleDark.copy(alpha = 0.38f),
            ),
            contentPadding = PaddingValues(horizontal = 6.dp),
            modifier = Modifier.fillMaxWidth().heightIn(min = 36.dp, max = 42.dp),
        ) {
            Text(
                text = stringResource(R.string.buy_price, product.item.price),
                autoSize = TextAutoSize.StepBased(
                    minFontSize = 7.sp,
                    maxFontSize = 10.sp,
                    stepSize = 0.5.sp,
                ),
                maxLines = 1,
            )
        }
    }
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
private fun MenuOverlay(onAction: (PetAction) -> Unit) {
    Dialog(onDismissRequest = { onAction(PetAction.CloseOverlay) }) {
        Surface(shape = RoundedCornerShape(24.dp), tonalElevation = 8.dp) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    stringResource(R.string.menu),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Button(
                    onClick = { onAction(PetAction.CloseOverlay) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.continue_action))
                }
                Button(
                    onClick = { onAction(PetAction.Restart) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.restart_action))
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
}

@Composable
private fun ShopOverlay(canBuy: Boolean, onAction: (PetAction) -> Unit) {
    Dialog(onDismissRequest = { onAction(PetAction.CloseOverlay) }) {
        Surface(shape = RoundedCornerShape(24.dp), tonalElevation = 8.dp) {
            Box(Modifier.fillMaxWidth().padding(20.dp)) {
                IconButton(
                    onClick = { onAction(PetAction.CloseOverlay) },
                    modifier = Modifier.align(Alignment.TopEnd),
                ) {
                    Text("×", fontSize = 30.sp)
                }
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        stringResource(R.string.store),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Box(Modifier.size(88.dp).background(Color.Black))
                    Text(stringResource(R.string.collar), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.collar_price))
                    Button(
                        onClick = { onAction(PetAction.BuyCollar) },
                        enabled = canBuy,
                    ) {
                        Text(stringResource(R.string.buy))
                    }
                }
            }
        }
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
