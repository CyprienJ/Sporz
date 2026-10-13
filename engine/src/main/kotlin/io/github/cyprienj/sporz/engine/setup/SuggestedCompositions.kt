package io.github.cyprienj.sporz.engine.setup

import io.github.cyprienj.sporz.engine.Rules
import io.github.cyprienj.sporz.engine.model.Composition
import io.github.cyprienj.sporz.engine.model.Role
import io.github.cyprienj.sporz.engine.model.Variants

/**
 * Compositions of the *Suggérer* button (docs/rules.md §2.5). A starting
 * point, to be tuned after playtests: keep this table and the rules table in sync.
 */
public object SuggestedCompositions {

    private class Row(
        val doctors: Int,
        val psychologist: Int,
        val geneticist: Int,
        val computerScientist: Int,
        val hacker: Int,
        val spy: Int,
        val traitor: Int,
        val extraHosts: Int,
        val resistants: Int,
    )

    // Players → row of §2.5 (astronauts are computed).
    private val table = mapOf(
        7 to Row(2, 1, 0, 1, 0, 0, 0, 0, 1),
        8 to Row(2, 1, 1, 1, 0, 0, 0, 0, 1),
        9 to Row(2, 1, 1, 1, 1, 0, 0, 1, 1),
        10 to Row(2, 1, 1, 1, 1, 1, 0, 1, 1),
        11 to Row(2, 1, 1, 1, 1, 1, 0, 1, 1),
        12 to Row(2, 1, 1, 1, 1, 1, 0, 1, 1),
        13 to Row(2, 1, 1, 1, 1, 1, 1, 1, 2),
        14 to Row(2, 1, 1, 1, 1, 1, 1, 1, 2),
        15 to Row(2, 1, 1, 1, 1, 1, 1, 2, 2),
        16 to Row(3, 1, 1, 1, 1, 1, 1, 2, 2),
        17 to Row(3, 1, 1, 1, 1, 1, 1, 2, 2),
        18 to Row(3, 1, 1, 1, 1, 1, 1, 2, 3),
        19 to Row(3, 1, 1, 1, 1, 1, 1, 2, 3),
        20 to Row(3, 1, 1, 1, 1, 1, 1, 2, 3),
    )

    /**
     * Suggested composition for [playerCount] players, keeping the MJ's
     * [variants]. Counts outside 7..20 are clamped to the nearest row; the
     * composition check then reports the wrong player count.
     */
    public fun forPlayers(playerCount: Int, variants: Variants = Variants()): Composition {
        val row = table.getValue(playerCount.coerceIn(Rules.MIN_PLAYERS, Rules.MAX_PLAYERS))
        val roles = mapOf(
            Role.BaseMutant to 1,
            Role.Doctor to row.doctors,
            Role.Psychologist to row.psychologist,
            Role.Geneticist to row.geneticist,
            Role.ComputerScientist to row.computerScientist,
            Role.Hacker to row.hacker,
            Role.Spy to row.spy,
            Role.Traitor to row.traitor,
        ).filterValues { it > 0 }
        return Composition(roles, row.extraHosts, row.resistants, variants)
    }
}
