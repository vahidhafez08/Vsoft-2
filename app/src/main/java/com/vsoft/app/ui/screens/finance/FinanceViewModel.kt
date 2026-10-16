package com.vsoft.app.ui.screens.finance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vsoft.app.data.model.AccountEntity
import com.vsoft.app.data.model.TransactionEntity
import com.vsoft.app.data.repository.FinanceRepository
import com.vsoft.app.util.JRange
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class FinanceUiState(
    val income: Long = 0L,
    val expense: Long = 0L,
    val prevIncome: Long = 0L,
    val prevExpense: Long = 0L,
    val incomeByCategory: List<Pair<String, Long>> = emptyList(),
    val expenseByCategory: List<Pair<String, Long>> = emptyList(),
    val accounts: List<AccountEntity> = emptyList()
)

@HiltViewModel
class FinanceViewModel @Inject constructor(
    repository: FinanceRepository
) : ViewModel() {

    val state: StateFlow<FinanceUiState> =
        combine(repository.observeTransactions(), repository.observeAccounts()) { tx, acc ->
            build(tx, acc)
        }
            .flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FinanceUiState())

    private fun build(tx: List<TransactionEntity>, acc: List<AccountEntity>): FinanceUiState {
        val now = System.currentTimeMillis()
        val curFrom = JRange.monthStart(now)
        val prevFrom = JRange.prevMonthStart(now)
        val prevTo = minOf(curFrom, prevFrom + (now - curFrom))

        val current = tx.filter { it.dateMillis >= curFrom }
        val previous = tx.filter { it.dateMillis >= prevFrom && it.dateMillis < prevTo }
        val incomes = current.filter { it.isIncome }
        val expenses = current.filter { !it.isIncome }

        fun group(list: List<TransactionEntity>): List<Pair<String, Long>> =
            list.groupBy { it.category }
                .map { (k, v) -> k to v.sumOf { it.amount } }
                .sortedByDescending { it.second }

        return FinanceUiState(
            income = incomes.sumOf { it.amount },
            expense = expenses.sumOf { it.amount },
            prevIncome = previous.filter { it.isIncome }.sumOf { it.amount },
            prevExpense = previous.filter { !it.isIncome }.sumOf { it.amount },
            incomeByCategory = group(incomes),
            expenseByCategory = group(expenses),
            accounts = acc
        )
    }
}
