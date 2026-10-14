package io.github.cyprienj.sporz.engine.model

import io.github.cyprienj.sporz.engine.EngineJson
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class GameSettingsTest {

    @Test
    fun `a save with the old phone settings still loads`() {
        val old = """{"preset":"assisted","nightActionsOnPhone":true,"guidedCall":false,"voteOnPhone":true,"debateTimerMinutes":7,"nightOrder":["spy","mutants","doctors","computerScientist","psychologist","geneticist","hacker"]}"""
        val settings = EngineJson.decodeFromString<GameSettings>(old)
        assertFalse(settings.guidedCall)
        assertEquals(7, settings.debateTimerMinutes)
        assertEquals(NightStep.Spy, settings.nightOrder.first())
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
