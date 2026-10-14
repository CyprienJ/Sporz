package io.github.cyprienj.sporz.accessibility

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.unit.Density
import androidx.test.platform.app.InstrumentationRegistry
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.junit4.accessibility.enableAccessibilityChecks
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.tryPerformAccessibilityChecks
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResult.AccessibilityCheckResultType
import com.google.android.apps.common.testing.accessibility.framework.integrations.espresso.AccessibilityValidator
import io.github.cyprienj.sporz.data.GameSession
import io.github.cyprienj.sporz.data.history.FinishedGame
import io.github.cyprienj.sporz.engine.day.VoteOption
import io.github.cyprienj.sporz.engine.model.GameMode
import io.github.cyprienj.sporz.engine.model.NightStep
import io.github.cyprienj.sporz.engine.night.DoctorsDecision
import io.github.cyprienj.sporz.engine.night.MutantsDecision
import io.github.cyprienj.sporz.engine.setup.SuggestedCompositions
import io.github.cyprienj.sporz.ui.end.GameEndRoute
import io.github.cyprienj.sporz.ui.end.GameEndViewModel
import io.github.cyprienj.sporz.ui.gm.game.DayRoute
import io.github.cyprienj.sporz.ui.gm.game.DayViewModel
import io.github.cyprienj.sporz.ui.gm.game.DesignCrew
import io.github.cyprienj.sporz.ui.gm.game.DesignCrew.id
import io.github.cyprienj.sporz.ui.gm.game.JournalRoute
import io.github.cyprienj.sporz.ui.gm.game.JournalViewModel
import io.github.cyprienj.sporz.ui.gm.game.NightRoute
import io.github.cyprienj.sporz.ui.gm.game.NightViewModel
import io.github.cyprienj.sporz.ui.gm.game.PlayerBoardRoute
import io.github.cyprienj.sporz.ui.gm.game.PlayerBoardViewModel
import io.github.cyprienj.sporz.ui.gm.settings.SettingsRoute
import io.github.cyprienj.sporz.ui.gm.settings.SettingsViewModel
import io.github.cyprienj.sporz.ui.gm.setup.CompositionRoute
import io.github.cyprienj.sporz.ui.gm.setup.CompositionViewModel
import io.github.cyprienj.sporz.ui.gm.setup.DistributionRoute
import io.github.cyprienj.sporz.ui.gm.setup.DistributionViewModel
import io.github.cyprienj.sporz.ui.gm.setup.GameModeRoute
import io.github.cyprienj.sporz.ui.gm.setup.GameModeViewModel
import io.github.cyprienj.sporz.ui.gm.setup.LobbyRoute
import io.github.cyprienj.sporz.ui.gm.setup.LobbyViewModel
import io.github.cyprienj.sporz.ui.guide.RoleGuideScreen
import io.github.cyprienj.sporz.ui.history.RecentGamesScreen
import io.github.cyprienj.sporz.ui.home.HomeScreen
import io.github.cyprienj.sporz.ui.home.ResumableGame
import io.github.cyprienj.sporz.ui.home.ResumeTarget
import io.github.cyprienj.sporz.ui.theme.SporzTheme
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.rules.TestName
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File
import kotlin.random.Random

/**
 * Accessibility Test Framework checks (ticket C14) on every MJ screen, in both
 * themes: labels, touch targets of 48 dp, text contrast, duplicate descriptions.
 */
@RunWith(Parameterized::class)
class AccessibilityTest(private val darkTheme: Boolean, private val fontScale: Float) {

    companion object {
        /** Both themes, at normal size and at the largest system font size (200 %). */
        @JvmStatic
        @Parameterized.Parameters(name = "dark={0},font={1}")
        fun cases(): List<Array<Any>> = listOf(false, true).flatMap { dark -> listOf(1f, 2f).map { arrayOf<Any>(dark, it) } }
    }

    @get:Rule
    val rule = createComposeRule()

    @get:Rule
    val testName = TestName()

    private fun check(content: @Composable () -> Unit) {
        // Warnings fail too: 48 dp touch targets and text contrast are warnings in ATF.
        rule.enableAccessibilityChecks(
            AccessibilityValidator().setRunChecksFromRootView(true).setThrowExceptionFor(AccessibilityCheckResultType.WARNING),
        )
        rule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                SporzTheme(darkTheme = darkTheme) { content() }
            }
        }
        saveScreenshot() // first, so a failing screen can be looked at
        rule.onRoot().tryPerformAccessibilityChecks()
    }

    /** With `-e screenshots true`: one PNG per screen, for a visual check of large fonts. */
    private fun saveScreenshot() {
        val arguments = InstrumentationRegistry.getArguments()
        if (arguments.getString("screenshots") != "true") return
        val dir = arguments.getString("additionalTestOutputDir")?.let(::File)
            ?: InstrumentationRegistry.getInstrumentation().targetContext.getExternalFilesDir("a11y")!!
        val name = testName.methodName.replace(Regex("[^A-Za-z0-9=,.]"), "_")
        File(dir, "$name.png").outputStream().use { rule.onRoot().captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    private fun setupSession() = GameSession().apply {
        chooseMode(GameMode.Offline)
        repeat(10) { addSeat("Joueur ${it + 1}") }
        setComposition(SuggestedCompositions.forPlayers(10))
    }

    private val afterNight = DesignCrew.game()
        .playMutants(MutantsDecision.Mutate(id("Hugo")))
        .playDoctors(DoctorsDecision.Heal(listOf(id("Alice"), id("Jules"))))
        .playComputerScientist().playPsychologist(id("Chloé")).playGeneticist(id("Farid"))
        .playHacker(NightStep.Psychologist).playSpy(id("Hugo"))

    private fun gameSession(game: io.github.cyprienj.sporz.engine.game.GameState = afterNight) = GameSession().apply { resumeGame(game) }

    /** Guard: a 20 dp button without a label must fail, or the checks above prove nothing. */
    @Test
    fun checksAreActive() {
        val failure = runCatching { check { Box(Modifier.size(20.dp).clickable {}) } }.exceptionOrNull()
        assertNotNull("Accessibility checks did not run", failure)
    }

    @Test
    fun home() = check { HomeScreen(ResumableGame(ResumeTarget.Night, 2, 10), {}, {}, onJoinGame = null, {}, {}) }

    @Test
    fun gameMode() {
        val viewModel = GameModeViewModel(GameSession())
        check { GameModeRoute({}, {}, viewModel) }
    }

    @Test
    fun lobby() {
        val viewModel = LobbyViewModel(setupSession())
        check { LobbyRoute({}, {}, {}, viewModel) }
    }

    @Test
    fun composition() {
        val viewModel = CompositionViewModel(setupSession())
        check { CompositionRoute({}, {}, viewModel) }
    }

    @Test
    fun distribution() {
        val session = setupSession().apply { deal(Random(3)) }
        val viewModel = DistributionViewModel(session)
        check { DistributionRoute({}, viewModel) }
    }

    @Test
    fun settings() {
        val viewModel = SettingsViewModel(setupSession())
        check { SettingsRoute({}, viewModel) }
    }

    @Test
    fun night() {
        val viewModel = NightViewModel(gameSession(DesignCrew.game()))
        check { NightRoute({}, {}, {}, viewModel) }
    }

    @Test
    fun day() {
        val viewModel = DayViewModel(gameSession())
        check { DayRoute({}, {}, {}, {}, viewModel) }
    }

    @Test
    fun playerBoard() {
        val viewModel = PlayerBoardViewModel(gameSession())
        check { PlayerBoardRoute({}, {}, viewModel) }
    }

    @Test
    fun journal() {
        val viewModel = JournalViewModel(gameSession())
        check { JournalRoute({}, {}, viewModel) }
    }

    @Test
    fun gameEnd() {
        val ended = DesignCrew.game().playMutants(MutantsDecision.Kill(id("Hugo")))
            .playDoctors(DoctorsDecision.Heal(listOf(id("Alice"), id("Jules"))))
            .playComputerScientist().playPsychologist(id("Chloé")).playGeneticist(id("Farid"))
            .playHacker(NightStep.Psychologist).playSpy(id("Chloé"))
            .castVote(id("Alice"), VoteOption.For(id("Gaëlle"))).closeVote()
        val viewModel = GameEndViewModel(gameSession(ended))
        check { GameEndRoute({}, {}, {}, viewModel) }
    }

    @Test
    fun roleGuide() = check { RoleGuideScreen(onBack = {}) }

    @Test
    fun recentGames() = check { RecentGamesScreen(listOf(FinishedGame(1, 1_700_000_000_000, DesignCrew.game().abort())), {}, {}) }
}
