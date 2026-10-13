package io.github.cyprienj.sporz.ui.history

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.cyprienj.sporz.R
import io.github.cyprienj.sporz.ui.common.PlaceholderScreen

@Composable
fun RecentGamesScreen(
    modifier: Modifier = Modifier,
) {
    PlaceholderScreen(
        title = R.string.screen_recent_games,
        designBoard = "Accueil · lien Parties récentes",
        modifier = modifier,
    )
}
