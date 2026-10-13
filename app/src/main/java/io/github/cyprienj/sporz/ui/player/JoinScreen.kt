package io.github.cyprienj.sporz.ui.player

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.cyprienj.sporz.R
import io.github.cyprienj.sporz.ui.common.PlaceholderAction
import io.github.cyprienj.sporz.ui.common.PlaceholderScreen

@Composable
fun JoinScreen(
    onJoined: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PlaceholderScreen(
        title = R.string.screen_join,
        designBoard = "Joueur · Rejoindre",
        modifier = modifier,
        actions = listOf(
            PlaceholderAction(R.string.action_join_this_game, onJoined),
        ),
    )
}
