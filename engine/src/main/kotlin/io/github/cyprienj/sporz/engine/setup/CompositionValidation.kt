package io.github.cyprienj.sporz.engine.setup

import io.github.cyprienj.sporz.engine.Rules
import io.github.cyprienj.sporz.engine.model.Composition
import io.github.cyprienj.sporz.engine.model.Role
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Something wrong with a composition. The UI turns each case into a sentence. */
@Serializable
public sealed interface CompositionProblem {
    /** Errors prevent the deal; warnings are only shown. */
    public val isError: Boolean get() = true

    /** R-SET-1 */
    @Serializable @SerialName("playerCount")
    public data class PlayerCountOutOfRange(val playerCount: Int, val min: Int, val max: Int) : CompositionProblem

    /** §2.2 */
    @Serializable @SerialName("roleCount")
    public data class RoleCountOutOfRange(val role: Role, val count: Int, val min: Int, val max: Int) : CompositionProblem

    /** R-SET-3: [missingSeats] = chosen roles − players. */
    @Serializable @SerialName("tooManyRoles")
    public data class TooManyRoles(val missingSeats: Int) : CompositionProblem

    /** R-SET-7: counts cannot be negative. */
    @Serializable @SerialName("negativeGenomeCount")
    public data object NegativeGenomeCount : CompositionProblem

    /** R-SET-8: hosts and resistants are drawn among players who are neither base mutant nor doctor. */
    @Serializable @SerialName("tooManyGenomes")
    public data class TooManyGenomes(val requested: Int, val available: Int) : CompositionProblem

    /** R-SET-4 (warning). */
    @Serializable @SerialName("hackerWithoutTarget")
    public data object HackerWithoutTarget : CompositionProblem {
        override val isError: Boolean get() = false
    }
}

@Serializable
public data class CompositionCheck(
    val playerCount: Int,
    /** R-SET-3; negative when too many roles are chosen. */
    val astronauts: Int,
    val problems: List<CompositionProblem>,
) {
    val errors: List<CompositionProblem> get() = problems.filter { it.isError }
    val warnings: List<CompositionProblem> get() = problems.filterNot { it.isError }

    /** True when the roles can be dealt (warnings allowed). */
    val isValid: Boolean get() = errors.isEmpty()
}

/** Checks [this] composition for [playerCount] players against docs/rules.md §2. */
public fun Composition.check(playerCount: Int): CompositionCheck {
    val problems = buildList {
        if (!Rules.isValidPlayerCount(playerCount)) {
            add(CompositionProblem.PlayerCountOutOfRange(playerCount, Rules.MIN_PLAYERS, Rules.MAX_PLAYERS))
        }

        Role.entries.filter { it.isChosen }.forEach { role ->
            val count = count(role)
            if (count !in role.minCount..role.maxCount) {
                add(CompositionProblem.RoleCountOutOfRange(role, count, role.minCount, role.maxCount))
            }
        }

        val astronauts = astronauts(playerCount)
        if (astronauts < 0) add(CompositionProblem.TooManyRoles(-astronauts))

        if (extraHosts < 0 || resistants < 0) {
            add(CompositionProblem.NegativeGenomeCount)
        } else {
            val available = (playerCount - count(Role.BaseMutant) - count(Role.Doctor)).coerceAtLeast(0)
            if (extraHosts + resistants > available) {
                add(CompositionProblem.TooManyGenomes(extraHosts + resistants, available))
            }
        }

        if (count(Role.Hacker) > 0 && hackableRoles().none { count(it) > 0 }) {
            add(CompositionProblem.HackerWithoutTarget)
        }
    }
    return CompositionCheck(playerCount, astronauts(playerCount), problems)
}

/** Roles whose report the hacker can copy (R-NIGHT-18). */
public fun Composition.hackableRoles(): List<Role> = buildList {
    add(Role.Psychologist)
    add(Role.Geneticist)
    add(Role.ComputerScientist)
    if (variants.hackerCanHackSpy) add(Role.Spy)
}
