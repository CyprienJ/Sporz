package io.github.cyprienj.sporz.engine.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PlayerTest {

    private fun player(role: Role = Role.Astronaut, state: PlayerState = PlayerState.Healthy, alive: Boolean = true) =
        Player(PlayerId("p"), "Chloé", role, Genome.Normal, state, alive)

    @Test
    fun `a healthy player is in the healthy camp`() {
        assertEquals(Camp.Healthy, player().camp)
    }

    @Test
    fun `a mutated player is in the mutant camp`() {
        assertEquals(Camp.Mutant, player(state = PlayerState.Mutated).camp)
    }

    @Test
    fun `the traitor is in the mutant camp, mutated or not (DEC-7, R-SET-12)`() {
        assertEquals(Camp.Mutant, player(Role.Traitor).camp)
        assertEquals(Camp.Mutant, player(Role.Traitor, PlayerState.Mutated).camp)
    }

    @Test
    fun `a dead player keeps their state (rules section 1)`() {
        val dead = player(state = PlayerState.Mutated, alive = false)
        assertTrue(dead.isMutated)
        assertEquals(Camp.Mutant, dead.camp)
    }

    @Test
    fun `only living healthy players can use their role power (R-NIGHT-4)`() {
        assertTrue(player(Role.Psychologist).hasRolePower)
        assertFalse(player(Role.Psychologist, PlayerState.Mutated).hasRolePower)
        assertFalse(player(Role.Psychologist, alive = false).hasRolePower)
    }
}
