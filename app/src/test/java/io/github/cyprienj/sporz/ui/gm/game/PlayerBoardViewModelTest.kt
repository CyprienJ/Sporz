package io.github.cyprienj.sporz.ui.gm.game

import io.github.cyprienj.sporz.engine.day.VoteOption
import io.github.cyprienj.sporz.engine.model.NightStep
import io.github.cyprienj.sporz.engine.night.DoctorsDecision
import io.github.cyprienj.sporz.engine.night.MutantsDecision
import io.github.cyprienj.sporz.ui.gm.game.DesignCrew.id
import org.junit.Assert.assertEquals
import org.junit.Test

class PlayerBoardViewModelTest {

    /** Night 1: Chloé mutated, Hugo killed by the doctors; day 1: Jules ejected. */
    private val played = DesignCrew.game()
        .playMutants(MutantsDecision.Mutate(id("Chloé")))
        .playDoctors(DoctorsDecision.Kill(id("Hugo")))
        .playComputerScientist().playPsychologist(id("Chloé")).playGeneticist(id("Chloé"))
        .playHacker(NightStep.Psychologist).playSpy(id("Chloé"))
        .castVote(id("Alice"), VoteOption.For(id("Jules"))).closeVote()

    private fun names(filter: BoardFilter) = PlayerBoardViewModel.filtered(played.players, filter).map { it.name }

    @Test
    fun `filters`() {
        assertEquals(10, names(BoardFilter.All).size)
        assertEquals(8, names(BoardFilter.Alive).size)
        assertEquals(listOf("Chloé", "Gaëlle"), names(BoardFilter.Mutated))
        assertEquals(listOf("Hugo", "Jules"), names(BoardFilter.Eliminated))
    }

    @Test
    fun `ejected players are told apart from night deaths`() {
        assertEquals(setOf(id("Jules")), PlayerBoardViewModel.ejected(played))
    }
}
