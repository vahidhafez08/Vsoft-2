package com.vsoft.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vsoft.app.ui.screens.SplashScreen
import com.vsoft.app.ui.screens.more.SettingsViewModel
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
        var showSplash by rememberSaveable { mutableStateOf(true) }
        Box(Modifier.fillMaxSize()) {
            AnimatedVisibility(visible = !showSplash, enter = fadeIn(), exit = fadeOut()) { MainScaffold() }
            AnimatedVisibility(visible = showSplash, exit = fadeOut()) { SplashScreen(onFinished = { showSplash = false }) }
        }
    }
}
