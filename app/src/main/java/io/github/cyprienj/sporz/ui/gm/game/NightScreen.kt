package io.github.cyprienj.sporz.ui.gm.game

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.cyprienj.sporz.R
import io.github.cyprienj.sporz.ui.common.PlaceholderAction
import io.github.cyprienj.sporz.ui.common.PlaceholderScreen

@Composable
fun NightScreen(
    onEndNight: () -> Unit,
    onOpenPlayerBoard: () -> Unit,
    onOpenJournal: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PlaceholderScreen(
        title = R.string.screen_night,
        designBoard = "MJ · Nuit",
        modifier = modifier,
        actions = listOf(
            PlaceholderAction(R.string.action_end_night, onEndNight),
            PlaceholderAction(R.string.tab_players, onOpenPlayerBoard),
            PlaceholderAction(R.string.tab_journal, onOpenJournal),
            PlaceholderAction(R.string.screen_settings, onOpenSettings),
        ),
    )
}
