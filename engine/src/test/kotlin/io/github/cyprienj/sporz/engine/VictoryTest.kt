package io.github.cyprienj.sporz.engine

import io.github.cyprienj.sporz.engine.model.Camp
import io.github.cyprienj.sporz.engine.model.Genome
import io.github.cyprienj.sporz.engine.model.Player
import io.github.cyprienj.sporz.engine.model.PlayerId
import io.github.cyprienj.sporz.engine.model.PlayerState
import io.github.cyprienj.sporz.engine.model.Role
import kotlinx.serialization.encodeToString
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class VictoryTest {

    private fun p(name: String, role: Role = Role.Astronaut, mutated: Boolean = false, alive: Boolean = true) =
        Player(PlayerId(name), name, role, Genome.Normal, if (mutated) PlayerState.Mutated else PlayerState.Healthy, alive)

    @Test
    fun `the game goes on while both camps are alive`() {
        assertNull(checkVictory(listOf(p("Alice"), p("Gaëlle", Role.BaseMutant, mutated = true))))
    }

    @Test
    fun `design end of game - healthy players retake the ship (R-WIN-1)`() {
        // Gaëlle (base mutant) ejected day 2, Chloé healed night 2: no mutant left.
        val players = listOf(
            p("Gaëlle", Role.BaseMutant, mutated = true, alive = false),
            p("Hugo", alive = false),
            p("Chloé"),
            p("Dorian", Role.Psychologist, alive = false),
            p("Alice", Role.Hacker),
        )
        assertEquals(GameEnd.HealthyWin, checkVictory(players))
        assertEquals(Camp.Healthy, GameEnd.HealthyWin.winner)
    }

    @Test
    fun `healing the last mutant wins for the healthy camp (R-WIN-1)`() {
        // The base mutant is dead; the only other mutant has just been healed.
        val players = listOf(p("Gaëlle", Role.BaseMutant, mutated = true, alive = false), p("Chloé"), p("Alice"))
        assertEquals(GameEnd.HealthyWin, checkVictory(players))
    }

    @Test
    fun `mutants win when every living player is in their camp (R-WIN-2)`() {
        val players = listOf(p("Gaëlle", Role.BaseMutant, mutated = true), p("Chloé", mutated = true), p("Alice", alive = false))
        assertEquals(GameEnd.MutantsWin, checkVictory(players))
        assertEquals(Camp.Mutant, GameEnd.MutantsWin.winner)
    }

    @Test
    fun `E-6 a living traitor does not prevent the healthy win (DEC-7)`() {
        // 3 healthy + traitor after ejecting the last mutant.
        val players = listOf(
            p("Alice"), p("Bastien", Role.Doctor), p("Chloé"),
            p("Hugo", Role.Traitor),
            p("Gaëlle", Role.BaseMutant, mutated = true, alive = false),
        )
        assertEquals(GameEnd.HealthyWin, checkVictory(players))
    }

    @Test
    fun `a mutated traitor must be eliminated like any mutant`() {
        val players = listOf(p("Alice"), p("Bastien", Role.Doctor), p("Hugo", Role.Traitor, mutated = true))
        assertNull(checkVictory(players))
    }

    @Test
    fun `mutants win with the traitor as the only other survivor (R-WIN-2)`() {
        val players = listOf(p("Gaëlle", Role.BaseMutant, mutated = true), p("Hugo", Role.Traitor), p("Alice", alive = false))
        assertEquals(GameEnd.MutantsWin, checkVictory(players))
    }

    @Test
    fun `the traitor alone wins for the mutants`() {
        val players = listOf(p("Hugo", Role.Traitor), p("Gaëlle", Role.BaseMutant, mutated = true, alive = false), p("Alice", alive = false))
        assertEquals(GameEnd.MutantsWin, checkVictory(players))
    }

    @Test
    fun `nobody alive means no winner (R-WIN-3)`() {
        val players = listOf(p("Alice", alive = false), p("Gaëlle", Role.BaseMutant, mutated = true, alive = false))
        assertEquals(GameEnd.NoSurvivors, checkVictory(players))
        assertNull(GameEnd.NoSurvivors.winner)
        assertNull(GameEnd.Aborted.winner)
    }

    @Test
    fun `end reasons use the API codes`() {
        assertEquals(
            listOf("allMutantsGone", "allHealthyGone", "noSurvivors", "aborted"),
            GameEnd.entries.map { EngineJson.encodeToString(it).trim('"') },
        )
    }
}
