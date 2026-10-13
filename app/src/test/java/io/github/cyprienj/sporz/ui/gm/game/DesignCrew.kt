package io.github.cyprienj.sporz.ui.gm.game

import io.github.cyprienj.sporz.engine.game.GameState
import io.github.cyprienj.sporz.engine.model.GameSettings
import io.github.cyprienj.sporz.engine.model.Genome
import io.github.cyprienj.sporz.engine.model.Player
import io.github.cyprienj.sporz.engine.model.PlayerId
import io.github.cyprienj.sporz.engine.model.PlayerState
import io.github.cyprienj.sporz.engine.model.Role
import io.github.cyprienj.sporz.engine.setup.SuggestedCompositions

/** The crew of design.html, dealt by hand for predictable tests. */
object DesignCrew {
    private fun p(name: String, role: Role, genome: Genome = Genome.Normal, state: PlayerState = PlayerState.Healthy) =
        Player(PlayerId(name), name, role, genome, state)

    val players = listOf(
        p("Alice", Role.Hacker),
        p("Bastien", Role.Doctor),
        p("Chloé", Role.Astronaut),
        p("Dorian", Role.Psychologist),
        p("Emma", Role.Doctor),
        p("Farid", Role.Spy, Genome.Resistant),
        p("Gaëlle", Role.BaseMutant, Genome.Host, PlayerState.Mutated),
        p("Hugo", Role.Astronaut),
        p("Inès", Role.ComputerScientist, Genome.Host),
        p("Jules", Role.Geneticist),
    )

    fun game(): GameState = GameState.fromDeal(players, SuggestedCompositions.forPlayers(10), GameSettings())

    fun id(name: String) = PlayerId(name)
}
