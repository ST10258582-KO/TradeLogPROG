package com.tradelog.app.ui.navigation

import android.app.Application
import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tradelog.app.ui.screens.analytics.AnalyticsScreen
import com.tradelog.app.ui.screens.auth.LoginScreen
import com.tradelog.app.ui.screens.auth.RegisterScreen
import com.tradelog.app.ui.screens.dashboard.DashboardScreen
import com.tradelog.app.ui.screens.journal.JournalScreen
import com.tradelog.app.ui.screens.riskcalc.RiskCalculatorScreen
import com.tradelog.app.ui.screens.settings.SettingsScreen
import com.tradelog.app.ui.screens.splash.SplashScreen
import com.tradelog.app.ui.screens.trades.TradeDetailScreen
import com.tradelog.app.ui.screens.trades.TradeEntryScreen
import com.tradelog.app.ui.screens.trades.TradesListScreen
import com.tradelog.app.ui.theme.Purple
import com.tradelog.app.viewmodel.AnalyticsViewModel
import com.tradelog.app.viewmodel.AuthViewModel
import com.tradelog.app.viewmodel.SettingsViewModel
import com.tradelog.app.viewmodel.TradeViewModel

private const val TAG = "AppNavigation"

// Bottom navigation items — matches the 5-tab layout from Part 1 Section 4.2
private data class BottomNavItem(val screen: Screen, val icon: ImageVector, val label: String)

private val bottomNavItems = listOf(
    BottomNavItem(Screen.Dashboard, Icons.Default.Home, "Home"),
    BottomNavItem(Screen.Trades, Icons.Default.List, "Trades"),
    BottomNavItem(Screen.Analytics, Icons.Default.BarChart, "Analytics"),
    BottomNavItem(Screen.Journal, Icons.Default.Book, "Journal"),
    BottomNavItem(Screen.Settings, Icons.Default.Settings, "Settings")
)

// Bottom nav routes — used to decide when to show the nav bar
private val mainRoutes = bottomNavItems.map { it.screen.route }

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current

    // Shared ViewModels — scoped to NavHost lifecycle
    val authViewModel: AuthViewModel = viewModel()
    val tradeViewModel: TradeViewModel = viewModel()
    val analyticsViewModel: AnalyticsViewModel = viewModel()
    val settingsViewModel: SettingsViewModel = viewModel(
        factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
            .getInstance(context.applicationContext as Application)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in mainRoutes

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp) {
                    val currentDestination = navBackStackEntry?.destination
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = currentDestination?.hierarchy?.any { it.route == item.screen.route } == true,
                            onClick = {
                                Log.d(TAG, "Bottom nav: ${item.label}")
                                navController.navigate(item.screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Purple,
                                selectedTextColor = Purple,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray,
                                indicatorColor = Purple.copy(alpha = 0.15f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    isLoggedIn = authViewModel.currentUser.collectAsState().value != null,
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route) { popUpTo(Screen.Splash.route) { inclusive = true } }
                    },
                    onNavigateToDashboard = {
                        navController.navigate(Screen.Dashboard.route) { popUpTo(Screen.Splash.route) { inclusive = true } }
                    }
                )
            }
            composable(Screen.Login.route) {
                LoginScreen(
                    authViewModel = authViewModel,
                    onNavigateToDashboard = {
                        navController.navigate(Screen.Dashboard.route) { popUpTo(Screen.Login.route) { inclusive = true } }
                    },
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) }
                )
            }
            composable(Screen.Register.route) {
                RegisterScreen(
                    authViewModel = authViewModel,
                    onNavigateToDashboard = {
                        navController.navigate(Screen.Dashboard.route) { popUpTo(Screen.Login.route) { inclusive = true } }
                    }
                )
            }
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    tradeViewModel = tradeViewModel,
                    onTradeClick = { id -> navController.navigate(Screen.TradeDetail.route(id)) }
                )
            }
            composable(Screen.Trades.route) {
                TradesListScreen(
                    tradeViewModel = tradeViewModel,
                    onTradeClick = { id -> navController.navigate(Screen.TradeDetail.route(id)) },
                    onAddTrade = { navController.navigate(Screen.TradeEntry.route()) }
                )
            }
            composable(
                route = Screen.TradeEntry.route,
                arguments = listOf(navArgument("tradeId") { type = NavType.StringType; nullable = true; defaultValue = null })
            ) { backStackEntry ->
                val tradeId = backStackEntry.arguments?.getString("tradeId")
                TradeEntryScreen(
                    tradeViewModel = tradeViewModel,
                    onDismiss = { navController.popBackStack() },
                    editTradeId = tradeId
                )
            }
            composable(
                route = Screen.TradeDetail.route,
                arguments = listOf(navArgument("tradeId") { type = NavType.StringType })
            ) { backStackEntry ->
                val tradeId = backStackEntry.arguments?.getString("tradeId") ?: return@composable
                TradeDetailScreen(
                    tradeId = tradeId,
                    tradeViewModel = tradeViewModel,
                    onBack = { navController.popBackStack() },
                    onEdit = { id -> navController.navigate(Screen.TradeEntry.route(id)) }
                )
            }
            composable(Screen.Analytics.route) {
                AnalyticsScreen(tradeViewModel = tradeViewModel, analyticsViewModel = analyticsViewModel)
            }
            composable(Screen.Journal.route) { JournalScreen() }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    authViewModel = authViewModel,
                    settingsViewModel = settingsViewModel,
                    onSignOut = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                        }
                    },
                    onNavigateToRiskCalc = { navController.navigate(Screen.RiskCalculator.route) }
                )
            }
            composable(Screen.RiskCalculator.route) {
                RiskCalculatorScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
