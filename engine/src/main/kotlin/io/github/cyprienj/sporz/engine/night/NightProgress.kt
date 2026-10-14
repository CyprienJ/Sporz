package io.github.cyprienj.sporz.engine.night

import io.github.cyprienj.sporz.engine.model.NightStep
import io.github.cyprienj.sporz.engine.model.Player
import io.github.cyprienj.sporz.engine.model.PlayerId
import io.github.cyprienj.sporz.engine.model.Role
import kotlinx.serialization.Serializable

/** A step being played, with the players woken for it. */
@Serializable
public data class StepCall(val step: NightStep, val actors: List<PlayerId>)

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
) {
    val isOver: Boolean get() = current == null

    /** True once [step] has been played or skipped this night. */
    public fun isFinished(step: NightStep): Boolean = isOver || order.indexOf(step) < position

    /** Moves to the next step somebody can play, skipping the others (R-NIGHT-3). */
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
            .map { StepCall(it, it.actors(players, paralysed)) }
            .filter { it.actors.isNotEmpty() }

    private fun moveFrom(start: Int, players: List<Player>): NightProgress {
        val newlySkipped = mutableListOf<NightStep>()
        for (index in start until order.size) {
            val step = order[index]
            val actors = step.actors(players, paralysed)
            if (actors.isNotEmpty()) {
                return copy(position = index, current = StepCall(step, actors), skipped = skipped + newlySkipped)
            }
            newlySkipped += step
        }
        return copy(position = order.size, current = null, skipped = skipped + newlySkipped)
    }

    public companion object {
        /** Starts night [round] (1 for the first night) with the call [order] of the settings. */
        public fun start(round: Int, order: List<NightStep>, players: List<Player>): NightProgress {
            require(round >= 1) { "Nights start at 1" }
            require(order.sorted() == NightStep.entries) { "order must contain every step exactly once" }
            return NightProgress(round, order, position = -1, current = null).moveFrom(0, players)
        }
    }
}
