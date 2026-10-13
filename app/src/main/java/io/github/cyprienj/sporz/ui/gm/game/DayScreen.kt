package io.github.cyprienj.sporz.ui.gm.game

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.cyprienj.sporz.R
import io.github.cyprienj.sporz.ui.common.PlaceholderAction
import io.github.cyprienj.sporz.ui.common.PlaceholderScreen

@Composable
fun DayScreen(
    onStartNight: () -> Unit,
    onGameOver: () -> Unit,
    onOpenPlayerBoard: () -> Unit,
    onOpenJournal: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PlaceholderScreen(
        title = R.string.screen_day,
        designBoard = "MJ · Jour et vote",
        modifier = modifier,
        actions = listOf(
            PlaceholderAction(R.string.action_start_night, onStartNight),
            PlaceholderAction(R.string.action_game_over, onGameOver),
            PlaceholderAction(R.string.tab_players, onOpenPlayerBoard),
            PlaceholderAction(R.string.tab_journal, onOpenJournal),
        ),
    )
}
