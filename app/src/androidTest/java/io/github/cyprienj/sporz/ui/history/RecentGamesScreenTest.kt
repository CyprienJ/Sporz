package io.github.cyprienj.sporz.ui.history

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.github.cyprienj.sporz.R
import io.github.cyprienj.sporz.data.history.FinishedGame
import io.github.cyprienj.sporz.ui.end.GameEndScreen
import io.github.cyprienj.sporz.ui.gm.game.DesignCrew
import io.github.cyprienj.sporz.ui.gm.game.JournalScreen
import io.github.cyprienj.sporz.ui.theme.SporzTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RecentGamesScreenTest {

    @get:Rule
    val rule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private fun text(id: Int, vararg args: Any) = context.getString(id, *args)

    @Test
    fun emptyHistoryExplainsItself() {
        rule.setContent { SporzTheme { RecentGamesScreen(emptyList(), {}, {}) } }
        rule.onNodeWithText(text(R.string.recent_empty)).assertIsDisplayed()
    }

    @Test
    fun aFinishedGameIsListedAndOpens() {
        val game = DesignCrew.game().copy(round = 3).abort()
        var opened: Long? = null
        rule.setContent { SporzTheme { RecentGamesScreen(listOf(FinishedGame(42, 1_700_000_000_000, game)), {}, { opened = it }) } }
        rule.onNodeWithText(text(R.string.journal_end_aborted)).assertIsDisplayed().performClick()
        assertEquals(42L, opened)
        rule.onNodeWithText(text(R.string.night_title, 3), substring = true).assertIsDisplayed()
    }

    @Test
    fun aPastGameCanBeDeleted() {
        val game = DesignCrew.game().abort()
        var deleted = false
        rule.setContent {
            SporzTheme {
                GameEndScreen(game, {}, {}, {}, backLabel = text(R.string.action_back), onDelete = { deleted = true })
            }
        }
        rule.onNodeWithText(text(R.string.action_back)).assertIsDisplayed()
        rule.onNodeWithText(text(R.string.action_delete_game)).performClick()
        assertTrue(deleted)
    }

    @Test
    fun aPastJournalHasABackArrowInsteadOfTabs() {
        var back = false
        rule.setContent { SporzTheme { JournalScreen(DesignCrew.game().abort(), onBackToPhase = { back = true }, onOpenPlayerBoard = null) } }
        rule.onNodeWithText(text(R.string.tab_players)).assertDoesNotExist()
        rule.onNodeWithContentDescription(text(R.string.action_back)).performClick()
        assertTrue(back)
    }
}
