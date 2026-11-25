package com.vsoft.app.ui.screens.finance

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.vsoft.app.data.model.InvestmentEntity
import com.vsoft.app.domain.InvestmentCalculator
import com.vsoft.app.ui.theme.Vsoft
import com.vsoft.app.util.toGroupedNumber
import com.vsoft.app.util.toMoney
import com.vsoft.app.util.toPersianDigits
import java.math.RoundingMode
import kotlinx.coroutines.delay

@Composable
fun InvestmentsScreen(
    onBack: () -> Unit,
    onAddInvestment: () -> Unit,
    onOpenInvestment: (Long) -> Unit,
    vm: InvestmentsViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val marketState by vm.marketState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    var selectedType by remember { mutableStateOf<Long?>(null) }
    var deleteTarget by remember { mutableStateOf<InvestmentEntity?>(null) }

    LaunchedEffect(vm, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) {
                vm.refreshMarketPrices()
                delay(30 * 60 * 1000L)
            }
        }
    }

    val activeItems = state.items.filter { it.status == "ACTIVE" }
    val invested = activeItems.sumOf { it.investedAmount }
    val currentValue = activeItems.sumOf { item ->
        val price = item.currentPrice ?: item.buyPrice
        (price * item.quantity).takeIf { it.isFinite() && it <= Long.MAX_VALUE.toDouble() }?.toLong() ?: 0L
    }
    val difference = currentValue - invested
    val visibleItems = state.items.filter { selectedType == null || it.typeId == selectedType }

    Column(Modifier.fillMaxSize().background(Vsoft.colors.bg).padding(horizontal = 16.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "بازگشت", tint = Vsoft.colors.textPrimary) }
            Column(Modifier.weight(1f)) {
                Text("سرمایه‌گذاری", color = Vsoft.colors.textPrimary, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                Text("سبد دارایی‌های شما", color = Vsoft.colors.textSecondary, fontSize = 12.sp)
            }
            IconButton(onClick = { vm.refreshMarketPrices() }, enabled = !marketState.isLoading) {
                if (marketState.isLoading) CircularProgressIndicator(Modifier.size(19.dp), strokeWidth = 2.dp)
                else Icon(Icons.Default.Refresh, "به‌روزرسانی قیمت‌ها", tint = Vsoft.colors.textSecondary)
            }
        }

        Card(
            Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Vsoft.colors.surface)
        ) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("ارزش کل دارایی‌های فعال", color = Vsoft.colors.textSecondary, fontSize = 12.sp)
                Text("${currentValue.toMoney()} تومان", color = Vsoft.colors.textPrimary, fontSize = 25.sp, fontWeight = FontWeight.Bold)
                HorizontalDivider(color = Vsoft.colors.outline.copy(alpha = .35f))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("مبلغ سرمایه‌گذاری", color = Vsoft.colors.textSecondary, fontSize = 11.sp)
                        Text("${invested.toMoney()} تومان", color = Vsoft.colors.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("سود و زیان کل", color = Vsoft.colors.textSecondary, fontSize = 11.sp)
                        Text(
                            (if (difference > 0) "+" else "") + difference.toMoney() + " تومان",
                            color = if (difference >= 0) Color(0xFF239B72) else Color(0xFFE16B63),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(18.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("دارایی‌های من", Modifier.weight(1f), color = Vsoft.colors.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
            Text("${visibleItems.size.toString().toPersianDigits()} مورد", color = Vsoft.colors.textSecondary, fontSize = 11.sp)
        }
        Spacer(Modifier.height(10.dp))

        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            InvestmentTypePill(null, "همه", selectedType == null) { selectedType = null }
            state.types.forEach { type ->
                InvestmentTypePill(type, type.name, selectedType == type.id) { selectedType = type.id }
            }
        }

        LazyColumn(
            Modifier.weight(1f),
            contentPadding = PaddingValues(top = 12.dp, bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (visibleItems.isEmpty()) {
                item {
                    Column(Modifier.fillMaxWidth().padding(vertical = 34.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("هنوز دارایی‌ای ثبت نکرده‌اید", color = Vsoft.colors.textPrimary, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(5.dp))
                        Text("با افزودن اولین سرمایه، سبد خود را بسازید", color = Vsoft.colors.textSecondary, fontSize = 12.sp)
                    }
                }
            }
            items(visibleItems, key = { it.id }) { item ->
                val type = state.types.firstOrNull { it.id == item.typeId }
                val result = InvestmentCalculator.compute(item)
                val tint = type?.let { parseTypeColor(it.colorHex) } ?: Vsoft.colors.textSecondary
                val profit = result.profitLoss
                val profitColor = when {
                    profit == null -> Vsoft.colors.textSecondary
                    profit.signum() >= 0 -> Color(0xFF239B72)
                    else -> Color(0xFFE16B63)
                }
                val shape = RoundedCornerShape(20.dp)
                Column(
                    Modifier.fillMaxWidth()
                        .clip(shape)
                        .background(Vsoft.colors.surface)
                        .border(1.dp, Vsoft.colors.outline.copy(alpha = .35f), shape)
                        .clickable { onOpenInvestment(item.id) }
                        .padding(15.dp),
                    verticalArrangement = Arrangement.spacedBy(13.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        InvestmentTypeLogo(
                            investmentIconKeyForMarketSymbol(item.marketSymbol, type?.iconKey ?: ""),
                            type?.colorHex ?: "#5C8FE8",
                            48.dp
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(item.title.ifBlank { type?.name ?: "سرمایه" }, color = Vsoft.colors.textPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                            Text(
                                "${item.quantity.toGroupedNumber()} ${item.marketUnit?.let(::marketUnitLabel) ?: type?.unitLabel.orEmpty()}",
                                color = Vsoft.colors.textSecondary,
                                fontSize = 11.sp
                            )
                        }
                        if (item.status == "SOLD") {
                            Text("فروخته‌شده", color = Vsoft.colors.textSecondary, fontSize = 10.sp)
                        }
                        IconButton(onClick = { onOpenInvestment(item.id) }, modifier = Modifier.size(34.dp)) {
                            Icon(Icons.Default.Edit, "ویرایش سرمایه", tint = Vsoft.colors.textSecondary, modifier = Modifier.size(18.dp))
                        }
                        if (item.status != "SOLD") {
                            IconButton(onClick = { deleteTarget = item }, modifier = Modifier.size(34.dp)) {
                                Icon(Icons.Default.Delete, "حذف سرمایه", tint = Vsoft.colors.textSecondary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f)) {
                            Text("قیمت هر واحد", color = Vsoft.colors.textSecondary, fontSize = 10.sp)
                            Text("${item.currentPrice?.toLong()?.toMoney() ?: "—"} تومان", color = Vsoft.colors.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                            Text("ارزش کل دارایی", color = Vsoft.colors.textSecondary, fontSize = 10.sp)
                            Text(
                                result.currentValue?.setScale(0, RoundingMode.HALF_UP)?.toLong()?.toMoney()?.plus(" تومان") ?: "قیمت نامشخص",
                                color = Vsoft.colors.textPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    HorizontalDivider(color = Vsoft.colors.outline.copy(alpha = .3f))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("سود / زیان", color = Vsoft.colors.textSecondary, fontSize = 10.sp)
                            Text(
                                profit?.let { (if (it.signum() > 0) "+" else "") + it.setScale(0, RoundingMode.HALF_UP).toLong().toMoney() + " تومان" } ?: "—",
                                color = profitColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        result.profitLossPercent?.let { percent ->
                            Text(
                                (if (percent.signum() > 0) "+" else "") + percent.setScale(2, RoundingMode.HALF_UP).toPlainString().toPersianDigits() + "٪",
                                color = profitColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Button(
            onClick = onAddInvestment,
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Add, null)
            Spacer(Modifier.width(7.dp))
            Text("افزودن سرمایه")
        }
    }

    deleteTarget?.let { item ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("حذف سرمایه") },
            text = { Text("این دارایی از سبد سرمایه‌گذاری حذف شود؟") },
            confirmButton = {
                TextButton(onClick = { vm.delete(item); deleteTarget = null }) { Text("حذف") }
            },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("انصراف") } }
        )
    }
}
