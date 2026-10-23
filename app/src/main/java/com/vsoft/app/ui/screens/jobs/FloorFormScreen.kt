package com.vsoft.app.ui.screens.jobs

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun FloorFormScreen(onBack: () -> Unit, viewModel: FloorFormViewModel = hiltViewModel()) {
    JobEntityFormScreen(kind = "floor", onBack = onBack, vm = viewModel)
}
