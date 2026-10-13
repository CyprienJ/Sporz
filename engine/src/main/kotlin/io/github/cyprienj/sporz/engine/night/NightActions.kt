package io.github.cyprienj.sporz.engine.night

import io.github.cyprienj.sporz.engine.model.Genome
import io.github.cyprienj.sporz.engine.model.Player
import io.github.cyprienj.sporz.engine.model.PlayerId
import io.github.cyprienj.sporz.engine.model.PlayerState
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Final choice of the mutants for the night (R-NIGHT-6); the MJ settles disagreements (R-NIGHT-9). */
@Serializable
public sealed interface MutantsDecision {
    public val target: PlayerId

    @Serializable @SerialName("mutate")
    public data class Mutate(override val target: PlayerId) : MutantsDecision

    @Serializable @SerialName("kill")
    public data class Kill(override val target: PlayerId) : MutantsDecision
}

/**
 * Final choice of the doctors, taken together since they are awake at the
 * same time (R-NIGHT-11); the MJ settles disagreements (R-NIGHT-12).
 */
@Serializable
public sealed interface DoctorsDecision {
    /** One heal per acting doctor, on different living players. */
    @Serializable @SerialName("heal")
    public data class Heal(val targets: List<PlayerId>) : DoctorsDecision

    @Serializable @SerialName("kill")
    public data class Kill(val target: PlayerId) : DoctorsDecision
}

/** Why a decision cannot be applied. */
@Serializable
public enum class ActionProblem {
    @SerialName("unknownPlayer") UnknownPlayer,
    @SerialName("targetDead") TargetDead,

    /** R-NIGHT-7 */
    @SerialName("targetAlreadyMutated") TargetAlreadyMutated,

    /** R-NIGHT-5 */
    @SerialName("selfTarget") SelfTarget,

    /** R-NIGHT-11: as many heals as acting doctors. */
    @SerialName("wrongTargetCount") WrongTargetCount,

    /** R-NIGHT-11: a player cannot be healed twice in the same night. */
    @SerialName("duplicateTarget") DuplicateTarget,
}

public class IllegalActionException(public val problem: ActionProblem, public val player: PlayerId?) :
    IllegalArgumentException("$problem${player?.let { " ($it)" } ?: ""}")

/** Players after a step, and what happened. */
@Serializable
public data class StepOutcome(val players: List<Player>, val events: List<NightEvent>)

/**
 * Applies the mutants' decision (R-NIGHT-6 to R-NIGHT-8). Effects are
 * immediate (R-NIGHT-2): a killed player is dead for the rest of the night.
 */
public fun resolveMutants(players: List<Player>, decision: MutantsDecision): StepOutcome {
    val target = players.find(decision.target)
    if (!target.alive) throw IllegalActionException(ActionProblem.TargetDead, target.id)
    if (target.isMutated) throw IllegalActionException(ActionProblem.TargetAlreadyMutated, target.id)

    return when (decision) {
        is MutantsDecision.Mutate -> {
            val succeeded = target.genome != Genome.Resistant
            StepOutcome(
                players = if (succeeded) players.replace(target.copy(state = PlayerState.Mutated)) else players,
                events = listOf(NightEvent.Mutation(target.id, succeeded)),
            )
        }
        is MutantsDecision.Kill -> StepOutcome(
            players = players.replace(target.copy(alive = false)),
            events = listOf(NightEvent.Kill(target.id, Killer.Mutants)),
        )
    }
}

/**
 * Applies the doctors' decision (R-NIGHT-11 to R-NIGHT-14). [actors] are the
 * doctors woken for the step (see `NightStep.actors`).
 */
public fun resolveDoctors(players: List<Player>, actors: List<PlayerId>, decision: DoctorsDecision): StepOutcome =
    when (decision) {
        is DoctorsDecision.Heal -> {
            if (decision.targets.size != actors.size) throw IllegalActionException(ActionProblem.WrongTargetCount, null)
            decision.targets.groupingBy { it }.eachCount().entries.firstOrNull { it.value > 1 }?.let {
                throw IllegalActionException(ActionProblem.DuplicateTarget, it.key)
            }

            val events = decision.targets.map { id ->
                val target = players.find(id)
                if (!target.alive) throw IllegalActionException(ActionProblem.TargetDead, target.id)
                val outcome = when {
                    !target.isMutated -> HealOutcome.NotMutated
                    target.genome == Genome.Host -> HealOutcome.Host
                    else -> HealOutcome.Healed
                }
                NightEvent.Heal(target.id, outcome)
            }
            val healed = events.filter { it.outcome == HealOutcome.Healed }.map { it.target }.toSet()
            StepOutcome(
                players = players.map { if (it.id in healed) it.copy(state = PlayerState.Healthy) else it },
                events = events,
            )
        }
        is DoctorsDecision.Kill -> {
            val target = players.find(decision.target)
            if (target.id in actors) throw IllegalActionException(ActionProblem.SelfTarget, target.id)
            if (!target.alive) throw IllegalActionException(ActionProblem.TargetDead, target.id)
            StepOutcome(
                players = players.replace(target.copy(alive = false)),
                events = listOf(NightEvent.Kill(target.id, Killer.Doctors)),
            )
        }
    }

private fun List<Player>.find(id: PlayerId): Player =
    firstOrNull { it.id == id } ?: throw IllegalActionException(ActionProblem.UnknownPlayer, id)

private fun List<Player>.replace(player: Player): List<Player> = map { if (it.id == player.id) player else it }
