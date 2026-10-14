package io.github.cyprienj.sporz.data

import io.github.cyprienj.sporz.data.history.FinishedGame
import io.github.cyprienj.sporz.data.history.GameHistory
import io.github.cyprienj.sporz.engine.GameEnd
import io.github.cyprienj.sporz.engine.EngineJson
import io.github.cyprienj.sporz.engine.game.GameState
import io.github.cyprienj.sporz.engine.setup.SuggestedCompositions
import io.github.cyprienj.sporz.engine.night.MutantsDecision
import io.github.cyprienj.sporz.ui.gm.game.DesignCrew
import io.github.cyprienj.sporz.ui.gm.game.DesignCrew.id
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.serialization.encodeToString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class GameSessionPersistenceTest {

    /** Keeps the last save, as JSON like the real file. */
    private class MemoryStore(var json: String? = null) : SessionStore {
        var saves = 0
        override fun load(): SavedSession? = json?.let { EngineJson.decodeFromString<SavedSession>(it) }
        override fun save(session: SavedSession) {
            saves++
            json = EngineJson.encodeToString(session)
        }
    }

    private class MemoryHistory : GameHistory {
        val recorded = mutableListOf<GameState>()
        override val games: Flow<List<FinishedGame>> = flowOf(emptyList())
        override suspend fun record(game: GameState, endedAt: Long) {
            recorded += game
        }
        override suspend fun get(id: Long): FinishedGame? = null
        override suspend fun delete(id: Long) = Unit
    }

    private fun session(store: SessionStore, history: GameHistory = MemoryHistory()) =
        GameSession(store, history, CoroutineScope(Dispatchers.Unconfined))

    @Test
    fun `nothing is written until something changes`() {
        val store = MemoryStore()
        session(store)
        assertEquals(0, store.saves)
    }

    @Test
    fun `a game survives the session being recreated`() {
        val store = MemoryStore()
        val first = session(store)
        first.startSetup()
        repeat(10) { first.addSeat("Joueur $it") }
        first.setComposition(SuggestedCompositions.forPlayers(10))
        val dealt = first.deal(Random(7))
        first.markAnnounced(dealt.players.first().id)

        val restored = session(store)
        assertEquals(first.draft.value, restored.draft.value)
        assertEquals(dealt, restored.game.value)
        assertEquals(setOf(dealt.players.first().id), restored.announced.value)
        assertTrue(restored.hasGameInProgress)
    }

    @Test
    fun `a new game replaces the saved one`() {
        val store = MemoryStore()
        val first = session(store)
        first.resumeGame(DesignCrew.game())
        first.startNewGame()

        val restored = session(store)
        assertNull(restored.game.value)
        assertNull(restored.draft.value)
    }

    @Test
    fun `an unreadable save starts an empty session`() {
        val store = object : SessionStore {
            override fun load(): SavedSession? = null
            override fun save(session: SavedSession) = Unit
        }
        assertNull(session(store).game.value)
    }

    @Test
    fun `a game is recorded in the history once, when it ends`() {
        val history = MemoryHistory()
        val session = session(MemoryStore(), history)
        session.resumeGame(DesignCrew.game())
        session.updateGame { it.abort() }
        assertEquals(listOf(GameEnd.Aborted), history.recorded.map { it.end })
        assertTrue(!session.hasGameInProgress)
    }

    @Test
    fun `replaying a past game starts a setup with its crew`() {
        val session = session(MemoryStore())
        val past = DesignCrew.game().abort()
        session.replay(past)
        assertEquals(past.players.map { it.name }, session.draft.value!!.seats.map { it.name })
        assertEquals(past.composition, session.draft.value!!.composition)
        assertNull(session.game.value)
    }

    @Test
    fun `a new game stops a played game in progress and keeps it as interrupted`() {
        val history = MemoryHistory()
        val session = session(MemoryStore(), history)
        session.resumeGame(DesignCrew.game().playMutants(MutantsDecision.Mutate(id("Hugo"))))
        session.startNewGame()
        assertEquals(listOf(GameEnd.Aborted), history.recorded.map { it.end })
        assertNull(session.game.value)
    }

    @Test
    fun `a deal never played is dropped without being recorded`() {
        val history = MemoryHistory()
        val session = session(MemoryStore(), history)
        session.resumeGame(DesignCrew.game())
        session.replay(DesignCrew.game().abort())
        assertEquals(emptyList<GameState>(), history.recorded)
    }

    @Test
    fun `a finished game is not recorded twice by a new game`() {
        val history = MemoryHistory()
        val session = session(MemoryStore(), history)
        session.resumeGame(DesignCrew.game().playMutants(MutantsDecision.Mutate(id("Hugo"))))
        session.updateGame { it.abort() }
        session.startNewGame()
        assertEquals(1, history.recorded.size)
    }
}
