package com.vsoft.app.ui.navigation

object Routes {
    const val TRANSACTIONS = "transactions"
    const val ACCOUNTS = "accounts"
    const val TASK_DETAIL = "task/{id}"
    const val JOBS = "jobs"
    const val SITE_DETAIL = "site/{id}"
    const val CALENDAR = "calendar"
    const val DOCUMENTS = "documents"

    fun taskDetail(id: Long): String = "task/$id"
    fun siteDetail(id: Long): String = "site/$id"
}
