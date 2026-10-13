package io.github.cyprienj.sporz.engine.setup

import io.github.cyprienj.sporz.engine.model.Composition
import io.github.cyprienj.sporz.engine.model.Role
import io.github.cyprienj.sporz.engine.model.Variants
import io.github.cyprienj.sporz.engine.setup.CompositionProblem.HackerWithoutTarget
import io.github.cyprienj.sporz.engine.setup.CompositionProblem.NegativeGenomeCount
import io.github.cyprienj.sporz.engine.setup.CompositionProblem.PlayerCountOutOfRange
import io.github.cyprienj.sporz.engine.setup.CompositionProblem.RoleCountOutOfRange
import io.github.cyprienj.sporz.engine.setup.CompositionProblem.TooManyGenomes
import io.github.cyprienj.sporz.engine.setup.CompositionProblem.TooManyRoles
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CompositionValidationTest {

    // Composition of design.html for 10 players.
    private val design = SuggestedCompositions.forPlayers(10)

    @Test
    fun `the design composition is valid with 2 astronauts`() {
        val check = design.check(10)
        assertTrue(check.isValid)
        assertEquals(emptyList<CompositionProblem>(), check.problems)
        assertEquals(2, check.astronauts)
    }

    @Test
    fun `player count must be 7 to 20 (R-SET-1)`() {
        val small = Composition(mapOf(Role.BaseMutant to 1, Role.Doctor to 2))
        assertEquals(listOf(PlayerCountOutOfRange(6, 7, 20)), small.check(6).problems)
        assertTrue(small.check(7).isValid)
        assertEquals(listOf(PlayerCountOutOfRange(21, 7, 20)), small.check(21).problems)
    }

    @Test
    fun `exactly one base mutant (section 2_2)`() {
        val none = design.copy(roles = design.roles - Role.BaseMutant)
        assertEquals(listOf(RoleCountOutOfRange(Role.BaseMutant, 0, 1, 1)), none.check(10).problems)
        val two = design.copy(roles = design.roles + (Role.BaseMutant to 2))
        assertTrue(RoleCountOutOfRange(Role.BaseMutant, 2, 1, 1) in two.check(10).problems)
    }

    @Test
    fun `one to three doctors (section 2_2)`() {
        assertFalse(design.copy(roles = design.roles - Role.Doctor).check(10).isValid)
        assertTrue(design.copy(roles = design.roles + (Role.Doctor to 3)).check(11).isValid)
        assertEquals(
            listOf(RoleCountOutOfRange(Role.Doctor, 4, 1, 3)),
            design.copy(roles = design.roles + (Role.Doctor to 4)).check(12).problems,
        )
    }

    @Test
    fun `single roles are 0 or 1 (section 2_2)`() {
        val twoSpies = design.copy(roles = design.roles + (Role.Spy to 2))
        assertEquals(listOf(RoleCountOutOfRange(Role.Spy, 2, 0, 1)), twoSpies.check(11).problems)
    }

    @Test
    fun `more roles than players is refused (R-SET-3)`() {
        val check = design.check(7)
        assertEquals(-1, check.astronauts)
        assertEquals(listOf(TooManyRoles(1)), check.problems)
    }

    @Test
    fun `hosts and resistants must fit among non mutant, non doctor players (R-SET-8)`() {
        // 7 players − base mutant − 2 doctors = 4 eligible.
        val base = Composition(mapOf(Role.BaseMutant to 1, Role.Doctor to 2))
        assertTrue(base.copy(extraHosts = 2, resistants = 2).check(7).isValid)
        assertEquals(listOf(TooManyGenomes(5, 4)), base.copy(extraHosts = 3, resistants = 2).check(7).problems)
    }

    @Test
    fun `negative genome counts are refused`() {
        assertEquals(listOf(NegativeGenomeCount), design.copy(resistants = -1).check(10).problems)
    }

    @Test
    fun `a hacker with nothing to hack is only a warning (R-SET-4)`() {
        val lonelyHacker = Composition(mapOf(Role.BaseMutant to 1, Role.Doctor to 2, Role.Hacker to 1, Role.Spy to 1))
        val check = lonelyHacker.check(8)
        assertTrue(check.isValid)
        assertEquals(listOf(HackerWithoutTarget), check.warnings)

        val spyHackable = lonelyHacker.copy(variants = Variants(hackerCanHackSpy = true))
        assertEquals(emptyList<CompositionProblem>(), spyHackable.check(8).problems)
    }

    @Test
    fun `all problems are reported at once`() {
        val bad = Composition(mapOf(Role.Doctor to 5), resistants = -2)
        val problems = bad.check(3).problems
        assertEquals(
            setOf(PlayerCountOutOfRange::class, RoleCountOutOfRange::class, TooManyRoles::class, NegativeGenomeCount::class),
            problems.map { it::class }.toSet(),
        )
        assertEquals(2, problems.count { it is RoleCountOutOfRange }) // no base mutant, 5 doctors
    }
}
