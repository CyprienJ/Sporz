package io.github.cyprienj.sporz.ui.home

import io.github.cyprienj.sporz.engine.day.VoteOption
import io.github.cyprienj.sporz.engine.model.NightStep
import io.github.cyprienj.sporz.engine.night.DoctorsDecision
import io.github.cyprienj.sporz.engine.night.MutantsDecision
import io.github.cyprienj.sporz.ui.gm.game.DesignCrew
import io.github.cyprienj.sporz.ui.gm.game.DesignCrew.id
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HomeViewModelTest {

    private val game = DesignCrew.game()
    private val everyone = game.players.map { it.id }.toSet()

    @Test
    fun `nothing to resume without a game`() {
        assertNull(HomeViewModel.resumableGame(null, emptySet()))
    }

    @Test
    fun `a deal with roles left to announce resumes at the distribution`() {
        assertEquals(ResumableGame(ResumeTarget.Distribution, 1, 10, played = false), HomeViewModel.resumableGame(game, everyone - id("Alice")))
    }

    @Test
    fun `a running game resumes at its phase`() {
        assertEquals(ResumableGame(ResumeTarget.Night, 1, 10, played = false), HomeViewModel.resumableGame(game, everyone))
        val day = game.playMutants(MutantsDecision.Mutate(id("Hugo")))
            .playDoctors(DoctorsDecision.Heal(listOf(id("Alice"), id("Jules"))))
            .playComputerScientist().playPsychologist(id("Chloé")).playGeneticist(id("Farid"))
            .playHacker(NightStep.Psychologist).playSpy(id("Hugo"))
        assertEquals(ResumableGame(ResumeTarget.Day, 1, 10), HomeViewModel.resumableGame(day, everyone))
        val night2 = day.castVote(id("Alice"), VoteOption.For(id("Hugo"))).closeVote().startNight()
        assertEquals(ResumableGame(ResumeTarget.Night, 2, 10), HomeViewModel.resumableGame(night2, everyone))
    }

    @Test
    fun `a finished game is not offered`() {
        assertNull(HomeViewModel.resumableGame(game.abort(), everyone))
    }
}
