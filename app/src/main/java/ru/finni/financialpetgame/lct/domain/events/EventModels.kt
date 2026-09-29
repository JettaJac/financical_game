package ru.finni.financialpetgame.lct.domain.events

import androidx.annotation.DrawableRes
import ru.finni.financialpetgame.lct.R
import ru.finni.financialpetgame.lct.data.GameSnapshot
import ru.finni.financialpetgame.lct.domain.Effect
import ru.finni.financialpetgame.lct.domain.GameDefaults
import ru.finni.financialpetgame.lct.domain.Resource
import ru.finni.financialpetgame.lct.domain.dayForPeriod
import ru.finni.financialpetgame.lct.domain.weekForPeriod
import kotlin.math.roundToInt

enum class EventKind { Story, Positive, Negative, Training, JobOffer, Seasonal }

enum class EventFrequency { Once, PerPeriod, PermanentModifier }

data class EventDeltas(
    val health: Int = 0,
    val happiness: Int = 0,
    val energy: Int = 0,
    val money: Int = 0,
) {
    fun withMoney(value: Int): EventDeltas = copy(money = value)

    fun asEffects(): List<Effect> = buildList {
        if (health != 0) add(Effect(Resource.Health, health))
        if (happiness != 0) add(Effect(Resource.Happiness, happiness))
        if (energy != 0) add(Effect(Resource.Energy, energy))
        if (money != 0) add(Effect(Resource.Money, money))
    }
}

data class EventDef(
    val id: String,
    val title: String,
    val kind: EventKind,
    val deltas: EventDeltas,
    val canDecline: Boolean,
    val frequency: EventFrequency,
    val minLevel: Int,
    val requiresFlags: Set<String>,
    val setsFlags: Set<String>,
    val unlocksJobId: String?,
    val hideRewardUntilAccept: Boolean,
    val moneyFromCard: Int,
    val moneyFromScript: Int?,
    val permanentExpenseDelta: Int = 0,
    val poolEligible: Boolean = false,
    @param:DrawableRes val illustrationRes: Int = R.drawable.goal_pillow,
) {
    val scriptMoneyOverride: Int?
        get() = moneyFromScript
}

enum class AllowedCycles { Any, Weekend, Cycle6Or7 }

data class JobDef(
    val id: String,
    val payout: Int,
    val statCost: EventDeltas,
    val remainingActions: Int,
    val allowedCycles: AllowedCycles,
    val periodLimit: Int?,
    val taskStorageId: String?,
)

data class JobInstance(
    val jobId: String,
    val remainingActions: Int,
)

data class ScenarioStep(
    val id: String,
    val week: Int,
    val day: Int,
    val eventId: String?,
    val jobActionId: String?,
    val moneyOverride: Int?,
    val requiresFlags: Set<String>,
    val minBalanceExclusive: Int?,
    val triggerImmediately: Boolean = false,
)

data class ScheduledEvent(
    val definition: EventDef,
    val scenarioStepId: String?,
    val isScripted: Boolean,
    val appliedDeltas: EventDeltas,
) {
    val acceptEffects: List<Effect>
        get() = appliedDeltas.asEffects()
}

internal fun canResolveScheduledEvent(
    event: ScheduledEvent,
    accepted: Boolean,
    currentMoney: Int,
): Boolean {
    val canAfford = currentMoney + event.appliedDeltas.money >= 0
    return if (accepted) canAfford else event.definition.canDecline || !canAfford
}

data class EventCatalogData(
    val events: List<EventDef>,
    val jobs: List<JobDef>,
    val scenario: List<ScenarioStep>,
) {
    val eventsById: Map<String, EventDef> = events.associateBy(EventDef::id)
    val jobsById: Map<String, JobDef> = jobs.associateBy(JobDef::id)
}

class EventScheduler(private val catalog: EventCatalogData) {
    fun next(
        snapshot: GameSnapshot,
        elapsedCycleSeconds: Int = GameDefaults.CYCLE_DURATION_SECONDS,
    ): ScheduledEvent? {
        if (!snapshot.onboardingCompleted) return null
        val week = weekForPeriod(snapshot.currentPeriod) + 1
        val day = dayForPeriod(snapshot.currentPeriod)

        val cycleEvents = catalog.scenario
            .filter { it.week == week && it.day == day && it.eventId != null }
        cycleEvents
            .withIndex()
            .asSequence()
            .filter { (_, step) -> step.id !in snapshot.handledScenarioEntryIds }
            .filter { (index, step) ->
                step.triggerImmediately ||
                    elapsedCycleSeconds >= eventTriggerSecond(index, cycleEvents.size)
            }
            .map(IndexedValue<ScenarioStep>::value)
            .firstOrNull { step -> step.isEligible(snapshot) }
            ?.let { step ->
                val definition = catalog.eventsById.getValue(checkNotNull(step.eventId))
                val scriptedMoney = step.moneyOverride ?: definition.moneyFromScript
                return ScheduledEvent(
                    definition = definition,
                    scenarioStepId = step.id,
                    isScripted = true,
                    appliedDeltas = definition.deltas.withMoney(
                        scriptedMoney ?: definition.moneyFromCard,
                    ),
                )
        }

        if (week < 3 || (week == 3 && day == 1)) return null
        if (elapsedCycleSeconds < eventTriggerSecond(index = 0, eventCount = 1)) return null
        if (snapshot.currentPeriod.toString() in snapshot.eventPoolHandledCycles) return null

        val eligible = catalog.events
            .asSequence()
            .filter(EventDef::poolEligible)
            .filter { it.isEligible(snapshot, week) }
            .toList()
        if (eligible.isEmpty()) return null
        val definition = eligible[(snapshot.currentPeriod - 1).mod(eligible.size)]
        return ScheduledEvent(
            definition = definition,
            scenarioStepId = null,
            isScripted = false,
            appliedDeltas = definition.deltas.withMoney(definition.moneyFromCard),
        )
    }

    fun jobFor(event: ScheduledEvent): JobDef? =
        event.definition.unlocksJobId?.let(catalog.jobsById::get)

    private fun ScenarioStep.isEligible(snapshot: GameSnapshot): Boolean {
        val definition = eventId?.let(catalog.eventsById::get) ?: return false
        return definition.minLevel <= snapshot.level &&
            definition.requiresFlags.all(snapshot.eventFlags::contains) &&
            requiresFlags.all(snapshot.eventFlags::contains) &&
            (minBalanceExclusive == null || snapshot.money > minBalanceExclusive) &&
            definition.isFrequencyAvailable(snapshot, week)
    }

    private fun EventDef.isEligible(snapshot: GameSnapshot, week: Int): Boolean =
        minLevel <= snapshot.level &&
            requiresFlags.all(snapshot.eventFlags::contains) &&
            isFrequencyAvailable(snapshot, week)

    private fun EventDef.isFrequencyAvailable(snapshot: GameSnapshot, week: Int): Boolean =
        when (frequency) {
            EventFrequency.Once,
            EventFrequency.PermanentModifier,
            -> id !in snapshot.completedEventIds
            EventFrequency.PerPeriod -> "$id@$week" !in snapshot.eventPeriodOccurrences
        }
}

internal fun eventTriggerSecond(
    index: Int,
    eventCount: Int,
    cycleDurationSeconds: Int = GameDefaults.CYCLE_DURATION_SECONDS,
): Int {
    require(eventCount > 0)
    require(index in 0 until eventCount)
    val triggerFraction = if (eventCount == 1) {
        0.5
    } else {
        EVENT_START_FRACTION +
            index * (EVENT_END_FRACTION - EVENT_START_FRACTION) / (eventCount - 1)
    }
    return (cycleDurationSeconds * triggerFraction)
        .roundToInt()
        .coerceIn(0, cycleDurationSeconds)
}

private const val EVENT_START_FRACTION = 0.08
private const val EVENT_END_FRACTION = 0.92
