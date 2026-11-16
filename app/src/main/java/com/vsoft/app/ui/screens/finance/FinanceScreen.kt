package com.vsoft.app.ui.screens.finance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vsoft.app.ui.components.*
import com.vsoft.app.ui.theme.ExpenseRed
import com.vsoft.app.ui.theme.IncomeGreen
import com.vsoft.app.ui.theme.Vsoft
import com.vsoft.app.util.toMoney
import com.vsoft.app.util.toPersianDigits
import kotlin.math.abs

private data class RowItem(val title: String, val subtitle: String?, val amount: Long, val color: Color, val logo: @Composable () -> Unit)
private data class TabContent(val items: List<RowItem>, val centerTitle: String, val centerValue: Long)
private fun buildContent(tab: Int, s: FinanceUiState): TabContent = when (tab) {
    0 -> TabContent(listOf(
        RowItem("درآمد", "این ماه", s.income, IncomeGreen) { GradientLogo(IncomeGreen, Icons.Filled.ArrowDownward, 44.dp) },
        RowItem("هزینه", "این ماه", s.expense, ExpenseRed) { GradientLogo(ExpenseRed, Icons.Filled.ArrowUpward, 44.dp) }
    ), "تراز", s.income - s.expense)
    1 -> TabContent(s.incomeByCategory.map { (n, a) -> RowItem(n, null, a, CategoryStyles.of(n).color) { CategoryLogo(n, 44.dp) } }, "جمع درآمد", s.income)
    2 -> TabContent(s.expenseByCategory.map { (n, a) -> RowItem(n, null, a, CategoryStyles.of(n).color) { CategoryLogo(n, 44.dp) } }, "جمع هزینه", s.expense)
    else -> TabContent(s.accounts.map { a -> RowItem(a.name, "•••• " + a.last4.toPersianDigits(), s.accountBalances[a.id] ?: a.balance, accountSwatch(a.colorKey)) { AccountLogo(a.colorKey, 44.dp) } },
        "کل موجودی", s.accounts.sumOf { s.accountBalances[it.id] ?: it.balance })
}
@Composable
private fun FinanceRow(item: RowItem, percent: Int, fraction: Float, showDivider: Boolean) {
    val c = Vsoft.colors
    Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        item.logo(); Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(item.title, color = c.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1)
            item.subtitle?.let { Text(it, color = c.textSecondary, fontSize = 11.sp) }
            Spacer(Modifier.height(6.dp))
            Box(Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)).background(c.outline.copy(alpha = .35f))) {
                Box(Modifier.fillMaxWidth(fraction.coerceIn(0f, 1f)).height(4.dp).clip(RoundedCornerShape(2.dp)).background(item.color))
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(item.amount.toMoney(), color = c.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(percent.toString().toPersianDigits() + "٪", color = c.textSecondary, fontSize = 11.sp)
        }
    }
    if (showDivider) Box(Modifier.fillMaxWidth().padding(start = 56.dp).height(1.dp).background(c.outline.copy(alpha = .5f)))
}
@Composable
fun FinanceScreen(onOpenTransactions: () -> Unit = {}, onOpenAccounts: () -> Unit = {}, onOpenTransactionForm: (String) -> Unit = {},
    onOpenDebts: () -> Unit = {}, onOpenInvestments: () -> Unit = {}, viewModel: FinanceViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val content = buildContent(tab, state); val total = content.items.sumOf { abs(it.amount) }
    Column(Modifier.fillMaxSize().background(Vsoft.colors.bg).verticalScroll(rememberScrollState())
        .padding(horizontal = 16.dp).padding(top = 16.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("مدیریت مالی", color = Vsoft.colors.textPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        FinanceShortcuts(onOpenTransactions, onOpenAccounts, onOpenTransactionForm, onOpenDebts, onOpenInvestments)
        SegmentedTabs(listOf("کلیات", "درآمد", "هزینه", "حساب‌ها"), tab) { tab = it }
        VCard { Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            DonutChart(content.items.map { ChartSlice(abs(it.amount).toFloat(), it.color) }, content.centerTitle, content.centerValue.toMoney())
        } }
        VCard {
            if (content.items.isEmpty()) Text("هنوز داده‌ای ثبت نشده است", color = Vsoft.colors.textSecondary, fontSize = 13.sp)
            else content.items.forEachIndexed { index, item ->
                val share = if (total > 0) abs(item.amount).toFloat() / total.toFloat() else 0f
                val percent = if (total > 0) ((abs(item.amount) * 100) / total).toInt() else 0
                FinanceRow(item, percent, share, index < content.items.lastIndex)
            }
        }
    }
}
