package io.github.cyprienj.sporz.ui.gm.setup

import io.github.cyprienj.sporz.data.GameSession
import io.github.cyprienj.sporz.engine.game.Phase
import io.github.cyprienj.sporz.engine.model.Role
import io.github.cyprienj.sporz.engine.model.Variants
import io.github.cyprienj.sporz.engine.setup.CompositionProblem
import io.github.cyprienj.sporz.engine.setup.SuggestedCompositions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class CompositionViewModelTest {

    private fun session(players: Int) = GameSession().apply {
        startSetup()
        repeat(players) { addSeat("P$it") }
    }

    @Test
    fun `starts from the suggestion and saves it in the session`() {
        val session = session(10)
        val viewModel = CompositionViewModel(session)
        assertEquals(SuggestedCompositions.forPlayers(10), viewModel.composition.value)
        assertEquals(viewModel.composition.value, session.draft.value!!.composition)
        assertTrue(viewModel.check.isValid)
    }

    @Test
    fun `a composition already chosen is kept when coming back`() {
        val session = session(10)
        CompositionViewModel(session).remove(Role.Spy)
        assertEquals(0, CompositionViewModel(session).composition.value.count(Role.Spy))
    }

    @Test
    fun `plus needs a free seat and stays under the role maximum (R-SET-3)`() {
        val viewModel = CompositionViewModel(session(10))
        // 10-player suggestion: 2 astronauts left, spy already at its maximum of 1.
        assertFalse(viewModel.canAdd(Role.Spy))
        assertTrue(viewModel.canAdd(Role.Traitor))
        viewModel.add(Role.Traitor)
        viewModel.add(Role.Doctor)
        assertEquals(0, viewModel.check.astronauts)
        assertFalse(viewModel.canAdd(Role.Doctor))
        assertTrue(viewModel.check.isValid)
    }

    @Test
    fun `base mutant and astronauts have no stepper, doctors stay between 1 and 3`() {
        val viewModel = CompositionViewModel(session(10))
        assertFalse(viewModel.canAdd(Role.BaseMutant) || viewModel.canRemove(Role.BaseMutant))
        assertFalse(viewModel.canAdd(Role.Astronaut) || viewModel.canRemove(Role.Astronaut))
        viewModel.remove(Role.Doctor)
        assertFalse(viewModel.canRemove(Role.Doctor))
        assertEquals(1, viewModel.composition.value.count(Role.Doctor))
    }

    @Test
    fun `genomes fit among players who are neither base mutant nor doctor (R-SET-8)`() {
        val viewModel = CompositionViewModel(session(7)) // 7 - 1 - 2 doctors = 4 seats, 1 resistant suggested
        repeat(10) { viewModel.addHost() }
        assertEquals(3, viewModel.composition.value.extraHosts)
        assertFalse(viewModel.canAddGenome())
        // A third doctor would leave only 3 seats for 4 genomes.
        viewModel.remove(Role.Psychologist)
        assertFalse(viewModel.canAdd(Role.Doctor))
        viewModel.removeHost()
        assertTrue(viewModel.canAdd(Role.Doctor))
    }

    @Test
    fun `suggest keeps the variants`() {
        val viewModel = CompositionViewModel(session(12))
        viewModel.setVariants(Variants(captain = true))
        viewModel.remove(Role.Hacker)
        viewModel.suggest()
        assertEquals(SuggestedCompositions.forPlayers(12, Variants(captain = true)), viewModel.composition.value)
    }

    @Test
    fun `dealing starts the game`() {
        val session = session(10)
        assertNull(session.game.value)
        assertTrue(CompositionViewModel(session).deal(Random(1)))
        val game = session.game.value!!
        assertEquals(Phase.Night, game.phase)
        assertEquals(10, game.players.size)
        assertEquals(1, game.players.count { it.role == Role.BaseMutant })
    }

    @Test
    fun `an invalid composition is not dealt`() {
        val session = session(10)
        CompositionViewModel(session) // composition saved for 10 players
        repeat(3) { session.removeSeat(session.draft.value!!.seats.last().id) }
        val stale = CompositionViewModel(session) // 7 players now, composition for 10
        assertEquals(listOf(CompositionProblem.TooManyRoles(1)), stale.check.errors)
        assertFalse(stale.deal())
        assertNull(session.game.value)
    }
}
