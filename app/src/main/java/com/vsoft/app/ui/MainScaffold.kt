package com.vsoft.app.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.vsoft.app.ui.navigation.Destination
import com.vsoft.app.ui.navigation.Routes
import com.vsoft.app.ui.screens.PlaceholderScreen
import com.vsoft.app.ui.screens.accounts.AccountsScreen
import com.vsoft.app.ui.screens.calendar.CalendarScreen
import com.vsoft.app.ui.screens.reports.ReportsScreen
import com.vsoft.app.ui.screens.finance.FinanceScreen
import com.vsoft.app.ui.screens.home.HomeScreen
import com.vsoft.app.ui.screens.more.MoreScreen
import com.vsoft.app.ui.screens.jobs.JobsScreen
import com.vsoft.app.ui.screens.jobs.SiteDetailScreen
import com.vsoft.app.ui.screens.tasks.TaskDetailScreen
import com.vsoft.app.ui.screens.tasks.TasksScreen
import com.vsoft.app.ui.screens.transactions.TransactionsScreen
import com.vsoft.app.ui.theme.BrandBlue
import com.vsoft.app.ui.theme.Vsoft

@Composable
fun MainScaffold() {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val financeGroup = listOf(Destination.Finance.route, Routes.TRANSACTIONS, Routes.ACCOUNTS)
    val tasksGroup = listOf(Destination.Tasks.route, Routes.TASK_DETAIL, Routes.JOBS, Routes.SITE_DETAIL, Routes.CALENDAR)

    fun openRoute(route: String) {
        navController.navigate(route) { launchSingleTop = true }
    }

    Scaffold(
        containerColor = Vsoft.colors.bg,
        bottomBar = {
            NavigationBar(containerColor = Vsoft.colors.surface) {
                Destination.values().forEach { dest ->
                    val selected = when (dest) {
                        Destination.Finance -> currentRoute in financeGroup
                        Destination.Tasks -> currentRoute in tasksGroup
                        else -> currentRoute == dest.route
                    }
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (selected) {
                                // Already inside this tab: go back to its root screen if we are deeper.
                                if (currentRoute != dest.route && !navController.popBackStack(dest.route, false)) {
                                    navController.navigate(dest.route) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
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
            enterTransition = { fadeIn(tween(260)) + slideInHorizontally(tween(260), initialOffsetX = { -it / 14 }) },
            exitTransition = { fadeOut(tween(160)) },
            popEnterTransition = { fadeIn(tween(260)) },
            popExitTransition = { fadeOut(tween(160)) + slideOutHorizontally(tween(260), targetOffsetX = { -it / 14 }) }
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
            composable(Routes.TRANSACTIONS) { TransactionsScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.ACCOUNTS) { AccountsScreen(onBack = { navController.popBackStack() }) }
            composable(Destination.Tasks.route) {
                TasksScreen(
                    onOpenTask = { id -> openRoute(Routes.taskDetail(id)) },
                    onOpenJobs = { openRoute(Routes.JOBS) },
                    onOpenCalendar = { openRoute(Routes.CALENDAR) }
                )
            }
            composable(
                route = Routes.TASK_DETAIL,
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { TaskDetailScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.JOBS) {
                JobsScreen(
                    onBack = { navController.popBackStack() },
                    onOpenSite = { id -> openRoute(Routes.siteDetail(id)) }
                )
            }
            composable(
                route = Routes.SITE_DETAIL,
                arguments = listOf(navArgument("id") { type = NavType.LongType })
            ) { SiteDetailScreen(onBack = { navController.popBackStack() }) }
            composable(Routes.CALENDAR) {
                CalendarScreen(
                    onBack = { navController.popBackStack() },
                    onOpenTask = { id -> openRoute(Routes.taskDetail(id)) }
                )
            }
            composable(Destination.Reports.route) { ReportsScreen() }
            composable(Destination.More.route) { MoreScreen() }
        }
    }
}
