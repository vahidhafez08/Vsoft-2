package com.vsoft.app.util

import com.vsoft.app.data.model.SubtaskEntity
import com.vsoft.app.data.model.TaskEntity

/** 0..1 completion of a task: done = 1, else share of finished subtasks. */
fun taskFraction(task: TaskEntity, subs: List<SubtaskEntity>): Float = when {
    task.isDone -> 1f
    subs.isEmpty() -> 0f
    else -> subs.count { it.isDone }.toFloat() / subs.size
}

fun isOverdue(due: Long?, done: Boolean): Boolean =
    due != null && !done && due < startOfDay(System.currentTimeMillis())
