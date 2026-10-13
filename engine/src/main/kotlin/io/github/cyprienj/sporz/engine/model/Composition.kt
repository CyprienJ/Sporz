package io.github.cyprienj.sporz.engine.model

import kotlinx.serialization.Serializable

/** Optional rules of §6. Defaults follow the table of §6. */
@Serializable
public data class Variants(
    /** R-NIGHT-18, R-NIGHT-19 */
    val hackerCanHackSpy: Boolean = false,
    /** R-NIGHT-21, R-DAY-11 */
    val revealOnElimination: Boolean = true,
    /** R-DAY-7, R-DAY-9 */
    val blankVoteAllowed: Boolean = true,
    /** R-DAY-4 to R-DAY-6, R-DAY-10 */
    val captain: Boolean = false,
)

/**
 * What the MJ chooses before the deal (composition screen). Astronauts are
 * not stored: they fill the seats left by [roles] (R-SET-3). Validation is
 * done by ticket B2.
 */
@Serializable
public data class Composition(
    /** Count of each chosen role ([Role.isChosen]); missing roles count 0. */
    val roles: Map<Role, Int> = mapOf(Role.BaseMutant to 1, Role.Doctor to Role.DEFAULT_DOCTORS),
    /** Hosts on top of the base mutant (R-SET-7). */
    val extraHosts: Int = 0,
    /** R-SET-7 */
    val resistants: Int = 0,
    val variants: Variants = Variants(),
) {
    public fun count(role: Role): Int = roles[role] ?: 0

    /** Number of chosen roles, base mutant included. */
    val chosenRoleCount: Int get() = roles.filterKeys { it.isChosen }.values.sum()

    /** R-SET-3: may be negative when too many roles are chosen. */
    public fun astronauts(playerCount: Int): Int = playerCount - chosenRoleCount
}
