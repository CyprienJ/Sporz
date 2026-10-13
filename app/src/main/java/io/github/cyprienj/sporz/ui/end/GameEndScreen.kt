package io.github.cyprienj.sporz.ui.end

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.cyprienj.sporz.R
import io.github.cyprienj.sporz.ui.common.PlaceholderAction
import io.github.cyprienj.sporz.ui.common.PlaceholderScreen

@Composable
fun GameEndScreen(
    onReplaySameCrew: () -> Unit,
    onOpenJournal: () -> Unit,
    onBackHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PlaceholderScreen(
        title = R.string.screen_game_end,
        designBoard = "Fin de partie",
        modifier = modifier,
        actions = listOf(
            PlaceholderAction(R.string.action_replay_same_crew, onReplaySameCrew),
            PlaceholderAction(R.string.action_open_full_journal, onOpenJournal),
            PlaceholderAction(R.string.action_back_home, onBackHome),
        ),
    )
}
