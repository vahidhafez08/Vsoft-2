package com.vsoft.app.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.vsoft.app.ui.navigation.Destination
import com.vsoft.app.ui.navigation.Routes
import com.vsoft.app.ui.screens.PlaceholderScreen
import com.vsoft.app.ui.screens.accounts.AccountsScreen
import com.vsoft.app.ui.screens.finance.FinanceScreen
import com.vsoft.app.ui.screens.home.HomeScreen
import com.vsoft.app.ui.screens.transactions.TransactionsScreen
import com.vsoft.app.ui.theme.BrandBlue
import com.vsoft.app.ui.theme.Vsoft

@Composable
fun MainScaffold() {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val financeGroup = listOf(Destination.Finance.route, Routes.TRANSACTIONS, Routes.ACCOUNTS)

    fun openRoute(route: String) {
        navController.navigate(route) { launchSingleTop = true }
    }

    Scaffold(
        containerColor = Vsoft.colors.bg,
        bottomBar = {
            NavigationBar(containerColor = Vsoft.colors.surface) {
                Destination.values().forEach { dest ->
                    val selected = if (dest == Destination.Finance) {
                        currentRoute in financeGroup
                    } else {
                        currentRoute == dest.route
                    }
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (selected) {
                                // Already inside this tab: go back to its root screen if we are deeper.
                                if (currentRoute != dest.route) {
                                    val popped = navController.popBackStack(dest.route, false)
                                    if (!popped) {
                                        navController.navigate(dest.route) {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                        }
                                    }
                                }
                            } else {
                                navController.navigate(dest.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(dest.icon, contentDescription = dest.label) },
                        label = { Text(dest.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BrandBlue,
                            selectedTextColor = BrandBlue,
                            indicatorColor = Vsoft.colors.surfaceHigh,
                            unselectedIconColor = Vsoft.colors.textSecondary,
                            unselectedTextColor = Vsoft.colors.textSecondary
                        )
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Destination.Home.route,
            modifier = Modifier.padding(padding),
            enterTransition = {
                fadeIn(tween(260)) + slideInHorizontally(tween(260), initialOffsetX = { -it / 14 })
            },
            exitTransition = { fadeOut(tween(160)) },
            popEnterTransition = { fadeIn(tween(260)) },
            popExitTransition = {
                fadeOut(tween(160)) + slideOutHorizontally(tween(260), targetOffsetX = { -it / 14 })
            }
        ) {
            composable(Destination.Home.route) {
                HomeScreen(
                    onOpenTransactions = { openRoute(Routes.TRANSACTIONS) },
                    onOpenAccounts = { openRoute(Routes.ACCOUNTS) }
                )
            }
            composable(Destination.Finance.route) {
                FinanceScreen(
                    onOpenTransactions = { openRoute(Routes.TRANSACTIONS) },
                    onOpenAccounts = { openRoute(Routes.ACCOUNTS) }
                )
            }
            composable(Routes.TRANSACTIONS) {
                TransactionsScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.ACCOUNTS) {
                AccountsScreen(onBack = { navController.popBackStack() })
            }
            composable(Destination.Tasks.route) { PlaceholderScreen(title = Destination.Tasks.label) }
            composable(Destination.Reports.route) { PlaceholderScreen(title = Destination.Reports.label) }
            composable(Destination.More.route) { PlaceholderScreen(title = Destination.More.label) }
        }
    }
}
