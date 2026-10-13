package io.github.cyprienj.sporz.engine.night

import io.github.cyprienj.sporz.engine.model.PlayerId
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Something that happened to a player during the night. Read by the spy
 * (R-NIGHT-19), the dawn announcement (R-NIGHT-21) and the journal.
 */
@Serializable
public sealed interface NightEvent {
    public val target: PlayerId

    /** R-NIGHT-8: [succeeded] is false when the target is resistant (the action is still used up). */
    @Serializable @SerialName("mutation")
    public data class Mutation(override val target: PlayerId, val succeeded: Boolean) : NightEvent

    /** R-NIGHT-6, R-NIGHT-14 */
    @Serializable @SerialName("kill")
    public data class Kill(override val target: PlayerId, val by: Killer) : NightEvent

    /** R-NIGHT-13: one event per healed player (the doctors heal together). */
    @Serializable @SerialName("heal")
    public data class Heal(override val target: PlayerId, val outcome: HealOutcome) : NightEvent
}

@Serializable
public enum class Killer {
    @SerialName("mutants") Mutants,
    @SerialName("doctors") Doctors,
}

@Serializable
public enum class HealOutcome {
    /** Was mutated, now healthy. */
    @SerialName("healed") Healed,

    /** Mutated host: cannot be healed. */
    @SerialName("host") Host,

    /** Was not mutated: nothing to heal. */
    @SerialName("notMutated") NotMutated,
}

/** Players who died this night, in order, for the dawn announcement (R-NIGHT-21). */
public fun List<NightEvent>.deaths(): List<PlayerId> = filterIsInstance<NightEvent.Kill>().map { it.target }.distinct()
