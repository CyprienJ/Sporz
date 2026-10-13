package io.github.cyprienj.sporz.ui.gm.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.cyprienj.sporz.data.GameSession
import io.github.cyprienj.sporz.engine.day.IllegalVoteException
import io.github.cyprienj.sporz.engine.day.VoteOption
import io.github.cyprienj.sporz.engine.game.GameState
import io.github.cyprienj.sporz.engine.game.Phase
import io.github.cyprienj.sporz.engine.model.PlayerId
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import kotlin.random.Random

/** MJ day screen (ticket C8): announcement, offline votes, captain, closing, next night. */
@HiltViewModel
class DayViewModel @Inject constructor(private val session: GameSession) : ViewModel() {

    val game: StateFlow<GameState?> = session.game

    /** Captain votes entered by the MJ before the election (R-DAY-4): voter → candidate. */
    var captainBallots by mutableStateOf<Map<PlayerId, PlayerId>>(emptyMap())
        private set

    /** Option picked by the captain on a tie (R-DAY-10), or successor picked by a dead captain (R-DAY-6). */
    var captainPick by mutableStateOf<VoteOption?>(null)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    /** Enters [voter]'s ballot; null withdraws it ("Choisir…"). */
    fun setVote(voter: PlayerId, option: VoteOption?) = run {
        if (option == null) it.withdrawVote(voter) else it.castVote(voter, option)
    }

    fun resetVote() = run { it.resetVote() }

    fun setCaptainBallot(voter: PlayerId, candidate: PlayerId?) {
        captainBallots = if (candidate == null) captainBallots - voter else captainBallots + (voter to candidate)
    }

    fun electCaptain(random: Random = Random.Default) {
        run { it.electCaptain(captainBallots, random) }
        captainBallots = emptyMap()
    }

    fun pickForCaptain(option: VoteOption?) {
        captainPick = option
    }

    fun nameSuccessor() {
        val successor = (captainPick as? VoteOption.For)?.target ?: return
        run { it.nameNewCaptain(successor) }
        captainPick = null
    }

    /** Closes the vote; on a tie with a captain, call again once [captainPick] is chosen. */
    fun closeVote() {
        run { it.closeVote(captainPick) }
        captainPick = null
    }

    fun startNight() = run { it.startNight() }

    val isOver: Boolean get() = game.value?.phase == Phase.Ended

    private fun run(command: (GameState) -> GameState) {
        try {
            session.updateGame(command)
            error = null
        } catch (e: IllegalVoteException) {
            error = e.problem.name
        } catch (e: IllegalStateException) {
            error = e.message
        }
    }
}
