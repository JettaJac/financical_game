package com.example.financial_game.domain

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.financial_game.R

enum class Goals(
    @StringRes val titleRes: Int,
    val target: Int,
    @StringRes val descriptionRes: Int,
    @DrawableRes val illustrationRes: Int,
    val goalEffects: List<Effect>,
    val level: Int,
) {
    Pillow(
        titleRes = R.string.goal_pillow_title,
        target = 100,
        descriptionRes = R.string.goal_pillow_description,
        illustrationRes = R.drawable.goal_pillow,
        goalEffects = listOf(Effect(resource = Resource.Energy, increase = 10)),
        level = 1,
    ),
    Ball(
        titleRes = R.string.goal_ball_title,
        target = 100,
        descriptionRes = R.string.goal_ball_description,
        illustrationRes = R.drawable.ball_game,
        goalEffects = listOf(Effect(resource = Resource.Happiness, increase = 10)),
        level = 1,
    ),
    Treat(
        titleRes = R.string.goal_treat_title,
        target = 100,
        descriptionRes = R.string.goal_treat_description,
        illustrationRes = R.drawable.treat,
        goalEffects = listOf(Effect(resource = Resource.Health, increase = 10)),
        level = 1,
    ),
    MoneyBox(
        titleRes = R.string.goal_money_box_title,
        target = 300,
        descriptionRes = R.string.goal_money_box_description,
        illustrationRes = R.drawable.carbon_piggy_bank,
        goalEffects = listOf(Effect(resource = Resource.Money, increase = 10)),
        level = 2,
    ),
    ToysBundle(
        titleRes = R.string.goal_toys_bundle_title,
        target = 300,
        descriptionRes = R.string.goal_toys_bundle_description,
        illustrationRes = R.drawable.board_game,
        goalEffects = listOf(Effect(resource = Resource.Happiness, increase = 15)),
        level = 2,
    ),
    PetHouse(
        titleRes = R.string.goal_pet_house_title,
        target = 300,
        descriptionRes = R.string.goal_pet_house_description,
        illustrationRes = R.drawable.home_background,
        goalEffects = listOf(Effect(resource = Resource.Energy, increase = 15)),
        level = 2,
    ),
    Bicycle(
        titleRes = R.string.goal_bicycle_title,
        target = 600,
        descriptionRes = R.string.goal_bicycle_description,
        illustrationRes = R.drawable.trip,
        goalEffects = listOf(Effect(resource = Resource.Happiness, increase = 10)),
        level = 3,
    ),
    Party(
        titleRes = R.string.goal_party_title,
        target = 600,
        descriptionRes = R.string.goal_party_description,
        illustrationRes = R.drawable.zoo,
        goalEffects = listOf(Effect(resource = Resource.Happiness, increase = 30)),
        level = 3,
    );

    companion object {
        fun fromStorageId(storageId: String): Goals =
            entries.firstOrNull { it.name == storageId } ?: Pillow
    }
}
