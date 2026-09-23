package com.example.financial_game.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.financial_game.R

val NunitoFontFamily = FontFamily(
    Font(R.font.nunito_variable, FontWeight.Normal),
    Font(R.font.nunito_variable, FontWeight.Medium),
    Font(R.font.nunito_variable, FontWeight.SemiBold),
    Font(R.font.nunito_variable, FontWeight.Bold),
    Font(R.font.nunito_variable, FontWeight.ExtraBold),
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
