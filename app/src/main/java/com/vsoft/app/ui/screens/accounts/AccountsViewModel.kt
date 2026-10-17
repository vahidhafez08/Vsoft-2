package com.vsoft.app.ui.screens.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vsoft.app.data.model.AccountEntity
import com.vsoft.app.data.repository.FinanceRepository
import com.vsoft.app.util.safeLaunch
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AccountsUiState(
    val accounts: List<AccountEntity> = emptyList(),
    val total: Long = 0L,
    val balances: Map<Long, Long> = emptyMap(),
    val loaded: Boolean = false
)

@HiltViewModel
class AccountsViewModel @Inject constructor(
    private val repository: FinanceRepository
) : ViewModel() {

    val state: StateFlow<AccountsUiState> =
        combine(repository.observeAccounts(), repository.observeAccountBalances()) { list, balances ->
            AccountsUiState(list, list.sumOf { balances[it.id] ?: it.balance }, balances, true)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AccountsUiState())

    fun save(account: AccountEntity) {
        safeLaunch { repository.saveAccount(account) }
    }

    fun delete(account: AccountEntity) {
        safeLaunch { repository.deleteAccount(account) }
    }

    fun transfer(fromId: Long, toId: Long, amount: Long) {
        safeLaunch {
            val list = state.value.accounts
            val from = list.firstOrNull { it.id == fromId }
            val to = list.firstOrNull { it.id == toId }
            if (from != null && to != null && from.id != to.id && amount > 0L && amount <= from.balance) {
                repository.transfer(from, to, amount)
            }
        }
    }
}
