package com.vsoft.app.util

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/** One-shot messages displayed by the app-level snackbar host. */
data class UiMessage(
    val text: String,
    val actionLabel: String? = null,
    val onAction: (() -> Unit)? = null
)

object UiEvents {
    private val _messages = MutableSharedFlow<UiMessage>(
        replay = 0,
        extraBufferCapacity = 8,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val messages: SharedFlow<UiMessage> = _messages.asSharedFlow()

    fun show(message: String) {
        _messages.tryEmit(UiMessage(text = message))
    }

    fun showWithAction(text: String, actionLabel: String, onAction: () -> Unit) {
        _messages.tryEmit(UiMessage(text = text, actionLabel = actionLabel, onAction = onAction))
    }
}