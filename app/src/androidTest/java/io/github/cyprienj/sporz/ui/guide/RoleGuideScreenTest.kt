package io.github.cyprienj.sporz.ui.guide

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import io.github.cyprienj.sporz.R
import io.github.cyprienj.sporz.ui.theme.SporzTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoleGuideScreenTest {

    @get:Rule
    val rule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private fun text(id: Int) = context.getString(id)

    @Test
    fun cardsOpenOneAtATimeAndTabsSwitch() {
        var back = false
        rule.setContent { SporzTheme(darkTheme = false) { RoleGuideScreen(onBack = { back = true }) } }

        // Roles tab: closed cards show the tag, not the description.
        rule.onNodeWithText(text(R.string.guide_doctor)).assertDoesNotExist()
        rule.onNodeWithText(text(R.string.role_doctor), ignoreCase = true).performClick()
        rule.onNodeWithText(text(R.string.guide_doctor)).assertExists()
        rule.onNodeWithText(text(R.string.role_spy), ignoreCase = true).performClick()
        rule.onNodeWithText(text(R.string.guide_spy)).assertExists()
        rule.onNodeWithText(text(R.string.guide_doctor)).assertDoesNotExist()

        // Genomes and game tabs.
        rule.onNodeWithText(text(R.string.guide_tab_genomes)).performClick()
        rule.onNodeWithText(text(R.string.guide_resistant), ignoreCase = true).performClick()
        rule.onNodeWithText(text(R.string.guide_resistant_body)).assertExists()
        rule.onNodeWithText(text(R.string.guide_tab_game)).performClick()
        rule.onNodeWithText(text(R.string.guide_vote), ignoreCase = true).performClick()
        rule.onNodeWithText(text(R.string.guide_vote_body)).assertExists()

        rule.onNodeWithContentDescription(text(R.string.action_back)).performClick()
        assertTrue(back)
    }
}
