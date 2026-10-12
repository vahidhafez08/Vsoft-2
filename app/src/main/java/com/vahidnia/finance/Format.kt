package com.vahidnia.finance

import java.util.Calendar
import java.util.Locale

private val calTl = object : ThreadLocal<Calendar>() {
    override fun initialValue(): Calendar = Calendar.getInstance()
}

private fun calAt(t: Long): Calendar {
    val c = calTl.get()!!
    c.timeInMillis = t
    return c
}

private val faDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

fun String.fa(): String {
    val sb = StringBuilder()
    for (ch in this) sb.append(if (ch in '0'..'9') faDigits[ch - '0'] else ch)
    return sb.toString()
}

fun Int.fa(): String = toString().fa()

/** تبدیل متن ورودی (فارسی/عربی/انگلیسی) به عدد */
fun parseAmount(s: String): Long {
    val sb = StringBuilder()
    for (ch in s) {
        if (ch in '0'..'9') sb.append(ch)
        else if (ch in '۰'..'۹') sb.append(ch - '۰')
        else if (ch in '٠'..'٩') sb.append(ch - '٠')
    }
    return sb.toString().toLongOrNull() ?: 0L
}

fun Long.money(): String {
    val s = Math.abs(this).toString()
    val sb = StringBuilder()
    for (i in s.indices) {
        if (i > 0 && (s.length - i) % 3 == 0) sb.append(',')
        sb.append(s[i])
    }
    return ((if (this < 0) "-" else "") + sb.toString()).fa()
}

fun norm(s: String): String {
    val t = s.replace('ي', 'ی').replace('ك', 'ک')
    val sb = StringBuilder()
    for (ch in t) sb.append(if (ch in '۰'..'۹') '0' + (ch - '۰') else ch)
    return sb.toString().lowercase()
}

val jMonths = listOf(
    "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
    "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
)

data class JDate(val y: Int, val m: Int, val d: Int) {
    val idx: Int get() = y * 12 + (m - 1)
    fun text(): String = "${d.fa()} ${jMonths[m - 1]} ${y.fa()}"
}

fun g2j(gy: Int, gm: Int, gd: Int): JDate {
    val gdm = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
    val gy2 = if (gm > 2) gy + 1 else gy
    var days = 355666 + (365 * gy) + ((gy2 + 3) / 4) - ((gy2 + 99) / 100) + ((gy2 + 399) / 400) + gd + gdm[gm - 1]
    var jy = -1595 + (33 * (days / 12053))
    days %= 12053
    jy += 4 * (days / 1461)
    days %= 1461
    if (days > 365) {
        jy += (days - 1) / 365
        days = (days - 1) % 365
    }
    val jm = if (days < 186) 1 + days / 31 else 7 + (days - 186) / 30
    val jd = 1 + (if (days < 186) days % 31 else (days - 186) % 30)
    return JDate(jy, jm, jd)
}

fun jalali(time: Long): JDate {
    val c = calAt(time)
    return g2j(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH))
}

fun timeText(time: Long): String {
    val c = calAt(time)
    return String.format(Locale.US, "%02d:%02d", c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE)).fa()
}

private fun startOfDay(t: Long): Long {
    val c = calAt(t)
    c.set(Calendar.HOUR_OF_DAY, 0)
    c.set(Calendar.MINUTE, 0)
    c.set(Calendar.SECOND, 0)
    c.set(Calendar.MILLISECOND, 0)
    return c.timeInMillis
}

fun dayLabel(time: Long): String {
    val diff = Math.round((startOfDay(System.currentTimeMillis()) - startOfDay(time)) / 86400000.0).toInt()
    return when (diff) {
        0 -> "امروز"
        1 -> "دیروز"
        else -> jalali(time).text()
    }
}
