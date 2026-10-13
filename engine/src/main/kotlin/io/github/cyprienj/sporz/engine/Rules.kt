package io.github.cyprienj.sporz.engine

/** Fixed limits of docs/rules.md. */
public object Rules {
    /** R-SET-1: a game has 7 to 20 players, not counting the MJ. */
    public const val MIN_PLAYERS: Int = 7

    /** R-SET-1 */
    public const val MAX_PLAYERS: Int = 20

    public fun isValidPlayerCount(count: Int): Boolean = count in MIN_PLAYERS..MAX_PLAYERS
}
