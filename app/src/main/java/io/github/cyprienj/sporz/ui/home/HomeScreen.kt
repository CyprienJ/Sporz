package io.github.cyprienj.sporz.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.cyprienj.sporz.R
import io.github.cyprienj.sporz.ui.common.PlaceholderAction
import io.github.cyprienj.sporz.ui.common.PlaceholderScreen

@Composable
fun HomeScreen(
    onCreateGame: () -> Unit,
    onJoinGame: () -> Unit,
    onOpenRoleGuide: () -> Unit,
    onOpenRecentGames: () -> Unit,
    onOpenDesignCatalog: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    PlaceholderScreen(
        title = R.string.screen_home,
        designBoard = "Accueil",
        modifier = modifier,
        actions = listOf(
            PlaceholderAction(R.string.action_create_game, onCreateGame),
            PlaceholderAction(R.string.action_join_game, onJoinGame),
            PlaceholderAction(R.string.action_rules_and_roles, onOpenRoleGuide),
            PlaceholderAction(R.string.screen_recent_games, onOpenRecentGames),
        ) + listOfNotNull(onOpenDesignCatalog?.let { PlaceholderAction(R.string.screen_design_catalog, it) }),
    )
}
