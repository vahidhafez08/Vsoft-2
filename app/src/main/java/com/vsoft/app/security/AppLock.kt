package com.vsoft.app.security

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AppLock {
    private val _locked = MutableStateFlow(false)
    val locked: StateFlow<Boolean> = _locked.asStateFlow()

    @Volatile
    private var lastStopAt: Long = 0L

    fun initOnColdStart(context: Context) {
        try {
            _locked.value = PinStore.isEnabled(context.applicationContext)
            lastStopAt = 0L
        } catch (_: Exception) {
            _locked.value = true
        }
    }

    fun onAppStopped() {
        try {
            lastStopAt = System.currentTimeMillis()
        } catch (_: Exception) {
            // Keep lifecycle callbacks safe.
        }
    }

    fun onAppStarted(context: Context) {
        try {
            if (PinStore.isEnabled(context.applicationContext) &&
                lastStopAt > 0L &&
                System.currentTimeMillis() - lastStopAt > 30_000L
            ) {
                _locked.value = true
            }
        } catch (_: Exception) {
            if (PinStore.isEnabled(context.applicationContext)) _locked.value = true
        }
    }

    fun unlock() {
        _locked.value = false
        lastStopAt = 0L
    }

    fun lockNow() {
        _locked.value = true
    }
}
