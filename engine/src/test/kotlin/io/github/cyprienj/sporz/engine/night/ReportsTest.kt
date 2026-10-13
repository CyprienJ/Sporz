package io.github.cyprienj.sporz.engine.night

import io.github.cyprienj.sporz.engine.EngineJson
import io.github.cyprienj.sporz.engine.model.Genome
import io.github.cyprienj.sporz.engine.model.NightStep
import io.github.cyprienj.sporz.engine.model.Player
import io.github.cyprienj.sporz.engine.model.PlayerId
import io.github.cyprienj.sporz.engine.model.PlayerState
import io.github.cyprienj.sporz.engine.model.Role
import io.github.cyprienj.sporz.engine.model.Variants
import kotlinx.serialization.encodeToString
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ReportsTest {

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

    private fun p(name: String, role: Role, genome: Genome = Genome.Normal, state: PlayerState = PlayerState.Healthy) =
        Player(PlayerId(name), name, role, genome, state)

    private fun id(name: String) = PlayerId(name)
    private fun List<Player>.update(name: String, change: (Player) -> Player) = map { if (it.name == name) change(it) else it }

    @Test
    fun `computer scientist counts living mutated players after mutants and doctors (R-NIGHT-15)`() {
        assertEquals(Report.MutantCount(1), computerScientistReport(crew))
        val afterMutants = resolveMutants(crew, MutantsDecision.Mutate(id("Chloé"))).players
        assertEquals(Report.MutantCount(2), computerScientistReport(afterMutants))
        val deadMutant = afterMutants.update("Chloé") { it.copy(alive = false) }
        assertEquals(Report.MutantCount(1), computerScientistReport(deadMutant))
    }

    @Test
    fun `the traitor is not counted nor seen as mutated unless mutated (R-NIGHT-15, R-NIGHT-16)`() {
        val withTraitor = crew.update("Hugo") { it.copy(role = Role.Traitor) }
        assertEquals(Report.MutantCount(1), computerScientistReport(withTraitor))
        assertEquals(
            Report.State(id("Hugo"), PlayerState.Healthy),
            psychologistReport(withTraitor, id("Dorian"), id("Hugo")).report,
        )
    }

    @Test
    fun `psychologist learns the state, geneticist the genome (R-NIGHT-16, R-NIGHT-17)`() {
        val psy = psychologistReport(crew, id("Dorian"), id("Gaëlle"))
        assertEquals(Report.State(id("Gaëlle"), PlayerState.Mutated), psy.report)
        assertEquals(NightEvent.Analysis(id("Gaëlle"), Analyst.Psychologist), psy.event)

        val gen = geneticistReport(crew, id("Jules"), id("Farid"))
        assertEquals(Report.GenomeOf(id("Farid"), Genome.Resistant), gen.report)
        assertEquals(NightEvent.Analysis(id("Farid"), Analyst.Geneticist), gen.event)
    }

    @Test
    fun `designating oneself or a dead player is refused (R-NIGHT-5)`() {
        assertEquals(ActionProblem.SelfTarget, assertThrows<IllegalActionException> { psychologistReport(crew, id("Dorian"), id("Dorian")) }.problem)
        val dead = crew.update("Hugo") { it.copy(alive = false) }
        assertEquals(ActionProblem.TargetDead, assertThrows<IllegalActionException> { geneticistReport(dead, id("Jules"), id("Hugo")) }.problem)
    }

    @Test
    fun `E-1 the spy sees Chloé mutated then healed (R-NIGHT-19)`() {
        val mutants = resolveMutants(crew, MutantsDecision.Mutate(id("Chloé")))
        val doctors = resolveDoctors(mutants.players, listOf(id("Bastien"), id("Emma")), DoctorsDecision.Heal(listOf(id("Chloé"), id("Alice"))))
        val psy = psychologistReport(doctors.players, id("Dorian"), id("Chloé"))
        val events = mutants.events + doctors.events + listOfNotNull(psy.event)

        assertEquals(
            Report.Spy(id("Chloé"), listOf(SpyFinding.Mutated, SpyFinding.Healed, SpyFinding.AnalysedByPsychologist)),
            spyReport(doctors.players, id("Farid"), id("Chloé"), events),
        )
        assertEquals(Report.Spy(id("Alice"), listOf(SpyFinding.HealFailed)), spyReport(doctors.players, id("Farid"), id("Alice"), events))
        assertEquals(Report.Spy(id("Hugo"), emptyList()), spyReport(doctors.players, id("Farid"), id("Hugo"), events))
    }

    @Test
    fun `E-3 the spy sees a failed mutation on a resistant player (R-NIGHT-19)`() {
        val withResistantHugo = crew.update("Hugo") { it.copy(genome = Genome.Resistant) }
        val events = resolveMutants(withResistantHugo, MutantsDecision.Mutate(id("Hugo"))).events
        assertEquals(listOf(SpyFinding.MutationFailed), spyReport(withResistantHugo, id("Farid"), id("Hugo"), events).findings)
    }

    @Test
    fun `the hacker copies a report of the night, the hacked player keeps theirs (R-NIGHT-18, DEC-24)`() {
        val psyReport = psychologistReport(crew, id("Dorian"), id("Gaëlle")).report
        var night = NightProgress.start(1, NightStep.DefaultOrder, crew)
        while (night.current?.step != NightStep.Hacker) night = night.advance(crew)

        val copy = hackerReport(NightStep.Psychologist, mapOf(NightStep.Psychologist to psyReport), night)
        assertEquals(Report.Hacked(NightStep.Psychologist, psyReport), copy)
    }

    @Test
    fun `the hacker gets nothing when the hacked role produced no report (R-NIGHT-18)`() {
        val psyMutated = crew.update("Dorian") { it.copy(state = PlayerState.Mutated) }
        var night = NightProgress.start(1, NightStep.DefaultOrder, psyMutated)
        while (night.current?.step != NightStep.Hacker) night = night.advance(psyMutated)

        assertEquals(Report.Hacked(NightStep.Psychologist, null), hackerReport(NightStep.Psychologist, emptyMap(), night))
    }

    @Test
    fun `hacking the spy needs the variant and is delivered at the end of the night (R-NIGHT-18, R-NIGHT-19)`() {
        assertEquals(ActionProblem.NotHackable, assertThrows<IllegalActionException> { checkHackerChoice(NightStep.Spy, Variants()) }.problem)
        checkHackerChoice(NightStep.Spy, Variants(hackerCanHackSpy = true))

        var night = NightProgress.start(1, NightStep.DefaultOrder, crew)
        while (night.current?.step != NightStep.Hacker) night = night.advance(crew)
        assertNull(hackerReport(NightStep.Spy, emptyMap(), night), "spy step not played yet")

        val spy = spyReport(crew, id("Farid"), id("Hugo"), emptyList())
        night = night.advance(crew) // spy
        night = night.advance(crew) // end of night
        assertEquals(Report.Hacked(NightStep.Spy, spy), hackerReport(NightStep.Spy, mapOf(NightStep.Spy to spy), night))
    }

    @Test
    fun `the doctors, mutants and hacker steps cannot be hacked`() {
        listOf(NightStep.Mutants, NightStep.Doctors, NightStep.Hacker).forEach {
            assertThrows<IllegalActionException> { checkHackerChoice(it, Variants(hackerCanHackSpy = true)) }
        }
    }

    @Test
    fun `reports round-trip through JSON`() {
        val reports: List<Report> = listOf(
            Report.MutantCount(2),
            Report.State(id("Chloé"), PlayerState.Healthy),
            Report.GenomeOf(id("Farid"), Genome.Resistant),
            Report.Spy(id("Hugo"), listOf(SpyFinding.Mutated)),
            Report.Hacked(NightStep.Psychologist, Report.State(id("Chloé"), PlayerState.Healthy)),
            Report.Hacked(NightStep.Spy, null),
        )
        assertEquals(reports, EngineJson.decodeFromString<List<Report>>(EngineJson.encodeToString(reports)))
    }
}
