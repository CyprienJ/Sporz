package io.github.cyprienj.sporz.engine.game

import io.github.cyprienj.sporz.engine.EngineJson
import io.github.cyprienj.sporz.engine.GameEnd
import io.github.cyprienj.sporz.engine.checkVictory
import io.github.cyprienj.sporz.engine.day.Vote
import io.github.cyprienj.sporz.engine.day.VoteOption
import io.github.cyprienj.sporz.engine.day.VoteResult
import io.github.cyprienj.sporz.engine.day.closeVote as closeDayVote
import io.github.cyprienj.sporz.engine.day.electCaptain as runCaptainElection
import io.github.cyprienj.sporz.engine.day.passCaptaincy
import io.github.cyprienj.sporz.engine.model.Composition
import io.github.cyprienj.sporz.engine.model.GameSettings
import io.github.cyprienj.sporz.engine.model.NightStep
import io.github.cyprienj.sporz.engine.model.Player
import io.github.cyprienj.sporz.engine.model.PlayerId
import io.github.cyprienj.sporz.engine.model.Variants
import io.github.cyprienj.sporz.engine.night.DoctorsDecision
import io.github.cyprienj.sporz.engine.night.MutantsDecision
import io.github.cyprienj.sporz.engine.night.NightEvent
import io.github.cyprienj.sporz.engine.night.NightProgress
import io.github.cyprienj.sporz.engine.night.Report
import io.github.cyprienj.sporz.engine.night.checkHackerChoice
import io.github.cyprienj.sporz.engine.night.computerScientistReport
import io.github.cyprienj.sporz.engine.night.deaths
import io.github.cyprienj.sporz.engine.night.geneticistReport
import io.github.cyprienj.sporz.engine.night.hackerReport
import io.github.cyprienj.sporz.engine.night.informantReport
import io.github.cyprienj.sporz.engine.night.investigatorReport
import io.github.cyprienj.sporz.engine.night.psychologistReport
import io.github.cyprienj.sporz.engine.night.resolveDoctors
import io.github.cyprienj.sporz.engine.night.resolveMutants
import io.github.cyprienj.sporz.engine.night.spyReport
import io.github.cyprienj.sporz.engine.setup.Seat
import io.github.cyprienj.sporz.engine.setup.deal
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlin.random.Random

/** A report given to a player (their notebook, R-NIGHT-15 to R-NIGHT-19). */
@Serializable
public data class ReportDelivery(val round: Int, val recipient: PlayerId, val step: NightStep, val report: Report)

/** The hacker chose a step that was not finished yet: copy delivered at the end of the night. */
@Serializable
public data class PendingHack(val hacker: PlayerId, val source: NightStep)

@Serializable
public data class NightState(
    val progress: NightProgress,
    val events: List<NightEvent> = emptyList(),
    /** Report of each information step played this night, for the hacker. */
    val reports: Map<NightStep, Report> = emptyMap(),
    val pendingHack: PendingHack? = null,
)

@Serializable
public data class DayState(
    val vote: Vote,
    /** Deaths of the previous night, announced at dawn (R-NIGHT-21). */
    val deaths: List<PlayerId>,
    /** Null while the vote is open; [VoteResult.CaptainMustChoose] while waiting for the captain. */
    val result: VoteResult? = null,
)

/**
 * A whole game, from the deal to the end. Every command checks the phase and
 * step, applies docs/rules.md, writes the journal and returns the new state;
 * the state itself never changes. It is plain data: save it with [toJson],
 * restore it with [fromJson].
 */
@Serializable
public data class GameState(
    val players: List<Player>,
    val composition: Composition,
    val settings: GameSettings,
    val round: Int,
    val phase: Phase,
    val night: NightState? = null,
    val day: DayState? = null,
    /** Captain variant (R-DAY-4 to R-DAY-6). */
    val captain: PlayerId? = null,
    /** Set when the captain died: they must name a successor (R-DAY-6). */
    val captainToReplace: PlayerId? = null,
    /** Every report delivered so far, all rounds (notebooks). */
    val reports: List<ReportDelivery> = emptyList(),
    val journal: List<JournalEntry> = emptyList(),
    val end: GameEnd? = null,
) {
    public val variants: Variants get() = composition.variants

    /** Step being played, or null outside the night. */
    public val currentStep: NightStep? get() = night?.progress?.current?.step

    /** Reports of one player, for their notebook. */
    public fun notebook(player: PlayerId): List<ReportDelivery> = reports.filter { it.recipient == player }

    /** Journal entries players may see during the game; everything once it is over. */
    public fun publicJournal(): List<JournalEntry> = if (phase == Phase.Ended) journal else journal.filter { it.event.isPublic }

    public fun toJson(): String = EngineJson.encodeToString(this)

    // --- Night -----------------------------------------------------------------

    /** R-NIGHT-6 to R-NIGHT-10, R-NIGHT-22 */
    public fun playMutants(decision: MutantsDecision): GameState {
        val actors = requireStep(NightStep.Mutants)
        val outcome = resolveMutants(players, decision)
        val night = night!!
        val withParalysis = copy(night = night.copy(progress = night.progress.copy(paralysed = decision.paralysed)))
        return withParalysis.afterStep(outcome.players, NightStep.Mutants, actors, outcome.events)
    }

    /** R-NIGHT-11 to R-NIGHT-14 */
    public fun playDoctors(decision: DoctorsDecision): GameState {
        val actors = requireStep(NightStep.Doctors)
        val outcome = resolveDoctors(players, actors, decision)
        return afterStep(outcome.players, NightStep.Doctors, actors, outcome.events)
    }

    /** R-NIGHT-15 */
    public fun playComputerScientist(): GameState {
        val actors = requireStep(NightStep.ComputerScientist)
        val report = computerScientistReport(players)
        return afterStep(players, NightStep.ComputerScientist, actors, report = report)
    }

    /** R-NIGHT-16 */
    public fun playPsychologist(target: PlayerId): GameState {
        val actors = requireStep(NightStep.Psychologist)
        val outcome = psychologistReport(players, actors.single(), target)
        return afterStep(players, NightStep.Psychologist, actors, listOfNotNull(outcome.event), outcome.report)
    }

    /** R-NIGHT-17 */
    public fun playGeneticist(target: PlayerId): GameState {
        val actors = requireStep(NightStep.Geneticist)
        val outcome = geneticistReport(players, actors.single(), target)
        return afterStep(players, NightStep.Geneticist, actors, listOfNotNull(outcome.event), outcome.report)
    }

    /** R-NIGHT-18: the copy is delivered now if [source] is finished, else at the end of the night. */
    public fun playHacker(source: NightStep): GameState {
        val actors = requireStep(NightStep.Hacker)
        checkHackerChoice(source, variants)
        val night = night!!
        val copy = hackerReport(source, night.reports, night.progress)
        val withChoice = if (copy == null) copy(night = night.copy(pendingHack = PendingHack(actors.single(), source))) else this
        return withChoice.afterStep(players, NightStep.Hacker, actors, report = copy, keepReport = false)
    }

    /**
     * R-NIGHT-3: the current step has nobody to wake but holders whose absence
     * must not show (paralysed, mutated, dead and not revealed). The MJ has
     * called it as if somebody played; nothing happens and the night goes on.
     */
    public fun pretendStep(): GameState {
        check(phase == Phase.Night) { "Not night: $phase" }
        val night = night!!
        val current = checkNotNull(night.progress.current) { "The night is over" }
        check(current.pretend) { "${current.step} has players to wake: play it" }
        return copy(night = night.copy(progress = night.progress.advance(players)))
            .log(JournalEvent.StepPretended(current.step, current.absent))
            .logSkipped(night.progress.skipped.size)
            .finishNightIfOver()
    }

    /** R-NIGHT-19 */
    public fun playSpy(target: PlayerId): GameState {
        val actors = requireStep(NightStep.Spy)
        val outcome = spyReport(players, actors.single(), target, night!!.events)
        return afterStep(players, NightStep.Spy, actors, listOfNotNull(outcome.event), outcome.report)
    }

    /** R-NIGHT-23 */
    public fun playInformant(target: PlayerId): GameState {
        val actors = requireStep(NightStep.Informant)
        val outcome = informantReport(players, actors.single(), target)
        return afterStep(players, NightStep.Informant, actors, listOfNotNull(outcome.event), outcome.report)
    }

    /** R-NIGHT-24: about the previous night, read from the journal. */
    public fun playInvestigator(target: PlayerId): GameState {
        val actors = requireStep(NightStep.Investigator)
        val outcome = investigatorReport(players, actors.single(), target, nightEvents(round - 1))
        return afterStep(players, NightStep.Investigator, actors, listOfNotNull(outcome.event), outcome.report)
    }

    /** Everything that happened during night [round], in order (empty for a night that did not happen). */
    private fun nightEvents(round: Int): List<NightEvent> = journal
        .filter { it.round == round && it.phase == Phase.Night }
        .flatMap { (it.event as? JournalEvent.StepPlayed)?.events.orEmpty() }

    // --- Day -------------------------------------------------------------------

    /** R-DAY-4: Day 1, before the first vote. */
    public fun electCaptain(ballots: Map<PlayerId, PlayerId>, random: Random): GameState {
        requireOpenVote()
        check(variants.captain) { "The captain variant is off" }
        check(captain == null) { "There is already a captain" }
        val election = runCaptainElection(players, ballots, random) ?: return this
        return copy(captain = election.captain).log(JournalEvent.CaptainElected(election))
    }

    /** R-DAY-6: named when the death is known, at dawn or after the vote. */
    public fun nameNewCaptain(successor: PlayerId): GameState {
        check(phase == Phase.Day) { "The successor is named during the day" }
        val from = checkNotNull(captainToReplace) { "No captain to replace" }
        val to = passCaptaincy(players, from, successor)
        return copy(captain = to, captainToReplace = null).log(JournalEvent.CaptainNamed(from, to))
    }

    /** R-DAY-7 */
    public fun castVote(voter: PlayerId, option: VoteOption): GameState {
        val day = requireOpenVote()
        return copy(day = day.copy(vote = day.vote.cast(players, voter, option, variants)))
    }

    /** Throws away every ballot of the open vote ("Annuler" of the MJ day screen). */
    public fun resetVote(): GameState {
        val day = requireOpenVote()
        return copy(day = day.copy(vote = Vote(round)))
    }

    /** R-DAY-8 */
    public fun withdrawVote(voter: PlayerId): GameState {
        val day = requireOpenVote()
        return copy(day = day.copy(vote = day.vote.withdraw(voter)))
    }

    /**
     * Closes the vote (R-DAY-9 to R-DAY-12). On a tie with a captain, the
     * state waits for the captain: call again with [captainChoice].
     */
    public fun closeVote(captainChoice: VoteOption? = null): GameState {
        val day = checkNotNull(day) { "No vote outside the day" }
        check(day.result == null || day.result is VoteResult.CaptainMustChoose) { "The vote is already closed" }
        check(captainToReplace == null) { "The dead captain must name a successor first (R-DAY-6)" }

        val closing = closeDayVote(players, day.vote, variants, captain, captainChoice)
        if (closing.result is VoteResult.CaptainMustChoose) return copy(day = day.copy(result = closing.result))

        val tally = day.vote.tally(captain, variants).map { (option, votes) -> TallyLine(option, votes) }
        val closed = copy(players = closing.players, day = day.copy(result = closing.result))
            .log(JournalEvent.VoteClosed(closing.result, tally))
            .withCaptainCheck()
        return closed.endIfOver()
    }

    /** Starts the next night once the vote is closed (R-DAY-12). */
    public fun startNight(): GameState {
        check(phase == Phase.Day) { "Not day: $phase" }
        val day = day!!
        check(day.result != null && day.result !is VoteResult.CaptainMustChoose) { "Close the vote first" }
        check(captainToReplace == null) { "The dead captain must name a successor first (R-DAY-6)" }
        return beginNight(round + 1)
    }

    /** R-WIN-4 */
    public fun abort(): GameState {
        check(phase != Phase.Ended) { "The game is over" }
        return copy(phase = Phase.Ended, end = GameEnd.Aborted).log(JournalEvent.GameEnded(GameEnd.Aborted))
    }

    // --- Internals -------------------------------------------------------------

    private fun requireStep(step: NightStep): List<PlayerId> {
        check(phase == Phase.Night) { "Not night: $phase" }
        val current = checkNotNull(night?.progress?.current) { "The night is over" }
        check(current.step == step) { "Current step is ${current.step}, not $step" }
        check(!current.pretend) { "$step has nobody to wake: pretend it" }
        return current.actors
    }

    private fun requireOpenVote(): DayState {
        check(phase == Phase.Day) { "Not day: $phase" }
        val day = day!!
        check(day.result == null) { "The vote is closed" }
        return day
    }

    private fun afterStep(
        newPlayers: List<Player>,
        step: NightStep,
        actors: List<PlayerId>,
        events: List<NightEvent> = emptyList(),
        report: Report? = null,
        keepReport: Boolean = true,
    ): GameState {
        val night = night!!
        val deliveries = if (report != null) actors.map { ReportDelivery(round, it, step, report) } else emptyList()
        val progress = night.progress.advance(newPlayers)
        val newNight = night.copy(
            progress = progress,
            events = night.events + events,
            reports = if (report != null && keepReport) night.reports + (step to report) else night.reports,
        )
        return copy(players = newPlayers, night = newNight, reports = reports + deliveries)
            .log(JournalEvent.StepPlayed(step, actors, events, report))
            .withCaptainCheck()
            .logSkipped(night.progress.skipped.size)
            .finishNightIfOver()
    }

    private fun beginNight(round: Int): GameState {
        val progress = NightProgress.start(round, settings.callOrder, players, deadRevealed = variants.revealOnElimination)
        return copy(round = round, phase = Phase.Night, night = NightState(progress), day = null)
            .logSkipped(0)
            .finishNightIfOver()
    }

    private fun logSkipped(alreadyLogged: Int): GameState =
        night!!.progress.skipped.drop(alreadyLogged).fold(this) { state, step -> state.log(JournalEvent.StepSkipped(step)) }

    /** R-NIGHT-18 (end-of-night copy), R-NIGHT-20, R-NIGHT-21 */
    private fun finishNightIfOver(): GameState {
        val night = night!!
        if (!night.progress.isOver) return this

        var state = this
        night.pendingHack?.let { pending ->
            val copy = Report.Hacked(pending.source, night.reports[pending.source])
            state = state.copy(reports = state.reports + ReportDelivery(round, pending.hacker, NightStep.Hacker, copy))
                .log(JournalEvent.HackDelivered(pending.hacker, copy))
        }
        val deaths = night.events.deaths()
        state = state.copy(phase = Phase.Day, day = DayState(Vote(round), deaths))
            .log(JournalEvent.Dawn(deaths))
            .withCaptainCheck()
        return state.endIfOver()
    }

    private fun withCaptainCheck(): GameState {
        val captain = captain ?: return this
        return if (players.single { it.id == captain }.alive) this else copy(captain = null, captainToReplace = captain)
    }

    private fun endIfOver(): GameState {
        val end = checkVictory(players) ?: return this
        // A dead captain has nobody to lead any more.
        return copy(phase = Phase.Ended, end = end, captainToReplace = null).log(JournalEvent.GameEnded(end))
    }

    private fun log(event: JournalEvent): GameState = copy(journal = journal + JournalEntry(round, phase, event))

    public companion object {
        /** Deals the roles (B3) and starts night 1 (R-NIGHT-1). */
        public fun start(seats: List<Seat>, composition: Composition, settings: GameSettings, random: Random): GameState =
            fromDeal(deal(seats, composition, random), composition, settings)

        /** Starts night 1 with players already dealt (by [deal], or elsewhere). */
        public fun fromDeal(players: List<Player>, composition: Composition, settings: GameSettings): GameState =
            GameState(players, composition, settings, round = 1, phase = Phase.Night)
                .log(JournalEvent.GameStarted(players.size))
                .beginNight(1)

        public fun fromJson(json: String): GameState = EngineJson.decodeFromString(json)
    }
}
