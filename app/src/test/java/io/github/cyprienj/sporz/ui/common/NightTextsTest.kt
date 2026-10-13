package io.github.cyprienj.sporz.ui.common

import io.github.cyprienj.sporz.engine.game.GameState
import io.github.cyprienj.sporz.engine.model.Composition
import io.github.cyprienj.sporz.engine.model.GameSettings
import io.github.cyprienj.sporz.engine.model.Genome
import io.github.cyprienj.sporz.engine.model.NightStep
import io.github.cyprienj.sporz.engine.model.Player
import io.github.cyprienj.sporz.engine.model.PlayerId
import io.github.cyprienj.sporz.engine.model.PlayerState
import io.github.cyprienj.sporz.engine.model.Role
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NightTextsTest {

    private val players = listOf(
        Player(PlayerId("m"), "M", Role.BaseMutant, Genome.Host, PlayerState.Mutated),
        Player(PlayerId("d"), "D", Role.Doctor, Genome.Normal, PlayerState.Healthy),
        Player(PlayerId("p"), "P", Role.Psychologist, Genome.Normal, PlayerState.Healthy),
    ) + (1..4).map { Player(PlayerId("a$it"), "A$it", Role.Astronaut, Genome.Normal, PlayerState.Healthy) }
    private val game = GameState.fromDeal(
        players,
        Composition(mapOf(Role.BaseMutant to 1, Role.Doctor to 1, Role.Psychologist to 1)),
        GameSettings(),
    )

    @Test
    fun `skipped steps are shown for roles in the game, hidden for absent ones (R-NIGHT-3)`() {
        assertTrue(game.showsSkipped(NightStep.Mutants))
        assertTrue(game.showsSkipped(NightStep.Psychologist))
        assertFalse(game.showsSkipped(NightStep.Spy))
        assertFalse(game.showsSkipped(NightStep.Hacker))
    }
}
