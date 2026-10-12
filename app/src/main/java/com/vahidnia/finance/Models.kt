package com.vahidnia.finance

data class BankCard(val id: String, val bank: String, val last4: String, val initial: Long)

data class Tx(
    val id: String,
    val cardId: String,
    val income: Boolean,
    val amount: Long,
    val category: String,
    val note: String,
    val time: Long
)

/** worker: 0 = خودم به‌تنهایی، 1 = با همکاران، 2 = شخص دیگر */
data class WorkItem(
    val id: String,
    val title: String,
    val progress: Int,
    val floor: Int,
    val days: Int,
    val hours: Int,
    val worker: Int,
    val workerName: String
)

data class Place(val id: String, val name: String, val items: List<WorkItem>)

val bankNames = listOf("بانک ملی", "بلو بانک", "بانک مسکن", "سایر")
val expenseCats = listOf("خرید", "غذا", "قبوض", "حمل‌ونقل", "اینترنت", "مصالح", "دستمزد", "سایر")
val incomeCats = listOf("حقوق", "پروژه", "واریز", "هدیه", "سایر")
