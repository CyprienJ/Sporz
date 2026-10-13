package io.github.cyprienj.sporz.ui.gm.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.cyprienj.sporz.R
import io.github.cyprienj.sporz.engine.game.GameState
import io.github.cyprienj.sporz.engine.game.JournalEvent
import io.github.cyprienj.sporz.engine.game.Phase
import io.github.cyprienj.sporz.engine.model.GameMode
import io.github.cyprienj.sporz.engine.model.NightStep
import io.github.cyprienj.sporz.engine.model.PlayerId
import io.github.cyprienj.sporz.engine.night.NightEvent
import io.github.cyprienj.sporz.engine.night.computerScientistReport
import io.github.cyprienj.sporz.ui.common.callRes
import io.github.cyprienj.sporz.ui.common.hackPendingText
import io.github.cyprienj.sporz.ui.common.nameRes
import io.github.cyprienj.sporz.ui.common.nightEventText
import io.github.cyprienj.sporz.ui.common.reportText
import io.github.cyprienj.sporz.ui.components.InfoNote
import io.github.cyprienj.sporz.ui.components.Pill
import io.github.cyprienj.sporz.ui.components.SegmentedControl
import io.github.cyprienj.sporz.ui.components.SporzButton
import io.github.cyprienj.sporz.ui.components.SporzCard
import io.github.cyprienj.sporz.ui.components.SporzTabBar
import io.github.cyprienj.sporz.ui.components.StepIndicator
import io.github.cyprienj.sporz.ui.components.StepState
import io.github.cyprienj.sporz.ui.components.TabItem
import io.github.cyprienj.sporz.ui.components.TargetChip
import io.github.cyprienj.sporz.ui.gm.setup.titleRes
import io.github.cyprienj.sporz.ui.theme.LightStatusBarIcons
import io.github.cyprienj.sporz.ui.theme.SecretSurface
import io.github.cyprienj.sporz.ui.theme.SporzIcons
import io.github.cyprienj.sporz.ui.theme.SporzTheme
import io.github.cyprienj.sporz.ui.theme.SporzTypography
import io.github.cyprienj.sporz.ui.theme.Tone

/** design.html, board "MJ · Nuit". */
@Composable
fun NightRoute(
    onEndNight: () -> Unit,
    onOpenPlayerBoard: () -> Unit,
    onOpenJournal: () -> Unit,
    viewModel: NightViewModel = hiltViewModel(),
) {
    val game by viewModel.game.collectAsStateWithLifecycle()
    val current = game ?: return
    // Leave for the day when the last step is validated here (not when arriving after the night).
    val arrivedDuringNight = remember { current.phase == Phase.Night }
    LaunchedEffect(current.phase) {
        if (arrivedDuringNight && current.phase != Phase.Night) onEndNight()
    }
    NightScreen(
        game = current,
        mode = viewModel.mode,
        kill = viewModel.kill,
        targets = viewModel.targets,
        hackSource = viewModel.hackSource,
        canConfirm = viewModel.canConfirm(),
        error = viewModel.error,
        onKill = viewModel::chooseKill,
        onTarget = viewModel::toggleTarget,
        onHack = viewModel::selectHack,
        onConfirm = viewModel::confirm,
        onGoToDay = onEndNight,
        onOpenPlayerBoard = onOpenPlayerBoard,
        onOpenJournal = onOpenJournal,
    )
}

@Composable
fun NightScreen(
    game: GameState,
    mode: GameMode,
    kill: Boolean,
    targets: List<PlayerId>,
    hackSource: NightStep?,
    canConfirm: Boolean,
    error: String?,
    onKill: (Boolean) -> Unit,
    onTarget: (PlayerId) -> Unit,
    onHack: (NightStep) -> Unit,
    onConfirm: () -> Unit,
    onGoToDay: () -> Unit,
    onOpenPlayerBoard: () -> Unit,
    onOpenJournal: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LightStatusBarIcons()
    val names = remember(game.players) { game.players.associate { it.id to it.name } }
    val name: (PlayerId) -> String = { names[it] ?: it.value }
    Column(
        modifier
            .fillMaxSize()
            .background(SporzTheme.colors.background),
    ) {
        SecretSurface(Modifier.fillMaxWidth()) { Header(game, mode) }
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PlayedSteps(game, name)
            val call = game.night?.progress?.current
            if (game.phase == Phase.Night && call != null) {
                CurrentStep(game, kill, targets, hackSource, canConfirm, error, name, onKill, onTarget, onHack, onConfirm)
                Upcoming(game, name)
            } else {
                InfoNote(stringResource(R.string.night_over), icon = SporzIcons.Night)
                SporzButton(stringResource(R.string.night_go_to_day), onGoToDay, icon = SporzIcons.Day, modifier = Modifier.fillMaxWidth())
            }
        }
        SporzTabBar(
            items = listOf(
                TabItem(stringResource(R.string.tab_phase), SporzIcons.Night),
                TabItem(stringResource(R.string.tab_players), SporzIcons.Players),
                TabItem(stringResource(R.string.tab_journal), SporzIcons.Journal),
            ),
            selectedIndex = 0,
            onSelect = { index ->
                when (index) {
                    1 -> onOpenPlayerBoard()
                    2 -> onOpenJournal()
                }
            },
        )
    }
}

@Composable
private fun Header(game: GameState, mode: GameMode) {
    val alive = game.players.count { it.alive }
    val mutated = game.players.count { it.alive && it.isMutated }
    val dead = game.players.size - alive
    Row(
        Modifier
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                stringResource(R.string.night_title, game.round).uppercase(),
                style = SporzTypography.titleMedium,
                modifier = Modifier.semantics { heading() },
            )
            Text(
                listOf(
                    pluralStringResource(R.plurals.night_players, game.players.size, game.players.size),
                    pluralStringResource(R.plurals.night_mutated, mutated, mutated),
                    pluralStringResource(R.plurals.night_dead, dead, dead),
                ).joinToString(" · "),
                style = SporzTypography.small,
                color = SporzTheme.colors.textMuted,
            )
        }
        Pill(stringResource(mode.titleRes).uppercase(), tone = Tone.Accent)
    }
}

/** Steps already played or skipped tonight, with what happened (MJ only). */
@Composable
private fun PlayedSteps(game: GameState, name: (PlayerId) -> String) {
    val entries = game.journal.filter { it.round == game.round && it.phase == Phase.Night }
    entries.forEach { entry ->
        when (val event = entry.event) {
            is JournalEvent.StepPlayed -> {
                val parts = event.events.filterNot { it is NightEvent.Analysis }.map { nightEventText(it, name) } +
                    listOfNotNull(
                        event.report?.let { reportText(it, name) }
                            ?: game.night?.pendingHack?.takeIf { event.step == NightStep.Hacker }?.let { hackPendingText(it.source) },
                    )
                PlayedRow(event.step, event.actors.joinToString(" · ") { name(it) }, parts.joinToString(" · "), done = true)
            }
            is JournalEvent.StepSkipped -> PlayedRow(event.step, "", stringResource(R.string.night_nobody), done = false)
            else -> Unit
        }
    }
}

@Composable
private fun PlayedRow(step: NightStep, actors: String, summary: String, done: Boolean) {
    val colors = SporzTheme.colors
    SporzCard(contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StepIndicator(0, if (done) StepState.Done else StepState.Upcoming)
            Column(Modifier.weight(1f)) {
                Text(stringResource(step.nameRes), style = SporzTypography.bodyStrong)
                if (actors.isNotEmpty()) Text(actors, style = SporzTypography.small, color = colors.textMuted)
            }
            Text(
                summary,
                style = SporzTypography.label.copy(fontWeight = FontWeight.SemiBold),
                color = if (done) colors.text else colors.textMuted,
                modifier = Modifier.weight(1.4f),
            )
        }
    }
}

@Composable
private fun CurrentStep(
    game: GameState,
    kill: Boolean,
    targets: List<PlayerId>,
    hackSource: NightStep?,
    canConfirm: Boolean,
    error: String?,
    name: (PlayerId) -> String,
    onKill: (Boolean) -> Unit,
    onTarget: (PlayerId) -> Unit,
    onHack: (NightStep) -> Unit,
    onConfirm: () -> Unit,
) {
    val colors = SporzTheme.colors
    val progress = game.night!!.progress
    val call = progress.current!!
    val step = call.step
    SporzCard(highlighted = true) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StepIndicator(progress.position + 1, StepState.Current)
            Text(stringResource(step.nameRes).uppercase(), style = SporzTypography.heading, modifier = Modifier.weight(1f))
            Text(
                call.actors.joinToString(" · ") { name(it) },
                style = SporzTypography.label.copy(fontWeight = FontWeight.Normal),
                color = colors.textMuted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (game.settings.guidedCall) {
            Text(stringResource(step.callRes), style = SporzTypography.body.copy(fontStyle = FontStyle.Italic), color = colors.textBody)
        }
        when (step) {
            NightStep.Mutants, NightStep.Doctors -> SegmentedControl(
                options = listOf(
                    stringResource(if (step == NightStep.Mutants) R.string.night_mutate else R.string.night_heal),
                    stringResource(R.string.night_kill),
                ),
                selectedIndex = if (kill) 1 else 0,
                onSelect = { onKill(it == 1) },
            )
            else -> Unit
        }
        when (step) {
            NightStep.ComputerScientist ->
                Text(reportText(computerScientistReport(game.players), name).uppercase(), style = SporzTypography.heading, color = colors.accent)
            NightStep.Hacker -> {
                Text(stringResource(R.string.night_hack_choice), style = SporzTypography.small, color = colors.textMuted)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    NightViewModel.hackChoices(game).forEach { source ->
                        TargetChip(stringResource(source.nameRes), selected = source == hackSource, onClick = { onHack(source) })
                    }
                }
            }
            else -> {
                val max = NightViewModel.maxTargets(game, kill)
                Text(
                    if (step == NightStep.Doctors && !kill) pluralStringResource(R.plurals.night_heal_targets, max, max) else stringResource(R.string.night_target),
                    style = SporzTypography.small,
                    color = colors.textMuted,
                )
                val eligible = NightViewModel.eligibleTargets(game, kill).toSet()
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    game.players.forEach { player ->
                        TargetChip(
                            label = player.name,
                            selected = player.id in targets,
                            onClick = { onTarget(player.id) },
                            enabled = player.id in eligible,
                            eliminated = !player.alive,
                        )
                    }
                }
            }
        }
        if (error != null) Text(stringResource(R.string.night_error, error), style = SporzTypography.small, color = colors.mutant.content)
        SporzButton(
            text = confirmLabel(step, kill, targets, hackSource, name),
            onClick = onConfirm,
            enabled = canConfirm,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun confirmLabel(step: NightStep, kill: Boolean, targets: List<PlayerId>, hackSource: NightStep?, name: (PlayerId) -> String): String {
    val chosen = targets.joinToString(", ") { name(it) }
    return when {
        step == NightStep.ComputerScientist -> stringResource(R.string.night_confirm_info)
        step == NightStep.Hacker -> hackSource?.let { stringResource(R.string.night_confirm_hack, stringResource(it.nameRes)) }
            ?: stringResource(R.string.night_hack_choice)
        targets.isEmpty() -> stringResource(R.string.night_choose_target)
        kill -> stringResource(R.string.night_confirm_kill, chosen)
        step == NightStep.Mutants -> stringResource(R.string.night_confirm_mutate, chosen)
        step == NightStep.Doctors -> stringResource(R.string.night_confirm_heal, chosen)
        step == NightStep.Spy -> stringResource(R.string.night_confirm_spy, chosen)
        else -> stringResource(R.string.night_confirm_analyse, chosen)
    }
}

/** Steps still to come and who would be woken (design: grey numbered rows). */
@Composable
private fun Upcoming(game: GameState, name: (PlayerId) -> String) {
    val colors = SporzTheme.colors
    val progress = game.night!!.progress
    val order = progress.order
    progress.upcoming(game.players).forEach { call ->
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StepIndicator(order.indexOf(call.step) + 1, StepState.Upcoming)
            Text(stringResource(call.step.nameRes), style = SporzTypography.body.copy(fontWeight = FontWeight.Medium), color = colors.textMuted, modifier = Modifier.weight(1f))
            Text(call.actors.joinToString(" · ") { name(it) }, style = SporzTypography.small, color = colors.textMuted)
        }
    }
}
