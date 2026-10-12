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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun Dashboard(
    vm: AppVm,
    go: (Int) -> Unit,
    onAddTx: () -> Unit,
    onAddCard: () -> Unit,
    onOpenTx: (Tx) -> Unit
) {
    val c = LocalVC.current
    var hide by rememberSaveable { mutableStateOf(false) }
    val now = remember { jalali(System.currentTimeMillis()) }
    val total = remember(vm.cards, vm.txs) { allBalances(vm.cards, vm.txs).values.sum() }
    val month = remember(vm.txs) {
        var i = 0L
        var e = 0L
        for (t in vm.txs) {
            val j = jalali(t.time)
            if (j.y == now.y && j.m == now.m) { if (t.income) i += t.amount else e += t.amount }
        }
        i to e
    }
    val inc = month.first
    val exp = month.second
    val recent = remember(vm.txs) { vm.txs.sortedByDescending { it.time }.take(5) }
    val cardNames = remember(vm.cards) { vm.cards.associate { it.id to it.bank } }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconSquare(Icons.Rounded.Person, c.accent, c.accentBg, 44.dp)
                Spacer(Modifier.width(12.dp))
                Column {
                    VText("سلام، خوش آمدی", size = 20, weight = FontWeight.ExtraBold)
                    VText("امیدوارم روز خوبی داشته باشی · ${jalali(System.currentTimeMillis()).text()}", size = 11, color = c.sub, maxLines = 1)
                }
            }
        }
        item {
            val shape = RoundedCornerShape(24.dp)
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .background(Brush.linearGradient(listOf(c.hero1, c.hero2)), shape)
                    .padding(20.dp)
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    VText("موجودی کل", size = 14, color = c.onHero.copy(alpha = 0.85f))
                    Icon(
                        if (hide) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility, null,
                        tint = c.onHero, modifier = Modifier.size(22.dp).clickable { hide = !hide }
                    )
                }
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    VText(if (hide) "••••••" else total.money(), size = 30, weight = FontWeight.ExtraBold, color = c.onHero)
                    Spacer(Modifier.width(6.dp))
                    VText("تومان", size = 13, color = c.onHero.copy(alpha = 0.85f))
                }
                Spacer(Modifier.height(6.dp))
                VText("${vm.cards.size.fa()} کارت بانکی", size = 12, color = c.onHero.copy(alpha = 0.8f))
            }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                QuickTile("تراکنش جدید", Icons.Rounded.Add, Modifier.weight(1f)) { onAddTx() }
                QuickTile("کارت جدید", Icons.Rounded.CreditCard, Modifier.weight(1f)) { onAddCard() }
                QuickTile("گزارش‌ها", Icons.Rounded.BarChart, Modifier.weight(1f)) { go(3) }
                QuickTile("پروژه‌ها", Icons.Rounded.Work, Modifier.weight(1f)) { go(2) }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionTitle("خلاصه وضعیت مالی")
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SummaryTile("درآمد", inc, c.income, c.incomeBg, Icons.Rounded.ArrowDownward, Modifier.weight(1f))
                    SummaryTile("هزینه", exp, c.expense, c.expenseBg, Icons.Rounded.ArrowUpward, Modifier.weight(1f))
                }
                VText("مجموع ${jMonths[now.m - 1]} ماه", size = 11, color = c.sub)
            }
        }
        if (vm.cards.isEmpty()) {
            item {
                EmptyBox("هنوز کارتی ثبت نشده", "برای شروع یک کارت بانکی اضافه کن یا داده نمونه را ببین.") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PrimaryButton("افزودن کارت") { onAddCard() }
                        SecondaryButton("داده نمونه") { vm.loadSample() }
                    }
                }
            }
        } else {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    SectionTitle("آخرین تراکنش‌ها")
                    VText("مشاهده همه", size = 12, weight = FontWeight.Bold, color = c.accent, modifier = Modifier.clickable { go(1) })
                }
            }
            if (recent.isEmpty()) {
                item { EmptyBox("تراکنشی ثبت نشده", "با دکمه «تراکنش جدید» اولین درآمد یا هزینه را ثبت کن.") }
            }
            items(recent, key = { it.id }) { t ->
                TxRow(t, cardNames[t.cardId] ?: "—") { onOpenTx(t) }
            }
        }
    }
}

@Composable
private fun QuickTile(title: String, icon: ImageVector, modifier: Modifier, onClick: () -> Unit) {
    val c = LocalVC.current
    Column(
        modifier.clip(RoundedCornerShape(16.dp)).clickable { onClick() }.padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            Modifier.size(56.dp).vcard(18.dp),
            contentAlignment = Alignment.Center
        ) { Icon(icon, null, tint = c.accent, modifier = Modifier.size(26.dp)) }
        VText(title, size = 11, weight = FontWeight.Medium, maxLines = 1)
    }
}

@Composable
private fun SummaryTile(title: String, amount: Long, tint: Color, bg: Color, icon: ImageVector, modifier: Modifier) {
    val c = LocalVC.current
    val shape = RoundedCornerShape(18.dp)
    Column(
        modifier.clip(shape).background(bg, shape).border(1.dp, tint.copy(alpha = 0.25f), shape).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            VText(title, size = 13, color = c.sub)
        }
        VText(amount.money(), size = 18, weight = FontWeight.ExtraBold, color = tint)
        VText("تومان", size = 11, color = c.sub)
    }
}
