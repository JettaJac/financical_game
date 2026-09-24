package com.example.financial_game.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.financial_game.R
import com.example.financial_game.ui.theme.NunitoFontFamily

private val GoalPurple = Color(0xFF8743D3)
private val GoalPurpleDark = Color(0xFF4B2163)
private val GoalTrack = Color(0xFFE4B2F8)
private val GoalSurface = Color(0xFFFCF7FF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun GoalOverlay(
    money: Int,
    target: Int,
    title: String,
    level: Int,
    onDismiss: () -> Unit,
) {
    val safeTarget = target.coerceAtLeast(1)
    val progress = (money.toFloat() / safeTarget).coerceIn(0f, 1f)
    val remaining = (safeTarget - money).coerceAtLeast(0)
    val surfaceInteraction = remember { MutableInteractionSource() }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.48f))
                .clickable(onClick = onDismiss),
        ) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .fillMaxHeight(0.76f)
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
                        Modifier
                            .width(42.dp)
                            .height(5.dp)
                            .clip(CircleShape)
                            .background(GoalTrack),
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.goal_level, level),
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
                            painter = painterResource(R.drawable.goal_pillow),
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
                                    text = stringResource(R.string.goal_energy_bonus),
                                    color = GoalPurple,
                                    fontFamily = NunitoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                )
                                Image(
                                    painter = painterResource(R.drawable.ic_energy),
                                    contentDescription = stringResource(R.string.energy),
                                    modifier = Modifier.size(18.dp),
                                    colorFilter = ColorFilter.tint(GoalPurple),
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.goal_pillow_description),
                        modifier = Modifier.fillMaxWidth(),
                        color = GoalPurpleDark,
                        fontFamily = NunitoFontFamily,
                        fontSize = 14.sp,
                        lineHeight = 17.sp,
                    )
                    Spacer(Modifier.height(14.dp))
                    Button(
                        onClick = {},
                        enabled = false,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            disabledContainerColor = Color(0xFFD9C8DC),
                            disabledContentColor = Color.White,
                        ),
                        shape = RoundedCornerShape(10.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.change_goal),
                            fontFamily = NunitoFontFamily,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.change_goal_hint),
                        modifier = Modifier.fillMaxWidth(),
                        color = GoalPurple,
                        fontFamily = NunitoFontFamily,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Start,
                    )
                }
            }
        }
    }
}
