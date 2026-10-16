package com.vsoft.app.ui.screens.finance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vsoft.app.ui.components.CategoryStyles
import com.vsoft.app.ui.components.FinanceShortcuts
import com.vsoft.app.ui.components.ChartSlice
import com.vsoft.app.ui.components.DonutChart
import com.vsoft.app.ui.components.SegmentedTabs
import com.vsoft.app.ui.components.VCard
import com.vsoft.app.ui.theme.BrandBlue
import com.vsoft.app.ui.theme.BrandPurple
import com.vsoft.app.ui.theme.ExpenseRed
import com.vsoft.app.ui.theme.IncomeGreen
import com.vsoft.app.ui.theme.Vsoft
import com.vsoft.app.ui.theme.WarningOrange
import com.vsoft.app.util.toMoney
import com.vsoft.app.util.toPersianDigits
import kotlin.math.abs

private data class RowItem(
    val title: String,
    val subtitle: String?,
    val amount: Long,
    val color: Color,
    val icon: ImageVector
)

private data class TabContent(
    val items: List<RowItem>,
    val centerTitle: String,
    val centerValue: Long
)

private val accountPalette = listOf(BrandBlue, Color(0xFF14B8A6), IncomeGreen, BrandPurple, WarningOrange)

private fun buildContent(tab: Int, s: FinanceUiState): TabContent = when (tab) {
    0 -> TabContent(
        items = listOf(
            RowItem("درآمد", "این ماه", s.income, IncomeGreen, Icons.Filled.ArrowDownward),
            RowItem("هزینه", "این ماه", s.expense, ExpenseRed, Icons.Filled.ArrowUpward)
        ),
        centerTitle = "تراز",
        centerValue = s.income - s.expense
    )
    1 -> TabContent(
        items = s.incomeByCategory.map { (name, amount) ->
            val st = CategoryStyles.of(name)
            RowItem(name, null, amount, st.color, st.icon)
        },
        centerTitle = "جمع درآمد",
        centerValue = s.income
    )
    2 -> TabContent(
        items = s.expenseByCategory.map { (name, amount) ->
            val st = CategoryStyles.of(name)
            RowItem(name, null, amount, st.color, st.icon)
        },
        centerTitle = "جمع هزینه",
        centerValue = s.expense
    )
    else -> TabContent(
        items = s.accounts.mapIndexed { i, a ->
            RowItem(
                a.name,
                "•••• " + a.last4.toPersianDigits(),
                a.balance,
                accountPalette[i % accountPalette.size],
                Icons.Filled.AccountBalance
            )
        },
        centerTitle = "کل موجودی",
        centerValue = s.accounts.sumOf { it.balance }
    )
}

@Composable
fun FinanceScreen(
    onOpenTransactions: () -> Unit = {},
    onOpenAccounts: () -> Unit = {},
    viewModel: FinanceViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableIntStateOf(0) }

    val content = buildContent(tab, state)
    val total = content.items.sumOf { abs(it.amount) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Vsoft.colors.bg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            "مدیریت مالی",
            color = Vsoft.colors.textPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        FinanceShortcuts(onOpenTransactions = onOpenTransactions, onOpenAccounts = onOpenAccounts)

        SegmentedTabs(
            items = listOf("کلیات", "درآمد", "هزینه", "حساب‌ها"),
            selected = tab,
            onSelect = { tab = it }
        )

        VCard {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                DonutChart(
                    slices = content.items.map { ChartSlice(abs(it.amount).toFloat(), it.color) },
                    centerTitle = content.centerTitle,
                    centerValue = content.centerValue.toMoney()
                )
            }
        }

        VCard {
            if (content.items.isEmpty()) {
                Text(
                    "هنوز داده‌ای ثبت نشده است",
                    color = Vsoft.colors.textSecondary,
                    fontSize = 13.sp
                )
            } else {
                content.items.forEachIndexed { index, item ->
                    if (index > 0) {
                        Box(Modifier.fillMaxWidth().height(1.dp).background(Vsoft.colors.outline))
                    }
                    val percent = if (total > 0) ((abs(item.amount) * 100) / total).toInt() else 0
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(item.color.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(item.icon, null, tint = item.color, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(item.title, color = Vsoft.colors.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            item.subtitle?.let {
                                Text(it, color = Vsoft.colors.textSecondary, fontSize = 11.sp)
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                item.amount.toMoney(),
                                color = Vsoft.colors.textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                percent.toString().toPersianDigits() + "٪",
                                color = Vsoft.colors.textSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}