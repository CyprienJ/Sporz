package io.github.cyprienj.sporz.ui.gm.setup

import io.github.cyprienj.sporz.data.GameSession
import io.github.cyprienj.sporz.engine.model.GameMode
import org.junit.Assert.assertEquals
import org.junit.Test

class GameModeViewModelTest {

    @Test
    fun `offline is selected by default`() {
        assertEquals(GameMode.Offline, GameModeViewModel(GameSession()).selected.value)
    }

    @Test
    fun `only offline can be chosen until the online milestone`() {
        val viewModel = GameModeViewModel(GameSession())
        assertEquals(setOf(GameMode.Offline), viewModel.availableModes)
        viewModel.select(GameMode.Online)
        assertEquals(GameMode.Offline, viewModel.selected.value)
    }

    @Test
    fun `confirming saves the mode in the session`() {
        val session = GameSession()
        GameModeViewModel(session).confirm()
        assertEquals(GameMode.Offline, session.draft.value!!.mode)
    }

    @Test
    fun `an existing setup's mode is preselected`() {
        val session = GameSession().apply { chooseMode(GameMode.Offline) }
        assertEquals(GameMode.Offline, GameModeViewModel(session).selected.value)
    }
}
