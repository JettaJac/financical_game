package com.example.financial_game.domain

enum class CareResource { Health, Happiness, Energy }

enum class CareItem(
    val price: Int,
    val resource: CareResource,
    val resourceIncrease: Int,
) {
    SchoolLunch(price = 30, resource = CareResource.Health, resourceIncrease = 25),
    Soda(price = 10, resource = CareResource.Health, resourceIncrease = 10),
    IceCream(price = 20, resource = CareResource.Health, resourceIncrease = 15),
    ToyMouse(price = 20, resource = CareResource.Happiness, resourceIncrease = 15),
    YarnBall(price = 30, resource = CareResource.Happiness, resourceIncrease = 25),
    Music(price = 10, resource = CareResource.Happiness, resourceIncrease = 10),
    Nap(price = 20, resource = CareResource.Energy, resourceIncrease = 20),
    Cocoa(price = 10, resource = CareResource.Energy, resourceIncrease = 10),
    Pillow(price = 40, resource = CareResource.Energy, resourceIncrease = 35),
}
