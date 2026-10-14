package io.github.cyprienj.sporz.ui.gm.settings

import io.github.cyprienj.sporz.data.GameSession
import io.github.cyprienj.sporz.engine.model.GameSettings
import io.github.cyprienj.sporz.engine.model.NightStep
import io.github.cyprienj.sporz.engine.setup.SuggestedCompositions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import kotlin.random.Random

class SettingsViewModelTest {

    private val session = GameSession().apply { startSetup() }
    private val viewModel = SettingsViewModel(session)

    @Test
    fun `defaults`() {
        assertEquals(GameSettings(), viewModel.settings.value)
    }

    @Test
    fun `changing a toggle is saved`() {
        viewModel.toggle(SettingToggle.GuidedCall, false)
        assertFalse(viewModel.settings.value.guidedCall)
        assertEquals(viewModel.settings.value, session.draft.value!!.settings)
    }

    @Test
    fun `debate timer stays between off and 20 minutes`() {
        viewModel.setDebateTimer(-3)
        assertEquals(0, viewModel.settings.value.debateTimerMinutes)
        viewModel.setDebateTimer(99)
        assertEquals(20, viewModel.settings.value.debateTimerMinutes)
    }

    @Test
    fun `moving a step in the night order`() {
        val order = NightStep.DefaultOrder
        assertEquals(listOf(NightStep.Doctors, NightStep.Mutants) + order.drop(2), SettingsViewModel.move(order, 1, -1))
        assertEquals(order, SettingsViewModel.move(order, 0, -1))
        assertEquals(order, SettingsViewModel.move(order, order.lastIndex, 1))
    }

    @Test
    fun `settings changed during a game reach the running game`() {
        repeat(7) { session.addSeat("P$it") }
        session.setComposition(SuggestedCompositions.forPlayers(7))
        session.deal(Random(1))
        val reversed = NightStep.DefaultOrder.reversed()
        SettingsViewModel(session).setNightOrder(reversed)
        assertEquals(reversed, session.game.value!!.settings.nightOrder)
        assertEquals(reversed, SettingsViewModel(session).settings.value.nightOrder)
    }
}
