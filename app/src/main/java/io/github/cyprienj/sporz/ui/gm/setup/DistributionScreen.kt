package io.github.cyprienj.sporz.ui.gm.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.cyprienj.sporz.R
import io.github.cyprienj.sporz.engine.model.Genome
import io.github.cyprienj.sporz.engine.model.Player
import io.github.cyprienj.sporz.engine.model.PlayerId
import io.github.cyprienj.sporz.engine.model.PlayerState
import io.github.cyprienj.sporz.engine.model.Role
import io.github.cyprienj.sporz.ui.common.nameRes
import io.github.cyprienj.sporz.ui.components.Pill
import io.github.cyprienj.sporz.ui.components.PillStyle
import io.github.cyprienj.sporz.ui.components.ProgressBar
import io.github.cyprienj.sporz.ui.components.SectionTitle
import io.github.cyprienj.sporz.ui.components.SporzButton
import io.github.cyprienj.sporz.ui.components.SporzButtonStyle
import io.github.cyprienj.sporz.ui.components.SporzCard
import io.github.cyprienj.sporz.ui.theme.LightStatusBarIcons
import io.github.cyprienj.sporz.ui.theme.SecretSurface
import io.github.cyprienj.sporz.ui.theme.SporzIcons
import io.github.cyprienj.sporz.ui.theme.SporzTheme
import io.github.cyprienj.sporz.ui.theme.SporzTypography
import io.github.cyprienj.sporz.ui.theme.Tone

/** design.html, board "MJ · Distribution des rôles". */
@Composable
fun DistributionRoute(
    onStartFirstNight: () -> Unit,
    viewModel: DistributionViewModel = hiltViewModel(),
) {
    val game by viewModel.game.collectAsStateWithLifecycle()
    val announced by viewModel.announced.collectAsStateWithLifecycle()
    DistributionScreen(
        players = game?.players.orEmpty(),
        announced = announced,
        revealed = viewModel.revealed,
        onReveal = viewModel::reveal,
        onHide = viewModel::hide,
        onConfirm = viewModel::confirmAnnounced,
        canStartNight = viewModel.canStartNight(),
        onStartFirstNight = onStartFirstNight,
    )
}

@Composable
fun DistributionScreen(
    players: List<Player>,
    announced: Set<PlayerId>,
    revealed: PlayerId?,
    onReveal: (PlayerId) -> Unit,
    onHide: () -> Unit,
    onConfirm: (PlayerId) -> Unit,
    canStartNight: Boolean,
    onStartFirstNight: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LightStatusBarIcons()
    val colors = SporzTheme.colors
    val done = players.count { it.id in announced }
    Column(
        modifier
            .fillMaxSize()
            .background(colors.background),
    ) {
        SecretSurface(Modifier.fillMaxWidth()) {
            Column(
                Modifier
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    stringResource(R.string.distribution_title).uppercase(),
                    style = SporzTypography.titleMedium,
                    modifier = Modifier.semantics { heading() },
                )
                Row {
                    Text(
                        pluralStringResource(R.plurals.distribution_progress, players.size, done, players.size),
                        style = SporzTypography.label.copy(fontWeight = FontWeight.Normal),
                        color = SporzTheme.colors.textMuted,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        stringResource(R.string.distribution_dealt),
                        style = SporzTypography.label.copy(fontWeight = FontWeight.Normal),
                        color = SporzTheme.colors.textMuted,
                    )
                }
                ProgressBar(if (players.isEmpty()) 0f else done.toFloat() / players.size)
            }
        }
        LazyColumn(
            Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (players.isEmpty()) {
                item { Text(stringResource(R.string.distribution_no_game), style = SporzTypography.body, color = colors.textMuted) }
            } else {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        SectionTitle(stringResource(R.string.distribution_offline))
                        Text(stringResource(R.string.distribution_offline_hint), style = SporzTypography.label.copy(fontWeight = FontWeight.Normal), color = colors.textMuted)
                    }
                }
                items(players, key = { it.id.value }) { player ->
                    PlayerCard(
                        player = player,
                        isAnnounced = player.id in announced,
                        isRevealed = player.id == revealed,
                        onReveal = { onReveal(player.id) },
                        onHide = onHide,
                        onConfirm = { onConfirm(player.id) },
                    )
                }
            }
        }
        Column(Modifier.background(colors.background)) {
            HorizontalDivider(color = colors.cardBorder)
            Column(
                Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (!canStartNight && players.isNotEmpty()) {
                    Text(stringResource(R.string.distribution_all_needed), style = SporzTypography.small, color = colors.textMuted)
                }
                SporzButton(
                    text = stringResource(R.string.action_start_first_night),
                    onClick = onStartFirstNight,
                    enabled = canStartNight,
                    icon = SporzIcons.Night,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun PlayerCard(
    player: Player,
    isAnnounced: Boolean,
    isRevealed: Boolean,
    onReveal: () -> Unit,
    onHide: () -> Unit,
    onConfirm: () -> Unit,
) {
    val colors = SporzTheme.colors
    SporzCard(highlighted = isRevealed, contentPadding = PaddingValues(start = 14.dp, top = 12.dp, end = 12.dp, bottom = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(player.name.uppercase(), style = SporzTypography.number, modifier = Modifier.weight(1f))
            if (isAnnounced) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(SporzIcons.Check, contentDescription = null, tint = colors.healthy.strong, modifier = Modifier.size(16.dp))
                    Text(
                        stringResource(R.string.distribution_announced),
                        style = SporzTypography.label.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.healthy.strong,
                    )
                }
            }
            when {
                // An announced role can be shown again until the game starts (a player forgot it).
                isAnnounced && !isRevealed -> SporzButton(
                    stringResource(R.string.action_reveal_again),
                    onReveal,
                    style = SporzButtonStyle.Secondary,
                    height = 44.dp,
                    contentDescription = stringResource(R.string.distribution_reveal_again_description, player.name),
                )
                !isRevealed -> SporzButton(
                    stringResource(R.string.action_reveal),
                    onReveal,
                    style = SporzButtonStyle.Inverse,
                    height = 44.dp,
                    contentDescription = stringResource(R.string.distribution_reveal_description, player.name),
                )
            }
        }
        if (isRevealed) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Pill(stringResource(player.role.nameRes))
                Pill(stringResource(R.string.distribution_genome, stringResource(player.genome.nameRes)), style = PillStyle.Outlined)
                // The state: the traitor is healthy, their role says whom they play for (R-SET-12).
                Pill(
                    stringResource(if (player.isMutated) R.string.camp_mutant else R.string.camp_healthy),
                    tone = if (player.isMutated) Tone.Mutant else Tone.Healthy,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SporzButton(
                    stringResource(R.string.action_hide),
                    onHide,
                    style = SporzButtonStyle.Secondary,
                    height = 44.dp,
                    modifier = if (isAnnounced) Modifier.weight(1f) else Modifier,
                )
                if (!isAnnounced) {
                    SporzButton(
                        stringResource(R.string.action_confirm_announced),
                        onConfirm,
                        style = SporzButtonStyle.Positive,
                        height = 44.dp,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

private val previewPlayers = listOf(
    Player(PlayerId("b"), "Bastien", Role.Doctor, Genome.Normal, PlayerState.Healthy),
    Player(PlayerId("g"), "Gaëlle", Role.BaseMutant, Genome.Host, PlayerState.Mutated),
    Player(PlayerId("h"), "Hugo", Role.Astronaut, Genome.Normal, PlayerState.Healthy),
)

@Preview(name = "Light, design state")
@Composable
private fun DistributionLightPreview() {
    SporzTheme(darkTheme = false) {
        DistributionScreen(previewPlayers, setOf(PlayerId("b")), PlayerId("g"), {}, {}, {}, false, {})
    }
}
