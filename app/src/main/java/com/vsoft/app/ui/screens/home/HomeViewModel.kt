package com.vsoft.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vsoft.app.data.model.AccountEntity
import com.vsoft.app.data.model.TransactionEntity
import com.vsoft.app.data.repository.FinanceRepository
import com.vsoft.app.util.DAY_MS
import com.vsoft.app.util.JCal
import com.vsoft.app.util.percentChange
import com.vsoft.app.util.startOfDay
import com.vsoft.app.util.weekdayShort
import com.vsoft.app.util.safeLaunch
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val totalBalance: Long = 0L,
    val income: Long = 0L,
    val expense: Long = 0L,
    val todayExpense: Long = 0L,
    val incomeChange: Int? = null,
    val expenseChange: Int? = null,
    val weekIncome: List<Float> = List(7) { 0f },
    val weekExpense: List<Float> = List(7) { 0f },
    val weekLabels: List<String> = List(7) { "" }
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: FinanceRepository
) : ViewModel() {

    val state: StateFlow<HomeUiState> =
        combine(repository.observeTransactions(), repository.observeAccounts(), repository.observeAccountBalances()) { tx, acc, balances ->
            buildState(tx, acc, balances)
        }
            .flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun addTransaction(title: String, amount: Long, isIncome: Boolean, category: String, accountId: Long? = null) {
        safeLaunch {
            repository.addTransaction(title, amount, isIncome, category, accountId)
        }
    }

    private fun buildState(tx: List<TransactionEntity>, acc: List<AccountEntity>, balances: Map<Long, Long>): HomeUiState {
        val now = System.currentTimeMillis()
        val todayStart = startOfDay(now)
        val j = JCal.toJalali(todayStart)
        val curFrom = JCal.fromJalali(j[0], j[1], 1)
        val prevFrom = if (j[1] == 1) JCal.fromJalali(j[0] - 1, 12, 1) else JCal.fromJalali(j[0], j[1] - 1, 1)
        val prevTo = minOf(curFrom, prevFrom + (now - curFrom))

        val cur = tx.filter { it.dateMillis >= curFrom }
        val prev = tx.filter { it.dateMillis >= prevFrom && it.dateMillis < prevTo }

        val income = cur.filter { it.isIncome }.sumOf { it.amount }
        val expense = cur.filter { !it.isIncome }.sumOf { it.amount }
        val prevIncome = prev.filter { it.isIncome }.sumOf { it.amount }
        val prevExpense = prev.filter { !it.isIncome }.sumOf { it.amount }

        val todayExpense = tx.filter { !it.isIncome && it.dateMillis >= todayStart }.sumOf { it.amount }

        val dayStarts = List(7) { i -> todayStart - (6 - i) * DAY_MS }
        val weekIncome = dayStarts.map { s ->
            tx.filter { it.isIncome && it.dateMillis >= s && it.dateMillis < s + DAY_MS }
                .sumOf { it.amount }.toFloat()
        }
        val weekExpense = dayStarts.map { s ->
            tx.filter { !it.isIncome && it.dateMillis >= s && it.dateMillis < s + DAY_MS }
                .sumOf { it.amount }.toFloat()
        }

        return HomeUiState(
            totalBalance = acc.sumOf { balances[it.id] ?: it.balance },
            income = income,
            expense = expense,
            todayExpense = todayExpense,
            incomeChange = percentChange(income, prevIncome),
            expenseChange = percentChange(expense, prevExpense),
            weekIncome = weekIncome,
            weekExpense = weekExpense,
            weekLabels = dayStarts.map { weekdayShort(it) }
        )
    }
}