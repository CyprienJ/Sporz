package io.github.cyprienj.sporz.engine.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CompositionTest {

    // Composition of design.html for 10 players.
    private val design = Composition(
        roles = mapOf(
            Role.BaseMutant to 1, Role.Doctor to 2, Role.Psychologist to 1, Role.Geneticist to 1,
            Role.ComputerScientist to 1, Role.Hacker to 1, Role.Spy to 1,
        ),
        extraHosts = 1,
        resistants = 1,
    )

    @Test
    fun `astronauts fill the remaining seats (R-SET-3)`() {
        assertEquals(8, design.chosenRoleCount)
        assertEquals(2, design.astronauts(10))
    }

    @Test
    fun `astronauts can be negative when too many roles are chosen`() {
        assertEquals(-1, design.astronauts(7))
    }

    @Test
    fun `astronauts listed in the map are ignored`() {
        val withAstronauts = design.copy(roles = design.roles + (Role.Astronaut to 5))
        assertEquals(2, withAstronauts.astronauts(10))
    }

    @Test
    fun `default composition has the base mutant and two doctors`() {
        val default = Composition()
        assertEquals(1, default.count(Role.BaseMutant))
        assertEquals(2, default.count(Role.Doctor))
        assertEquals(0, default.count(Role.Spy))
    }

    @Test
    fun `default variants follow the rules table (section 6)`() {
        assertEquals(Variants(hackerCanHackSpy = false, revealOnElimination = true, blankVoteAllowed = true, captain = false), Variants())
    }

    @Test
    fun `role counts allowed by section 2_2`() {
        assertEquals(1..1, Role.BaseMutant.minCount..Role.BaseMutant.maxCount)
        assertEquals(1..3, Role.Doctor.minCount..Role.Doctor.maxCount)
        listOf(Role.Psychologist, Role.Geneticist, Role.ComputerScientist, Role.Hacker, Role.Spy, Role.Traitor).forEach {
            assertEquals(0..1, it.minCount..it.maxCount, it.name)
        }
    }
}
