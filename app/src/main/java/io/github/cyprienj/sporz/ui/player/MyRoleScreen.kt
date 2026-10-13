package io.github.cyprienj.sporz.ui.player

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.cyprienj.sporz.R
import io.github.cyprienj.sporz.ui.common.PlaceholderAction
import io.github.cyprienj.sporz.ui.common.PlaceholderScreen

@Composable
fun MyRoleScreen(
    onYourTurn: () -> Unit,
    onVoteOpened: () -> Unit,
    onOpenPlayers: () -> Unit,
    onOpenJournal: () -> Unit,
    onGameOver: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PlaceholderScreen(
        title = R.string.screen_my_role,
        designBoard = "Joueur · Mon rôle",
        modifier = modifier,
        actions = listOf(
            PlaceholderAction(R.string.action_your_turn, onYourTurn),
            PlaceholderAction(R.string.action_vote_opened, onVoteOpened),
            PlaceholderAction(R.string.tab_players, onOpenPlayers),
            PlaceholderAction(R.string.tab_journal, onOpenJournal),
            PlaceholderAction(R.string.action_game_over, onGameOver),
        ),
    )
}
