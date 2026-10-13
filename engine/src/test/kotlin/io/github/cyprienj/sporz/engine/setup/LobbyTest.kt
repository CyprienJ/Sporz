package io.github.cyprienj.sporz.engine.setup

import io.github.cyprienj.sporz.engine.model.PlayerId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class LobbyTest {

    private val seats = listOf(Seat(PlayerId("a"), "Alice"), Seat(PlayerId("b"), "Bastien"))

    @Test
    fun `a new unique name is accepted`() {
        assertNull(Lobby.checkNewSeat("Chloé", seats))
    }

    @Test
    fun `names are unique ignoring case and spaces (R-SET-2)`() {
        assertEquals(SeatProblem.NameTaken, Lobby.checkNewSeat("  alice ", seats))
    }

    @Test
    fun `blank and too long names are refused`() {
        assertEquals(SeatProblem.BlankName, Lobby.checkNewSeat("   ", seats))
        assertEquals(SeatProblem.NameTooLong, Lobby.checkNewSeat("x".repeat(25), seats))
        assertNull(Lobby.checkNewSeat("x".repeat(24), seats))
    }

    @Test
    fun `at most 20 players (R-SET-1)`() {
        val full = (1..20).map { Seat(PlayerId("$it"), "P$it") }
        assertEquals(SeatProblem.LobbyFull, Lobby.checkNewSeat("Zoé", full))
    }

    @Test
    fun `renaming a player to their own name, other case, is fine`() {
        assertNull(Lobby.checkRename(PlayerId("a"), "ALICE", seats))
        assertEquals(SeatProblem.NameTaken, Lobby.checkRename(PlayerId("a"), "bastien", seats))
    }
}
