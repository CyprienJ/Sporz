package io.github.cyprienj.sporz.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.cyprienj.sporz.MainActivity
import io.github.cyprienj.sporz.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SporzNavHostTest {

    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    private fun text(@StringRes id: Int) = rule.activity.getString(id)

    private fun click(@StringRes label: Int) {
        rule.onNodeWithText(text(label)).performScrollTo().performClick()
    }

    private fun assertOn(@StringRes title: Int) {
        // Screen titles are uppercase in the design.
        rule.onNodeWithText(text(title), ignoreCase = true).assertIsDisplayed()
    }

    private fun pressBack() {
        rule.runOnUiThread { rule.activity.onBackPressedDispatcher.onBackPressed() }
        rule.waitForIdle()
    }

    @Test
    fun gameMasterFlow_reachesNightThenBackLeavesTheGame() {
        assertOn(R.string.screen_home)
        click(R.string.action_create_game)
        assertOn(R.string.screen_game_mode)
        click(R.string.action_open_lobby)
        assertOn(R.string.screen_lobby)
        click(R.string.action_choose_roles)
        assertOn(R.string.screen_composition)
        click(R.string.action_deal)
        assertOn(R.string.screen_distribution)
        click(R.string.action_start_first_night)
        assertOn(R.string.screen_night)

        // Setup screens are no longer in the back stack.
        pressBack()
        assertOn(R.string.screen_home)
    }

    @Test
    fun nightAndDayReplaceEachOther() {
        click(R.string.action_create_game)
        click(R.string.action_open_lobby)
        click(R.string.action_choose_roles)
        click(R.string.action_deal)
        click(R.string.action_start_first_night)
        click(R.string.action_end_night)
        assertOn(R.string.screen_day)
        click(R.string.action_start_night)
        assertOn(R.string.screen_night)

        pressBack()
        assertOn(R.string.screen_home)
    }

    @Test
    fun playerFlow_joinWaitRoleVote() {
        click(R.string.action_join_game)
        assertOn(R.string.screen_join)
        click(R.string.action_join_this_game)
        assertOn(R.string.screen_waiting_room)
        click(R.string.action_role_dealt)
        assertOn(R.string.screen_my_role)
        click(R.string.action_vote_opened)
        assertOn(R.string.screen_vote)
        click(R.string.action_confirm)
        assertOn(R.string.screen_my_role)
    }

    @Test
    fun designCatalogOpensInDebugBuilds() {
        click(R.string.screen_design_catalog)
        assertOn(R.string.screen_design_catalog)
        pressBack()
        assertOn(R.string.screen_home)
    }

    @Test
    fun leavingTheWaitingRoomGoesHome() {
        click(R.string.action_join_game)
        click(R.string.action_join_this_game)
        click(R.string.action_leave_game)
        assertOn(R.string.screen_home)
    }
}
