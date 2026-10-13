package io.github.cyprienj.sporz.ui.gm.game

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.github.cyprienj.sporz.R
import io.github.cyprienj.sporz.data.GameSession
import io.github.cyprienj.sporz.engine.day.VoteOption
import io.github.cyprienj.sporz.engine.model.NightStep
import io.github.cyprienj.sporz.engine.night.DoctorsDecision
import io.github.cyprienj.sporz.engine.night.MutantsDecision
import io.github.cyprienj.sporz.ui.gm.game.DesignCrew.id
import io.github.cyprienj.sporz.ui.theme.SporzTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class JournalScreenTest {

    @get:Rule
    val rule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private fun text(id: Int, vararg args: Any) = context.getString(id, *args)

    @Test
    fun designNightAndVoteAreLogged() {
        // Night 1 and day 1 of the design's journal.
        val game = DesignCrew.game()
            .playMutants(MutantsDecision.Mutate(id("Hugo")))
            .playDoctors(DoctorsDecision.Heal(listOf(id("Alice"), id("Jules"))))
            .playComputerScientist().playPsychologist(id("Chloé")).playGeneticist(id("Farid"))
            .playHacker(NightStep.Psychologist).playSpy(id("Hugo"))
            .castVote(id("Alice"), VoteOption.For(id("Hugo"))).castVote(id("Bastien"), VoteOption.For(id("Hugo")))
            .closeVote()
        val viewModel = JournalViewModel(GameSession().apply { resumeGame(game) })
        rule.setContent { SporzTheme(darkTheme = false) { JournalRoute(onBackToPhase = {}, onOpenPlayerBoard = {}, viewModel = viewModel) } }

        rule.onNodeWithText(text(R.string.event_mutated, "Hugo")).assertExists()
        rule.onNodeWithText(context.resources.getQuantityString(R.plurals.report_mutant_count, 2, 2)).assertExists()
        rule.onNodeWithText(text(R.string.report_genome, "Farid", text(R.string.genome_resistant))).assertExists()
        rule.onNodeWithText(context.resources.getQuantityString(R.plurals.day_result_ejected, 2, "Hugo", 2)).assertExists()
        rule.onNodeWithText(text(R.string.day_result_revealed, text(R.string.role_astronaut), text(R.string.state_mutated))).assertExists()
        rule.onNodeWithText(text(R.string.journal_current)).assertExists()

        // Most recent first: day 1 above night 1.
        val day = rule.onNodeWithText(text(R.string.day_title, 1), ignoreCase = true).getUnclippedBoundsInRoot().top
        val night = rule.onNodeWithText(text(R.string.night_title, 1), ignoreCase = true).getUnclippedBoundsInRoot().top
        assertTrue(day < night)
    }

    @Test
    fun aFinishedGameEndsInItsLastDayWithNothingInProgress() {
        // Ended during day 1: the end entry joins the "Jour 1" section.
        val game = DesignCrew.game()
            .playMutants(MutantsDecision.Kill(id("Hugo")))
            .playDoctors(DoctorsDecision.Heal(listOf(id("Alice"), id("Jules"))))
            .playComputerScientist().playPsychologist(id("Chloé")).playGeneticist(id("Farid"))
            .playHacker(NightStep.Psychologist).playSpy(id("Chloé"))
            .castVote(id("Alice"), VoteOption.For(id("Gaëlle"))).closeVote()
        rule.setContent { SporzTheme(darkTheme = false) { JournalScreen(game, onBackToPhase = {}, onOpenPlayerBoard = null) } }

        rule.onNodeWithText(text(R.string.journal_end_healthy)).assertExists()
        rule.onAllNodesWithText(text(R.string.day_title, 1), ignoreCase = true).assertCountEquals(1)
        rule.onNodeWithText(text(R.string.journal_current)).assertDoesNotExist()
    }
}
