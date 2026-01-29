package br.com.loterias.cebolaolotofacil.presentation.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import br.com.loterias.cebolaolotofacil.presentation.navigation.Screen
import br.com.loterias.cebolaolotofacil.presentation.theme.CebolaoTheme
import timber.log.Timber

/**
 * Main activity for the application
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            CebolaoTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation()
                }
            }
        }
    }
}

/**
 * Main navigation structure with bottom bar
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                BottomNavItems.forEach { item ->
                    NavigationBarItem(
                        icon = {
                            when (item.route) {
                                "home" -> Icon(Icons.Default.Home, contentDescription = item.label)
                                "generator" -> Icon(Icons.Default.Settings, contentDescription = item.label)
                                "games" -> Icon(Icons.Default.Favorite, contentDescription = item.label)
                                "checker" -> Icon(Icons.Default.CheckCircle, contentDescription = item.label)
                                "about" -> Icon(Icons.Default.Info, contentDescription = item.label)
                                else -> Icon(Icons.Default.Home, contentDescription = item.label)
                            }
                        },
                        label = { Text(item.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            Timber.d("Navigating to ${item.route}")
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(Screen.Home.route) {
                    HomeScreen()
                }
                composable(Screen.Generator.route) {
                    GeneratorScreen()
                }
                composable(Screen.Games.route) {
                    GamesScreen()
                }
                composable(Screen.Checker.route) {
                    CheckerScreen()
                }
                composable(Screen.About.route) {
                    AboutScreen()
                }
            }
        }
    }
}

/**
 * Bottom navigation items configuration
 */
val BottomNavItems = listOf(
    BottomNavItem("home", "Resultados"),
    BottomNavItem("generator", "Gerador"),
    BottomNavItem("games", "Jogos"),
    BottomNavItem("checker", "Verificar"),
    BottomNavItem("about", "Sobre")
)

/**
 * Data class for bottom navigation items
 */
data class BottomNavItem(val route: String, val label: String)
