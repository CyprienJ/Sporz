package io.github.cyprienj.sporz.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.cyprienj.sporz.data.GameSession
import io.github.cyprienj.sporz.engine.game.GameState
import io.github.cyprienj.sporz.engine.game.JournalEvent
import io.github.cyprienj.sporz.engine.game.Phase
import io.github.cyprienj.sporz.engine.model.PlayerId
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** Screen where a game in progress resumes. */
enum class ResumeTarget { Distribution, Night, Day }

/**
 * The game offered by "Reprendre la partie" (ticket C13). [played] is false for
 * a deal where nothing happened yet: stopping it does not keep it in the history.
 */
data class ResumableGame(val target: ResumeTarget, val round: Int, val players: Int, val played: Boolean = true)

@HiltViewModel
class HomeViewModel @Inject constructor(private val session: GameSession) : ViewModel() {

    val resumable: StateFlow<ResumableGame?> = combine(session.game, session.announced, ::resumableGame)
        .stateIn(viewModelScope, SharingStarted.Eagerly, resumableGame(session.game.value, session.announced.value))

    /** A new game starts from a clean setup; the game in progress is dropped. */
    fun createGame() = session.startNewGame()

    companion object {
        fun resumableGame(game: GameState?, announced: Set<PlayerId>): ResumableGame? {
            if (game == null) return null
            val target = when {
                game.phase == Phase.Ended -> return null
                // The first night only starts once every role is announced.
                game.round == 1 && !announced.containsAll(game.players.map { it.id }) -> ResumeTarget.Distribution
                game.phase == Phase.Night -> ResumeTarget.Night
                else -> ResumeTarget.Day
            }
            return ResumableGame(target, game.round, game.players.size, played = game.journal.any { it.event !is JournalEvent.GameStarted })
        }
    }
}
