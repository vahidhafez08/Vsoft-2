package com.vsoft.app.ui.screens.finance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vsoft.app.data.model.InvestmentEntity
import com.vsoft.app.domain.InvestmentCalculator
import com.vsoft.app.ui.theme.Vsoft
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import com.vsoft.app.ui.components.form.FormNumberField
import com.vsoft.app.util.toGroupedNumber
import com.vsoft.app.util.toLatinDigits
import com.vsoft.app.util.toMoney
import com.vsoft.app.util.toPersianDigits
import com.vsoft.app.util.toPlainNumber
import java.math.RoundingMode

@Composable
fun InvestmentsScreen(onBack: () -> Unit, onAddInvestment: () -> Unit, vm: InvestmentsViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val marketState by vm.marketState.collectAsStateWithLifecycle()
    var selectedType by remember { mutableStateOf<Long?>(null) }
    var priceTarget by remember { mutableStateOf<InvestmentEntity?>(null) }
    var priceInput by remember { mutableStateOf("") }
    var deleteTarget by remember { mutableStateOf<InvestmentEntity?>(null) }
    val visibleItems = state.items.filter { selectedType == null || it.typeId == selectedType }
    val activeItems = state.items.filter { it.status == "ACTIVE" }
    val invested = activeItems.sumOf { it.investedAmount }
    val currentValue = activeItems.sumOf { item ->
        val price = item.currentPrice ?: item.buyPrice
        (price * item.quantity).takeIf { it.isFinite() }?.toLong() ?: 0L
    }

    Column(Modifier.fillMaxSize().background(Vsoft.colors.bg).padding(horizontal = 16.dp)) {
        Row(Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "بازگشت", tint = Vsoft.colors.textPrimary) }
            Column(Modifier.weight(1f)) {
                Text("سرمایه‌گذاری", color = Vsoft.colors.textPrimary, fontSize = 21.sp, fontWeight = FontWeight.Bold)
                Text("دارایی‌ها و ارزش روز آن‌ها", color = Vsoft.colors.textSecondary, fontSize = 12.sp)
            }
        }
        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Vsoft.colors.surface)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("ارزش تقریبی دارایی‌های فعال", color = Vsoft.colors.textSecondary, fontSize = 12.sp)
                Text("${currentValue.toMoney()} تومان", color = Vsoft.colors.textPrimary, fontSize = 23.sp, fontWeight = FontWeight.Bold)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("مبلغ خرید", color = Vsoft.colors.textSecondary, fontSize = 11.sp)
                        Text("${invested.toMoney()} تومان", color = Vsoft.colors.textPrimary, fontSize = 13.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("تغییر ارزش", color = Vsoft.colors.textSecondary, fontSize = 11.sp)
                        val difference = currentValue - invested
                        Text((if (difference > 0) "+" else "") + difference.toMoney() + " تومان",
                            color = if (difference >= 0) Color(0xFF279B70) else Color(0xFFE16B63),
                            fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Vsoft.colors.surface)) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("قیمت‌های روز بازار", color = Vsoft.colors.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Text("اطلاعات از Iran Market Data", color = Vsoft.colors.textSecondary, fontSize = 10.sp)
                    }
                    TextButton(
                        onClick = { vm.refreshMarketPrices() },
                        enabled = !marketState.isLoading
                    ) {
                        if (marketState.isLoading) {
                            CircularProgressIndicator(Modifier.size(15.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                        Spacer(Modifier.width(4.dp))
                        Text("به‌روزرسانی", fontSize = 11.sp)
                    }
                }
                val featuredSymbols = setOf(
                    "USD_IRR_FREE", "EUR_IRR_FREE", "GOLD_18K_IRR",
                    "COIN_EMAMI_IRR", "COIN_HALF_IRR", "USDT_IRR"
                )
                val featuredPrices = marketState.prices.filter { it.symbol in featuredSymbols }
                if (featuredPrices.isEmpty()) {
                    Text(
                        marketState.error ?: if (marketState.isLoading) "در حال دریافت قیمت‌ها..." else "قیمت‌ها در دسترس نیستند.",
                        color = Vsoft.colors.textSecondary,
                        fontSize = 12.sp
                    )
                } else {
                    featuredPrices.chunked(2).forEach { rowPrices ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            rowPrices.forEach { price ->
                                Column(
                                    Modifier.weight(1f).clip(RoundedCornerShape(12.dp))
                                        .background(Vsoft.colors.bg).padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(price.name, color = Vsoft.colors.textSecondary, fontSize = 10.sp, maxLines = 1)
                                    Text(
                                        "\${price.price.toLong().toMoney()} تومان",
                                        color = Vsoft.colors.textPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1
                                    )
                                    val change = price.changePercent
                                    Text(
                                        when {
                                            price.stale -> "اطلاعات قدیمی"
                                            change == null -> " "
                                            else -> (if (change > 0) "+" else "") + change.toString().toPersianDigits() + "٪"
                                        },
                                        color = when {
                                            price.stale -> Vsoft.colors.textSecondary
                                            change != null && change < 0 -> Color(0xFFE16B63)
                                            change != null && change > 0 -> Color(0xFF279B70)
                                            else -> Vsoft.colors.textSecondary
                                        },
                                        fontSize = 10.sp
                                    )
                                }
                            }
                            if (rowPrices.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
                if (marketState.updatedAtJalali.isNotBlank()) {
                    Text(
                        "آخرین انتشار: \${marketState.updatedAtJalali}",
                        color = Vsoft.colors.textSecondary,
                        fontSize = 10.sp
                    )
                }
                if (marketState.isCached) {
                    Text("این اطلاعات از حافظهٔ دستگاه نمایش داده شده و ممکن است به‌روز نباشد.", color = Vsoft.colors.textSecondary, fontSize = 10.sp)
                }
                if (marketState.error != null && featuredPrices.isNotEmpty()) {
                    Text(marketState.error.orEmpty(), color = Vsoft.colors.textSecondary, fontSize = 10.sp)
                }
                Text(
                    "منبع: Iran Market Data · \${marketState.source}",
                    color = Vsoft.colors.textSecondary,
                    fontSize = 10.sp
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        Text("دارایی‌ها", color = Vsoft.colors.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            InvestmentTypePill(null, "همه", selectedType == null) { selectedType = null }
            state.types.forEach { type ->
                InvestmentTypePill(type, type.name, selectedType == type.id) { selectedType = type.id }
            }
        }
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(0.dp, 12.dp, 0.dp, 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (visibleItems.isEmpty()) item {
                Text("هنوز سرمایه‌ای ثبت نشده است.", Modifier.fillMaxWidth().padding(vertical = 32.dp), color = Vsoft.colors.textSecondary)
            }
            items(visibleItems, key = { it.id }) { item ->
                val type = state.types.firstOrNull { it.id == item.typeId }
                val result = InvestmentCalculator.compute(item)
                val current = result.currentValue?.setScale(0, RoundingMode.HALF_UP)?.toLong()
                val tint = type?.let { parseTypeColor(it.colorHex) } ?: Vsoft.colors.textSecondary
                val cardShape = RoundedCornerShape(20.dp)
                Column(
                    Modifier.fillMaxWidth().clip(cardShape).background(Vsoft.colors.surface)
                        .border(1.dp, tint.copy(alpha = .22f), cardShape).padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        InvestmentTypeLogo(type?.iconKey ?: "", type?.colorHex ?: "#5C8FE8", 48.dp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text(item.title.ifBlank { type?.name ?: "سرمایه" }, color = Vsoft.colors.textPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(Modifier.clip(RoundedCornerShape(8.dp)).background(tint.copy(alpha = .14f)).padding(horizontal = 8.dp, vertical = 3.dp)) {
                                    Text(type?.name ?: "دارایی", color = tint, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                                Text("${item.quantity.toGroupedNumber()} ${type?.unitLabel.orEmpty()}", color = Vsoft.colors.textSecondary, fontSize = 12.sp)
                            }
                        }
                        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            val pct = result.profitLossPercent
                            if (pct != null) {
                                val up = pct.signum() >= 0
                                val col = if (up) Color(0xFF279B70) else Color(0xFFE16B63)
                                Box(Modifier.clip(RoundedCornerShape(8.dp)).background(col.copy(alpha = .14f)).padding(horizontal = 8.dp, vertical = 3.dp)) {
                                    Text((if (up) "+" else "") + pct.setScale(1, RoundingMode.HALF_UP).toPlainString().toPersianDigits() + "٪", color = col, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            if (item.status == "SOLD") Text("فروخته‌شده", color = Vsoft.colors.textSecondary, fontSize = 11.sp)
                        }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("مبلغ خرید", color = Vsoft.colors.textSecondary, fontSize = 11.sp)
                            Text("${item.investedAmount.toMoney()} تومان", color = Vsoft.colors.textPrimary, fontSize = 13.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("ارزش فعلی", color = Vsoft.colors.textSecondary, fontSize = 11.sp)
                            Text(if (current != null) "${current.toMoney()} تومان" else "قیمت وارد نشده", color = Vsoft.colors.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (item.status == "ACTIVE") TextButton(onClick = { priceTarget = item; priceInput = item.currentPrice?.toPlainNumber().orEmpty() }) { Text("ثبت / ویرایش قیمت") }
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = { deleteTarget = item }) { Icon(Icons.Default.Delete, "حذف", tint = Vsoft.colors.textSecondary) }
                    }
                }
            }
        }
        Button(onClick = onAddInvestment, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
            Icon(Icons.Default.Add, null)
            Text("ثبت سرمایه‌گذاری")
        }
    }
    priceTarget?.let { item ->
        AlertDialog(
            onDismissRequest = { priceTarget = null },
            title = { Text("قیمت دستی ${item.title.ifBlank { "سرمایه" }}") },
            text = { FormNumberField(raw = priceInput, onChange = { priceInput = it }, placeholder = "قیمت فعلی هر واحد", unit = "تومان", maxDigits = 15, groupThousands = true) },
            confirmButton = { TextButton(onClick = { priceInput.toLatinDigits().toDoubleOrNull()?.let { vm.updatePrice(item, it) }; priceTarget = null }) { Text("ذخیره قیمت") } },
            dismissButton = { TextButton(onClick = { priceTarget = null }) { Text("انصراف") } }
        )
    }
    deleteTarget?.let { item ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("حذف سرمایه") },
            text = { Text("این مورد از فهرست سرمایه‌گذاری حذف شود؟") },
            confirmButton = { TextButton(onClick = { vm.delete(item); deleteTarget = null }) { Text("حذف") } },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("انصراف") } }
        )
    }
}
