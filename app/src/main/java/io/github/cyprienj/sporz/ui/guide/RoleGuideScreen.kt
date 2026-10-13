package io.github.cyprienj.sporz.ui.guide

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.cyprienj.sporz.R
import io.github.cyprienj.sporz.ui.common.PlaceholderScreen

@Composable
fun RoleGuideScreen(
    modifier: Modifier = Modifier,
) {
    PlaceholderScreen(
        title = R.string.screen_role_guide,
        designBoard = "Guide des rôles",
        modifier = modifier,
    )
}
