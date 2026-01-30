package dev.mlzzen.kaiqiu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.mlzzen.kaiqiu.ui.navigation.KaiqiuNavHost
import dev.mlzzen.kaiqiu.ui.navigation.Screen
import dev.mlzzen.kaiqiu.ui.state.LocalUserState
import dev.mlzzen.kaiqiu.ui.state.UserState
import dev.mlzzen.kaiqiu.ui.state.rememberUserState
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
fun KaiqiuApp() {
    val context = LocalContext.current
    val userState: UserState = rememberUserState(context)

    CompositionLocalProvider(LocalUserState provides userState) {
        val navController = rememberNavController()
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

        Box(modifier = Modifier.fillMaxSize()) {
            KaiqiuNavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = if (showBottomBar) {
                    Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                } else {
                    Modifier
                }
            )

            if (showBottomBar) {
                NavigationBar(
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    items.forEach { (route, label, icon) ->
                        NavigationBarItem(
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
        }
    }
}
