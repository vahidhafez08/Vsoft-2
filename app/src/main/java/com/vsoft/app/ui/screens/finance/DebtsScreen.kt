package com.vsoft.app.ui.screens.finance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vsoft.app.data.model.DebtEntity
import com.vsoft.app.data.model.DebtType
import com.vsoft.app.data.repository.FinanceRepository
import com.vsoft.app.ui.theme.Vsoft
import com.vsoft.app.util.toMoney
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DebtsViewModel @Inject constructor(private val repo: FinanceRepository) : ViewModel() {
    val debts = repo.observeDebts().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    suspend fun save(name: String, type: String, amount: Long, due: Long?, note: String) {
        if (name.isBlank() || amount <= 0L) return
        repo.saveDebt(DebtEntity(personName = name.trim(), type = type, totalAmount = amount,
            dateMillis = System.currentTimeMillis(), dueDateMillis = due, note = note.trim()))
    }

    fun settle(debt: DebtEntity, settled: Boolean) = viewModelScope.launch {
        repo.setDebtManualSettlement(debt, settled)
    }

    fun delete(debt: DebtEntity) = viewModelScope.launch { repo.deleteDebt(debt) }
    suspend fun paid(id: Long) = repo.debtPaid(id)
}

@Composable
fun DebtsScreen(
    onBack: () -> Unit,
    onAddDebt: () -> Unit,
    vm: DebtsViewModel = hiltViewModel()
) {
    val debts by vm.debts.collectAsStateWithLifecycle()
    var tab by remember { mutableIntStateOf(0) }
    var deleting by remember { mutableStateOf<DebtEntity?>(null) }
    var paid by remember { mutableStateOf<Map<Long, Long>>(emptyMap()) }

    LaunchedEffect(debts) {
        paid = debts.associate { it.id to vm.paid(it.id) }
    }

    val owing = debts.filter { it.type == DebtType.I_OWE.name && !it.isSettled }
        .sumOf { (it.totalAmount - (paid[it.id] ?: 0L)).coerceAtLeast(0L) }
    val owedToMe = debts.filter { it.type == DebtType.OWED_TO_ME.name && !it.isSettled }
        .sumOf { (it.totalAmount - (paid[it.id] ?: 0L)).coerceAtLeast(0L) }
    val filtered = debts.filter { it.type == if (tab == 0) DebtType.I_OWE.name else DebtType.OWED_TO_ME.name }

    Column(Modifier.fillMaxSize().background(Vsoft.colors.bg).padding(horizontal = 16.dp)) {
        Row(Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "بازگشت", tint = Vsoft.colors.textPrimary) }
            Column(Modifier.weight(1f)) {
                Text("بدهی و طلب", color = Vsoft.colors.textPrimary, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                Text("مدیریت مانده حساب‌های شخصی", color = Vsoft.colors.textSecondary, fontSize = 12.sp)
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Vsoft.colors.surface)
        ) {
            Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1f)) {
                    Text("بدهی من", color = Vsoft.colors.textSecondary, fontSize = 12.sp)
                    Spacer(Modifier.height(5.dp))
                    Text("${owing.toMoney()} تومان", color = Color(0xFFE16B63), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Column(Modifier.weight(1f)) {
                    Text("طلب من", color = Vsoft.colors.textSecondary, fontSize = 12.sp)
                    Spacer(Modifier.height(5.dp))
                    Text("${owedToMe.toMoney()} تومان", color = Color(0xFF279B70), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        TabRow(selectedTabIndex = tab, containerColor = Vsoft.colors.bg) {
            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("بدهکارم") })
            Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("طلبکارم") })
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(vertical = 12.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (filtered.isEmpty()) {
                item {
                    Text(
                        if (tab == 0) "هنوز بدهی‌ای ثبت نکرده‌ای." else "هنوز طلبی ثبت نکرده‌ای.",
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                        color = Vsoft.colors.textSecondary
                    )
                }
            }
            items(filtered, key = { it.id }) { debt ->
                val settledAmount = paid[debt.id] ?: 0L
                val remaining = (debt.totalAmount - settledAmount).coerceAtLeast(0L)
                val progress = if (debt.totalAmount > 0L) (settledAmount.toFloat() / debt.totalAmount).coerceIn(0f, 1f) else 0f
                Card(colors = CardDefaults.cardColors(containerColor = Vsoft.colors.surface), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(debt.personName, color = Vsoft.colors.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                                Text(if (debt.isSettled) "تسویه‌شده" else "باقی‌مانده", color = Vsoft.colors.textSecondary, fontSize = 12.sp)
                            }
                            Text("${remaining.toMoney()} تومان", color = Vsoft.colors.textPrimary, fontWeight = FontWeight.Bold)
                        }
                        LinearProgressIndicator(progress = progress, modifier = Modifier.fillMaxWidth())
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("مبلغ اولیه: ${debt.totalAmount.toMoney()}", color = Vsoft.colors.textSecondary, fontSize = 11.sp)
                            Text("پرداخت‌شده: ${settledAmount.toMoney()}", color = Vsoft.colors.textSecondary, fontSize = 11.sp)
                        }
                        if (debt.note.isNotBlank()) Text(debt.note, color = Vsoft.colors.textSecondary, fontSize = 12.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(onClick = { vm.settle(debt, !debt.isSettled) }) {
                                Text(if (debt.isSettled) "لغو تسویه" else "تسویه دستی")
                            }
                            Spacer(Modifier.weight(1f))
                            IconButton(onClick = { deleting = debt }) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Vsoft.colors.textSecondary)
                            }
                        }
                    }
                }
            }
        }

        Button(onClick = onAddDebt, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
            Icon(Icons.Default.Add, null)
            Text("ثبت بدهی یا طلب")
        }
    }

    deleting?.let { debt ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("حذف مورد") },
            text = { Text("این بدهی یا طلب حذف شود؟ تراکنش‌های مرتبط باقی می‌مانند.") },
            confirmButton = {
                TextButton(onClick = { vm.delete(debt); deleting = null }) { Text("حذف") }
            },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text("انصراف") } }
        )
    }
}
