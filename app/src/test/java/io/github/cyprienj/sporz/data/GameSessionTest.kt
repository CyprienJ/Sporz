package io.github.cyprienj.sporz.data

import io.github.cyprienj.sporz.engine.model.GameMode
import io.github.cyprienj.sporz.engine.setup.SeatProblem
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

    @Test
    fun `players are added with trimmed names, renamed and removed`() {
        val session = GameSession()
        assertNull(session.addSeat("  Alice "))
        assertNull(session.addSeat("Bastien"))
        val alice = session.draft.value!!.seats.first()
        assertEquals("Alice", alice.name)

        assertNull(session.renameSeat(alice.id, "Alicia"))
        assertEquals(listOf("Alicia", "Bastien"), session.draft.value!!.seats.map { it.name })

        session.removeSeat(alice.id)
        assertEquals(listOf("Bastien"), session.draft.value!!.seats.map { it.name })
    }

    @Test
    fun `refused names leave the lobby unchanged`() {
        val session = GameSession()
        session.addSeat("Alice")
        assertEquals(SeatProblem.NameTaken, session.addSeat("alice"))
        assertEquals(SeatProblem.BlankName, session.renameSeat(session.draft.value!!.seats.single().id, " "))
        assertEquals(listOf("Alice"), session.draft.value!!.seats.map { it.name })
    }

    @Test
    fun `adding a player keeps the chosen mode`() {
        val session = GameSession()
        session.chooseMode(GameMode.Offline)
        session.addSeat("Alice")
        assertEquals(GameMode.Offline, session.draft.value!!.mode)
        session.chooseMode(GameMode.SemiOnline)
        assertEquals(1, session.draft.value!!.seats.size)
    }
}
