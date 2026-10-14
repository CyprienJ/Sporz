package io.github.cyprienj.sporz.ui.gm.setup

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.github.cyprienj.sporz.R
import io.github.cyprienj.sporz.data.GameSession
import io.github.cyprienj.sporz.ui.theme.SporzTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LobbyScreenTest {

    @get:Rule
    val rule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private fun text(id: Int, vararg args: Any) = context.getString(id, *args)

    private val session = GameSession().apply { startSetup() }

    private fun add(name: String) {
        rule.onNode(hasSetTextAction()).performTextInput(name)
        rule.onNodeWithContentDescription(text(R.string.action_add)).performClick()
    }

    @Test
    fun addRefuseDuplicateRemoveAndUnlockRoles() {
        val viewModel = LobbyViewModel(session)
        rule.setContent {
            SporzTheme(darkTheme = false) { LobbyRoute(onBack = {}, onChooseRoles = {}, onOpenSettings = {}, viewModel = viewModel) }
        }
        val chooseRoles = rule.onNodeWithText(text(R.string.action_choose_roles))
        chooseRoles.assertIsNotEnabled()

        add("Alice")
        add("alice")
        rule.onNodeWithText(text(R.string.lobby_error_taken)).assertExists()
        assertEquals(1, session.draft.value!!.seats.size)

        rule.onNodeWithContentDescription(text(R.string.lobby_remove, "Alice")).performClick()
        assertEquals(0, session.draft.value!!.seats.size)

        listOf("A", "B", "C", "D", "E", "F").forEach { add(it) }
        chooseRoles.assertIsNotEnabled()
        add("G")
        chooseRoles.assertIsEnabled()
    }
}
