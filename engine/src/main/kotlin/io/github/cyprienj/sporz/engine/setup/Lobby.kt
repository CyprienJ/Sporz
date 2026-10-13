package io.github.cyprienj.sporz.engine.setup

import io.github.cyprienj.sporz.engine.Rules
import io.github.cyprienj.sporz.engine.model.PlayerId
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Why a player cannot be added or renamed in the lobby. */
@Serializable
public enum class SeatProblem {
    @SerialName("blankName") BlankName,
    @SerialName("nameTooLong") NameTooLong,

    /** R-SET-2 */
    @SerialName("nameTaken") NameTaken,

    /** R-SET-1 */
    @SerialName("lobbyFull") LobbyFull,
}

/** Lobby limits (docs/openapi.yaml `JoinRequest.name`). */
public object Lobby {
    public const val MAX_NAME_LENGTH: Int = 24

    /** Problem with adding a player called [name] to [seats], or null if it is fine. */
    public fun checkNewSeat(name: String, seats: List<Seat>): SeatProblem? =
        if (seats.size >= Rules.MAX_PLAYERS) SeatProblem.LobbyFull else checkName(name, seats, ignoring = null)

    /** Problem with renaming [id] to [name], or null if it is fine. */
    public fun checkRename(id: PlayerId, name: String, seats: List<Seat>): SeatProblem? = checkName(name, seats, ignoring = id)

    private fun checkName(name: String, seats: List<Seat>, ignoring: PlayerId?): SeatProblem? {
        val trimmed = name.trim()
        return when {
            trimmed.isEmpty() -> SeatProblem.BlankName
            trimmed.length > MAX_NAME_LENGTH -> SeatProblem.NameTooLong
            seats.any { it.id != ignoring && normalizedName(it.name) == normalizedName(trimmed) } -> SeatProblem.NameTaken
            else -> null
        }
    }
}
