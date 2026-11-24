package com.vsoft.app.ui.screens.finance

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.hilt.navigation.compose.hiltViewModel
import com.vsoft.app.data.model.DebtEntity
import com.vsoft.app.data.model.DebtType
import com.vsoft.app.data.repository.FinanceRepository
import com.vsoft.app.ui.components.form.*
import com.vsoft.app.ui.theme.Vsoft
import com.vsoft.app.util.DAY_MS
import com.vsoft.app.util.toLatinDigits
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DebtFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repo: FinanceRepository
) : ViewModel() {
    private val editId = savedStateHandle.get<Long>("id") ?: -1L
    val isEdit = editId > 0L
    private var original: DebtEntity? = null

    var person by mutableStateOf("")
    var amountRaw by mutableStateOf("")
    var note by mutableStateOf("")
    var type by mutableStateOf(DebtType.I_OWE.name)
    var dateMillis by mutableLongStateOf(System.currentTimeMillis())
    var dueEnabled by mutableStateOf(false)
    var dueMillis by mutableLongStateOf(System.currentTimeMillis() + 30L * DAY_MS)
    var loading by mutableStateOf(isEdit)
        private set
    var saving by mutableStateOf(false)
        private set

    private val amount get() = amountRaw.toLatinDigits().toLongOrNull() ?: 0L
    val canSave get() = person.isNotBlank() && amount > 0L && !saving && (!isEdit || original != null)

    init {
        if (isEdit) viewModelScope.launch {
            val debt = repo.observeDebts().first().firstOrNull { it.id == editId }
            if (debt != null) {
                original = debt
                person = debt.personName
                amountRaw = debt.totalAmount.toString()
                note = debt.note
                type = debt.type
                dateMillis = debt.dateMillis
                debt.dueDateMillis?.let { dueEnabled = true; dueMillis = it }
            }
            loading = false
        }
    }

    fun save(onDone: () -> Unit) {
        if (!canSave) return
        viewModelScope.launch {
            saving = true
            try {
                val base = original ?: DebtEntity(personName = person.trim(), type = type, totalAmount = amount, dateMillis = dateMillis)
                repo.saveDebt(base.copy(
                    personName = person.trim(),
                    type = type,
                    totalAmount = amount,
                    dateMillis = dateMillis,
                    dueDateMillis = if (dueEnabled) dueMillis else null,
                    note = note.trim()
                ))
                onDone()
            } finally {
                saving = false
            }
        }
    }
}

private enum class DebtDatePick { NONE, REGISTERED, DUE }

@Composable
fun DebtFormScreen(onBack: () -> Unit, vm: DebtFormViewModel = hiltViewModel()) {
    val c = Vsoft.colors
    var picking by remember { mutableStateOf(DebtDatePick.NONE) }
    FormScaffold(
        title = if (vm.isEdit) "ویرایش بدهی یا طلب" else "ثبت بدهی یا طلب",
        subtitle = "حساب شخصی",
        icon = Icons.Default.AccountBalanceWallet,
        onBack = onBack,
        bottomBar = {
            FormPrimaryButton(if (vm.isEdit) "ذخیره تغییرات" else "ذخیره", vm.canSave, vm.saving) { vm.save(onBack) }
        }
    ) {
        if (vm.loading) {
            Box(Modifier.fillMaxWidth().padding(28.dp), contentAlignment = Alignment.Center) {
                androidx.compose.material3.CircularProgressIndicator()
            }
        } else {
            DebtTypeToggle(vm.type) { vm.type = it }
            FormCard(Icons.Default.People, "اطلاعات شخص") {
                FormTextField(value = vm.person, onValueChange = { vm.person = it.take(60) }, placeholder = "نام شخص یا مجموعه")
            }
            FormCard(Icons.Default.Payments, "مبلغ") {
                FormAmountField(value = vm.amountRaw, onValueChange = { vm.amountRaw = it }, placeholder = "مبلغ بدهی یا طلب",
                    trailing = { Text("تومان", color = c.textSecondary, fontSize = 13.sp) })
            }
            FormCard(Icons.Default.CalendarMonth, "تاریخ‌ها") {
                FormPickerRow(Icons.Default.CalendarMonth, FormFormat.date(vm.dateMillis),
                    "تاریخ ثبت · ${FormFormat.weekday(vm.dateMillis)}", onClick = { picking = DebtDatePick.REGISTERED })
                FormSwitchRow("تاریخ سررسید", "برای نمایش وضعیت و تأخیر", vm.dueEnabled) { vm.dueEnabled = it }
                if (vm.dueEnabled) FormPickerRow(Icons.Default.Schedule, FormFormat.date(vm.dueMillis),
                    "سررسید · ${FormFormat.weekday(vm.dueMillis)}", onClick = { picking = DebtDatePick.DUE })
            }
            FormCard(Icons.Default.Description, "توضیحات (اختیاری)") {
                FormTextField(value = vm.note, onValueChange = { vm.note = it.take(250) },
                    placeholder = "توضیحات بیشتر...", singleLine = false, minLines = 3, minHeight = 96.dp)
                FormCounterText(vm.note.length, 250)
            }
        }
    }
    when (picking) {
        DebtDatePick.REGISTERED -> FormDatePickerSheet(vm.dateMillis, onPick = { vm.dateMillis = it; picking = DebtDatePick.NONE }, onDismiss = { picking = DebtDatePick.NONE })
        DebtDatePick.DUE -> FormDatePickerSheet(vm.dueMillis, onPick = { vm.dueMillis = it; picking = DebtDatePick.NONE }, onDismiss = { picking = DebtDatePick.NONE })
        DebtDatePick.NONE -> Unit
    }
}

@Composable
private fun DebtTypeToggle(type: String, onChange: (String) -> Unit) {
    val c = Vsoft.colors
    val shape = RoundedCornerShape(20.dp)
    Row(Modifier.fillMaxWidth().clip(shape).background(c.surface)
        .border(1.dp, c.outline.copy(alpha = .6f), shape).padding(6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        DebtTypeOption("من بدهکارم", "باید پرداخت کنم", Icons.Default.ArrowUpward, DebtOweColor,
            type == DebtType.I_OWE.name, { onChange(DebtType.I_OWE.name) }, Modifier.weight(1f))
        DebtTypeOption("طلبکارم", "باید دریافت کنم", Icons.Default.ArrowDownward, DebtOwedColor,
            type == DebtType.OWED_TO_ME.name, { onChange(DebtType.OWED_TO_ME.name) }, Modifier.weight(1f))
    }
}

@Composable
private fun DebtTypeOption(title: String, subtitle: String, icon: ImageVector, tint: Color,
    selected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val c = Vsoft.colors
    val bg by animateColorAsState(if (selected) tint else c.surfaceHigh, tween(220), label = "debtType")
    Row(modifier.height(66.dp).clip(RoundedCornerShape(16.dp)).background(bg).clickable(onClick = onClick)
        .padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(32.dp).clip(CircleShape)
            .background(if (selected) Color.White.copy(alpha = .2f) else tint.copy(alpha = .15f)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = if (selected) Color.White else tint, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(8.dp))
        Column {
            Text(title, color = if (selected) Color.White else c.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = if (selected) Color.White.copy(alpha = .85f) else c.textSecondary, fontSize = 10.sp)
        }
    }
}
