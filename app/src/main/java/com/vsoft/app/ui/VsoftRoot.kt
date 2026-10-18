package com.vsoft.app.ui

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.vsoft.app.security.AppLock
import com.vsoft.app.ui.screens.lock.LockScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vsoft.app.ui.screens.SplashScreen
import com.vsoft.app.ui.screens.more.SettingsViewModel
import com.vsoft.app.ui.theme.Vsoft
import com.vsoft.app.ui.theme.VsoftTheme

@Composable
fun VsoftRoot(settingsViewModel: SettingsViewModel = hiltViewModel()) {
    val themeMode by settingsViewModel.themeMode.collectAsStateWithLifecycle()
    val systemDark = isSystemInDarkTheme()
    val dark = when (themeMode) {
        1 -> false
        2 -> true
        else -> systemDark
    }
    VsoftTheme(darkTheme = dark) {
        val navController = rememberNavController()
        val locked by AppLock.locked.collectAsStateWithLifecycle()
        val view = LocalView.current
        val barBg = Vsoft.colors.bg.toArgb()
        val barSurface = Vsoft.colors.surface.toArgb()
        if (!view.isInEditMode) {
            SideEffect {
                val window = (view.context as? Activity)?.window
                if (window != null) {
                    window.statusBarColor = barBg
                    window.navigationBarColor = barSurface
                    val controller = WindowCompat.getInsetsController(window, view)
                    controller.isAppearanceLightStatusBars = !dark
                    controller.isAppearanceLightNavigationBars = !dark
                }
            }
        }
        var showSplash by rememberSaveable { mutableStateOf(true) }
        Box(Modifier.fillMaxSize()) {
            AnimatedVisibility(visible = !showSplash, enter = fadeIn(), exit = fadeOut()) {
                if (locked) LockScreen(onUnlocked = { AppLock.unlock() }) else MainScaffold(navController = navController)
            }
            AnimatedVisibility(visible = showSplash, exit = fadeOut()) { SplashScreen(onFinished = { showSplash = false }) }
        }
    }
}
