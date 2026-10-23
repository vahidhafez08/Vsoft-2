package com.vsoft.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SwapHoriz
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vsoft.app.ui.components.AddTransactionSheet
import com.vsoft.app.ui.screens.more.SettingsViewModel
import com.vsoft.app.ui.components.AnimatedMoney
import com.vsoft.app.ui.components.bounceClick
import com.vsoft.app.ui.components.LineChart
import com.vsoft.app.ui.components.VCard
import com.vsoft.app.ui.theme.BrandBlue
import com.vsoft.app.ui.theme.BrandPurple
import com.vsoft.app.ui.theme.ExpenseRed
import com.vsoft.app.ui.theme.IncomeGreen
import com.vsoft.app.ui.theme.Vsoft
import com.vsoft.app.util.toMoney
import com.vsoft.app.util.toPersianDigits
import kotlin.math.abs

@Composable
fun HomeScreen(
    onOpenTransactions: () -> Unit = {},
    onOpenAccounts: () -> Unit = {},
    onOpenDocuments: () -> Unit = {},
    onOpenTransactionForm: (String) -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val userName by settingsViewModel.userName.collectAsStateWithLifecycle()
    val isDark = Vsoft.colors.isDark

    val balanceBrush = if (isDark)
        Brush.linearGradient(listOf(Color(0xFF14532D), Color(0xFF15803D)))
    else
        Brush.linearGradient(listOf(Color(0xFFD1FAE5), Color(0xFFA7F3D0)))
    val balanceContent = if (isDark) Color.White else Color(0xFF064E3B)

    val incomeBrush = if (isDark)
        Brush.linearGradient(listOf(Color(0xFF1E3A8A), Color(0xFF1D4ED8)))
    else
        Brush.linearGradient(listOf(Color(0xFFDBEAFE), Color(0xFFBFDBFE)))
    val incomeContent = if (isDark) Color.White else Color(0xFF1E3A8A)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Vsoft.colors.bg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(BrandBlue, BrandPurple))),
                contentAlignment = Alignment.Center
            ) {
                Text(userName.trim().firstOrNull()?.toString() ?: "؟", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("سلام $userName", color = Vsoft.colors.textPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                Text("امروز یک قدم به اهداف نزدیک‌تر هستی", color = Vsoft.colors.textSecondary, fontSize = 11.sp)
            }
            Icon(Icons.Filled.Notifications, contentDescription = "اعلان‌ها", tint = Vsoft.colors.textSecondary)
        }

        // Balance + income
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("موجودی کل", state.totalBalance, null, balanceBrush, balanceContent, Modifier.weight(1f))
            StatCard("درآمد (این ماه)", state.income, state.incomeChange, incomeBrush, incomeContent, Modifier.weight(1f))
        }

        // Today's expense
        VCard(Modifier.bounceClick(onOpenTransactions)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(ExpenseRed.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Payments, null, tint = ExpenseRed)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("هزینه امروز", color = Vsoft.colors.textSecondary, fontSize = 12.sp)
                    Text(
                        "${state.todayExpense.toMoney()} تومان",
                        color = Vsoft.colors.textPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                state.expenseChange?.let { change ->
                    Text(
                        (if (change >= 0) "▲ " else "▼ ") + abs(change).toString().toPersianDigits() + "٪",
                        color = ExpenseRed,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Chart
        VCard {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "نمای کلی مالی (۷ روز اخیر)",
                    color = Vsoft.colors.textPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                LegendDot(IncomeGreen, "درآمد")
                Spacer(Modifier.width(10.dp))
                LegendDot(ExpenseRed, "هزینه")
            }
            Spacer(Modifier.height(12.dp))
            LineChart(state.weekIncome, state.weekExpense, state.weekLabels)
        }

        // Quick actions
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickAction("ثبت هزینه", Icons.Filled.Remove, ExpenseRed, Modifier.weight(1f)) { onOpenTransactionForm("expense") }
            QuickAction("ثبت درآمد", Icons.Filled.Add, IncomeGreen, Modifier.weight(1f)) { onOpenTransactionForm("income") }
            QuickAction("انتقال وجه", Icons.Filled.SwapHoriz, BrandBlue, Modifier.weight(1f)) {
                onOpenAccounts()
            }
            QuickAction("اسناد", Icons.Filled.Description, BrandPurple, Modifier.weight(1f), onClick = onOpenDocuments)
        }
    }

}

@Composable
private fun StatCard(
    title: String,
    amount: Long,
    change: Int?,
    brush: Brush,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(brush)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(title, color = contentColor.copy(alpha = 0.85f), fontSize = 11.sp, maxLines = 1)
        AnimatedMoney(amount, color = contentColor, fontSize = 18.sp)
        Text("تومان", color = contentColor.copy(alpha = 0.7f), fontSize = 11.sp)
        if (change != null) {
            Text(
                (if (change >= 0) "▲ " else "▼ ") + abs(change).toString().toPersianDigits() + "٪",
                color = contentColor,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(4.dp))
        Text(label, color = Vsoft.colors.textSecondary, fontSize = 11.sp)
    }
}

@Composable
private fun QuickAction(
    label: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier.clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick).padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            Modifier.size(56.dp).clip(RoundedCornerShape(18.dp)).background(tint.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = tint)
        }
        Text(label, color = Vsoft.colors.textSecondary, fontSize = 11.sp, maxLines = 1)
    }
}