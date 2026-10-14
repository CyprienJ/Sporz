package io.github.cyprienj.sporz.ui.gm.setup

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.cyprienj.sporz.data.GameSession
import io.github.cyprienj.sporz.data.SetupDraft
import io.github.cyprienj.sporz.engine.Rules
import io.github.cyprienj.sporz.engine.model.PlayerId
import io.github.cyprienj.sporz.engine.setup.Seat
import io.github.cyprienj.sporz.engine.setup.SeatProblem
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/** Lobby (ticket C3): the MJ types the players' names. */
@HiltViewModel
class LobbyViewModel @Inject constructor(private val session: GameSession) : ViewModel() {

    init {
        // Reached without a setup (e.g. after process death): start one.
        if (session.draft.value == null) session.startSetup()
    }

    val draft: StateFlow<SetupDraft?> = session.draft

    /** Text of the "add a player" field. */
    var newName by mutableStateOf("")
        private set

    /** Why the last add was refused, cleared when the text changes. */
    var addProblem by mutableStateOf<SeatProblem?>(null)
        private set

    /** Player being renamed, with the edited name and its problem. */
    var renaming by mutableStateOf<Renaming?>(null)
        private set

    fun onNewNameChange(value: String) {
        newName = value
        addProblem = null
    }

    fun add() {
        addProblem = session.addSeat(newName)
        if (addProblem == null) newName = ""
    }

    fun remove(id: PlayerId) = session.removeSeat(id)

    fun startRenaming(seat: Seat) {
        renaming = Renaming(seat.id, seat.name)
    }

    fun onRenameChange(value: String) {
        renaming = renaming?.copy(name = value, problem = null)
    }

    fun confirmRename() {
        val current = renaming ?: return
        val problem = session.renameSeat(current.id, current.name)
        renaming = if (problem == null) null else current.copy(problem = problem)
    }

    fun cancelRename() {
        renaming = null
    }

    data class Renaming(val id: PlayerId, val name: String, val problem: SeatProblem? = null)

    companion object {
        /** Players still missing before the roles can be chosen (R-SET-1). */
        fun missingPlayers(seats: List<Seat>): Int = (Rules.MIN_PLAYERS - seats.size).coerceAtLeast(0)
    }
}
