package io.github.cyprienj.sporz.ui.player

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.cyprienj.sporz.R
import io.github.cyprienj.sporz.ui.common.PlaceholderAction
import io.github.cyprienj.sporz.ui.common.PlaceholderScreen

@Composable
fun WaitingRoomScreen(
    onRoleDealt: () -> Unit,
    onOpenRoleGuide: () -> Unit,
    onLeave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PlaceholderScreen(
        title = R.string.screen_waiting_room,
        designBoard = "Joueur · Salle d'attente",
        modifier = modifier,
        actions = listOf(
            PlaceholderAction(R.string.action_role_dealt, onRoleDealt),
            PlaceholderAction(R.string.action_open_role_guide, onOpenRoleGuide),
            PlaceholderAction(R.string.action_leave_game, onLeave),
        ),
    )
}
