package io.github.cyprienj.sporz.ui.gm.game

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.github.cyprienj.sporz.R
import io.github.cyprienj.sporz.data.GameSession
import io.github.cyprienj.sporz.engine.game.Phase
import io.github.cyprienj.sporz.engine.model.NightStep
import io.github.cyprienj.sporz.engine.night.DoctorsDecision
import io.github.cyprienj.sporz.engine.night.MutantsDecision
import io.github.cyprienj.sporz.ui.gm.game.DesignCrew.id
import io.github.cyprienj.sporz.ui.theme.SporzTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DayScreenTest {

    @get:Rule
    val rule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private fun text(id: Int, vararg args: Any) = context.getString(id, *args)

    @Test
    fun enterVotesCloseAndStartTheNextNight() {
        val afterNight = DesignCrew.game()
            .playMutants(MutantsDecision.Kill(id("Hugo")))
            .playDoctors(DoctorsDecision.Heal(listOf(id("Alice"), id("Jules"))))
            .playComputerScientist().playPsychologist(id("Chloé")).playGeneticist(id("Chloé"))
            .playHacker(NightStep.Psychologist).playSpy(id("Chloé"))
        val session = GameSession().apply { resumeGame(afterNight) }
        val viewModel = DayViewModel(session)
        var nightStarted = false
        rule.setContent {
            SporzTheme(darkTheme = false) {
                DayRoute(onStartNight = { nightStarted = true }, onGameOver = {}, onOpenPlayerBoard = {}, onOpenJournal = {}, viewModel = viewModel)
            }
        }

        rule.onNodeWithText(text(R.string.day_title, 1), ignoreCase = true).assertExists()
        // Hugo died during the night and is revealed (default variant).
        rule.onNodeWithText(text(R.string.day_deaths, text(R.string.day_death_revealed, "Hugo", text(R.string.role_astronaut), text(R.string.state_healthy)))).assertExists()

        // Alice and Bastien vote for Chloé through the select boxes.
        listOf("Alice", "Bastien").forEach { voter ->
            rule.onNodeWithContentDescription(voter).performScrollTo().performClick()
            // The open menu is the last node; "Chloé" also appears as a row label and in filled select boxes.
            rule.onAllNodesWithText("Chloé").onLast().performClick()
        }
        assertEquals(2, session.game.value!!.day!!.vote.ballots.size)

        rule.onNodeWithText(text(R.string.day_close_vote)).performClick()
        rule.onNodeWithText(context.resources.getQuantityString(R.plurals.day_result_ejected, 2, "Chloé", 2)).performScrollTo().assertExists()

        rule.onNodeWithText(text(R.string.day_next_night, 2)).performClick()
        rule.waitForIdle()
        assertEquals(Phase.Night, session.game.value!!.phase)
        assertTrue(nightStarted)
    }
}
