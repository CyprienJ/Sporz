package io.github.cyprienj.sporz.data

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.github.cyprienj.sporz.data.history.FinishedGameEntity
import io.github.cyprienj.sporz.data.history.RoomGameHistory
import io.github.cyprienj.sporz.data.history.SporzDatabase
import io.github.cyprienj.sporz.ui.gm.game.DesignCrew
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomGameHistoryTest {

    private val database = Room.inMemoryDatabaseBuilder(
        InstrumentationRegistry.getInstrumentation().targetContext,
        SporzDatabase::class.java,
    ).build()
    private val history = RoomGameHistory(database.finishedGames())

    @After
    fun close() = database.close()

    @Test
    fun finishedGamesAreListedNewestFirst() = runBlocking {
        val first = DesignCrew.game().abort()
        val second = DesignCrew.game().copy(round = 2).abort()
        history.record(first, endedAt = 1_000)
        history.record(second, endedAt = 2_000)

        val games = history.games.first()
        assertEquals(listOf(2_000L, 1_000L), games.map { it.endedAt })
        assertEquals(second, games.first().game)
        assertEquals(first, history.get(games.last().id)!!.game)
    }

    @Test
    fun aDeletedGameIsGone() = runBlocking {
        history.record(DesignCrew.game().abort(), endedAt = 1_000)
        val id = history.games.first().single().id
        history.delete(id)
        assertEquals(emptyList<Any>(), history.games.first())
        assertNull(history.get(id))
    }

    @Test
    fun anUnreadableRowIsSkipped() = runBlocking {
        database.finishedGames().insert(FinishedGameEntity(endedAt = 1, playerCount = 10, end = "Aborted", round = 1, stateJson = "{}"))
        history.record(DesignCrew.game().abort(), endedAt = 2)
        assertEquals(listOf(2L), history.games.first().map { it.endedAt })
    }
}
