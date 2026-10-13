package io.github.cyprienj.sporz.engine.setup

import io.github.cyprienj.sporz.engine.model.Composition
import io.github.cyprienj.sporz.engine.model.Genome
import io.github.cyprienj.sporz.engine.model.PlayerId
import io.github.cyprienj.sporz.engine.model.PlayerState
import io.github.cyprienj.sporz.engine.model.Role
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.random.Random

class DealTest {

    private val names = listOf("Alice", "Bastien", "Chloé", "Dorian", "Emma", "Farid", "Gaëlle", "Hugo", "Inès", "Jules")
    private fun seats(n: Int = 10) = names.take(n).mapIndexed { i, name -> Seat(PlayerId("p$i"), name) }
    private val design = SuggestedCompositions.forPlayers(10)

    @Test
    fun `every role of the composition is dealt once, astronauts fill the rest`() {
        val players = deal(seats(), design, Random(1))
        val counts = players.groupingBy { it.role }.eachCount()
        assertEquals(
            mapOf(
                Role.BaseMutant to 1, Role.Doctor to 2, Role.Psychologist to 1, Role.Geneticist to 1,
                Role.ComputerScientist to 1, Role.Hacker to 1, Role.Spy to 1, Role.Astronaut to 2,
            ),
            counts,
        )
    }

    @Test
    fun `players keep their seat order, id and trimmed name`() {
        val seats = seats().mapIndexed { i, s -> if (i == 0) s.copy(name = "  Alice ") else s }
        val players = deal(seats, design, Random(2))
        assertEquals(seats.map { it.id }, players.map { it.id })
        assertEquals("Alice", players[0].name)
    }

    @ParameterizedTest
    @ValueSource(ints = [7, 10, 13, 16, 20])
    fun `genome and state rules hold for every suggested composition (R-SET-5 to R-SET-10)`(n: Int) {
        val composition = SuggestedCompositions.forPlayers(n)
        val allNames = (names + listOf("K", "L", "M", "N", "O", "P", "Q", "R", "S", "T")).take(n)
        repeat(50) { seed ->
            val players = deal(allNames.mapIndexed { i, s -> Seat(PlayerId("p$i"), s) }, composition, Random(seed))
            val base = players.single { it.role == Role.BaseMutant }
            assertEquals(Genome.Host, base.genome)
            assertEquals(PlayerState.Mutated, base.state)
            assertTrue(players.filter { it.role == Role.Doctor }.all { it.genome == Genome.Normal })
            assertTrue(players.filter { it.role != Role.BaseMutant }.all { it.state == PlayerState.Healthy })
            assertEquals(composition.extraHosts + 1, players.count { it.genome == Genome.Host })
            assertEquals(composition.resistants, players.count { it.genome == Genome.Resistant })
            assertTrue(players.all { it.alive })
        }
    }

    @Test
    fun `the same seed gives the same deal, another seed another one (R-SET-9)`() {
        assertEquals(deal(seats(), design, Random(42)), deal(seats(), design, Random(42)))
        assertNotEquals(deal(seats(), design, Random(42)), deal(seats(), design, Random(43)))
    }

    @Test
    fun `roles and genomes are spread fairly`() {
        val baseMutantSeat = IntArray(10)
        val resistantRoles = mutableSetOf<Role>()
        repeat(2000) { seed ->
            val players = deal(seats(), design, Random(seed))
            baseMutantSeat[players.indexOfFirst { it.role == Role.BaseMutant }]++
            players.filter { it.genome == Genome.Resistant }.forEach { resistantRoles += it.role }
        }
        // Expected 200 per seat; the bounds are far beyond normal variation.
        baseMutantSeat.forEachIndexed { seat, count -> assertTrue(count in 120..280, "seat $seat got the base mutant $count times") }
        // Resistant genome is independent of the role (R-SET-7), doctors and base mutant excluded.
        assertEquals(
            setOf(Role.Psychologist, Role.Geneticist, Role.ComputerScientist, Role.Hacker, Role.Spy, Role.Astronaut),
            resistantRoles,
        )
    }

    @Test
    fun `an invalid composition is refused with its problems`() {
        val error = assertThrows<InvalidCompositionException> { deal(seats(7), design, Random(0)) }
        assertEquals(listOf(CompositionProblem.TooManyRoles(1)), error.check.errors)
    }

    @Test
    fun `duplicate names are refused, ignoring case and spaces (R-SET-2)`() {
        val seats = seats().mapIndexed { i, s -> if (i == 1) s.copy(name = " alice") else s }
        assertThrows<IllegalArgumentException> { deal(seats, design, Random(0)) }
    }

    @Test
    fun `duplicate ids are refused`() {
        val seats = seats().mapIndexed { i, s -> if (i == 1) s.copy(id = PlayerId("p0")) else s }
        assertThrows<IllegalArgumentException> { deal(seats, design, Random(0)) }
    }

    @Test
    fun `a traitor is dealt healthy (R-SET-10)`() {
        val withTraitor = Composition(mapOf(Role.BaseMutant to 1, Role.Doctor to 2, Role.Traitor to 1))
        val traitor = deal(seats(8), withTraitor, Random(3)).single { it.role == Role.Traitor }
        assertEquals(PlayerState.Healthy, traitor.state)
    }
}
