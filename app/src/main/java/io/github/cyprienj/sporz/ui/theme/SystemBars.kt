package io.github.cyprienj.sporz.ui.theme

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Light status bar icons while this screen is shown, for screens whose top is
 * a secret (dark) surface. The previous appearance is restored on exit.
 */
@Composable
fun LightStatusBarIcons() {
    val view = LocalView.current
    if (LocalInspectionMode.current) return
    DisposableEffect(view) {
        val window = (view.context as? Activity)?.window ?: return@DisposableEffect onDispose {}
        val controller = WindowCompat.getInsetsController(window, view)
        val previous = controller.isAppearanceLightStatusBars
        controller.isAppearanceLightStatusBars = false
        onDispose { controller.isAppearanceLightStatusBars = previous }
    }
}
