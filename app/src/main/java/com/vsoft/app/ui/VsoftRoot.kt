package com.vsoft.app.ui

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.vsoft.app.ui.screens.rememberReduceMotion
import com.vsoft.app.ui.screens.more.SettingsViewModel
import com.vsoft.app.ui.onboarding.OnboardingScreen
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
        val onboardingPrefs = remember(view) { view.context.getSharedPreferences("vsoft_prefs", 0) }
        var showOnboarding by rememberSaveable { mutableStateOf(!onboardingPrefs.getBoolean("onboarding_completed", false)) }
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
        // rememberSaveable, above the NavHost: the splash runs once per app launch
        // and does not repeat on navigation, rotation or theme change.
        var showSplash by rememberSaveable { mutableStateOf(true) }
        val reduceMotion = rememberReduceMotion()
        Box(Modifier.fillMaxSize().background(Vsoft.colors.bg)) {
            AnimatedVisibility(
                visible = !showSplash,
                enter = EnterTransition.None,
                exit = ExitTransition.None
            ) {
                if (locked) LockScreen(onUnlocked = { AppLock.unlock() }) else if (showOnboarding) OnboardingScreen { name ->
                    if (name != null) settingsViewModel.setUserName(name)
                    onboardingPrefs.edit().putBoolean("onboarding_completed", true).apply()
                    showOnboarding = false
                } else MainScaffold(navController = navController, settingsViewModel = settingsViewModel)
            }
            AnimatedVisibility(
                visible = showSplash,
                enter = EnterTransition.None,
                exit = if (reduceMotion) ExitTransition.None else fadeOut(tween(300))
            ) { SplashScreen(onFinished = { showSplash = false }) }
        }
    }
}
