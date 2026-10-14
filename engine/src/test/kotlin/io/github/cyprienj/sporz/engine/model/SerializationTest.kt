package io.github.cyprienj.sporz.engine.model

import io.github.cyprienj.sporz.engine.EngineJson
import kotlinx.serialization.encodeToString
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/** JSON names must be the codes of docs/rules.md. */
class SerializationTest {

    @Test
    fun `roles use the API codes`() {
        assertEquals(
            listOf("baseMutant", "doctor", "psychologist", "geneticist", "computerScientist", "hacker", "spy", "traitor", "astronaut"),
            Role.entries.map { EngineJson.encodeToString(it).trim('"') },
        )
    }

    @Test
    fun `genomes, states and camps use the API codes`() {
        assertEquals(listOf("host", "normal", "resistant"), Genome.entries.map { EngineJson.encodeToString(it).trim('"') })
        assertEquals(listOf("healthy", "mutated"), PlayerState.entries.map { EngineJson.encodeToString(it).trim('"') })
        assertEquals(listOf("healthy", "mutant"), Camp.entries.map { EngineJson.encodeToString(it).trim('"') })
    }

    @Test
    fun `a player round-trips with its id as a plain string`() {
        val player = Player(PlayerId("p1"), "Gaëlle", Role.BaseMutant, Genome.Host, PlayerState.Mutated)
        val json = EngineJson.encodeToString(player)
        assertEquals("""{"id":"p1","name":"Gaëlle","role":"baseMutant","genome":"host","state":"mutated","alive":true}""", json)
        assertEquals(player, EngineJson.decodeFromString<Player>(json))
    }

    @Test
    fun `composition and settings round-trip`() {
        val composition = Composition(roles = mapOf(Role.BaseMutant to 1, Role.Doctor to 3), extraHosts = 2, resistants = 1)
        assertEquals(composition, EngineJson.decodeFromString<Composition>(EngineJson.encodeToString(composition)))
        val settings = GameSettings(guidedCall = false, debateTimerMinutes = 8, nightOrder = NightStep.DefaultOrder.reversed())
        assertEquals(settings, EngineJson.decodeFromString<GameSettings>(EngineJson.encodeToString(settings)))
    }
}
