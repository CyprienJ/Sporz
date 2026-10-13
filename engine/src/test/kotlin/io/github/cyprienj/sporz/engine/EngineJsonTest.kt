package io.github.cyprienj.sporz.engine

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class EngineJsonTest {

    @Serializable
    sealed interface Event

    @Serializable
    data class Eliminated(val playerId: String, val round: Int = 1) : Event

    @Test
    fun `sealed types are tagged with a type field and round-trip`() {
        val event: Event = Eliminated("p1")
        val json = EngineJson.encodeToString(event)

        assertEquals("""{"type":"io.github.cyprienj.sporz.engine.EngineJsonTest.Eliminated","playerId":"p1","round":1}""", json)
        assertEquals(event, EngineJson.decodeFromString<Event>(json))
    }

    @Test
    fun `unknown keys from a newer save are ignored`() {
        val json = """{"playerId":"p1","round":2,"addedLater":true}"""
        assertEquals(Eliminated("p1", 2), EngineJson.decodeFromString<Eliminated>(json))
    }
}
