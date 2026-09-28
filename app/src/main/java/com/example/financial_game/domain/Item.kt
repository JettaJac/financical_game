package com.example.financial_game.domain

import kotlin.math.ceil

enum class Resource { Health, Happiness, Energy, Money }

interface CardItem {
    val storageId: String
    val price: Int
    val careEffects: List<Effect>
    val cooldown: Cooldown
    val level: Int
}

sealed interface Cooldown {
    data object None : Cooldown

    data class Cycles(val value: Double) : Cooldown {
        init { require(value > 0.0) }
    }
}

sealed interface CooldownRemaining {
    data class Time(val seconds: Int) : CooldownRemaining
}

data class Effect(
    val resource: Resource,
    val increase: Int
)

internal fun currentCyclePosition(
    currentPeriod: Int,
    cycleSecondsRemaining: Int,
    cycleDurationSeconds: Int = GameDefaults.CYCLE_DURATION_SECONDS,
): Double {
    if (cycleDurationSeconds <= 0) return currentPeriod.toDouble()
    val remainingSeconds = cycleSecondsRemaining.coerceIn(0, cycleDurationSeconds)
    val elapsedFraction = 1.0 - remainingSeconds.toDouble() / cycleDurationSeconds
    return currentPeriod + elapsedFraction
}

internal fun cooldownUnlockCycle(currentCyclePosition: Double, cooldownCycles: Double): Double =
    currentCyclePosition + cooldownCycles

internal fun cooldownSecondsRemaining(
    unlockCycle: Double,
    currentCyclePosition: Double,
    cycleDurationSeconds: Int = GameDefaults.CYCLE_DURATION_SECONDS,
): Int {
    val remainingSeconds =
        (unlockCycle - currentCyclePosition).coerceAtLeast(0.0) * cycleDurationSeconds
    return ceil((remainingSeconds - 1e-9).coerceAtLeast(0.0))
        .coerceAtMost(Int.MAX_VALUE.toDouble())
        .toInt()
}

internal fun cooldownRemaining(
    cooldown: Cooldown,
    unlockCycle: Double,
    currentPeriod: Int,
    cycleSecondsRemaining: Int,
): CooldownRemaining? = when (cooldown) {
    Cooldown.None -> null
    is Cooldown.Cycles -> cooldownSecondsRemaining(
        unlockCycle = unlockCycle,
        currentCyclePosition = currentCyclePosition(
            currentPeriod = currentPeriod,
            cycleSecondsRemaining = cycleSecondsRemaining,
        ),
    )
        .takeIf { it > 0 }
        ?.let(CooldownRemaining::Time)
}

enum class FoodItem(
    override val price: Int,
    override val careEffects: List<Effect>,
    override val cooldown: Cooldown,
    override val level: Int,
) : CardItem {
    SchoolLunch(price = 0, cooldown = Cooldown.Cycles(0.15), level = 1, careEffects = listOf(
        Effect(resource = Resource.Health, increase = 20),
        Effect(resource = Resource.Energy, increase = 5),
    )),
    Soda(price = 10, cooldown = Cooldown.Cycles(0.5), level = 2, careEffects = listOf(
        Effect(resource = Resource.Health, increase = 10),
        Effect(resource = Resource.Energy, increase = 10),
        Effect(resource = Resource.Happiness, increase = 5),
    )),
    IceCream(price = 20, cooldown = Cooldown.Cycles(0.5), level = 2, careEffects = listOf(
        Effect(resource = Resource.Health, increase = 13),
        Effect(resource = Resource.Energy, increase = 5),
        Effect(resource = Resource.Happiness, increase = 8),
    )),
    MashedPotatoes(price = 15, cooldown = Cooldown.Cycles(0.5), level = 2, careEffects = listOf(
        Effect(resource = Resource.Health, increase = 15),
        Effect(resource = Resource.Energy, increase = 13),
        Effect(resource = Resource.Happiness, increase = 1),
    )),
    HamburgerWithCola(price = 25, cooldown = Cooldown.Cycles(0.5), level = 3, careEffects = listOf(
        Effect(resource = Resource.Health, increase = 23),
        Effect(resource = Resource.Energy, increase = 14),
        Effect(resource = Resource.Happiness, increase = 8),
    )),
    Pizza(price = 30, cooldown = Cooldown.Cycles(0.5), level = 3, careEffects = listOf(
        Effect(resource = Resource.Health, increase = 25),
        Effect(resource = Resource.Energy, increase = 20),
        Effect(resource = Resource.Happiness, increase = 9),
    )),
    Pasta(price = 40, cooldown = Cooldown.Cycles(0.65), level = 4, careEffects = listOf(
        Effect(resource = Resource.Health, increase = 35),
        Effect(resource = Resource.Energy, increase = 23),
        Effect(resource = Resource.Happiness, increase = 10),
    )),
    SetRolls(price = 40, cooldown = Cooldown.Cycles(0.5), level = 5, careEffects = listOf(
        Effect(resource = Resource.Health, increase = 40),
        Effect(resource = Resource.Energy, increase = 20),
        Effect(resource = Resource.Happiness, increase = 12),
    ));

    override val storageId: String
        get() = "food_$name"
}

enum class HappinessItem(
    override val price: Int,
    override val careEffects: List<Effect>,
    override val cooldown: Cooldown,
    override val level: Int,
) : CardItem {
    BudgetMaster(price = 0, cooldown = Cooldown.Cycles(0.5), level = 1, careEffects = listOf(
        Effect(resource = Resource.Health, increase = 0),
        Effect(resource = Resource.Energy, increase = 0),
        Effect(resource = Resource.Happiness, increase = 10),
    )),
    CatchMoney(price = 15, cooldown = Cooldown.Cycles(1.0), level = 2, careEffects = listOf(
        Effect(resource = Resource.Health, increase = -5),
        Effect(resource = Resource.Energy, increase = 0),
        Effect(resource = Resource.Happiness, increase = 15),
    )),
    ChangeMoney(price = 20, cooldown = Cooldown.Cycles(1.0), level = 3, careEffects = listOf(
        Effect(resource = Resource.Health, increase = -5),
        Effect(resource = Resource.Happiness, increase = 20),
        Effect(resource = Resource.Energy, increase = 0),
    )),
    PlayWithBall(price = 0, cooldown = Cooldown.Cycles(0.3), level = 3, careEffects = listOf(
        Effect(resource = Resource.Health, increase = 0),
        Effect(resource = Resource.Happiness, increase = 5),
        Effect(resource = Resource.Energy, increase = -5),
    )),
    BoardGame(price = 20, cooldown = Cooldown.Cycles(1.0), level = 3, careEffects = listOf(
        Effect(resource = Resource.Health, increase = 0),
        Effect(resource = Resource.Happiness, increase = 15),
        Effect(resource = Resource.Energy, increase = -5),
    )),
    MeetingWithFriends(price = 20, cooldown = Cooldown.Cycles(0.5), level = 3, careEffects = listOf(
        Effect(resource = Resource.Health, increase = 10),
        Effect(resource = Resource.Happiness, increase = 10),
        Effect(resource = Resource.Energy, increase = 0),
    )),
    Trip(price = 35, cooldown = Cooldown.Cycles(1.0), level = 1, careEffects = listOf(
        Effect(resource = Resource.Health, increase = -2),
        Effect(resource = Resource.Happiness, increase = 20),
        Effect(resource = Resource.Energy, increase = -15),
    )),
    Zoo(price = 45, cooldown = Cooldown.Cycles(0.5), level = 3, careEffects = listOf(
        Effect(resource = Resource.Health, increase = -2),
        Effect(resource = Resource.Happiness, increase = 20),
        Effect(resource = Resource.Energy, increase = -15),
    ));

    override val storageId: String
        get() = "happiness_$name"
}

enum class EnergyItem(
    override val price: Int,
    override val careEffects: List<Effect>,
    override val cooldown: Cooldown,
    override val level: Int,
) : CardItem {
    TakeASeat(price = 0, cooldown = Cooldown.Cycles(0.1), level = 1, careEffects = listOf(
        Effect(resource = Resource.Health, increase = 0),
        Effect(resource = Resource.Happiness, increase = 0),
        Effect(resource = Resource.Energy, increase = 7),
    )),
    TakeANap(price = 0, cooldown = Cooldown.Cycles(1.0), level = 1, careEffects = listOf(
        Effect(resource = Resource.Health, increase = 0),
        Effect(resource = Resource.Happiness, increase = 2),
        Effect(resource = Resource.Energy, increase = 17),
    )),
    ListenMusic(price = 15, cooldown = Cooldown.Cycles(0.5), level = 2, careEffects = listOf(
        Effect(resource = Resource.Health, increase = 0),
        Effect(resource = Resource.Happiness, increase = 3),
        Effect(resource = Resource.Energy, increase = 15),
    )),
    TakeAMassage(price = 25, cooldown = Cooldown.Cycles(1.0), level = 2, careEffects = listOf(
        Effect(resource = Resource.Health, increase = 0),
        Effect(resource = Resource.Happiness, increase = 3),
        Effect(resource = Resource.Energy, increase = 20),
    )),
    HotBath(price = 28, cooldown = Cooldown.Cycles(1.0), level = 2, careEffects = listOf(
        Effect(resource = Resource.Health, increase = 0),
        Effect(resource = Resource.Happiness, increase = 3),
        Effect(resource = Resource.Energy, increase = 22),
    )),
    SPA(price = 1000, cooldown = Cooldown.Cycles(0.5), level = 3, careEffects = listOf(
        Effect(resource = Resource.Health, increase = 0),
        Effect(resource = Resource.Happiness, increase = 5),
        Effect(resource = Resource.Energy, increase = 22),
    )),
    BodyMassage(price = 1000, cooldown = Cooldown.Cycles(1.0), level = 3, careEffects = listOf(
        Effect(resource = Resource.Health, increase = 0),
        Effect(resource = Resource.Happiness, increase = 5),
        Effect(resource = Resource.Energy, increase = 22),
    )),
    Yoga(price = 1000, cooldown = Cooldown.Cycles(1.0), level = 3, careEffects = listOf(
        Effect(resource = Resource.Health, increase = 0),
        Effect(resource = Resource.Happiness, increase = 5),
        Effect(resource = Resource.Energy, increase = 22),
    ));

    override val storageId: String
        get() = "energy_$name"
}

sealed interface TaskUsageLimit {
    data object None : TaskUsageLimit
    data class PerWeek(val count: Int) : TaskUsageLimit
    data class Total(val count: Int) : TaskUsageLimit
}

private val OncePerCycleTaskCooldown = Cooldown.Cycles(1.0)

enum class TaskItem(
    val earnings: Int,
    override val level: Int,
    override val cooldown: Cooldown,
    override val careEffects: List<Effect>,
    val activeDays: IntRange = 1..7,
    val usageLimit: TaskUsageLimit = TaskUsageLimit.None,
    val requiredTaskStorageId: String? = null,
    val requiresEvent: Boolean = false,
) : CardItem {
    GetReady(
        earnings = 0,
        level = 1,
        cooldown = OncePerCycleTaskCooldown,
        careEffects = taskEffects(health = 0, happiness = -5, energy = -5),
    ),
    Lessons(
        earnings = 0,
        level = 1,
        cooldown = OncePerCycleTaskCooldown,
        activeDays = 1..5,
        careEffects = taskEffects(health = 0, happiness = -5, energy = -5),
    ),
    Cleaning(
        earnings = 0,
        level = 1,
        cooldown = OncePerCycleTaskCooldown,
        activeDays = 6..7,
        careEffects = taskEffects(health = 0, happiness = -5, energy = -10),
    ),
    BeadCrafts(
        earnings = 0,
        level = 2,
        cooldown = OncePerCycleTaskCooldown,
        usageLimit = TaskUsageLimit.PerWeek(3),
        requiredTaskStorageId = "task_GetReady",
        careEffects = taskEffects(health = -3, happiness = 0, energy = -3),
    ),
    DeliverNewspapers(
        earnings = 25,
        level = 2,
        cooldown = OncePerCycleTaskCooldown,
        requiredTaskStorageId = "task_Lessons",
        requiresEvent = true,
        careEffects = taskEffects(health = -10, happiness = 0, energy = -10, money = 25),
    ),
    HandOutFlyers(
        earnings = 30,
        level = 2,
        cooldown = OncePerCycleTaskCooldown,
        usageLimit = TaskUsageLimit.Total(5),
        requiresEvent = true,
        careEffects = taskEffects(health = -8, happiness = 0, energy = -8, money = 30),
    ),
    RecyclePaper(
        earnings = 0,
        level = 2,
        cooldown = OncePerCycleTaskCooldown,
        requiresEvent = true,
        careEffects = taskEffects(health = -5, happiness = 0, energy = -5),
    ),
    FeedNeighboursCat(
        earnings = 40,
        level = 2,
        cooldown = OncePerCycleTaskCooldown,
        requiresEvent = true,
        careEffects = taskEffects(health = -5, happiness = 0, energy = -10, money = 40),
    ),
    WalkNeighboursDog(
        earnings = 55,
        level = 3,
        cooldown = OncePerCycleTaskCooldown,
        requiresEvent = true,
        careEffects = taskEffects(health = -15, happiness = 0, energy = -15, money = 55),
    ),
    HelpGrandfather(
        earnings = 20,
        level = 3,
        cooldown = OncePerCycleTaskCooldown,
        activeDays = 6..7,
        usageLimit = TaskUsageLimit.Total(5),
        requiresEvent = true,
        careEffects = taskEffects(health = -2, happiness = 0, energy = -2, money = 20),
    ),
    WaterPlants(
        earnings = 10,
        level = 3,
        cooldown = OncePerCycleTaskCooldown,
        requiresEvent = true,
        careEffects = taskEffects(health = -2, happiness = 0, energy = -2, money = 10),
    );

    override val price: Int = 0
    override val storageId: String
        get() = "task_$name"
}

internal fun TaskItem.isAvailable(
    currentPeriod: Int,
    totalUses: Int,
    weeklyUses: Int,
    usageWeek: Int,
    completedTaskCounts: Map<String, Int>,
    eventUnlocked: Boolean,
): Boolean {
    val currentWeek = weekForPeriod(currentPeriod)
    val usesThisWeek = if (usageWeek == currentWeek) weeklyUses else 0
    if (dayForPeriod(currentPeriod) !in activeDays) return false
    if (requiresEvent && !eventUnlocked) return false
    if (!eventUnlocked &&
        requiredTaskStorageId != null && completedTaskCounts[requiredTaskStorageId] == null
    ) {
        return false
    }
    return when (val limit = usageLimit) {
        TaskUsageLimit.None -> true
        is TaskUsageLimit.PerWeek -> usesThisWeek < limit.count
        is TaskUsageLimit.Total -> totalUses < limit.count
    }
}

internal fun dayForPeriod(currentPeriod: Int): Int =
    ((currentPeriod.coerceAtLeast(1) - 1) % 7) + 1

internal fun weekForPeriod(currentPeriod: Int): Int =
    (currentPeriod.coerceAtLeast(1) - 1) / 7

private fun taskEffects(
    health: Int,
    happiness: Int,
    energy: Int,
    money: Int = 0,
): List<Effect> = buildList {
    add(Effect(Resource.Health, health))
    add(Effect(Resource.Happiness, happiness))
    add(Effect(Resource.Energy, energy))
    if (money != 0) add(Effect(Resource.Money, money))
}

enum class ShopItem(
    override val price: Int,
    override val careEffects: List<Effect>,
    override val level: Int,
    override val cooldown: Cooldown = Cooldown.None,
) : CardItem {
    FlowerPot(price = 15, level = 1, careEffects = listOf(
        Effect(Resource.Health, 0), Effect(Resource.Happiness, 5), Effect(Resource.Energy, 2),
    )),
    FavouriteMug(price = 10, level = 1, careEffects = listOf(
        Effect(Resource.Health, 0), Effect(Resource.Happiness, 2), Effect(Resource.Energy, 3),
    )),
    FloorLamp(price = 15, level = 2, careEffects = listOf(
        Effect(Resource.Health, 0), Effect(Resource.Happiness, 4), Effect(Resource.Energy, 3),
    )),
    SoftRug(price = 15, level = 2, careEffects = listOf(
        Effect(Resource.Health, 0), Effect(Resource.Happiness, 3), Effect(Resource.Energy, 4),
    )),
    SoftArmchair(price = 25, level = 2, careEffects = listOf(
        Effect(Resource.Health, 0), Effect(Resource.Happiness, 3), Effect(Resource.Energy, 8),
    )),
    StylishScarf(price = 35, level = 3, careEffects = listOf(
        Effect(Resource.Health, 10), Effect(Resource.Happiness, 10), Effect(Resource.Energy, 5),
    )),
    GlowingOrb(price = 35, level = 3, careEffects = listOf(
        Effect(Resource.Health, 10), Effect(Resource.Happiness, 10), Effect(Resource.Energy, 5),
    ));

    override val storageId: String
        get() = "shop_$name"
}
