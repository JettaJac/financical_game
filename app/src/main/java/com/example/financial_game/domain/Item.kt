package com.example.financial_game.domain

enum class CareResource { Health, Happiness, Energy }

interface CardItem {
    val storageId: String
    val price: Int
    val careEffects: List<CareEffect>
    val coolDownSeconds: Int
    val level: Int
}

data class CareEffect(
    val resource: CareResource,
    val increase: Int
)

internal fun cooldownExpireAt(nowMillis: Long, cooldownSeconds: Int): Long =
    nowMillis + cooldownSeconds * 1_000L

internal fun cooldownSecondsRemaining(expireAtMillis: Long, nowMillis: Long): Int {
    val remainingMillis = (expireAtMillis - nowMillis).coerceAtLeast(0L)
    return ((remainingMillis + 999L) / 1_000L).toInt()
}

enum class FoodItem(
    override val price: Int,
    override val careEffects: List<CareEffect>,
    override val coolDownSeconds: Int,
    override val level: Int,
) : CardItem {
    SchoolLunch(price = 0, coolDownSeconds = 3*60, level = 1, careEffects = listOf(
        CareEffect(resource = CareResource.Health, increase = 20),
        CareEffect(resource = CareResource.Energy, increase = 5),
    )),
    Soda(price = 10, coolDownSeconds = 10*60, level = 2, careEffects = listOf(
        CareEffect(resource = CareResource.Health, increase = 10),
        CareEffect(resource = CareResource.Energy, increase = 10),
        CareEffect(resource = CareResource.Happiness, increase = 5),
    )),
    IceCream(price = 20, coolDownSeconds = 10*60, level = 2, careEffects = listOf(
        CareEffect(resource = CareResource.Health, increase = 13),
        CareEffect(resource = CareResource.Energy, increase = 5),
        CareEffect(resource = CareResource.Happiness, increase = 8),
    )),
    MashedPotatoes(price = 15, coolDownSeconds = 10*60, level = 2, careEffects = listOf(
        CareEffect(resource = CareResource.Health, increase = 15),
        CareEffect(resource = CareResource.Energy, increase = 13),
        CareEffect(resource = CareResource.Happiness, increase = 1),
    )),
    HamburgerWithCola(price = 25, coolDownSeconds = 10*60, level = 3, careEffects = listOf(
        CareEffect(resource = CareResource.Health, increase = 23),
        CareEffect(resource = CareResource.Energy, increase = 14),
        CareEffect(resource = CareResource.Happiness, increase = 8),
    )),
    Pizza(price = 30, coolDownSeconds = 10*60, level = 3, careEffects = listOf(
        CareEffect(resource = CareResource.Health, increase = 25),
        CareEffect(resource = CareResource.Energy, increase = 20),
        CareEffect(resource = CareResource.Happiness, increase = 9),
    )),
    Pasta(price = 40, coolDownSeconds = 13*60, level = 4, careEffects = listOf(
        CareEffect(resource = CareResource.Health, increase = 35),
        CareEffect(resource = CareResource.Energy, increase = 23),
        CareEffect(resource = CareResource.Happiness, increase = 10),
    )),
    SetRolls(price = 40, coolDownSeconds = 10*60, level = 5, careEffects = listOf(
        CareEffect(resource = CareResource.Health, increase = 40),
        CareEffect(resource = CareResource.Energy, increase = 20),
        CareEffect(resource = CareResource.Happiness, increase = 12),
    ));

    override val storageId: String
        get() = "food_$name"
}

enum class HappinessItem(
    override val price: Int,
    override val careEffects: List<CareEffect>,
    override val coolDownSeconds: Int,
    override val level: Int,
) : CardItem {
    BudgetMaster(price = 0, coolDownSeconds = 10*60, level = 1, careEffects = listOf(
        CareEffect(resource = CareResource.Health, increase = 0),
        CareEffect(resource = CareResource.Energy, increase = 0),
        CareEffect(resource = CareResource.Happiness, increase = 10),
    )),
    CatchMoney(price = 15, coolDownSeconds = 20*60, level = 2, careEffects = listOf(
        CareEffect(resource = CareResource.Health, increase = -5),
        CareEffect(resource = CareResource.Energy, increase = 0),
        CareEffect(resource = CareResource.Happiness, increase = 15),
    )),
    ChangeMoney(price = 20, coolDownSeconds = 20*60, level = 3, careEffects = listOf(
        CareEffect(resource = CareResource.Health, increase = -5),
        CareEffect(resource = CareResource.Happiness, increase = 20),
        CareEffect(resource = CareResource.Energy, increase = 0),
    )),
    PlayWithBall(price = 0, coolDownSeconds = 6*60, level = 3, careEffects = listOf(
        CareEffect(resource = CareResource.Health, increase = 0),
        CareEffect(resource = CareResource.Happiness, increase = 5),
        CareEffect(resource = CareResource.Energy, increase = -5),
    )),
    BoardGame(price = 20, coolDownSeconds = 20*60, level = 3, careEffects = listOf(
        CareEffect(resource = CareResource.Health, increase = 0),
        CareEffect(resource = CareResource.Happiness, increase = 15),
        CareEffect(resource = CareResource.Energy, increase = -5),
    )),
    MeetingWithFriends(price = 20, coolDownSeconds = 10*60, level = 3, careEffects = listOf(
        CareEffect(resource = CareResource.Health, increase = 10),
        CareEffect(resource = CareResource.Happiness, increase = 10),
        CareEffect(resource = CareResource.Energy, increase = 0),
    )),
    Trip(price = 35, coolDownSeconds = 20*60, level = 1, careEffects = listOf(
        CareEffect(resource = CareResource.Health, increase = -2),
        CareEffect(resource = CareResource.Happiness, increase = 20),
        CareEffect(resource = CareResource.Energy, increase = -15),
    )),
    Zoo(price = 45, coolDownSeconds = 10*60, level = 3, careEffects = listOf(
        CareEffect(resource = CareResource.Health, increase = -2),
        CareEffect(resource = CareResource.Happiness, increase = 20),
        CareEffect(resource = CareResource.Energy, increase = -15),
    ));

    override val storageId: String
        get() = "happiness_$name"
}

enum class EnergyItem(
    override val price: Int,
    override val careEffects: List<CareEffect>,
    override val coolDownSeconds: Int,
    override val level: Int,
) : CardItem {
    TakeASeat(price = 0, coolDownSeconds = 2*60, level = 1, careEffects = listOf(
        CareEffect(resource = CareResource.Health, increase = 0),
        CareEffect(resource = CareResource.Happiness, increase = 0),
        CareEffect(resource = CareResource.Energy, increase = 7),
    )),
    TakeANap(price = 0, coolDownSeconds = 20*60, level = 1, careEffects = listOf(
        CareEffect(resource = CareResource.Health, increase = 0),
        CareEffect(resource = CareResource.Happiness, increase = 2),
        CareEffect(resource = CareResource.Energy, increase = 17),
    )),
    ListenMusic(price = 15, coolDownSeconds = 10*60, level = 2, careEffects = listOf(
        CareEffect(resource = CareResource.Health, increase = 0),
        CareEffect(resource = CareResource.Happiness, increase = 3),
        CareEffect(resource = CareResource.Energy, increase = 15),
    )),
    TakeAMassage(price = 25, coolDownSeconds = 20*60, level = 2, careEffects = listOf(
        CareEffect(resource = CareResource.Health, increase = 0),
        CareEffect(resource = CareResource.Happiness, increase = 3),
        CareEffect(resource = CareResource.Energy, increase = 20),
    )),
    HotBath(price = 28, coolDownSeconds = 20*60, level = 2, careEffects = listOf(
        CareEffect(resource = CareResource.Health, increase = 0),
        CareEffect(resource = CareResource.Happiness, increase = 3),
        CareEffect(resource = CareResource.Energy, increase = 22),
    )),
    SPA(price = 1000, coolDownSeconds = 10*60, level = 3, careEffects = listOf(
        CareEffect(resource = CareResource.Health, increase = 0),
        CareEffect(resource = CareResource.Happiness, increase = 5),
        CareEffect(resource = CareResource.Energy, increase = 22),
    )),
    BodyMassage(price = 1000, coolDownSeconds = 20*60, level = 3, careEffects = listOf(
        CareEffect(resource = CareResource.Health, increase = 0),
        CareEffect(resource = CareResource.Happiness, increase = 5),
        CareEffect(resource = CareResource.Energy, increase = 22),
    )),
    Yoga(price = 1000, coolDownSeconds = 20*60, level = 3, careEffects = listOf(
        CareEffect(resource = CareResource.Health, increase = 0),
        CareEffect(resource = CareResource.Happiness, increase = 5),
        CareEffect(resource = CareResource.Energy, increase = 22),
    ));

    override val storageId: String
        get() = "energy_$name"
}
