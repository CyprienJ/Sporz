package io.github.cyprienj.sporz.data.history

import io.github.cyprienj.sporz.engine.EngineJson
import io.github.cyprienj.sporz.engine.game.GameState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import javax.inject.Inject
import javax.inject.Singleton

/** A finished game as listed in "Parties récentes". */
data class FinishedGame(val id: Long, val endedAt: Long, val game: GameState)

/** History of finished games (ticket C13). */
interface GameHistory {
    val games: Flow<List<FinishedGame>>
    suspend fun record(game: GameState, endedAt: Long = System.currentTimeMillis())
    suspend fun get(id: Long): FinishedGame?
    suspend fun delete(id: Long)

    /** Keeps nothing: unit tests and previews. */
    object None : GameHistory {
        override val games: Flow<List<FinishedGame>> = flowOf(emptyList())
        override suspend fun record(game: GameState, endedAt: Long) = Unit
        override suspend fun get(id: Long): FinishedGame? = null
        override suspend fun delete(id: Long) = Unit
    }
}

@Singleton
class RoomGameHistory @Inject constructor(private val dao: FinishedGameDao) : GameHistory {

    override val games: Flow<List<FinishedGame>> = dao.observeAll().map { rows -> rows.mapNotNull { it.toModel() } }

    override suspend fun record(game: GameState, endedAt: Long) {
        val end = checkNotNull(game.end) { "Only finished games are recorded" }
        dao.insert(
            FinishedGameEntity(
                endedAt = endedAt,
                playerCount = game.players.size,
                end = end.name,
                round = game.round,
                stateJson = EngineJson.encodeToString(game),
            ),
        )
    }

    override suspend fun get(id: Long): FinishedGame? = dao.get(id)?.toModel()

    override suspend fun delete(id: Long) = dao.delete(id)

    /** A row saved by an incompatible version is skipped rather than crashing the list. */
    private fun FinishedGameEntity.toModel(): FinishedGame? =
        runCatching { FinishedGame(id, endedAt, EngineJson.decodeFromString<GameState>(stateJson)) }.getOrNull()
}
