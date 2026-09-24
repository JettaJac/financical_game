package com.example.financial_game.domain

enum class CareResource { Health, Happiness, Energy }

data class CareEffect(
    val resource: CareResource,
    val increaseLevel: Int
)

enum class CareItem(
    val price: Int,
    val careEffect: List<CareEffect>
) {
    SchoolLunch(price = 0, careEffect = listOf(
        CareEffect(resource = CareResource.Health, increaseLevel = 20),
        CareEffect(resource = CareResource.Energy, increaseLevel = 5),
    )),
    Soda(price = 10, careEffect = listOf(
        CareEffect(resource = CareResource.Health, increaseLevel = 10),
        CareEffect(resource = CareResource.Energy, increaseLevel = 10),
        CareEffect(resource = CareResource.Happiness, increaseLevel = 5),
    )),
    IceCream(price = 20, careEffect = listOf(
        CareEffect(resource = CareResource.Health, increaseLevel = 13),
        CareEffect(resource = CareResource.Energy, increaseLevel = 5),
        CareEffect(resource = CareResource.Happiness, increaseLevel = 8),
    )),
    MashedPotatoes(price = 15, careEffect = listOf(
        CareEffect(resource = CareResource.Health, increaseLevel = 15),
        CareEffect(resource = CareResource.Energy, increaseLevel = 13),
        CareEffect(resource = CareResource.Happiness, increaseLevel = 1),
    )),
    HamburgerWithCola(price = 25, careEffect = listOf(
        CareEffect(resource = CareResource.Health, increaseLevel = 23),
        CareEffect(resource = CareResource.Energy, increaseLevel = 14),
        CareEffect(resource = CareResource.Happiness, increaseLevel = 8),
    )),
    Pizza(price = 30, careEffect = listOf(
        CareEffect(resource = CareResource.Health, increaseLevel = 25),
        CareEffect(resource = CareResource.Energy, increaseLevel = 20),
        CareEffect(resource = CareResource.Happiness, increaseLevel = 9),
    )),
    Pasta(price = 40, careEffect = listOf(
        CareEffect(resource = CareResource.Health, increaseLevel = 35),
        CareEffect(resource = CareResource.Energy, increaseLevel = 23),
        CareEffect(resource = CareResource.Happiness, increaseLevel = 10),
    )),
    SetRolls(price = 40, careEffect = listOf(
        CareEffect(resource = CareResource.Health, increaseLevel = 40),
        CareEffect(resource = CareResource.Energy, increaseLevel = 20),
        CareEffect(resource = CareResource.Happiness, increaseLevel = 12),
    )),
}
