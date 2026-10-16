package com.vsoft.app.util

import java.util.Calendar

/** Self-contained Jalali <-> Gregorian helpers (no external library). */
object JCal {
    val monthNames = listOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    )
    val weekdayNames = listOf("شنبه", "یکشنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه")
    val dayHeaders = listOf("ش", "ی", "د", "س", "چ", "پ", "ج")

    fun fa(n: Int): String = n.toString().map { if (it in '0'..'9') '\u06F0' + (it - '0') else it }.joinToString("")

    fun toJalali(millis: Long): IntArray {
        val c = Calendar.getInstance().apply { timeInMillis = millis }
        return g2j(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH))
    }

    fun fromJalali(jy: Int, jm: Int, jd: Int): Long {
        val month = jm.coerceIn(1, 12)
        val day = jd.coerceIn(1, monthLength(jy, month))
        val g = j2g(jy, month, day)
        val c = Calendar.getInstance()
        c.clear()
        c.set(g[0], g[1] - 1, g[2], 0, 0, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }

    fun monthLength(jy: Int, jm: Int): Int {
        val month = jm.coerceIn(1, 12)
        return when {
            month <= 6 -> 31
            month <= 11 -> 30
            else -> {
                val a = toGregorianMillis(j2g(jy, 12, 1))
                val b = toGregorianMillis(j2g(jy + 1, 1, 1))
                Math.round((b - a) / DAY_MS.toDouble()).toInt().coerceIn(29, 30)
            }
        }
    }

    private fun toGregorianMillis(g: IntArray): Long {
        val c = Calendar.getInstance()
        c.clear()
        c.set(g[0], g[1] - 1, g[2], 0, 0, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }

    fun weekdayIndex(millis: Long): Int {
        val c = Calendar.getInstance().apply { timeInMillis = millis }
        return c.get(Calendar.DAY_OF_WEEK) % 7
    }

    private fun g2j(gy: Int, gm: Int, gd: Int): IntArray {
        val gdm = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
        val gy2 = if (gm > 2) gy + 1 else gy
        var days = 355666 + 365 * gy + (gy2 + 3) / 4 - (gy2 + 99) / 100 + (gy2 + 399) / 400 + gd + gdm[gm - 1]
        var jy = -1595 + 33 * (days / 12053)
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
        return intArrayOf(jy, jm, jd)
    }

    private fun j2g(jy: Int, jm: Int, jd: Int): IntArray {
        val jy2 = jy + 1595
        var days = -355668 + 365 * jy2 + (jy2 / 33) * 8 + ((jy2 % 33) + 3) / 4 + jd +
            (if (jm < 7) (jm - 1) * 31 else (jm - 7) * 30 + 186)
        var gy = 400 * (days / 146097)
        days %= 146097
        if (days > 36524) {
            days--
            gy += 100 * (days / 36524)
            days %= 36524
            if (days >= 365) days++
        }
        gy += 4 * (days / 1461)
        days %= 1461
        if (days > 365) {
            gy += (days - 1) / 365
            days = (days - 1) % 365
        }
        var gd = days + 1
        val leap = (gy % 4 == 0 && gy % 100 != 0) || gy % 400 == 0
        val monthDays = intArrayOf(0, 31, if (leap) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        var gm = 0
        while (gm < 13 && gd > monthDays[gm]) {
            gd -= monthDays[gm]
            gm++
        }
        return intArrayOf(gy, gm, gd)
    }
}
