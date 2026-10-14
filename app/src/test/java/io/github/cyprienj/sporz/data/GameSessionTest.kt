package io.github.cyprienj.sporz.data

import io.github.cyprienj.sporz.engine.setup.SeatProblem
import io.github.cyprienj.sporz.engine.setup.SuggestedCompositions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.random.Random

class GameSessionTest {

    @Test
    fun `no setup until a mode is chosen`() {
        assertNull(GameSession().draft.value)
    }

    @Test
    fun `starting a setup keeps the current one`() {
        val session = GameSession()
        session.startSetup()
        assertEquals(SetupDraft(), session.draft.value)
        session.addSeat("Alice")
        session.startSetup()
        assertEquals(1, session.draft.value!!.seats.size)
    }

    @Test
    fun `discarding forgets the setup`() {
        val session = GameSession()
        session.startSetup()
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
    fun `a new game forgets the previous setup and deal`() {
        val session = GameSession()
        session.startSetup()
        repeat(7) { session.addSeat("P$it") }
        session.setComposition(SuggestedCompositions.forPlayers(7))
        val game = session.deal(Random(1))
        session.markAnnounced(game.players.first().id)

        session.startNewGame()
        assertNull(session.draft.value)
        assertNull(session.game.value)
        assertEquals(emptySet<Any>(), session.announced.value)
    }
}
