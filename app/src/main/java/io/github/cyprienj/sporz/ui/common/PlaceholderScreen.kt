package io.github.cyprienj.sporz.ui.common

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.cyprienj.sporz.R
import io.github.cyprienj.sporz.ui.components.ScreenHeader
import io.github.cyprienj.sporz.ui.components.SporzButton
import io.github.cyprienj.sporz.ui.components.SporzButtonStyle
import io.github.cyprienj.sporz.ui.theme.SporzTheme

data class PlaceholderAction(@param:StringRes val label: Int, val onClick: () -> Unit)

/**
 * Temporary screen used until the real one is built (tickets C* and E*).
 * Shows the screen title, the matching board of design.html, and buttons
 * for the navigation flows leaving this screen.
 */
@Composable
fun PlaceholderScreen(
    @StringRes title: Int,
    designBoard: String,
    modifier: Modifier = Modifier,
    actions: List<PlaceholderAction> = emptyList(),
) {
    Column(
        modifier
            .fillMaxSize()
            .background(SporzTheme.colors.background),
    ) {
        ScreenHeader(
            title = stringResource(title),
            overline = stringResource(R.string.placeholder_design_board, designBoard),
        )
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            actions.forEach { action ->
                SporzButton(
                    text = stringResource(action.label),
                    onClick = action.onClick,
                    style = SporzButtonStyle.Secondary,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PlaceholderScreenPreview() {
    SporzTheme {
        PlaceholderScreen(
            title = R.string.screen_lobby,
            designBoard = "MJ · Lobby",
            actions = listOf(PlaceholderAction(R.string.action_choose_roles) {}),
        )
    }
}
