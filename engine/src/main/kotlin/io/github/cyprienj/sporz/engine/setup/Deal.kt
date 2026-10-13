package io.github.cyprienj.sporz.engine.setup

import io.github.cyprienj.sporz.engine.model.Composition
import io.github.cyprienj.sporz.engine.model.Genome
import io.github.cyprienj.sporz.engine.model.Player
import io.github.cyprienj.sporz.engine.model.PlayerId
import io.github.cyprienj.sporz.engine.model.PlayerState
import io.github.cyprienj.sporz.engine.model.Role
import kotlinx.serialization.Serializable
import kotlin.random.Random

/** A player in the lobby, before the deal. */
@Serializable
public data class Seat(val id: PlayerId, val name: String)

/** The deal was asked with an invalid composition; [check] says why. */
public class InvalidCompositionException(public val check: CompositionCheck) :
    IllegalArgumentException("Invalid composition: ${check.errors}")

/** R-SET-2: names are compared trimmed and case-insensitively. */
public fun normalizedName(name: String): String = name.trim().lowercase()

/**
 * Deals roles then genomes at random (R-SET-9) and sets the initial states
 * (R-SET-10). Players are returned in the order of [seats].
 *
 * @param random pass a seeded `Random(seed)` in tests for reproducible deals.
 * @throws InvalidCompositionException if [composition] is not valid for these seats.
 * @throws IllegalArgumentException if two seats share an id or a name (R-SET-2).
 */
public fun deal(seats: List<Seat>, composition: Composition, random: Random): List<Player> {
    require(seats.map { it.id }.toSet().size == seats.size) { "Player ids must be unique" }
    require(seats.map { normalizedName(it.name) }.toSet().size == seats.size) { "Player names must be unique (R-SET-2)" }
    val check = composition.check(seats.size)
    if (!check.isValid) throw InvalidCompositionException(check)

    // Roles (R-SET-9)
    val roles = buildList {
        Role.entries.filter { it.isChosen }.forEach { role -> repeat(composition.count(role)) { add(role) } }
        repeat(check.astronauts) { add(Role.Astronaut) }
    }.shuffled(random)

    // Genomes (R-SET-5 to R-SET-7): drawn among players who are neither base mutant nor doctor.
    val eligible = seats.indices.filter { roles[it] != Role.BaseMutant && roles[it] != Role.Doctor }.shuffled(random)
    val hosts = eligible.take(composition.extraHosts).toSet()
    val resistants = eligible.drop(composition.extraHosts).take(composition.resistants).toSet()

    return seats.mapIndexed { index, seat ->
        val role = roles[index]
        Player(
            id = seat.id,
            name = seat.name.trim(),
            role = role,
            genome = when {
                role == Role.BaseMutant -> Genome.Host
                index in hosts -> Genome.Host
                index in resistants -> Genome.Resistant
                else -> Genome.Normal
            },
            state = if (role == Role.BaseMutant) PlayerState.Mutated else PlayerState.Healthy,
        )
    }
}
