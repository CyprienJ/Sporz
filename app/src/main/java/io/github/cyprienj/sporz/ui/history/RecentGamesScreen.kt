package io.github.cyprienj.sporz.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.os.ConfigurationCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.cyprienj.sporz.R
import io.github.cyprienj.sporz.data.GameSession
import io.github.cyprienj.sporz.data.history.FinishedGame
import io.github.cyprienj.sporz.data.history.GameHistory
import io.github.cyprienj.sporz.engine.GameEnd
import io.github.cyprienj.sporz.engine.game.Phase
import io.github.cyprienj.sporz.ui.components.CardGroup
import io.github.cyprienj.sporz.ui.components.GroupDivider
import io.github.cyprienj.sporz.ui.components.InfoNote
import io.github.cyprienj.sporz.ui.components.ScreenHeader
import io.github.cyprienj.sporz.ui.components.ConfirmDialog
import io.github.cyprienj.sporz.ui.end.GameEndScreen
import io.github.cyprienj.sporz.ui.end.GameEndViewModel
import io.github.cyprienj.sporz.ui.end.titleRes
import io.github.cyprienj.sporz.ui.gm.game.JournalScreen
import io.github.cyprienj.sporz.ui.home.HomeViewModel
import io.github.cyprienj.sporz.ui.home.ResumableGame
import io.github.cyprienj.sporz.ui.home.label
import io.github.cyprienj.sporz.ui.theme.SporzIcons
import io.github.cyprienj.sporz.ui.theme.SporzShapes
import io.github.cyprienj.sporz.ui.theme.SporzTheme
import io.github.cyprienj.sporz.ui.theme.SporzTypography
import io.github.cyprienj.sporz.ui.theme.StandardSurface
import io.github.cyprienj.sporz.ui.theme.Tone
import io.github.cyprienj.sporz.ui.theme.colors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date
import javax.inject.Inject

/** "Parties récentes" (ticket C13): finished games kept on this phone (DEC-12). */
@HiltViewModel
class RecentGamesViewModel @Inject constructor(history: GameHistory) : ViewModel() {
    /** Null while loading. */
    val games: StateFlow<List<FinishedGame>?> = history.games.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}

@Composable
fun RecentGamesRoute(
    onBack: () -> Unit,
    onOpenGame: (Long) -> Unit,
    viewModel: RecentGamesViewModel = hiltViewModel(),
) {
    val games by viewModel.games.collectAsStateWithLifecycle()
    RecentGamesScreen(games, onBack, onOpenGame)
}

@Composable
fun RecentGamesScreen(
    games: List<FinishedGame>?,
    onBack: () -> Unit,
    onOpenGame: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    StandardSurface(modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            ScreenHeader(stringResource(R.string.screen_recent_games), onBack = onBack)
            LazyColumn(
                Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                when {
                    games == null -> Unit
                    games.isEmpty() -> item { InfoNote(stringResource(R.string.recent_empty)) }
                    else -> {
                        item {
                            CardGroup {
                                games.forEachIndexed { index, game ->
                                    if (index > 0) GroupDivider()
                                    GameRow(game) { onOpenGame(game.id) }
                                }
                            }
                        }
                        item {
                            Text(stringResource(R.string.recent_kept_locally), style = SporzTypography.small, color = SporzTheme.colors.textMuted)
                        }
                    }
                }
            }
            Box(Modifier.windowInsetsPadding(WindowInsets.navigationBars))
        }
    }
}

@Composable
private fun GameRow(finished: FinishedGame, onClick: () -> Unit) {
    val colors = SporzTheme.colors
    val game = finished.game
    val end = game.end ?: GameEnd.Aborted
    val dot = when (end) {
        GameEnd.HealthyWin -> colors.healthy.strong
        GameEnd.MutantsWin -> colors.mutant.strong
        GameEnd.NoSurvivors, GameEnd.Aborted -> colors.neutral.strong
    }
    val phase = stringResource(
        if (GameEndViewModel.endedIn(game) == Phase.Night) R.string.night_title else R.string.day_title,
        game.round,
    )
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(Modifier.size(10.dp).background(dot, SporzShapes.pill))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(stringResource(end.titleRes), style = SporzTypography.body.copy(fontWeight = FontWeight.SemiBold))
            Text(
                pluralStringResource(R.plurals.recent_item_details, game.players.size, formatDate(finished.endedAt), game.players.size, phase),
                style = SporzTypography.small,
                color = colors.textMuted,
            )
        }
        Icon(SporzIcons.ChevronRight, contentDescription = null, tint = colors.textMuted, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun formatDate(epochMillis: Long): String {
    val locale = ConfigurationCompat.getLocales(LocalConfiguration.current)[0] ?: java.util.Locale.getDefault()
    val format = remember(locale) { DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, locale) }
    return format.format(Date(epochMillis))
}

/** One recent game: its end screen and its journal (routes PastGame and PastJournal). */
@HiltViewModel
class PastGameViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val history: GameHistory,
    private val session: GameSession,
) : ViewModel() {

    private val id: Long = checkNotNull(savedStateHandle["id"]) { "No game id" }

    private val _game = MutableStateFlow<FinishedGame?>(null)
    val game: StateFlow<FinishedGame?> = _game.asStateFlow()

    /** The game in progress on this phone, that a replay would stop. */
    val inProgress: StateFlow<ResumableGame?> = combine(session.game, session.announced, HomeViewModel::resumableGame)
        .stateIn(viewModelScope, SharingStarted.Eagerly, HomeViewModel.resumableGame(session.game.value, session.announced.value))

    /** True once loaded and missing (deleted, or saved by an incompatible version). */
    var missing by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            val found = history.get(id)
            _game.value = found
            missing = found == null
        }
    }

    /** New setup with this game's crew; the caller opens the composition screen. */
    fun replay() {
        _game.value?.let { session.replay(it.game) }
    }

    fun delete(onDeleted: () -> Unit) {
        viewModelScope.launch {
            history.delete(id)
            onDeleted()
        }
    }
}

@Composable
fun PastGameRoute(
    onBack: () -> Unit,
    onReplay: () -> Unit,
    onOpenJournal: () -> Unit,
    viewModel: PastGameViewModel = hiltViewModel(),
) {
    val finished by viewModel.game.collectAsStateWithLifecycle()
    if (viewModel.missing) LaunchedEffect(Unit) { onBack() }
    val inProgress by viewModel.inProgress.collectAsStateWithLifecycle()
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    var confirmReplay by rememberSaveable { mutableStateOf(false) }
    val replay = {
        viewModel.replay()
        onReplay()
    }
    GameEndScreen(
        game = finished?.game ?: return,
        // Replaying replaces the game in progress: ask first.
        onReplaySameCrew = { if (inProgress != null) confirmReplay = true else replay() },
        onOpenJournal = onOpenJournal,
        onBackHome = onBack,
        backLabel = stringResource(R.string.action_back),
        onDelete = { confirmDelete = true },
    )
    if (confirmDelete) {
        ConfirmDialog(
            title = stringResource(R.string.recent_delete_title),
            body = stringResource(R.string.recent_delete_body),
            confirmLabel = stringResource(R.string.action_delete),
            onConfirm = {
                confirmDelete = false
                viewModel.delete(onBack)
            },
            onDismiss = { confirmDelete = false },
        )
    }
    val running = inProgress
    if (confirmReplay && running != null) {
        ConfirmDialog(
            title = stringResource(R.string.confirm_new_game_title),
            body = stringResource(if (running.played) R.string.confirm_new_game_body else R.string.confirm_new_game_body_unplayed, running.label()),
            confirmLabel = stringResource(R.string.action_stop_and_replay),
            onConfirm = {
                confirmReplay = false
                replay()
            },
            onDismiss = { confirmReplay = false },
        )
    }
}

@Composable
fun PastJournalRoute(onBack: () -> Unit, viewModel: PastGameViewModel = hiltViewModel()) {
    val finished by viewModel.game.collectAsStateWithLifecycle()
    if (viewModel.missing) LaunchedEffect(Unit) { onBack() }
    JournalScreen(finished?.game ?: return, onBackToPhase = onBack, onOpenPlayerBoard = null)
}

@Preview(name = "Empty")
@Composable
private fun RecentGamesEmptyPreview() {
    SporzTheme(darkTheme = false) { RecentGamesScreen(emptyList(), {}, {}) }
}
