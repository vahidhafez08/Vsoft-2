package com.vsoft.app.ui.navigation

object Routes {
    const val TRANSACTIONS = "transactions"
    const val ACCOUNTS = "accounts"
    const val TASK_DETAIL = "task/{id}"

    fun taskDetail(id: Long): String = "task/$id"
}