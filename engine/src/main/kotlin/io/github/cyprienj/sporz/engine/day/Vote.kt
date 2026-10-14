package io.github.cyprienj.sporz.engine.day

import io.github.cyprienj.sporz.engine.model.Player
import io.github.cyprienj.sporz.engine.model.PlayerId
import io.github.cyprienj.sporz.engine.model.PlayerState
import io.github.cyprienj.sporz.engine.model.Role
import io.github.cyprienj.sporz.engine.model.Variants
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** What a ballot can name (R-DAY-7). */
@Serializable
public sealed interface VoteOption {
    @Serializable @SerialName("player")
    public data class For(val target: PlayerId) : VoteOption

    @Serializable @SerialName("blank")
    public data object Blank : VoteOption
}

/** Why a ballot or a decision is refused. */
@Serializable
public enum class VoteProblem {
    @SerialName("unknownPlayer") UnknownPlayer,

    /** R-DAY-2 */
    @SerialName("voterDead") VoterDead,
    @SerialName("targetDead") TargetDead,

    /** R-DAY-7: blank votes need the variant. */
    @SerialName("blankNotAllowed") BlankNotAllowed,

    /** R-DAY-10: the captain must pick one of the tied options. */
    @SerialName("notATiedOption") NotATiedOption,

    /** R-DAY-6: the successor must be another living player. */
    @SerialName("invalidSuccessor") InvalidSuccessor,
}

public class IllegalVoteException(public val problem: VoteProblem, public val player: PlayerId? = null) :
    IllegalArgumentException("$problem${player?.let { " ($it)" } ?: ""}")

/** Ballots of the day's vote, changeable until it is closed (R-DAY-7). */
@Serializable
public data class Vote(val round: Int, val ballots: Map<PlayerId, VoteOption> = emptyMap()) {

    /** Records or replaces [voter]'s ballot. */
    public fun cast(players: List<Player>, voter: PlayerId, option: VoteOption, variants: Variants): Vote {
        if (!players.find(voter).alive) throw IllegalVoteException(VoteProblem.VoterDead, voter)
        when (option) {
            VoteOption.Blank -> if (!variants.blankVoteAllowed) throw IllegalVoteException(VoteProblem.BlankNotAllowed)
            is VoteOption.For -> if (!players.find(option.target).alive) throw IllegalVoteException(VoteProblem.TargetDead, option.target)
        }
        return copy(ballots = ballots + (voter to option))
    }

    /** Takes back [voter]'s ballot: they abstain unless they vote again (R-DAY-8). */
    public fun withdraw(voter: PlayerId): Vote = copy(ballots = ballots - voter)

    /**
     * Weighted count per option, most votes first. The captain's ballot counts
     * [CAPTAIN_VOTE_WEIGHT] with the captain variant (R-DAY-5).
     */
    public fun tally(captain: PlayerId?, variants: Variants): List<Pair<VoteOption, Double>> =
        ballots.entries
            .groupBy({ it.value }, { if (variants.captain && it.key == captain) CAPTAIN_VOTE_WEIGHT else 1.0 })
            .mapValues { it.value.sum() }
            .toList()
            .sortedByDescending { it.second }
}

/** R-DAY-5: weight of the captain's ballot (a multiple of 0.5, so sums stay exact). */
public const val CAPTAIN_VOTE_WEIGHT: Double = 1.5

/** What happens when the vote is closed (R-DAY-9 to R-DAY-11). */
@Serializable
public sealed interface VoteResult {
    /** R-DAY-11: [revealedRole] and [revealedState] are set with the reveal variant. */
    @Serializable @SerialName("ejected")
    public data class Ejected(
        val player: PlayerId,
        val votes: Double,
        val revealedRole: Role? = null,
        val revealedState: PlayerState? = null,
    ) : VoteResult

    /** R-DAY-9: blank came first. */
    @Serializable @SerialName("blankWins")
    public data object BlankWins : VoteResult

    /** R-DAY-10: tie for first place without a captain to break it. */
    @Serializable @SerialName("tie")
    public data class Tie(val options: List<VoteOption>) : VoteResult

    @Serializable @SerialName("noVotes")
    public data object NoVotes : VoteResult

    /** R-DAY-10: tie with a living captain, who must choose among [options]. Close again with their choice. */
    @Serializable @SerialName("captainMustChoose")
    public data class CaptainMustChoose(val options: List<VoteOption>) : VoteResult
}

/** Result of closing the vote, with the players updated (ejected player dead). */
@Serializable
public data class VoteClosing(val result: VoteResult, val players: List<Player>)

/**
 * Closes [vote] (R-DAY-9 to R-DAY-11). When the captain variant is on and the
 * first place is tied, the first call returns [VoteResult.CaptainMustChoose];
 * call again with [captainChoice] set to one of the tied options.
 */
public fun closeVote(
    players: List<Player>,
    vote: Vote,
    variants: Variants,
    captain: PlayerId? = null,
    captainChoice: VoteOption? = null,
): VoteClosing {
    val tally = vote.tally(captain, variants)
    if (tally.isEmpty()) return VoteClosing(VoteResult.NoVotes, players)

    val best = tally.first().second
    val leaders = tally.filter { it.second == best }.map { it.first }
    val winner = when {
        leaders.size == 1 -> leaders.single()
        variants.captain && captain != null && players.find(captain).alive -> {
            if (captainChoice == null) return VoteClosing(VoteResult.CaptainMustChoose(leaders), players)
            if (captainChoice !in leaders) throw IllegalVoteException(VoteProblem.NotATiedOption)
            captainChoice
        }
        else -> return VoteClosing(VoteResult.Tie(leaders), players)
    }

    return when (winner) {
        VoteOption.Blank -> VoteClosing(VoteResult.BlankWins, players)
        is VoteOption.For -> {
            val ejected = players.find(winner.target)
            val result = VoteResult.Ejected(
                player = ejected.id,
                votes = best,
                revealedRole = ejected.role.takeIf { variants.revealOnElimination },
                revealedState = ejected.state.takeIf { variants.revealOnElimination },
            )
            VoteClosing(result, players.map { if (it.id == ejected.id) it.copy(alive = false) else it })
        }
    }
}

internal fun List<Player>.find(id: PlayerId): Player =
    firstOrNull { it.id == id } ?: throw IllegalVoteException(VoteProblem.UnknownPlayer, id)
