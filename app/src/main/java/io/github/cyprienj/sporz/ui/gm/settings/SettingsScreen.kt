package io.github.cyprienj.sporz.ui.gm.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.cyprienj.sporz.R
import io.github.cyprienj.sporz.ui.common.PlaceholderScreen

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
) {
    PlaceholderScreen(
        title = R.string.screen_settings,
        designBoard = "MJ · Paramètres et automatisation",
        modifier = modifier,
    )
}
