package io.github.cyprienj.sporz.ui.gm.setup

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.cyprienj.sporz.BuildConfig
import io.github.cyprienj.sporz.data.GameSession
import io.github.cyprienj.sporz.engine.model.GameMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class GameModeViewModel @Inject constructor(private val session: GameSession) : ViewModel() {

    /** Online and semi-online need the backend: Milestone 2 (README, epics D and E). */
    val availableModes: Set<GameMode> = if (BuildConfig.ONLINE_PLAY) GameMode.entries.toSet() else setOf(GameMode.Offline)

    private val _selected = MutableStateFlow(
        session.draft.value?.mode?.takeIf { it in availableModes } ?: GameMode.Offline,
    )
    val selected: StateFlow<GameMode> = _selected.asStateFlow()

    fun select(mode: GameMode) {
        if (mode in availableModes) _selected.value = mode
    }

    /** Saves the choice before opening the lobby. */
    fun confirm() {
        session.chooseMode(_selected.value)
    }
}
