package io.github.cyprienj.sporz.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import io.github.cyprienj.sporz.R

val Barlow = FontFamily(
    Font(R.font.barlow_regular, FontWeight.Normal),
    Font(R.font.barlow_medium, FontWeight.Medium),
    Font(R.font.barlow_semibold, FontWeight.SemiBold),
    Font(R.font.barlow_bold, FontWeight.Bold),
)

val BarlowCondensed = FontFamily(
    Font(R.font.barlow_condensed_semibold, FontWeight.SemiBold),
    Font(R.font.barlow_condensed_bold, FontWeight.Bold),
)

/**
 * Text styles of design.html. Styles named "display", "title", "heading",
 * "section" and "overline" are uppercase in the design: use them through
 * components or call `.uppercase()` on the text, Compose has no text-transform.
 */
object SporzTypography {
    private fun condensed(size: Int, lineHeight: Double = 1.0, weight: FontWeight = FontWeight.Bold) = TextStyle(
        fontFamily = BarlowCondensed,
        fontWeight = weight,
        fontSize = size.sp,
        lineHeight = (size * lineHeight).sp,
    )

    private fun barlow(size: Int, lineHeight: Double = 1.4, weight: FontWeight = FontWeight.Normal) = TextStyle(
        fontFamily = Barlow,
        fontWeight = weight,
        fontSize = size.sp,
        lineHeight = (size * lineHeight).sp,
    )

    // Condensed, uppercase
    val display = condensed(76, lineHeight = 0.9)           // "SPORZ" on the home screen
    val roleTitle = condensed(52, lineHeight = 0.95)        // "GÉNÉTICIEN" on the role card
    val titleLarge = condensed(44, lineHeight = 0.95)       // night action role
    val title = condensed(36)                               // screen titles
    val titleMedium = condensed(32)                         // "NUIT 2", "ÉQUIPAGE"
    val titleSmall = condensed(28)                          // dark headers with a back button
    val heading = condensed(24, lineHeight = 1.1)           // card titles, night steps
    val section = condensed(18).copy(letterSpacing = 0.1.em)
    val number = condensed(22)                              // counters, stats
    val numberLarge = condensed(26)

    // Barlow
    val overline = barlow(13).copy(letterSpacing = 0.12.em)
    val bodyLarge = barlow(16, lineHeight = 1.55)
    val body = barlow(15, lineHeight = 1.45)
    val bodyStrong = barlow(16, weight = FontWeight.SemiBold)
    val label = barlow(14, weight = FontWeight.Medium)
    val small = barlow(13)
    val caption = barlow(12)
    val button = barlow(16, weight = FontWeight.SemiBold)
}

/** Material typography mapped on Barlow, so Material components match the design. */
internal val MaterialTypography = Typography().let { base ->
    Typography(
        displayLarge = SporzTypography.display,
        displayMedium = SporzTypography.roleTitle,
        displaySmall = SporzTypography.titleLarge,
        headlineLarge = SporzTypography.title,
        headlineMedium = SporzTypography.titleMedium,
        headlineSmall = SporzTypography.titleSmall,
        titleLarge = SporzTypography.heading,
        titleMedium = SporzTypography.bodyStrong,
        titleSmall = SporzTypography.label,
        bodyLarge = SporzTypography.bodyLarge,
        bodyMedium = SporzTypography.body,
        bodySmall = SporzTypography.small,
        labelLarge = base.labelLarge.copy(fontFamily = Barlow, fontWeight = FontWeight.SemiBold),
        labelMedium = base.labelMedium.copy(fontFamily = Barlow),
        labelSmall = base.labelSmall.copy(fontFamily = Barlow),
    )
}
