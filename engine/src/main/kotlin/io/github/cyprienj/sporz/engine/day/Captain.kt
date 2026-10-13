package io.github.cyprienj.sporz.engine.day

import io.github.cyprienj.sporz.engine.model.Player
import io.github.cyprienj.sporz.engine.model.PlayerId
import kotlinx.serialization.Serializable
import kotlin.random.Random

/** Outcome of the captain election; [drawn] is true when a tie was settled at random. */
@Serializable
public data class CaptainElection(val captain: PlayerId, val votes: Int, val drawn: Boolean)

/**
 * R-DAY-4: on Day 1, living players elect a captain by secret vote. Most
 * votes wins; on a tie the MJ draws at random among the tied players.
 *
 * @param ballots voter → candidate; abstentions are simply missing.
 * @return null when nobody voted.
 */
public fun electCaptain(players: List<Player>, ballots: Map<PlayerId, PlayerId>, random: Random): CaptainElection? {
    ballots.forEach { (voter, candidate) ->
        if (!players.find(voter).alive) throw IllegalVoteException(VoteProblem.VoterDead, voter)
        if (!players.find(candidate).alive) throw IllegalVoteException(VoteProblem.TargetDead, candidate)
    }
    val counts = ballots.values.groupingBy { it }.eachCount()
    val best = counts.values.maxOrNull() ?: return null
    // Sorted so that a given seed always draws the same player.
    val leaders = counts.filterValues { it == best }.keys.sortedBy { it.value }
    return CaptainElection(leaders.random(random), best, drawn = leaders.size > 1)
}

/** R-DAY-6: the dying captain names another living player. */
public fun passCaptaincy(players: List<Player>, from: PlayerId, to: PlayerId): PlayerId {
    if (to == from || !players.find(to).alive) throw IllegalVoteException(VoteProblem.InvalidSuccessor, to)
    return to
}
