package io.github.cyprienj.sporz.engine.game

import io.github.cyprienj.sporz.engine.GameEnd
import io.github.cyprienj.sporz.engine.day.VoteOption
import io.github.cyprienj.sporz.engine.day.VoteResult
import io.github.cyprienj.sporz.engine.game.JournalEvent.Dawn
import io.github.cyprienj.sporz.engine.game.JournalEvent.GameEnded
import io.github.cyprienj.sporz.engine.game.JournalEvent.GameStarted
import io.github.cyprienj.sporz.engine.game.JournalEvent.HackDelivered
import io.github.cyprienj.sporz.engine.game.JournalEvent.StepPlayed
import io.github.cyprienj.sporz.engine.game.JournalEvent.StepSkipped
import io.github.cyprienj.sporz.engine.game.JournalEvent.VoteClosed
import io.github.cyprienj.sporz.engine.model.Composition
import io.github.cyprienj.sporz.engine.model.GameSettings
import io.github.cyprienj.sporz.engine.model.Genome
import io.github.cyprienj.sporz.engine.model.NightStep
import io.github.cyprienj.sporz.engine.model.Player
import io.github.cyprienj.sporz.engine.model.PlayerId
import io.github.cyprienj.sporz.engine.model.PlayerState
import io.github.cyprienj.sporz.engine.model.Role
import io.github.cyprienj.sporz.engine.model.Variants
import io.github.cyprienj.sporz.engine.night.DoctorsDecision
import io.github.cyprienj.sporz.engine.night.HealOutcome
import io.github.cyprienj.sporz.engine.night.MutantsDecision
import io.github.cyprienj.sporz.engine.night.NightEvent
import io.github.cyprienj.sporz.engine.night.Report
import io.github.cyprienj.sporz.engine.night.SpyFinding
import io.github.cyprienj.sporz.engine.setup.Seat
import io.github.cyprienj.sporz.engine.setup.SuggestedCompositions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.random.Random

class GameStateTest {

    /** Crew of design.html. */
    private val crew = listOf(
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
    private val composition = SuggestedCompositions.forPlayers(10)

    private fun p(name: String, role: Role, genome: Genome = Genome.Normal, state: PlayerState = PlayerState.Healthy) =
        Player(PlayerId(name), name, role, genome, state)

    private fun id(name: String) = PlayerId(name)
    private fun GameState.player(name: String) = players.single { it.name == name }
    private fun game(variants: Variants = Variants(), players: List<Player> = crew) =
        GameState.fromDeal(players, composition.copy(variants = variants), GameSettings())

    /** Design night: mutants mutate Chloé, doctors heal Chloé and Alice, then every information role. */
    private fun GameState.designNight(): GameState = this
        .playMutants(MutantsDecision.Mutate(id("Chloé")))
        .playDoctors(DoctorsDecision.Heal(listOf(id("Chloé"), id("Alice"))))
        .playComputerScientist()
        .playPsychologist(id("Chloé"))
        .playGeneticist(id("Farid"))
        .playHacker(NightStep.Psychologist)
        .playSpy(id("Chloé"))

    @Test
    fun `a game starts at night 1 with the mutants`() {
        val game = game()
        assertEquals(Phase.Night, game.phase)
        assertEquals(1, game.round)
        assertEquals(NightStep.Mutants, game.currentStep)
        assertEquals(listOf(JournalEntry(1, Phase.Night, GameStarted(10))), game.journal)
    }

    @Test
    fun `a full night delivers every report and ends at dawn`() {
        val day = game().designNight()

        assertEquals(Phase.Day, day.phase)
        assertEquals(PlayerState.Healthy, day.player("Chloé").state)
        assertEquals(emptyList<PlayerId>(), day.day!!.deaths)

        assertEquals(listOf(ReportDelivery(1, id("Inès"), NightStep.ComputerScientist, Report.MutantCount(1))), day.notebook(id("Inès")))
        val psy = Report.State(id("Chloé"), PlayerState.Healthy)
        assertEquals(psy, day.notebook(id("Dorian")).single().report)
        assertEquals(Report.GenomeOf(id("Farid"), Genome.Resistant), day.notebook(id("Jules")).single().report)
        assertEquals(Report.Hacked(NightStep.Psychologist, psy), day.notebook(id("Alice")).single().report)
        assertEquals(
            Report.Spy(id("Chloé"), listOf(SpyFinding.Mutated, SpyFinding.Healed, SpyFinding.AnalysedByPsychologist)),
            day.notebook(id("Farid")).single().report,
        )
        // Doctors and mutants get no report.
        assertTrue(day.notebook(id("Bastien")).isEmpty() && day.notebook(id("Gaëlle")).isEmpty())

        val events = day.journal.map { it.event }
        assertEquals(
            StepPlayed(
                NightStep.Doctors, listOf(id("Bastien"), id("Emma")),
                listOf(NightEvent.Heal(id("Chloé"), HealOutcome.Healed), NightEvent.Heal(id("Alice"), HealOutcome.NotMutated)),
            ),
            events[2],
        )
        assertEquals(Dawn(emptyList()), events.last())
    }

    @Test
    fun `commands are refused out of turn`() {
        val night = game()
        assertThrows<IllegalStateException> { night.playDoctors(DoctorsDecision.Kill(id("Gaëlle"))) }
        assertThrows<IllegalStateException> { night.castVote(id("Alice"), VoteOption.Blank) }
        assertThrows<IllegalStateException> { night.startNight() }
        val day = night.designNight()
        assertThrows<IllegalStateException> { day.playMutants(MutantsDecision.Kill(id("Hugo"))) }
        assertThrows<IllegalStateException> { day.startNight() } // vote not closed
    }

    @Test
    fun `a game saved mid-night restores and carries on identically`() {
        val midNight = game().playMutants(MutantsDecision.Mutate(id("Chloé"))).playDoctors(DoctorsDecision.Heal(listOf(id("Chloé"), id("Alice"))))
        val restored = GameState.fromJson(midNight.toJson())
        assertEquals(midNight, restored)
        assertEquals(midNight.playComputerScientist(), restored.playComputerScientist())
    }

    @Test
    fun `skipped steps are written in the journal (R-NIGHT-3)`() {
        val noSpy = crew.filter { it.role != Role.Spy }
        val day = game(players = noSpy)
            .playMutants(MutantsDecision.Kill(id("Hugo")))
            .playDoctors(DoctorsDecision.Heal(listOf(id("Chloé"), id("Alice"))))
            .playComputerScientist()
            .playPsychologist(id("Chloé"))
            .playGeneticist(id("Inès"))
            .playHacker(NightStep.Geneticist)
        assertTrue(day.journal.any { it.event == StepSkipped(NightStep.Spy) })
        assertEquals(listOf(id("Hugo")), day.day!!.deaths)
    }

    @Test
    fun `hacking the spy is delivered at the end of the night (R-NIGHT-18)`() {
        val day = game(Variants(hackerCanHackSpy = true))
            .playMutants(MutantsDecision.Mutate(id("Chloé")))
            .playDoctors(DoctorsDecision.Heal(listOf(id("Jules"), id("Alice"))))
            .playComputerScientist()
            .playPsychologist(id("Chloé"))
            .playGeneticist(id("Farid"))
            .playHacker(NightStep.Spy)
            .also { assertTrue(it.notebook(id("Alice")).isEmpty(), "nothing before the spy plays") }
            .playSpy(id("Chloé"))
        val copy = Report.Hacked(NightStep.Spy, Report.Spy(id("Chloé"), listOf(SpyFinding.Mutated, SpyFinding.AnalysedByPsychologist)))
        assertEquals(copy, day.notebook(id("Alice")).single().report)
        assertTrue(day.journal.any { it.event == HackDelivered(id("Alice"), copy) })
    }

    @Test
    fun `the vote ejects, then the next night starts (R-DAY-9 to R-DAY-12)`() {
        var game = game().designNight()
        listOf("Alice", "Bastien", "Chloé", "Dorian", "Emma").forEach { game = game.castVote(id(it), VoteOption.For(id("Hugo"))) }
        game = game.castVote(id("Inès"), VoteOption.Blank).closeVote()

        assertEquals(VoteResult.Ejected(id("Hugo"), 5.0, Role.Astronaut, PlayerState.Healthy), game.day!!.result)
        assertFalse(game.player("Hugo").alive)
        assertTrue(game.journal.last().event is VoteClosed)
        assertThrows<IllegalStateException> { game.castVote(id("Alice"), VoteOption.Blank) }

        game = game.startNight()
        assertEquals(2, game.round)
        assertEquals(Phase.Night, game.phase)
        assertEquals(NightStep.Mutants, game.currentStep)
    }

    @Test
    fun `ejecting the last mutant ends the game (R-WIN-1)`() {
        val ended = game().designNight()
            .castVote(id("Alice"), VoteOption.For(id("Gaëlle")))
            .closeVote()
        assertEquals(Phase.Ended, ended.phase)
        assertEquals(GameEnd.HealthyWin, ended.end)
        assertEquals(GameEnded(GameEnd.HealthyWin), ended.journal.last().event)
        assertThrows<IllegalStateException> { ended.startNight() }
    }

    @Test
    fun `players only see the public journal until the end`() {
        val day = game().designNight()
        assertEquals(listOf(GameStarted(10), Dawn(emptyList())), day.publicJournal().map { it.event })
        val ended = day.abort()
        assertEquals(ended.journal, ended.publicJournal())
        assertEquals(GameEnd.Aborted, ended.end)
    }

    @Test
    fun `captain - elected, killed at night, replaced before the vote closes (R-DAY-4 to R-DAY-6)`() {
        var game = game(Variants(captain = true)).designNight()
        game = game.electCaptain(mapOf(id("Alice") to id("Emma"), id("Bastien") to id("Emma")), Random(0))
        assertEquals(id("Emma"), game.captain)
        assertThrows<IllegalStateException> { game.electCaptain(mapOf(id("Alice") to id("Jules")), Random(0)) }

        game = game.castVote(id("Alice"), VoteOption.Blank).closeVote().startNight()
        game = game.playMutants(MutantsDecision.Kill(id("Emma")))
            .playDoctors(DoctorsDecision.Heal(listOf(id("Alice"))))
            .playComputerScientist().playPsychologist(id("Chloé")).playGeneticist(id("Chloé"))
            .playHacker(NightStep.Psychologist).playSpy(id("Chloé"))

        assertNull(game.captain)
        assertEquals(id("Emma"), game.captainToReplace)
        val voted = game.castVote(id("Alice"), VoteOption.Blank)
        assertThrows<IllegalStateException> { voted.closeVote() }

        game = voted.nameNewCaptain(id("Jules"))
        assertEquals(id("Jules"), game.captain)
        assertEquals(VoteResult.BlankWins, game.closeVote().day!!.result)
    }

    @Test
    fun `captain breaks a tie through the game state (R-DAY-10)`() {
        val game = game(Variants(captain = true)).designNight()
            .electCaptain(mapOf(id("Alice") to id("Emma")), Random(0))
            .castVote(id("Alice"), VoteOption.For(id("Gaëlle")))
            .castVote(id("Bastien"), VoteOption.For(id("Hugo")))
            .closeVote()
        assertTrue(game.day!!.result is VoteResult.CaptainMustChoose)
        assertThrows<IllegalStateException> { game.startNight() }
        val decided = game.closeVote(VoteOption.For(id("Hugo")))
        assertFalse(decided.player("Hugo").alive)
    }

    @Test
    fun `start deals with a seeded random`() {
        val seats = crew.map { Seat(it.id, it.name) }
        val a = GameState.start(seats, composition, GameSettings(), Random(5))
        assertEquals(a, GameState.start(seats, composition, GameSettings(), Random(5)))
        assertEquals(1, a.players.count { it.role == Role.BaseMutant })
    }

    @Test
    fun `a game without suggestion also works`() {
        val small = listOf(p("A", Role.BaseMutant, Genome.Host, PlayerState.Mutated), p("B", Role.Doctor)) +
            (1..5).map { p("X$it", Role.Astronaut) }
        val game = GameState.fromDeal(small, Composition(), GameSettings())
            .playMutants(MutantsDecision.Kill(id("B")))
        assertEquals(Phase.Day, game.phase, "doctors' step skipped, night over")
        assertEquals(listOf(id("B")), game.day!!.deaths)
    }

    @Test
    fun `reset throws away every ballot and keeps the vote open`() {
        val day = game().designNight()
            .castVote(id("Alice"), VoteOption.For(id("Hugo")))
            .castVote(id("Bastien"), VoteOption.Blank)
            .resetVote()
        assertTrue(day.day!!.vote.ballots.isEmpty())
        assertNull(day.day!!.result)
        assertEquals(VoteResult.NoVotes, day.closeVote().day!!.result)
    }

    @Test
    fun `a paralysed doctor sits out the night, and only that night (R-NIGHT-22)`() {
        val night = game().playMutants(MutantsDecision.Mutate(id("Chloé"), paralysed = id("Bastien")))
        assertEquals(NightStep.Doctors, night.currentStep)
        assertEquals(listOf(id("Emma")), night.night!!.progress.current!!.actors)
        // One doctor left: one heal.
        val healed = night.playDoctors(DoctorsDecision.Heal(listOf(id("Chloé"))))
        assertEquals(PlayerState.Healthy, healed.player("Chloé").state)

        val nextNight = healed.playComputerScientist()
            .playPsychologist(id("Hugo"))
            .playGeneticist(id("Hugo"))
            .playHacker(NightStep.Psychologist)
            .playSpy(id("Bastien"))
            .closeVote()
            .startNight()
            .playMutants(MutantsDecision.Kill(id("Hugo")))
        assertEquals(listOf(id("Bastien"), id("Emma")), nextNight.night!!.progress.current!!.actors)
    }

    @Test
    fun `a paralysed information role is skipped and the spy sees the paralysis (R-NIGHT-22, R-NIGHT-19)`() {
        val night = game().playMutants(MutantsDecision.Kill(id("Hugo"), paralysed = id("Dorian")))
            .playDoctors(DoctorsDecision.Heal(listOf(id("Alice"), id("Jules"))))
            .playComputerScientist()
        // The psychologist is skipped; the hacker copying them gets nothing.
        assertEquals(NightStep.Geneticist, night.currentStep)
        val spied = night.playGeneticist(id("Chloé"))
            .playHacker(NightStep.Psychologist)
            .playSpy(id("Dorian"))
        assertTrue(spied.journal.any { it.event == StepSkipped(NightStep.Psychologist) })
        assertEquals(Report.Hacked(NightStep.Psychologist, null), spied.notebook(id("Alice")).single().report)
        assertEquals(Report.Spy(id("Dorian"), listOf(SpyFinding.Paralysed)), spied.notebook(id("Farid")).single().report)
    }

    @Test
    fun `a game saved before paralysis existed still loads`() {
        val json = game().toJson().replace(Regex(",?\"paralysed\":null"), "")
        assertNull(GameState.fromJson(json).night!!.progress.paralysed)
    }
}
