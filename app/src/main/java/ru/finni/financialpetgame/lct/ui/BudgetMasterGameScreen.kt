package ru.finni.financialpetgame.lct.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import ru.finni.financialpetgame.lct.domain.BUDGET_MASTER_SUCCESS_SCORE
import ru.finni.financialpetgame.lct.domain.BudgetDirection
import ru.finni.financialpetgame.lct.domain.BudgetMasterCategory

private val BudgetMasterDark = Color(0xFF4B2163)
private val BudgetMasterPurple = Color(0xFF8138D0)
private val BudgetMasterLightPurple = Color(0xFFE2B7F6)
private val BudgetMasterPale = Color(0xFFF0D9FB)
private val BudgetMasterError = Color(0xFFFFE6EB)
private val BudgetMasterBackground = Brush.verticalGradient(
    listOf(Color(0xFFF4E2FD), Color(0xFFFCF8FF), Color(0xFFF7EAFF)),
)

@Composable
internal fun BudgetMasterGameScreen(
    game: BudgetMasterGameState,
    bestScore: Int,
    onAction: (PetAction) -> Unit,
) {
    var showHandbook by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(game.stage, game.pauseDialogVisible, showHandbook) {
        if (game.stage != BudgetMasterStage.Playing || game.pauseDialogVisible || showHandbook) {
            return@LaunchedEffect
        }
        while (true) {
            delay(1_000L)
            onAction(PetAction.TickBudgetMaster)
        }
    }

    if (showHandbook) {
        HandbookScreen(onBack = { showHandbook = false })
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BudgetMasterBackground)
            .safeDrawingPadding(),
    ) {
        when (game.stage) {
            BudgetMasterStage.Intro -> BudgetMasterIntro(
                onNext = { onAction(PetAction.AdvanceBudgetMaster) },
                onClose = { onAction(PetAction.PauseBudgetMaster) },
            )
            BudgetMasterStage.Rules -> BudgetMasterRules(
                bestScore = bestScore,
                onStart = { onAction(PetAction.StartBudgetMasterRound) },
                onClose = { onAction(PetAction.PauseBudgetMaster) },
                onHandbook = { showHandbook = true },
            )
            BudgetMasterStage.Playing -> BudgetMasterPlaying(
                game = game,
                onDirection = { onAction(PetAction.SelectBudgetDirection(it)) },
                onCategory = { onAction(PetAction.SelectBudgetCategory(it)) },
                onFinish = { onAction(PetAction.FinishBudgetMaster) },
                onPause = { onAction(PetAction.PauseBudgetMaster) },
                onHandbook = { showHandbook = true },
            )
            BudgetMasterStage.Result -> BudgetMasterResult(
                score = game.score,
                bestScore = maxOf(bestScore, game.score),
                onFinish = { onAction(PetAction.ExitBudgetMaster) },
            )
        }

        if (game.pauseDialogVisible) {
            BudgetMasterPauseDialog(
                onContinue = { onAction(PetAction.ResumeBudgetMaster) },
                onFinish = { onAction(PetAction.ExitBudgetMaster) },
            )
        }
    }
}

@Composable
private fun BudgetMasterIntro(onNext: () -> Unit, onClose: () -> Unit) {
    BudgetMasterPage(
        onClose = onClose,
        buttonText = stringResource(R.string.onboarding_next),
        onButtonClick = onNext,
    ) {
        Spacer(Modifier.height(50.dp))
        BudgetMasterSpeech(
            text = stringResource(R.string.budget_master_intro),
            modifier = Modifier.widthIn(max = 275.dp),
        )
        Image(
            painter = painterResource(R.drawable.pet_main),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 26.dp),
            contentScale = ContentScale.Fit,
        )
    }
}

@Composable
private fun BudgetMasterRules(
    bestScore: Int,
    onStart: () -> Unit,
    onClose: () -> Unit,
    onHandbook: () -> Unit,
) {
    BudgetMasterPage(
        onClose = onClose,
        buttonText = stringResource(R.string.change_money_start),
        onButtonClick = onStart,
        footer = {
            BudgetMasterResultRow(
                stringResource(R.string.budget_master_best_result),
                pluralStringResource(R.plurals.budget_master_cards, bestScore, bestScore),
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(verticalAlignment = Alignment.Top) {
                BudgetMasterSpeech(
                    text = stringResource(R.string.budget_master_reminder),
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(10.dp))
                BudgetMasterPetHead(Modifier.size(width = 125.dp, height = 100.dp))
            }
            Text(
                stringResource(R.string.budget_master_rules_title),
                color = BudgetMasterDark,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(
                text = stringResource(R.string.budget_master_rules),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BudgetMasterPale, RoundedCornerShape(12.dp))
                    .padding(14.dp),
                color = BudgetMasterDark,
                fontSize = 15.sp,
                lineHeight = 20.sp,
            )
            Text(
                stringResource(R.string.budget_master_hint_title),
                color = BudgetMasterDark,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, BudgetMasterDark, RoundedCornerShape(12.dp))
                    .clickable(onClick = onHandbook)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(R.drawable.tip_pic),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    stringResource(R.string.budget_master_hint),
                    modifier = Modifier.weight(1f),
                    color = BudgetMasterDark,
                    fontSize = 15.sp,
                    lineHeight = 19.sp,
                )
            }
        }
    }
}

@Composable
private fun BudgetMasterPlaying(
    game: BudgetMasterGameState,
    onDirection: (BudgetDirection) -> Unit,
    onCategory: (BudgetMasterCategory) -> Unit,
    onFinish: () -> Unit,
    onPause: () -> Unit,
    onHandbook: () -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "✦ ${game.score}",
                modifier = Modifier.weight(1f),
                color = BudgetMasterPurple,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "◷ ${game.secondsRemaining}",
                color = BudgetMasterPurple,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = "?",
                modifier = Modifier.size(42.dp).clickable(onClick = onHandbook),
                color = BudgetMasterDark,
                fontSize = 25.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = "Ⅱ",
                modifier = Modifier.size(42.dp).clickable(onClick = onPause),
                color = BudgetMasterDark,
                fontSize = 25.sp,
                textAlign = TextAlign.Center,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 4.dp),
            verticalAlignment = Alignment.Top,
        ) {
            BudgetMasterSpeech(
                text = if (game.step == BudgetMasterStep.Direction) {
                    stringResource(R.string.budget_master_choose_direction)
                } else {
                    stringResource(R.string.budget_master_choose_category)
                },
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(10.dp))
            BudgetMasterPetHead(Modifier.size(width = 125.dp, height = 100.dp))
        }
        Column(
            modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(R.string.budget_master_choose_category),
                color = BudgetMasterDark,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            Spacer(Modifier.height(14.dp))
            Image(
                painter = painterResource(R.drawable.master_budget_wallet),
                contentDescription = null,
                modifier = Modifier.size(210.dp),
                contentScale = ContentScale.Fit,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = game.card.title,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                color = BudgetMasterDark,
                fontSize = 18.sp,
                lineHeight = 23.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            game.errorCategory?.let { category ->
                Spacer(Modifier.height(10.dp))
                Text(
                    text = stringResource(
                        R.string.budget_master_wrong_answer,
                        budgetMasterExplanation(category),
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BudgetMasterError, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    color = Color(0xFF9A2344),
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            if (game.step == BudgetMasterStep.Direction) {
                BudgetMasterButton(
                    text = stringResource(R.string.budget_master_income),
                    onClick = { onDirection(BudgetDirection.Income) },
                    enabled = game.errorCategory == null,
                )
                BudgetMasterButton(
                    text = stringResource(R.string.budget_master_expense),
                    onClick = { onDirection(BudgetDirection.Expense) },
                    secondary = true,
                    enabled = game.errorCategory == null,
                )
            } else {
                val income = game.card.category.direction == BudgetDirection.Income
                BudgetMasterButton(
                    text = stringResource(
                        if (income) R.string.budget_master_recurring_income
                        else R.string.budget_master_mandatory_expense,
                    ),
                    onClick = {
                        onCategory(
                            if (income) BudgetMasterCategory.RecurringIncome
                            else BudgetMasterCategory.MandatoryExpense,
                        )
                    },
                    enabled = game.errorCategory == null,
                )
                BudgetMasterButton(
                    text = stringResource(
                        if (income) R.string.budget_master_irregular_income
                        else R.string.budget_master_optional_expense,
                    ),
                    onClick = {
                        onCategory(
                            if (income) BudgetMasterCategory.IrregularIncome
                            else BudgetMasterCategory.OptionalExpense,
                        )
                    },
                    secondary = true,
                    enabled = game.errorCategory == null,
                )
            }
            BudgetMasterButton(
                text = stringResource(R.string.budget_master_finish),
                onClick = onFinish,
            )
        }
    }
}

@Composable
private fun BudgetMasterResult(score: Int, bestScore: Int, onFinish: () -> Unit) {
    val successful = score >= BUDGET_MASTER_SUCCESS_SCORE
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(0.45f))
        Text("✦", color = Color(0xFFE684ED), fontSize = 34.sp)
        Text(
            text = score.toString(),
            color = BudgetMasterDark,
            fontSize = 92.sp,
            lineHeight = 92.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = pluralStringResource(R.plurals.budget_master_cards, score, score)
                .substringAfter(' '),
            color = BudgetMasterDark,
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(18.dp))
        Text(
            text = stringResource(R.string.budget_master_best_result) + "  " + bestScore,
            color = BudgetMasterPurple,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.weight(0.75f))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            BudgetMasterSpeech(
                text = stringResource(
                    if (successful) R.string.budget_master_result_success
                    else R.string.budget_master_result_failure,
                ),
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(10.dp))
            BudgetMasterPetHead(Modifier.size(width = 125.dp, height = 100.dp))
        }
        Spacer(Modifier.weight(0.3f))
        BudgetMasterButton(
            text = stringResource(R.string.budget_master_finish),
            onClick = onFinish,
            modifier = Modifier.padding(bottom = 18.dp),
            secondary = true,
        )
    }
}

@Composable
private fun BudgetMasterPage(
    onClose: () -> Unit,
    buttonText: String,
    onButtonClick: () -> Unit,
    footer: @Composable (() -> Unit)? = null,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(Modifier.size(42.dp))
            Text(
                text = stringResource(R.string.budget_master_game_title),
                modifier = Modifier.weight(1f),
                color = BudgetMasterDark,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "×",
                modifier = Modifier.size(42.dp).clickable(onClick = onClose),
                color = BudgetMasterDark,
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
            footer?.invoke()
            BudgetMasterButton(buttonText, onButtonClick)
        }
    }
}

@Composable
private fun BudgetMasterSpeech(text: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, color = Color.White, shape = RoundedCornerShape(10.dp)) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
            color = BudgetMasterDark,
            fontSize = 15.sp,
            lineHeight = 19.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun BudgetMasterPetHead(modifier: Modifier = Modifier) {
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
private fun BudgetMasterButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    secondary: Boolean = false,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().height(50.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (secondary) BudgetMasterLightPurple else BudgetMasterPurple,
            contentColor = if (secondary) BudgetMasterDark else Color.White,
        ),
        shape = RoundedCornerShape(10.dp),
    ) {
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun BudgetMasterResultRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = BudgetMasterDark, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Text(value, color = BudgetMasterDark, fontSize = 21.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun budgetMasterExplanation(category: BudgetMasterCategory): String = stringResource(
    when (category) {
        BudgetMasterCategory.RecurringIncome -> R.string.budget_master_error_recurring_income
        BudgetMasterCategory.IrregularIncome -> R.string.budget_master_error_irregular_income
        BudgetMasterCategory.MandatoryExpense -> R.string.budget_master_error_mandatory_expense
        BudgetMasterCategory.OptionalExpense -> R.string.budget_master_error_optional_expense
    },
)

@Composable
private fun BudgetMasterPauseDialog(onContinue: () -> Unit, onFinish: () -> Unit) {
    Dialog(onDismissRequest = onContinue) {
        Surface(color = Color.White, shape = RoundedCornerShape(24.dp)) {
            Column(Modifier.fillMaxWidth().padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.change_money_pause_title),
                        modifier = Modifier.weight(1f),
                        color = BudgetMasterDark,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = "×",
                        modifier = Modifier.size(34.dp).clickable(onClick = onContinue),
                        color = BudgetMasterDark,
                        fontSize = 24.sp,
                        textAlign = TextAlign.Center,
                    )
                }
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    BudgetMasterButton(
                        text = stringResource(R.string.change_money_continue),
                        onClick = onContinue,
                        modifier = Modifier.weight(1f),
                    )
                    BudgetMasterButton(
                        text = stringResource(R.string.change_money_finish),
                        onClick = onFinish,
                        modifier = Modifier.weight(1f),
                        secondary = true,
                    )
                }
            }
        }
    }
}
