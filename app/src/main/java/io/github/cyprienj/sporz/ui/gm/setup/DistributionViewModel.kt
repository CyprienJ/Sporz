package io.github.cyprienj.sporz.ui.gm.setup

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.cyprienj.sporz.data.GameSession
import io.github.cyprienj.sporz.engine.game.GameState
import io.github.cyprienj.sporz.engine.model.PlayerId
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/** Distribution (ticket C5): the MJ announces each offline player's role in private. */
@HiltViewModel
class DistributionViewModel @Inject constructor(private val session: GameSession) : ViewModel() {

    val game: StateFlow<GameState?> = session.game
    val announced: StateFlow<Set<PlayerId>> = session.announced

    /** The only role shown on screen, so two roles are never visible together. */
    var revealed by mutableStateOf<PlayerId?>(null)
        private set

    fun reveal(id: PlayerId) {
        revealed = id
    }

    fun hide() {
        revealed = null
    }

    fun confirmAnnounced(id: PlayerId) {
        session.markAnnounced(id)
        if (revealed == id) revealed = null
    }

    /** The night can start once every role is announced. */
    fun canStartNight(): Boolean {
        val players = game.value?.players ?: return false
        return players.all { it.id in announced.value }
    }
}
