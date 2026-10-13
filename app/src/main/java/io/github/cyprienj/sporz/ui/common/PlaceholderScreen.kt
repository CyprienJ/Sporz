package io.github.cyprienj.sporz.ui.common

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.cyprienj.sporz.R

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
    Scaffold(modifier = modifier) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = stringResource(title), style = MaterialTheme.typography.headlineMedium)
            Text(
                text = stringResource(R.string.placeholder_design_board, designBoard),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            actions.forEach { action ->
                OutlinedButton(onClick = action.onClick, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(action.label))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PlaceholderScreenPreview() {
    MaterialTheme {
        PlaceholderScreen(
            title = R.string.screen_lobby,
            designBoard = "MJ · Lobby",
            actions = listOf(PlaceholderAction(R.string.action_choose_roles) {}),
        )
    }
}
