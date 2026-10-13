package io.github.cyprienj.sporz.ui.end

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.cyprienj.sporz.R
import io.github.cyprienj.sporz.engine.GameEnd
import io.github.cyprienj.sporz.engine.game.GameState
import io.github.cyprienj.sporz.engine.game.Phase
import io.github.cyprienj.sporz.engine.model.Camp
import io.github.cyprienj.sporz.engine.model.Genome
import io.github.cyprienj.sporz.engine.model.Player
import io.github.cyprienj.sporz.ui.common.nameRes
import io.github.cyprienj.sporz.ui.components.CardGroup
import io.github.cyprienj.sporz.ui.components.GroupDivider
import io.github.cyprienj.sporz.ui.components.Overline
import io.github.cyprienj.sporz.ui.components.SectionTitle
import io.github.cyprienj.sporz.ui.components.SporzButton
import io.github.cyprienj.sporz.ui.components.TextLink
import io.github.cyprienj.sporz.ui.theme.LightStatusBarIcons
import io.github.cyprienj.sporz.ui.theme.SporzPalette
import io.github.cyprienj.sporz.ui.theme.SporzShapes
import io.github.cyprienj.sporz.ui.theme.SporzTheme
import io.github.cyprienj.sporz.ui.theme.SporzTypography
import io.github.cyprienj.sporz.ui.theme.Tone
import io.github.cyprienj.sporz.ui.theme.colors

/** design.html, board "Fin de partie". */
@Composable
fun GameEndRoute(
    onReplaySameCrew: () -> Unit,
    onOpenJournal: () -> Unit,
    onBackHome: () -> Unit,
    viewModel: GameEndViewModel = hiltViewModel(),
) {
    val game by viewModel.game.collectAsStateWithLifecycle()
    GameEndScreen(
        game = game ?: return,
        onReplaySameCrew = {
            viewModel.replaySameCrew()
            onReplaySameCrew()
        },
        onOpenJournal = onOpenJournal,
        onBackHome = onBackHome,
    )
}

@Composable
fun GameEndScreen(
    game: GameState,
    onReplaySameCrew: () -> Unit,
    onOpenJournal: () -> Unit,
    onBackHome: () -> Unit,
    modifier: Modifier = Modifier,
    backLabel: String = stringResource(R.string.action_back_home),
    /** A recent game (ticket C13) can be deleted; the game just finished cannot. */
    onDelete: (() -> Unit)? = null,
) {
    LightStatusBarIcons()
    val fates = GameEndViewModel.fates(game)
    Column(
        modifier
            .fillMaxSize()
            .background(SporzTheme.colors.background),
    ) {
        Banner(game)
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SectionTitle(stringResource(R.string.end_revelations))
            CardGroup {
                game.players.forEachIndexed { index, player ->
                    if (index > 0) GroupDivider()
                    Revelation(player, fates.getValue(player.id))
                }
            }
        }
        Column(Modifier.background(SporzTheme.colors.background)) {
            HorizontalDivider(color = SporzTheme.colors.cardBorder)
            Column(
                Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                SporzButton(stringResource(R.string.action_replay_same_crew), onReplaySameCrew, modifier = Modifier.fillMaxWidth())
                Row {
                    TextLink(stringResource(R.string.action_open_full_journal), onOpenJournal)
                    TextLink(backLabel, onBackHome, Modifier.padding(start = 16.dp))
                }
                if (onDelete != null) TextLink(stringResource(R.string.action_delete_game), onDelete)
            }
        }
    }
}

/** Winner banner: teal (healthy), red (mutants) or neutral; deep tint in the dark theme. */
@Composable
private fun Banner(game: GameState) {
    val end = game.end ?: GameEnd.Aborted
    val tone = when (end) {
        GameEnd.HealthyWin -> Tone.Healthy
        GameEnd.MutantsWin -> Tone.Mutant
        GameEnd.NoSurvivors, GameEnd.Aborted -> Tone.Neutral
    }.colors
    val dark = SporzTheme.isDark
    val background = if (dark) tone.background else if (end == GameEnd.NoSurvivors || end == GameEnd.Aborted) SporzPalette.Navy else tone.strong
    val content = if (dark) tone.content else SporzPalette.White
    val phase = stringResource(
        if (GameEndViewModel.endedIn(game) == Phase.Night) R.string.night_title else R.string.day_title,
        game.round,
    )
    Column(
        Modifier
            .fillMaxWidth()
            .background(background)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 20.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Overline(stringResource(R.string.end_overline, phase), color = content.copy(alpha = 0.8f))
        Text(
            stringResource(end.titleRes).uppercase(),
            style = SporzTypography.titleLarge.copy(fontSize = 48.sp, lineHeight = 46.sp),
            color = content,
            modifier = Modifier.semantics { heading() },
        )
        Text(stringResource(end.reasonRes), style = SporzTypography.body, color = content.copy(alpha = 0.9f))
    }
}

@Composable
private fun Revelation(player: Player, fate: Fate) {
    val colors = SporzTheme.colors
    val inMutantCamp = player.camp == Camp.Mutant
    Row(
        Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(Modifier.size(8.dp).background(if (inMutantCamp) colors.mutant.strong else colors.healthy.strong, SporzShapes.pill))
        Text(player.name, style = SporzTypography.body.copy(fontWeight = FontWeight.SemiBold), modifier = Modifier.width(90.dp), maxLines = 1)
        val role = stringResource(player.role.nameRes)
        Text(
            if (player.genome == Genome.Normal) role else stringResource(R.string.end_role_genome, role, stringResource(player.genome.nameRes)),
            style = SporzTypography.label.copy(fontWeight = FontWeight.Normal),
            color = colors.textBody,
            modifier = Modifier.weight(1f),
        )
        Text(fate.text(), style = SporzTypography.small, color = colors.textMuted)
    }
}

@Composable
private fun Fate.text(): String = when (this) {
    Fate.Alive -> stringResource(R.string.fate_alive)
    is Fate.Ejected -> stringResource(R.string.fate_ejected, day)
    is Fate.Dead -> stringResource(R.string.fate_dead, night)
    is Fate.Healed -> stringResource(R.string.fate_healed, night)
}

internal val GameEnd.titleRes: Int
    get() = when (this) {
        GameEnd.HealthyWin -> R.string.journal_end_healthy
        GameEnd.MutantsWin -> R.string.journal_end_mutants
        GameEnd.NoSurvivors -> R.string.journal_end_none
        GameEnd.Aborted -> R.string.journal_end_aborted
    }

private val GameEnd.reasonRes: Int
    get() = when (this) {
        GameEnd.HealthyWin -> R.string.end_reason_healthy
        GameEnd.MutantsWin -> R.string.end_reason_mutants
        GameEnd.NoSurvivors -> R.string.end_reason_none
        GameEnd.Aborted -> R.string.end_reason_aborted
    }
