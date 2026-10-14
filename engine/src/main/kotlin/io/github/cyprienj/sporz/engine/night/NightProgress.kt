package io.github.cyprienj.sporz.engine.night

import io.github.cyprienj.sporz.engine.model.NightStep
import io.github.cyprienj.sporz.engine.model.Player
import io.github.cyprienj.sporz.engine.model.PlayerId
import io.github.cyprienj.sporz.engine.model.Role
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Why a holder of the step's role is not woken, when the table must not notice it (R-NIGHT-3). */
@Serializable
public enum class AbsenceReason {
    /** R-NIGHT-22 */
    @SerialName("paralysed") Paralysed,

    /** R-NIGHT-4: lost their power. */
    @SerialName("mutated") Mutated,

    /** Dead, and their role was not revealed (reveal variant off, R-NIGHT-21). */
    @SerialName("dead") Dead,
}

@Serializable
public data class Absence(val player: PlayerId, val reason: AbsenceReason)

/**
 * A step being played, with the players woken for it and the holders who are
 * not ([absent]). A step with no actor but someone absent is still called, so
 * its silence gives nothing away: see [pretend].
 */
@Serializable
public data class StepCall(val step: NightStep, val actors: List<PlayerId>, val absent: List<Absence> = emptyList()) {
    /** R-NIGHT-3: nobody plays, the MJ calls the step as if somebody did ([io.github.cyprienj.sporz.engine.game.GameState.pretendStep]). */
    val pretend: Boolean get() = actors.isEmpty() && absent.isNotEmpty()
}

/**
 * Who is woken at [this] step given the players' current state (R-NIGHT-1,
 * R-NIGHT-4): every living mutated player for the mutants, otherwise the
 * living, non-mutated holders of the step's role, except the [paralysed]
 * player (R-NIGHT-22). The traitor is never woken (R-SET-12).
 */
public fun NightStep.actors(players: List<Player>, paralysed: PlayerId? = null): List<PlayerId> = when (this) {
    NightStep.Mutants -> players.filter { it.alive && it.isMutated }
    else -> players.filter { it.role == role && it.hasRolePower && it.id != paralysed }
}.map { it.id }

/**
 * Holders of this step's role who are not woken and whose absence must not show
 * (R-NIGHT-3): paralysed, mutated, or dead with their role still secret. A dead
 * holder whose role was revealed ([deadRevealed]) is known to be gone: left out.
 */
public fun NightStep.absent(players: List<Player>, paralysed: PlayerId?, deadRevealed: Boolean): List<Absence> {
    val role = role ?: return emptyList()
    return players.filter { it.role == role }.mapNotNull {
        when {
            !it.alive -> if (deadRevealed) null else Absence(it.id, AbsenceReason.Dead)
            it.isMutated -> Absence(it.id, AbsenceReason.Mutated)
            it.id == paralysed -> Absence(it.id, AbsenceReason.Paralysed)
            else -> null
        }
    }
}

/** Role whose holders play this step, or null for the mutants (they act whatever their role). */
public val NightStep.role: Role?
    get() = when (this) {
        NightStep.Mutants -> null
        NightStep.Doctors -> Role.Doctor
        NightStep.ComputerScientist -> Role.ComputerScientist
        NightStep.Psychologist -> Role.Psychologist
        NightStep.Geneticist -> Role.Geneticist
        NightStep.Hacker -> Role.Hacker
        NightStep.Spy -> Role.Spy
    }

/**
 * Where the night stands. Effects of a step apply immediately (R-NIGHT-2), so
 * the actors of a step are computed when the step starts: call [advance] with
 * the players as they are after the current step has been resolved.
 */
@Serializable
public data class NightProgress(
    val round: Int,
    val order: List<NightStep>,
    /** Index in [order] of [current]; `order.size` once the night is over. */
    val position: Int,
    /** Step being played, null once every step has been played or skipped. */
    val current: StepCall?,
    /** Steps skipped so far because nobody could play them (R-NIGHT-3). */
    val skipped: List<NightStep> = emptyList(),
    /** Player paralysed by the mutants: not woken for the rest of the night (R-NIGHT-22). */
    val paralysed: PlayerId? = null,
    /** Reveal variant (R-NIGHT-21): dead holders are known, their steps are skipped rather than pretended. */
    val deadRevealed: Boolean = true,
) {
    val isOver: Boolean get() = current == null

    /** True once [step] has been played or skipped this night. */
    public fun isFinished(step: NightStep): Boolean = isOver || order.indexOf(step) < position

    /** Moves to the next step somebody can play or that must be pretended, skipping the others (R-NIGHT-3). */
    public fun advance(players: List<Player>): NightProgress {
        check(!isOver) { "Night $round is already over" }
        return moveFrom(position + 1, players)
    }

    /**
     * Steps still to come with the players who would be woken if nothing
     * changed (MJ night screen, "upcoming roles"). Skippable steps are left out.
     */
    public fun upcoming(players: List<Player>): List<StepCall> =
        order.drop(position + 1)
            .map { call(it, players) }
            .filter { it.actors.isNotEmpty() || it.pretend }

    private fun call(step: NightStep, players: List<Player>) =
        StepCall(step, step.actors(players, paralysed), step.absent(players, paralysed, deadRevealed))

    private fun moveFrom(start: Int, players: List<Player>): NightProgress {
        val newlySkipped = mutableListOf<NightStep>()
        for (index in start until order.size) {
            val step = order[index]
            val call = call(step, players)
            if (call.actors.isNotEmpty() || call.pretend) {
                return copy(position = index, current = call, skipped = skipped + newlySkipped)
            }
            newlySkipped += step
        }
        return copy(position = order.size, current = null, skipped = skipped + newlySkipped)
    }

    public companion object {
        /** Starts night [round] (1 for the first night) with the call [order] of the settings. */
        public fun start(round: Int, order: List<NightStep>, players: List<Player>, deadRevealed: Boolean = true): NightProgress {
            require(round >= 1) { "Nights start at 1" }
            require(order.sorted() == NightStep.entries) { "order must contain every step exactly once" }
            return NightProgress(round, order, position = -1, current = null, deadRevealed = deadRevealed).moveFrom(0, players)
        }
    }
}
