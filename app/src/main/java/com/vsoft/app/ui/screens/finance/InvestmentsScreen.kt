package com.vsoft.app.ui.screens.finance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
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
import com.vsoft.app.util.toLatinDigits
import com.vsoft.app.util.toMoney
import java.math.RoundingMode

@Composable
fun InvestmentsScreen(onBack: () -> Unit, onAddInvestment: () -> Unit, vm: InvestmentsViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
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
        Spacer(Modifier.height(14.dp))
        Text("دارایی‌ها", color = Vsoft.colors.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            FilterChip(selectedType == null, { selectedType = null }, label = { Text("همه") })
            state.types.take(4).forEach { type ->
                FilterChip(selectedType == type.id, { selectedType = type.id }, label = { Text(type.name) })
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            state.types.drop(4).forEach { type ->
                FilterChip(selectedType == type.id, { selectedType = type.id }, label = { Text(type.name) })
            }
        }
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(vertical = 12.dp, bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (visibleItems.isEmpty()) item {
                Text("هنوز سرمایه‌ای ثبت نشده است.", Modifier.fillMaxWidth().padding(vertical = 32.dp), color = Vsoft.colors.textSecondary)
            }
            items(visibleItems, key = { it.id }) { item ->
                val type = state.types.firstOrNull { it.id == item.typeId }
                val result = InvestmentCalculator.compute(item)
                val current = result.currentValue?.setScale(0, RoundingMode.HALF_UP)?.toPlainString()
                Card(colors = CardDefaults.cardColors(containerColor = Vsoft.colors.surface), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(item.title.ifBlank { type?.name ?: "سرمایه" }, color = Vsoft.colors.textPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                                Text("${type?.name ?: "دارایی"} · ${item.quantity} ${type?.unitLabel.orEmpty()}", color = Vsoft.colors.textSecondary, fontSize = 12.sp)
                            }
                            Text(if (item.status == "SOLD") "فروخته‌شده" else "فعال", color = Vsoft.colors.textSecondary, fontSize = 11.sp)
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("مبلغ خرید", color = Vsoft.colors.textSecondary, fontSize = 11.sp)
                                Text("${item.investedAmount.toMoney()} تومان", color = Vsoft.colors.textPrimary, fontSize = 13.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("ارزش فعلی", color = Vsoft.colors.textSecondary, fontSize = 11.sp)
                                Text(if (current != null) "$current تومان" else "قیمت وارد نشده", color = Vsoft.colors.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (item.status == "ACTIVE") TextButton(onClick = { priceTarget = item; priceInput = item.currentPrice?.toString().orEmpty() }) { Text("ثبت / ویرایش قیمت") }
                            Spacer(Modifier.weight(1f))
                            IconButton(onClick = { deleteTarget = item }) { Icon(Icons.Default.Delete, "حذف", tint = Vsoft.colors.textSecondary) }
                        }
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
            text = { OutlinedTextField(priceInput, { priceInput = it.toLatinDigits().filter { c -> c.isDigit() || c == '.' }.take(18) }, label = { Text("قیمت فعلی هر واحد (تومان)") }, singleLine = true) },
            confirmButton = { TextButton(onClick = { priceInput.toDoubleOrNull()?.let { vm.updatePrice(item, it) }; priceTarget = null }) { Text("ذخیره قیمت") } },
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
