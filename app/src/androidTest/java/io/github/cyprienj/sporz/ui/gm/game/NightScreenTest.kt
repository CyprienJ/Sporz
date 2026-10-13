package io.github.cyprienj.sporz.ui.gm.game

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.github.cyprienj.sporz.R
import io.github.cyprienj.sporz.data.GameSession
import io.github.cyprienj.sporz.engine.game.Phase
import io.github.cyprienj.sporz.engine.model.PlayerState
import io.github.cyprienj.sporz.ui.theme.SporzTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NightScreenTest {

    @get:Rule
    val rule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private fun text(id: Int, vararg args: Any) = context.getString(id, *args)

    /** Taps the target chip of [name] (chips are selectable; names also appear elsewhere). */
    private fun chip(name: String) = rule.onNode(isSelectable() and hasText(name)).performScrollTo().performClick()

    private fun confirm(label: String) = rule.onNodeWithText(label).performScrollTo().performClick()

    @Test
    fun designNightPlayedThroughTheScreen() {
        val session = GameSession().apply { resumeGame(DesignCrew.game()) }
        val viewModel = NightViewModel(session)
        var dayReached = false
        rule.setContent {
            SporzTheme(darkTheme = false) {
                NightRoute(onEndNight = { dayReached = true }, onOpenPlayerBoard = {}, onOpenJournal = {}, viewModel = viewModel)
            }
        }

        rule.onNodeWithText(text(R.string.night_title, 1), ignoreCase = true).assertExists()
        // The base mutant cannot be targeted by the mutants.
        rule.onNode(isSelectable() and hasText("Gaëlle")).assertIsNotEnabled()

        chip("Chloé"); confirm(text(R.string.night_confirm_mutate, "Chloé"))
        chip("Chloé"); chip("Alice"); confirm(text(R.string.night_confirm_heal, "Chloé, Alice"))
        confirm(text(R.string.night_confirm_info))
        chip("Gaëlle"); confirm(text(R.string.night_confirm_analyse, "Gaëlle"))
        chip("Farid"); confirm(text(R.string.night_confirm_analyse, "Farid"))
        chip(text(R.string.role_psychologist)); confirm(text(R.string.night_confirm_hack, text(R.string.role_psychologist)))
        chip("Chloé"); confirm(text(R.string.night_confirm_spy, "Chloé"))

        rule.waitForIdle()
        val game = session.game.value!!
        assertEquals(Phase.Day, game.phase)
        assertEquals(PlayerState.Healthy, game.players.single { it.name == "Chloé" }.state)
        assertTrue(dayReached)
    }
}
