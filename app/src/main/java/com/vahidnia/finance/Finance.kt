package com.vahidnia.finance

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun FinanceScreen(vm: AppVm, onAddTx: () -> Unit, onAddCard: () -> Unit, onOpenTx: (Tx) -> Unit) {
    val c = LocalVC.current
    var query by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(0) } // 0 همه، 1 درآمد، 2 هزینه
    var cardId by remember { mutableStateOf<String?>(null) }
    var confirmDelete by remember { mutableStateOf(false) }

    // اگر کارت انتخاب‌شده حذف شده باشد به «همه کارت‌ها» برمی‌گردیم
    val selected = vm.cards.firstOrNull { it.id == cardId }
    val q = norm(query.trim())

    val filtered = vm.txs
        .filter { selected == null || it.cardId == selected.id }
        .filter { type == 0 || (type == 1 && it.income) || (type == 2 && !it.income) }
        .filter { q.isEmpty() || norm(it.note).contains(q) || norm(it.category).contains(q) || it.amount.toString().contains(q) }
        .sortedByDescending { it.time }
    val grouped = filtered.groupBy { dayLabel(it.time) }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ScreenTitle("امور مالی") {
                Box(
                    Modifier.size(42.dp).clip(CircleShape).background(c.accent).clickable { onAddTx() },
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Rounded.Add, null, tint = c.onAccent) }
            }
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(vm.cards, key = { it.id }) { card ->
                    BankCardView(card, cardBalance(card, vm.txs), selected?.id == card.id) {
                        cardId = if (selected?.id == card.id) null else card.id
                    }
                }
                item {
                    Box(
                        Modifier.width(120.dp).height(140.dp).vcard(22.dp).clickable { onAddCard() },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            IconBadge(Icons.Rounded.Add, c.accent, 40.dp)
                            VText("کارت جدید", size = 12, color = c.sub)
                        }
                    }
                }
            }
        }
        item {
            ChipRow {
                Chip("همه کارت‌ها", selected == null) { cardId = null }
                for (card in vm.cards) Chip(card.bank, selected?.id == card.id) { cardId = card.id }
            }
        }
        if (selected != null) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    VText("موجودی اولیه: ${selected.initial.money()} تومان", size = 12, color = c.sub)
                    VText("حذف کارت", size = 12, color = c.expense, modifier = Modifier.clickable { confirmDelete = true })
                }
            }
        }
        item { Field(query, { query = it }, "جستجو در تراکنش‌ها...") }
        item {
            ChipRow {
                Chip("همه", type == 0) { type = 0 }
                Chip("درآمد", type == 1) { type = 1 }
                Chip("هزینه", type == 2) { type = 2 }
            }
        }
        if (filtered.isEmpty()) {
            item {
                EmptyBox(
                    if (vm.txs.isEmpty()) "تراکنشی ثبت نشده" else "نتیجه‌ای پیدا نشد",
                    if (vm.txs.isEmpty()) "با دکمه + بالای صفحه اولین تراکنش را ثبت کن." else "فیلتر یا عبارت جستجو را تغییر بده."
                )
            }
        }
        grouped.forEach { (label, list) ->
            item(key = "h_$label") { VText(label, size = 13, weight = FontWeight.Bold, color = c.sub) }
            items(list, key = { it.id }) { t ->
                Column {
                    TxRow(t, vm.cards.firstOrNull { it.id == t.cardId }?.bank ?: "—") { onOpenTx(t) }
                }
            }
        }
        if (filtered.isNotEmpty()) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Receipt, null, tint = c.sub, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    VText("برای دیدن رسید روی هر تراکنش بزن", size = 11, color = c.sub)
                }
            }
        }
    }

    if (confirmDelete && selected != null) {
        AppDialog(
            title = "حذف کارت؟",
            onDismiss = { confirmDelete = false },
            confirmText = "حذف",
            danger = true,
            dismissText = "انصراف",
            onConfirm = { vm.deleteCard(selected.id); cardId = null; confirmDelete = false }
        ) {
            VText("کارت «${selected.bank}» و همه تراکنش‌های آن حذف می‌شود.", size = 14, color = c.sub)
        }
    }
}
