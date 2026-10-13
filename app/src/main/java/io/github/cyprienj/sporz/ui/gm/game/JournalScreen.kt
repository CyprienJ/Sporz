package io.github.cyprienj.sporz.ui.gm.game

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.cyprienj.sporz.R
import io.github.cyprienj.sporz.ui.common.PlaceholderScreen

@Composable
fun JournalScreen(
    modifier: Modifier = Modifier,
) {
    PlaceholderScreen(
        title = R.string.screen_journal,
        designBoard = "MJ · Journal",
        modifier = modifier,
    )
}
