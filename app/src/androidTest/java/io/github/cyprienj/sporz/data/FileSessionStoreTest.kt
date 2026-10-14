package io.github.cyprienj.sporz.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.github.cyprienj.sporz.ui.gm.game.DesignCrew
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class FileSessionStoreTest {

    private val file = File(InstrumentationRegistry.getInstrumentation().targetContext.cacheDir, "session-test.json")

    @After
    fun deleteFile() {
        file.delete()
    }

    @Test
    fun noFileMeansNoSession() {
        assertNull(FileSessionStore(file).load())
    }

    @Test
    fun aSavedSessionIsReadBack() {
        val game = DesignCrew.game()
        val session = SavedSession(SetupDraft(), game, setOf(game.players.first().id))
        FileSessionStore(file).save(session)
        assertEquals(session, FileSessionStore(file).load())
    }

    @Test
    fun aDamagedFileIsIgnored() {
        file.writeText("{\"game\": 42")
        assertNull(FileSessionStore(file).load())
    }
}
