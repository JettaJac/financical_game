package com.example.financial_game.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.financial_game.R

private val HomePurple = Color(0xFF8743D3)
private val HomePurpleDark = Color(0xFF4B2163)
private val HomePurpleLight = Color(0xFFE9C7FF)
private val HomeSurface = Color(0xFFFCF7FF)
private val HomeTrack = Color(0xFFE6C1FA)

@Composable
fun PetScreen(state: PetState, onAction: (PetAction) -> Unit) {
    if (!state.isInitialized) {
        Box(Modifier.fillMaxSize().background(HomeSurface))
        return
    }

    val pagerState = rememberPagerState(initialPage = HOME_PAGE, pageCount = { PAGE_COUNT })

    Box(Modifier.fillMaxSize().background(HomeSurface)) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            when (page) {
                HOME_PAGE -> HomePage(state, onAction)
                else -> EmptyPage()
            }
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
}

@Composable
private fun HomePage(state: PetState, onAction: (PetAction) -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(HomeSurface)
            .safeDrawingPadding(),
    ) {
        HomeBackground()

        HomeHeader(
            state = state,
            onMenuClick = { onAction(PetAction.OpenMenu) },
            modifier = Modifier.align(Alignment.TopCenter),
        )

        QuickActions(
            currentPeriod = state.resources.currentPeriod,
            onShopClick = { onAction(PetAction.OpenShop) },
            onSkipCycle = { onAction(PetAction.ForceNextCycle )},
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 138.dp),
        )

        Image(
            painter = painterResource(R.drawable.pet_main),
            contentDescription = stringResource(R.string.pet),
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = 4.dp)
                .size(width = 292.dp, height = 360.dp),
        )

        HomeBottomPanel(
            state = state,
            onSectionClick = { onAction(PetAction.SelectSection(it)) },
            onBuyClick = { onAction(PetAction.BuyCollar) },
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun HomeBackground() {
    Box(Modifier.fillMaxWidth().height(659.dp)) {
        Image(
            painter = painterResource(R.drawable.home_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().padding(top = 116.dp),
        )
        Box(
            Modifier
                .align(Alignment.TopCenter)
                .offset(y = 116.dp)
                .fillMaxWidth()
                .height(88.dp)
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
                .height(116.dp)
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
    modifier: Modifier = Modifier,
) {
    val goalTarget = state.resources.goalTarget.coerceAtLeast(1)
    val progress = (state.resources.money.toFloat() / goalTarget).coerceIn(0f, 1f)

    Box(modifier.fillMaxWidth().height(126.dp).padding(horizontal = 17.dp)) {
        Column(
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 27.dp),
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
            modifier = Modifier.align(Alignment.TopEnd).padding(top = 8.dp),
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
            )
        }
    }
}

@Composable
private fun HeaderValue(@DrawableRes icon: Int, value: String) {
    Column(
        modifier = Modifier.width(29.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
        )
        Text(
            text = value,
            color = HomePurpleDark,
            fontSize = 14.sp,
            lineHeight = 16.sp,
        )
    }
}

@Composable
private fun QuickActions(
    currentPeriod: Int,
    onShopClick: () -> Unit,
    onSkipCycle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 17.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        ActionTile(onClick = onSkipCycle) {
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

        ActionTile(onClick = onShopClick) {
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
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit,
) {
    val interactionModifier = if (onClick == null) Modifier else Modifier.clickable(onClick = onClick)
    Box(
        modifier = Modifier
            .size(53.dp)
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
    onBuyClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth()) {
        HomeTabs(state, onSectionClick)
        Column(Modifier.fillMaxWidth().background(HomeSurface)) {
            when (state.selectedSection) {
                HomeSection.Food -> ProductRow(onBuyClick)
                else -> SectionPlaceholder(state.selectedSection)
            }
            PageDots(isVisible = state.selectedSection == HomeSection.Food)
        }
    }
}

@Composable
private fun HomeTabs(state: PetState, onSectionClick: (HomeSection) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(74.dp).padding(horizontal = 17.dp),
        horizontalArrangement = Arrangement.spacedBy(9.dp),
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
private fun SectionPlaceholder(section: HomeSection) {
    val sectionName = when (section) {
        HomeSection.Food -> stringResource(R.string.food)
        HomeSection.Happiness -> stringResource(R.string.happiness)
        HomeSection.Energy -> stringResource(R.string.energy)
        HomeSection.Shop -> stringResource(R.string.shop)
        HomeSection.Tasks -> stringResource(R.string.tasks)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(158.dp)
            .padding(horizontal = 17.dp, vertical = 9.dp)
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
private fun ProductRow(onBuyClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(158.dp).padding(horizontal = 17.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ProductCard(
            illustration = "🍱",
            title = stringResource(R.string.school_lunch),
            modifier = Modifier.weight(1f),
        ) {
            Button(
                onClick = onBuyClick,
                colors = ButtonDefaults.buttonColors(containerColor = HomePurple),
                contentPadding = ButtonDefaults.ContentPadding,
                modifier = Modifier.fillMaxWidth().height(29.dp),
            ) {
                Text(stringResource(R.string.buy), fontSize = 12.sp)
            }
        }
        ProductCard(
            illustration = "🥤",
            title = stringResource(R.string.soda),
            modifier = Modifier.weight(1f),
        ) {
            QuantityControl(value = 1)
        }
        ProductCard(
            illustration = "🍦",
            title = stringResource(R.string.ice_cream),
            modifier = Modifier.weight(1f),
        ) {
            QuantityControl(value = 4)
        }
    }
}

@Composable
private fun ProductCard(
    illustration: String,
    title: String,
    modifier: Modifier = Modifier,
    action: @Composable () -> Unit,
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
        Text(text = illustration, fontSize = 39.sp, lineHeight = 47.sp)
        Spacer(Modifier.weight(1f))
        Text(
            text = title,
            color = HomePurpleDark,
            fontSize = 12.sp,
            lineHeight = 14.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
        Spacer(Modifier.height(5.dp))
        action()
    }
}

@Composable
private fun QuantityControl(value: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(29.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(HomePurpleLight),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("−", color = HomePurpleDark, fontSize = 17.sp)
        Text(value.toString(), color = HomePurpleDark, fontSize = 12.sp)
        Text("+", color = HomePurpleDark, fontSize = 17.sp)
    }
}

@Composable
private fun PageDots(isVisible: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().height(28.dp),
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
