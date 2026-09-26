package com.example.financial_game.ui

import android.graphics.ImageDecoder
import android.graphics.drawable.Animatable
import android.os.Build
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.financial_game.R
import com.example.financial_game.data.GameSnapshot
import com.example.financial_game.domain.EyeColour
import com.example.financial_game.domain.Goals
import com.example.financial_game.domain.HairColour
import com.example.financial_game.domain.HairStyle
import com.example.financial_game.domain.PetSetup
import com.example.financial_game.domain.Resource
import com.example.financial_game.ui.theme.NunitoFontFamily

private const val INTRO_STEP = 0
private const val NAME_STEP = 1
private const val STORY_STEP = 2
private const val GOAL_STEP = 3
private const val HAIR_COLOUR_STEP = 4
private const val EYE_COLOUR_STEP = 5
private const val HAIR_STYLE_STEP = 6
private const val MAX_NAME_LENGTH = 20
private const val HELLO_ANIMATION = "webm/hello_animated.webp"
private const val TALKING_ANIMATION = "webm/talking_animated.webp"

private val OnboardingPurple = Color(0xFF8743D3)
private val OnboardingPurpleDark = Color(0xFF4B2163)
private val OnboardingLavender = Color(0xFFF5E7FF)

@Composable
internal fun OnboardingScreen(
    initialState: GameSnapshot,
    onComplete: (PetSetup) -> Unit,
    modifier: Modifier = Modifier,
) {
    var step by rememberSaveable { mutableIntStateOf(INTRO_STEP) }
    var name by rememberSaveable { mutableStateOf(initialState.name) }
    var hairColour by rememberSaveable { mutableStateOf(initialState.hairColour) }
    var eyeColour by rememberSaveable { mutableStateOf(initialState.eyeColour) }
    var hairStyle by rememberSaveable { mutableStateOf(initialState.hairStyle) }
    val availableGoals = remember(initialState.level) {
        Goals.entries.filter { it.level <= initialState.level }.ifEmpty { listOf(Goals.Pillow) }
    }
    var goal by rememberSaveable { mutableStateOf(availableGoals.first()) }

    when (step) {
        INTRO_STEP -> IntroOnboardingStep(
            onGreetingClick = { step = NAME_STEP },
            modifier = modifier,
        )
        NAME_STEP -> NameOnboardingStep(
            name = name,
            onNameChange = { name = it.take(MAX_NAME_LENGTH) },
            onContinue = { step = STORY_STEP },
            modifier = modifier,
        )
        STORY_STEP -> HelpOnboardingStep(
            onContinue = { step = GOAL_STEP },
            modifier = modifier,
        )
        GOAL_STEP -> GoalOnboardingStep(
            goals = availableGoals,
            onGoalSelected = {
                goal = it
                step = HAIR_COLOUR_STEP
            },
            modifier = modifier,
        )
        HAIR_COLOUR_STEP -> HairColourOnboardingStep(
            selected = hairColour,
            onSelected = { hairColour = it },
            onContinue = { step = EYE_COLOUR_STEP },
            modifier = modifier,
        )
        EYE_COLOUR_STEP -> EyeColourOnboardingStep(
            selected = eyeColour,
            onSelected = { eyeColour = it },
            onContinue = { step = HAIR_STYLE_STEP },
            modifier = modifier,
        )
        HAIR_STYLE_STEP -> HairStyleOnboardingStep(
            selected = hairStyle,
            onSelected = { hairStyle = it },
            onContinue = {
                onComplete(
                    PetSetup(
                        name = name.trim(),
                        hairColour = hairColour,
                        eyeColour = eyeColour,
                        hairStyle = hairStyle,
                        goal = goal,
                    ),
                )
            },
            modifier = modifier,
        )
    }
}

@Composable
private fun IntroOnboardingStep(
    onGreetingClick: () -> Unit,
    modifier: Modifier,
) {
    OnboardingStage(
        animationAssetPath = HELLO_ANIMATION,
        modifier = modifier.selectable(
            selected = false,
            enabled = true,
            role = Role.Button,
            onClick = onGreetingClick,
        ),
    ) {
        PetSpeechBubble(
            text = stringResource(
                R.string.onboarding_greeting,
            ),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 64.dp, end = 16.dp)
                .widthIn(max = 250.dp),
        )
    }
}

@Composable
private fun HelpOnboardingStep(
    onContinue: () -> Unit,
    modifier: Modifier,
) {
    OnboardingStage(
        animationAssetPath = TALKING_ANIMATION,
        modifier = modifier,
    ) {
        PetSpeechBubble(
            text = stringResource(
                R.string.onboarding_explanation
            ),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 64.dp, end = 16.dp)
                .widthIn(max = 250.dp),
        )

        OnboardingButton(
            text = stringResource(R.string.onboarding_start),
            onClick = onContinue,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun NameOnboardingStep(
    name: String,
    onNameChange: (String) -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier,
) {
    OnboardingStage(
        animationAssetPath = TALKING_ANIMATION,
        modifier = modifier,
    ) {
        PetSpeechBubble(
            text = stringResource(R.string.onboarding_name_question),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 64.dp, end = 16.dp)
                .widthIn(max = 245.dp),
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = TextStyle(
                    color = OnboardingPurpleDark,
                    fontFamily = NunitoFontFamily,
                    fontSize = 16.sp,
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OnboardingPurple,
                    unfocusedBorderColor = OnboardingPurpleDark,
                    cursorColor = OnboardingPurple,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                ),
            )
            PrimaryButton(
                text = stringResource(R.string.onboarding_next),
                enabled = name.isNotBlank(),
                onClick = onContinue,
            )
        }
    }
}

@Composable
private fun HairColourOnboardingStep(
    selected: HairColour,
    onSelected: (HairColour) -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier,
) {
    ChoiceOnboardingStep(
        title = stringResource(R.string.onboarding_choose_hair_colour),
        modifier = modifier,
        onContinue = onContinue,
    ) {
        HairColour.entries.forEach { colour ->
            ChoiceTile(
                selected = colour == selected,
                contentDescription = stringResource(colour.labelRes()),
                onClick = { onSelected(colour) },
                modifier = Modifier.weight(1f).aspectRatio(1f),
            ) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(9.dp))
                        .background(colour.displayColor()),
                )
            }
        }
    }
}

@Composable
private fun EyeColourOnboardingStep(
    selected: EyeColour,
    onSelected: (EyeColour) -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier,
) {
    ChoiceOnboardingStep(
        title = stringResource(R.string.onboarding_choose_eye_colour),
        modifier = modifier,
        onContinue = onContinue,
    ) {
        EyeColour.entries.forEach { colour ->
            ChoiceTile(
                selected = colour == selected,
                contentDescription = stringResource(colour.labelRes()),
                onClick = { onSelected(colour) },
                modifier = Modifier.weight(1f).aspectRatio(1f),
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().background(OnboardingLavender),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color.White, colour.displayColor(), OnboardingPurpleDark),
                                    start = Offset.Zero,
                                    end = Offset.Infinite,
                                ),
                            ),
                    )
                }
            }
        }
    }
}

@Composable
private fun HairStyleOnboardingStep(
    selected: HairStyle,
    onSelected: (HairStyle) -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier,
) {
    ChoiceOnboardingStep(
        title = stringResource(R.string.onboarding_choose_hair_style),
        modifier = modifier,
        onContinue = onContinue,
    ) {
        HairStyle.entries.forEach { style ->
            ChoiceTile(
                selected = style == selected,
                contentDescription = stringResource(style.labelRes()),
                onClick = { onSelected(style) },
                modifier = Modifier.weight(1f).aspectRatio(1f),
            ) {
                Image(
                    painter = painterResource(R.drawable.pet_main),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().padding(style.previewPadding()),
                    contentScale = ContentScale.Fit,
                )
            }
        }
    }
}

@Composable
private fun ChoiceOnboardingStep(
    title: String,
    onContinue: () -> Unit,
    modifier: Modifier,
    choices: @Composable RowScope.() -> Unit,
) {
    OnboardingStage(
        animationAssetPath = TALKING_ANIMATION,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp),
        ) {
            Text(
                text = title,
                color = OnboardingPurpleDark,
                fontFamily = NunitoFontFamily,
                fontSize = 22.sp,
                lineHeight = 26.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                content = choices,
            )
            Spacer(Modifier.height(18.dp))
            PrimaryButton(
                text = stringResource(R.string.onboarding_select),
                onClick = onContinue,
            )
        }
    }
}

@Composable
private fun GoalOnboardingStep(
    goals: List<Goals>,
    onGoalSelected: (Goals) -> Unit,
    modifier: Modifier,
) {
    val pagerState = rememberPagerState(pageCount = { goals.size })

    OnboardingStage(
        animationAssetPath = TALKING_ANIMATION,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp),
        ) {
            Text(
                text = stringResource(R.string.onboarding_choose_goal),
                color = OnboardingPurpleDark,
                fontFamily = NunitoFontFamily,
                fontSize = 23.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            Spacer(Modifier.height(8.dp))
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth().height(140.dp),
                pageSpacing = 12.dp,
            ) { page ->
                GoalChoiceCard(goals[page])
            }
            Spacer(Modifier.height(8.dp))
            GoalPageIndicator(
                pageCount = goals.size,
                selectedPage = pagerState.currentPage,
            )
            Spacer(Modifier.height(18.dp))
            PrimaryButton(
                text = stringResource(R.string.onboarding_select),
                onClick = { onGoalSelected(goals[pagerState.currentPage]) },
            )
        }
    }
}

@Composable
private fun GoalChoiceCard(goal: Goals) {
    val effect = goal.goalEffects.firstOrNull()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(11.dp))
            .background(Color.White)
            .border(2.dp, OnboardingPurpleDark, RoundedCornerShape(11.dp)),
    ) {
        Image(
            painter = painterResource(goal.illustrationRes),
            contentDescription = null,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp).size(84.dp),
            contentScale = ContentScale.Fit,
        )
        GoalValueBadge(
            text = goal.target.toString(),
            iconRes = R.drawable.ic_coin,
            contentDescription = stringResource(R.string.coins),
            modifier = Modifier.align(Alignment.TopStart).padding(8.dp),
        )
        if (effect != null) {
            GoalValueBadge(
                text = "+${effect.increase}",
                iconRes = effect.resource.iconRes(),
                contentDescription = stringResource(effect.resource.labelRes()),
                modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
            )
        }
        Text(
            text = stringResource(goal.titleRes),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            color = OnboardingPurpleDark,
            fontFamily = NunitoFontFamily,
            fontSize = 18.sp,
            lineHeight = 21.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
        )
    }
}

@Composable
private fun GoalValueBadge(
    text: String,
    @DrawableRes iconRes: Int,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = Color(0xFFECC8FF),
        shape = RoundedCornerShape(6.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = text,
                color = OnboardingPurple,
                fontFamily = NunitoFontFamily,
                fontWeight = FontWeight.Bold,
            )
            Image(
                painter = painterResource(iconRes),
                contentDescription = contentDescription,
                modifier = Modifier.size(16.dp),
                colorFilter = ColorFilter.tint(OnboardingPurple),
            )
        }
    }
}

@Composable
private fun GoalPageIndicator(pageCount: Int, selectedPage: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        repeat(pageCount) { page ->
            Box(
                Modifier
                    .padding(horizontal = 2.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        if (page == selectedPage) OnboardingPurple
                        else Color(0xFFD9A4F3),
                    ),
            )
        }
    }
}

@Composable
private fun ChoiceTile(
    selected: Boolean,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(OnboardingLavender)
            .border(
                width = if (selected) 3.dp else 1.dp,
                color = if (selected) OnboardingPurple else Color.Transparent,
                shape = RoundedCornerShape(10.dp),
            )
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onClick,
            )
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
        content = content,
    )
}

@Composable
private fun OnboardingStage(
    animationAssetPath: String,
    modifier: Modifier = Modifier,
    content: @Composable BoxWithConstraintsScope.() -> Unit,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .safeDrawingPadding(),
    ) {
        val backgroundLift = (maxHeight * 0.07f).coerceIn(52.dp, 72.dp)
        Image(
            painter = painterResource(R.drawable.home_background),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .offset(y = -backgroundLift),
            contentScale = ContentScale.Crop,
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0f to Color.Transparent,
                            0.48f to Color.Transparent,
                            0.62f to Color.White.copy(alpha = 0.12f),
                            0.74f to Color.White.copy(alpha = 0.45f),
                            0.86f to Color.White.copy(alpha = 0.88f),
                            0.92f to Color.White,
                            1f to Color.White,
                        ),
                    ),
                ),
        )
        val finnWidth = minOf(
            maxWidth * 0.78f,
            maxHeight * 0.43f * (6f / 7f),
            350.dp,
        )
        val finnTopPadding = (maxHeight * 0.24f).coerceIn(140.dp, 190.dp)
        FinnickAnimation(
            assetPath = animationAssetPath,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = finnTopPadding)
                .width(finnWidth)
                .height(finnWidth * (7f / 6f)),
        )
        content()
    }
}

@Composable
private fun OnboardingButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PrimaryButton(
        text = text,
        onClick = onClick,
        modifier = modifier
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 18.dp),
    )
}

@Composable
private fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().height(52.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = OnboardingPurple,
            disabledContainerColor = OnboardingPurple.copy(alpha = 0.28f),
        ),
        shape = RoundedCornerShape(10.dp),
    ) {
        Text(
            text = text,
            fontFamily = NunitoFontFamily,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )
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
private fun FinnickAnimation(
    assetPath: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val description = stringResource(R.string.pet)
    val drawable = remember(context, assetPath) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeDrawable(
                ImageDecoder.createSource(context.assets, assetPath),
            )
        } else {
            context.getDrawable(R.drawable.pet_main)
        }
    }

    DisposableEffect(drawable) {
        (drawable as? Animatable)?.start()
        onDispose { (drawable as? Animatable)?.stop() }
    }

    AndroidView(
        factory = { viewContext ->
            ImageView(viewContext).apply {
                scaleType = ImageView.ScaleType.FIT_CENTER
                setImageDrawable(drawable)
                contentDescription = description
            }
        },
        update = { imageView -> imageView.setImageDrawable(drawable) },
        modifier = modifier.semantics { contentDescription = description },
    )
}

private fun HairColour.displayColor(): Color = when (this) {
    HairColour.Beige -> Color(0xFFFFDFAE)
    HairColour.Violet -> Color(0xFFC8C1F7)
    HairColour.Orange -> Color(0xFFF4C4AA)
}

private fun EyeColour.displayColor(): Color = when (this) {
    EyeColour.Violet -> Color(0xFF6554C4)
    EyeColour.Green -> Color(0xFF00A79D)
    EyeColour.Blue -> Color(0xFF398CCB)
}

private fun HairStyle.previewPadding() = when (this) {
    HairStyle.Default -> 8.dp
    HairStyle.Hairy -> 3.dp
    HairStyle.Curly -> 12.dp
}

private fun HairColour.labelRes(): Int = when (this) {
    HairColour.Beige -> R.string.hair_colour_beige
    HairColour.Violet -> R.string.hair_colour_violet
    HairColour.Orange -> R.string.hair_colour_orange
}

private fun EyeColour.labelRes(): Int = when (this) {
    EyeColour.Violet -> R.string.eye_colour_violet
    EyeColour.Green -> R.string.eye_colour_green
    EyeColour.Blue -> R.string.eye_colour_blue
}

private fun HairStyle.labelRes(): Int = when (this) {
    HairStyle.Default -> R.string.hair_style_default
    HairStyle.Hairy -> R.string.hair_style_hairy
    HairStyle.Curly -> R.string.hair_style_curly
}

@DrawableRes
private fun Resource.iconRes(): Int = when (this) {
    Resource.Health -> R.drawable.ic_food
    Resource.Happiness -> R.drawable.ic_happy
    Resource.Energy -> R.drawable.ic_energy
    Resource.Money -> R.drawable.ic_coin
}

private fun Resource.labelRes(): Int = when (this) {
    Resource.Health -> R.string.health
    Resource.Happiness -> R.string.happiness
    Resource.Energy -> R.string.energy
    Resource.Money -> R.string.coins
}
