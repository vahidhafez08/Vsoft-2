package com.vsoft.app.ui.screens.transactions

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vsoft.app.data.model.ExpenseCategories
import com.vsoft.app.data.model.IncomeCategories
import com.vsoft.app.data.model.TransactionEntity
import com.vsoft.app.data.repository.DocumentRepository
import com.vsoft.app.data.repository.FinanceRepository
import com.vsoft.app.ui.components.form.FormAccountOption
import com.vsoft.app.util.UiEvents
import com.vsoft.app.util.safeLaunch
import com.vsoft.app.util.startOfDay
import com.vsoft.app.util.toMoney
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class TransactionFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val finance: FinanceRepository,
    private val documents: DocumentRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val editId: Long = savedStateHandle.get<Long>("id") ?: -1L
    val isEdit: Boolean get() = editId > 0L
    var isIncome by mutableStateOf((savedStateHandle.get<String>("type") ?: "expense") == "income")
    var category by mutableStateOf<String?>(null)
    var amountRaw by mutableStateOf("")
    var accountId by mutableStateOf<Long?>(null)
    var dayMillis by mutableStateOf(startOfDay(System.currentTimeMillis()))
    var hour by mutableStateOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY))
    var minute by mutableStateOf(Calendar.getInstance().get(Calendar.MINUTE))
    var note by mutableStateOf("")
    var attachmentUri by mutableStateOf<Uri?>(null)
    var attachmentName by mutableStateOf<String?>(null)
    var saving by mutableStateOf(false)
    private var originalTitle: String? = null
    private val _done = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val done = _done.asSharedFlow()

    val accounts: StateFlow<List<FormAccountOption>> =
        combine(finance.observeAccounts(), finance.observeAccountBalances()) { list, balances ->
            list.map { a -> FormAccountOption(a.id, a.name, (balances[a.id] ?: a.balance).toMoney()) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun categoryNames(): List<String> = if (isIncome) IncomeCategories.toList() else ExpenseCategories.toList()
    val canSave: Boolean get() = (amountRaw.toLongOrNull() ?: 0L) > 0L && category != null && !saving

    init {
        val last = context.getSharedPreferences("vsoft_prefs", Context.MODE_PRIVATE).getLong("last_account_id", -1L)
        if (!isEdit && last > 0L) safeLaunch {
            if (finance.observeAccounts().first().any { it.id == last }) accountId = last
        }
        if (isEdit) safeLaunch {
            val tx = finance.observeTransactions().first().firstOrNull { it.id == editId }
            if (tx == null) {
                UiEvents.show("تراکنش پیدا نشد")
                _done.tryEmit(Unit)
            } else {
                isIncome = tx.isIncome
                category = tx.category
                amountRaw = tx.amount.toString()
                accountId = tx.accountId
                dayMillis = startOfDay(tx.dateMillis)
                val cal = Calendar.getInstance().apply { timeInMillis = tx.dateMillis }
                hour = cal.get(Calendar.HOUR_OF_DAY)
                minute = cal.get(Calendar.MINUTE)
                originalTitle = tx.title
            }
        }
    }

    fun setAttachment(uri: Uri) {
        attachmentUri = uri
        attachmentName = try {
            context.contentResolver.query(uri, null, null, null, null)?.use { c ->
                val i = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (c.moveToFirst() && i >= 0) c.getString(i) else null
            }
        } catch (_: Exception) { null } ?: "فایل انتخاب‌شده"
    }
    fun clearAttachment() { attachmentUri = null; attachmentName = null }

    fun save() {
        if (!canSave) return
        val cat = category ?: return
        val amount = amountRaw.toLongOrNull() ?: return
        saving = true
        safeLaunch {
            try {
                val millis = dayMillis + hour * 3_600_000L + minute * 60_000L
                val cleanNote = note.trim().take(250)
                val title = if (cleanNote.isNotBlank()) cleanNote else (originalTitle ?: cat)
                finance.saveTransaction(TransactionEntity(
                    id = if (isEdit) editId else 0L, title = title, amount = amount,
                    isIncome = isIncome, category = cat, dateMillis = millis, accountId = accountId
                ))
                context.getSharedPreferences("vsoft_prefs", Context.MODE_PRIVATE).edit()
                    .putLong("last_account_id", accountId ?: -1L).apply()
                val uri = attachmentUri
                if (uri != null && !isEdit) {
                    val result = documents.addFromUri(uri, "$cat - ${cleanNote.ifBlank { amountRaw }}", 2, "ضمیمه‌ی تراکنش")
                    if (result.isFailure) UiEvents.show("ذخیره‌ی ضمیمه ناموفق بود")
                }
                UiEvents.show("تراکنش ذخیره شد ✅")
                _done.tryEmit(Unit)
            } finally { saving = false }
        }
    }
}
