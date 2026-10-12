package com.vahidnia.finance

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
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
fun ReportsScreen(vm: AppVm) {
    val c = LocalVC.current
    val nowIdx = jalali(System.currentTimeMillis()).idx
    var period by remember { mutableStateOf(0) } // 0 ماهانه، 1 سه‌ماهه، 2 سالانه
    var idx by remember { mutableStateOf(nowIdx) }
    var cardId by remember { mutableStateOf<String?>(null) }

    val selected = vm.cards.firstOrNull { it.id == cardId }
    val sy = idx / 12
    val sm = idx % 12 + 1
    val start: Int
    val end: Int
    when (period) {
        0 -> { start = idx; end = idx }
        1 -> { start = idx - 2; end = idx }
        else -> { start = sy * 12; end = sy * 12 + 11 }
    }
    val title = when (period) {
        0 -> "${jMonths[sm - 1]} ${sy.fa()}"
        1 -> "${jMonths[(start % 12 + 12) % 12]} تا ${jMonths[sm - 1]} ${sy.fa()}"
        else -> "سال ${sy.fa()}"
    }

    val list = vm.txs.filter {
        val i = jalali(it.time).idx
        i in start..end && (selected == null || it.cardId == selected.id)
    }
    val inc = list.filter { it.income }.sumOf { it.amount }
    val exp = list.filter { !it.income }.sumOf { it.amount }

    val groups: List<Pair<Long, Long>>
    val labels: List<String>
    if (period == 0) {
        val n = if (sm <= 6) 31 else 30
        val ai = LongArray(n)
        val ae = LongArray(n)
        for (t in list) {
            val d = jalali(t.time).d.coerceIn(1, n) - 1
            if (t.income) ai[d] += t.amount else ae[d] += t.amount
        }
        groups = List(n) { ai[it] to ae[it] }
        labels = List(n) { (it + 1).fa() }
    } else {
        val n = end - start + 1
        val ai = LongArray(n)
        val ae = LongArray(n)
        for (t in list) {
            val k = jalali(t.time).idx - start
            if (k in 0 until n) { if (t.income) ai[k] += t.amount else ae[k] += t.amount }
        }
        groups = List(n) { ai[it] to ae[it] }
        labels = List(n) { jMonths[((start + it) % 12 + 12) % 12].take(3) }
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { ScreenTitle("گزارش‌ها") }
        item {
            ChipRow {
                Chip("ماهانه", period == 0) { period = 0 }
                Chip("سه‌ماهه", period == 1) { period = 1 }
                Chip("سالانه", period == 2) { period = 2 }
            }
        }
        item {
            ChipRow {
                Chip("همه کارت‌ها", selected == null) { cardId = null }
                for (card in vm.cards) Chip(card.bank, selected?.id == card.id) { cardId = card.id }
            }
        }
        item {
            Row(Modifier.fillMaxWidth().vcard().padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Icon(Icons.Rounded.ChevronRight, null, tint = c.text, modifier = Modifier.size(28.dp).clickable { idx -= if (period == 2) 12 else 1 })
                VText(title, size = 15, weight = FontWeight.Bold)
                Icon(Icons.Rounded.ChevronLeft, null, tint = if (idx >= nowIdx) c.border else c.text, modifier = Modifier.size(28.dp).clickable { if (idx < nowIdx) idx += if (period == 2) 12 else 1 })
            }
        }
        item {
            Column(Modifier.fillMaxWidth().vcard().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionTitle(if (period == 0) "گزارش ماهانه" else "مقایسه درآمد و هزینه")
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.weight(1f)) { Sum("درآمد", inc, c.income) }
                    Box(Modifier.weight(1f)) { Sum("هزینه", exp, c.expense) }
                }
                Sum("مانده دوره", inc - exp, if (inc - exp >= 0) c.income else c.expense)
            }
        }
        item {
            Column(Modifier.fillMaxWidth().vcard().padding(16.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Legend("درآمد", c.income)
                    Legend("هزینه", c.expense)
                }
                Spacer(Modifier.size(10.dp))
                if (list.isEmpty()) VText("در این بازه تراکنشی وجود ندارد.", size = 13, color = c.sub)
                else BarChart(groups, labels)
            }
        }
        if (selected == null && vm.cards.isNotEmpty()) {
            item {
                val total = vm.cards.sumOf { cardBalance(it, vm.txs) }
                Column(Modifier.fillMaxWidth().vcard().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionTitle("بررسی کارت‌ها")
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Donut(
                            vm.cards.map { (cardBalance(it, vm.txs).coerceAtLeast(0L)).toFloat() to bankColors(it.bank)[0] }
                        ) { VText("همه", size = 12, color = c.sub) }
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            for (card in vm.cards) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(10.dp).clip(CircleShape).background(bankColors(card.bank)[0]))
                                    Spacer(Modifier.width(8.dp))
                                    VText(card.bank, size = 12, modifier = Modifier.weight(1f), maxLines = 1)
                                    VText(cardBalance(card, vm.txs).money(), size = 12, color = c.sub)
                                }
                            }
                        }
                    }
                    VText("جمع کل: ${total.money()} تومان", size = 13, weight = FontWeight.Bold, color = c.accent)
                }
            }
        }
    }
}

@Composable
private fun Sum(label: String, amount: Long, tint: androidx.compose.ui.graphics.Color) {
    val c = LocalVC.current
    Column(Modifier.fillMaxWidth()) {
        VText(label, size = 12, color = c.sub)
        Row(verticalAlignment = Alignment.Bottom) {
            VText(amount.money(), size = 18, weight = FontWeight.ExtraBold, color = tint)
            Spacer(Modifier.width(4.dp))
            VText("تومان", size = 11, color = c.sub)
        }
    }
}

@Composable
private fun Legend(text: String, color: androidx.compose.ui.graphics.Color) {
    val c = LocalVC.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(6.dp))
        VText(text, size = 12, color = c.sub)
    }
}
