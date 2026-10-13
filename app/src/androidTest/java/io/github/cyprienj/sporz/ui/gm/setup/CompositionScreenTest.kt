package io.github.cyprienj.sporz.ui.gm.setup

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.github.cyprienj.sporz.R
import io.github.cyprienj.sporz.data.GameSession
import io.github.cyprienj.sporz.engine.model.GameMode
import io.github.cyprienj.sporz.engine.model.Role
import io.github.cyprienj.sporz.ui.theme.SporzTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CompositionScreenTest {

    @get:Rule
    val rule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private fun session(players: Int) = GameSession().apply {
        chooseMode(GameMode.Offline)
        repeat(players) { addSeat("P$it") }
    }

    @Test
    fun designCompositionCanBeDealt() {
        val session = session(10)
        val viewModel = CompositionViewModel(session)
        var dealt = false
        rule.setContent { SporzTheme(darkTheme = false) { CompositionRoute(onBack = {}, onDealt = { dealt = true }, viewModel = viewModel) } }

        rule.onNodeWithText(context.resources.getQuantityString(R.plurals.composition_total, 10, 10, 10)).assertExists()
        // Adding a traitor takes an astronaut seat.
        val traitor = context.getString(R.string.role_traitor)
        rule.onNodeWithContentDescription(context.getString(R.string.composition_more, traitor)).performScrollTo().performClick()
        assertEquals(1, viewModel.composition.value.count(Role.Traitor))

        rule.onNodeWithText(context.getString(R.string.action_deal)).assertIsEnabled().performClick()
        assertEquals(true, dealt)
        assertNotNull(session.game.value)
    }

    @Test
    fun tooManyRolesBlocksTheDeal() {
        val session = session(10)
        CompositionViewModel(session) // composition for 10 players saved: 8 chosen roles
        repeat(3) { session.removeSeat(session.draft.value!!.seats.last().id) } // 7 players left
        val viewModel = CompositionViewModel(session)
        rule.setContent { SporzTheme(darkTheme = true) { CompositionRoute(onBack = {}, onDealt = {}, viewModel = viewModel) } }

        rule.onNodeWithText(context.resources.getQuantityString(R.plurals.composition_error_too_many_roles, 1, 1)).assertExists()
        rule.onNodeWithText(context.getString(R.string.action_deal)).assertIsNotEnabled()
    }
}
