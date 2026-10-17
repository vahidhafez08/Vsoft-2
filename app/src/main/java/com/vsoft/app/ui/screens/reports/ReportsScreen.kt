package com.vsoft.app.ui.screens.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.Switch
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
import com.vsoft.app.ui.components.EmptyState
import com.vsoft.app.ui.components.MiniChip
import com.vsoft.app.ui.components.SegmentedTabs
import com.vsoft.app.ui.components.VCard
import com.vsoft.app.ui.theme.BrandBlue
import com.vsoft.app.ui.theme.BrandPurple
import com.vsoft.app.ui.theme.ExpenseRed
import com.vsoft.app.ui.theme.IncomeGreen
import com.vsoft.app.ui.theme.Vsoft
import com.vsoft.app.ui.theme.Dimens
import com.vsoft.app.ui.theme.WarningOrange
import com.vsoft.app.util.JCal
import com.vsoft.app.util.toMoney
import kotlin.math.abs
import kotlin.math.roundToInt

private val palette = listOf(
    BrandBlue, BrandPurple, IncomeGreen, WarningOrange, ExpenseRed, Color(0xFF14B8A6), Color(0xFFEC4899)
)

private fun signedMoney(v: Long): String = (if (v < 0L) "−" else "") + abs(v).toMoney() + " تومان"

@Composable
fun ReportsScreen(viewModel: ReportsViewModel = hiltViewModel()) {
    val s by viewModel.state.collectAsStateWithLifecycle()
    val includeJobIncome by viewModel.includeJobIncome.collectAsStateWithLifecycle()
    var period by rememberSaveable { mutableIntStateOf(1) }
    var catTab by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(period) { viewModel.setPeriod(period) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Vsoft.colors.bg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Dimens.screenHorizontal)
            .padding(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("گزارش‌ها", color = Vsoft.colors.textPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(s.rangeLabel, color = Vsoft.colors.textSecondary, fontSize = 12.sp)
        }

        SegmentedTabs(items = listOf("هفته", "ماه", "سه‌ماهه", "سال"), selected = period, onSelect = { period = it })

        VCard {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "شامل درآمد کارگاه‌ها",
                    color = Vsoft.colors.textPrimary,
                    fontSize = 13.sp,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = includeJobIncome,
                    onCheckedChange = { viewModel.setIncludeJobs(it) }
                )
            }
        }

        VCard {
            SummaryRow("درآمد", s.income, IncomeGreen, s.prevIncome, goodWhenUp = true)
            Spacer(Modifier.height(10.dp))
            SummaryRow("هزینه", s.expense, ExpenseRed, s.prevExpense, goodWhenUp = false)
            Spacer(Modifier.height(10.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(Vsoft.colors.outline))
            Spacer(Modifier.height(10.dp))
            val net = s.income - s.expense
            SummaryRow("تراز", net, if (net >= 0L) IncomeGreen else ExpenseRed, null, goodWhenUp = true)
            Spacer(Modifier.height(10.dp))
            Text(
                "میانگین هزینه روزانه: " + signedMoney(s.avgDailyExpense) + " | " + JCal.fa(s.count) + " تراکنش",
                color = Vsoft.colors.textSecondary,
                fontSize = 12.sp
            )
            if (s.includeJobIncome && s.jobIncome > 0L) {
                Spacer(Modifier.height(6.dp))
                Text(
                    "درآمد کارگاه: " + s.jobIncome.toMoney() + " تومان",
                    color = IncomeGreen,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        VCard {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "روند درآمد و هزینه",
                    color = Vsoft.colors.textPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Legend(IncomeGreen, "درآمد")
                Spacer(Modifier.width(10.dp))
                Legend(ExpenseRed, "هزینه")
            }
            Spacer(Modifier.height(12.dp))
            if (s.loaded && s.count == 0 && s.jobIncome <= 0L) {
                EmptyState(
                    icon = Icons.Filled.BarChart,
                    title = "تراکنشی در این بازه نیست",
                    subtitle = "با ثبت درآمد و هزینه، نمودار اینجا ساخته می‌شود"
                )
            } else {
                ReportLines(s.labels, s.incomeSeries, s.expenseSeries)
            }
        }

        VCard {
            Text("دسته‌بندی‌ها", color = Vsoft.colors.textPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(10.dp))
            SegmentedTabs(items = listOf("هزینه‌ها", "درآمدها"), selected = catTab, onSelect = { catTab = it })
            Spacer(Modifier.height(14.dp))

            val list = if (catTab == 0) s.expenseCats else s.incomeCats
            val total = list.sumOf { it.amount }
            if (list.isEmpty() || total <= 0L) {
                Text("برای این بازه داده‌ای نیست.", color = Vsoft.colors.textSecondary, fontSize = 12.sp)
            } else {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    ReportDonut(
                        slices = list.mapIndexed { i, c -> c.amount to palette[i % palette.size] },
                        modifier = Modifier.size(170.dp)
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("مجموع", color = Vsoft.colors.textSecondary, fontSize = 11.sp)
                        Text(total.toMoney(), color = Vsoft.colors.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(14.dp))
                list.forEachIndexed { i, c ->
                    key(c.name) {
                        val pct = (c.amount * 100.0 / total).roundToInt().coerceIn(0, 100)
                        Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(10.dp).clip(CircleShape).background(palette[i % palette.size]))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                c.name,
                                color = Vsoft.colors.textPrimary, fontSize = 13.sp,
                                maxLines = 1, modifier = Modifier.weight(1f)
                            )
                            Text(JCal.fa(pct) + "٪", color = Vsoft.colors.textSecondary, fontSize = 12.sp)
                            Spacer(Modifier.width(10.dp))
                            Text(c.amount.toMoney(), color = Vsoft.colors.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Legend(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(4.dp))
        Text(text, color = Vsoft.colors.textSecondary, fontSize = 11.sp)
    }
}

@Composable
private fun SummaryRow(label: String, value: Long, color: Color, prev: Long?, goodWhenUp: Boolean) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, color = Vsoft.colors.textSecondary, fontSize = 12.sp)
            Text(signedMoney(value), color = color, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        }
        if (prev != null && prev > 0L) {
            val rawPct = (value.toDouble() - prev.toDouble()) * 100.0 / prev.toDouble()
            val pct = if (rawPct.isFinite()) rawPct.coerceIn(-9999.0, 9999.0).roundToInt() else 0
            if (pct == 0) {
                MiniChip("بدون تغییر", Vsoft.colors.textSecondary)
            } else {
                val up = pct > 0
                val good = up == goodWhenUp
                MiniChip(
                    text = (if (up) "▲ " else "▼ ") + JCal.fa(abs(pct).coerceAtMost(9999)) + "٪",
                    color = if (good) IncomeGreen else ExpenseRed
                )
            }
        }
    }
}