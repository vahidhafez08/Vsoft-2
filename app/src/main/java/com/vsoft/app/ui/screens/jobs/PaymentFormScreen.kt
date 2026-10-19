package com.vsoft.app.ui.screens.jobs

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun PaymentFormScreen(onBack: () -> Unit, viewModel: PaymentFormViewModel = hiltViewModel()) {
    JobEntityFormScreen(kind = "payment", onBack = onBack, vm = viewModel)
}
