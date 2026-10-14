package io.github.cyprienj.sporz.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.cyprienj.sporz.R
import io.github.cyprienj.sporz.ui.components.ActionCard
import io.github.cyprienj.sporz.ui.components.ConfirmDialog
import io.github.cyprienj.sporz.ui.components.TextLink
import io.github.cyprienj.sporz.ui.theme.LightStatusBarIcons
import io.github.cyprienj.sporz.ui.theme.SecretSurface
import io.github.cyprienj.sporz.ui.theme.SporzIcons
import io.github.cyprienj.sporz.ui.theme.SporzShapes
import io.github.cyprienj.sporz.ui.theme.SporzTheme
import io.github.cyprienj.sporz.ui.theme.SporzTypography
import io.github.cyprienj.sporz.ui.theme.StandardSurface

/** design.html, board "Accueil". */
@Composable
fun HomeRoute(
    onCreateGame: () -> Unit,
    onJoinGame: () -> Unit,
    onOpenRoleGuide: () -> Unit,
    onOpenRecentGames: () -> Unit,
    onOpenDesignCatalog: (() -> Unit)?,
    onResume: (ResumeTarget) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val resumable by viewModel.resumable.collectAsStateWithLifecycle()
    var confirmNewGame by rememberSaveable { mutableStateOf(false) }
    val createGame = {
        viewModel.createGame()
        onCreateGame()
    }
    HomeScreen(
        resumable = resumable,
        onResume = { resumable?.let { onResume(it.target) } },
        // A new game stops the one in progress: ask first.
        onCreateGame = { if (resumable != null) confirmNewGame = true else createGame() },
        onJoinGame = onJoinGame,
        onOpenRoleGuide = onOpenRoleGuide,
        onOpenRecentGames = onOpenRecentGames,
        onOpenDesignCatalog = onOpenDesignCatalog,
    )
    val running = resumable
    if (confirmNewGame && running != null) {
        ConfirmDialog(
            title = stringResource(R.string.confirm_new_game_title),
            body = stringResource(if (running.played) R.string.confirm_new_game_body else R.string.confirm_new_game_body_unplayed, running.label()),
            confirmLabel = stringResource(R.string.action_stop_and_create),
            onConfirm = {
                confirmNewGame = false
                createGame()
            },
            onDismiss = { confirmNewGame = false },
        )
    }
}

/** "Nuit 2", "Jour 1" or "Distribution". */
@Composable
fun ResumableGame.label(): String = when (target) {
    ResumeTarget.Distribution -> stringResource(R.string.distribution_title)
    ResumeTarget.Night -> stringResource(R.string.night_title, round)
    ResumeTarget.Day -> stringResource(R.string.day_title, round)
}

@Composable
fun HomeScreen(
    resumable: ResumableGame?,
    onResume: () -> Unit,
    onCreateGame: () -> Unit,
    onJoinGame: () -> Unit,
    onOpenRoleGuide: () -> Unit,
    onOpenRecentGames: () -> Unit,
    onOpenDesignCatalog: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    LightStatusBarIcons()
    SecretSurface(modifier.fillMaxSize()) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            // With large fonts the actions may not fit: they scroll, leaving room for the brand.
            val actionsMaxHeight = maxHeight - 140.dp
            Column(Modifier.fillMaxSize()) {
                Brand(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                )
                Actions(
                    resumable, onResume, onCreateGame, onJoinGame, onOpenRoleGuide, onOpenRecentGames, onOpenDesignCatalog,
                    Modifier.heightIn(max = actionsMaxHeight),
                )
            }
        }
    }
}

@Composable
private fun Brand(modifier: Modifier = Modifier) {
    val colors = SporzTheme.colors
    Column(
        modifier
            .windowInsetsPadding(WindowInsets.statusBars)
            .verticalScroll(rememberScrollState(), reverseScrolling = true)
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.Bottom),
    ) {
        Icon(SporzIcons.Logo, contentDescription = null, tint = colors.accent, modifier = Modifier.size(64.dp))
        Text(
            stringResource(R.string.app_name).uppercase(),
            style = SporzTypography.display,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            stringResource(R.string.home_tagline),
            style = SporzTypography.bodyLarge.copy(fontSize = 17.sp, lineHeight = 25.sp),
            color = colors.textMuted,
            modifier = Modifier.widthIn(max = 300.dp),
        )
    }
}

@Composable
private fun Actions(
    resumable: ResumableGame?,
    onResume: () -> Unit,
    onCreateGame: () -> Unit,
    onJoinGame: () -> Unit,
    onOpenRoleGuide: () -> Unit,
    onOpenRecentGames: () -> Unit,
    onOpenDesignCatalog: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    StandardSurface(modifier.fillMaxWidth(), shape = SporzShapes.sheet) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (resumable != null) {
                // Not in design.html: a game in progress comes first (ticket C13).
                val phase = resumable.label()
                ActionCard(
                    title = stringResource(R.string.action_resume_game),
                    subtitle = pluralStringResource(R.plurals.home_resume_subtitle, resumable.players, phase, resumable.players),
                    icon = if (resumable.target == ResumeTarget.Day) SporzIcons.Day else SporzIcons.Night,
                    onClick = onResume,
                    primary = true,
                )
            }
            ActionCard(
                title = stringResource(R.string.action_create_game),
                subtitle = stringResource(R.string.home_create_subtitle),
                icon = SporzIcons.Plus,
                onClick = onCreateGame,
                primary = resumable == null,
            )
            ActionCard(
                title = stringResource(R.string.action_join_game),
                subtitle = stringResource(R.string.home_join_subtitle),
                icon = SporzIcons.Join,
                onClick = onJoinGame,
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextLink(stringResource(R.string.action_rules_and_roles), onOpenRoleGuide)
                TextLink(stringResource(R.string.screen_recent_games), onOpenRecentGames)
            }
            if (onOpenDesignCatalog != null) {
                TextLink(
                    stringResource(R.string.screen_design_catalog),
                    onOpenDesignCatalog,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }
        }
    }
}

@Preview(name = "Light")
@Composable
private fun HomeLightPreview() {
    SporzTheme(darkTheme = false) { HomeScreen(null, {}, {}, {}, {}, {}, onOpenDesignCatalog = null) }
}

@Preview(name = "Dark")
@Composable
private fun HomeDarkPreview() {
    SporzTheme(darkTheme = true) { HomeScreen(ResumableGame(ResumeTarget.Night, 2, 9), {}, {}, {}, {}, {}, onOpenDesignCatalog = {}) }
}
