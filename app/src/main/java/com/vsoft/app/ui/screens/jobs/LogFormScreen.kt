package com.vsoft.app.ui.screens.jobs

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun LogFormScreen(onBack: () -> Unit, viewModel: LogFormViewModel = hiltViewModel()) {
    JobEntityFormScreen(kind = "log", onBack = onBack, vm = viewModel)
}
