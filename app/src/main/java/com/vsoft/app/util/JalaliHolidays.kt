package com.vsoft.app.util

/** Fixed solar-calendar official Iranian holidays; lunar holidays need a Hijri converter. */
object JHolidays {
    private val fixed: Map<Pair<Int, Int>, String> = mapOf(
        (1 to 1) to "جشن نوروز",
        (1 to 2) to "عید نوروز",
        (1 to 3) to "عید نوروز",
        (1 to 4) to "عید نوروز",
        (1 to 12) to "روز جمهوری اسلامی",
        (1 to 13) to "روز طبیعت",
        (3 to 14) to "رحلت امام خمینی",
        (3 to 15) to "قیام ۱۵ خرداد",
        (11 to 22) to "پیروزی انقلاب اسلامی",
        (12 to 29) to "ملی شدن صنعت نفت"
    )

    fun nameOf(month: Int, day: Int): String? = fixed[month to day]

    fun ofMonth(month: Int): List<Pair<Int, String>> =
        fixed.entries
            .filter { it.key.first == month }
            .map { it.key.second to it.value }
            .sortedBy { it.first }
}
