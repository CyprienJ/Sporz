package io.github.cyprienj.sporz.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
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
        val node = rule.onNodeWithText(text(label), ignoreCase = true)
        // Only screens with a scrollable list need scrolling (the home screen has none).
        runCatching { node.performScrollTo() }
        node.performClick()
    }

    private fun assertOn(@StringRes title: Int) {
        // Screen titles are uppercase in the design.
        rule.onNodeWithText(text(title), ignoreCase = true).assertIsDisplayed()
    }

    private fun assertOnHome() {
        rule.onNodeWithText(text(R.string.action_create_game), ignoreCase = true).assertIsDisplayed()
    }

    /** Fills the lobby with the 7 players needed to choose the roles. */
    private fun addSevenPlayers() {
        listOf("Alice", "Bastien", "Chloé", "Dorian", "Emma", "Farid", "Gaëlle").forEach { name ->
            rule.onNode(hasSetTextAction()).performTextInput(name)
            rule.onNodeWithContentDescription(text(R.string.action_add)).performClick()
        }
    }

    /** Reveals and confirms every role on the distribution screen. */
    private fun announceAllRoles() {
        repeat(7) {
            rule.onAllNodesWithText(text(R.string.action_reveal))[0].performScrollTo().performClick()
            rule.onNodeWithText(text(R.string.action_confirm_announced)).performScrollTo().performClick()
        }
    }

    private fun pressBack() {
        rule.runOnUiThread { rule.activity.onBackPressedDispatcher.onBackPressed() }
        rule.waitForIdle()
    }

    @Test
    fun gameMasterFlow_reachesNightThenBackLeavesTheGame() {
        assertOnHome()
        click(R.string.action_create_game)
        assertOn(R.string.mode_title)
        click(R.string.action_open_lobby)
        assertOn(R.string.screen_lobby)
        addSevenPlayers()
        click(R.string.action_choose_roles)
        assertOn(R.string.composition_title)
        click(R.string.action_deal)
        assertOn(R.string.distribution_title)
        announceAllRoles()
        click(R.string.action_start_first_night)
        rule.onNodeWithText(rule.activity.getString(R.string.night_title, 1), ignoreCase = true).assertIsDisplayed()

        // Setup screens are no longer in the back stack.
        pressBack()
        assertOnHome()

        // The game is still there (ticket C13).
        click(R.string.action_resume_game)
        rule.onNodeWithText(rule.activity.getString(R.string.night_title, 1), ignoreCase = true).assertIsDisplayed()

        // Creating a game asks before stopping this one; cancelling keeps it.
        pressBack()
        click(R.string.action_create_game)
        assertOn(R.string.confirm_new_game_title)
        click(R.string.action_cancel)
        rule.onNodeWithText(text(R.string.action_resume_game), ignoreCase = true).assertIsDisplayed()
        click(R.string.action_create_game)
        click(R.string.action_stop_and_create)
        assertOn(R.string.mode_title)
        pressBack()
        rule.onNodeWithText(text(R.string.action_resume_game), ignoreCase = true).assertDoesNotExist()
    }

    @Test
    fun recentGamesOpensFromHome() {
        click(R.string.screen_recent_games)
        assertOn(R.string.screen_recent_games)
        pressBack()
        assertOnHome()
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
        assertOnHome()
    }

    @Test
    fun leavingTheWaitingRoomGoesHome() {
        click(R.string.action_join_game)
        click(R.string.action_join_this_game)
        click(R.string.action_leave_game)
        assertOnHome()
    }
}
