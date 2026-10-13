package io.github.cyprienj.sporz.ui.theme

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Number of screens currently asking for light status bar icons. During a
 * navigation both screens are composed for a moment, so each screen cannot
 * simply restore what it found: the last one leaving would undo the new one
 * (e.g. lobby → settings, both with a dark header).
 */
private var lightIconRequests = 0

/**
 * Light status bar icons while this screen is shown, for screens whose top is
 * a secret (dark) surface. When no such screen is left, the theme's default
 * comes back: dark icons in the light theme, light icons in the dark theme.
 */
@Composable
fun LightStatusBarIcons() {
    val view = LocalView.current
    val darkTheme = SporzTheme.isDark
    if (LocalInspectionMode.current) return
    DisposableEffect(view) {
        val window = (view.context as? Activity)?.window ?: return@DisposableEffect onDispose {}
        val controller = WindowCompat.getInsetsController(window, view)
        lightIconRequests++
        controller.isAppearanceLightStatusBars = false
        onDispose {
            lightIconRequests--
            if (lightIconRequests == 0) controller.isAppearanceLightStatusBars = !darkTheme
        }
    }
}
