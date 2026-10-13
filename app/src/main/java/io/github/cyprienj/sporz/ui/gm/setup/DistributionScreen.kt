package io.github.cyprienj.sporz.ui.gm.setup

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.cyprienj.sporz.R
import io.github.cyprienj.sporz.ui.common.PlaceholderAction
import io.github.cyprienj.sporz.ui.common.PlaceholderScreen

@Composable
fun DistributionScreen(
    onStartFirstNight: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PlaceholderScreen(
        title = R.string.screen_distribution,
        designBoard = "MJ · Distribution des rôles",
        modifier = modifier,
        actions = listOf(
            PlaceholderAction(R.string.action_start_first_night, onStartFirstNight),
        ),
    )
}
