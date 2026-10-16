package com.vsoft.app.util

import java.util.Calendar
import java.util.Locale

private val gDaysBefore = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)

val JalaliMonths = listOf(
    "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
    "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
)

data class JalaliDate(val year: Int, val month: Int, val day: Int)

fun gregorianToJalali(gy0: Int, gm: Int, gd: Int): JalaliDate {
    var jy: Int
    val gy: Int
    if (gy0 > 1600) {
        jy = 979
        gy = gy0 - 1600
    } else {
        jy = 0
        gy = gy0 - 621
    }
    val gy2 = if (gm > 2) gy + 1 else gy
    var days = 365 * gy + (gy2 + 3) / 4 - (gy2 + 99) / 100 + (gy2 + 399) / 400 - 80 + gd + gDaysBefore[gm - 1]
    jy += 33 * (days / 12053)
    days %= 12053
    jy += 4 * (days / 1461)
    days %= 1461
    if (days > 365) {
        jy += (days - 1) / 365
        days = (days - 1) % 365
    }
    val jm: Int
    val jd: Int
    if (days < 186) {
        jm = 1 + days / 31
        jd = 1 + days % 31
    } else {
        jm = 7 + (days - 186) / 30
        jd = 1 + (days - 186) % 30
    }
    return JalaliDate(jy, jm, jd)
}

fun jalaliOf(millis: Long): JalaliDate {
    val c = Calendar.getInstance()
    c.timeInMillis = millis
    return gregorianToJalali(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH))
}

fun formatJalali(millis: Long): String {
    val j = jalaliOf(millis)
    return String.format(Locale.US, "%04d/%02d/%02d", j.year, j.month, j.day).toPersianDigits()
}

fun formatTime(millis: Long): String {
    val c = Calendar.getInstance()
    c.timeInMillis = millis
    return String.format(Locale.US, "%02d:%02d", c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE)).toPersianDigits()
}

fun dayLabel(millis: Long): String {
    val diff = ((startOfDay(System.currentTimeMillis()) - startOfDay(millis) + DAY_MS / 2) / DAY_MS).toInt()
    return when (diff) {
        0 -> "امروز"
        1 -> "دیروز"
        else -> {
            val j = jalaliOf(millis)
            "${j.day.toPersianText()} ${JalaliMonths[j.month - 1]} ${j.year.toPersianText()}"
        }
    }
}
