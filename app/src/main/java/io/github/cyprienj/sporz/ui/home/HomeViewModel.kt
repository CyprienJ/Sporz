package io.github.cyprienj.sporz.ui.home

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.cyprienj.sporz.data.GameSession
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val session: GameSession) : ViewModel() {

    /** A new game starts from a clean setup (resuming a game: ticket C13). */
    fun createGame() = session.startNewGame()
}
