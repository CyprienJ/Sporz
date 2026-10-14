package io.github.cyprienj.sporz.engine.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Steps of the night, in default call order (R-NIGHT-1). */
@Serializable
public enum class NightStep {
    @SerialName("mutants") Mutants,
    @SerialName("doctors") Doctors,
    @SerialName("computerScientist") ComputerScientist,
    @SerialName("psychologist") Psychologist,
    @SerialName("geneticist") Geneticist,
    @SerialName("hacker") Hacker,
    @SerialName("spy") Spy,
    ;

    public companion object {
        public val DefaultOrder: List<NightStep> = entries.toList()
    }
}

/**
 * Settings of the *Paramètres* screen (README §3). Saves made when the app
 * still had phone settings load fine: unknown keys are ignored (EngineJson).
 */
@Serializable
public data class GameSettings(
    /** The night screen shows the text the MJ reads to wake each step. */
    val guidedCall: Boolean = true,
    val debateTimerMinutes: Int = DEFAULT_DEBATE_MINUTES,
    val nightOrder: List<NightStep> = NightStep.DefaultOrder,
) {
    init {
        require(debateTimerMinutes in 0..MAX_DEBATE_MINUTES) { "debateTimerMinutes must be 0..$MAX_DEBATE_MINUTES (0 = off)" }
        require(nightOrder.sorted() == NightStep.entries) { "nightOrder must contain every step exactly once" }
    }

    public companion object {
        public const val DEFAULT_DEBATE_MINUTES: Int = 5
        public const val MAX_DEBATE_MINUTES: Int = 20
    }
}
