package com.example.financial_game.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.example.financial_game.R
import androidx.compose.ui.text.ExperimentalTextApi

@OptIn(ExperimentalTextApi::class)
val NunitoFontFamily = FontFamily(
    Font(
        R.font.nunito_variable,
        FontWeight.Normal,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(FontWeight.Normal.weight),
        )
    ),
    Font(
        R.font.nunito_variable,
        FontWeight.Medium,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(FontWeight.Medium.weight),
        )
    ),
    Font(
        R.font.nunito_variable,
        FontWeight.SemiBold,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(FontWeight.SemiBold.weight),
        )
    ),
    Font(
        R.font.nunito_variable,
        FontWeight.Bold,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(FontWeight.Bold.weight),
        )
    ),
    Font(
        R.font.nunito_variable,
        FontWeight.ExtraBold,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(FontWeight.ExtraBold.weight),
        )
    ),
)

private fun TextStyle.withNunito(): TextStyle = copy(fontFamily = NunitoFontFamily)

private val MaterialTypography = Typography()

val Typography = with(MaterialTypography) {
    Typography(
        displayLarge = displayLarge.withNunito(),
        displayMedium = displayMedium.withNunito(),
        displaySmall = displaySmall.withNunito(),
        headlineLarge = headlineLarge.withNunito(),
        headlineMedium = headlineMedium.withNunito(),
        headlineSmall = headlineSmall.withNunito(),
        titleLarge = titleLarge.withNunito(),
        titleMedium = titleMedium.withNunito(),
        titleSmall = titleSmall.withNunito(),
        bodyLarge = bodyLarge.withNunito(),
        bodyMedium = bodyMedium.withNunito(),
        bodySmall = bodySmall.withNunito(),
        labelLarge = labelLarge.withNunito(),
        labelMedium = labelMedium.withNunito(),
        labelSmall = labelSmall.withNunito(),
    )
}
