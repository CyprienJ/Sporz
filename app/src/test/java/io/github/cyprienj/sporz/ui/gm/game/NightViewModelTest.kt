package io.github.cyprienj.sporz.ui.gm.game

import io.github.cyprienj.sporz.data.GameSession
import io.github.cyprienj.sporz.engine.game.GameState
import io.github.cyprienj.sporz.engine.game.Phase
import io.github.cyprienj.sporz.engine.model.NightStep
import io.github.cyprienj.sporz.engine.model.PlayerState
import io.github.cyprienj.sporz.engine.night.Report
import io.github.cyprienj.sporz.ui.gm.game.DesignCrew.id
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NightViewModelTest {

    private val session = GameSession().apply { resumeGame(DesignCrew.game()) }
    private val viewModel = NightViewModel(session)
    private val game: GameState get() = session.game.value!!

    private fun pick(vararg names: String) = names.forEach { viewModel.toggleTarget(id(it)) }

    /** Mutants' step: one target and one paralysed player (R-NIGHT-22), then confirm. */
    private fun mutants(target: String, paralysed: String = if (target == "Hugo") "Chloé" else "Hugo") {
        pick(target)
        viewModel.toggleParalysed(id(paralysed))
        viewModel.confirm()
    }

    @Test
    fun `mutants target living healthy players only (R-NIGHT-7)`() {
        val eligible = NightViewModel.eligibleTargets(game, kill = false)
        assertFalse(id("Gaëlle") in eligible)
        assertEquals(9, eligible.size)
    }

    @Test
    fun `mutate then heal, the night moves on step by step`() {
        pick("Chloé")
        assertFalse(viewModel.canConfirm()) // the paralysis is still to choose
        viewModel.toggleParalysed(id("Hugo"))
        assertTrue(viewModel.canConfirm())
        viewModel.confirm()
        assertEquals(NightStep.Doctors, game.currentStep)
        assertEquals(null, viewModel.paralysed)
        assertEquals(PlayerState.Mutated, game.players.single { it.name == "Chloé" }.state)
        assertEquals(emptyList<Any>(), viewModel.targets)

        // Two doctors: two different players to heal.
        pick("Chloé")
        assertFalse(viewModel.canConfirm())
        pick("Alice", "Jules") // third pick ignored: only two heals
        assertEquals(listOf(id("Chloé"), id("Alice")), viewModel.targets)
        viewModel.confirm()
        assertEquals(PlayerState.Healthy, game.players.single { it.name == "Chloé" }.state)
        assertEquals(NightStep.ComputerScientist, game.currentStep)
    }

    @Test
    fun `doctors killing pick one player who is not a doctor (R-NIGHT-5)`() {
        mutants("Hugo")
        viewModel.chooseKill(true)
        val eligible = NightViewModel.eligibleTargets(game, kill = true)
        assertFalse(id("Bastien") in eligible || id("Emma") in eligible)
        assertEquals(1, NightViewModel.maxTargets(game, kill = true))
        pick("Gaëlle"); viewModel.confirm()
        assertFalse(game.players.single { it.name == "Gaëlle" }.alive)
    }

    @Test
    fun `information roles cannot designate themselves (R-NIGHT-5)`() {
        mutants("Hugo")
        pick("Chloé", "Alice"); viewModel.confirm()
        viewModel.confirm() // computer scientist: nothing to pick
        assertEquals(NightStep.Psychologist, game.currentStep)
        assertFalse(id("Dorian") in NightViewModel.eligibleTargets(game, kill = false))
    }

    @Test
    fun `a whole night ends at dawn with the reports delivered`() {
        mutants("Chloé")
        pick("Chloé", "Alice"); viewModel.confirm()
        viewModel.confirm()
        pick("Gaëlle"); viewModel.confirm()
        pick("Farid"); viewModel.confirm()
        assertFalse(viewModel.canConfirm())
        viewModel.selectHack(NightStep.Psychologist); viewModel.confirm()
        pick("Chloé"); viewModel.confirm()

        assertEquals(Phase.Day, game.phase)
        assertEquals(Report.State(id("Gaëlle"), PlayerState.Mutated), game.notebook(id("Dorian")).single().report)
    }

    @Test
    fun `mutants paralyse a living healthy player, who then sits out the night (R-NIGHT-22)`() {
        val eligible = NightViewModel.eligibleParalysis(game)
        assertFalse(id("Gaëlle") in eligible)
        assertEquals(9, eligible.size)

        viewModel.toggleParalysed(id("Bastien"))
        viewModel.toggleParalysed(id("Bastien")) // tapping again clears it
        assertEquals(null, viewModel.paralysed)
        mutants("Chloé", paralysed = "Bastien")
        assertEquals(listOf(id("Emma")), game.night!!.progress.current!!.actors)
        assertEquals(1, NightViewModel.maxTargets(game, kill = false))
    }

    @Test
    fun `hacker choices follow the variant (R-NIGHT-18)`() {
        assertEquals(listOf(NightStep.Psychologist, NightStep.Geneticist, NightStep.ComputerScientist), NightViewModel.hackChoices(game))
    }
}
