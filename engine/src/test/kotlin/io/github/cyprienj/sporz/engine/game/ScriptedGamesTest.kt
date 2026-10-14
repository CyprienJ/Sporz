package io.github.cyprienj.sporz.engine.game

import io.github.cyprienj.sporz.engine.GameEnd
import io.github.cyprienj.sporz.engine.day.VoteOption
import io.github.cyprienj.sporz.engine.day.VoteResult
import io.github.cyprienj.sporz.engine.model.Composition
import io.github.cyprienj.sporz.engine.model.GameSettings
import io.github.cyprienj.sporz.engine.model.Genome
import io.github.cyprienj.sporz.engine.model.NightStep
import io.github.cyprienj.sporz.engine.model.Player
import io.github.cyprienj.sporz.engine.model.PlayerId
import io.github.cyprienj.sporz.engine.model.PlayerState
import io.github.cyprienj.sporz.engine.model.Role
import io.github.cyprienj.sporz.engine.night.DoctorsDecision
import io.github.cyprienj.sporz.engine.night.InvestigationFinding
import io.github.cyprienj.sporz.engine.night.MutantsDecision
import io.github.cyprienj.sporz.engine.night.Report
import io.github.cyprienj.sporz.engine.night.SpyFinding
import io.github.cyprienj.sporz.engine.setup.SuggestedCompositions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/** Whole games played through [GameState], checked against docs/rules.md (ticket B10). */
class ScriptedGamesTest {

    private fun p(name: String, role: Role, genome: Genome = Genome.Normal, state: PlayerState = PlayerState.Healthy) =
        Player(PlayerId(name), name, role, genome, state)

    private fun id(name: String) = PlayerId(name)
    private fun ids(vararg names: String) = names.map { PlayerId(it) }
    private fun GameState.player(name: String) = players.single { it.name == name }
    private fun GameState.votes(vararg ballots: Pair<String, String?>) =
        ballots.fold(this) { g, (voter, target) -> g.castVote(id(voter), target?.let { VoteOption.For(id(it)) } ?: VoteOption.Blank) }

    /**
     * The sample game of design.html (journal, day, board and end screens).
     * The design ends it on day 4, but by R-WIN-1 it ends as soon as Gaëlle,
     * the last mutant, is ejected on day 2.
     */
    @Test
    fun `the game of design_html`() {
        val crew = listOf(
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
        var game = GameState.fromDeal(crew, SuggestedCompositions.forPlayers(10), GameSettings())

        // Night 1 (journal): mutants mutate Hugo, doctors heal Alice (and Jules), computer scientist: 2 mutants,
        // psychologist: Chloé healthy, geneticist: Farid resistant, hacker hacks the psychologist, spy: Hugo mutated.
        game = game
            .playMutants(MutantsDecision.Mutate(id("Hugo")))
            .playDoctors(DoctorsDecision.Heal(ids("Alice", "Jules")))
            .playComputerScientist()
            .playPsychologist(id("Chloé"))
            .playGeneticist(id("Farid"))
            .playHacker(NightStep.Psychologist)
            .playSpy(id("Hugo"))
        assertEquals(Report.MutantCount(2), game.notebook(id("Inès")).last().report)
        assertEquals(Report.State(id("Chloé"), PlayerState.Healthy), game.notebook(id("Dorian")).last().report)
        assertEquals(Report.GenomeOf(id("Farid"), Genome.Resistant), game.notebook(id("Jules")).last().report)
        assertEquals(Report.Hacked(NightStep.Psychologist, Report.State(id("Chloé"), PlayerState.Healthy)), game.notebook(id("Alice")).last().report)
        assertEquals(Report.Spy(id("Hugo"), listOf(SpyFinding.Mutated)), game.notebook(id("Farid")).last().report)

        // Day 1: "Hugo éjecté (5 voix sur 10, 1 blanc)", revealed "Astronaute, muté".
        game = game.votes(
            "Alice" to "Hugo", "Bastien" to "Hugo", "Chloé" to "Hugo", "Dorian" to "Hugo", "Emma" to "Hugo",
            "Farid" to "Gaëlle", "Gaëlle" to "Farid", "Hugo" to "Gaëlle", "Inès" to null, "Jules" to "Farid",
        ).closeVote()
        assertEquals(VoteResult.Ejected(id("Hugo"), 5.0, Role.Astronaut, PlayerState.Mutated), game.day!!.result)
        assertEquals(Phase.Day, game.phase)

        // Night 2 (MJ night screen): mutants mutate Chloé, doctors heal Chloé.
        game = game.startNight()
            .playMutants(MutantsDecision.Mutate(id("Chloé")))
            .playDoctors(DoctorsDecision.Heal(ids("Chloé", "Alice")))
            .playComputerScientist()
            .playPsychologist(id("Gaëlle"))
            .playGeneticist(id("Inès"))
            .playHacker(NightStep.ComputerScientist)
            .playSpy(id("Chloé"))
        // Day screen: "Personne n'est mort cette nuit." and 9 alive.
        assertEquals(emptyList<PlayerId>(), game.day!!.deaths)
        assertEquals(9, game.players.count { it.alive })
        assertEquals(PlayerState.Healthy, game.player("Chloé").state)

        // Day 2 (day screen tally): Gaëlle 4, Chloé 1, blank 1 → Gaëlle ejected, last mutant gone.
        game = game.votes(
            "Alice" to "Gaëlle", "Bastien" to "Gaëlle", "Dorian" to "Gaëlle", "Jules" to "Gaëlle",
            "Emma" to "Chloé", "Inès" to null,
        ).closeVote()
        assertEquals(VoteResult.Ejected(id("Gaëlle"), 4.0, Role.BaseMutant, PlayerState.Mutated), game.day!!.result)
        assertEquals(Phase.Ended, game.phase)
        assertEquals(GameEnd.HealthyWin, game.end)

        // Saving the finished game keeps everything.
        assertEquals(game, GameState.fromJson(game.toJson()))
    }

    @Test
    fun `mutants spread and win (R-WIN-2)`() {
        val crew = listOf(
            p("Mia", Role.BaseMutant, Genome.Host, PlayerState.Mutated),
            p("Doc", Role.Doctor),
            p("Psy", Role.Psychologist),
            p("Ana", Role.Astronaut),
            p("Ben", Role.Astronaut),
            p("Cyd", Role.Astronaut),
            p("Dan", Role.Astronaut),
        )
        val composition = Composition(mapOf(Role.BaseMutant to 1, Role.Doctor to 1, Role.Psychologist to 1))
        var game = GameState.fromDeal(crew, composition, GameSettings())

        // Night 1: the doctor is mutated, so the doctors' step is only pretended (R-NIGHT-3).
        game = game.playMutants(MutantsDecision.Mutate(id("Doc")))
        assertTrue(game.night!!.progress.current!!.pretend)
        game = game.pretendStep().playPsychologist(id("Ana"))
        assertTrue(game.journal.any { (it.event as? JournalEvent.StepPretended)?.step == NightStep.Doctors })
        game = game.votes("Ana" to "Ben", "Mia" to "Ben", "Doc" to "Ben").closeVote().startNight()

        // Night 2: two mutants, nobody to heal; the psychologist is killed (role revealed: skipped).
        assertEquals(ids("Mia", "Doc"), game.night!!.progress.current!!.actors)
        game = game.playMutants(MutantsDecision.Kill(id("Psy"))).pretendStep()
        assertEquals(Phase.Day, game.phase)
        game = game.votes("Ana" to "Cyd", "Mia" to "Cyd", "Doc" to "Cyd").closeVote().startNight()

        // Night 3: Ana mutated → only Dan is healthy; day 3 the mutants eject him.
        game = game.playMutants(MutantsDecision.Mutate(id("Ana"))).pretendStep()
        game = game.votes("Mia" to "Dan", "Doc" to "Dan", "Ana" to "Dan").closeVote()
        assertEquals(GameEnd.MutantsWin, game.end)
        assertEquals(JournalEvent.GameEnded(GameEnd.MutantsWin), game.journal.last().event)
    }

    @Test
    fun `E-6 ejecting the last mutant wins, even with the traitor alive`() {
        val crew = listOf(
            p("Mia", Role.BaseMutant, Genome.Host, PlayerState.Mutated),
            p("Doc", Role.Doctor),
            p("Tom", Role.Traitor),
            p("Ana", Role.Astronaut),
            p("Ben", Role.Astronaut),
            p("Cyd", Role.Astronaut),
            p("Dan", Role.Astronaut),
        )
        val composition = Composition(mapOf(Role.BaseMutant to 1, Role.Doctor to 1, Role.Traitor to 1))
        var game = GameState.fromDeal(crew, composition, GameSettings())
            .playMutants(MutantsDecision.Kill(id("Dan")))
            .playDoctors(DoctorsDecision.Heal(ids("Ana")))

        // Day 1: the base mutant is ejected → healthy win, the traitor is still alive.
        game = game.votes("Ana" to "Mia", "Ben" to "Mia", "Cyd" to "Mia").closeVote()
        assertEquals(GameEnd.HealthyWin, game.end)
        assertTrue(game.player("Tom").alive)
    }

    @Test
    fun `informant and investigator over two nights (R-NIGHT-23, R-NIGHT-24)`() {
        val crew = listOf(
            p("Mia", Role.BaseMutant, Genome.Host, PlayerState.Mutated),
            p("Doc", Role.Doctor),
            p("Psy", Role.Psychologist),
            p("Ind", Role.Informant),
            p("Inv", Role.Investigator),
            p("Spy", Role.Spy),
            p("Tom", Role.Traitor),
            p("Ana", Role.Astronaut, Genome.Resistant),
            p("Ben", Role.Astronaut),
        )
        val composition = Composition(mapOf(Role.BaseMutant to 1, Role.Doctor to 1, Role.Psychologist to 1, Role.Informant to 1,
            Role.Investigator to 1, Role.Spy to 1, Role.Traitor to 1), resistants = 1)
        var game = GameState.fromDeal(crew, composition, GameSettings())

        // Night 1: a failed mutation and a useless heal on Ana, analysed by the psychologist.
        game = game.playMutants(MutantsDecision.Mutate(id("Ana"), paralysed = id("Ben")))
            .playDoctors(DoctorsDecision.Heal(listOf(id("Ana"))))
            .playPsychologist(id("Ana"))
            .playInformant(id("Tom"))
        assertEquals(Report.Allegiance(id("Tom"), traitor = true), game.notebook(id("Ind")).last().report)
        game = game.playInvestigator(id("Ana"))
        assertEquals(Report.Investigation(id("Ana"), emptyList()), game.notebook(id("Inv")).last().report, "no night before night 1")
        game = game.playSpy(id("Ana"))
        assertEquals(
            Report.Spy(id("Ana"), listOf(SpyFinding.MutationFailed, SpyFinding.HealFailed, SpyFinding.AnalysedByPsychologist, SpyFinding.InvestigatedByInvestigator)),
            game.notebook(id("Spy")).last().report,
        )
        game = game.votes("Ana" to "Ben", "Ben" to "Ana").closeVote().startNight()

        // Night 2: the investigator learns what was done to Ana last night, without outcomes nor tonight's paralysis.
        game = game.playMutants(MutantsDecision.Mutate(id("Ben"), paralysed = id("Ana")))
            .playDoctors(DoctorsDecision.Heal(listOf(id("Ben"))))
            .playPsychologist(id("Mia"))
            .playInformant(id("Mia"))
            .playInvestigator(id("Ana"))
            .playSpy(id("Mia"))
        assertEquals(Report.Allegiance(id("Mia"), traitor = false), game.notebook(id("Ind")).last().report, "a mutant is not the traitor")
        assertEquals(
            Report.Investigation(id("Ana"), listOf(InvestigationFinding.MutationAttempt, InvestigationFinding.HealAttempt,
                InvestigationFinding.AnalysedByPsychologist, InvestigationFinding.AnalysedBySpy)),
            game.notebook(id("Inv")).last().report,
        )
        assertEquals(
            Report.Spy(id("Mia"), listOf(SpyFinding.AnalysedByPsychologist, SpyFinding.AnalysedByInformant)),
            game.notebook(id("Spy")).last().report,
        )
        assertEquals(game, GameState.fromJson(game.toJson()))
    }
}
