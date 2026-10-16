package com.vsoft.app.util

import com.vsoft.app.data.model.WorkItemEntity

/** Parses Latin / Persian / Arabic digits. Returns null when empty or longer than 12 digits. */
fun parseJobNumber(s: String): Long? {
    val sb = StringBuilder()
    for (c in s) {
        when (c) {
            in '0'..'9' -> sb.append(c)
            in '\u06F0'..'\u06F9' -> sb.append('0' + (c - '\u06F0'))
            in '\u0660'..'\u0669' -> sb.append('0' + (c - '\u0660'))
            else -> Unit
        }
    }
    val digits = sb.toString()
    return if (digits.isEmpty() || digits.length > 12) null else digits.toLongOrNull()
}

/** 0..1 progress of a group of work items. Over-delivery on one item never inflates the total. */
fun jobProgress(items: List<WorkItemEntity>, doneByItem: Map<Long, Int>): Float {
    val target = items.sumOf { it.targetQty.toLong() }
    if (target <= 0L) return 0f
    val done = items.sumOf { minOf(doneByItem[it.id] ?: 0, it.targetQty).toLong() }
    return (done.toFloat() / target).coerceIn(0f, 1f)
}
