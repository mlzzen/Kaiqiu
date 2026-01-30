package dev.mlzzen.kaiqiu.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import dev.mlzzen.kaiqiu.ui.screens.event.EventDetailScreen
import dev.mlzzen.kaiqiu.ui.screens.event.EventListScreen
import dev.mlzzen.kaiqiu.ui.screens.event.EventMembersScreen
import dev.mlzzen.kaiqiu.ui.screens.home.HomeScreen
import dev.mlzzen.kaiqiu.ui.screens.login.LoginScreen
import dev.mlzzen.kaiqiu.ui.screens.match.ScoreEntryScreen
import dev.mlzzen.kaiqiu.ui.screens.match.GroupScoreScreen
import dev.mlzzen.kaiqiu.ui.screens.profile.AboutScreen
import dev.mlzzen.kaiqiu.ui.screens.profile.CitySelectScreen
import dev.mlzzen.kaiqiu.ui.screens.profile.ProfileScreen
import dev.mlzzen.kaiqiu.ui.screens.search.SearchScreen
import dev.mlzzen.kaiqiu.ui.screens.search.Top100Screen
import dev.mlzzen.kaiqiu.ui.screens.search.TopSearchScreen
import dev.mlzzen.kaiqiu.ui.screens.search.RankScreen
import dev.mlzzen.kaiqiu.ui.screens.search.GymScreen
import dev.mlzzen.kaiqiu.ui.screens.user.FavoritesScreen
import dev.mlzzen.kaiqiu.ui.screens.user.FollowedPlayersScreen
import dev.mlzzen.kaiqiu.ui.screens.user.UserDetailScreen
import dev.mlzzen.kaiqiu.ui.screens.user.UserEventsScreen

@Composable
fun KaiqiuNavHost(
    navController: NavHostController,
    startDestination: String = Screen.Home.route,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Auth
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // Main
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToSearch = { navController.navigate(Screen.Search.route) },
                onNavigateToEvent = { eventid ->
                    navController.navigate(Screen.EventDetail.createRoute(eventid))
                },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                onNavigateToCitySelect = {
                    navController.navigate(Screen.CitySelect.route)
                }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToUserDetail = { uid ->
                    navController.navigate(Screen.UserDetail.createRoute(uid))
                },
                onNavigateToUserEvents = { uid ->
                    navController.navigate(Screen.UserEvents.createRoute(uid))
                },
                onNavigateToCitySelect = {
                    navController.navigate(Screen.CitySelect.route)
                },
                onNavigateToAbout = {
                    navController.navigate(Screen.About.route)
                }
            )
        }

        composable(Screen.Search.route) {
            SearchScreen(
                onNavigateToEvent = { eventid ->
                    navController.navigate(Screen.EventDetail.createRoute(eventid))
                },
                onNavigateToUser = { uid ->
                    navController.navigate(Screen.UserDetail.createRoute(uid))
                }
            )
        }

        composable(Screen.Favorites.route) {
            FavoritesScreen(
                onNavigateToUser = { uid ->
                    navController.navigate(Screen.UserDetail.createRoute(uid))
                }
            )
        }

        composable(Screen.TopSearch.route) {
            TopSearchScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToRank = {
                    navController.navigate(Screen.Rank.route)
                },
                onNavigateToTop100 = { tid, name ->
                    navController.navigate(Screen.Top100.route)
                }
            )
        }

        composable(Screen.Top100.route) {
            Top100Screen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToUser = { uid ->
                    navController.navigate(Screen.UserDetail.createRoute(uid))
                }
            )
        }

        composable(Screen.Rank.route) {
            RankScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToUser = { uid ->
                    navController.navigate(Screen.UserDetail.createRoute(uid))
                }
            )
        }

        composable(
            route = Screen.Gym.route,
            arguments = listOf(navArgument("arenaid") { type = NavType.StringType })
        ) { backStackEntry ->
            val arenaid = backStackEntry.arguments?.getString("arenaid") ?: ""
            GymScreen(
                arenaid = arenaid,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEvent = { eventid ->
                    navController.navigate(Screen.EventDetail.createRoute(eventid))
                }
            )
        }

        // User
        composable(
            route = Screen.UserDetail.route,
            arguments = listOf(navArgument("uid") { type = NavType.StringType })
        ) { backStackEntry ->
            val uid = backStackEntry.arguments?.getString("uid") ?: ""
            UserDetailScreen(
                uid = uid,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEvents = {
                    navController.navigate(Screen.UserEvents.createRoute(uid))
                },
                onNavigateToUser = { targetUid ->
                    navController.navigate(Screen.UserDetail.createRoute(targetUid))
                },
                onNavigateToEvent = { eventid ->
                    navController.navigate(Screen.EventDetail.createRoute(eventid))
                }
            )
        }

        composable(
            route = Screen.UserEvents.route,
            arguments = listOf(navArgument("uid") { type = NavType.StringType })
        ) { backStackEntry ->
            val uid = backStackEntry.arguments?.getString("uid") ?: ""
            UserEventsScreen(
                uid = uid,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEvent = { eventid ->
                    navController.navigate(Screen.EventDetail.createRoute(eventid))
                }
            )
        }

        composable(Screen.FollowedPlayers.route) {
            FollowedPlayersScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToUser = { uid ->
                    navController.navigate(Screen.UserDetail.createRoute(uid))
                }
            )
        }

        // Event
        composable(Screen.EventList.route) {
            EventListScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEvent = { eventid ->
                    navController.navigate(Screen.EventDetail.createRoute(eventid))
                }
            )
        }

        composable(
            route = Screen.EventDetail.route,
            arguments = listOf(navArgument("eventid") { type = NavType.StringType })
        ) { backStackEntry ->
            val eventid = backStackEntry.arguments?.getString("eventid") ?: ""
            EventDetailScreen(
                eventid = eventid,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToMembers = { matchId, itemId ->
                    navController.navigate(Screen.EventMembers.createRoute(eventid, matchId, itemId))
                },
                onNavigateToScore = { itemid ->
                    navController.navigate(Screen.ScoreEntry.createRoute(eventid, itemid))
                }
            )
        }

        composable(
            route = Screen.EventMembers.route,
            arguments = listOf(
                navArgument("eventid") { type = NavType.StringType },
                navArgument("matchId") { type = NavType.StringType },
                navArgument("itemId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val eventid = backStackEntry.arguments?.getString("eventid") ?: ""
            val matchId = backStackEntry.arguments?.getString("matchId") ?: ""
            val itemId = backStackEntry.arguments?.getString("itemId") ?: ""
            EventMembersScreen(
                eventid = eventid,
                matchId = matchId,
                itemId = itemId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToUser = { uid ->
                    navController.navigate(Screen.UserDetail.createRoute(uid))
                }
            )
        }

        composable(
            route = Screen.ScoreEntry.route,
            arguments = listOf(
                navArgument("eventid") { type = NavType.StringType },
                navArgument("itemid") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val eventid = backStackEntry.arguments?.getString("eventid") ?: ""
            val itemid = backStackEntry.arguments?.getString("itemid") ?: ""
            ScoreEntryScreen(
                eventid = eventid,
                itemid = itemid,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToGroupScore = {
                    navController.navigate(Screen.GroupScore.createRoute(eventid, itemid))
                }
            )
        }

        composable(
            route = Screen.GroupScore.route,
            arguments = listOf(
                navArgument("eventid") { type = NavType.StringType },
                navArgument("itemid") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val eventid = backStackEntry.arguments?.getString("eventid") ?: ""
            val itemid = backStackEntry.arguments?.getString("itemid") ?: ""
            GroupScoreScreen(
                eventid = eventid,
                itemid = itemid,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToScoreEntry = {
                    navController.navigate(Screen.ScoreEntry.createRoute(eventid, itemid))
                }
            )
        }

        // Profile
        composable(Screen.CitySelect.route) {
            CitySelectScreen(
                onNavigateBack = { navController.popBackStack() },
                onCitySelected = { navController.popBackStack() }
            )
        }

        composable(Screen.About.route) {
            AboutScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
