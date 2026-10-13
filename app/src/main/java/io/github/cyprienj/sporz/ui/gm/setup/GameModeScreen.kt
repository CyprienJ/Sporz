package io.github.cyprienj.sporz.ui.gm.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.cyprienj.sporz.R
import io.github.cyprienj.sporz.engine.model.GameMode
import io.github.cyprienj.sporz.ui.components.InfoNote
import io.github.cyprienj.sporz.ui.components.RadioCard
import io.github.cyprienj.sporz.ui.components.ScreenHeader
import io.github.cyprienj.sporz.ui.components.SporzButton
import io.github.cyprienj.sporz.ui.theme.SporzTheme
import io.github.cyprienj.sporz.ui.theme.SporzTypography

/** design.html, board "MJ · Mode de partie". */
@Composable
fun GameModeRoute(
    onBack: () -> Unit,
    onOpenLobby: () -> Unit,
    viewModel: GameModeViewModel = hiltViewModel(),
) {
    val selected by viewModel.selected.collectAsStateWithLifecycle()
    GameModeScreen(
        selected = selected,
        availableModes = viewModel.availableModes,
        onSelect = viewModel::select,
        onBack = onBack,
        onOpenLobby = {
            viewModel.confirm()
            onOpenLobby()
        },
    )
}

@Composable
fun GameModeScreen(
    selected: GameMode,
    availableModes: Set<GameMode>,
    onSelect: (GameMode) -> Unit,
    onBack: () -> Unit,
    onOpenLobby: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .fillMaxSize()
            .background(SporzTheme.colors.background),
    ) {
        ScreenHeader(
            title = stringResource(R.string.mode_title),
            overline = stringResource(R.string.mode_step),
            onBack = onBack,
            titleStyle = SporzTypography.title.copy(fontSize = 40.sp, lineHeight = 40.sp),
            divider = false,
        )
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                GameMode.entries.forEach { mode ->
                    val enabled = mode in availableModes
                    RadioCard(
                        title = stringResource(mode.titleRes),
                        description = stringResource(mode.description),
                        selected = mode == selected,
                        onClick = { onSelect(mode) },
                        enabled = enabled,
                        tag = if (enabled) null else stringResource(R.string.mode_coming_soon),
                    )
                }
            }
            InfoNote(stringResource(R.string.mode_gm_note))
        }
        SporzButton(
            text = stringResource(R.string.action_open_lobby),
            onClick = onOpenLobby,
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 20.dp),
        )
    }
}

private val GameMode.description: Int
    get() = when (this) {
        GameMode.Offline -> R.string.mode_offline_description
        GameMode.SemiOnline -> R.string.mode_semi_online_description
        GameMode.Online -> R.string.mode_online_description
    }

@Preview(name = "Light")
@Composable
private fun GameModeLightPreview() {
    SporzTheme(darkTheme = false) { GameModeScreen(GameMode.Offline, setOf(GameMode.Offline), {}, {}, {}) }
}

@Preview(name = "Dark, all modes available")
@Composable
private fun GameModeDarkPreview() {
    SporzTheme(darkTheme = true) { GameModeScreen(GameMode.SemiOnline, GameMode.entries.toSet(), {}, {}, {}) }
}
