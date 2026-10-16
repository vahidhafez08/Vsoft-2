package com.vsoft.app.util

import java.util.Calendar
import java.util.Locale

const val DAY_MS = 86_400_000L

private val persianDigitChars = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

fun String.toPersianDigits(): String =
    map { if (it in '0'..'9') persianDigitChars[it - '0'] else it }.joinToString("")

fun String.toLatinDigits(): String =
    map {
        when (it) {
            in '۰'..'۹' -> '0' + (it - '۰')
            in '٠'..'٩' -> '0' + (it - '٠')
            else -> it
        }
    }.joinToString("")

fun Long.toMoney(): String = String.format(Locale.US, "%,d", this).toPersianDigits()

fun Int.toPersianText(): String = this.toString().toPersianDigits()

fun startOfDay(millis: Long): Long {
    val c = Calendar.getInstance()
    c.timeInMillis = millis
    c.set(Calendar.HOUR_OF_DAY, 0)
    c.set(Calendar.MINUTE, 0)
    c.set(Calendar.SECOND, 0)
    c.set(Calendar.MILLISECOND, 0)
    return c.timeInMillis
}

/** Short Persian weekday letter for a given time. */
fun weekdayShort(millis: Long): String {
    val c = Calendar.getInstance()
    c.timeInMillis = millis
    return when (c.get(Calendar.DAY_OF_WEEK)) {
        Calendar.SATURDAY -> "ش"
        Calendar.SUNDAY -> "ی"
        Calendar.MONDAY -> "د"
        Calendar.TUESDAY -> "س"
        Calendar.WEDNESDAY -> "چ"
        Calendar.THURSDAY -> "پ"
        else -> "ج"
    }
}

fun percentChange(current: Long, previous: Long): Int? =
    if (previous <= 0L) null else (((current - previous) * 100) / previous).toInt()