package io.github.cyprienj.sporz.data

import io.github.cyprienj.sporz.engine.model.GameMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GameSessionTest {

    @Test
    fun `no setup until a mode is chosen`() {
        assertNull(GameSession().draft.value)
    }

    @Test
    fun `choosing a mode starts a setup`() {
        val session = GameSession()
        session.chooseMode(GameMode.Offline)
        assertEquals(SetupDraft(mode = GameMode.Offline), session.draft.value)
    }

    @Test
    fun `changing the mode updates the current setup`() {
        val session = GameSession()
        session.chooseMode(GameMode.Offline)
        session.chooseMode(GameMode.SemiOnline)
        assertEquals(GameMode.SemiOnline, session.draft.value!!.mode)
    }

    @Test
    fun `discarding forgets the setup`() {
        val session = GameSession()
        session.chooseMode(GameMode.Offline)
        session.discardSetup()
        assertNull(session.draft.value)
    }
}
