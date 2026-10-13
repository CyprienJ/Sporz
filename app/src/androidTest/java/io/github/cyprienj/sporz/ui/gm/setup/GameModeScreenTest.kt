package io.github.cyprienj.sporz.ui.gm.setup

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.github.cyprienj.sporz.R
import io.github.cyprienj.sporz.engine.model.GameMode
import io.github.cyprienj.sporz.ui.theme.SporzTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameModeScreenTest {

    @get:Rule
    val rule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private fun text(id: Int) = context.getString(id)

    /** The selectable card whose title is [titleId]: the card merges its texts (title shown uppercase). */
    private fun card(titleId: Int) =
        rule.onNode(isSelectable() and hasText(text(titleId), substring = true, ignoreCase = true))

    @Test
    fun onlyOfflineCanBeChosen() {
        var selectedMode = GameMode.Offline
        var opened = false
        rule.setContent {
            SporzTheme(darkTheme = false) {
                GameModeScreen(selectedMode, setOf(GameMode.Offline), { selectedMode = it }, {}, { opened = true })
            }
        }

        card(R.string.mode_offline_title).assertIsSelected().assertIsEnabled()
        card(R.string.mode_semi_online_title).assertIsNotSelected().assertIsNotEnabled()
        card(R.string.mode_online_title).assertIsNotEnabled()
        rule.onAllNodesWithText(text(R.string.mode_coming_soon), ignoreCase = true).assertCountEquals(2)

        card(R.string.mode_online_title).performClick()
        assertEquals(GameMode.Offline, selectedMode)

        rule.onNodeWithText(text(R.string.action_open_lobby)).performClick()
        assertTrue(opened)
    }
}
