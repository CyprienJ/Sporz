package io.github.cyprienj.sporz.engine.night

import io.github.cyprienj.sporz.engine.model.Genome
import io.github.cyprienj.sporz.engine.model.NightStep
import io.github.cyprienj.sporz.engine.model.Player
import io.github.cyprienj.sporz.engine.model.PlayerId
import io.github.cyprienj.sporz.engine.model.PlayerState
import io.github.cyprienj.sporz.engine.model.Variants
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** What an information role learns at night (docs/rules.md §3.4). */
@Serializable
public sealed interface Report {

    /** R-NIGHT-15: living mutated players. */
    @Serializable @SerialName("mutantCount")
    public data class MutantCount(val count: Int) : Report

    /** R-NIGHT-16 */
    @Serializable @SerialName("state")
    public data class State(val target: PlayerId, val state: PlayerState) : Report

    /** R-NIGHT-17 */
    @Serializable @SerialName("genome")
    public data class GenomeOf(val target: PlayerId, val genome: Genome) : Report

    /** R-NIGHT-19: in the order things happened; empty when nothing targeted the player. */
    @Serializable @SerialName("spy")
    public data class Spy(val target: PlayerId, val findings: List<SpyFinding>) : Report

    /** R-NIGHT-18: [copy] is null when the hacked role produced no report ("nothing"). */
    @Serializable @SerialName("hacked")
    public data class Hacked(val source: NightStep, val copy: Report?) : Report
}

/** What the spy learns about each action that targeted their player (R-NIGHT-19). */
@Serializable
public enum class SpyFinding {
    @SerialName("mutated") Mutated,
    @SerialName("mutationFailed") MutationFailed,
    @SerialName("killedByMutants") KilledByMutants,
    @SerialName("healed") Healed,

    /** Heal on a host or on a player who was not mutated: nothing happened. */
    @SerialName("healFailed") HealFailed,
    @SerialName("killedByDoctors") KilledByDoctors,
    @SerialName("analysedByPsychologist") AnalysedByPsychologist,
    @SerialName("analysedByGeneticist") AnalysedByGeneticist,
}

/** A report and the event it leaves for the spy, if any. */
@Serializable
public data class ReportOutcome(val report: Report, val event: NightEvent? = null)

/** R-NIGHT-15: counted at the moment of the step, after the mutants and doctors. */
public fun computerScientistReport(players: List<Player>): Report.MutantCount =
    Report.MutantCount(players.count { it.alive && it.isMutated })

/** R-NIGHT-16: the traitor shows as healthy unless mutated. */
public fun psychologistReport(players: List<Player>, actor: PlayerId, target: PlayerId): ReportOutcome {
    val player = players.designate(actor, target)
    return ReportOutcome(Report.State(player.id, player.state), NightEvent.Analysis(player.id, Analyst.Psychologist))
}

/** R-NIGHT-17 */
public fun geneticistReport(players: List<Player>, actor: PlayerId, target: PlayerId): ReportOutcome {
    val player = players.designate(actor, target)
    return ReportOutcome(Report.GenomeOf(player.id, player.genome), NightEvent.Analysis(player.id, Analyst.Geneticist))
}

/** R-NIGHT-19: [events] are the night's events so far. */
public fun spyReport(players: List<Player>, actor: PlayerId, target: PlayerId, events: List<NightEvent>): Report.Spy {
    val player = players.designate(actor, target)
    val findings = events.filter { it.target == player.id }.map { event ->
        when (event) {
            is NightEvent.Mutation -> if (event.succeeded) SpyFinding.Mutated else SpyFinding.MutationFailed
            is NightEvent.Kill -> when (event.by) {
                Killer.Mutants -> SpyFinding.KilledByMutants
                Killer.Doctors -> SpyFinding.KilledByDoctors
            }
            is NightEvent.Heal -> if (event.outcome == HealOutcome.Healed) SpyFinding.Healed else SpyFinding.HealFailed
            is NightEvent.Analysis -> when (event.by) {
                Analyst.Psychologist -> SpyFinding.AnalysedByPsychologist
                Analyst.Geneticist -> SpyFinding.AnalysedByGeneticist
            }
        }
    }
    return Report.Spy(player.id, findings)
}

/** Steps whose report the hacker can copy (R-NIGHT-18). */
public fun hackableSteps(variants: Variants): List<NightStep> = buildList {
    add(NightStep.Psychologist)
    add(NightStep.Geneticist)
    add(NightStep.ComputerScientist)
    if (variants.hackerCanHackSpy) add(NightStep.Spy)
}

/** Refuses a hacker choice the rules do not allow (R-NIGHT-18). */
public fun checkHackerChoice(source: NightStep, variants: Variants) {
    if (source !in hackableSteps(variants)) throw IllegalActionException(ActionProblem.NotHackable, null)
}

/**
 * The hacker's copy of [source]'s report (R-NIGHT-18, DEC-24), or null while
 * that step is not finished yet: the hacker then gets it at the end of the
 * night (always the case for the spy with the default order). The hacked
 * player keeps their own report.
 *
 * @param reports reports produced so far this night, by step.
 */
public fun hackerReport(source: NightStep, reports: Map<NightStep, Report>, night: NightProgress): Report.Hacked? =
    if (night.isFinished(source)) Report.Hacked(source, reports[source]) else null

/** R-NIGHT-5: a living player other than the actor. */
private fun List<Player>.designate(actor: PlayerId, target: PlayerId): Player {
    if (actor == target) throw IllegalActionException(ActionProblem.SelfTarget, target)
    val player = find(target)
    if (!player.alive) throw IllegalActionException(ActionProblem.TargetDead, target)
    return player
}
