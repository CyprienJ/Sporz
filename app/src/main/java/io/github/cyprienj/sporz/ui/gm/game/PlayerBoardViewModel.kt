package io.github.cyprienj.sporz.ui.gm.game

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.cyprienj.sporz.data.GameSession
import io.github.cyprienj.sporz.engine.day.VoteResult
import io.github.cyprienj.sporz.engine.game.GameState
import io.github.cyprienj.sporz.engine.game.JournalEvent
import io.github.cyprienj.sporz.engine.model.GameMode
import io.github.cyprienj.sporz.engine.model.Player
import io.github.cyprienj.sporz.engine.model.PlayerId
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

enum class BoardFilter { All, Alive, Mutated, Offline, Eliminated }

/** MJ player board (ticket C9). */
@HiltViewModel
class PlayerBoardViewModel @Inject constructor(session: GameSession) : ViewModel() {

    val game: StateFlow<GameState?> = session.game
    val mode: GameMode = session.draft.value?.mode ?: GameMode.Offline

    var filter by mutableStateOf(BoardFilter.All)
        private set

    fun select(filter: BoardFilter) {
        this.filter = filter
    }

    /** Filters shown: "Hors ligne" only when some players have a phone. */
    val filters: List<BoardFilter> =
        if (mode == GameMode.Offline) BoardFilter.entries - BoardFilter.Offline else BoardFilter.entries

    companion object {
        /** Players kept by [filter]; offline mode: every player is offline. */
        fun filtered(players: List<Player>, filter: BoardFilter): List<Player> = players.filter {
            when (filter) {
                BoardFilter.All, BoardFilter.Offline -> true
                BoardFilter.Alive -> it.alive
                BoardFilter.Mutated -> it.alive && it.isMutated
                BoardFilter.Eliminated -> !it.alive
            }
        }

        /** Players ejected by a vote (the others who died were killed at night). */
        fun ejected(game: GameState): Set<PlayerId> = game.journal
            .mapNotNull { ((it.event as? JournalEvent.VoteClosed)?.result as? VoteResult.Ejected)?.player }
            .toSet()
    }
}
