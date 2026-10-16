package com.vsoft.app.ui
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.vsoft.app.ui.navigation.Destination
import com.vsoft.app.ui.screens.PlaceholderScreen
import com.vsoft.app.ui.theme.BrandBlue
import com.vsoft.app.ui.theme.Vsoft
import androidx.compose.foundation.layout.padding
@Composable
fun MainScaffold() {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    Scaffold(containerColor = Vsoft.colors.bg, bottomBar = {
        NavigationBar(containerColor = Vsoft.colors.surface) {
            Destination.values().forEach { dest ->
                NavigationBarItem(selected = currentRoute == dest.route, onClick = {
                    navController.navigate(dest.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }, icon = { Icon(dest.icon, contentDescription = dest.label) }, label = { Text(dest.label) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = BrandBlue, selectedTextColor = BrandBlue, indicatorColor = Vsoft.colors.surfaceHigh, unselectedIconColor = Vsoft.colors.textSecondary, unselectedTextColor = Vsoft.colors.textSecondary))
            }
        }
    }) { padding ->
        NavHost(navController, startDestination = Destination.Home.route, modifier = Modifier.padding(padding)) {
            Destination.values().forEach { dest -> composable(dest.route) { PlaceholderScreen(dest.label) } }
        }
    }
}
