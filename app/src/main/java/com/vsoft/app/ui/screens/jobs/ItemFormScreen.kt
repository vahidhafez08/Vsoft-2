package com.vsoft.app.ui.screens.jobs

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ItemFormScreen(onBack: () -> Unit, viewModel: ItemFormViewModel = hiltViewModel()) {
    JobEntityFormScreen(kind = "item", onBack = onBack, vm = viewModel)
}
