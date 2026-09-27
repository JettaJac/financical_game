package com.example.financial_game.data

import android.content.Context
import com.example.financial_game.domain.events.AllowedCycles
import com.example.financial_game.domain.events.EventCatalogData
import com.example.financial_game.domain.events.EventDef
import com.example.financial_game.domain.events.EventDeltas
import com.example.financial_game.domain.events.EventFrequency
import com.example.financial_game.domain.events.EventKind
import com.example.financial_game.domain.events.JobDef
import com.example.financial_game.domain.events.ScenarioStep
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import org.json.JSONArray
import org.json.JSONObject

@Singleton
class EventCatalog @Inject constructor(@ApplicationContext context: Context) {
    val data: EventCatalogData = EventCatalogData(
        events = JSONArray(context.readAsset("events/event_defs.json")).objects().map(::eventDef),
        jobs = JSONArray(context.readAsset("events/job_defs.json")).objects().map(::jobDef),
        scenario = JSONArray(context.readAsset("events/scenario_script.json"))
            .objects()
            .map(::scenarioStep),
    )

    private fun eventDef(json: JSONObject) = EventDef(
        id = json.getString("id"),
        title = json.getString("title"),
        kind = EventKind.valueOf(json.getString("kind").snakeToPascal()),
        deltas = json.getJSONObject("deltas").deltas(),
        canDecline = json.getBoolean("canDecline"),
        frequency = EventFrequency.valueOf(json.getString("frequency").snakeToPascal()),
        minLevel = json.optInt("minLevel", 1),
        requiresFlags = json.optJSONArray("requiresFlags").strings(),
        setsFlags = json.optJSONArray("setsFlags").strings(),
        unlocksJobId = json.optString("unlocksJobId").takeIf(String::isNotBlank),
        hideRewardUntilAccept = json.optBoolean("hideRewardUntilAccept", false),
        moneyFromCard = json.optInt("moneyFromCard", json.getJSONObject("deltas").optInt("money")),
        moneyFromScript = json.optIntOrNull("moneyFromScript"),
        permanentExpenseDelta = json.optInt("permanentExpenseDelta", 0),
        poolEligible = json.optBoolean("poolEligible", false),
    )

    private fun jobDef(json: JSONObject) = JobDef(
        id = json.getString("id"),
        payout = json.getInt("payout"),
        statCost = json.getJSONObject("statCost").deltas(),
        remainingActions = json.getInt("remainingActions"),
        allowedCycles = AllowedCycles.valueOf(json.getString("allowedCycles").snakeToPascal()),
        periodLimit = json.optIntOrNull("periodLimit"),
        taskStorageId = json.optString("taskStorageId").takeIf(String::isNotBlank),
    )

    private fun scenarioStep(json: JSONObject) = ScenarioStep(
        id = json.getString("id"),
        week = json.getInt("week"),
        day = json.getInt("day"),
        eventId = json.optString("eventId").takeIf(String::isNotBlank),
        jobActionId = json.optString("jobActionId").takeIf(String::isNotBlank),
        moneyOverride = json.optIntOrNull("moneyOverride"),
        requiresFlags = json.optJSONArray("requiresFlags").strings(),
        minBalanceExclusive = json.optIntOrNull("minBalanceExclusive"),
    )
}

private fun Context.readAsset(path: String): String =
    assets.open(path).bufferedReader().use { it.readText() }

private fun JSONArray.objects(): List<JSONObject> =
    List(length()) { index -> getJSONObject(index) }

private fun JSONArray?.strings(): Set<String> =
    if (this == null) emptySet() else List(length()) { index -> getString(index) }.toSet()

private fun JSONObject.deltas() = EventDeltas(
    health = optInt("health"),
    happiness = optInt("happiness"),
    energy = optInt("energy"),
    money = optInt("money"),
)

private fun JSONObject.optIntOrNull(key: String): Int? =
    if (has(key) && !isNull(key)) getInt(key) else null

private fun String.snakeToPascal(): String =
    split('_').joinToString("") { word -> word.replaceFirstChar(Char::uppercase) }
