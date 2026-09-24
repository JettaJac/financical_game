package com.example.financial_game.ui

import android.graphics.ImageDecoder
import android.graphics.drawable.Animatable
import android.os.Build
import android.widget.ImageView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.financial_game.R
import com.example.financial_game.ui.theme.NunitoFontFamily
import kotlinx.coroutines.delay

private const val INTRO_STEP = 0
private const val START_STEP = 1
private val OnboardingPurple = Color(0xFF8743D3)
private val OnboardingPurpleDark = Color(0xFF4B2163)

@Composable
internal fun OnboardingScreen(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var step by rememberSaveable { mutableIntStateOf(INTRO_STEP) }

//    LaunchedEffect(Unit) {
//        delay(1_800)
//        step = START_STEP
//    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .clickable(
                enabled = step == INTRO_STEP,
                role = Role.Button,
                onClick = { step = START_STEP },
            ),
    ) {
        val finnWidth = (maxWidth * 0.76f).coerceAtMost(340.dp)
        val finnHeight = finnWidth * (7f / 6f)
        val speechAreaHeight = 150.dp
        val bubbleStart = ((maxWidth - finnWidth) / 2 + finnWidth * 0.3f)
            .coerceAtLeast(16.dp)
        val bubbleMaxWidth = (maxWidth - bubbleStart - 16.dp)
            .coerceAtMost(250.dp)

        Image(
            painter = painterResource(R.drawable.home_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        0.68f to Color.Transparent,
                        1f to Color.White,
                    ),
                ),
        )

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = maxHeight * 0.08f - speechAreaHeight / 2)
                .fillMaxWidth()
                .height(finnHeight + speechAreaHeight),
        ) {
            FinnickAnimation(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .width(finnWidth)
                    .height(finnHeight),
            )

            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(speechAreaHeight),
            ) {
                PetSpeechBubble(
                    text = if (step == INTRO_STEP) {
                        stringResource(R.string.onboarding_greeting)
                    } else {
                        stringResource(R.string.onboarding_explanation)
                    },
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = bubbleStart)
                        .widthIn(max = bubbleMaxWidth),
                )
            }
        }

        if (step == START_STEP) {
            Button(
                onClick = onComplete,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 18.dp)
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OnboardingPurple),
                shape = RoundedCornerShape(10.dp),
            ) {
                Text(
                    text = stringResource(R.string.onboarding_start),
                    fontFamily = NunitoFontFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

/** Reusable speech bubble for Finnick's dialogue. */
@Composable
internal fun PetSpeechBubble(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.padding(bottom = 12.dp)) {
        Canvas(
            Modifier
                .align(Alignment.BottomStart)
                .offset(x = 28.dp, y = 10.dp)
                .size(width = 34.dp, height = 22.dp),
        ) {
            val tail = Path().apply {
                moveTo(0f, 0f)
                lineTo(size.width * 0.52f, size.height)
                lineTo(size.width, 0f)
                close()
            }
            drawPath(tail, Color.White)
        }

        Surface(
            color = Color.White,
            shape = RoundedCornerShape(10.dp),
            shadowElevation = 2.dp,
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                color = OnboardingPurpleDark,
                fontFamily = NunitoFontFamily,
                fontSize = 15.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun FinnickAnimation(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val description = stringResource(R.string.pet)
    val drawable = remember(context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeDrawable(
                ImageDecoder.createSource(
                    context.assets,
                    "webm/phinnik_animated.webp",
                ),
            )
        } else {
            context.getDrawable(R.drawable.pet_main)
        }
    }

    DisposableEffect(drawable) {
        (drawable as? Animatable)?.start()
        onDispose {
            (drawable as? Animatable)?.stop()
        }
    }

    AndroidView(
        factory = { viewContext ->
            ImageView(viewContext).apply {
                scaleType = ImageView.ScaleType.FIT_CENTER
                setImageDrawable(drawable)
                contentDescription = description
            }
        },
        modifier = modifier.semantics { contentDescription = description },
    )
}
