package io.github.cyprienj.sporz.ui.gm.game

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.cyprienj.sporz.R
import io.github.cyprienj.sporz.data.GameSession
import io.github.cyprienj.sporz.engine.GameEnd
import io.github.cyprienj.sporz.engine.day.VoteResult
import io.github.cyprienj.sporz.engine.game.GameState
import io.github.cyprienj.sporz.engine.game.JournalEvent
import io.github.cyprienj.sporz.engine.game.Phase
import io.github.cyprienj.sporz.engine.model.NightStep
import io.github.cyprienj.sporz.engine.model.PlayerId
import io.github.cyprienj.sporz.engine.model.PlayerState
import io.github.cyprienj.sporz.engine.night.NightEvent
import io.github.cyprienj.sporz.ui.common.nameRes
import io.github.cyprienj.sporz.ui.common.nightEventText
import io.github.cyprienj.sporz.ui.common.reportText
import io.github.cyprienj.sporz.ui.common.showsSkipped
import io.github.cyprienj.sporz.ui.components.SporzIconButton
import io.github.cyprienj.sporz.ui.components.SporzTabBar
import io.github.cyprienj.sporz.ui.components.TabItem
import io.github.cyprienj.sporz.ui.theme.LightStatusBarIcons
import io.github.cyprienj.sporz.ui.theme.SecretSurface
import io.github.cyprienj.sporz.ui.theme.SporzIcons
import io.github.cyprienj.sporz.ui.theme.SporzPalette
import io.github.cyprienj.sporz.ui.theme.SporzShapes
import io.github.cyprienj.sporz.ui.theme.SporzTheme
import io.github.cyprienj.sporz.ui.theme.SporzTypography
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class JournalViewModel @Inject constructor(session: GameSession) : ViewModel() {
    val game: StateFlow<GameState?> = session.game
}

/** One night or day of the journal, with its "who | what" rows. */
data class JournalSection(val title: String, val phase: Phase, val current: Boolean, val rows: List<Pair<String, String>>)

/** design.html, board "MJ · Journal". */
@Composable
fun JournalRoute(
    onBackToPhase: () -> Unit,
    onOpenPlayerBoard: () -> Unit,
    viewModel: JournalViewModel = hiltViewModel(),
) {
    val game by viewModel.game.collectAsStateWithLifecycle()
    JournalScreen(game ?: return, onBackToPhase, onOpenPlayerBoard)
}

@Composable
fun JournalScreen(game: GameState, onBackToPhase: () -> Unit, onOpenPlayerBoard: () -> Unit, modifier: Modifier = Modifier) {
    LightStatusBarIcons()
    val context = LocalContext.current
    val sections = journalSections(game)
    val title = stringResource(R.string.journal_title)
    val exportLabel = stringResource(R.string.journal_export)
    Column(
        modifier
            .fillMaxSize()
            .background(SporzTheme.colors.background),
    ) {
        SecretSurface(Modifier.fillMaxWidth()) {
            Row(
                Modifier
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(title.uppercase(), style = SporzTypography.titleMedium, modifier = Modifier.weight(1f).semantics { heading() })
                Box(Modifier.border(1.dp, SporzTheme.colors.outline, SporzShapes.medium)) {
                    SporzIconButton(SporzIcons.Download, exportLabel, size = 44.dp, onClick = {
                        val send = Intent(Intent.ACTION_SEND)
                            .setType("text/plain")
                            .putExtra(Intent.EXTRA_SUBJECT, title)
                            .putExtra(Intent.EXTRA_TEXT, journalAsText(sections))
                        context.startActivity(Intent.createChooser(send, exportLabel))
                    })
                }
            }
        }
        LazyColumn(
            Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            if (sections.isEmpty()) item { Text(stringResource(R.string.journal_empty), color = SporzTheme.colors.textMuted) }
            items(sections) { section -> Section(section) }
        }
        SporzTabBar(
            items = listOf(
                TabItem(stringResource(R.string.tab_phase), if (game.phase == Phase.Night) SporzIcons.Night else SporzIcons.Day),
                TabItem(stringResource(R.string.tab_players), SporzIcons.Players),
                TabItem(stringResource(R.string.tab_journal), SporzIcons.Journal),
            ),
            selectedIndex = 2,
            onSelect = { index ->
                when (index) {
                    0 -> onBackToPhase()
                    1 -> onOpenPlayerBoard()
                }
            },
        )
    }
}

@Composable
private fun Section(section: JournalSection) {
    val colors = SporzTheme.colors
    val dot = when {
        section.current -> colors.accent
        section.phase == Phase.Night -> colors.text
        else -> SporzPalette.OrangeLight
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.size(10.dp).background(dot, SporzShapes.pill))
            Text(section.title.uppercase(), style = SporzTypography.heading.copy(fontSize = 20.sp))
            if (section.current) {
                Text(stringResource(R.string.journal_current), style = SporzTypography.caption.copy(fontWeight = FontWeight.SemiBold), color = colors.textMuted)
            }
        }
        // Timeline: a thin line on the left, one white row per event.
        Column(
            Modifier
                .padding(start = 4.dp)
                .drawLeftLine(colors.cardBorder)
                .padding(start = 14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            section.rows.forEach { (who, what) ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(colors.card, SporzShapes.small)
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(who, style = SporzTypography.label.copy(fontWeight = FontWeight.SemiBold), color = colors.textBody, modifier = Modifier.width(92.dp))
                    Text(what, style = SporzTypography.label.copy(fontWeight = FontWeight.Normal), modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

private fun Modifier.drawLeftLine(color: Color): Modifier = drawBehind {
    drawLine(color, start = Offset(1.dp.toPx(), 0f), end = Offset(1.dp.toPx(), size.height), strokeWidth = 2.dp.toPx())
}

/** Sections, most recent first, built from the engine's journal (everything: this is the MJ view). */
@Composable
fun journalSections(game: GameState): List<JournalSection> {
    val names = game.players.associate { it.id to it.name }
    val name: (PlayerId) -> String = { names[it] ?: it.value }
    val groups = game.journal.groupBy { it.round to it.phase }
    return groups.entries.map { (key, entries) ->
        val (round, phase) = key
        JournalSection(
            title = stringResource(if (phase == Phase.Night) R.string.night_title else R.string.day_title, round),
            phase = phase,
            current = round == game.round && phase == game.phase,
            rows = entries.flatMap { journalRows(it.event, game, name) },
        )
    }.filter { it.rows.isNotEmpty() }.reversed()
}

@Composable
private fun journalRows(event: JournalEvent, game: GameState, name: (PlayerId) -> String): List<Pair<String, String>> = when (event) {
    is JournalEvent.GameStarted -> listOf(stringResource(R.string.journal_who_start) to pluralStringResource(R.plurals.journal_game_started, event.playerCount, event.playerCount))
    is JournalEvent.StepPlayed -> {
        val what = event.events.filterNot { it is NightEvent.Analysis }.map { nightEventText(it, name) } + listOfNotNull(event.report?.let { reportText(it, name) })
        listOf(stringResource(event.step.nameRes) to what.joinToString(" · ").ifEmpty { "—" })
    }
    is JournalEvent.StepSkipped ->
        if (game.showsSkipped(event.step)) listOf(stringResource(event.step.nameRes) to stringResource(R.string.night_nobody)) else emptyList()
    is JournalEvent.HackDelivered -> listOf(stringResource(NightStep.Hacker.nameRes) to reportText(event.report, name))
    is JournalEvent.Dawn -> listOf(
        stringResource(R.string.journal_who_dawn) to
            if (event.deaths.isEmpty()) stringResource(R.string.day_nobody_died) else stringResource(R.string.day_deaths, event.deaths.joinToString(", ") { name(it) }),
    )
    is JournalEvent.CaptainElected -> listOf(
        stringResource(R.string.journal_who_captain) to
            if (event.election.drawn) {
                stringResource(R.string.journal_captain_drawn, name(event.election.captain))
            } else {
                pluralStringResource(R.plurals.journal_captain_elected, event.election.votes, name(event.election.captain), event.election.votes)
            },
    )
    is JournalEvent.CaptainNamed -> listOf(stringResource(R.string.journal_who_captain) to stringResource(R.string.journal_captain_named, name(event.from), name(event.to)))
    is JournalEvent.VoteClosed -> {
        val vote = stringResource(R.string.journal_who_vote)
        when (val result = event.result) {
            is VoteResult.Ejected -> buildList {
                add(vote to pluralStringResource(R.plurals.day_result_ejected, result.votes, name(result.player), result.votes))
                val role = result.revealedRole
                val state = result.revealedState
                if (role != null && state != null) {
                    add(
                        stringResource(R.string.journal_who_reveal) to stringResource(
                            R.string.day_result_revealed,
                            stringResource(role.nameRes),
                            stringResource(if (state == PlayerState.Mutated) R.string.state_mutated else R.string.state_healthy),
                        ),
                    )
                }
            }
            VoteResult.BlankWins -> listOf(vote to stringResource(R.string.day_result_blank))
            is VoteResult.Tie -> listOf(vote to stringResource(R.string.day_result_tie))
            VoteResult.NoVotes -> listOf(vote to stringResource(R.string.day_result_no_votes))
            is VoteResult.CaptainMustChoose -> emptyList()
        }
    }
    is JournalEvent.GameEnded -> listOf(
        stringResource(R.string.journal_who_end) to stringResource(
            when (event.end) {
                GameEnd.HealthyWin -> R.string.journal_end_healthy
                GameEnd.MutantsWin -> R.string.journal_end_mutants
                GameEnd.NoSurvivors -> R.string.journal_end_none
                GameEnd.Aborted -> R.string.journal_end_aborted
            },
        ),
    )
}

/** Plain text of the journal for the share sheet. */
fun journalAsText(sections: List<JournalSection>): String = sections.joinToString("\n\n") { section ->
    section.title.uppercase() + "\n" + section.rows.joinToString("\n") { (who, what) -> "• $who : $what" }
}
