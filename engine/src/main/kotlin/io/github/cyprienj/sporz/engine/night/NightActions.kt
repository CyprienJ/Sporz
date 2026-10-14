package io.github.cyprienj.sporz.engine.night

import io.github.cyprienj.sporz.engine.model.Genome
import io.github.cyprienj.sporz.engine.model.Player
import io.github.cyprienj.sporz.engine.model.PlayerId
import io.github.cyprienj.sporz.engine.model.PlayerState
import io.github.cyprienj.sporz.engine.model.Role
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Final choice of the mutants for the night (R-NIGHT-6); the MJ settles
 * disagreements (R-NIGHT-9). [paralysed] does not play for the rest of the
 * night (R-NIGHT-22); null when the MJ records no paralysis.
 */
@Serializable
public sealed interface MutantsDecision {
    public val target: PlayerId
    public val paralysed: PlayerId?

    @Serializable @SerialName("mutate")
    public data class Mutate(override val target: PlayerId, override val paralysed: PlayerId? = null) : MutantsDecision

    @Serializable @SerialName("kill")
    public data class Kill(override val target: PlayerId, override val paralysed: PlayerId? = null) : MutantsDecision
}

/**
 * Final choice of the doctors, taken together since they are awake at the
 * same time (R-NIGHT-11); the MJ settles disagreements (R-NIGHT-12).
 */
@Serializable
public sealed interface DoctorsDecision {
    /** One heal per acting doctor ([healCount]), on different living players who are not awake doctors. */
    @Serializable @SerialName("heal")
    public data class Heal(val targets: List<PlayerId>) : DoctorsDecision

    @Serializable @SerialName("kill")
    public data class Kill(val target: PlayerId) : DoctorsDecision
}

/**
 * R-NIGHT-11: one heal per acting doctor, but never more than the players they can heal:
 * awake doctors are healthy and see each other, so they never heal one another.
 */
public fun healCount(players: List<Player>, actors: List<PlayerId>): Int =
    minOf(actors.size, players.count { it.alive && it.id !in actors })

/**
 * R-NIGHT-22: players the mutants can paralyse alongside [target]: living, not mutated,
 * and not the target. Empty when the target is the last such player (no paralysis then).
 */
public fun paralysable(players: List<Player>, target: PlayerId?): List<PlayerId> =
    players.filter { it.alive && !it.isMutated && it.id != target }.map { it.id }

/** Why a decision cannot be applied. */
@Serializable
public enum class ActionProblem {
    @SerialName("unknownPlayer") UnknownPlayer,
    @SerialName("targetDead") TargetDead,

    /** R-NIGHT-7, R-NIGHT-22 */
    @SerialName("targetAlreadyMutated") TargetAlreadyMutated,

    /** R-NIGHT-5 */
    @SerialName("selfTarget") SelfTarget,

    /** R-NIGHT-22: the mutants paralyse someone other than their target. */
    @SerialName("paralysedIsTarget") ParalysedIsTarget,

    /** R-NIGHT-11: as many heals as acting doctors. */
    @SerialName("wrongTargetCount") WrongTargetCount,

    /** R-NIGHT-11: a player cannot be healed twice in the same night. */
    @SerialName("duplicateTarget") DuplicateTarget,

    /** R-NIGHT-18: the hacker can only copy the psychologist, geneticist, computer scientist (and spy with the variant). */
    @SerialName("notHackable") NotHackable,
}

public class IllegalActionException(public val problem: ActionProblem, public val player: PlayerId?) :
    IllegalArgumentException("$problem${player?.let { " ($it)" } ?: ""}")

/** Players after a step, and what happened. */
@Serializable
public data class StepOutcome(val players: List<Player>, val events: List<NightEvent>)

/**
 * Applies the mutants' decision (R-NIGHT-6 to R-NIGHT-8, R-NIGHT-22).
 * Effects are immediate (R-NIGHT-2): a killed player is dead for the rest of
 * the night. The paralysis itself is applied by `NightProgress.paralysed`.
 */
public fun resolveMutants(players: List<Player>, decision: MutantsDecision): StepOutcome {
    val target = players.find(decision.target)
    if (!target.alive) throw IllegalActionException(ActionProblem.TargetDead, target.id)
    if (target.isMutated) throw IllegalActionException(ActionProblem.TargetAlreadyMutated, target.id)
    // Checked before the action: the mutants choose both at the same time.
    val paralysis = decision.paralysed?.let { id ->
        val paralysed = players.find(id)
        if (!paralysed.alive) throw IllegalActionException(ActionProblem.TargetDead, id)
        if (paralysed.isMutated) throw IllegalActionException(ActionProblem.TargetAlreadyMutated, id)
        if (id == target.id) throw IllegalActionException(ActionProblem.ParalysedIsTarget, id)
        NightEvent.Paralysis(id)
    }

    val action = when (decision) {
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
    return action.copy(events = action.events + listOfNotNull(paralysis))
}

/**
 * Applies the doctors' decision (R-NIGHT-11 to R-NIGHT-14, R-SET-12). [actors] are the
 * doctors woken for the step (see `NightStep.actors`).
 */
public fun resolveDoctors(players: List<Player>, actors: List<PlayerId>, decision: DoctorsDecision): StepOutcome =
    when (decision) {
        is DoctorsDecision.Heal -> {
            if (decision.targets.size != healCount(players, actors)) throw IllegalActionException(ActionProblem.WrongTargetCount, null)
            decision.targets.groupingBy { it }.eachCount().entries.firstOrNull { it.value > 1 }?.let {
                throw IllegalActionException(ActionProblem.DuplicateTarget, it.key)
            }
            // R-NIGHT-11: an awake doctor is healthy, so neither they nor the other doctors heal them.
            decision.targets.firstOrNull { it in actors }?.let { throw IllegalActionException(ActionProblem.SelfTarget, it) }

            val events = decision.targets.map { id ->
                val target = players.find(id)
                if (!target.alive) throw IllegalActionException(ActionProblem.TargetDead, target.id)
                val outcome = when {
                    !target.isMutated -> HealOutcome.NotMutated
                    target.role == Role.Traitor -> HealOutcome.Traitor
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

internal fun List<Player>.find(id: PlayerId): Player =
    firstOrNull { it.id == id } ?: throw IllegalActionException(ActionProblem.UnknownPlayer, id)

private fun List<Player>.replace(player: Player): List<Player> = map { if (it.id == player.id) player else it }
