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

internal fun canApplyEffects(money: Int, effects: List<Effect>): Boolean =
    money + effects.filter { it.resource == Resource.Money }.sumOf(Effect::increase) >= 0

internal fun canDeclineEvent(snapshot: ru.finni.financialpetgame.lct.data.GameSnapshot, event: GameEvent): Boolean =
    event.canDecline || (
        event.scheduledEvent?.definition?.kind ==
            ru.finni.financialpetgame.lct.domain.events.EventKind.Negative &&
            snapshot.activeDeposit == null &&
            snapshot.health == 0 &&
            snapshot.happiness == 0 &&
            snapshot.energy == 0
        )
