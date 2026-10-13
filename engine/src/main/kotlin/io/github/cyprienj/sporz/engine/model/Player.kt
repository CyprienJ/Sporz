package io.github.cyprienj.sporz.engine.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/** Fixed for the whole game (§1, §2.3). */
@Serializable
public enum class Genome {
    @SerialName("host") Host,
    @SerialName("normal") Normal,
    @SerialName("resistant") Resistant,
}

/**
 * Healthy or mutated (§1). Death is tracked separately ([Player.alive]):
 * a dead player keeps the state they had when they died, so it can be
 * revealed (R-NIGHT-21, R-DAY-11).
 */
@Serializable
public enum class PlayerState {
    @SerialName("healthy") Healthy,
    @SerialName("mutated") Mutated,
}

@Serializable
public enum class Camp {
    @SerialName("healthy") Healthy,
    @SerialName("mutant") Mutant,
}

@Serializable
@JvmInline
public value class PlayerId(public val value: String) {
    override fun toString(): String = value
}

@Serializable
public data class Player(
    val id: PlayerId,
    val name: String,
    val role: Role,
    val genome: Genome,
    val state: PlayerState,
    val alive: Boolean = true,
) {
    val isMutated: Boolean get() = state == PlayerState.Mutated

    /**
     * Mutant camp = mutated players + the traitor while not mutated (§1,
     * DEC-7). A healed traitor is back in the mutant camp (R-SET-12).
     */
    val camp: Camp
        get() = if (isMutated || role == Role.Traitor) Camp.Mutant else Camp.Healthy

    /**
     * Whether the player can use their role's night power: alive and not
     * mutated (R-NIGHT-4, DEC-4). Mutants act together, not through their role.
     */
    val hasRolePower: Boolean get() = alive && !isMutated
}
