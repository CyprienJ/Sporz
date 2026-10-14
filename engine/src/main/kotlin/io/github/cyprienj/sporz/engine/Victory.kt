package io.github.cyprienj.sporz.engine

import io.github.cyprienj.sporz.engine.model.Camp
import io.github.cyprienj.sporz.engine.model.Player
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** How a game ended (docs/rules.md §5). */
@Serializable
public enum class GameEnd(public val winner: Camp?) {
    /** R-WIN-1: every mutated player is dead or healed; a living traitor does not prevent it. */
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
        // Mutated players and the traitor (R-WIN-2); the traitor alone left also wins for them.
        alive.all { it.camp == Camp.Mutant } -> GameEnd.MutantsWin
        // The traitor does not count here (DEC-7).
        alive.none { it.isMutated } -> GameEnd.HealthyWin
        else -> null
    }
}
