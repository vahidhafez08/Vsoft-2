package com.vsoft.app.util

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private val safeHandler = CoroutineExceptionHandler { _, t ->
    Log.e("VSOFT", "Background task failed", t)
}

/** Like viewModelScope.launch, but an exception (e.g. a database error) is logged instead of crashing the app. */
fun ViewModel.safeLaunch(block: suspend CoroutineScope.() -> Unit): Job =
    viewModelScope.launch(safeHandler, block = block)
