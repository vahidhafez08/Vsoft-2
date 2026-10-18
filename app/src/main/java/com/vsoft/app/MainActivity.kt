package com.vsoft.app

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.vsoft.app.security.AppLock
import com.vsoft.app.security.PinStore
import com.vsoft.app.ui.VsoftRoot
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            AppLock.initOnColdStart(this)
            applySecureScreenFlag()
        } catch (_: Exception) {
            // Do not prevent the app from starting if window security setup fails.
        }
        enableEdgeToEdge()
        setContent { VsoftRoot() }
    }

    override fun onStart() {
        super.onStart()
        try {
            AppLock.onAppStarted(this)
            applySecureScreenFlag()
        } catch (_: Exception) {
            // Keep lifecycle handling resilient.
        }
    }

    override fun onStop() {
        try {
            AppLock.onAppStopped()
        } catch (_: Exception) {
            // Keep lifecycle handling resilient.
        }
        super.onStop()
    }

    private fun applySecureScreenFlag() {
        try {
            if (PinStore.isSecureScreen(this)) {
                window.setFlags(
                    WindowManager.LayoutParams.FLAG_SECURE,
                    WindowManager.LayoutParams.FLAG_SECURE
                )
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            }
        } catch (_: Exception) {
            // FLAG_SECURE is best-effort on unsupported window states.
        }
    }
}
