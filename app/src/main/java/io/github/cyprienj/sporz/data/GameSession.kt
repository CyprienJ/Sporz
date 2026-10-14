package io.github.cyprienj.sporz.data

import io.github.cyprienj.sporz.data.history.GameHistory
import io.github.cyprienj.sporz.di.ApplicationScope
import io.github.cyprienj.sporz.engine.game.GameState
import io.github.cyprienj.sporz.engine.game.JournalEvent
import io.github.cyprienj.sporz.engine.game.Phase
import io.github.cyprienj.sporz.engine.model.Composition
import io.github.cyprienj.sporz.engine.model.GameSettings
import io.github.cyprienj.sporz.engine.model.PlayerId
import io.github.cyprienj.sporz.engine.setup.Lobby
import io.github.cyprienj.sporz.engine.setup.SeatProblem
import io.github.cyprienj.sporz.engine.setup.Seat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.util.UUID
import kotlin.random.Random
import javax.inject.Inject
import javax.inject.Singleton

/** A game being set up by the MJ, before the deal (lobby, composition, settings). */
@Serializable
data class SetupDraft(
    val seats: List<Seat> = emptyList(),
    val composition: Composition? = null,
    val settings: GameSettings = GameSettings(),
)

/**
 * The game of this phone, shared by the MJ screens. It is restored from [store] when created and saved there after
 * every change, so a game survives the app being killed; finished games go
 * to [history].
 */
@Singleton
class GameSession @Inject constructor(
    private val store: SessionStore,
    private val history: GameHistory,
    @ApplicationScope private val scope: CoroutineScope,
) {
    /** Nothing saved: unit tests. */
    constructor() : this(SessionStore.None, GameHistory.None, CoroutineScope(Dispatchers.Unconfined))

    private val saved = store.load()

    private val _draft = MutableStateFlow(saved?.draft)

    /** Null when no game is being set up. */
    val draft: StateFlow<SetupDraft?> = _draft.asStateFlow()

    /** Starts a setup, or keeps the current one. */
    fun startSetup() {
        _draft.update { it ?: SetupDraft() }
    }

    /** Adds a player; returns why it was refused, or null. */
    fun addSeat(name: String): SeatProblem? {
        val draft = current()
        Lobby.checkNewSeat(name, draft.seats)?.let { return it }
        _draft.value = draft.copy(seats = draft.seats + Seat(PlayerId(UUID.randomUUID().toString()), name.trim()))
        return null
    }

    /** Renames a player; returns why it was refused, or null. */
    fun renameSeat(id: PlayerId, name: String): SeatProblem? {
        val draft = current()
        Lobby.checkRename(id, name, draft.seats)?.let { return it }
        _draft.value = draft.copy(seats = draft.seats.map { if (it.id == id) it.copy(name = name.trim()) else it })
        return null
    }

    fun removeSeat(id: PlayerId) {
        _draft.update { draft -> draft?.copy(seats = draft.seats.filterNot { it.id == id }) }
    }

    /** Settings of the running game, or of the setup before the deal. */
    fun currentSettings(): GameSettings = _game.value?.settings ?: current().settings

    /** Saves the settings in the setup and, once dealt, in the running game (night order: next night). */
    fun updateSettings(settings: GameSettings) {
        _draft.value = current().copy(settings = settings)
        _game.update { it?.copy(settings = settings) }
    }

    fun setComposition(composition: Composition) {
        _draft.value = current().copy(composition = composition)
    }

    private val _game = MutableStateFlow(saved?.game)

    /** The game once the roles are dealt: the whole game runs on this phone. */
    val game: StateFlow<GameState?> = _game.asStateFlow()

    /**
     * Deals the roles of the current setup and starts the game (rules §2.4).
     * Dealing again (back from the distribution) replaces the previous deal.
     *
     * @throws io.github.cyprienj.sporz.engine.setup.InvalidCompositionException if the composition is not valid.
     */
    fun deal(random: Random = Random.Default): GameState {
        val draft = current()
        val composition = checkNotNull(draft.composition) { "No composition chosen" }
        _announced.value = emptySet()
        return GameState.start(draft.seats, composition, draft.settings, random).also { _game.value = it }
    }

    /** Continues an existing game (a saved one, ticket C13). Every role counts as announced. */
    fun resumeGame(game: GameState) {
        _draft.value = SetupDraft(game.players.map { Seat(it.id, it.name) }, game.composition, game.settings)
        _game.value = game
        _announced.value = game.players.map { it.id }.toSet()
    }

    /**
     * "Rejouer avec le même équipage": a new setup with the same players (ids
     * kept), composition and settings; the finished game is forgotten.
     */
    fun replaySameCrew() = replay(checkNotNull(_game.value) { "No game to replay" })

    /** Same as [replaySameCrew] from any game, e.g. one of the recent games (a game in progress is stopped first). */
    fun replay(game: GameState) {
        stopGameInProgress()
        _draft.value = SetupDraft(game.players.map { Seat(it.id, it.name) }, game.composition, game.settings)
        _game.value = null
        _announced.value = emptySet()
    }

    /** Applies a command of the running game (night steps, votes…) and keeps the result. */
    fun updateGame(command: (GameState) -> GameState): GameState {
        val game = checkNotNull(_game.value) { "No game in progress" }
        return command(game).also { next ->
            _game.value = next
            if (game.phase != Phase.Ended && next.phase == Phase.Ended) scope.launch { history.record(next) }
        }
    }

    /**
     * Ends the game in progress as interrupted, so it is kept in the recent games.
     * A deal that was never played (only "game started" in the journal) is just dropped.
     */
    private fun stopGameInProgress() {
        val game = _game.value?.takeIf { hasGameInProgress } ?: return
        if (game.journal.any { it.event !is JournalEvent.GameStarted }) updateGame { it.abort() } // same rule as ResumableGame.played
    }

    /** True when a dealt game is not over yet: the home screen offers to resume it. */
    val hasGameInProgress: Boolean get() = _game.value?.let { it.phase != Phase.Ended } == true

    private val _announced = MutableStateFlow(saved?.announced.orEmpty())

    /** Players whose role the MJ has announced in private (distribution screen). */
    val announced: StateFlow<Set<PlayerId>> = _announced.asStateFlow()

    fun markAnnounced(id: PlayerId) {
        _announced.update { it + id }
    }

    private fun current(): SetupDraft = _draft.value ?: SetupDraft().also { _draft.value = it }

    fun discardSetup() {
        _draft.value = null
    }

    /**
     * "Créer une partie": forgets the previous setup, deal and announcements.
     * A game in progress is first stopped (see [stopGameInProgress]).
     */
    fun startNewGame() {
        stopGameInProgress()
        _draft.value = null
        _game.value = null
        _announced.value = emptySet()
    }

    init {
        // One collector: saves are written in order, the latest state last.
        scope.launch {
            combine(_draft, _game, _announced, ::SavedSession).drop(1).conflate().collect(store::save)
        }
    }
}
