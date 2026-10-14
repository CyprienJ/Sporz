package io.github.cyprienj.sporz.engine.night

import io.github.cyprienj.sporz.engine.EngineJson
import io.github.cyprienj.sporz.engine.model.Genome
import io.github.cyprienj.sporz.engine.model.NightStep
import io.github.cyprienj.sporz.engine.model.Player
import io.github.cyprienj.sporz.engine.model.PlayerId
import io.github.cyprienj.sporz.engine.model.PlayerState
import io.github.cyprienj.sporz.engine.model.Role
import kotlinx.serialization.encodeToString
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class NightActionsTest {

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
    private val doctors = ids("Bastien", "Emma")

    private fun p(name: String, role: Role, genome: Genome = Genome.Normal, state: PlayerState = PlayerState.Healthy) =
        Player(PlayerId(name), name, role, genome, state)

    private fun id(name: String) = PlayerId(name)
    private fun ids(vararg names: String) = names.map { PlayerId(it) }
    private fun List<Player>.get(name: String) = single { it.name == name }
    private fun List<Player>.update(name: String, change: (Player) -> Player) = map { if (it.name == name) change(it) else it }
    private fun heal(vararg targets: String) = DoctorsDecision.Heal(targets.map { id(it) })

    // --- Mutants ---------------------------------------------------------------

    @Test
    fun `mutating a normal or host player works (R-NIGHT-8)`() {
        val normal = resolveMutants(crew, MutantsDecision.Mutate(id("Chloé")))
        assertTrue(normal.players.get("Chloé").isMutated)
        assertEquals(listOf(NightEvent.Mutation(id("Chloé"), succeeded = true)), normal.events)

        val host = resolveMutants(crew, MutantsDecision.Mutate(id("Inès")))
        assertTrue(host.players.get("Inès").isMutated)
    }

    @Test
    fun `E-3 mutating a resistant player fails but uses the action (R-NIGHT-8)`() {
        val outcome = resolveMutants(crew, MutantsDecision.Mutate(id("Farid")))
        assertEquals(crew, outcome.players)
        assertEquals(listOf(NightEvent.Mutation(id("Farid"), succeeded = false)), outcome.events)
    }

    @Test
    fun `mutants can kill instead (R-NIGHT-6)`() {
        val outcome = resolveMutants(crew, MutantsDecision.Kill(id("Hugo")))
        assertFalse(outcome.players.get("Hugo").alive)
        assertEquals(listOf(id("Hugo")), outcome.events.deaths())
    }

    @Test
    fun `mutants cannot target a mutated or dead player (R-NIGHT-6, R-NIGHT-7)`() {
        val mutated = assertThrows<IllegalActionException> { resolveMutants(crew, MutantsDecision.Kill(id("Gaëlle"))) }
        assertEquals(ActionProblem.TargetAlreadyMutated, mutated.problem)

        val dead = crew.update("Hugo") { it.copy(alive = false) }
        assertEquals(ActionProblem.TargetDead, assertThrows<IllegalActionException> { resolveMutants(dead, MutantsDecision.Mutate(id("Hugo"))) }.problem)
        assertEquals(ActionProblem.UnknownPlayer, assertThrows<IllegalActionException> { resolveMutants(crew, MutantsDecision.Mutate(id("Zoé"))) }.problem)
    }

    @Test
    fun `mutants can target the traitor, who then becomes a mutant (R-NIGHT-7, R-SET-12)`() {
        val withTraitor = crew.update("Hugo") { it.copy(role = Role.Traitor) }
        assertTrue(resolveMutants(withTraitor, MutantsDecision.Mutate(id("Hugo"))).players.get("Hugo").isMutated)
    }

    @Test
    fun `a mutated traitor cannot be healed (R-SET-12)`() {
        val mutatedTraitor = crew.update("Hugo") { it.copy(role = Role.Traitor, state = PlayerState.Mutated) }
        val outcome = resolveDoctors(mutatedTraitor, doctors, heal("Hugo", "Alice"))
        assertTrue(outcome.players.get("Hugo").isMutated)
        assertEquals(NightEvent.Heal(id("Hugo"), HealOutcome.Traitor), outcome.events.first())
    }

    @Test
    fun `mutants also paralyse a living healthy player (R-NIGHT-22)`() {
        val outcome = resolveMutants(crew, MutantsDecision.Kill(id("Hugo"), paralysed = id("Dorian")))
        assertEquals(listOf(NightEvent.Kill(id("Hugo"), Killer.Mutants), NightEvent.Paralysis(id("Dorian"))), outcome.events)
        assertTrue(outcome.players.get("Dorian").alive)

        val mutated = assertThrows<IllegalActionException> { resolveMutants(crew, MutantsDecision.Mutate(id("Hugo"), paralysed = id("Gaëlle"))) }
        assertEquals(ActionProblem.TargetAlreadyMutated, mutated.problem)
        val dead = crew.update("Dorian") { it.copy(alive = false) }
        assertEquals(ActionProblem.TargetDead, assertThrows<IllegalActionException> { resolveMutants(dead, MutantsDecision.Mutate(id("Hugo"), id("Dorian"))) }.problem)
    }

    @Test
    fun `mutants cannot paralyse their own target (R-NIGHT-22)`() {
        listOf(MutantsDecision.Mutate(id("Hugo"), paralysed = id("Hugo")), MutantsDecision.Kill(id("Hugo"), paralysed = id("Hugo"))).forEach {
            val error = assertThrows<IllegalActionException> { resolveMutants(crew, it) }
            assertEquals(ActionProblem.ParalysedIsTarget, error.problem)
            assertEquals(id("Hugo"), error.player)
        }
        // The last healthy player cannot be both: nobody is paralysed.
        val lastHealthy = crew.map { if (it.name in setOf("Gaëlle", "Hugo")) it else it.copy(alive = false) }
        assertEquals(emptyList<PlayerId>(), paralysable(lastHealthy, id("Hugo")))
    }

    @Test
    fun `the paralysed player is not woken (R-NIGHT-22)`() {
        assertEquals(ids("Emma"), NightStep.Doctors.actors(crew, paralysed = id("Bastien")))
        assertEquals(emptyList<PlayerId>(), NightStep.Psychologist.actors(crew, paralysed = id("Dorian")))
        assertEquals(ids("Gaëlle"), NightStep.Mutants.actors(crew, paralysed = id("Gaëlle")))
    }

    // --- Doctors ---------------------------------------------------------------

    @Test
    fun `E-1 mutated then healed (design night 2)`() {
        val afterMutants = resolveMutants(crew, MutantsDecision.Mutate(id("Chloé")))
        val afterDoctors = resolveDoctors(afterMutants.players, doctors, heal("Chloé", "Alice"))

        assertEquals(PlayerState.Healthy, afterDoctors.players.get("Chloé").state)
        assertEquals(
            listOf(
                NightEvent.Heal(id("Chloé"), HealOutcome.Healed),
                NightEvent.Heal(id("Alice"), HealOutcome.NotMutated),
            ),
            afterDoctors.events,
        )
        assertEquals(emptyList<PlayerId>(), (afterMutants.events + afterDoctors.events).deaths())
    }

    @Test
    fun `E-4 a mutated host cannot be healed (R-NIGHT-13)`() {
        val inesMutated = crew.update("Inès") { it.copy(state = PlayerState.Mutated) }
        val outcome = resolveDoctors(inesMutated, doctors, heal("Inès", "Chloé"))
        assertTrue(outcome.players.get("Inès").isMutated)
        assertEquals(NightEvent.Heal(id("Inès"), HealOutcome.Host), outcome.events.first())
    }

    @Test
    fun `doctors heal different players, decided together (R-NIGHT-11, DEC-2)`() {
        val chloeMutated = crew.update("Chloé") { it.copy(state = PlayerState.Mutated) }
        val error = assertThrows<IllegalActionException> { resolveDoctors(chloeMutated, doctors, heal("Chloé", "Chloé")) }
        assertEquals(ActionProblem.DuplicateTarget, error.problem)
        assertEquals(id("Chloé"), error.player)
    }

    @Test
    fun `awake doctors heal neither themselves nor each other (R-NIGHT-11)`() {
        val self = assertThrows<IllegalActionException> { resolveDoctors(crew, ids("Bastien"), heal("Bastien")) }
        assertEquals(ActionProblem.SelfTarget, self.problem)
        assertEquals(id("Bastien"), self.player)
        val other = assertThrows<IllegalActionException> { resolveDoctors(crew, doctors, heal("Chloé", "Emma")) }
        assertEquals(ActionProblem.SelfTarget, other.problem)
        assertEquals(id("Emma"), other.player)
    }

    @Test
    fun `a doctor who does not wake up can be healed by the other (R-NIGHT-11)`() {
        // A mutated doctor does not wake up, and the other one may heal them.
        val emmaMutated = crew.update("Emma") { it.copy(state = PlayerState.Mutated) }
        assertEquals(NightEvent.Heal(id("Emma"), HealOutcome.Healed), resolveDoctors(emmaMutated, ids("Bastien"), heal("Emma")).events.single())
    }

    @Test
    fun `one heal per acting doctor (R-NIGHT-11)`() {
        assertEquals(ActionProblem.WrongTargetCount, assertThrows<IllegalActionException> { resolveDoctors(crew, doctors, heal("Chloé")) }.problem)
        assertEquals(ActionProblem.WrongTargetCount, assertThrows<IllegalActionException> { resolveDoctors(crew, doctors, heal("Chloé", "Alice", "Hugo")) }.problem)
        // Only one doctor left: one heal.
        assertEquals(1, resolveDoctors(crew, ids("Bastien"), heal("Chloé")).events.size)
        // Two doctors and a single other living player: one heal only.
        val lastThree = crew.map { if (it.name in setOf("Bastien", "Emma", "Gaëlle")) it else it.copy(alive = false) }
        assertEquals(1, healCount(lastThree, doctors))
        assertEquals(1, resolveDoctors(lastThree, doctors, heal("Gaëlle")).events.size)
    }

    @Test
    fun `doctors cannot heal a player killed earlier this night (R-NIGHT-2)`() {
        val afterMutants = resolveMutants(crew, MutantsDecision.Kill(id("Chloé")))
        val error = assertThrows<IllegalActionException> { resolveDoctors(afterMutants.players, doctors, heal("Chloé", "Alice")) }
        assertEquals(ActionProblem.TargetDead, error.problem)
    }

    @Test
    fun `doctors can kill anybody but themselves (R-NIGHT-14, R-NIGHT-5)`() {
        val outcome = resolveDoctors(crew, doctors, DoctorsDecision.Kill(id("Gaëlle")))
        assertFalse(outcome.players.get("Gaëlle").alive)
        assertEquals(listOf(NightEvent.Kill(id("Gaëlle"), Killer.Doctors)), outcome.events)
        assertEquals(ActionProblem.SelfTarget, assertThrows<IllegalActionException> { resolveDoctors(crew, doctors, DoctorsDecision.Kill(id("Emma"))) }.problem)
    }

    @Test
    fun `a dead player keeps their state (rules section 1)`() {
        val chloeMutated = crew.update("Chloé") { it.copy(state = PlayerState.Mutated) }
        val outcome = resolveDoctors(chloeMutated, doctors, DoctorsDecision.Kill(id("Chloé")))
        assertTrue(outcome.players.get("Chloé").isMutated)
    }

    // --- Whole night -----------------------------------------------------------

    @Test
    fun `E-2 a mutated psychologist loses their step until healed (R-NIGHT-4)`() {
        var night = NightProgress.start(2, NightStep.DefaultOrder, crew)
        val afterMutants = resolveMutants(crew, MutantsDecision.Mutate(id("Dorian")))
        night = night.advance(afterMutants.players)
        val afterDoctors = resolveDoctors(afterMutants.players, night.current!!.actors, heal("Alice", "Jules"))
        night = night.advance(afterDoctors.players) // computer scientist
        night = night.advance(afterDoctors.players) // psychologist skipped
        assertEquals(NightStep.Geneticist, night.current!!.step)
        assertEquals(listOf(NightStep.Psychologist), night.skipped)
    }

    @Test
    fun `a mutated doctor wakes with the mutants, not with the doctors (DEC-4)`() {
        val afterMutants = resolveMutants(crew, MutantsDecision.Mutate(id("Emma")))
        assertEquals(ids("Bastien"), NightStep.Doctors.actors(afterMutants.players))
        // Next night:
        assertEquals(ids("Emma", "Gaëlle"), NightStep.Mutants.actors(afterMutants.players))
    }

    @Test
    fun `events round-trip through JSON`() {
        val events: List<NightEvent> = listOf(
            NightEvent.Mutation(id("Chloé"), true),
            NightEvent.Kill(id("Hugo"), Killer.Doctors),
            NightEvent.Heal(id("Chloé"), HealOutcome.Healed),
        )
        assertEquals(events, EngineJson.decodeFromString<List<NightEvent>>(EngineJson.encodeToString(events)))
    }
}
