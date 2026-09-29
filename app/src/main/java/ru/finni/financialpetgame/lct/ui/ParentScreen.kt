package ru.finni.financialpetgame.lct.ui

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.finni.financialpetgame.lct.R
import ru.finni.financialpetgame.lct.data.GameActionRecord
import ru.finni.financialpetgame.lct.data.GameSnapshot
import ru.finni.financialpetgame.lct.ui.theme.NunitoFontFamily
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val ParentPurple = Color(0xFF59256F)
private val ParentAccent = Color(0xFF8743D3)
private val ParentBackground = Color(0xFFFCF8FF)

private enum class ParentPage { Home, GoalForm, GoalCreated, History, Cycle }

@Composable
internal fun ParentScreen(
    state: GameSnapshot,
    onBack: () -> Unit,
    onSystemBack: () -> Unit,
    onAddGoal: (String, Int, Int) -> Unit,
) {
    var page by rememberSaveable { mutableStateOf(ParentPage.Home) }
    var selectedCycle by rememberSaveable { mutableStateOf<Int?>(null) }
    var createdTitle by rememberSaveable { mutableStateOf("") }
    var createdTarget by rememberSaveable { mutableStateOf(0) }
    var createdImage by rememberSaveable { mutableStateOf(R.drawable.goal_pillow) }

    val back: () -> Unit = {
        if (page == ParentPage.Home) onBack() else page = when (page) {
            ParentPage.Cycle -> ParentPage.History
            else -> ParentPage.Home
        }
    }
    BackHandler(onBack = onSystemBack)

    Column(
        Modifier
            .fillMaxSize()
            .background(ParentBackground)
            .safeDrawingPadding()
            .padding(horizontal = 20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = back) {
                Text("‹", color = ParentPurple, fontSize = 34.sp)
            }
            Text(
                text = when (page) {
                    ParentPage.History -> stringResource(R.string.parent_history)
                    ParentPage.Cycle -> stringResource(R.string.parent_cycle_history, selectedCycle ?: 1)
                    else -> stringResource(R.string.parent_area_action)
                },
                color = ParentPurple,
                fontFamily = NunitoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.height(12.dp))
        when (page) {
            ParentPage.Home -> ParentHome(
                onGoal = { page = ParentPage.GoalForm },
                onHistory = { page = ParentPage.History },
            )
            ParentPage.GoalForm -> GoalForm { title, target ->
                val images = listOf(
                    R.drawable.goal_pillow,
                    R.drawable.ball_game,
                    R.drawable.board_game,
                    R.drawable.trip,
                    R.drawable.zoo,
                )
                createdTitle = title
                createdTarget = target
                createdImage = images.random()
                onAddGoal(title, target, createdImage)
                page = ParentPage.GoalCreated
            }
            ParentPage.GoalCreated -> GoalCreated(createdTitle, createdTarget, createdImage)
            ParentPage.History -> HistoryList(state.actionHistory) { cycle ->
                selectedCycle = cycle
                page = ParentPage.Cycle
            }
            ParentPage.Cycle -> CycleHistory(
                cycle = selectedCycle ?: 1,
                records = state.actionHistory.filter { it.cycle == selectedCycle },
            )
        }
    }
}

@Composable
private fun ParentHome(onGoal: () -> Unit, onHistory: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ParentCard(stringResource(R.string.parent_goal_planning), onGoal)
        ParentCard(stringResource(R.string.parent_game_history), onHistory)
    }
}

@Composable
private fun ParentCard(title: String, onClick: () -> Unit) {
    Box(
        Modifier.fillMaxWidth().heightIn(min = 94.dp)
            .border(1.5.dp, ParentPurple, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick).padding(12.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = title,
            modifier = Modifier.fillMaxWidth(),
            color = ParentPurple,
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            autoSize = TextAutoSize.StepBased(minFontSize = 14.sp, maxFontSize = 21.sp),
        )
    }
}

@Composable
private fun GoalForm(onCreate: (String, Int) -> Unit) {
    var title by rememberSaveable { mutableStateOf("") }
    var amount by rememberSaveable { mutableStateOf("") }
    val target = amount.toIntOrNull()
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(stringResource(R.string.parent_goal_planning), color = ParentPurple, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        OutlinedTextField(title, { title = it }, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.parent_goal_name)) }, singleLine = true)
        OutlinedTextField(amount, { amount = it.filter(Char::isDigit) }, Modifier.fillMaxWidth(), label = { Text(stringResource(R.string.parent_goal_amount)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
        Button(
            onClick = { onCreate(title.trim(), checkNotNull(target)) },
            enabled = title.isNotBlank() && target != null && target > 0,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = ParentAccent),
        ) { Text(stringResource(R.string.parent_plan_action)) }
    }
}

@Composable
private fun GoalCreated(title: String, target: Int, image: Int) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(stringResource(R.string.parent_goal_planning), color = ParentPurple, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(28.dp))
        Image(painterResource(image), null, Modifier.size(150.dp))
        Text("$title  $target", color = ParentPurple, fontSize = 21.sp, fontWeight = FontWeight.Bold)
        Text(stringResource(R.string.parent_goal_added), color = ParentAccent)
    }
}

@Composable
private fun HistoryList(records: List<GameActionRecord>, onCycle: (Int) -> Unit) {
    val groups = records.groupBy(GameActionRecord::cycle).toSortedMap(compareByDescending { it })
    if (groups.isEmpty()) Text(stringResource(R.string.parent_history_empty), color = ParentPurple)
    Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        groups.forEach { (cycle, entries) ->
            ParentCard(stringResource(R.string.parent_cycle_summary, cycle, entries.size)) { onCycle(cycle) }
        }
    }
}

@Composable
private fun CycleHistory(cycle: Int, records: List<GameActionRecord>) {
    val formatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        records.forEach { record ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    Text(record.description, color = ParentPurple, fontSize = 17.sp)
                    Text(formatter.format(Date(record.timestamp)), color = ParentPurple.copy(alpha = 0.55f), fontSize = 12.sp)
                }
                if (record.moneyDelta != 0) Text("${if (record.moneyDelta > 0) "+" else ""}${record.moneyDelta}", color = ParentPurple, fontWeight = FontWeight.Bold)
            }
        }
        if (records.isEmpty()) Text(stringResource(R.string.parent_history_empty), color = ParentPurple)
    }
}
