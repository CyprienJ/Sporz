package io.github.cyprienj.sporz.ui.gm.setup

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.cyprienj.sporz.R
import io.github.cyprienj.sporz.ui.common.PlaceholderAction
import io.github.cyprienj.sporz.ui.common.PlaceholderScreen

@Composable
fun GameModeScreen(
    onOpenLobby: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PlaceholderScreen(
        title = R.string.screen_game_mode,
        designBoard = "MJ · Mode de partie",
        modifier = modifier,
        actions = listOf(
            PlaceholderAction(R.string.action_open_lobby, onOpenLobby),
        ),
    )
}
