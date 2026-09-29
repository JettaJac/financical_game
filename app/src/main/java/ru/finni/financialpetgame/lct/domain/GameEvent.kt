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

internal fun canApplyEffects(money: Int, effects: List<Effect>): Boolean =
    money + effects.filter { it.resource == Resource.Money }.sumOf(Effect::increase) >= 0

internal fun canDeclineEvent(money: Int, event: GameEvent): Boolean =
    event.canDecline || !canApplyEffects(money, event.acceptEffects)
