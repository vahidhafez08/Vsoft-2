package com.vsoft.app.ui.screens.jobs

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SiteFormScreen(onBack: () -> Unit, viewModel: SiteFormViewModel = hiltViewModel()) {
    JobEntityFormScreen(kind = "site", onBack = onBack, vm = viewModel)
}
