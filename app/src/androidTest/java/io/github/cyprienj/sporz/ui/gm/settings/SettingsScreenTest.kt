package io.github.cyprienj.sporz.ui.gm.settings

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
import io.github.cyprienj.sporz.engine.model.NightStep
import io.github.cyprienj.sporz.ui.theme.SporzTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsScreenTest {

    @get:Rule
    val rule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private fun text(id: Int, vararg args: Any) = context.getString(id, *args)

    @Test
    fun offlineShowsOnlyWhatAppliesAndNightOrderCanBeChanged() {
        val session = GameSession().apply { chooseMode(GameMode.Offline) }
        val viewModel = SettingsViewModel(session)
        rule.setContent { SporzTheme(darkTheme = false) { SettingsRoute(onBack = {}, viewModel = viewModel) } }

        rule.onNodeWithText(text(R.string.settings_offline_note)).assertExists()
        rule.onNodeWithText(text(R.string.setting_guided_call)).assertExists()
        rule.onNodeWithText(text(R.string.setting_vote_on_phone)).assertDoesNotExist()
        rule.onNodeWithText(text(R.string.preset_manual)).assertDoesNotExist()

        // Move the doctors before the mutants, cancel once, then save.
        rule.onNodeWithText(text(R.string.setting_night_order)).performScrollTo().performClick()
        val doctors = text(R.string.night_step_doctors)
        rule.onNodeWithContentDescription(text(R.string.night_order_up, doctors)).performClick()
        rule.onNodeWithText(text(R.string.action_cancel)).performClick()
        assertEquals(NightStep.DefaultOrder, session.draft.value!!.settings.nightOrder)

        rule.onNodeWithText(text(R.string.setting_night_order)).performScrollTo().performClick()
        rule.onNodeWithContentDescription(text(R.string.night_order_up, doctors)).performClick()
        rule.onNodeWithText(text(R.string.action_save)).performClick()
        assertEquals(NightStep.Doctors, session.draft.value!!.settings.nightOrder.first())
    }

    @Test
    fun onlineShowsPresets() {
        val session = GameSession().apply { chooseMode(GameMode.SemiOnline) }
        val viewModel = SettingsViewModel(session)
        rule.setContent { SporzTheme(darkTheme = true) { SettingsRoute(onBack = {}, viewModel = viewModel) } }

        rule.onNodeWithText(text(R.string.preset_manual)).performClick()
        rule.onNodeWithText(text(R.string.preset_manual_description)).assertExists()
        rule.onNodeWithText(text(R.string.setting_vote_on_phone)).performScrollTo().assertExists()
        assertEquals(false, session.draft.value!!.settings.voteOnPhone)
    }
}
