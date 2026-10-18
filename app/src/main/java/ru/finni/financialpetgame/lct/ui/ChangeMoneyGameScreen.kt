package ru.finni.financialpetgame.lct.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.delay
import ru.finni.financialpetgame.lct.R
import ru.finni.financialpetgame.lct.domain.ChangeMoneyChallenge
import ru.finni.financialpetgame.lct.domain.ChangeMoneyDenominations
import ru.finni.financialpetgame.lct.domain.ChangeMoneyValidation
import ru.finni.financialpetgame.lct.ui.theme.NunitoFontFamily

private val ChangeGameDark = Color(0xFF4B2163)
private val ChangeGamePurple = Color(0xFF8138D0)
private val ChangeGamePale = Color(0xFFF0D9FB)
private val ChangeGameBackground = Brush.verticalGradient(
    listOf(Color(0xFFF4E2FD), Color(0xFFFCF8FF), Color(0xFFF7EAFF)),
)

@Composable
internal fun ChangeMoneyGameScreen(
    game: ChangeMoneyGameState,
    bestTimeMillis: Int?,
    onAction: (PetAction) -> Unit,
) {
    var elapsedMillis by remember(game.challenge.id) { mutableLongStateOf(0L) }
    LaunchedEffect(game.stage, game.pauseDialogVisible) {
        if (game.stage != ChangeMoneyStage.Playing || game.pauseDialogVisible) return@LaunchedEffect
        var previousTick = android.os.SystemClock.elapsedRealtime()
        while (true) {
            delay(50)
            val currentTick = android.os.SystemClock.elapsedRealtime()
            elapsedMillis += currentTick - previousTick
            previousTick = currentTick
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ChangeGameBackground)
            .safeDrawingPadding(),
    ) {
        when (game.stage) {
            ChangeMoneyStage.Intro -> ChangeMoneyIntro(
                onNext = { onAction(PetAction.AdvanceChangeMoney) },
                onClose = { onAction(PetAction.PauseChangeMoney) },
            )
            ChangeMoneyStage.Rules -> ChangeMoneyRules(
                onNext = { onAction(PetAction.AdvanceChangeMoney) },
                onClose = { onAction(PetAction.PauseChangeMoney) },
            )
            ChangeMoneyStage.Ready -> ChangeMoneyReady(
                challenge = game.challenge,
                bestTimeMillis = bestTimeMillis,
                onStart = { onAction(PetAction.StartChangeMoneyRound) },
                onClose = { onAction(PetAction.PauseChangeMoney) },
            )
            ChangeMoneyStage.Playing -> ChangeMoneyPlaying(
                game = game,
                elapsedMillis = elapsedMillis,
                onCountChanged = { denomination, delta ->
                    onAction(PetAction.ChangeMoneyNoteCount(denomination, delta))
                },
                onCheck = {
                    onAction(PetAction.CheckChangeMoney(elapsedMillis.coerceAtLeast(1L)))
                },
                onPause = { onAction(PetAction.PauseChangeMoney) },
            )
            ChangeMoneyStage.Success -> ChangeMoneySuccess(
                game = game,
                onNext = { onAction(PetAction.ShowChangeMoneyResult) },
                onPause = { onAction(PetAction.PauseChangeMoney) },
            )
            ChangeMoneyStage.Result -> ChangeMoneyResult(
                elapsedMillis = game.elapsedMillis,
                bestTimeMillis = bestTimeMillis,
                onFinish = { onAction(PetAction.ExitChangeMoney) },
            )
        }

        if (game.pauseDialogVisible) {
            ChangeMoneyPauseDialog(
                onContinue = { onAction(PetAction.ResumeChangeMoney) },
                onFinish = { onAction(PetAction.ExitChangeMoney) },
            )
        }
    }
}

@Composable
private fun ChangeMoneyIntro(onNext: () -> Unit, onClose: () -> Unit) {
    ChangeMoneyPage(
        onClose = onClose,
        buttonText = stringResource(R.string.onboarding_next),
        onButtonClick = onNext,
    ) {
        Spacer(Modifier.height(36.dp))
        SpeechCard(
            text = stringResource(R.string.change_money_intro),
            modifier = Modifier.widthIn(max = 290.dp),
        )
        Image(
            painter = painterResource(R.drawable.pet_main),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 20.dp),
        )
    }
}

@Composable
private fun ChangeMoneyRules(onNext: () -> Unit, onClose: () -> Unit) {
    ChangeMoneyPage(
        onClose = onClose,
        buttonText = stringResource(R.string.change_money_to_task),
        onButtonClick = onNext,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            RuleSection(
                title = stringResource(R.string.change_money_rules_task_title),
                text = stringResource(R.string.change_money_rules_task),
            )
            RuleSection(
                title = stringResource(R.string.change_money_rules_main_title),
                text = stringResource(R.string.change_money_rules_main),
                icon = R.drawable.tasks_icon,
                outlined = true,
            )
            RuleSection(
                title = stringResource(R.string.change_money_rules_hint_title),
                text = stringResource(R.string.change_money_rules_hint),
                icon = R.drawable.tip_pic,
                outlined = true,
            )
        }
    }
}

@Composable
private fun ChangeMoneyReady(
    challenge: ChangeMoneyChallenge,
    bestTimeMillis: Int?,
    onStart: () -> Unit,
    onClose: () -> Unit,
) {
    ChangeMoneyPage(
        onClose = onClose,
        buttonText = stringResource(R.string.change_money_start),
        onButtonClick = onStart,
        footerContent = {
            ResultRow(
                label = stringResource(R.string.change_money_best_result),
                value = bestTimeMillis?.let { formatChangeMoneyTime(it.toLong()) }
                    ?: stringResource(R.string.change_money_no_result),
            )
        },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().weight(1f).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(Modifier.height(6.dp))
            ChallengeSummary(challenge)
        }
    }
}

@Composable
private fun ChangeMoneyPlaying(
    game: ChangeMoneyGameState,
    elapsedMillis: Long,
    onCountChanged: (Int, Int) -> Unit,
    onCheck: () -> Unit,
    onPause: () -> Unit,
) {
    val pages = remember { listOf(ChangeMoneyDenominations.take(4), ChangeMoneyDenominations.drop(4)) }
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val total = game.selectedCounts.entries.sumOf { (denomination, count) -> denomination * count }
    val noteCount = game.selectedCounts.values.sum()

    Column(Modifier.fillMaxSize()) {
        ChangeMoneyPlayHeader(elapsedMillis = elapsedMillis, onPause = onPause)
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            SpeechCard(
                text = buildChallengeInstruction(game.challenge),
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(12.dp))
            PetHead(Modifier.size(width = 125.dp, height = 100.dp))
        }
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth().weight(1f),
        ) { page ->
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
            ) {
                pages[page].forEach { denomination ->
                    DenominationCounter(
                        denomination = denomination,
                        count = game.selectedCounts[denomination] ?: 0,
                        onMinus = { onCountChanged(denomination, -1) },
                        onPlus = { onCountChanged(denomination, 1) },
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            pages.indices.forEach { index ->
                Box(
                    Modifier
                        .padding(horizontal = 3.dp)
                        .size(if (pagerState.currentPage == index) 9.dp else 8.dp)
                        .background(
                            if (pagerState.currentPage == index) ChangeGamePurple else Color(0xFFE0B3F5),
                            CircleShape,
                        ),
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ResultRow(stringResource(R.string.change_money_total), total.toString())
            ResultRow(stringResource(R.string.change_money_note_count), noteCount.toString())
            game.validation?.let { validation ->
                Text(
                    text = validationMessage(validation),
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFB3261E),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
            }
            ChangeMoneyButton(text = stringResource(R.string.change_money_check), onClick = onCheck)
        }
    }
}

@Composable
private fun ChangeMoneySuccess(
    game: ChangeMoneyGameState,
    onNext: () -> Unit,
    onPause: () -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        ChangeMoneyPlayHeader(elapsedMillis = game.elapsedMillis, onPause = onPause)
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Top,
        ) {
            SpeechCard(
                text = stringResource(R.string.change_money_success),
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(12.dp))
            PetHead(Modifier.size(width = 125.dp, height = 100.dp))
        }
        SelectedBanknotes(
            selectedCounts = game.selectedCounts,
            modifier = Modifier.fillMaxWidth().weight(1f),
        )
        Column(
            modifier = Modifier.fillMaxWidth().background(Color.White).padding(16.dp),
        ) {
            ChangeMoneyButton(
                text = stringResource(R.string.change_money_success_next),
                onClick = onNext,
            )
        }
    }
}

@Composable
private fun ChangeMoneyResult(
    elapsedMillis: Long,
    bestTimeMillis: Int?,
    onFinish: () -> Unit,
) {
    val effectiveBestTime = bestTimeMillis?.toLong()?.coerceAtMost(elapsedMillis) ?: elapsedMillis
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(0.55f))
        Box(Modifier.width(300.dp).height(150.dp), contentAlignment = Alignment.Center) {
            Text(
                text = "✦",
                modifier = Modifier.align(Alignment.TopStart).offset(x = 26.dp),
                color = Color(0xFFE684ED),
                fontSize = 34.sp,
            )
            Text(
                text = "✦",
                modifier = Modifier.align(Alignment.BottomStart).offset(x = 2.dp, y = (-8).dp),
                color = Color(0xFFFFD19A),
                fontSize = 31.sp,
            )
            Text(
                text = "✦",
                modifier = Modifier.align(Alignment.BottomEnd).offset(x = (-5).dp, y = (-22).dp),
                color = Color(0xFFE684ED),
                fontSize = 29.sp,
            )
            Text(
                text = ((elapsedMillis + 999L) / 1_000L).toString(),
                color = ChangeGameDark,
                fontSize = 92.sp,
                lineHeight = 92.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Text(
            text = pluralStringResource(
                R.plurals.change_money_result_seconds,
                ((elapsedMillis + 999L) / 1_000L).toInt(),
                ((elapsedMillis + 999L) / 1_000L).toInt(),
            ).substringAfter(' ').replaceFirstChar { it.titlecase() },
            color = ChangeGameDark,
            fontSize = 38.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.change_money_best_result) + "  " +
                formatChangeMoneyResultTime(effectiveBestTime),
            color = ChangeGamePurple,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.weight(0.65f))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SpeechCard(
                text = stringResource(R.string.change_money_result_message),
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(12.dp))
            PetHead(Modifier.size(width = 125.dp, height = 100.dp))
        }
        Spacer(Modifier.weight(0.35f))
        ChangeMoneyButton(
            text = stringResource(R.string.change_money_finish),
            onClick = onFinish,
            modifier = Modifier.padding(bottom = 18.dp),
        )
    }
}

@Composable
private fun ChangeMoneyPage(
    onClose: () -> Unit,
    buttonText: String,
    onButtonClick: () -> Unit,
    footerContent: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(Modifier.size(42.dp))
            Text(
                text = stringResource(R.string.change_money_game_title),
                modifier = Modifier.weight(1f),
                color = ChangeGameDark,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "×",
                modifier = Modifier.size(42.dp).clickable(onClick = onClose),
                color = ChangeGameDark,
                fontSize = 30.sp,
                textAlign = TextAlign.Center,
            )
        }
        Column(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content,
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            footerContent?.invoke()
            ChangeMoneyButton(text = buttonText, onClick = onButtonClick)
        }
    }
}

@Composable
private fun ChangeMoneyPlayHeader(elapsedMillis: Long, onPause: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "◷ ${formatChangeMoneyTime(elapsedMillis)}",
            modifier = Modifier.weight(1f),
            color = ChangeGamePurple,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Ⅱ",
            modifier = Modifier.size(42.dp).clickable(onClick = onPause),
            color = ChangeGameDark,
            fontSize = 25.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ChallengeSummary(challenge: ChangeMoneyChallenge) {
    Text(
        text = stringResource(R.string.change_money_given),
        color = ChangeGameDark,
        fontSize = 19.sp,
        fontWeight = FontWeight.ExtraBold,
    )
    ResultCard(
        label = stringResource(R.string.change_money_banknote_amount),
        value = challenge.amount.toString(),
        trailingIcon = R.drawable.coin_money,
    )
    Text(
        text = stringResource(R.string.change_money_need),
        color = ChangeGameDark,
        fontSize = 19.sp,
        fontWeight = FontWeight.ExtraBold,
    )
    ResultCard(
        label = buildRequiredNotesText(challenge),
        value = "",
    )
}

@Composable
private fun buildRequiredNotesText(challenge: ChangeMoneyChallenge): String {
    val notes = pluralStringResource(
        R.plurals.change_money_notes,
        challenge.requiredNotes,
        challenge.requiredNotes,
    )
    return challengeRestriction(challenge)?.let { "$notes, $it" } ?: notes
}

@Composable
private fun ResultCard(
    label: String,
    value: String,
    @DrawableRes trailingIcon: Int? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, ChangeGameDark, RoundedCornerShape(11.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = ChangeGameDark, fontSize = 15.sp)
        if (value.isNotEmpty()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(value, color = ChangeGameDark, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                trailingIcon?.let {
                    Spacer(Modifier.width(6.dp))
                    Image(
                        painter = painterResource(it),
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun RuleSection(
    title: String,
    text: String,
    @DrawableRes icon: Int? = null,
    outlined: Boolean = false,
) {
    Text(title, color = ChangeGameDark, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (outlined) {
                    Modifier.border(1.5.dp, ChangeGameDark, RoundedCornerShape(12.dp))
                } else {
                    Modifier.background(ChangeGamePale, RoundedCornerShape(12.dp))
                },
            )
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon?.let {
            Image(
                painter = painterResource(it),
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                contentScale = ContentScale.Fit,
            )
            Spacer(Modifier.width(12.dp))
        }
        Text(text, modifier = Modifier.weight(1f), color = ChangeGameDark, fontSize = 15.sp)
    }
}

@Composable
private fun DenominationCounter(
    denomination: Int,
    count: Int,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Image(
            painter = painterResource(denominationDrawable(denomination)),
            contentDescription = denomination.toString(),
            modifier = Modifier.width(180.dp).height(72.dp),
            contentScale = ContentScale.Fit,
        )
        Row(
            modifier = Modifier
                .background(ChangeGamePale, RoundedCornerShape(12.dp))
                .padding(horizontal = 7.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CounterButton("−", onMinus)
            Text(
                text = count.toString(),
                modifier = Modifier.width(32.dp),
                color = ChangeGamePurple,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            CounterButton("+", onPlus)
        }
    }
}

@Composable
private fun CounterButton(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        modifier = Modifier.size(32.dp).clickable(onClick = onClick),
        color = ChangeGamePurple,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun SelectedBanknotes(selectedCounts: Map<Int, Int>, modifier: Modifier = Modifier) {
    val banknotes = selectedCounts.entries
        .sortedBy { it.key }
        .flatMap { (denomination, count) -> List(count) { denomination } }
        .take(8)
    Box(modifier, contentAlignment = Alignment.Center) {
        banknotes.forEachIndexed { index, denomination ->
            val row = index / 4
            val column = index % 4
            Image(
                painter = painterResource(denominationDrawable(denomination)),
                contentDescription = null,
                modifier = Modifier
                    .width(245.dp)
                    .offset(
                        x = ((column - 1.5f) * 18).dp,
                        y = ((row - 0.5f) * 95).dp,
                    )
                    .rotate((column - 1.5f) * 8f),
                contentScale = ContentScale.Fit,
            )
        }
    }
}

@Composable
private fun PetHead(modifier: Modifier = Modifier) {
    Box(modifier.clip(RoundedCornerShape(4.dp)), contentAlignment = Alignment.TopCenter) {
        Image(
            painter = painterResource(R.drawable.pet_main),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth(),
            contentScale = ContentScale.FillWidth,
            alignment = Alignment.TopCenter,
        )
    }
}

@Composable
private fun SpeechCard(text: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, color = Color.White, shape = RoundedCornerShape(10.dp)) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
            color = ChangeGameDark,
            fontSize = 15.sp,
            lineHeight = 19.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ResultRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = ChangeGameDark, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Text(value, color = ChangeGameDark, fontSize = 25.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ChangeMoneyButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(52.dp),
        colors = ButtonDefaults.buttonColors(containerColor = ChangeGamePurple),
        shape = RoundedCornerShape(10.dp),
    ) {
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ChangeMoneyPauseDialog(onContinue: () -> Unit, onFinish: () -> Unit) {
    Dialog(onDismissRequest = onContinue) {
        Surface(color = Color.White, shape = RoundedCornerShape(24.dp)) {
            Column(Modifier.fillMaxWidth().padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.change_money_pause_title),
                        modifier = Modifier.weight(1f),
                        color = ChangeGameDark,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = "×",
                        modifier = Modifier.size(34.dp).clickable(onClick = onContinue),
                        color = ChangeGameDark,
                        fontSize = 24.sp,
                        textAlign = TextAlign.Center,
                    )
                }
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ChangeMoneyDialogButton(
                        text = stringResource(R.string.change_money_continue),
                        onClick = onContinue,
                        background = ChangeGamePurple,
                        textColor = Color.White,
                        modifier = Modifier.weight(1f),
                    )
                    ChangeMoneyDialogButton(
                        text = stringResource(R.string.change_money_finish),
                        onClick = onFinish,
                        background = Color(0xFFE2B7F6),
                        textColor = ChangeGameDark,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun ChangeMoneyDialogButton(
    text: String,
    onClick: () -> Unit,
    background: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(50.dp),
        colors = ButtonDefaults.buttonColors(containerColor = background),
        shape = RoundedCornerShape(10.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp),
    ) {
        Text(text, color = textColor, fontSize = 15.sp, maxLines = 1)
    }
}

@Composable
private fun challengeRestriction(challenge: ChangeMoneyChallenge): String? = when {
    challenge.forbiddenDenominations.isNotEmpty() -> stringResource(
        R.string.change_money_restriction_forbidden,
        challenge.forbiddenDenominations.first(),
    )
    challenge.minimumDenomination != null && challenge.maximumDenomination != null ->
        stringResource(
            R.string.change_money_restriction_range,
            challenge.minimumDenomination,
            challenge.maximumDenomination,
        )
    challenge.maximumDenomination != null -> stringResource(
        R.string.change_money_restriction_maximum,
        challenge.maximumDenomination,
    )
    else -> null
}

@Composable
private fun buildChallengeInstruction(challenge: ChangeMoneyChallenge): String {
    val base = stringResource(
        R.string.change_money_task,
        challenge.amount,
        pluralStringResource(
            R.plurals.change_money_smaller_notes,
            challenge.requiredNotes,
            challenge.requiredNotes,
        ),
    )
    return challengeRestriction(challenge)?.let { "$base, $it" } ?: base
}

@Composable
private fun validationMessage(validation: ChangeMoneyValidation): String = stringResource(
    when (validation) {
        ChangeMoneyValidation.Success -> R.string.change_money_success
        ChangeMoneyValidation.WrongAmount -> R.string.change_money_error_amount
        ChangeMoneyValidation.WrongNoteCount -> R.string.change_money_error_count
        ChangeMoneyValidation.ForbiddenDenomination -> R.string.change_money_error_forbidden
        ChangeMoneyValidation.DenominationTooSmall -> R.string.change_money_error_too_small
        ChangeMoneyValidation.DenominationTooLarge -> R.string.change_money_error_too_large
    },
)

private fun formatChangeMoneyTime(millis: Long): String {
    val totalSeconds = ((millis.coerceAtLeast(0L) + 999L) / 1_000L).toInt()
    return "%d:%02d".format(totalSeconds / 60, totalSeconds % 60)
}

private fun formatChangeMoneyResultTime(millis: Long): String =
    "${((millis.coerceAtLeast(0L) + 999L) / 1_000L)} сек"

@DrawableRes
private fun denominationDrawable(denomination: Int): Int = when (denomination) {
    10 -> R.drawable.ten_coupure
    50 -> R.drawable.fifity_coupure
    100 -> R.drawable.one_hundred_coupure
    500 -> R.drawable.five_hundred_coupure
    1_000 -> R.drawable.one_thousand_coupure
    5_000 -> R.drawable.five_thousand_coupure
    else -> error("Unsupported denomination: $denomination")
}
