package ru.finni.financialpetgame.lct.domain

import kotlin.random.Random

internal const val BUDGET_MASTER_ROUND_SECONDS = 60
internal const val BUDGET_MASTER_SUCCESS_SCORE = 3

enum class BudgetDirection { Income, Expense }

enum class BudgetMasterCategory(val direction: BudgetDirection) {
    RecurringIncome(BudgetDirection.Income),
    IrregularIncome(BudgetDirection.Income),
    MandatoryExpense(BudgetDirection.Expense),
    OptionalExpense(BudgetDirection.Expense),
}

data class BudgetMasterCard(
    val id: String,
    val title: String,
    val category: BudgetMasterCategory,
)

internal val BudgetMasterCards: List<BudgetMasterCard> = buildList {
    fun cards(category: BudgetMasterCategory, vararg titles: String) {
        titles.forEachIndexed { index, title ->
            add(BudgetMasterCard("${category.name}_$index", title, category))
        }
    }

    cards(
        BudgetMasterCategory.RecurringIncome,
        "Карманные деньги, которые тебе регулярно выдают родители",
        "Зарплата родителей",
        "Пенсия бабушки или дедушки",
        "Стипендия за отличную учёбу",
        "Проценты по вкладу",
        "Ежемесячная премия",
        "Деньги от сдачи квартиры в аренду",
        "Деньги от сдачи машины в аренду",
        "Деньги от сдачи помещения в аренду",
        "Доход от своего бизнеса",
        "Доход от фермы",
        "Доход от пасеки",
        "Доход от теплицы",
        "Доход от бизнеса \"Стоянка\"",
        "Доход от кафе",
        "Прибыль от бизнеса",
        "Доход от акций",
        "Дивиденды — доход от акций. Платится ежемесячно",
        "Купон — доход от облигаций. Платится ежемеквартально",
        "Доход от написанной тобой книги",
        "Доход от YouTube-канала",
        "Доход от рекламы в блоге",
    )
    cards(
        BudgetMasterCategory.IrregularIncome,
        "Деньги от продажи игрушки",
        "Деньги от продажи поделки",
        "Бабушка подарила деньги на день рождения",
        "Деньги от сдачи макулатуры",
        "Поймал рыбу и продал её",
        "Деньги от продажи старой вещи",
        "Подработал в выходные и получил 100 монет",
        "Деньги от перепродажи старой вещи",
        "Деньги от продажи коллекции",
        "Раздал листовки и получил за это деньги",
        "Помог с ремонтом и получил вознаграждение",
        "Выгулял собаку соседей и получил вознаграждение",
        "Сделал открытку и получил деньги за её продажу",
        "Нарисовал рисунок и получил деньги за его продажу",
        "Связал шарф и продал его",
        "Деньги от продажи урожая",
        "Деньги за участие в ярмарке",
        "Нерегулярная прибыль от продаж на маркетплейсе",
        "Деньги за работу на фрилансе",
    )
    cards(
        BudgetMasterCategory.MandatoryExpense,
        "Еда",
        "Квартплата",
        "Проездной",
        "Одежда",
        "Лекарства",
        "Обучение в школе",
        "Питание в школе",
        "Медицинская помощь",
        "Средства личной гигиены",
    )
    cards(
        BudgetMasterCategory.OptionalExpense,
        "Купить мороженое",
        "Купить газировку",
        "Купить наклейки",
        "Сходить в кино на новый мультфильм",
        "Покататься на карусели",
        "Купить воздушный шарик",
        "Купить кофе",
        "Купить круассан",
        "Купить чизкейк",
        "Заказать пиццу домой",
        "Заказать суши",
        "Сходить в кафе",
        "Сходить в кофейню",
    )
}

internal fun randomBudgetMasterCard(
    excludedIds: Set<String> = emptySet(),
    random: Random = Random.Default,
): BudgetMasterCard {
    val available = BudgetMasterCards.filterNot { it.id in excludedIds }
    return (available.ifEmpty { BudgetMasterCards }).random(random)
}

internal fun isCorrectBudgetDirection(card: BudgetMasterCard, direction: BudgetDirection): Boolean =
    card.category.direction == direction

internal fun isCorrectBudgetCategory(
    card: BudgetMasterCard,
    category: BudgetMasterCategory,
): Boolean = card.category == category
