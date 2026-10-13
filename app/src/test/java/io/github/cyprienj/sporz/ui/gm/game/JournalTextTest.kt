package io.github.cyprienj.sporz.ui.gm.game

import io.github.cyprienj.sporz.engine.game.Phase
import org.junit.Assert.assertEquals
import org.junit.Test

class JournalTextTest {

    @Test
    fun `export lists each phase then its rows`() {
        val sections = listOf(
            JournalSection("Jour 1", Phase.Day, current = true, rows = listOf("Vote" to "Éjection de Hugo (5 voix)")),
            JournalSection("Nuit 1", Phase.Night, current = false, rows = listOf("Mutants" to "ont muté Hugo", "Médecins" to "Alice : rien à soigner")),
        )
        assertEquals(
            "JOUR 1\n• Vote : Éjection de Hugo (5 voix)\n\nNUIT 1\n• Mutants : ont muté Hugo\n• Médecins : Alice : rien à soigner",
            journalAsText(sections),
        )
    }
}
