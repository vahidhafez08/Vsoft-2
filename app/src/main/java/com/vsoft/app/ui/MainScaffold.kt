package com.vsoft.app.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.vsoft.app.ui.navigation.Destination
import com.vsoft.app.ui.navigation.Routes
import com.vsoft.app.ui.screens.accounts.AccountsScreen
import com.vsoft.app.ui.screens.calendar.CalendarScreen
import com.vsoft.app.ui.screens.documents.DocumentsScreen
import com.vsoft.app.ui.screens.finance.FinanceScreen
import com.vsoft.app.ui.screens.home.HomeScreen
import com.vsoft.app.ui.screens.jobs.JobsScreen
import com.vsoft.app.ui.screens.jobs.SiteDetailScreen
import com.vsoft.app.ui.screens.more.MoreScreen
import com.vsoft.app.ui.screens.reports.ReportsScreen
import com.vsoft.app.ui.screens.tasks.TaskDetailScreen
import com.vsoft.app.ui.screens.tasks.TasksScreen
import com.vsoft.app.ui.screens.transactions.TransactionsScreen
import com.vsoft.app.ui.theme.BrandBlue
import com.vsoft.app.ui.theme.Vsoft
import com.vsoft.app.util.UiEvents
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sign

private val tabDestinations = Destination.values().toList()

@Composable
fun MainScaffold() {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val snackbarHostState = remember { SnackbarHostState() }
    val financeGroup = listOf(Destination.Finance.route, Routes.TRANSACTIONS, Routes.ACCOUNTS)
    val tasksGroup = listOf(Destination.Tasks.route, Routes.TASK_DETAIL, Routes.JOBS, Routes.SITE_DETAIL, Routes.CALENDAR)
    val currentTabIndex = tabDestinations.indexOfFirst { it.route == currentRoute }
    val isTabRoute = currentTabIndex >= 0
    val pagerState = rememberPagerState(initialPage = currentTabIndex.coerceAtLeast(0)) { tabDestinations.size }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        UiEvents.messages.collect { msg ->
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(msg, duration = SnackbarDuration.Short)
        }
    }

    fun openRoute(route: String) {
        navController.navigate(route) { launchSingleTop = true }
    }

    fun navigateToTab(dest: Destination) {
        if (navController.currentBackStackEntry?.destination?.route == dest.route) return
        navController.navigate(dest.route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    // A route change from a deep screen (including system Back/deep links) restores the
    // pager to that tab without adding a second animated transition.
    LaunchedEffect(currentRoute) {
        val index = tabDestinations.indexOfFirst { it.route == currentRoute }
        if (index >= 0 && pagerState.currentPage != index) {
            pagerState.scrollToPage(index)
        }
    }

    // A completed swipe updates the Navigation back stack, which remains the source of truth.
    LaunchedEffect(pagerState, navController) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .collect { page ->
                val destination = tabDestinations.getOrNull(page) ?: return@collect
                if (navController.currentBackStackEntry?.destination?.route != destination.route) {
                    navigateToTab(destination)
                }
            }
    }

    val highlightedPage by remember {
        derivedStateOf {
            if (pagerState.isScrollInProgress) pagerState.targetPage else pagerState.currentPage
        }
    }

    Scaffold(
        containerColor = Vsoft.colors.bg,
        snackbarHost = {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                SnackbarHost(snackbarHostState)
            }
        },
        bottomBar = {
            NavigationBar(containerColor = Vsoft.colors.surface) {
                tabDestinations.forEachIndexed { index, dest ->
                    val selected = if (isTabRoute) {
                        highlightedPage == index
                    } else {
                        when (dest) {
                            Destination.Finance -> currentRoute in financeGroup
                            Destination.Tasks -> currentRoute in tasksGroup
                            else -> currentRoute == dest.route
                        }
                    }
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (isTabRoute) {
                                if (index != pagerState.currentPage && !pagerState.isScrollInProgress) {
                                    scope.launch {
                                        try {
                                            val distance = index - pagerState.currentPage
                                            if (abs(distance) > 1) {
                                                pagerState.scrollToPage(index - sign(distance).toInt())
                                            }
                                            pagerState.animateScrollToPage(
                                                index,
                                                animationSpec = tween(280, easing = FastOutSlowInEasing)
                                            )
                                        } catch (cancelled: CancellationException) {
                                            throw cancelled
                                        }
                                    }
                                }
                            } else if (selected) {
                                // Preserve existing behavior: tapping the active group returns to its root.
                                if (currentRoute != dest.route && !navController.popBackStack(dest.route, false)) {
                                    navigateToTab(dest)
                                }
                            } else {
                                navigateToTab(dest)
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
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Vsoft.colors.bg)
        ) {
            NavHost(
                navController = navController,
                startDestination = Destination.Home.route,
                modifier = Modifier.fillMaxSize(),
                enterTransition = { fadeIn(tween(260)) + slideInHorizontally(tween(260), initialOffsetX = { -it / 14 }) },
                exitTransition = { fadeOut(tween(160)) },
                popEnterTransition = { fadeIn(tween(260)) },
                popExitTransition = { fadeOut(tween(160)) + slideOutHorizontally(tween(260), targetOffsetX = { -it / 14 }) }
            ) {
                tabDestinations.forEach { dest ->
                    composable(dest.route) {
                        // The pager is hosted outside the NavHost so its state/content survives tab route changes.
                        Box(Modifier.fillMaxSize().background(Vsoft.colors.bg))
                    }
                }
                composable(Routes.TRANSACTIONS) { TransactionsScreen(onBack = { navController.popBackStack() }) }
                composable(Routes.ACCOUNTS) { AccountsScreen(onBack = { navController.popBackStack() }) }
                composable(Routes.DOCUMENTS) { DocumentsScreen(onBack = { navController.popBackStack() }) }
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
            }

            if (isTabRoute) {
                TabsHost(
                    navController = navController,
                    pagerState = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    onOpenRoute = ::openRoute
                )
            }
        }
    }
}

@Composable
private fun TabsHost(
    navController: NavController,
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    onOpenRoute: (String) -> Unit
) {
    HorizontalPager(
        state = pagerState,
        modifier = modifier.background(Vsoft.colors.bg),
        beyondBoundsPageCount = 1,
        key = { index -> tabDestinations.getOrNull(index)?.route ?: index },
        userScrollEnabled = true,
        pageSpacing = 0.dp,
        flingBehavior = PagerDefaults.flingBehavior(
            state = pagerState,
            snapAnimationSpec = spring(stiffness = Spring.StiffnessMediumLow)
        )
    ) { page ->
        Box(
            Modifier
                .fillMaxSize()
                .background(Vsoft.colors.bg)
        ) {
            when (tabDestinations.getOrNull(page)) {
                Destination.Home -> HomeScreen(
                    onOpenTransactions = { onOpenRoute(Routes.TRANSACTIONS) },
                    onOpenAccounts = { onOpenRoute(Routes.ACCOUNTS) },
                    onOpenDocuments = { onOpenRoute(Routes.DOCUMENTS) }
                )
                Destination.Finance -> FinanceScreen(
                    onOpenTransactions = { onOpenRoute(Routes.TRANSACTIONS) },
                    onOpenAccounts = { onOpenRoute(Routes.ACCOUNTS) }
                )
                Destination.Tasks -> TasksScreen(
                    onOpenTask = { id -> onOpenRoute(Routes.taskDetail(id)) },
                    onOpenJobs = { onOpenRoute(Routes.JOBS) },
                    onOpenCalendar = { onOpenRoute(Routes.CALENDAR) }
                )
                Destination.Reports -> ReportsScreen()
                Destination.More -> MoreScreen()
                null -> Unit
            }
        }
    }
}
