package io.github.cyprienj.sporz.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

object SporzShapes {
    val small = RoundedCornerShape(8.dp)     // stepper buttons, segmented options, stat tiles
    val medium = RoundedCornerShape(10.dp)   // cards, main buttons, inputs
    val large = RoundedCornerShape(12.dp)    // list groups, radio cards
    val sheet = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    val pill = RoundedCornerShape(percent = 50)
}

/**
 * The app theme. Light = design.html (light screens, navy secret areas).
 * Dark = everything dark, secret areas darker. Follows the system setting
 * unless [darkTheme] is given.
 */
@Composable
fun SporzTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val scheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val c = scheme.standard
    val material = if (darkTheme) darkColorScheme() else lightColorScheme()
    MaterialTheme(
        colorScheme = material.copy(
            primary = c.accent,
            onPrimary = c.onAccent,
            primaryContainer = c.accentSoft,
            onPrimaryContainer = c.text,
            secondary = c.inverse,
            onSecondary = c.onInverse,
            background = c.background,
            onBackground = c.text,
            surface = c.background,
            onSurface = c.text,
            surfaceVariant = c.card,
            onSurfaceVariant = c.textMuted,
            surfaceContainerLowest = c.card,
            surfaceContainerLow = c.card,
            surfaceContainer = c.card,
            outline = c.outline,
            outlineVariant = c.cardBorder,
            error = c.mutant.strong,
        ),
        typography = MaterialTypography,
    ) {
        CompositionLocalProvider(LocalSporzColorScheme provides scheme) {
            ProvideSporzColors(scheme.standard, content)
        }
    }
}

/** Access to the current Sporz colors and text styles, like `MaterialTheme`. */
object SporzTheme {
    /** Colors of the surface being drawn on. */
    val colors: SporzColors
        @Composable @ReadOnlyComposable get() = LocalSporzColors.current

    val isDark: Boolean
        @Composable @ReadOnlyComposable get() = LocalSporzColorScheme.current.isDark

    val typography: SporzTypography get() = SporzTypography
}

/**
 * Secret area (role card, night action, dark headers): navy in the light
 * theme, near-black in the dark theme. Everything inside uses its colors.
 */
@Composable
fun SecretSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
    content: @Composable () -> Unit,
) {
    val colors = LocalSporzColorScheme.current.secret
    ProvideSporzColors(colors) {
        Box(modifier.background(colors.background, shape)) { content() }
    }
}

/** Standard area placed inside a secret one (bottom sheet of the home screen). */
@Composable
fun StandardSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
    content: @Composable () -> Unit,
) {
    val colors = LocalSporzColorScheme.current.standard
    ProvideSporzColors(colors) {
        Box(modifier.background(colors.background, shape)) { content() }
    }
}

/** Card that stands out inside a secret area (night action result). */
@Composable
fun InsetSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
    content: @Composable () -> Unit,
) {
    val colors = LocalSporzColorScheme.current.inset
    ProvideSporzColors(colors) {
        Box(modifier.background(colors.background, shape)) { content() }
    }
}

@Composable
private fun ProvideSporzColors(colors: SporzColors, content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalSporzColors provides colors,
        LocalContentColor provides colors.text,
        content = content,
    )
}
