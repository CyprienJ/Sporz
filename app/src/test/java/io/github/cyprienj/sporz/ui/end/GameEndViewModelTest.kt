package io.github.cyprienj.sporz.ui.end

import io.github.cyprienj.sporz.data.GameSession
import io.github.cyprienj.sporz.engine.GameEnd
import io.github.cyprienj.sporz.engine.day.VoteOption
import io.github.cyprienj.sporz.engine.game.GameState
import io.github.cyprienj.sporz.engine.game.Phase
import io.github.cyprienj.sporz.engine.model.NightStep
import io.github.cyprienj.sporz.engine.night.DoctorsDecision
import io.github.cyprienj.sporz.engine.night.MutantsDecision
import io.github.cyprienj.sporz.ui.gm.game.DesignCrew
import io.github.cyprienj.sporz.ui.gm.game.DesignCrew.id
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GameEndViewModelTest {

    /** The design's game: Hugo mutated then ejected day 1, Chloé mutated and healed night 2, Gaëlle ejected day 2. */
    private val designGame: GameState = DesignCrew.game()
        .playMutants(MutantsDecision.Mutate(id("Hugo")))
        .playDoctors(DoctorsDecision.Heal(listOf(id("Alice"), id("Jules"))))
        .playComputerScientist().playPsychologist(id("Chloé")).playGeneticist(id("Farid"))
        .playHacker(NightStep.Psychologist).playSpy(id("Hugo"))
        .castVote(id("Alice"), VoteOption.For(id("Hugo"))).closeVote().startNight()
        .playMutants(MutantsDecision.Mutate(id("Chloé")))
        .playDoctors(DoctorsDecision.Heal(listOf(id("Chloé"), id("Alice"))))
        .playComputerScientist().playPsychologist(id("Gaëlle")).playGeneticist(id("Inès"))
        .playHacker(NightStep.ComputerScientist).playSpy(id("Chloé"))
        .castVote(id("Alice"), VoteOption.For(id("Gaëlle"))).closeVote()

    @Test
    fun `the design game ends day 2 with the healthy camp winning`() {
        assertEquals(GameEnd.HealthyWin, designGame.end)
        assertEquals(Phase.Day, GameEndViewModel.endedIn(designGame))
        assertEquals(2, designGame.round)
    }

    @Test
    fun `fates of the design game`() {
        val fates = GameEndViewModel.fates(designGame)
        assertEquals(Fate.Ejected(1), fates[id("Hugo")])
        assertEquals(Fate.Ejected(2), fates[id("Gaëlle")])
        assertEquals(Fate.Healed(2), fates[id("Chloé")])
        assertEquals(Fate.Alive, fates[id("Farid")])
    }

    @Test
    fun `a night death is told apart`() {
        val game = DesignCrew.game().playMutants(MutantsDecision.Kill(id("Hugo")))
            .playDoctors(DoctorsDecision.Heal(listOf(id("Alice"), id("Jules"))))
            .playComputerScientist().playPsychologist(id("Chloé")).playGeneticist(id("Farid"))
            .playHacker(NightStep.Psychologist).playSpy(id("Chloé"))
        assertEquals(Fate.Dead(1), GameEndViewModel.fates(game)[id("Hugo")])
    }

    @Test
    fun `replay keeps the crew, composition and settings, forgets the game`() {
        val session = GameSession().apply { resumeGame(designGame) }
        GameEndViewModel(session).replaySameCrew()
        val draft = session.draft.value!!
        assertEquals(designGame.players.map { it.id }, draft.seats.map { it.id })
        assertEquals(designGame.composition, draft.composition)
        assertEquals(designGame.settings, draft.settings)
        assertNull(session.game.value)
        assertEquals(emptySet<Any>(), session.announced.value)
    }
}
