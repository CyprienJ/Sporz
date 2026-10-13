package io.github.cyprienj.sporz.ui.gm.game

import io.github.cyprienj.sporz.data.GameSession
import io.github.cyprienj.sporz.engine.GameEnd
import io.github.cyprienj.sporz.engine.day.VoteOption
import io.github.cyprienj.sporz.engine.day.VoteResult
import io.github.cyprienj.sporz.engine.game.GameState
import io.github.cyprienj.sporz.engine.game.Phase
import io.github.cyprienj.sporz.engine.model.GameSettings
import io.github.cyprienj.sporz.engine.model.NightStep
import io.github.cyprienj.sporz.engine.model.Variants
import io.github.cyprienj.sporz.engine.night.DoctorsDecision
import io.github.cyprienj.sporz.engine.night.MutantsDecision
import io.github.cyprienj.sporz.engine.setup.SuggestedCompositions
import io.github.cyprienj.sporz.ui.gm.game.DesignCrew.id
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class DayViewModelTest {

    /** Design crew after a night where the mutants killed [victim] and nothing else happened. */
    private fun dayAfterNight(variants: Variants = Variants(), victim: String = "Hugo"): GameState =
        GameState.fromDeal(DesignCrew.players, SuggestedCompositions.forPlayers(10, variants), GameSettings())
            .playMutants(MutantsDecision.Kill(id(victim)))
            .playDoctors(DoctorsDecision.Heal(listOf(id("Alice"), id("Jules"))))
            .playComputerScientist()
            .playPsychologist(id("Chloé"))
            .playGeneticist(id("Chloé"))
            .playHacker(NightStep.Psychologist)
            .playSpy(id("Chloé"))

    private fun viewModel(game: GameState): Pair<GameSession, DayViewModel> {
        val session = GameSession().apply { resumeGame(game) }
        return session to DayViewModel(session)
    }

    @Test
    fun `votes are entered, changed, withdrawn and reset`() {
        val (session, vm) = viewModel(dayAfterNight())
        vm.setVote(id("Alice"), VoteOption.For(id("Gaëlle")))
        vm.setVote(id("Alice"), VoteOption.Blank)
        vm.setVote(id("Bastien"), VoteOption.For(id("Chloé")))
        assertEquals(mapOf(id("Alice") to VoteOption.Blank, id("Bastien") to VoteOption.For(id("Chloé"))), session.game.value!!.day!!.vote.ballots)
        vm.setVote(id("Bastien"), null)
        assertEquals(1, session.game.value!!.day!!.vote.ballots.size)
        vm.resetVote()
        assertTrue(session.game.value!!.day!!.vote.ballots.isEmpty())
    }

    @Test
    fun `a dead player cannot vote, the refusal is shown`() {
        val (_, vm) = viewModel(dayAfterNight())
        vm.setVote(id("Hugo"), VoteOption.Blank)
        assertEquals("VoterDead", vm.error)
    }

    @Test
    fun `closing ejects, then the next night starts`() {
        val (session, vm) = viewModel(dayAfterNight())
        vm.setVote(id("Alice"), VoteOption.For(id("Chloé")))
        vm.closeVote()
        assertTrue(session.game.value!!.day!!.result is VoteResult.Ejected)
        vm.startNight()
        assertEquals(Phase.Night, session.game.value!!.phase)
        assertEquals(2, session.game.value!!.round)
    }

    @Test
    fun `ejecting the last mutant ends the game`() {
        val (session, vm) = viewModel(dayAfterNight())
        vm.setVote(id("Alice"), VoteOption.For(id("Gaëlle")))
        vm.closeVote()
        assertTrue(vm.isOver)
        assertEquals(GameEnd.HealthyWin, session.game.value!!.end)
    }

    @Test
    fun `captain elected from the MJ's ballots, breaks a tie`() {
        val (session, vm) = viewModel(dayAfterNight(Variants(captain = true)))
        vm.setCaptainBallot(id("Alice"), id("Emma"))
        vm.setCaptainBallot(id("Bastien"), id("Emma"))
        vm.electCaptain(Random(0))
        assertEquals(id("Emma"), session.game.value!!.captain)
        assertTrue(vm.captainBallots.isEmpty())

        vm.setVote(id("Alice"), VoteOption.For(id("Gaëlle")))
        vm.setVote(id("Bastien"), VoteOption.For(id("Chloé")))
        vm.closeVote()
        assertTrue(session.game.value!!.day!!.result is VoteResult.CaptainMustChoose)
        vm.pickForCaptain(VoteOption.For(id("Chloé")))
        vm.closeVote()
        assertFalse(session.game.value!!.players.single { it.name == "Chloé" }.alive)
    }

    @Test
    fun `a captain killed at night names a successor before the vote closes`() {
        val withCaptain = dayAfterNight(Variants(captain = true), victim = "Hugo")
            .electCaptain(mapOf(id("Alice") to id("Emma")), Random(0))
            .castVote(id("Alice"), VoteOption.Blank).closeVote().startNight()
            .playMutants(MutantsDecision.Kill(id("Emma")))
            .playDoctors(DoctorsDecision.Heal(listOf(id("Alice"))))
            .playComputerScientist().playPsychologist(id("Chloé")).playGeneticist(id("Chloé"))
            .playHacker(NightStep.Psychologist).playSpy(id("Chloé"))
        val (session, vm) = viewModel(withCaptain)
        assertEquals(id("Emma"), session.game.value!!.captainToReplace)
        vm.pickForCaptain(VoteOption.For(id("Jules")))
        vm.nameSuccessor()
        assertEquals(id("Jules"), session.game.value!!.captain)
        assertNull(session.game.value!!.captainToReplace)
    }
}
