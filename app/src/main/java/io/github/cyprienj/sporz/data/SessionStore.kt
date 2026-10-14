package io.github.cyprienj.sporz.data

import android.content.Context
import android.util.Log
import androidx.core.util.AtomicFile
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.cyprienj.sporz.engine.EngineJson
import io.github.cyprienj.sporz.engine.game.GameState
import io.github.cyprienj.sporz.engine.model.PlayerId
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/** Everything [GameSession] needs to come back after the process was killed. */
@Serializable
data class SavedSession(
    val draft: SetupDraft? = null,
    val game: GameState? = null,
    val announced: Set<PlayerId> = emptySet(),
)

/** Where the current session is kept between launches (ticket C13). */
interface SessionStore {
    /** The saved session, or null if there is none or it cannot be read. */
    fun load(): SavedSession?

    fun save(session: SavedSession)

    /** Stores nothing: unit tests and previews. */
    object None : SessionStore {
        override fun load(): SavedSession? = null
        override fun save(session: SavedSession) = Unit
    }
}

/**
 * Keeps the session in a small JSON file. The file is read synchronously at
 * startup (a few kilobytes) so a screen restored after process death finds
 * its game; writes are atomic so a crash mid-write keeps the previous copy.
 */
@Singleton
class FileSessionStore(path: File) : SessionStore {

    @Inject
    constructor(@ApplicationContext context: Context) : this(File(context.filesDir, FILE_NAME))

    private val file = AtomicFile(path)

    override fun load(): SavedSession? = try {
        file.baseFile.takeIf { it.exists() }?.let { EngineJson.decodeFromString<SavedSession>(String(file.readFully())) }
    } catch (e: Exception) {
        // A save from an incompatible version, or a damaged file: start clean rather than crash.
        Log.w(TAG, "Saved session ignored", e)
        null
    }

    override fun save(session: SavedSession) {
        val out = try {
            file.startWrite()
        } catch (e: IOException) {
            Log.w(TAG, "Cannot save the session", e)
            return
        }
        try {
            out.write(EngineJson.encodeToString(session).toByteArray())
            file.finishWrite(out)
        } catch (e: IOException) {
            file.failWrite(out)
            Log.w(TAG, "Cannot save the session", e)
        }
    }

    companion object {
        const val FILE_NAME = "current_game.json"
        private const val TAG = "SessionStore"
    }
}
