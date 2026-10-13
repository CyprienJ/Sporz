package io.github.cyprienj.sporz.data.history

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

/** A finished game of "Parties récentes" (DEC-12: kept on the MJ phone only). */
@Entity(tableName = "finished_games", indices = [Index("endedAt")])
data class FinishedGameEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** Epoch milliseconds. */
    val endedAt: Long,
    val playerCount: Int,
    /** `GameEnd` name. */
    val end: String,
    val round: Int,
    /** The whole `GameState` as JSON (EngineJson), for the end screen and journal. */
    val stateJson: String,
)

@Dao
interface FinishedGameDao {
    @Insert
    suspend fun insert(game: FinishedGameEntity): Long

    @Query("SELECT * FROM finished_games ORDER BY endedAt DESC")
    fun observeAll(): Flow<List<FinishedGameEntity>>

    @Query("SELECT * FROM finished_games WHERE id = :id")
    suspend fun get(id: Long): FinishedGameEntity?

    @Query("DELETE FROM finished_games WHERE id = :id")
    suspend fun delete(id: Long)
}

@Database(entities = [FinishedGameEntity::class], version = 1, exportSchema = false)
abstract class SporzDatabase : RoomDatabase() {
    abstract fun finishedGames(): FinishedGameDao
}
