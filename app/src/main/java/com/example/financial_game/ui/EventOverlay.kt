package com.example.financial_game.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.financial_game.R
import com.example.financial_game.domain.Effect
import com.example.financial_game.domain.GameEvent
import com.example.financial_game.domain.Resource
import com.example.financial_game.ui.theme.NunitoFontFamily

private val EventPurple = Color(0xFF8743D3)
private val EventPurpleDark = Color(0xFF4B2163)
private val EventSurface = Color(0xFFFCF7FF)
private val EventSecondaryButton = Color(0xFFE3B2FA)

@Composable
internal fun EventOverlay(
    event: GameEvent,
    acceptButtonText: String? = null,
    declineButtonText: String? = null,
    canClose: Boolean = false,
    canAccept: Boolean = true,
    onAccept: (() -> Unit)? = null,
    onDecline: (() -> Unit)? = null,
    onClose: (() -> Unit)? = null,
) {
    val closeDescription = stringResource(R.string.event_close)

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 22.dp),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .widthIn(max = 410.dp)
                    .heightIn(max = 590.dp),
                color = EventSurface,
                shape = RoundedCornerShape(24.dp),
                shadowElevation = 10.dp,
            ) {
                Box {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 16.dp),
                    ) {
                        Image(
                            painter = painterResource(event.illustrationRes),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(166.dp)
                                .offset(y = (-16).dp),
                            alignment = Alignment.TopCenter,
                            contentScale = ContentScale.Crop,
                        )

                        Text(
                            text = event.descriptionText ?: stringResource(event.descriptionRes),
                            modifier = Modifier.fillMaxWidth(),
                            color = EventPurpleDark,
                            fontFamily = NunitoFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            lineHeight = 22.sp,
                        )

                        Spacer(Modifier.height(7.dp))
                        EventEffects(
                            if (event.hideRewardUntilAccept) {
                                event.acceptEffects.filterNot { it.resource == Resource.Money }
                            } else {
                                event.acceptEffects
                            },
                        )

                        if (acceptButtonText != null || declineButtonText != null) {
                            Spacer(Modifier.height(14.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                acceptButtonText?.let { text ->
                                    EventButton(
                                        text = text,
                                        onClick = { onAccept?.invoke() },
                                        enabled = canAccept,
                                        containerColor = EventPurple,
                                        contentColor = Color.White,
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                                declineButtonText?.let { text ->
                                    EventButton(
                                        text = text,
                                        onClick = { onDecline?.invoke() },
                                        containerColor = EventSecondaryButton,
                                        contentColor = EventPurpleDark,
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                            }
                        }
                    }

                    if (canClose) {
                        IconButton(
                            onClick = { onClose?.invoke() },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(40.dp)
                                .semantics {
                                    contentDescription = closeDescription
                                },
                        ) {
                            Text(
                                text = "×",
                                color = EventPurpleDark,
                                fontSize = 30.sp,
                                lineHeight = 30.sp,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EventEffects(effects: List<Effect>) {
    val visibleEffects = effects.filter { it.increase != 0 }
    val statusEffects = visibleEffects.filterNot { it.resource == Resource.Money }
    val moneyEffects = visibleEffects.filter { it.resource == Resource.Money }

    Column(verticalArrangement = Arrangement.spacedBy(15.dp)) {
        if (statusEffects.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(13.dp)) {
                statusEffects.forEach { effect -> EventEffect(effect) }
            }
        }
        if (moneyEffects.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(13.dp)) {
                moneyEffects.forEach { effect -> EventEffect(effect) }
            }
        }
    }
}

@Composable
private fun EventEffect(effect: Effect) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (effect.resource == Resource.Money) {
            EventEffectValue(effect)
            Spacer(Modifier.width(3.dp))
            EventEffectIcon(effect.resource)
        } else {
            EventEffectIcon(effect.resource)
            Spacer(Modifier.width(3.dp))
            EventEffectValue(effect)
        }
    }
}

@Composable
private fun EventEffectIcon(resource: Resource) {
    Image(
        painter = painterResource(resource.eventIcon()),
        contentDescription = null,
        modifier = Modifier.size(20.dp),
        colorFilter = if (resource == Resource.Money) {
            null
        } else {
            ColorFilter.tint(EventPurple)
        },
    )
}

@Composable
private fun EventEffectValue(effect: Effect) {
    Text(
        text = effect.increase.toString(),
        color = EventPurple,
        fontFamily = NunitoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        lineHeight = 18.sp,
    )
}

@Composable
private fun EventButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(46.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
        shape = RoundedCornerShape(9.dp),
    ) {
        Text(
            text = text,
            maxLines = 1,
            fontFamily = NunitoFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
        )
    }
}

@DrawableRes
private fun Resource.eventIcon(): Int = when (this) {
    Resource.Health -> R.drawable.ic_food
    Resource.Happiness -> R.drawable.ic_happy
    Resource.Energy -> R.drawable.ic_energy
    Resource.Money -> R.drawable.coin_money
}
