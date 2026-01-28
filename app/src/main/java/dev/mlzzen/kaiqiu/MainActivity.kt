package dev.mlzzen.kaiqiu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.mlzzen.kaiqiu.ui.navigation.KaiqiuNavHost
import dev.mlzzen.kaiqiu.ui.navigation.Screen
import dev.mlzzen.kaiqiu.ui.state.UserState
import dev.mlzzen.kaiqiu.ui.theme.KaiqiuTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KaiqiuTheme {
                KaiqiuApp()
            }
        }
    }
}

@Composable
fun KaiqiuApp(userState: UserState = viewModel()) {
    val navController = rememberNavController()
    val isLoggedIn by userState.isLoggedIn.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Search.route,
        Screen.Favorites.route,
        Screen.Profile.route
    )

    val items = listOf(
        Triple(Screen.Home.route, "Home", Icons.Default.Home),
        Triple(Screen.Search.route, "Search", Icons.Default.Search),
        Triple(Screen.Favorites.route, "Favorites", Icons.Default.Favorite),
        Triple(Screen.Profile.route, "Profile", Icons.Default.Person)
    )

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            if (showBottomBar) {
                items.forEach { (route, label, icon) ->
                    item(
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label) },
                        selected = currentRoute == route,
                        onClick = {
                            if (currentRoute != route) {
                                navController.navigate(route) {
                                    popUpTo(Screen.Home.route) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        }
    ) {
        KaiqiuNavHost(
            navController = navController,
            startDestination = if (isLoggedIn) Screen.Home.route else Screen.Login.route
        )
    }
}
