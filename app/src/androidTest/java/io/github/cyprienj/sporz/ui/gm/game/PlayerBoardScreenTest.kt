package io.github.cyprienj.sporz.ui.gm.game

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.github.cyprienj.sporz.R
import io.github.cyprienj.sporz.data.GameSession
import io.github.cyprienj.sporz.engine.model.NightStep
import io.github.cyprienj.sporz.engine.night.DoctorsDecision
import io.github.cyprienj.sporz.engine.night.MutantsDecision
import io.github.cyprienj.sporz.ui.gm.game.DesignCrew.id
import io.github.cyprienj.sporz.ui.theme.SporzTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlayerBoardScreenTest {

    @get:Rule
    val rule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private fun text(id: Int) = context.getString(id)

    @Test
    fun filtersShowTheRightPlayers() {
        val game = DesignCrew.game()
            .playMutants(MutantsDecision.Kill(id("Hugo")))
            .playDoctors(DoctorsDecision.Heal(listOf(id("Alice"), id("Jules"))))
            .playComputerScientist().playPsychologist(id("Chloé")).playGeneticist(id("Chloé"))
            .playHacker(NightStep.Psychologist).playSpy(id("Chloé"))
        val viewModel = PlayerBoardViewModel(GameSession().apply { resumeGame(game) })
        rule.setContent { SporzTheme(darkTheme = false) { PlayerBoardRoute(onBackToPhase = {}, onOpenJournal = {}, viewModel = viewModel) } }

        rule.onNodeWithText("Hugo").assertExists()
        rule.onNodeWithText(text(R.string.badge_dead)).assertExists()

        rule.onNodeWithText(text(R.string.board_filter_mutated)).performClick()
        rule.onNodeWithText("Gaëlle").assertExists()
        rule.onNodeWithText("Hugo").assertDoesNotExist()

        rule.onNodeWithText(text(R.string.board_filter_eliminated)).performClick()
        rule.onNodeWithText("Hugo").assertExists()
        rule.onNodeWithText("Gaëlle").assertDoesNotExist()
    }
}
