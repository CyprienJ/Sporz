package io.github.cyprienj.sporz.ui.end

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.github.cyprienj.sporz.R
import io.github.cyprienj.sporz.data.GameSession
import io.github.cyprienj.sporz.engine.day.VoteOption
import io.github.cyprienj.sporz.engine.model.NightStep
import io.github.cyprienj.sporz.engine.night.DoctorsDecision
import io.github.cyprienj.sporz.engine.night.MutantsDecision
import io.github.cyprienj.sporz.ui.gm.game.DesignCrew
import io.github.cyprienj.sporz.ui.gm.game.DesignCrew.id
import io.github.cyprienj.sporz.ui.theme.SporzTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameEndScreenTest {

    @get:Rule
    val rule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private fun text(id: Int, vararg args: Any) = context.getString(id, *args)

    @Test
    fun healthyWinShowsRevelationsAndReplays() {
        val game = DesignCrew.game()
            .playMutants(MutantsDecision.Kill(id("Hugo")))
            .playDoctors(DoctorsDecision.Heal(listOf(id("Alice"), id("Jules"))))
            .playComputerScientist().playPsychologist(id("Chloé")).playGeneticist(id("Farid"))
            .playHacker(NightStep.Psychologist).playSpy(id("Chloé"))
            .castVote(id("Alice"), VoteOption.For(id("Gaëlle"))).closeVote()
        val session = GameSession().apply { resumeGame(game) }
        val viewModel = GameEndViewModel(session)
        var replayed = false
        rule.setContent {
            SporzTheme(darkTheme = false) { GameEndRoute(onReplaySameCrew = { replayed = true }, onOpenJournal = {}, onBackHome = {}, viewModel = viewModel) }
        }

        rule.onNodeWithText(text(R.string.journal_end_healthy), ignoreCase = true).assertExists()
        rule.onNodeWithText(text(R.string.end_overline, text(R.string.day_title, 1)), ignoreCase = true).assertExists()
        rule.onNodeWithText(text(R.string.fate_ejected, 1)).assertExists()
        rule.onNodeWithText(text(R.string.fate_dead, 1)).assertExists()
        rule.onNodeWithText(text(R.string.end_role_genome, text(R.string.role_base_mutant), text(R.string.genome_host))).assertExists()

        rule.onNodeWithText(text(R.string.action_replay_same_crew)).performClick()
        assertTrue(replayed)
        assertEquals(10, session.draft.value!!.seats.size)
    }
}
