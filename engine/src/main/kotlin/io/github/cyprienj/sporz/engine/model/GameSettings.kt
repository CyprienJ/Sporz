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

@Serializable
public enum class GameMode {
    @SerialName("offline") Offline,
    @SerialName("semiOnline") SemiOnline,
    @SerialName("online") Online,
}

@Serializable
public enum class AutomationPreset {
    @SerialName("manual") Manual,
    @SerialName("assisted") Assisted,
    @SerialName("automatic") Automatic,

    /** Any toggle changed by hand. */
    @SerialName("custom") Custom,
}

/**
 * Automation settings of the *Paramètres* screen (README §3, docs/openapi.yaml
 * `GameSettings`). Defaults are the *Assisté* preset of the design.
 */
@Serializable
public data class GameSettings(
    val preset: AutomationPreset = AutomationPreset.Assisted,
    // Night
    val nightActionsOnPhone: Boolean = true,
    val guidedCall: Boolean = true,
    val vibrate: Boolean = true,
    // Information to players
    val autoReports: Boolean = false,
    val notifyMutation: Boolean = false,
    val notebook: Boolean = true,
    val turnReminder: Boolean = true,
    // Votes
    val voteOnPhone: Boolean = true,
    val autoCloseVote: Boolean = false,
    val liveTally: Boolean = false,
    // Privacy
    val autoHideRole: Boolean = true,
    // Flow
    val debateTimerMinutes: Int = DEFAULT_DEBATE_MINUTES,
    val nightOrder: List<NightStep> = NightStep.DefaultOrder,
) {
    init {
        require(debateTimerMinutes in 0..MAX_DEBATE_MINUTES) { "debateTimerMinutes must be 0..$MAX_DEBATE_MINUTES (0 = off)" }
        require(nightOrder.sorted() == NightStep.entries) { "nightOrder must contain every step exactly once" }
    }

    /** Same settings with the toggles of [preset]; timer and night order are kept. */
    public fun withPreset(preset: AutomationPreset): GameSettings = when (preset) {
        AutomationPreset.Custom -> copy(preset = preset)
        else -> forPreset(preset).copy(debateTimerMinutes = debateTimerMinutes, nightOrder = nightOrder)
    }

    public companion object {
        public const val DEFAULT_DEBATE_MINUTES: Int = 5
        public const val MAX_DEBATE_MINUTES: Int = 20

        /** Toggle values of each preset, as in design.html. */
        public fun forPreset(preset: AutomationPreset): GameSettings = when (preset) {
            AutomationPreset.Manual -> GameSettings(
                preset = preset,
                nightActionsOnPhone = false, guidedCall = false, vibrate = false,
                autoReports = false, notifyMutation = false, notebook = false, turnReminder = false,
                voteOnPhone = false, autoCloseVote = false, liveTally = false,
                autoHideRole = true,
            )
            AutomationPreset.Assisted, AutomationPreset.Custom -> GameSettings(preset = preset)
            AutomationPreset.Automatic -> GameSettings(
                preset = preset,
                autoReports = true, notifyMutation = true, autoCloseVote = true,
            )
        }
    }
}
