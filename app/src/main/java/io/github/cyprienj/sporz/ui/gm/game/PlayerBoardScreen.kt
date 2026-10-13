package io.github.cyprienj.sporz.ui.gm.game

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.cyprienj.sporz.R
import io.github.cyprienj.sporz.engine.game.GameState
import io.github.cyprienj.sporz.engine.game.Phase
import io.github.cyprienj.sporz.engine.model.Camp
import io.github.cyprienj.sporz.engine.model.Player
import io.github.cyprienj.sporz.ui.common.nameRes
import io.github.cyprienj.sporz.ui.components.CampBar
import io.github.cyprienj.sporz.ui.components.CardGroup
import io.github.cyprienj.sporz.ui.components.FilterChip
import io.github.cyprienj.sporz.ui.components.GroupDivider
import io.github.cyprienj.sporz.ui.components.PlayerRow
import io.github.cyprienj.sporz.ui.components.SporzTabBar
import io.github.cyprienj.sporz.ui.components.StatTile
import io.github.cyprienj.sporz.ui.components.StatusBadge
import io.github.cyprienj.sporz.ui.components.TabItem
import io.github.cyprienj.sporz.ui.theme.LightStatusBarIcons
import io.github.cyprienj.sporz.ui.theme.SecretSurface
import io.github.cyprienj.sporz.ui.theme.SporzIcons
import io.github.cyprienj.sporz.ui.theme.SporzTheme
import io.github.cyprienj.sporz.ui.theme.SporzTypography
import io.github.cyprienj.sporz.ui.theme.Tone
import io.github.cyprienj.sporz.ui.theme.colors

/** design.html, board "MJ · Tableau des joueurs". */
@Composable
fun PlayerBoardRoute(
    onBackToPhase: () -> Unit,
    onOpenJournal: () -> Unit,
    viewModel: PlayerBoardViewModel = hiltViewModel(),
) {
    val game by viewModel.game.collectAsStateWithLifecycle()
    PlayerBoardScreen(
        game = game ?: return,
        filters = viewModel.filters,
        filter = viewModel.filter,
        onFilter = viewModel::select,
        onBackToPhase = onBackToPhase,
        onOpenJournal = onOpenJournal,
    )
}

@Composable
fun PlayerBoardScreen(
    game: GameState,
    filters: List<BoardFilter>,
    filter: BoardFilter,
    onFilter: (BoardFilter) -> Unit,
    onBackToPhase: () -> Unit,
    onOpenJournal: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LightStatusBarIcons()
    val ejected = PlayerBoardViewModel.ejected(game)
    val rows = PlayerBoardViewModel.filtered(game.players, filter)
    Column(
        modifier
            .fillMaxSize()
            .background(SporzTheme.colors.background),
    ) {
        SecretSurface(Modifier.fillMaxWidth()) { Header(game) }
        Row(
            Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            filters.forEach { f -> FilterChip(stringResource(f.labelRes), f == filter, { onFilter(f) }) }
        }
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 12.dp)) {
            item {
                if (rows.isEmpty()) {
                    Text(stringResource(R.string.board_empty), style = SporzTypography.body, color = SporzTheme.colors.textMuted)
                } else {
                    CardGroup {
                        rows.forEachIndexed { index, player ->
                            if (index > 0) GroupDivider()
                            BoardRow(player, player.id in ejected)
                        }
                    }
                }
            }
        }
        SporzTabBar(
            items = listOf(
                TabItem(stringResource(R.string.tab_phase), if (game.phase == Phase.Night) SporzIcons.Night else SporzIcons.Day),
                TabItem(stringResource(R.string.tab_players), SporzIcons.Players),
                TabItem(stringResource(R.string.tab_journal), SporzIcons.Journal),
            ),
            selectedIndex = 1,
            onSelect = { index ->
                when (index) {
                    0 -> onBackToPhase()
                    2 -> onOpenJournal()
                }
            },
        )
    }
}

@Composable
private fun Header(game: GameState) {
    val alive = game.players.filter { it.alive }
    val healthy = alive.count { it.camp == Camp.Healthy }
    val mutant = alive.count { it.camp == Camp.Mutant }
    val eliminated = game.players.size - alive.size
    Column(
        Modifier
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.board_title).uppercase(),
                style = SporzTypography.titleMedium,
                modifier = Modifier
                    .weight(1f)
                    .semantics { heading() },
            )
            Text(
                stringResource(if (game.phase == Phase.Night) R.string.night_title else R.string.day_title, game.round),
                style = SporzTypography.small,
                color = SporzTheme.colors.textMuted,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatTile("$healthy", pluralStringResource(R.plurals.board_stat_healthy, healthy), Modifier.weight(1f), valueColor = Tone.Healthy.colors.content)
            StatTile("$mutant", stringResource(R.string.board_stat_mutant), Modifier.weight(1f), valueColor = Tone.Mutant.colors.content)
            StatTile("$eliminated", pluralStringResource(R.plurals.board_stat_eliminated, eliminated), Modifier.weight(1f))
        }
    }
}

/** Camp bar (red for the mutant camp, traitor included), role and genome, state badge. */
@Composable
private fun BoardRow(player: Player, wasEjected: Boolean) {
    val colors = SporzTheme.colors
    val (badge, tone) = when {
        !player.alive -> (if (wasEjected) R.string.badge_ejected else R.string.badge_dead) to Tone.Neutral
        player.isMutated -> R.string.badge_mutated to Tone.Mutant
        else -> R.string.badge_healthy to Tone.Healthy
    }
    val bar = when {
        !player.alive -> colors.neutral.strong
        player.camp == Camp.Mutant -> colors.mutant.strong
        else -> colors.healthy.strong
    }
    PlayerRow(
        name = player.name,
        subtitle = stringResource(R.string.board_role_genome, stringResource(player.role.nameRes), stringResource(player.genome.nameRes)),
        eliminated = !player.alive,
        leading = { CampBar(bar) },
        trailing = { StatusBadge(stringResource(badge), tone) },
    )
}

private val BoardFilter.labelRes: Int
    get() = when (this) {
        BoardFilter.All -> R.string.board_filter_all
        BoardFilter.Alive -> R.string.board_filter_alive
        BoardFilter.Mutated -> R.string.board_filter_mutated
        BoardFilter.Offline -> R.string.board_filter_offline
        BoardFilter.Eliminated -> R.string.board_filter_eliminated
    }
