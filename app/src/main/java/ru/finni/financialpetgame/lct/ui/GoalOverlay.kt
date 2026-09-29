package ru.finni.financialpetgame.lct.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.animation.core.animate
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ru.finni.financialpetgame.lct.R
import ru.finni.financialpetgame.lct.domain.Goals
import ru.finni.financialpetgame.lct.domain.Resource
import ru.finni.financialpetgame.lct.ui.theme.NunitoFontFamily
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private val GoalPurple = Color(0xFF8743D3)
private val GoalPurpleDark = Color(0xFF4B2163)
private val GoalTrack = Color(0xFFE4B2F8)
private val GoalSurface = Color(0xFFFCF7FF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun GoalOverlay(
    money: Int,
    target: Int,
    goal: Goals,
    alreadyPurchased: Boolean,
    onBuy: () -> Unit,
    onDismiss: () -> Unit,
) {
    val title = stringResource(goal.titleRes)
    val primaryEffect = goal.goalEffects.firstOrNull()
    val safeTarget = target.coerceAtLeast(1)
    val progress = (money.toFloat() / safeTarget).coerceIn(0f, 1f)
    val remaining = (safeTarget - money).coerceAtLeast(0)
    val surfaceInteraction = remember { MutableInteractionSource() }
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
            dismissOnClickOutside = false,
        ),
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.48f)),
        ) {
            val sheetHeight = maxHeight * 0.76f
            val restingOffsetPx = with(density) { (maxHeight - sheetHeight).toPx() }
            val maximumOffsetPx = with(density) { maxHeight.toPx() }
            val sheetHeightPx = maximumOffsetPx - restingOffsetPx
            val dismissThresholdPx = restingOffsetPx + sheetHeightPx * 0.18f
            val velocityThreshold = with(density) { 300.dp.toPx() }
            var sheetOffsetPx by remember(restingOffsetPx) {
                mutableFloatStateOf(restingOffsetPx)
            }
            val dragState = rememberDraggableState { delta ->
                sheetOffsetPx = (sheetOffsetPx + delta).coerceIn(0f, maximumOffsetPx)
            }
            val settleSheet: (Float) -> Unit = { velocity ->
                val shouldDismiss =
                    sheetOffsetPx >= dismissThresholdPx ||
                        (velocity > velocityThreshold && sheetOffsetPx > restingOffsetPx)
                val target = when {
                    shouldDismiss -> maximumOffsetPx
                    velocity < -velocityThreshold -> 0f
                    velocity > velocityThreshold -> restingOffsetPx
                    sheetOffsetPx < restingOffsetPx / 2f -> 0f
                    else -> restingOffsetPx
                }
                scope.launch {
                    animate(
                        initialValue = sheetOffsetPx,
                        targetValue = target,
                        initialVelocity = velocity,
                    ) { value, _ ->
                        sheetOffsetPx = value
                    }
                    if (shouldDismiss) onDismiss()
                }
            }
            val bottomFillHeight = with(density) {
                (restingOffsetPx - sheetOffsetPx).coerceAtLeast(0f).toDp()
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(maxHeight - sheetHeight)
                    .clickable(onClick = onDismiss),
            )

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(bottomFillHeight)
                    .background(GoalSurface)
                    .clickable(
                        interactionSource = surfaceInteraction,
                        indication = null,
                        onClick = {},
                    ),
            )

            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset { IntOffset(0, sheetOffsetPx.roundToInt()) }
                    .fillMaxWidth()
                    .height(sheetHeight)
                    .clickable(
                        interactionSource = surfaceInteraction,
                        indication = null,
                        onClick = {},
                    ),
                color = GoalSurface,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                            .draggable(
                                state = dragState,
                                orientation = Orientation.Vertical,
                                onDragStopped = { velocity -> settleSheet(velocity) },
                            ),
                        contentAlignment = Alignment.TopCenter,
                    ) {
                        Box(
                            Modifier
                                .width(42.dp)
                                .height(5.dp)
                                .clip(CircleShape)
                                .background(GoalTrack),
                        )
                    }
                    Text(
                        text = stringResource(R.string.goal_level, goal.level),
                        color = GoalPurple,
                        fontFamily = NunitoFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .heightIn(min = 120.dp, max = 200.dp)
                            .clip(RoundedCornerShape(8.dp)),
                    ) {
                        Image(
                            painter = painterResource(goal.illustrationRes),
                            contentDescription = title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.FillWidth,
                            alignment = Alignment.TopCenter,
                        )
                    }

                    Text(
                        text = title,
                        color = GoalPurpleDark,
                        fontFamily = NunitoFontFamily,
                        fontSize = 24.sp,
                        lineHeight = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(7.dp)
                            .clip(CircleShape),
                        color = GoalPurple,
                        trackColor = GoalTrack,
                        gapSize = 0.dp,
                        drawStopIndicator = {},
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = if (remaining > 0) {
                            stringResource(R.string.goal_remaining, remaining)
                        } else {
                            stringResource(R.string.goal_reached)
                        },
                        color = GoalPurple,
                        fontFamily = NunitoFontFamily,
                        fontSize = 14.sp,
                    )
                    Spacer(Modifier.height(10.dp))

                    if (primaryEffect != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Surface(
                                color = GoalTrack.copy(alpha = 0.72f),
                                shape = RoundedCornerShape(6.dp),
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Text(
                                        text = "+${primaryEffect.increase}",
                                        color = GoalPurple,
                                        fontFamily = NunitoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Image(
                                        painter = painterResource(primaryEffect.resource.goalIconResource()),
                                        contentDescription = stringResource(
                                            primaryEffect.resource.labelResource(),
                                        ),
                                        modifier = Modifier.size(18.dp),
                                        colorFilter = ColorFilter.tint(GoalPurple),
                                    )
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(goal.descriptionRes),
                        modifier = Modifier.fillMaxWidth(),
                        color = GoalPurpleDark,
                        fontFamily = NunitoFontFamily,
                        fontSize = 14.sp,
                        lineHeight = 17.sp,
                    )
                    Spacer(Modifier.height(14.dp))
                    Button(
                        onClick = onBuy,
                        enabled = money >= target && !alreadyPurchased,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoalPurple,
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFFD9C8DC),
                            disabledContentColor = Color.White,
                        ),
                        shape = RoundedCornerShape(10.dp),
                    ) {
                        Text(
                            text = stringResource(
                                if (alreadyPurchased) R.string.goal_purchased else R.string.buy,
                            ),
                            fontFamily = NunitoFontFamily,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

@DrawableRes
private fun Resource.goalIconResource(): Int = when (this) {
    Resource.Health -> R.drawable.ic_food
    Resource.Happiness -> R.drawable.ic_happy
    Resource.Energy -> R.drawable.ic_energy
    Resource.Money -> R.drawable.ic_coin
}

private fun Resource.labelResource(): Int = when (this) {
    Resource.Health -> R.string.health
    Resource.Happiness -> R.string.happiness
    Resource.Energy -> R.string.energy
    Resource.Money -> R.string.coins
}
