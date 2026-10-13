package io.github.cyprienj.sporz.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.cyprienj.sporz.BuildConfig
import io.github.cyprienj.sporz.ui.catalog.DesignCatalogScreen
import io.github.cyprienj.sporz.ui.end.GameEndScreen
import io.github.cyprienj.sporz.ui.gm.game.DayScreen
import io.github.cyprienj.sporz.ui.gm.game.JournalScreen
import io.github.cyprienj.sporz.ui.gm.game.NightScreen
import io.github.cyprienj.sporz.ui.gm.game.PlayerBoardScreen
import io.github.cyprienj.sporz.ui.gm.settings.SettingsRoute
import io.github.cyprienj.sporz.ui.gm.setup.CompositionRoute
import io.github.cyprienj.sporz.ui.gm.setup.DistributionRoute
import io.github.cyprienj.sporz.ui.gm.setup.GameModeRoute
import io.github.cyprienj.sporz.ui.gm.setup.LobbyRoute
import io.github.cyprienj.sporz.ui.guide.RoleGuideScreen
import io.github.cyprienj.sporz.ui.history.RecentGamesScreen
import io.github.cyprienj.sporz.ui.home.HomeRoute
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
                onJoinGame = { navController.navigate(Join) },
                onOpenRoleGuide = { navController.navigate(RoleGuide) },
                onOpenRecentGames = { navController.navigate(RecentGames) },
                onOpenDesignCatalog = if (BuildConfig.DEBUG) {
                    { navController.navigate(DesignCatalog) }
                } else null,
            )
        }
        composable<RoleGuide> { RoleGuideScreen() }
        composable<DesignCatalog> { DesignCatalogScreen(onBack = { navController.popBackStack() }) }
        composable<RecentGames> { RecentGamesScreen() }
        composable<GameEnd> {
            GameEndScreen(
                onReplaySameCrew = { navController.startFresh(Lobby) },
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
            DistributionRoute(onStartFirstNight = { navController.startFresh(Night) })
        }
        composable<Settings> { SettingsRoute(onBack = { navController.popBackStack() }) }

        // Game Master: running game
        composable<Night> {
            NightScreen(
                onEndNight = { navController.replaceCurrent(Day) },
                onOpenPlayerBoard = { navController.navigate(PlayerBoard) },
                onOpenJournal = { navController.navigate(Journal) },
                onOpenSettings = { navController.navigate(Settings) },
            )
        }
        composable<Day> {
            DayScreen(
                onStartNight = { navController.replaceCurrent(Night) },
                onGameOver = { navController.startFresh(GameEnd) },
                onOpenPlayerBoard = { navController.navigate(PlayerBoard) },
                onOpenJournal = { navController.navigate(Journal) },
            )
        }
        composable<PlayerBoard> { PlayerBoardScreen() }
        composable<Journal> { JournalScreen() }

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
