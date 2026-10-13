package io.github.cyprienj.sporz.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import io.github.cyprienj.sporz.R
import io.github.cyprienj.sporz.engine.game.GameState
import io.github.cyprienj.sporz.engine.model.NightStep
import io.github.cyprienj.sporz.engine.model.PlayerId
import io.github.cyprienj.sporz.engine.model.PlayerState
import io.github.cyprienj.sporz.engine.night.HealOutcome
import io.github.cyprienj.sporz.engine.night.Killer
import io.github.cyprienj.sporz.engine.night.NightEvent
import io.github.cyprienj.sporz.engine.night.Report
import io.github.cyprienj.sporz.engine.night.SpyFinding
import io.github.cyprienj.sporz.engine.night.role

/** Sentences for what happened at night, shared by the night screen, the journal and notebooks. */

@Composable
fun nightEventText(event: NightEvent, name: (PlayerId) -> String): String = when (event) {
    is NightEvent.Mutation ->
        stringResource(if (event.succeeded) R.string.event_mutated else R.string.event_mutation_failed, name(event.target))
    is NightEvent.Kill -> stringResource(R.string.event_killed, name(event.target))
    is NightEvent.Heal -> stringResource(
        when (event.outcome) {
            HealOutcome.Healed -> R.string.event_healed
            HealOutcome.Host -> R.string.event_heal_host
            HealOutcome.NotMutated -> R.string.event_heal_not_mutated
        },
        name(event.target),
    )
    // Analyses are told through the report of the step.
    is NightEvent.Analysis -> ""
}

/** One line for a report; [pending] is for a hacker's copy delivered at the end of the night. */
@Composable
fun reportText(report: Report, name: (PlayerId) -> String): String = when (report) {
    is Report.MutantCount -> pluralStringResource(R.plurals.report_mutant_count, report.count, report.count)
    is Report.State -> stringResource(
        if (report.state == PlayerState.Mutated) R.string.report_state_mutated else R.string.report_state_healthy,
        name(report.target),
    )
    is Report.GenomeOf -> stringResource(R.string.report_genome, name(report.target), stringResource(report.genome.nameRes))
    is Report.Spy ->
        if (report.findings.isEmpty()) {
            stringResource(R.string.report_spy_nothing, name(report.target))
        } else {
            stringResource(R.string.report_spy, name(report.target), report.findings.map { stringResource(it.textRes) }.joinToString(", "))
        }
    is Report.Hacked -> {
        val source = stringResource(report.source.nameRes)
        val copy = report.copy
        if (copy == null) stringResource(R.string.report_hacked_nothing, source) else stringResource(R.string.report_hacked, source, reportText(copy, name))
    }
}

@Composable
fun hackPendingText(source: NightStep): String = stringResource(R.string.report_hacked_pending, stringResource(source.nameRes))

private val SpyFinding.textRes: Int
    get() = when (this) {
        SpyFinding.Mutated -> R.string.spy_mutated
        SpyFinding.MutationFailed -> R.string.spy_mutation_failed
        SpyFinding.KilledByMutants -> R.string.spy_killed_by_mutants
        SpyFinding.Healed -> R.string.spy_healed
        SpyFinding.HealFailed -> R.string.spy_heal_failed
        SpyFinding.KilledByDoctors -> R.string.spy_killed_by_doctors
        SpyFinding.AnalysedByPsychologist -> R.string.spy_analysed_by_psychologist
        SpyFinding.AnalysedByGeneticist -> R.string.spy_analysed_by_geneticist
    }

/** Guided call text of a step (setting "Appel guidé"). */
val NightStep.callRes: Int
    get() = when (this) {
        NightStep.Mutants -> R.string.call_mutants
        NightStep.Doctors -> R.string.call_doctors
        NightStep.ComputerScientist -> R.string.call_computer_scientist
        NightStep.Psychologist -> R.string.call_psychologist
        NightStep.Geneticist -> R.string.call_geneticist
        NightStep.Hacker -> R.string.call_hacker
        NightStep.Spy -> R.string.call_spy
    }

/**
 * Whether a skipped step is worth showing: yes when its role is in the game
 * (dead or mutated holder: the MJ may pretend to call it, R-NIGHT-3), no when
 * the role is not in the composition at all (everybody knows it is absent).
 */
fun GameState.showsSkipped(step: NightStep): Boolean {
    val role = step.role ?: return true
    return composition.count(role) > 0
}
