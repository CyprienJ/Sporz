package io.github.cyprienj.sporz.ui.end

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.cyprienj.sporz.data.GameSession
import io.github.cyprienj.sporz.engine.day.VoteResult
import io.github.cyprienj.sporz.engine.game.GameState
import io.github.cyprienj.sporz.engine.game.JournalEvent
import io.github.cyprienj.sporz.engine.game.Phase
import io.github.cyprienj.sporz.engine.model.PlayerId
import io.github.cyprienj.sporz.engine.night.HealOutcome
import io.github.cyprienj.sporz.engine.night.NightEvent
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/** What became of a player, for the revelations (design: "Éjectée J2", "Guérie N2", "Vivant"). */
sealed interface Fate {
    data object Alive : Fate
    data class Ejected(val day: Int) : Fate
    data class Dead(val night: Int) : Fate
    data class Healed(val night: Int) : Fate
}

/** End of game (ticket C11). */
@HiltViewModel
class GameEndViewModel @Inject constructor(private val session: GameSession) : ViewModel() {

    val game: StateFlow<GameState?> = session.game

    /** New setup with the same crew; the caller opens the composition screen. */
    fun replaySameCrew() = session.replaySameCrew()

    companion object {
        fun fates(game: GameState): Map<PlayerId, Fate> {
            val ejected = mutableMapOf<PlayerId, Int>()
            val killed = mutableMapOf<PlayerId, Int>()
            val healed = mutableMapOf<PlayerId, Int>()
            game.journal.forEach { entry ->
                when (val event = entry.event) {
                    is JournalEvent.VoteClosed -> (event.result as? VoteResult.Ejected)?.let { ejected[it.player] = entry.round }
                    is JournalEvent.Dawn -> event.deaths.forEach { killed[it] = entry.round }
                    is JournalEvent.StepPlayed -> event.events
                        .filter { it is NightEvent.Heal && it.outcome == HealOutcome.Healed }
                        .forEach { healed[it.target] = entry.round }
                    else -> Unit
                }
            }
            return game.players.associate { player ->
                player.id to when {
                    player.id in ejected -> Fate.Ejected(ejected.getValue(player.id))
                    player.id in killed -> Fate.Dead(killed.getValue(player.id))
                    player.alive && player.id in healed -> Fate.Healed(healed.getValue(player.id))
                    else -> Fate.Alive
                }
            }
        }

        /** Phase in which the game ended: the one of the last entry before the end. */
        fun endedIn(game: GameState): Phase =
            game.journal.lastOrNull { it.phase != Phase.Ended }?.phase ?: Phase.Day
    }
}
