package com.vsoft.app.util

import java.util.Calendar

/** Helpers for Jalali-based date ranges. All values are epoch millis, local timezone. */
object JRange {

    /** Local midnight of the first day of the Jalali month containing [millis]. */
    fun monthStart(millis: Long): Long {
        val j = JCal.toJalali(millis)
        return JCal.fromJalali(j[0], j[1], 1)
    }

    /** Local midnight of the first day of the next Jalali month (exclusive end). */
    fun nextMonthStart(millis: Long): Long {
        val j = JCal.toJalali(millis)
        var y = j[0]
        var m = j[1] + 1
        if (m > 12) { m = 1; y += 1 }
        return JCal.fromJalali(y, m, 1)
    }

    /** Local midnight of the first day of the previous Jalali month. */
    fun prevMonthStart(millis: Long): Long {
        val j = JCal.toJalali(millis)
        var y = j[0]
        var m = j[1] - 1
        if (m < 1) { m = 12; y -= 1 }
        return JCal.fromJalali(y, m, 1)
    }

    /** Local midnight of the Saturday that starts the Jalali week containing [millis]. */
    fun weekStart(millis: Long): Long {
        val idx = JCal.weekdayIndex(millis) // 0 = Saturday .. 6 = Friday
        val cal = Calendar.getInstance()
        cal.timeInMillis = startOfDay(millis)
        cal.add(Calendar.DAY_OF_YEAR, -idx)
        return cal.timeInMillis
    }

    /** Start of the previous Jalali week (7 days before [weekStart]). */
    fun prevWeekStart(millis: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = weekStart(millis)
        cal.add(Calendar.DAY_OF_YEAR, -7)
        return cal.timeInMillis
    }
}
