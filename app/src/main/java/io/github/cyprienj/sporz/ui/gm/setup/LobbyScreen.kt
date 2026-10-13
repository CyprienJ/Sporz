package io.github.cyprienj.sporz.ui.gm.setup

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.cyprienj.sporz.R
import io.github.cyprienj.sporz.ui.common.PlaceholderAction
import io.github.cyprienj.sporz.ui.common.PlaceholderScreen

@Composable
fun LobbyScreen(
    onChooseRoles: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PlaceholderScreen(
        title = R.string.screen_lobby,
        designBoard = "MJ · Lobby",
        modifier = modifier,
        actions = listOf(
            PlaceholderAction(R.string.action_choose_roles, onChooseRoles),
            PlaceholderAction(R.string.screen_settings, onOpenSettings),
        ),
    )
}
