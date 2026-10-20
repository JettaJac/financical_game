package ru.finni.financialpetgame.lct.domain

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import ru.finni.financialpetgame.lct.R
import ru.finni.financialpetgame.lct.domain.events.ScheduledEvent

data class GameEvent(
    @param:StringRes val descriptionRes: Int,
    @param:DrawableRes val illustrationRes: Int,
    val acceptEffects: List<Effect>,
    val declineEffects: List<Effect> = emptyList(),
    val showCloseButton: Boolean = false,
    @param:StringRes val acceptButtonTextRes: Int = R.string.event_accept,
    @param:StringRes val declineButtonTextRes: Int = R.string.event_decline,
    val goalPurchase: GoalPurchase? = null,
    val customGoalPurchase: Boolean = false,
    val descriptionText: String? = null,
    val canDecline: Boolean = true,
    val hideRewardUntilAccept: Boolean = false,
    val scheduledEvent: ScheduledEvent? = null,
)

data class GoalPurchase(
    val goal: Goals,
    val price: Int,
)

object GameEvents {
    val SportsSection = GameEvent(
        descriptionRes = R.string.event_sports_section_description,
        illustrationRes = R.drawable.gym,
        acceptEffects = listOf(
            Effect(Resource.Health, 10),
            Effect(Resource.Happiness, 5),
            Effect(Resource.Energy, 10),
            Effect(Resource.Money, -30),
        ),
    )
}

internal fun canApplyEffects(
    health: Int = Int.MAX_VALUE,
    happiness: Int = Int.MAX_VALUE,
    energy: Int = Int.MAX_VALUE,
    money: Int,
    effects: List<Effect>,
): Boolean {
    val changes = effects
        .groupBy(Effect::resource)
        .mapValues { (_, resourceEffects) -> resourceEffects.sumOf(Effect::increase) }

    fun characteristicRemainsPositive(resource: Resource, currentValue: Int): Boolean {
        val change = changes[resource] ?: 0
        return change >= 0 || currentValue + change > 0
    }

    return characteristicRemainsPositive(Resource.Health, health) &&
        characteristicRemainsPositive(Resource.Happiness, happiness) &&
        characteristicRemainsPositive(Resource.Energy, energy) &&
        money + (changes[Resource.Money] ?: 0) >= 0
}

internal fun canDeclineEvent(snapshot: ru.finni.financialpetgame.lct.data.GameSnapshot, event: GameEvent): Boolean =
    event.canDecline || !canApplyEffects(
        health = snapshot.health,
        happiness = snapshot.happiness,
        energy = snapshot.energy,
        money = snapshot.money,
        effects = event.acceptEffects,
    )
