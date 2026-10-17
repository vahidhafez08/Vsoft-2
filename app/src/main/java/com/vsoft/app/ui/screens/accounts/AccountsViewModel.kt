package com.vsoft.app.ui.screens.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vsoft.app.data.model.AccountEntity
import com.vsoft.app.data.model.TransferEntity
import com.vsoft.app.data.repository.FinanceRepository
import com.vsoft.app.util.safeLaunch
import com.vsoft.app.util.UiEvents
import kotlinx.coroutines.CancellationException
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
    val transfers: List<TransferEntity> = emptyList(),
    val accountNames: Map<Long, String> = emptyMap(),
    val loaded: Boolean = false
)

@HiltViewModel
class AccountsViewModel @Inject constructor(
    private val repository: FinanceRepository
) : ViewModel() {

    val state: StateFlow<AccountsUiState> =
        combine(repository.observeAccounts(), repository.observeAccountBalances(), repository.observeTransfers()) { list, balances, transfers ->
            AccountsUiState(
                accounts = list,
                total = list.sumOf { balances[it.id] ?: it.balance },
                balances = balances,
                transfers = transfers,
                accountNames = list.associate { it.id to it.name },
                loaded = true
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AccountsUiState())

    fun save(account: AccountEntity) {
        safeLaunch { repository.saveAccount(account) }
    }

    fun delete(account: AccountEntity) {
        safeLaunch { repository.deleteAccount(account) }
    }

    fun addTransfer(fromId: Long, toId: Long, amount: Long, dateMillis: Long, note: String) {
        safeLaunch {
            try {
                val list = state.value.accounts
                val from = list.firstOrNull { it.id == fromId }
                    ?: throw IllegalArgumentException("حساب مبدأ در دسترس نیست")
                val to = list.firstOrNull { it.id == toId }
                    ?: throw IllegalArgumentException("حساب مقصد در دسترس نیست")
                repository.addTransfer(from, to, amount, dateMillis, note)
                UiEvents.show("انتقال ثبت شد ✅")
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (exception: IllegalArgumentException) {
                UiEvents.show(exception.message ?: "اطلاعات انتقال معتبر نیست")
            }
        }
    }

    fun deleteTransfer(transfer: TransferEntity) {
        safeLaunch {
            repository.deleteTransfer(transfer)
            UiEvents.show("انتقال حذف شد")
        }
    }
}
