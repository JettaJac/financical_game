package ru.finni.financialpetgame.lct.ui

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.finni.financialpetgame.lct.R
import ru.finni.financialpetgame.lct.ui.theme.NunitoFontFamily

private val HandbookDark = Color(0xFF4B2163)
private val HandbookBackground = Color(0xFFFEFBFF)
private val HandbookHeader = Color(0xFFF3E2FC)
private val HandbookDivider = Color(0xFF6A2E88)

private enum class HandbookPage { Home, Basics, Deposit, Budget }
private enum class HandbookSection { Income, Expenses, Deposit, Other }

private data class HandbookEntryRes(
    @param:StringRes val titleRes: Int,
    @param:StringRes val bodyRes: Int,
)

private data class HandbookEntry(val title: String, val body: String)

private val otherTermResources = listOf(
    HandbookEntryRes(R.string.handbook_term_antivirus, R.string.handbook_definition_antivirus),
    HandbookEntryRes(R.string.handbook_term_antiques, R.string.handbook_definition_antiques),
    HandbookEntryRes(R.string.handbook_term_rent, R.string.handbook_definition_rent),
    HandbookEntryRes(R.string.handbook_term_grant, R.string.handbook_definition_grant),
    HandbookEntryRes(R.string.handbook_term_dividends, R.string.handbook_definition_dividends),
    HandbookEntryRes(R.string.handbook_term_jewelry, R.string.handbook_definition_jewelry),
    HandbookEntryRes(R.string.handbook_term_salary, R.string.handbook_definition_salary),
    HandbookEntryRes(R.string.handbook_term_investments, R.string.handbook_definition_investments),
    HandbookEntryRes(R.string.handbook_term_mortgage, R.string.handbook_definition_mortgage),
    HandbookEntryRes(R.string.handbook_term_capitalization, R.string.handbook_definition_capitalization),
    HandbookEntryRes(R.string.handbook_term_pocket_money, R.string.handbook_definition_pocket_money),
    HandbookEntryRes(R.string.handbook_term_rent_payment, R.string.handbook_definition_rent_payment),
    HandbookEntryRes(R.string.handbook_term_credit, R.string.handbook_definition_credit),
    HandbookEntryRes(R.string.handbook_term_crypto, R.string.handbook_definition_crypto),
    HandbookEntryRes(R.string.handbook_term_utilities, R.string.handbook_definition_utilities),
    HandbookEntryRes(R.string.handbook_term_coupon, R.string.handbook_definition_coupon),
    HandbookEntryRes(R.string.handbook_term_cashback, R.string.handbook_definition_cashback),
    HandbookEntryRes(R.string.handbook_term_marketplace, R.string.handbook_definition_marketplace),
    HandbookEntryRes(R.string.handbook_term_savings, R.string.handbook_definition_savings),
    HandbookEntryRes(R.string.handbook_term_inheritance, R.string.handbook_definition_inheritance),
    HandbookEntryRes(R.string.handbook_term_optional_expenses, R.string.handbook_definition_optional_expenses),
    HandbookEntryRes(R.string.handbook_term_unplanned_expenses, R.string.handbook_definition_unplanned_expenses),
    HandbookEntryRes(R.string.handbook_term_bond, R.string.handbook_definition_bond),
    HandbookEntryRes(R.string.handbook_term_mandatory_expenses, R.string.handbook_definition_mandatory_expenses),
    HandbookEntryRes(R.string.handbook_term_mutual_fund, R.string.handbook_definition_mutual_fund),
    HandbookEntryRes(R.string.handbook_term_passive_income, R.string.handbook_definition_passive_income),
    HandbookEntryRes(R.string.handbook_term_penalty_interest, R.string.handbook_definition_penalty_interest),
    HandbookEntryRes(R.string.handbook_term_pension_fund, R.string.handbook_definition_pension_fund),
    HandbookEntryRes(R.string.handbook_term_pension, R.string.handbook_definition_pension),
    HandbookEntryRes(R.string.handbook_term_part_time_job, R.string.handbook_definition_part_time_job),
    HandbookEntryRes(R.string.handbook_term_portfolio, R.string.handbook_definition_portfolio),
    HandbookEntryRes(R.string.handbook_term_allowance, R.string.handbook_definition_allowance),
    HandbookEntryRes(R.string.handbook_term_bonus, R.string.handbook_definition_bonus),
    HandbookEntryRes(R.string.handbook_term_deposit_interest, R.string.handbook_definition_deposit_interest),
    HandbookEntryRes(R.string.handbook_term_reserve_capital, R.string.handbook_definition_reserve_capital),
    HandbookEntryRes(R.string.handbook_term_annuity, R.string.handbook_definition_annuity),
    HandbookEntryRes(R.string.handbook_term_royalty, R.string.handbook_definition_royalty),
    HandbookEntryRes(R.string.handbook_term_insurance, R.string.handbook_definition_insurance),
    HandbookEntryRes(R.string.handbook_term_credit_insurance, R.string.handbook_definition_credit_insurance),
    HandbookEntryRes(R.string.handbook_term_scholarship, R.string.handbook_definition_scholarship),
    HandbookEntryRes(R.string.handbook_term_franchising, R.string.handbook_definition_franchising),
    HandbookEntryRes(R.string.handbook_term_fine, R.string.handbook_definition_fine),
)

private val depositArticleResources = listOf(
    HandbookEntryRes(R.string.handbook_deposit_what_title, R.string.handbook_deposit_what_body),
    HandbookEntryRes(R.string.handbook_deposit_profit_title, R.string.handbook_deposit_profit_body),
    HandbookEntryRes(R.string.handbook_deposit_early_title, R.string.handbook_deposit_early_body),
    HandbookEntryRes(R.string.handbook_deposit_budget_title, R.string.handbook_deposit_budget_body),
)

private val budgetArticleResources = listOf(
    HandbookEntryRes(R.string.handbook_budget_plan_title, R.string.handbook_budget_plan_body),
    HandbookEntryRes(R.string.handbook_budget_formula_title, R.string.handbook_budget_formula_body),
    HandbookEntryRes(R.string.handbook_budget_follow_title, R.string.handbook_budget_follow_body),
    HandbookEntryRes(R.string.handbook_budget_compare_title, R.string.handbook_budget_compare_body),
)

@Composable
internal fun HandbookScreen(onBack: () -> Unit) {
    var page by rememberSaveable { mutableStateOf(HandbookPage.Home) }
    var query by rememberSaveable { mutableStateOf("") }

    fun navigate(nextPage: HandbookPage) {
        query = ""
        page = nextPage
    }

    BackHandler {
        if (page == HandbookPage.Home) onBack() else navigate(HandbookPage.Home)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HandbookBackground)
            .safeDrawingPadding(),
    ) {
        HandbookHeader(
            title = stringResource(
                when (page) {
                    HandbookPage.Home -> R.string.handbook_title
                    HandbookPage.Home -> R.string.handbook_game
                    HandbookPage.Basics -> R.string.handbook_basics
                    HandbookPage.Deposit -> R.string.handbook_deposit
                    HandbookPage.Budget -> R.string.handbook_budget
                },
            ),
            onBack = {
                if (page == HandbookPage.Home) onBack() else navigate(HandbookPage.Home)
            },
        )
        key(page) {
            when (page) {
                HandbookPage.Home -> HandbookHome(
                    query = query,
                    onQueryChange = { query = it },
                    onOpen = ::navigate,
                )
                HandbookPage.Basics -> HandbookBasics(
                    query = query,
                    onQueryChange = { query = it },
                )
                HandbookPage.Deposit -> HandbookArticle(
                    query = query,
                    onQueryChange = { query = it },
                    entries = localizedEntries(depositArticleResources),
                )
                HandbookPage.Budget -> HandbookArticle(
                    query = query,
                    onQueryChange = { query = it },
                    entries = localizedEntries(budgetArticleResources),
                )
            }
        }
    }
}

@Composable
private fun HandbookHeader(title: String, onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(HandbookHeader)
            .padding(horizontal = 12.dp, vertical = 16.dp),
    ) {
        Text(
            text = "‹",
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(44.dp)
                .clickable(onClick = onBack),
            color = HandbookDark,
            fontSize = 40.sp,
            lineHeight = 40.sp,
            textAlign = TextAlign.Center,
        )
        Text(
            text = title,
            modifier = Modifier.align(Alignment.Center),
            color = HandbookDark,
            fontFamily = NunitoFontFamily,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun HandbookHome(
    query: String,
    onQueryChange: (String) -> Unit,
    onOpen: (HandbookPage) -> Unit,
) {
    val basicsEntries = handbookCoreEntries() + localizedEntries(otherTermResources)
    val depositEntries = localizedEntries(depositArticleResources)
    val budgetEntries = localizedEntries(budgetArticleResources)
    val normalizedQuery = query.trim()
    val pages = remember(normalizedQuery, basicsEntries, depositEntries, budgetEntries) {
        listOf(
            Triple(HandbookPage.Basics, R.string.handbook_basics, basicsEntries),
            Triple(HandbookPage.Deposit, R.string.handbook_deposit, depositEntries),
            Triple(HandbookPage.Budget, R.string.handbook_budget, budgetEntries),
        ).filter { (_, _, entries) ->
            normalizedQuery.isBlank() || entries.any { it.matches(normalizedQuery) }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        HandbookSearch(query, onQueryChange)
        pages.forEach { (target, titleRes, _) ->
            HandbookCategoryCard(stringResource(titleRes)) { onOpen(target) }
        }
        if (pages.isEmpty()) HandbookEmpty()
    }
}

@Composable
private fun HandbookBasics(query: String, onQueryChange: (String) -> Unit) {
    val coreEntries = handbookCoreEntries()
    val otherEntries = localizedEntries(otherTermResources)
    val results = (coreEntries + otherEntries).filter { it.matches(query) }
    var expandedSection by rememberSaveable {
        mutableStateOf<HandbookSection?>(HandbookSection.Income)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        HandbookSearch(query, onQueryChange)
        Spacer(Modifier.height(6.dp))
        if (query.isNotBlank()) {
            results.forEach { HandbookDefinition(it) }
            if (results.isEmpty()) HandbookEmpty()
        } else {
            HandbookAccordion(
                title = stringResource(R.string.handbook_income),
                expanded = expandedSection == HandbookSection.Income,
                onClick = { expandedSection = expandedSection.toggle(HandbookSection.Income) },
            ) {
                Text(stringResource(R.string.handbook_income_definition), style = handbookBodyStyle())
                HandbookParagraph(
                    stringResource(R.string.handbook_income_recurring_title),
                    stringResource(R.string.handbook_income_recurring_body),
                )
                HandbookParagraph(
                    stringResource(R.string.handbook_income_irregular_title),
                    stringResource(R.string.handbook_income_irregular_body),
                )
            }
            HandbookAccordion(
                title = stringResource(R.string.handbook_expenses),
                expanded = expandedSection == HandbookSection.Expenses,
                onClick = { expandedSection = expandedSection.toggle(HandbookSection.Expenses) },
            ) {
                Text(stringResource(R.string.handbook_expense_definition), style = handbookBodyStyle())
                HandbookParagraph(
                    stringResource(R.string.handbook_expense_mandatory_title),
                    stringResource(R.string.handbook_expense_mandatory_body),
                )
                HandbookParagraph(
                    stringResource(R.string.handbook_expense_optional_title),
                    stringResource(R.string.handbook_expense_optional_body),
                )
                HandbookParagraph(
                    stringResource(R.string.handbook_expense_unplanned_title),
                    stringResource(R.string.handbook_expense_unplanned_body),
                )
            }
            HandbookAccordion(
                title = stringResource(R.string.handbook_deposit_term),
                expanded = expandedSection == HandbookSection.Deposit,
                onClick = { expandedSection = expandedSection.toggle(HandbookSection.Deposit) },
            ) {
                Text(stringResource(R.string.handbook_deposit_definition), style = handbookBodyStyle())
            }
            HandbookAccordion(
                title = stringResource(R.string.handbook_other_terms),
                expanded = expandedSection == HandbookSection.Other,
                onClick = { expandedSection = expandedSection.toggle(HandbookSection.Other) },
            ) {
                otherEntries.forEach { HandbookDefinition(it) }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun HandbookArticle(
    query: String,
    onQueryChange: (String) -> Unit,
    entries: List<HandbookEntry>,
) {
    val filtered = entries.filter { it.matches(query) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        HandbookSearch(query, onQueryChange)
        filtered.forEach { HandbookDefinitionCard(it) }
        if (filtered.isEmpty()) HandbookEmpty()
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun HandbookSearch(query: String, onQueryChange: (String) -> Unit) {
    val focusManager = LocalFocusManager.current
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(stringResource(R.string.handbook_search), color = HandbookDark) },
        trailingIcon = { SearchGlyph() },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        textStyle = handbookBodyStyle(),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = HandbookDark,
            unfocusedBorderColor = HandbookDark,
            focusedTextColor = HandbookDark,
            unfocusedTextColor = HandbookDark,
            cursorColor = HandbookDark,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
        ),
    )
}

@Composable
private fun SearchGlyph() {
    Canvas(Modifier.size(27.dp).padding(2.dp)) {
        val radius = size.minDimension * 0.31f
        val center = androidx.compose.ui.geometry.Offset(size.width * 0.43f, size.height * 0.43f)
        drawCircle(
            color = HandbookDark,
            radius = radius,
            center = center,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5.dp.toPx()),
        )
        drawLine(
            color = HandbookDark,
            start = androidx.compose.ui.geometry.Offset(
                center.x + radius * 0.72f,
                center.y + radius * 0.72f,
            ),
            end = androidx.compose.ui.geometry.Offset(size.width * 0.88f, size.height * 0.88f),
            strokeWidth = 2.5.dp.toPx(),
            cap = StrokeCap.Round,
        )
    }
}

@Composable
private fun HandbookCategoryCard(title: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(104.dp)
            .border(1.5.dp, HandbookDark, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = title,
            color = HandbookDark,
            fontFamily = NunitoFontFamily,
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun HandbookAccordion(
    title: String,
    expanded: Boolean,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                color = HandbookDark,
                fontFamily = NunitoFontFamily,
                fontSize = 29.sp,
                fontWeight = FontWeight.ExtraBold,
            )
            Text(
                text = if (expanded) "⌃" else "⌄",
                color = HandbookDark,
                fontSize = 26.sp,
            )
        }
        Box(Modifier.fillMaxWidth().height(1.5.dp).background(HandbookDivider))
        if (expanded) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                content()
            }
        }
    }
}

@Composable
private fun HandbookParagraph(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = title,
            color = HandbookDark,
            fontSize = 19.sp,
            fontWeight = FontWeight.ExtraBold,
        )
        Text(text = body, style = handbookBodyStyle())
    }
}

@Composable
private fun HandbookDefinition(entry: HandbookEntry) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Text(
            text = entry.title,
            color = HandbookDark,
            fontSize = 19.sp,
            fontWeight = FontWeight.ExtraBold,
        )
        Text(text = entry.body, style = handbookBodyStyle())
    }
}

@Composable
private fun HandbookDefinitionCard(entry: HandbookEntry) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, HandbookDark, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = entry.title,
            color = HandbookDark,
            fontSize = 21.sp,
            fontWeight = FontWeight.ExtraBold,
        )
        Text(text = entry.body, style = handbookBodyStyle())
    }
}

@Composable
private fun HandbookEmpty() {
    Text(
        text = stringResource(R.string.handbook_no_results),
        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
        color = HandbookDark.copy(alpha = 0.65f),
        fontSize = 17.sp,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun handbookCoreEntries(): List<HandbookEntry> = listOf(
    HandbookEntry(
        stringResource(R.string.handbook_income),
        listOf(
            stringResource(R.string.handbook_income_definition),
            stringResource(R.string.handbook_income_recurring_body),
            stringResource(R.string.handbook_income_irregular_body),
        ).joinToString(" "),
    ),
    HandbookEntry(
        stringResource(R.string.handbook_expenses),
        listOf(
            stringResource(R.string.handbook_expense_definition),
            stringResource(R.string.handbook_expense_mandatory_body),
            stringResource(R.string.handbook_expense_optional_body),
            stringResource(R.string.handbook_expense_unplanned_body),
        ).joinToString(" "),
    ),
    HandbookEntry(
        stringResource(R.string.handbook_deposit_term),
        stringResource(R.string.handbook_deposit_definition),
    ),
)

@Composable
private fun localizedEntries(resources: List<HandbookEntryRes>): List<HandbookEntry> =
    resources.map { HandbookEntry(stringResource(it.titleRes), stringResource(it.bodyRes)) }

private fun HandbookEntry.matches(query: String): Boolean {
    val normalized = query.trim()
    return normalized.isBlank() || title.contains(normalized, ignoreCase = true) ||
        body.contains(normalized, ignoreCase = true)
}

private fun HandbookSection?.toggle(section: HandbookSection): HandbookSection? =
    if (this == section) null else section

@Composable
private fun handbookBodyStyle() = androidx.compose.ui.text.TextStyle(
    color = HandbookDark,
    fontFamily = NunitoFontFamily,
    fontSize = 17.sp,
    lineHeight = 23.sp,
)
