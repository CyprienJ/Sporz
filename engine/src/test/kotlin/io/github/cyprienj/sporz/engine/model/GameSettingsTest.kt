package io.github.cyprienj.sporz.engine.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class GameSettingsTest {

    @Test
    fun `default settings are the Assisted preset`() {
        assertEquals(GameSettings.forPreset(AutomationPreset.Assisted), GameSettings())
    }

    @Test
    fun `manual preset only keeps auto-hide of the role (design)`() {
        val manual = GameSettings.forPreset(AutomationPreset.Manual)
        assertTrue(manual.autoHideRole)
        assertFalse(manual.nightActionsOnPhone || manual.guidedCall || manual.vibrate || manual.autoReports ||
            manual.notifyMutation || manual.notebook || manual.turnReminder || manual.voteOnPhone ||
            manual.autoCloseVote || manual.liveTally)
    }

    @Test
    fun `automatic preset sends reports, notifies mutations and closes votes (design)`() {
        val auto = GameSettings.forPreset(AutomationPreset.Automatic)
        assertTrue(auto.autoReports && auto.notifyMutation && auto.autoCloseVote && auto.nightActionsOnPhone)
        assertFalse(auto.liveTally)
    }

    @Test
    fun `changing preset keeps the timer and the night order`() {
        val order = NightStep.DefaultOrder.reversed()
        val custom = GameSettings(debateTimerMinutes = 8, nightOrder = order)
        val manual = custom.withPreset(AutomationPreset.Manual)
        assertEquals(AutomationPreset.Manual, manual.preset)
        assertEquals(8, manual.debateTimerMinutes)
        assertEquals(order, manual.nightOrder)
    }

    @Test
    fun `default night order follows R-NIGHT-1`() {
        assertEquals(
            listOf(NightStep.Mutants, NightStep.Doctors, NightStep.ComputerScientist, NightStep.Psychologist,
                NightStep.Geneticist, NightStep.Hacker, NightStep.Spy),
            GameSettings().nightOrder,
        )
    }

    @Test
    fun `invalid timer or night order is refused`() {
        assertThrows<IllegalArgumentException> { GameSettings(debateTimerMinutes = 21) }
        assertThrows<IllegalArgumentException> { GameSettings(nightOrder = listOf(NightStep.Mutants)) }
        assertThrows<IllegalArgumentException> { GameSettings(nightOrder = NightStep.DefaultOrder + NightStep.Spy) }
    }
}
