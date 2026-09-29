package ru.finni.financialpetgame.lct.ui

import androidx.annotation.DrawableRes
import ru.finni.financialpetgame.lct.R
import ru.finni.financialpetgame.lct.domain.HairColour
import ru.finni.financialpetgame.lct.domain.HairStyle

@DrawableRes
internal fun petImageResource(level: Int, colour: HairColour, style: HairStyle): Int {
    val visualLevel = level.coerceIn(1, 3)
    return when (visualLevel) {
        1 -> when (style) {
            HairStyle.Default -> when (colour) {
                HairColour.Beige -> R.drawable.pet_1_lvl_sleek_beige
                HairColour.Violet -> R.drawable.pet_1_lvl_sleek_violet
                HairColour.Orange -> R.drawable.pet_1_lvl_sleek_orange
            }
            HairStyle.Hairy -> when (colour) {
                HairColour.Beige -> R.drawable.pet_1_lvl_wool_beige
                HairColour.Violet -> R.drawable.pet_1_lvl_wool_violet
                HairColour.Orange -> R.drawable.pet_1_lvl_wool_orange
            }
            HairStyle.Curly -> when (colour) {
                HairColour.Beige -> R.drawable.pet_1_lvl_curly_beige
                HairColour.Violet -> R.drawable.pet_1_lvl_curly_violet
                HairColour.Orange -> R.drawable.pet_1_lvl_curly_orange
            }
        }
        2 -> when (style) {
            HairStyle.Default -> when (colour) {
                HairColour.Beige -> R.drawable.pet_2_lvl_sleek_beige
                HairColour.Violet -> R.drawable.pet_2_lvl_sleek_violet
                HairColour.Orange -> R.drawable.pet_2_lvl_sleek_orange
            }
            HairStyle.Hairy -> when (colour) {
                HairColour.Beige -> R.drawable.pet_2_lvl_wool_beige
                HairColour.Violet -> R.drawable.pet_2_lvl_wool_violet
                HairColour.Orange -> R.drawable.pet_2_lvl_wool_orange
            }
            HairStyle.Curly -> when (colour) {
                HairColour.Beige -> R.drawable.pet_2_lvl_curly_beige
                HairColour.Violet -> R.drawable.pet_2_lvl_curly_violet
                HairColour.Orange -> R.drawable.pet_2_lvl_curly_orange
            }
        }
        else -> when (style) {
            HairStyle.Default -> when (colour) {
                HairColour.Beige -> R.drawable.pet_3_lvl_sleek_beige
                HairColour.Violet -> R.drawable.pet_3_lvl_sleek_violet
                HairColour.Orange -> R.drawable.pet_3_lvl_sleek_orange
            }
            HairStyle.Hairy -> when (colour) {
                HairColour.Beige -> R.drawable.pet_3_lvl_wool_beige
                HairColour.Violet -> R.drawable.pet_3_lvl_wool_violet
                HairColour.Orange -> R.drawable.pet_3_lvl_wool_orange
            }
            HairStyle.Curly -> when (colour) {
                HairColour.Beige -> R.drawable.pet_3_lvl_curly_beige
                HairColour.Violet -> R.drawable.pet_3_lvl_curly_violet
                HairColour.Orange -> R.drawable.pet_3_lvl_curly_orange
            }
        }
    }
}
