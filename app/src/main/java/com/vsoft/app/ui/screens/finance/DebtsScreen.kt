@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.vsoft.app.ui.screens.finance

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.vsoft.app.data.model.AccountEntity
import com.vsoft.app.data.model.DebtEntity
import com.vsoft.app.data.model.DebtType
import com.vsoft.app.data.model.TransactionEntity
import com.vsoft.app.data.repository.FinanceRepository
import com.vsoft.app.ui.components.form.FormAmountField
import com.vsoft.app.ui.components.form.FormChip
import com.vsoft.app.ui.components.form.FormOption
import com.vsoft.app.ui.components.form.FormOptionSheet
import com.vsoft.app.ui.components.form.FormPrimaryButton
import com.vsoft.app.ui.components.form.FormStyle
import com.vsoft.app.ui.theme.BrandBlue
import com.vsoft.app.ui.theme.Vsoft
import com.vsoft.app.util.DAY_MS
import com.vsoft.app.util.startOfDay
import com.vsoft.app.util.toLatinDigits
import com.vsoft.app.util.toMoney
import com.vsoft.app.util.toPersianText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

internal val DebtOweColor = Color(0xFFE16B63)
internal val DebtOwedColor = Color(0xFF239B72)

data class DebtItem(val debt: DebtEntity, val paid: Long) {
    val isOwe get() = debt.type == DebtType.I_OWE.name
    val isSettled get() = debt.isSettled
    val remaining get() = if (isSettled) 0L else (debt.totalAmount - paid).coerceAtLeast(0L)
    val progress get() = if (isSettled) 1f else if (debt.totalAmount > 0L) (paid.toFloat() / debt.totalAmount).coerceIn(0f, 1f) else 0f
}

@HiltViewModel
class DebtsViewModel @Inject constructor(private val repo: FinanceRepository) : ViewModel() {
    val items: StateFlow<List<DebtItem>> = combine(repo.observeDebts(), repo.observeTransactions()) { debts, txs ->
        val paid = HashMap<Long, Long>()
        txs.forEach { tx -> tx.debtId?.let { id -> paid[id] = (paid[id] ?: 0L) + tx.amount } }
        debts.map { DebtItem(it, paid[it.id] ?: 0L) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val accounts: StateFlow<List<AccountEntity>> =
        repo.observeAccounts().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun settle(debt: DebtEntity, settled: Boolean) = viewModelScope.launch { repo.setDebtManualSettlement(debt, settled) }
    fun delete(debt: DebtEntity) = viewModelScope.launch { repo.deleteDebt(debt) }

    fun pay(item: DebtItem, amount: Long, accountId: Long?) = viewModelScope.launch {
        repo.saveTransaction(TransactionEntity(
            title = if (item.isOwe) "پرداخت بدهی به ${item.debt.personName}" else "دریافت طلب از ${item.debt.personName}",
            amount = amount,
            isIncome = !item.isOwe,
            category = "سایر",
            dateMillis = System.currentTimeMillis(),
            accountId = accountId,
            debtId = item.debt.id
        ))
    }
}

private enum class DebtFilter(val label: String) { ACTIVE("فعال"), SETTLED("تسویه‌شده"), ALL("همه") }

@Composable
fun DebtsScreen(
    onBack: () -> Unit,
    onAddDebt: () -> Unit,
    onEditDebt: (Long) -> Unit,
    vm: DebtsViewModel = hiltViewModel()
) {
    val c = Vsoft.colors
    val debts by vm.items.collectAsStateWithLifecycle()
    val accounts by vm.accounts.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(initialPage = 0) { 2 }
    val scope = rememberCoroutineScope()
    var filter by remember { mutableStateOf(DebtFilter.ACTIVE) }
    var deleting by remember { mutableStateOf<DebtItem?>(null) }
    var paying by remember { mutableStateOf<DebtItem?>(null) }
    val owing = debts.filter { it.isOwe && !it.isSettled }.sumOf { it.remaining }
    val owed = debts.filter { !it.isOwe && !it.isSettled }.sumOf { it.remaining }

    Column(Modifier.fillMaxSize().background(c.bg).padding(horizontal = 16.dp)) {
        Row(Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(c.surface)
                .border(1.dp, c.outline.copy(alpha = .7f), RoundedCornerShape(14.dp)).clickable(onClick = onBack),
                contentAlignment = Alignment.Center) { Icon(Icons.Default.ArrowForward, "بازگشت", tint = c.textPrimary) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("بدهی و طلب", color = c.textPrimary, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                Text("مدیریت مانده حساب‌های شخصی", color = c.textSecondary, fontSize = 12.sp)
            }
        }
        DebtSummaryCard(owing, owed)
        Spacer(Modifier.height(14.dp))
        // Segmented tabs with swipeable pages.
        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(c.surface).padding(4.dp)) {
            listOf("بدهکارم · ${debts.count { it.isOwe }.toPersianText()}", "طلبکارم · ${debts.count { !it.isOwe }.toPersianText()}").forEachIndexed { index, label ->
                val selected = pagerState.currentPage == index
                Box(Modifier.weight(1f).clip(RoundedCornerShape(12.dp))
                    .background(if (selected) BrandBlue else Color.Transparent)
                    .clickable { scope.launch { pagerState.animateScrollToPage(index) } }.padding(vertical = 11.dp),
                    contentAlignment = Alignment.Center) {
                    Text(label, color = if (selected) Color.White else c.textSecondary, fontSize = 13.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium)
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DebtFilter.values().forEach { f ->
                val selected = filter == f
                val bg by animateColorAsState(if (selected) BrandBlue.copy(alpha = .16f) else c.surface, tween(180), label = "debtFilter")
                Box(Modifier.clip(RoundedCornerShape(17.dp)).background(bg)
                    .border(1.dp, if (selected) BrandBlue.copy(alpha = .55f) else c.outline.copy(alpha = .55f), RoundedCornerShape(17.dp))
                    .clickable { filter = f }.padding(horizontal = 16.dp, vertical = 9.dp)) {
                    Text(f.label, color = if (selected) BrandBlue else c.textSecondary, fontSize = 12.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        HorizontalPager(state = pagerState, modifier = Modifier.weight(1f).fillMaxWidth(), verticalAlignment = Alignment.Top) { page ->
            val list = debts.filter { it.isOwe == (page == 0) && when (filter) {
                DebtFilter.ACTIVE -> !it.isSettled
                DebtFilter.SETTLED -> it.isSettled
                DebtFilter.ALL -> true
            } }
            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(top = 2.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (list.isEmpty()) item { DebtEmptyState(page == 0, filter) }
                items(list, key = { it.debt.id }) { item ->
                    DebtCard(item, onPay = { paying = item }, onEdit = { onEditDebt(item.debt.id) },
                        onToggleSettle = { vm.settle(item.debt, !item.isSettled) }, onDelete = { deleting = item })
                }
            }
        }
        Box(Modifier.fillMaxWidth().padding(bottom = 12.dp).height(54.dp).clip(RoundedCornerShape(28.dp))
            .background(FormStyle.gradient).clickable(onClick = onAddDebt), contentAlignment = Alignment.Center) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Add, null, tint = Color.White); Spacer(Modifier.width(8.dp))
                Text("ثبت بدهی یا طلب", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    paying?.let { item ->
        DebtPaymentSheet(item, accounts, onDismiss = { paying = null }, onConfirm = { amount, account ->
            vm.pay(item, amount, account); paying = null
        })
    }
    deleting?.let { item ->
        AlertDialog(onDismissRequest = { deleting = null }, containerColor = c.surface,
            title = { Text("حذف «${item.debt.personName}»", color = c.textPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("این مورد حذف می‌شود. تراکنش‌های مرتبط باقی می‌مانند و از این بدهی/طلب جدا می‌شوند.", color = c.textSecondary) },
            confirmButton = { TextButton(onClick = { vm.delete(item.debt); deleting = null }) { Text("حذف", color = DebtOweColor) } },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text("انصراف") } })
    }
}

@Composable
private fun DebtSummaryCard(owing: Long, owed: Long) {
    val net = owed - owing
    val total = (owing + owed).coerceAtLeast(1L)
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp))
        .background(Brush.linearGradient(listOf(Color(0xFF2F6BFF), Color(0xFF6D5BF6)))).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(when { net > 0 -> "در مجموع طلبکاری"; net < 0 -> "در مجموع بدهکاری"; else -> "تراز کل" },
            color = Color.White.copy(alpha = .78f), fontSize = 12.sp)
        Text((if (net > 0) "+" else "") + net.toMoney() + " تومان", color = Color.White,
            fontSize = 24.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        if (owing + owed > 0L) Row(Modifier.fillMaxWidth().height(6.dp).clip(CircleShape)) {
            if (owing > 0L) Box(Modifier.weight(owing.toFloat() / total).fillMaxHeight().background(Color(0xFFFF9A92)))
            if (owed > 0L) Box(Modifier.weight(owed.toFloat() / total).fillMaxHeight().background(Color(0xFF7BE0B5)))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            DebtSummaryStat("بدهی من", owing, Icons.Default.ArrowUpward, Color(0xFFFF9A92), Modifier.weight(1f))
            DebtSummaryStat("طلب من", owed, Icons.Default.ArrowDownward, Color(0xFF7BE0B5), Modifier.weight(1f))
        }
    }
}

@Composable
private fun DebtSummaryStat(label: String, amount: Long, icon: ImageVector, tint: Color, modifier: Modifier) {
    Row(modifier.clip(RoundedCornerShape(16.dp)).background(Color.White.copy(alpha = .14f)).padding(10.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Column { Text(label, color = Color.White.copy(alpha = .75f), fontSize = 11.sp); Text(amount.toMoney(), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold) }
    }
}

private data class DebtStatus(val label: String, val color: Color)
private fun debtStatus(item: DebtItem): DebtStatus {
    if (item.isSettled) return DebtStatus("تسویه‌شده", DebtOwedColor)
    val due = item.debt.dueDateMillis ?: return DebtStatus("جاری", BrandBlue)
    val days = Math.floorDiv(startOfDay(due) - startOfDay(System.currentTimeMillis()), DAY_MS).toInt()
    return when { days < 0 -> DebtStatus("${(-days).toPersianText()} روز تأخیر", DebtOweColor)
        days == 0 -> DebtStatus("سررسید امروز", Color(0xFFE69A35))
        days <= 3 -> DebtStatus("${days.toPersianText()} روز مانده", Color(0xFFE69A35))
        else -> DebtStatus("جاری", BrandBlue) }
}

@Composable
private fun DebtCard(item: DebtItem, onPay: () -> Unit, onEdit: () -> Unit, onToggleSettle: () -> Unit, onDelete: () -> Unit) {
    val c = Vsoft.colors
    val debt = item.debt
    val tint = if (item.isOwe) DebtOweColor else DebtOwedColor
    val progress by animateFloatAsState(item.progress, tween(550), label = "debtProgress")
    val status = debtStatus(item)
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(c.surface)
        .border(1.dp, c.outline.copy(alpha = .5f), RoundedCornerShape(22.dp)).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).clip(RoundedCornerShape(15.dp)).background(tint.copy(alpha = .14f)), contentAlignment = Alignment.Center) {
                Text(debt.personName.trim().take(1).ifBlank { "؟" }, color = tint, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(debt.personName, color = c.textPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("ثبت: ${com.vsoft.app.ui.components.form.FormFormat.date(debt.dateMillis)}", color = c.textSecondary, fontSize = 11.sp)
            }
            Box(Modifier.clip(RoundedCornerShape(10.dp)).background(status.color.copy(alpha = .14f)).padding(horizontal = 9.dp, vertical = 5.dp)) {
                Text(status.label, color = status.color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
        Row(verticalAlignment = Alignment.Bottom) {
            Column(Modifier.weight(1f)) {
                Text(if (item.isSettled) "وضعیت" else "مانده", color = c.textSecondary, fontSize = 11.sp)
                Text(if (item.isSettled) "تسویه کامل" else "${item.remaining.toMoney()} تومان",
                    color = if (item.isSettled) c.textSecondary else tint, fontSize = 19.sp, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("مبلغ کل", color = c.textSecondary, fontSize = 11.sp)
                Text("${debt.totalAmount.toMoney()} تومان", color = c.textPrimary, fontSize = 12.sp)
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(Modifier.fillMaxWidth().height(7.dp).clip(CircleShape).background(tint.copy(alpha = .14f))) {
                Box(Modifier.fillMaxHeight().fillMaxWidth(progress).clip(CircleShape)
                    .background(Brush.horizontalGradient(listOf(tint.copy(alpha = .7f), tint))))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${if (item.isOwe) "پرداخت‌شده" else "دریافت‌شده"}: ${item.paid.toMoney()}", color = c.textSecondary, fontSize = 10.sp)
                Text("${(progress * 100).toInt().toPersianText()}٪", color = c.textSecondary, fontSize = 10.sp)
            }
        }
        if (!item.isSettled) debt.dueDateMillis?.let {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarMonth, null, tint = c.textSecondary, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(5.dp)); Text("سررسید: ${com.vsoft.app.ui.components.form.FormFormat.date(it)}", color = c.textSecondary, fontSize = 11.sp)
            }
        }
        if (debt.note.isNotBlank()) Text(debt.note, Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
            .background(c.surfaceHigh).padding(10.dp), color = c.textSecondary, fontSize = 12.sp)
        HorizontalDivider(color = c.outline.copy(alpha = .35f))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            if (!item.isSettled) TextButton(onClick = onPay) { Text(if (item.isOwe) "ثبت پرداخت" else "ثبت دریافت", color = tint, fontWeight = FontWeight.Bold) }
            TextButton(onClick = onToggleSettle) { Text(if (item.isSettled) "لغو تسویه" else "تسویه کامل") }
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, "ویرایش", tint = c.textSecondary) }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "حذف", tint = c.textSecondary) }
        }
    }
}

@Composable
private fun DebtEmptyState(isOwe: Boolean, filter: DebtFilter) {
    val c = Vsoft.colors
    val tint = if (isOwe) DebtOweColor else DebtOwedColor
    Column(Modifier.fillMaxWidth().padding(vertical = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(if (isOwe) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward, null, tint = tint, modifier = Modifier.size(38.dp))
        Spacer(Modifier.height(12.dp))
        Text(when { filter == DebtFilter.SETTLED -> "موردی تسویه‌شده وجود ندارد"; isOwe -> "هنوز بدهی‌ای ثبت نکرده‌ای"; else -> "هنوز طلبی ثبت نکرده‌ای" },
            color = c.textPrimary, fontWeight = FontWeight.SemiBold)
        Text("با دکمه پایین اولین مورد را اضافه کن", color = c.textSecondary, fontSize = 12.sp)
    }
}

@Composable
private fun DebtPaymentSheet(item: DebtItem, accounts: List<AccountEntity>, onDismiss: () -> Unit, onConfirm: (Long, Long?) -> Unit) {
    val c = Vsoft.colors
    var amount by remember { mutableStateOf("") }
    var accountId by remember { mutableStateOf<Long?>(null) }
    var showAccounts by remember { mutableStateOf(false) }
    val parsed = amount.toLatinDigits().toLongOrNull() ?: 0L
    val valid = parsed in 1..item.remaining
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true), containerColor = c.surface) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 16.dp).navigationBarsPadding().imePadding(),
            verticalArrangement = Arrangement.spacedBy(13.dp)) {
            Text(if (item.isOwe) "ثبت پرداخت به ${item.debt.personName}" else "ثبت دریافت از ${item.debt.personName}",
                color = c.textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("مانده: ${item.remaining.toMoney()} تومان", color = c.textSecondary, fontSize = 13.sp)
            FormAmountField(value = amount, onValueChange = { amount = it }, placeholder = "مبلغ",
                trailing = { Text("تومان", color = c.textSecondary, fontSize = 13.sp) })
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FormChip("کل مانده", parsed == item.remaining && parsed > 0, { amount = item.remaining.toString() })
                FormChip("نصف مانده", parsed == item.remaining / 2 && parsed > 0, { amount = (item.remaining / 2).toString() })
            }
            if (parsed > item.remaining) Text("مبلغ از مانده بیشتر است", color = Color(0xFFE69A35), fontSize = 12.sp)
            Text("حساب: ${accounts.firstOrNull { it.id == accountId }?.name ?: "بدون حساب"}",
                Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(c.surfaceHigh).clickable { showAccounts = true }.padding(13.dp),
                color = c.textPrimary, fontSize = 13.sp)
            FormPrimaryButton("ثبت", valid, false) { onConfirm(parsed, accountId) }
        }
    }
    if (showAccounts) FormOptionSheet(
        title = "انتخاب حساب",
        options = listOf(FormOption(null, "بدون حساب")) + accounts.map { FormOption(it.id, it.name, "حساب بانکی") },
        selectedId = accountId,
        onPick = { accountId = it; showAccounts = false },
        onDismiss = { showAccounts = false }
    )
}
