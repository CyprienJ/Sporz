package io.github.cyprienj.sporz.data

import io.github.cyprienj.sporz.engine.model.Composition
import io.github.cyprienj.sporz.engine.model.GameMode
import io.github.cyprienj.sporz.engine.model.GameSettings
import io.github.cyprienj.sporz.engine.model.PlayerId
import io.github.cyprienj.sporz.engine.setup.Lobby
import io.github.cyprienj.sporz.engine.setup.SeatProblem
import io.github.cyprienj.sporz.engine.setup.Seat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/** A game being set up by the MJ, before the deal (game mode, lobby, composition, settings). */
data class SetupDraft(
    val mode: GameMode = GameMode.Offline,
    val seats: List<Seat> = emptyList(),
    val composition: Composition? = null,
    val settings: GameSettings = GameSettings(),
)

/**
 * The game of this phone, shared by the MJ screens. Kept in memory for now;
 * saving it across process death is ticket C13, the online source ticket E2.
 */
@Singleton
class GameSession @Inject constructor() {
    private val _draft = MutableStateFlow<SetupDraft?>(null)

    /** Null when no game is being set up. */
    val draft: StateFlow<SetupDraft?> = _draft.asStateFlow()

    /** Starts a setup with [mode], or changes the mode of the current one (players are kept). */
    fun chooseMode(mode: GameMode) {
        _draft.update { (it ?: SetupDraft()).copy(mode = mode) }
    }

    /** Adds an offline player; returns why it was refused, or null. */
    fun addSeat(name: String): SeatProblem? {
        val draft = current()
        Lobby.checkNewSeat(name, draft.seats)?.let { return it }
        _draft.value = draft.copy(seats = draft.seats + Seat(PlayerId(UUID.randomUUID().toString()), name.trim()))
        return null
    }

    /** Renames a player; returns why it was refused, or null. */
    fun renameSeat(id: PlayerId, name: String): SeatProblem? {
        val draft = current()
        Lobby.checkRename(id, name, draft.seats)?.let { return it }
        _draft.value = draft.copy(seats = draft.seats.map { if (it.id == id) it.copy(name = name.trim()) else it })
        return null
    }

    fun removeSeat(id: PlayerId) {
        _draft.update { draft -> draft?.copy(seats = draft.seats.filterNot { it.id == id }) }
    }

    private fun current(): SetupDraft = _draft.value ?: SetupDraft().also { _draft.value = it }

    fun discardSetup() {
        _draft.value = null
    }
}
