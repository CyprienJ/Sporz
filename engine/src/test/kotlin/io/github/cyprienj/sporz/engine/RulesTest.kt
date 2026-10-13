package io.github.cyprienj.sporz.engine

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class RulesTest {

    @ParameterizedTest
    @ValueSource(ints = [7, 10, 20])
    fun `7 to 20 players are allowed (R-SET-1)`(count: Int) {
        assertTrue(Rules.isValidPlayerCount(count))
    }

    @ParameterizedTest
    @ValueSource(ints = [0, 6, 21])
    fun `fewer than 7 or more than 20 players are refused (R-SET-1)`(count: Int) {
        assertFalse(Rules.isValidPlayerCount(count))
    }
}
