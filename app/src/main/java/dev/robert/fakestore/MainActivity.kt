package dev.robert.fakestore

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.navigation.dependency
import com.ramcosta.composedestinations.rememberNavHostEngine
import com.ramcosta.composedestinations.scope.DestinationScope
import com.ramcosta.composedestinations.spec.NavGraphSpec
import dagger.hilt.android.AndroidEntryPoint
import dev.robert.cart.presentation.destinations.CartScreenDestination
//import dev.robert.cart.presentation.destinations.CartScreenDestination
import dev.robert.fakestore.composables.BottomNavItem
import dev.robert.fakestore.navigation.NavGraphsBuilder
import dev.robert.fakestore.navigation.NavigationHelper
import dev.robert.fakestore.ui.theme.FakeStoreTheme
import dev.robert.products.presentation.NavGraphs
import dev.robert.products.presentation.destinations.HomeScreenDestination
import dev.robert.user.presentation.destinations.ProfileScreenDestination
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        setContent {
            FakeStoreTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val showBottomNav: Boolean
                    val navController = rememberNavController()
                    val navHostEngine = rememberNavHostEngine()
                    val newBackStackEntry by navController.currentBackStackEntryAsState()
                    val route = newBackStackEntry?.destination?.route

                    val bottomNavItems = listOf(
                        BottomNavItem.Home,
                        BottomNavItem.Cart,
                        BottomNavItem.Profile
                    )

                    showBottomNav = route in listOf(
                        HomeScreenDestination.route,
                        CartScreenDestination.route,
                        ProfileScreenDestination.route,
                    )
                    Scaffold(
                        bottomBar = {
                            if (showBottomNav) {
                                NavigationBar(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    modifier = Modifier
                                        .shadow(
                                            shape = RoundedCornerShape(0.dp),
                                            clip = true,
                                            elevation = 0.dp
                                        ),
                                ) {
                                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                                    val currentDestination = navBackStackEntry?.destination
                                    bottomNavItems.forEach { bottomNavItem ->
                                        val selected =
                                            currentDestination?.route?.contains(bottomNavItem.destination.route) == true
                                        NavigationBarItem(
                                            selected = selected,
                                            onClick = {
                                                navController.navigate(bottomNavItem.destination.route) {
                                                    navController.graph.startDestinationRoute?.let { screenRoute ->
                                                        popUpTo(screenRoute) {
                                                            saveState = true
                                                        }
                                                    }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            },
                                            icon = {
                                                Icon(
                                                    imageVector = bottomNavItem.icon,
                                                    contentDescription = bottomNavItem.label,
                                                )
                                            })
                                    }
                                }

                            }
                        }
                    ) { values ->
                        Box(modifier = Modifier.padding(values)) {
                            DestinationsNavHost(
                                navGraph = NavGraphsBuilder.root,
                                navController = navController,
                                engine = navHostEngine,
                                dependenciesContainerBuilder = {
                                    dependency(
                                        NavigationHelper(
                                            navGraph = navBackStackEntry.destination.navGraph(),
                                            navController = navController
                                        )
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}


fun NavDestination.navGraph(): NavGraphSpec {
    hierarchy.forEach { destination ->
        NavGraphs.root.nestedNavGraphs.forEach { navGraph ->
            if (destination.route == navGraph.route) {
                return navGraph
            }
        }
    }
    throw RuntimeException("Unknown nav graph for destination $route")
}

