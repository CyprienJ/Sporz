package io.github.cyprienj.sporz.engine.night

import io.github.cyprienj.sporz.engine.EngineJson
import io.github.cyprienj.sporz.engine.model.Genome
import io.github.cyprienj.sporz.engine.model.NightStep
import io.github.cyprienj.sporz.engine.model.NightStep.ComputerScientist
import io.github.cyprienj.sporz.engine.model.NightStep.Doctors
import io.github.cyprienj.sporz.engine.model.NightStep.Geneticist
import io.github.cyprienj.sporz.engine.model.NightStep.Hacker
import io.github.cyprienj.sporz.engine.model.NightStep.Mutants
import io.github.cyprienj.sporz.engine.model.NightStep.Psychologist
import io.github.cyprienj.sporz.engine.model.NightStep.Spy
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

class NightProgressTest {

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

    private fun List<Player>.update(name: String, change: (Player) -> Player) = map { if (it.name == name) change(it) else it }
    private fun ids(vararg names: String) = names.map { PlayerId(it) }

    /** Plays the whole night without changes, returning the steps called in order. */
    private fun callOrder(players: List<Player>, order: List<NightStep> = NightStep.DefaultOrder, deadRevealed: Boolean = true): List<NightStep> {
        var night = NightProgress.start(1, order, players, deadRevealed)
        val called = mutableListOf<NightStep>()
        while (!night.isOver) {
            called += night.current!!.step
            night = night.advance(players)
        }
        return called
    }

    @Test
    fun `full crew plays every step in the default order (R-NIGHT-1)`() {
        assertEquals(NightStep.DefaultOrder, callOrder(crew))
    }

    @Test
    fun `actors of each step`() {
        assertEquals(ids("Gaëlle"), Mutants.actors(crew))
        assertEquals(ids("Bastien", "Emma"), Doctors.actors(crew))
        assertEquals(ids("Inès"), ComputerScientist.actors(crew))
        assertEquals(ids("Farid"), Spy.actors(crew))
    }

    @Test
    fun `all living mutants wake together, whatever their role (R-NIGHT-6)`() {
        val players = crew
            .update("Emma") { it.copy(state = PlayerState.Mutated) }
            .update("Hugo") { it.copy(state = PlayerState.Mutated, alive = false) }
        assertEquals(ids("Emma", "Gaëlle"), Mutants.actors(players))
        assertEquals(ids("Bastien"), Doctors.actors(players))
    }

    @Test
    fun `steps of absent roles are skipped (R-NIGHT-3)`() {
        val noSpyNoHacker = crew.filter { it.role != Role.Spy && it.role != Role.Hacker }
        var night = NightProgress.start(1, NightStep.DefaultOrder, noSpyNoHacker)
        while (!night.isOver) night = night.advance(noSpyNoHacker)
        assertEquals(listOf(Hacker, Spy), night.skipped)
    }

    @Test
    fun `a dead or mutated holder does not play their step but it is pretended (R-NIGHT-3, R-NIGHT-4)`() {
        val players = crew
            .update("Dorian") { it.copy(state = PlayerState.Mutated) }
            .update("Jules") { it.copy(alive = false) }
        // Reveal variant on: Jules's death and role are known, his step is skipped.
        assertEquals(listOf(Mutants, Doctors, ComputerScientist, Psychologist, Hacker, Spy), callOrder(players))
        val psychologist = NightProgress.start(1, listOf(Psychologist) + (NightStep.DefaultOrder - Psychologist), players).current!!
        assertEquals(StepCall(Psychologist, emptyList(), listOf(Absence(PlayerId("Dorian"), AbsenceReason.Mutated))), psychologist)
        assertTrue(psychologist.pretend)
        // Reveal variant off: nobody knows Jules was the geneticist, his step is pretended too.
        assertEquals(NightStep.DefaultOrder, callOrder(players, deadRevealed = false))
    }

    @Test
    fun `a holder absent among others is listed, the step is played normally (R-NIGHT-22)`() {
        // The mutants paralysed Bastien: Emma heals alone, the MJ is reminded not to wake Bastien.
        val call = NightProgress.start(1, NightStep.DefaultOrder, crew).copy(paralysed = PlayerId("Bastien")).advance(crew).current!!
        assertEquals(StepCall(Doctors, ids("Emma"), listOf(Absence(PlayerId("Bastien"), AbsenceReason.Paralysed))), call)
        assertFalse(call.pretend)
    }

    @Test
    fun `actors are computed when the step starts, after earlier effects (R-NIGHT-2)`() {
        var night = NightProgress.start(1, NightStep.DefaultOrder, crew)
        assertEquals(Mutants, night.current!!.step)

        // Mutants mutate Dorian (psychologist) and kill Inès (computer scientist)...
        var players = crew
            .update("Dorian") { it.copy(state = PlayerState.Mutated) }
            .update("Inès") { it.copy(alive = false) }
        night = night.advance(players)
        assertEquals(StepCall(Doctors, ids("Bastien", "Emma")), night.current)

        // ...doctors heal Dorian: he plays his step again (R-NIGHT-4), Inès does not.
        players = players.update("Dorian") { it.copy(state = PlayerState.Healthy) }
        night = night.advance(players)
        assertEquals(StepCall(Psychologist, ids("Dorian")), night.current)
        assertEquals(listOf(ComputerScientist), night.skipped)
    }

    @Test
    fun `a custom call order is followed`() {
        val order = listOf(Spy, Hacker, Geneticist, Psychologist, ComputerScientist, Doctors, Mutants)
        assertEquals(order, callOrder(crew, order))
    }

    @Test
    fun `upcoming steps list who would be woken, skipping empty steps`() {
        val players = crew.update("Jules") { it.copy(alive = false) }
        val night = NightProgress.start(2, NightStep.DefaultOrder, players).advance(players) // at Doctors
        assertEquals(
            listOf(
                StepCall(ComputerScientist, ids("Inès")),
                StepCall(Psychologist, ids("Dorian")),
                StepCall(Hacker, ids("Alice")),
                StepCall(Spy, ids("Farid")),
            ),
            night.upcoming(players),
        )
    }

    @Test
    fun `the traitor is never woken (R-SET-12)`() {
        val players = crew.update("Hugo") { it.copy(role = Role.Traitor) }
        NightStep.entries.forEach { step -> assertTrue(PlayerId("Hugo") !in step.actors(players), step.name) }
    }

    @Test
    fun `advancing past the end or starting badly is refused`() {
        var night = NightProgress.start(1, NightStep.DefaultOrder, crew)
        while (!night.isOver) night = night.advance(crew)
        assertThrows<IllegalStateException> { night.advance(crew) }
        assertThrows<IllegalArgumentException> { NightProgress.start(0, NightStep.DefaultOrder, crew) }
        assertThrows<IllegalArgumentException> { NightProgress.start(1, listOf(Mutants), crew) }
    }

    @Test
    fun `a night in progress can be saved and restored`() {
        val night = NightProgress.start(3, NightStep.DefaultOrder, crew).advance(crew)
        assertEquals(night, EngineJson.decodeFromString<NightProgress>(EngineJson.encodeToString(night)))
    }
}
