package com.vsoft.app.ui.screens.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vsoft.app.data.db.TransactionDao
import com.vsoft.app.data.model.TransactionEntity
import com.vsoft.app.util.DAY_MS
import com.vsoft.app.util.JCal
import com.vsoft.app.util.JRange
import com.vsoft.app.util.startOfDay
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class CatSlice(val name: String, val amount: Long)

data class ReportsUi(
    val rangeLabel: String = "",
    val income: Long = 0L,
    val expense: Long = 0L,
    val prevIncome: Long = 0L,
    val prevExpense: Long = 0L,
    val avgDailyExpense: Long = 0L,
    val count: Int = 0,
    val labels: List<String> = emptyList(),
    val incomeSeries: List<Long> = emptyList(),
    val expenseSeries: List<Long> = emptyList(),
    val expenseCats: List<CatSlice> = emptyList(),
    val incomeCats: List<CatSlice> = emptyList(),
    val loaded: Boolean = false
)

private class Range(val start: Long, val end: Long)

private val seasonNames = listOf("بهار", "تابستان", "پاییز", "زمستان")

private fun monthStart(idx: Int): Long = JCal.fromJalali(idx / 12, idx % 12 + 1, 1)

/** period: 0 Jalali week (Saturday-Friday), 1 Jalali month, 2 Jalali season, 3 Jalali year. offset 0 = current, -1 = previous. */
private fun rangeFor(period: Int, offset: Int, todayStart: Long, today: IntArray): Range = when (period) {
    0 -> {
        val start = JRange.weekStart(todayStart)
        val previousStart = JRange.prevWeekStart(todayStart)
        val end = if (offset == 0) todayStart + DAY_MS else minOf(start, previousStart + (todayStart - start))
        Range(if (offset == 0) start else previousStart, end)
    }
    1 -> {
        val idx = today[0] * 12 + (today[1] - 1) + offset
        Range(monthStart(idx), monthStart(idx + 1))
    }
    2 -> {
        val idx = today[0] * 12 + ((today[1] - 1) / 3) * 3 + offset * 3
        Range(monthStart(idx), monthStart(idx + 3))
    }
    else -> {
        val idx = today[0] * 12 + offset * 12
        Range(monthStart(idx), monthStart(idx + 12))
    }
}

private fun slices(txs: List<TransactionEntity>, income: Boolean): List<CatSlice> {
    val sorted = txs
        .filter { it.isIncome == income }
        .groupBy { it.category.ifBlank { "سایر" } }
        .map { e -> CatSlice(e.key, e.value.sumOf { t -> t.amount }) }
        .sortedByDescending { it.amount }
    if (sorted.size <= 6) return sorted
    val rest = sorted.drop(5).sumOf { it.amount }
    return sorted.take(5) + CatSlice("سایر", rest)
}

@HiltViewModel
class ReportsViewModel @Inject constructor(dao: TransactionDao) : ViewModel() {

    private val period = MutableStateFlow(1)

    val state: StateFlow<ReportsUi> =
        combine(dao.observeAll(), period) { all, p -> build(all, p) }
            .flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReportsUi())

    fun setPeriod(p: Int) {
        period.value = p.coerceIn(0, 3)
    }

    private fun build(all: List<TransactionEntity>, p: Int): ReportsUi {
        val now = System.currentTimeMillis()
        val todayStart = startOfDay(now)
        val today = JCal.toJalali(todayStart)
        val cur = if (p == 0) {
            val start = JRange.weekStart(now)
            Range(start, now + 1L)
        } else {
            rangeFor(p, 0, todayStart, today)
        }
        val prev = if (p == 0) {
            val start = JRange.weekStart(now)
            val previousStart = JRange.prevWeekStart(now)
            Range(previousStart, minOf(start, previousStart + (now - start)))
        } else {
            rangeFor(p, -1, todayStart, today)
        }
        val txs = all.filter { it.dateMillis >= cur.start && it.dateMillis < cur.end }
        val prevTxs = all.filter { it.dateMillis >= prev.start && it.dateMillis < prev.end }
        val j = JCal.toJalali(cur.start)

        val starts: List<Long>
        val labels: List<String>
        val rangeLabel: String
        when (p) {
            0 -> {
                starts = List(7) { cur.start + it * DAY_MS }
                labels = JCal.dayHeaders
                rangeLabel = "هفته جاری"
            }
            1 -> {
                val n = JCal.monthLength(j[0], j[1])
                starts = List(n) { JCal.fromJalali(j[0], j[1], it + 1) }
                labels = List(n) { JCal.fa(it + 1) }
                rangeLabel = JCal.monthNames[(j[1] - 1).coerceIn(0, 11)] + " " + JCal.fa(j[0])
            }
            else -> {
                val count = if (p == 2) 3 else 12
                val base = j[0] * 12 + (j[1] - 1)
                starts = List(count) { monthStart(base + it) }
                labels = List(count) { JCal.monthNames[(base + it) % 12] }
                rangeLabel = if (p == 2) {
                    seasonNames[((j[1] - 1) / 3).coerceIn(0, 3)] + " " + JCal.fa(j[0])
                } else {
                    "سال " + JCal.fa(j[0])
                }
            }
        }

        val inc = LongArray(starts.size)
        val exp = LongArray(starts.size)
        for (t in txs) {
            val i = starts.indexOfLast { s -> s <= t.dateMillis }
            if (i >= 0) {
                if (t.isIncome) inc[i] += t.amount else exp[i] += t.amount
            }
        }
        val visible = if (p == 0) 7 else starts.count { it <= todayStart }.coerceAtLeast(1)

        val income = txs.filter { it.isIncome }.sumOf { it.amount }
        val expense = txs.filter { !it.isIncome }.sumOf { it.amount }
        val days = ((minOf(cur.end, todayStart + DAY_MS) - cur.start) / DAY_MS).toInt().coerceAtLeast(1)

        return ReportsUi(
            rangeLabel = rangeLabel,
            income = income,
            expense = expense,
            prevIncome = prevTxs.filter { it.isIncome }.sumOf { it.amount },
            prevExpense = prevTxs.filter { !it.isIncome }.sumOf { it.amount },
            avgDailyExpense = expense / days,
            count = txs.size,
            labels = labels,
            incomeSeries = inc.take(visible),
            expenseSeries = exp.take(visible),
            expenseCats = slices(txs, false),
            incomeCats = slices(txs, true),
            loaded = true
        )
    }
}
