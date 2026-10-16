package com.vsoft.app.ui.navigation
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.ui.graphics.vector.ImageVector
enum class Destination(val route: String, val label: String, val icon: ImageVector) {
    Home("home", "خانه", Icons.Filled.Home),
    Finance("finance", "مالی", Icons.Filled.AccountBalanceWallet),
    Tasks("tasks", "کارها", Icons.Filled.TaskAlt),
    Reports("reports", "گزارش‌ها", Icons.Filled.BarChart),
    More("more", "بیشتر", Icons.Filled.Apps)
}
