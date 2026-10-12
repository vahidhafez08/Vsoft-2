package com.vahidnia.finance

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun AddTxDialog(vm: AppVm, onDismiss: () -> Unit, onNeedCard: () -> Unit, startIncome: Boolean = false) {
    val c = LocalVC.current
    if (vm.cards.isEmpty()) {
        AppDialog(
            title = "ابتدا یک کارت اضافه کن",
            onDismiss = onDismiss,
            confirmText = "افزودن کارت",
            dismissText = "بستن",
            onConfirm = { onDismiss(); onNeedCard() }
        ) { VText("برای ثبت تراکنش باید حداقل یک کارت بانکی داشته باشی.", size = 14, color = c.sub) }
        return
    }
    var income by remember { mutableStateOf(startIncome) }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(if (startIncome) incomeCats[0] else expenseCats[0]) }
    var cardId by remember { mutableStateOf(vm.cards[0].id) }
    var note by remember { mutableStateOf("") }
    val cats = if (income) incomeCats else expenseCats

    AppDialog(
        title = "ثبت تراکنش",
        onDismiss = onDismiss,
        confirmText = "ثبت تراکنش",
        dismissText = "انصراف",
        onConfirm = {
            val a = parseAmount(amount)
            if (a > 0L) {
                vm.addTx(income, cardId, a, category, note.trim())
                onDismiss()
            }
        }
    ) {
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(50)).background(c.surfaceVar).padding(4.dp)
        ) {
            TypeSeg("هزینه", !income, c.expense, Color.White, Modifier.weight(1f)) { income = false; category = expenseCats[0] }
            TypeSeg("درآمد", income, c.income, c.onAccent, Modifier.weight(1f)) { income = true; category = incomeCats[0] }
        }
        Field(amount, { amount = it }, "مبلغ (تومان)", number = true)
        if (parseAmount(amount) > 0L) VText(parseAmount(amount).money() + " تومان", size = 12, color = c.accent)
        VText("دسته‌بندی", size = 13, color = c.sub)
        ChipRow { for (k in cats) Chip(k, category == k) { category = k } }
        VText("کارت", size = 13, color = c.sub)
        ChipRow { for (card in vm.cards) Chip(card.bank, cardId == card.id) { cardId = card.id } }
        Field(note, { note = it }, "توضیحات (اختیاری)", singleLine = false)
        VText("تاریخ و زمان: ${jalali(System.currentTimeMillis()).text()} · ${timeText(System.currentTimeMillis())}", size = 12, color = c.sub)
    }
}

@Composable
fun AddCardDialog(vm: AppVm, onDismiss: () -> Unit) {
    val c = LocalVC.current
    var bank by remember { mutableStateOf(bankNames[0]) }
    var custom by remember { mutableStateOf("") }
    var last4 by remember { mutableStateOf("") }
    var initial by remember { mutableStateOf("") }

    AppDialog(
        title = "کارت جدید",
        onDismiss = onDismiss,
        confirmText = "افزودن",
        dismissText = "انصراف",
        onConfirm = {
            val name = if (bank == "سایر") custom.trim() else bank
            if (name.isNotEmpty()) {
                vm.addCard(name, last4.filter { it.isDigit() || it in '۰'..'۹' }.takeLast(4), parseAmount(initial))
                onDismiss()
            }
        }
    ) {
        VText("بانک", size = 13, color = c.sub)
        ChipRow { for (b in bankNames) Chip(b, bank == b) { bank = b } }
        if (bank == "سایر") Field(custom, { custom = it }, "نام بانک")
        Field(last4, { last4 = it }, "۴ رقم آخر کارت", number = true)
        Field(initial, { initial = it }, "موجودی اولیه (تومان)", number = true)
        VText("موجودی اولیه بعداً با تراکنش‌ها تغییر نمی‌کند؛ فقط موجودی فعلی محاسبه می‌شود.", size = 11, color = c.sub)
    }
}

@Composable
fun TxDetailDialog(vm: AppVm, tx: Tx, onDismiss: () -> Unit) {
    val c = LocalVC.current
    var confirm by remember { mutableStateOf(false) }
    val tint = if (tx.income) c.income else c.expense
    val cardName = vm.cards.firstOrNull { it.id == tx.cardId }?.bank ?: "—"

    if (confirm) {
        AppDialog(
            title = "حذف تراکنش؟",
            onDismiss = { confirm = false },
            confirmText = "حذف",
            danger = true,
            dismissText = "انصراف",
            onConfirm = { vm.deleteTx(tx.id); confirm = false; onDismiss() }
        ) { VText("با حذف تراکنش، موجودی کارت دوباره محاسبه می‌شود.", size = 14, color = c.sub) }
        return
    }

    AppDialog(
        title = "رسید تراکنش",
        onDismiss = onDismiss,
        confirmText = "حذف",
        danger = true,
        dismissText = "بستن",
        onConfirm = { confirm = true }
    ) {
        Column(
            Modifier.fillMaxWidth().border(1.dp, c.border, RoundedCornerShape(16.dp)).padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            VText(tx.amount.money() + " تومان", size = 22, weight = FontWeight.ExtraBold, color = tint)
            ReceiptRow("نوع", if (tx.income) "درآمد" else "هزینه")
            ReceiptRow("دسته‌بندی", tx.category)
            ReceiptRow("کارت", cardName)
            ReceiptRow("تاریخ", jalali(tx.time).text())
            ReceiptRow("ساعت", timeText(tx.time))
            if (tx.note.isNotBlank()) ReceiptRow("توضیحات", tx.note)
            VText("VSOFT", size = 11, weight = FontWeight.Bold, color = c.accent)
        }
    }
}

@Composable
private fun ReceiptRow(label: String, value: String) {
    val c = LocalVC.current
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        VText(label, size = 13, color = c.sub)
        VText(value, size = 13, weight = FontWeight.Medium)
    }
}

@Composable
private fun TypeSeg(text: String, selected: Boolean, fill: Color, onFill: Color, modifier: Modifier, onClick: () -> Unit) {
    val c = LocalVC.current
    Box(
        modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) fill else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        VText(text, size = 14, weight = FontWeight.Bold, color = if (selected) onFill else c.sub)
    }
}
