package io.github.cyprienj.sporz.ui.gm.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.cyprienj.sporz.data.GameSession
import io.github.cyprienj.sporz.engine.game.GameState
import io.github.cyprienj.sporz.engine.model.GameMode
import io.github.cyprienj.sporz.engine.model.NightStep
import io.github.cyprienj.sporz.engine.model.PlayerId
import io.github.cyprienj.sporz.engine.night.DoctorsDecision
import io.github.cyprienj.sporz.engine.night.IllegalActionException
import io.github.cyprienj.sporz.engine.night.MutantsDecision
import io.github.cyprienj.sporz.engine.night.hackableSteps
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/** MJ night screen (ticket C7): one step at a time, through the engine's [GameState]. */
@HiltViewModel
class NightViewModel @Inject constructor(private val session: GameSession) : ViewModel() {

    val game: StateFlow<GameState?> = session.game
    val mode: GameMode = session.draft.value?.mode ?: GameMode.Offline

    /** Mutants or doctors chose to kill rather than mutate / heal. */
    var kill by mutableStateOf(false)
        private set

    /** Chosen targets, in the order they were picked. */
    var targets by mutableStateOf<List<PlayerId>>(emptyList())
        private set

    var hackSource by mutableStateOf<NightStep?>(null)
        private set

    /** Why the last validation was refused, if it was (should not happen: choices are filtered). */
    var error by mutableStateOf<String?>(null)
        private set

    fun chooseKill(value: Boolean) {
        kill = value
        targets = emptyList()
    }

    /** Doctors healing pick one player per doctor; every other step picks one target. */
    fun toggleTarget(id: PlayerId) {
        val game = game.value ?: return
        val max = maxTargets(game, kill)
        targets = when {
            id in targets -> targets - id
            max == 1 -> listOf(id)
            targets.size < max -> targets + id
            else -> targets
        }
    }

    fun selectHack(step: NightStep) {
        hackSource = step
    }

    fun canConfirm(): Boolean {
        val game = game.value ?: return false
        return when (game.currentStep) {
            null -> false
            NightStep.ComputerScientist -> true
            NightStep.Hacker -> hackSource != null
            else -> targets.size == maxTargets(game, kill)
        }
    }

    fun confirm() {
        if (!canConfirm()) return
        try {
            session.updateGame { game ->
                when (game.currentStep) {
                    NightStep.Mutants -> game.playMutants(
                        if (kill) MutantsDecision.Kill(targets.single()) else MutantsDecision.Mutate(targets.single()),
                    )
                    NightStep.Doctors -> game.playDoctors(
                        if (kill) DoctorsDecision.Kill(targets.single()) else DoctorsDecision.Heal(targets),
                    )
                    NightStep.ComputerScientist -> game.playComputerScientist()
                    NightStep.Psychologist -> game.playPsychologist(targets.single())
                    NightStep.Geneticist -> game.playGeneticist(targets.single())
                    NightStep.Hacker -> game.playHacker(checkNotNull(hackSource))
                    NightStep.Spy -> game.playSpy(targets.single())
                    null -> game
                }
            }
            kill = false
            targets = emptyList()
            hackSource = null
            error = null
        } catch (e: IllegalActionException) {
            error = e.problem.name
        } catch (e: IllegalStateException) {
            error = e.message
        }
    }

    companion object {
        /** Players the MJ can pick at the current step (rules §3.2 to §3.4). */
        fun eligibleTargets(game: GameState, kill: Boolean): List<PlayerId> {
            val actors = game.night?.progress?.current?.actors.orEmpty()
            val alive = game.players.filter { it.alive }
            return when (game.currentStep) {
                NightStep.Mutants -> alive.filter { !it.isMutated }                   // R-NIGHT-7
                NightStep.Doctors -> if (kill) alive.filter { it.id !in actors } else alive // R-NIGHT-5, R-NIGHT-11
                NightStep.Psychologist, NightStep.Geneticist, NightStep.Spy -> alive.filter { it.id !in actors }
                NightStep.ComputerScientist, NightStep.Hacker, null -> emptyList()
            }.map { it.id }
        }

        /** Number of targets to pick: one per acting doctor when healing, one otherwise. */
        fun maxTargets(game: GameState, kill: Boolean): Int = when (game.currentStep) {
            NightStep.Doctors -> if (kill) 1 else game.night?.progress?.current?.actors?.size ?: 1
            NightStep.ComputerScientist, NightStep.Hacker, null -> 0
            else -> 1
        }

        fun hackChoices(game: GameState): List<NightStep> = hackableSteps(game.variants)
    }
}
