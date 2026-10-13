package io.github.cyprienj.sporz.engine

import io.github.cyprienj.sporz.engine.model.Camp
import io.github.cyprienj.sporz.engine.model.Player
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** How a game ended (docs/rules.md §5). */
@Serializable
public enum class GameEnd(public val winner: Camp?) {
    /** R-WIN-1: every mutated player is dead or healed, and the traitor is dead. */
    @SerialName("allMutantsGone") HealthyWin(Camp.Healthy),

    /** R-WIN-2: every living player is in the mutant camp. */
    @SerialName("allHealthyGone") MutantsWin(Camp.Mutant),

    /** R-WIN-3 */
    @SerialName("noSurvivors") NoSurvivors(null),

    /** R-WIN-4: the MJ ended the game. */
    @SerialName("aborted") Aborted(null),
}

/**
 * Checks the victory conditions (R-WIN-1 to R-WIN-3). Call it after the end
 * of each night (R-NIGHT-20) and after each vote (R-DAY-12).
 *
 * @return how the game ended, or null if it goes on.
 */
public fun checkVictory(players: List<Player>): GameEnd? {
    val alive = players.filter { it.alive }
    return when {
        alive.isEmpty() -> GameEnd.NoSurvivors
        alive.none { it.camp == Camp.Mutant } -> GameEnd.HealthyWin
        alive.all { it.camp == Camp.Mutant } -> GameEnd.MutantsWin
        else -> null
    }
}
