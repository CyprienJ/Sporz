package io.github.cyprienj.sporz.engine.setup

import io.github.cyprienj.sporz.engine.model.Role
import io.github.cyprienj.sporz.engine.model.Variants
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.io.File

class SuggestedCompositionsTest {

    @ParameterizedTest
    @ValueSource(ints = [7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20])
    fun `every suggestion is valid without warnings`(players: Int) {
        val check = SuggestedCompositions.forPlayers(players).check(players)
        assertEquals(emptyList<CompositionProblem>(), check.problems, "$players players")
    }

    @Test
    fun `the 10 player suggestion is the composition of design_html`() {
        val ten = SuggestedCompositions.forPlayers(10)
        assertEquals(
            mapOf(
                Role.BaseMutant to 1, Role.Doctor to 2, Role.Psychologist to 1, Role.Geneticist to 1,
                Role.ComputerScientist to 1, Role.Hacker to 1, Role.Spy to 1,
            ),
            ten.roles,
        )
        assertEquals(1, ten.extraHosts)
        assertEquals(1, ten.resistants)
    }

    @Test
    fun `the MJ variants are kept`() {
        val variants = Variants(captain = true, blankVoteAllowed = false)
        assertEquals(variants, SuggestedCompositions.forPlayers(12, variants).variants)
    }

    @Test
    fun `out of range counts use the nearest row`() {
        assertEquals(SuggestedCompositions.forPlayers(7), SuggestedCompositions.forPlayers(3))
        assertEquals(SuggestedCompositions.forPlayers(20), SuggestedCompositions.forPlayers(30))
    }

    /** The table of docs/rules.md §2.5 and the code must not drift apart. */
    @Test
    fun `suggestions match the table of docs rules_md`() {
        val rules = File("../docs/rules.md").readText()
        val section = rules.substringAfter("### 2.5").substringBefore("\n---")
        val rows = section.lines()
            .filter { it.matches(Regex("""^\|\s*\d+\s*\|.*""")) }
            .map { line -> line.split('|').map { it.trim() }.filter { it.isNotEmpty() }.map { it.toInt() } }
        assertEquals(14, rows.size, "rows for 7 to 20 players")

        rows.forEach { r ->
            val players = r[0]
            val c = SuggestedCompositions.forPlayers(players)
            val actual = listOf(
                players, c.count(Role.Doctor), c.count(Role.Psychologist), c.count(Role.Geneticist),
                c.count(Role.ComputerScientist), c.count(Role.Hacker), c.count(Role.Spy), c.count(Role.Traitor),
                c.astronauts(players), c.extraHosts, c.resistants,
            )
            assertEquals(r, actual, "row for $players players (Players | Doctors | Psy | Gen | Info | Hacker | Spy | Traitor | Astronauts | Hosts | Resistants)")
            assertTrue(c.count(Role.BaseMutant) == 1)
        }
    }
}
