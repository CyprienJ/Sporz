package io.github.cyprienj.sporz.navigation

import kotlinx.serialization.Serializable

// One route per board of design.html. Game data is not passed as route
// arguments: it lives in the game repository (ticket E2).

// Common
@Serializable data object Home
@Serializable data object RoleGuide
@Serializable data object RecentGames
@Serializable data class PastGame(val id: Long) // a finished game of RecentGames
@Serializable data class PastJournal(val id: Long)
@Serializable data object GameEnd

// Game Master: setup
@Serializable data object GameMode
@Serializable data object Lobby
@Serializable data object Composition
@Serializable data object Distribution
@Serializable data object Settings

// Game Master: running game
@Serializable data object Night
@Serializable data object Day
@Serializable data object PlayerBoard
@Serializable data object Journal

// Player
@Serializable data object Join
@Serializable data object WaitingRoom
@Serializable data object MyRole
@Serializable data object NightAction
@Serializable data object Vote
