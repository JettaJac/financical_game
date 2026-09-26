package com.example.financial_game.domain

enum class HairColour { Beige, Violet, Orange }

enum class EyeColour { Violet, Green, Blue }

enum class HairStyle { Default, Hairy, Curly }

data class PetAppearance(
    val name: String,
    val hairColour: HairColour,
    val eyeColour: EyeColour,
    val hairStyle: HairStyle,
)

data class PetSetup(
    val name: String,
    val hairColour: HairColour,
    val eyeColour: EyeColour,
    val hairStyle: HairStyle,
    val goal: Goals,
)
