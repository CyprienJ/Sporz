package io.github.cyprienj.sporz.engine.game

import io.github.cyprienj.sporz.engine.GameEnd
import io.github.cyprienj.sporz.engine.day.CaptainElection
import io.github.cyprienj.sporz.engine.day.VoteOption
import io.github.cyprienj.sporz.engine.day.VoteResult
import io.github.cyprienj.sporz.engine.model.NightStep
import io.github.cyprienj.sporz.engine.model.PlayerId
import io.github.cyprienj.sporz.engine.night.Absence
import io.github.cyprienj.sporz.engine.night.NightEvent
import io.github.cyprienj.sporz.engine.night.Report
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public enum class Phase {
    @SerialName("night") Night,
    @SerialName("day") Day,
    @SerialName("ended") Ended,
}

/** One line of the journal (MJ *Journal* screen, end of game "journal complet"). */
@Serializable
public data class JournalEntry(val round: Int, val phase: Phase, val event: JournalEvent)

@Serializable
public data class TallyLine(val option: VoteOption, val votes: Double)

/** What the journal records. The UI writes the sentence for each case. */
@Serializable
public sealed interface JournalEvent {
    /** Whether players may see it during the game (the MJ sees everything). */
    public val isPublic: Boolean get() = false

    @Serializable @SerialName("gameStarted")
    public data class GameStarted(val playerCount: Int) : JournalEvent {
        override val isPublic: Boolean get() = true
    }

    /** A night step was played: who acted, what happened, what they learned. */
    @Serializable @SerialName("stepPlayed")
    public data class StepPlayed(
        val step: NightStep,
        val actors: List<PlayerId>,
        val events: List<NightEvent> = emptyList(),
        val report: Report? = null,
    ) : JournalEvent

    /** R-NIGHT-3 */
    @Serializable @SerialName("stepSkipped")
    public data class StepSkipped(val step: NightStep) : JournalEvent

    /** R-NIGHT-3: called by the MJ although nobody could play it, so the table notices nothing. */
    @Serializable @SerialName("stepPretended")
    public data class StepPretended(val step: NightStep, val absent: List<Absence>) : JournalEvent

    /** R-NIGHT-18: the hacker's copy delivered at the end of the night. */
    @Serializable @SerialName("hackDelivered")
    public data class HackDelivered(val hacker: PlayerId, val report: Report.Hacked) : JournalEvent

    /** R-NIGHT-21: deaths announced at dawn, without saying how. */
    @Serializable @SerialName("dawn")
    public data class Dawn(val deaths: List<PlayerId>) : JournalEvent {
        override val isPublic: Boolean get() = true
    }

    /** R-DAY-4 */
    @Serializable @SerialName("captainElected")
    public data class CaptainElected(val election: CaptainElection) : JournalEvent {
        override val isPublic: Boolean get() = true
    }

    /** R-DAY-6 */
    @Serializable @SerialName("captainNamed")
    public data class CaptainNamed(val from: PlayerId, val to: PlayerId) : JournalEvent {
        override val isPublic: Boolean get() = true
    }

    /** R-DAY-9 to R-DAY-11. The tally is for the MJ; the result is public. */
    @Serializable @SerialName("voteClosed")
    public data class VoteClosed(val result: VoteResult, val tally: List<TallyLine>) : JournalEvent {
        override val isPublic: Boolean get() = true
    }

    @Serializable @SerialName("gameEnded")
    public data class GameEnded(val end: GameEnd) : JournalEvent {
        override val isPublic: Boolean get() = true
    }
}
