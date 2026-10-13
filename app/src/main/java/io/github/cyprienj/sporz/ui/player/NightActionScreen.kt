package io.github.cyprienj.sporz.ui.player

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.cyprienj.sporz.R
import io.github.cyprienj.sporz.ui.common.PlaceholderAction
import io.github.cyprienj.sporz.ui.common.PlaceholderScreen

@Composable
fun NightActionScreen(
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PlaceholderScreen(
        title = R.string.screen_night_action,
        designBoard = "Joueur · Action de nuit",
        modifier = modifier,
        actions = listOf(
            PlaceholderAction(R.string.action_confirm, onDone),
        ),
    )
}
