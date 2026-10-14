package io.github.cyprienj.sporz.ui.gm.setup

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.assertCountEquals
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.github.cyprienj.sporz.R
import io.github.cyprienj.sporz.data.GameSession
import io.github.cyprienj.sporz.engine.model.GameMode
import io.github.cyprienj.sporz.ui.common.nameRes
import io.github.cyprienj.sporz.ui.theme.SporzTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.random.Random

@RunWith(AndroidJUnit4::class)
class DistributionScreenTest {

    @get:Rule
    val rule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private fun text(id: Int, vararg args: Any) = context.getString(id, *args)

    @Test
    fun revealOneAtATimeThenStartTheNight() {
        val session = GameSession().apply {
            chooseMode(GameMode.Offline)
            listOf("Alice", "Bastien", "Chloé", "Dorian", "Emma", "Farid", "Gaëlle").forEach { addSeat(it) }
            CompositionViewModel(this).deal(Random(2))
        }
        val first = session.game.value!!.players.first()
        val viewModel = DistributionViewModel(session)
        var started = false
        rule.setContent { SporzTheme(darkTheme = false) { DistributionRoute(onStartFirstNight = { started = true }, viewModel = viewModel) } }

        val start = rule.onNodeWithText(text(R.string.action_start_first_night))
        start.assertIsNotEnabled()

        // Revealing the first player shows their role; revealing the second hides it again.
        rule.onAllNodesWithContentDescription(text(R.string.distribution_reveal_description, ""), substring = true)[0].performClick()
        rule.onNodeWithText(context.getString(first.role.nameRes)).assertExists()
        rule.onAllNodesWithText(text(R.string.action_hide)).assertCountEquals(1)
        rule.onAllNodesWithContentDescription(text(R.string.distribution_reveal_description, ""), substring = true)[0].performClick()
        rule.onAllNodesWithText(text(R.string.action_hide)).assertCountEquals(1)

        repeat(7) {
            rule.onAllNodesWithContentDescription(text(R.string.distribution_reveal_description, ""), substring = true).let { reveals ->
                runCatching { reveals[0].performScrollTo().performClick() }
            }
            rule.onNodeWithText(text(R.string.action_confirm_announced)).performScrollTo().performClick()
        }
        rule.onAllNodesWithText(text(R.string.distribution_announced)).assertCountEquals(7)
        start.assertIsEnabled().performClick()
        assertTrue(started)
    }
}
