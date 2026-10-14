package io.github.cyprienj.sporz.navigation

import io.github.cyprienj.sporz.BuildConfig
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import io.github.cyprienj.sporz.ui.end.GameEndRoute
import io.github.cyprienj.sporz.ui.gm.game.DayRoute
import io.github.cyprienj.sporz.ui.gm.game.JournalRoute
import io.github.cyprienj.sporz.ui.gm.game.NightRoute
import io.github.cyprienj.sporz.ui.gm.game.PlayerBoardRoute
import io.github.cyprienj.sporz.ui.gm.settings.SettingsRoute
import io.github.cyprienj.sporz.ui.gm.setup.CompositionRoute
import io.github.cyprienj.sporz.ui.gm.setup.DistributionRoute
import io.github.cyprienj.sporz.ui.gm.setup.GameModeRoute
import io.github.cyprienj.sporz.ui.gm.setup.LobbyRoute
import io.github.cyprienj.sporz.ui.guide.RoleGuideScreen
import io.github.cyprienj.sporz.ui.history.PastGameRoute
import io.github.cyprienj.sporz.ui.history.PastJournalRoute
import io.github.cyprienj.sporz.ui.history.RecentGamesRoute
import io.github.cyprienj.sporz.ui.home.HomeRoute
import io.github.cyprienj.sporz.ui.home.ResumeTarget
import io.github.cyprienj.sporz.ui.player.JoinScreen
import io.github.cyprienj.sporz.ui.player.MyRoleScreen
import io.github.cyprienj.sporz.ui.player.NightActionScreen
import io.github.cyprienj.sporz.ui.player.VoteScreen
import io.github.cyprienj.sporz.ui.player.WaitingRoomScreen

@Composable
fun SporzNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(navController = navController, startDestination = Home, modifier = modifier) {
        // Common
        composable<Home> {
            HomeRoute(
                onCreateGame = { navController.navigate(GameMode) },
                onJoinGame = if (BuildConfig.ONLINE_PLAY) {
                    { navController.navigate(Join) }
                } else null,
                onOpenRoleGuide = { navController.navigate(RoleGuide) },
                onOpenRecentGames = { navController.navigate(RecentGames) },
                onResume = { target ->
                    navController.startFresh(
                        when (target) {
                            ResumeTarget.Distribution -> Distribution
                            ResumeTarget.Night -> Night
                            ResumeTarget.Day -> Day
                        },
                    )
                },
            )
        }
        composable<RoleGuide> { RoleGuideScreen(onBack = { navController.popBackStack() }) }
        composable<RecentGames> {
            RecentGamesRoute(
                onBack = { navController.popBackStack() },
                onOpenGame = { id -> navController.navigate(PastGame(id)) },
            )
        }
        composable<PastGame> { entry ->
            val id = entry.toRoute<PastGame>().id
            PastGameRoute(
                onBack = { navController.popBackStack() },
                onReplay = { navController.navigate(Composition) { popUpTo(Home) } },
                onOpenJournal = { navController.navigate(PastJournal(id)) },
            )
        }
        composable<PastJournal> { PastJournalRoute(onBack = { navController.popBackStack() }) }
        composable<GameEnd> {
            GameEndRoute(
                onReplaySameCrew = { navController.navigate(Composition) { popUpTo(Home) } },
                onOpenJournal = { navController.navigate(Journal) },
                onBackHome = { navController.backToHome() },
            )
        }

        // Game Master: setup
        composable<GameMode> {
            GameModeRoute(
                onBack = { navController.popBackStack() },
                onOpenLobby = { navController.navigate(Lobby) },
            )
        }
        composable<Lobby> {
            LobbyRoute(
                onBack = { navController.popBackStack() },
                onChooseRoles = { navController.navigate(Composition) },
                onOpenSettings = { navController.navigate(Settings) },
            )
        }
        composable<Composition> {
            CompositionRoute(
                onBack = { navController.popBackStack() },
                onDealt = { navController.navigate(Distribution) },
            )
        }
        composable<Distribution> {
            // Once the night starts, Back must not return to the setup screens.
            RunningGameScreen { DistributionRoute(onStartFirstNight = { navController.startFresh(Night) }) }
        }
        composable<Settings> { SettingsRoute(onBack = { navController.popBackStack() }) }

        // Game Master: running game
        composable<Night> {
            RunningGameScreen(onLeave = { navController.backToHome() }) {
                NightRoute(
                    onEndNight = { navController.replaceCurrent(Day) },
                    onOpenPlayerBoard = { navController.navigate(PlayerBoard) },
                    onOpenJournal = { navController.navigate(Journal) },
                )
            }
        }
        composable<Day> {
            RunningGameScreen(onLeave = { navController.backToHome() }) {
                DayRoute(
                    onStartNight = { navController.replaceCurrent(Night) },
                    onGameOver = { navController.startFresh(GameEnd) },
                    onOpenPlayerBoard = { navController.navigate(PlayerBoard) },
                    onOpenJournal = { navController.navigate(Journal) },
                )
            }
        }
        composable<PlayerBoard> {
            KeepScreenOn()
            PlayerBoardRoute(
                onBackToPhase = { navController.popBackStack() },
                onOpenJournal = { navController.navigate(Journal) { popUpTo<PlayerBoard> { inclusive = true } } },
            )
        }
        composable<Journal> {
            KeepScreenOn()
            JournalRoute(
                onBackToPhase = { navController.popBackStack() },
                onOpenPlayerBoard = { navController.navigate(PlayerBoard) { popUpTo<Journal> { inclusive = true } } },
            )
        }

        // Player
        composable<Join> {
            JoinScreen(onJoined = { navController.navigate(WaitingRoom) })
        }
        composable<WaitingRoom> {
            WaitingRoomScreen(
                onRoleDealt = { navController.startFresh(MyRole) },
                onOpenRoleGuide = { navController.navigate(RoleGuide) },
                onLeave = { navController.backToHome() },
            )
        }
        composable<MyRole> {
            MyRoleScreen(
                onYourTurn = { navController.navigate(NightAction) },
                onVoteOpened = { navController.navigate(Vote) },
                onOpenPlayers = { navController.navigate(PlayerBoard) },
                onOpenJournal = { navController.navigate(Journal) },
                onGameOver = { navController.startFresh(GameEnd) },
            )
        }
        composable<NightAction> {
            NightActionScreen(onDone = { navController.popBackStack() })
        }
        composable<Vote> {
            VoteScreen(onVoted = { navController.popBackStack() })
        }
    }
}

/** Goes to [route] with only Home below it, so Back leaves the game. */
private fun NavHostController.startFresh(route: Any) = navigate(route) {
    popUpTo(Home)
    launchSingleTop = true
}

/** Replaces the current destination, e.g. Night <-> Day. */
private fun NavHostController.replaceCurrent(route: Any) {
    val current = currentDestination?.id ?: return navigate(route)
    navigate(route) {
        popUpTo(current) { inclusive = true }
        launchSingleTop = true
    }
}

private fun NavHostController.backToHome() {
    popBackStack(Home, inclusive = false)
}
