package io.github.cyprienj.sporz.ui.gm.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.cyprienj.sporz.R
import io.github.cyprienj.sporz.engine.day.VoteOption
import io.github.cyprienj.sporz.engine.day.VoteResult
import io.github.cyprienj.sporz.engine.game.GameState
import io.github.cyprienj.sporz.engine.game.Phase
import io.github.cyprienj.sporz.engine.model.PlayerId
import io.github.cyprienj.sporz.engine.model.PlayerState
import io.github.cyprienj.sporz.engine.night.NightEvent
import io.github.cyprienj.sporz.ui.common.nameRes
import io.github.cyprienj.sporz.ui.common.nightEventText
import io.github.cyprienj.sporz.ui.components.InfoNote
import io.github.cyprienj.sporz.ui.components.Pill
import io.github.cyprienj.sporz.ui.components.SectionTitle
import io.github.cyprienj.sporz.ui.components.SporzButton
import io.github.cyprienj.sporz.ui.components.SporzButtonStyle
import io.github.cyprienj.sporz.ui.components.SporzCard
import io.github.cyprienj.sporz.ui.components.SporzDropdown
import io.github.cyprienj.sporz.ui.components.SporzTabBar
import io.github.cyprienj.sporz.ui.components.TabItem
import io.github.cyprienj.sporz.ui.components.TargetChip
import io.github.cyprienj.sporz.ui.theme.SporzIcons
import io.github.cyprienj.sporz.ui.theme.SporzPalette
import io.github.cyprienj.sporz.ui.theme.SporzShapes
import io.github.cyprienj.sporz.ui.theme.SporzTheme
import io.github.cyprienj.sporz.ui.theme.SporzTypography
import kotlinx.coroutines.delay
import java.text.NumberFormat

/** design.html, board "MJ · Jour et vote". */
@Composable
fun DayRoute(
    onStartNight: () -> Unit,
    onGameOver: () -> Unit,
    onOpenPlayerBoard: () -> Unit,
    onOpenJournal: () -> Unit,
    viewModel: DayViewModel = hiltViewModel(),
) {
    val game by viewModel.game.collectAsStateWithLifecycle()
    val current = game ?: return
    // Leave for the night once it starts from here (startNight changes the phase).
    val arrivedDuringDay = remember { current.phase == Phase.Day }
    LaunchedEffect(current.phase, current.round) {
        if (arrivedDuringDay && current.phase == Phase.Night) onStartNight()
    }
    DayScreen(
        game = current,
        captainBallots = viewModel.captainBallots,
        captainPick = viewModel.captainPick,
        error = viewModel.error,
        actions = DayActions(
            setVote = viewModel::setVote,
            resetVote = viewModel::resetVote,
            closeVote = viewModel::closeVote,
            setCaptainBallot = viewModel::setCaptainBallot,
            electCaptain = { viewModel.electCaptain() },
            pickForCaptain = viewModel::pickForCaptain,
            nameSuccessor = viewModel::nameSuccessor,
            startNight = viewModel::startNight,
            gameOver = onGameOver,
        ),
        onOpenPlayerBoard = onOpenPlayerBoard,
        onOpenJournal = onOpenJournal,
    )
}

data class DayActions(
    val setVote: (PlayerId, VoteOption?) -> Unit,
    val resetVote: () -> Unit,
    val closeVote: () -> Unit,
    val setCaptainBallot: (PlayerId, PlayerId?) -> Unit,
    val electCaptain: () -> Unit,
    val pickForCaptain: (VoteOption?) -> Unit,
    val nameSuccessor: () -> Unit,
    val startNight: () -> Unit,
    val gameOver: () -> Unit,
)

@Composable
fun DayScreen(
    game: GameState,
    captainBallots: Map<PlayerId, PlayerId>,
    captainPick: VoteOption?,
    error: String?,
    actions: DayActions,
    onOpenPlayerBoard: () -> Unit,
    onOpenJournal: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val names = remember(game.players) { game.players.associate { it.id to it.name } }
    val name: (PlayerId) -> String = { names[it] ?: it.value }
    val day = game.day
    val result = day?.result
    val voteOpen = game.phase == Phase.Day && (result == null || result is VoteResult.CaptainMustChoose)
    Column(
        modifier
            .fillMaxSize()
            .background(SporzTheme.colors.background),
    ) {
        DayHeader(game, voteOpen)
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Announcement(game, name)
            DebateTimer(game.settings.debateTimerMinutes, key = game.round)
            val captainToReplace = game.captainToReplace
            when {
                captainToReplace != null -> Successor(game, captainToReplace, captainPick, name, actions)
                voteOpen && game.variants.captain && game.captain == null && game.round == 1 && day!!.vote.ballots.isEmpty() ->
                    CaptainElection(game, captainBallots, name, actions)
                else -> game.captain?.let { Text(stringResource(R.string.day_captain, name(it)), style = SporzTypography.bodyStrong) }
            }
            if (day != null) {
                Tally(game, name)
                if (result is VoteResult.CaptainMustChoose) {
                    TieChoice(game, result, captainPick, name, actions)
                } else if (voteOpen) {
                    Ballots(game, name, actions)
                } else if (result != null) {
                    ResultCard(game, result, name)
                }
            }
            if (error != null) Text(error, style = SporzTypography.small, color = SporzTheme.colors.mutant.content)
        }
        Footer(game, voteOpen, captainBlocks = game.captainToReplace != null, actions)
        SporzTabBar(
            items = listOf(
                TabItem(stringResource(R.string.tab_phase), SporzIcons.Day),
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

/** Light orange header of the design (a warm tint in the dark theme). */
@Composable
private fun DayHeader(game: GameState, voteOpen: Boolean) {
    val dark = SporzTheme.isDark
    val background = if (dark) SporzPalette.NavySelected else SporzPalette.OrangeLight
    val content = if (dark) SporzPalette.OrangeLight else SporzPalette.Navy
    val muted = if (dark) SporzPalette.MutedOnNavy else Color(0xFF3C2A1E)
    val alive = game.players.count { it.alive }
    Row(
        Modifier
            .fillMaxWidth()
            .background(background)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(SporzIcons.Day, contentDescription = null, tint = content, modifier = Modifier.size(26.dp))
                Text(
                    stringResource(R.string.day_title, game.round).uppercase(),
                    style = SporzTypography.titleMedium,
                    color = content,
                    modifier = Modifier.semantics { heading() },
                )
            }
            Text(pluralStringResource(R.plurals.day_alive, alive, alive), style = SporzTypography.small, color = muted)
        }
        Text(
            stringResource(if (voteOpen) R.string.day_vote_open else R.string.day_vote_closed).uppercase(),
            style = SporzTypography.caption.copy(fontWeight = FontWeight.SemiBold),
            color = if (dark) SporzPalette.Navy else SporzPalette.OrangeLight,
            modifier = Modifier
                .background(content, SporzShapes.pill)
                .padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun Section(title: String, trailing: String? = null, content: @Composable ColumnScope.() -> Unit) {
    SporzCard {
        Row(verticalAlignment = Alignment.Bottom) {
            SectionTitle(title, Modifier.weight(1f))
            if (trailing != null) Text(trailing, style = SporzTypography.small, color = SporzTheme.colors.textMuted)
        }
        content()
    }
}

/** R-NIGHT-21: deaths for everyone, what else happened for the MJ only. */
@Composable
private fun Announcement(game: GameState, name: (PlayerId) -> String) {
    val day = game.day ?: return
    val nightEvents = game.night?.events.orEmpty()
    Section(stringResource(R.string.day_announce)) {
        val deaths = day.deaths.map { id ->
            val player = game.players.single { it.id == id }
            if (game.variants.revealOnElimination) {
                stringResource(R.string.day_death_revealed, player.name, stringResource(player.role.nameRes), stringResource(player.state.nameRes))
            } else {
                player.name
            }
        }
        Text(
            if (deaths.isEmpty()) stringResource(R.string.day_nobody_died) else stringResource(R.string.day_deaths, deaths.joinToString(", ")),
            style = SporzTypography.bodyStrong.copy(fontSize = 17.sp),
        )
        // Who acted is said in front, e.g. "Mutants : ont muté Chloé".
        val secret = nightEvents.filter { it !is NightEvent.Kill && it !is NightEvent.Analysis }.map { event ->
            val actor = stringResource(if (event is NightEvent.Heal) R.string.night_step_doctors else R.string.night_step_mutants)
            "$actor : ${nightEventText(event, name)}"
        }
        InfoNote(stringResource(R.string.day_gm_only, secret.joinToString(" · ").ifEmpty { stringResource(R.string.day_nothing_else) }))
    }
}

@Composable
private fun DebateTimer(minutes: Int, key: Int) {
    if (minutes == 0) return
    var remaining by rememberSaveable(key) { mutableIntStateOf(minutes * 60) }
    var running by rememberSaveable(key) { mutableStateOf(false) }
    LaunchedEffect(running, key) {
        while (running && remaining > 0) {
            delay(1_000)
            remaining--
        }
        if (remaining == 0) running = false
    }
    Section(stringResource(R.string.day_timer)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "%d:%02d".format(remaining / 60, remaining % 60),
                style = SporzTypography.titleMedium,
                color = if (remaining == 0) SporzTheme.colors.mutant.content else SporzTheme.colors.text,
                modifier = Modifier.weight(1f),
            )
            if (remaining > 0) {
                SporzButton(
                    stringResource(if (running) R.string.day_timer_pause else R.string.day_timer_start),
                    { running = !running },
                    style = SporzButtonStyle.Secondary,
                    height = 44.dp,
                )
            }
        }
        if (remaining == 0) Text(stringResource(R.string.day_timer_over), style = SporzTypography.bodyStrong, color = SporzTheme.colors.mutant.content)
    }
}

@Composable
private fun CaptainElection(game: GameState, ballots: Map<PlayerId, PlayerId>, name: (PlayerId) -> String, actions: DayActions) {
    val alive = game.players.filter { it.alive }
    Section(stringResource(R.string.day_captain_election)) {
        Text(stringResource(R.string.day_captain_hint), style = SporzTypography.small, color = SporzTheme.colors.textMuted)
        alive.forEach { voter ->
            VoteRow(voter.name) {
                SporzDropdown(
                    options = alive.map { it.id },
                    selected = ballots[voter.id],
                    label = name,
                    placeholder = stringResource(R.string.day_choose),
                    onSelect = { actions.setCaptainBallot(voter.id, it) },
                    contentDescription = voter.name,
                )
            }
        }
        SporzButton(stringResource(R.string.day_elect), actions.electCaptain, enabled = ballots.isNotEmpty(), style = SporzButtonStyle.Inverse, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun Successor(game: GameState, deadCaptain: PlayerId, pick: VoteOption?, name: (PlayerId) -> String, actions: DayActions) {
    SporzCard(highlighted = true) {
        Text(stringResource(R.string.day_captain_dead, name(deadCaptain)), style = SporzTypography.bodyStrong)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            game.players.filter { it.alive }.forEach { player ->
                TargetChip(player.name, pick == VoteOption.For(player.id), { actions.pickForCaptain(VoteOption.For(player.id)) })
            }
        }
        val chosen = (pick as? VoteOption.For)?.target
        SporzButton(
            chosen?.let { stringResource(R.string.day_name_successor, name(it)) } ?: stringResource(R.string.day_choose),
            actions.nameSuccessor,
            enabled = chosen != null,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

/** Weighted tally with bars (captain counts 1.5), most votes first. */
@Composable
private fun Tally(game: GameState, name: (PlayerId) -> String) {
    val day = game.day ?: return
    val alive = game.players.count { it.alive }
    val tally = day.vote.tally(game.captain, game.variants)
    Section(stringResource(R.string.day_tally), pluralStringResource(R.plurals.day_votes_received, day.vote.ballots.size, day.vote.ballots.size, alive)) {
        tally.forEachIndexed { index, (option, votes) ->
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row {
                    Text(option.label(name, stringResource(R.string.day_blank)), style = SporzTypography.body.copy(fontWeight = FontWeight.SemiBold), modifier = Modifier.weight(1f))
                    Text(formatVotes(votes), style = SporzTypography.heading.copy(fontSize = 18.sp))
                }
                val colors = SporzTheme.colors
                Box(Modifier.fillMaxWidth().height(8.dp).background(colors.divider, SporzShapes.pill)) {
                    Box(
                        Modifier
                            .fillMaxWidth((votes.toFloat() / alive.coerceAtLeast(1)).coerceAtMost(1f))
                            .height(8.dp)
                            .background(
                                when {
                                    option == VoteOption.Blank -> colors.outline
                                    index == 0 -> colors.accent
                                    else -> colors.textMuted
                                },
                                SporzShapes.pill,
                            ),
                    )
                }
            }
        }
    }
}

@Composable
private fun Ballots(game: GameState, name: (PlayerId) -> String, actions: DayActions) {
    val day = game.day ?: return
    val alive = game.players.filter { it.alive }
    val options = buildList {
        if (game.variants.blankVoteAllowed) add(VoteOption.Blank)
        alive.forEach { add(VoteOption.For(it.id)) }
    }
    val blank = stringResource(R.string.day_blank)
    Section(stringResource(R.string.day_offline_votes)) {
        alive.forEach { voter ->
            VoteRow(voter.name) {
                SporzDropdown(
                    options = options,
                    selected = day.vote.ballots[voter.id],
                    label = { it.label(name, blank) },
                    placeholder = stringResource(R.string.day_choose),
                    onSelect = { actions.setVote(voter.id, it) },
                    contentDescription = voter.name,
                )
            }
        }
    }
}

@Composable
private fun VoteRow(voter: String, field: @Composable () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(voter, style = SporzTypography.body.copy(fontWeight = FontWeight.Medium), modifier = Modifier.weight(1f))
        Box(Modifier.width(170.dp)) { field() }
    }
}

@Composable
private fun TieChoice(game: GameState, result: VoteResult.CaptainMustChoose, pick: VoteOption?, name: (PlayerId) -> String, actions: DayActions) {
    SporzCard(highlighted = true) {
        Text(stringResource(R.string.day_tie_captain, game.captain?.let(name).orEmpty()), style = SporzTypography.bodyStrong)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            result.options.forEach { option ->
                TargetChip(option.label(name, stringResource(R.string.day_blank)), pick == option, { actions.pickForCaptain(option) })
            }
        }
        SporzButton(stringResource(R.string.day_confirm_choice), actions.closeVote, enabled = pick != null, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun ResultCard(game: GameState, result: VoteResult, name: (PlayerId) -> String) {
    SporzCard(highlighted = true) {
        when (result) {
            is VoteResult.Ejected -> {
                Text(pluralStringResource(R.plurals.day_result_ejected, result.votes.toInt(), name(result.player), formatVotes(result.votes)), style = SporzTypography.heading)
                val role = result.revealedRole
                val state = result.revealedState
                if (role != null && state != null) {
                    Pill(stringResource(R.string.day_result_revealed, stringResource(role.nameRes), stringResource(state.nameRes)))
                }
            }
            VoteResult.BlankWins -> Text(stringResource(R.string.day_result_blank), style = SporzTypography.bodyStrong)
            is VoteResult.Tie -> Text(stringResource(R.string.day_result_tie), style = SporzTypography.bodyStrong)
            VoteResult.NoVotes -> Text(stringResource(R.string.day_result_no_votes), style = SporzTypography.bodyStrong)
            is VoteResult.CaptainMustChoose -> Unit
        }
    }
}

@Composable
private fun Footer(game: GameState, voteOpen: Boolean, captainBlocks: Boolean, actions: DayActions) {
    val colors = SporzTheme.colors
    val result = game.day?.result
    Column(Modifier.background(colors.background)) {
        HorizontalDivider(color = colors.cardBorder)
        Row(Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            when {
                game.phase == Phase.Ended -> SporzButton(stringResource(R.string.screen_game_end), actions.gameOver, modifier = Modifier.weight(1f))
                voteOpen && result !is VoteResult.CaptainMustChoose -> {
                    SporzButton(stringResource(R.string.day_cancel_vote), actions.resetVote, style = SporzButtonStyle.Secondary)
                    SporzButton(
                        stringResource(R.string.day_close_vote),
                        actions.closeVote,
                        style = SporzButtonStyle.Inverse,
                        enabled = !captainBlocks,
                        modifier = Modifier.weight(1f),
                    )
                }
                !voteOpen -> SporzButton(
                    stringResource(R.string.day_next_night, game.round + 1),
                    actions.startNight,
                    enabled = !captainBlocks,
                    icon = SporzIcons.Night,
                    modifier = Modifier.weight(1f),
                )
                else -> Unit
            }
        }
    }
}

/** "Blanc" or the player's name; [blank] is the resource text, read once by the caller. */
private fun VoteOption.label(name: (PlayerId) -> String, blank: String): String = when (this) {
    VoteOption.Blank -> blank
    is VoteOption.For -> name(target)
}

private val PlayerState.nameRes: Int
    get() = if (this == PlayerState.Mutated) R.string.state_mutated else R.string.state_healthy

/** Weighted vote count for display: "4", or "2,5" with the captain's half vote (R-DAY-5). */
internal fun formatVotes(votes: Double): String =
    NumberFormat.getNumberInstance().apply { maximumFractionDigits = 1 }.format(votes)
