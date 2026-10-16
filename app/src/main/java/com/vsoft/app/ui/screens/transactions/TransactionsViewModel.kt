package com.vsoft.app.ui.screens.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vsoft.app.data.model.TransactionEntity
import com.vsoft.app.data.repository.FinanceRepository
import com.vsoft.app.util.dayLabel
import com.vsoft.app.util.startOfDay
import com.vsoft.app.util.toLatinDigits
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TxFilters(
    val query: String = "",
    val type: Int = 0,          // 0 all, 1 income, 2 expense
    val category: String? = null
)

data class TxGroup(
    val key: Long,
    val label: String,
    val items: List<TransactionEntity>,
    val net: Long
)

data class TransactionsUiState(
    val groups: List<TxGroup> = emptyList(),
    val income: Long = 0L,
    val expense: Long = 0L,
    val count: Int = 0,
    val categories: List<String> = emptyList(),
    val loaded: Boolean = false
)

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val repository: FinanceRepository
) : ViewModel() {

    private val filters = MutableStateFlow(TxFilters())

    val state: StateFlow<TransactionsUiState> =
        combine(repository.observeTransactions(), filters) { all, f -> build(all, f) }
            .flowOn(Dispatchers.Default)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TransactionsUiState())

    fun setFilters(f: TxFilters) {
        filters.value = f
    }

    fun save(tx: TransactionEntity) {
        viewModelScope.launch { repository.saveTransaction(tx) }
    }

    fun delete(tx: TransactionEntity) {
        viewModelScope.launch { repository.deleteTransaction(tx) }
    }

    fun restore(tx: TransactionEntity) {
        viewModelScope.launch { repository.saveTransaction(tx) }
    }

    private fun build(all: List<TransactionEntity>, f: TxFilters): TransactionsUiState {
        val byType = all.filter {
            f.type == 0 || (f.type == 1 && it.isIncome) || (f.type == 2 && !it.isIncome)
        }
        val categories = byType.map { it.category }.distinct()
        val q = f.query.trim().toLatinDigits().lowercase()
        val filtered = byType.filter { t ->
            (f.category == null || t.category == f.category) &&
                (q.isEmpty() ||
                    t.title.lowercase().contains(q) ||
                    t.category.contains(q) ||
                    t.amount.toString().contains(q))
        }
        val income = filtered.filter { it.isIncome }.sumOf { it.amount }
        val expense = filtered.filter { !it.isIncome }.sumOf { it.amount }
        val groups = filtered
            .groupBy { startOfDay(it.dateMillis) }
            .map { (day, items) ->
                TxGroup(
                    key = day,
                    label = dayLabel(items.first().dateMillis),
                    items = items,
                    net = items.sumOf { if (it.isIncome) it.amount else -it.amount }
                )
            }
        return TransactionsUiState(
            groups = groups,
            income = income,
            expense = expense,
            count = filtered.size,
            categories = categories,
            loaded = true
        )
    }
}
