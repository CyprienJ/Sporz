package io.github.cyprienj.sporz.ui.gm.setup

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.cyprienj.sporz.R
import io.github.cyprienj.sporz.ui.common.PlaceholderAction
import io.github.cyprienj.sporz.ui.common.PlaceholderScreen

@Composable
fun CompositionScreen(
    onDeal: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PlaceholderScreen(
        title = R.string.screen_composition,
        designBoard = "MJ · Composition des rôles",
        modifier = modifier,
        actions = listOf(
            PlaceholderAction(R.string.action_deal, onDeal),
        ),
    )
}
