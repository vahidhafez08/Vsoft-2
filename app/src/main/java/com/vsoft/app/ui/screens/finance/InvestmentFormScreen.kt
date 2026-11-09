package com.vsoft.app.ui.screens.finance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vsoft.app.data.model.InvestmentEntity
import com.vsoft.app.ui.theme.Vsoft
import com.vsoft.app.util.toLatinDigits

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvestmentFormScreen(onBack: () -> Unit, vm: InvestmentsViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    var typeId by remember { mutableLongStateOf(0L) }
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var buyPrice by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var currentPrice by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    LaunchedEffect(state.types) { if (typeId == 0L) typeId = state.types.firstOrNull()?.id ?: 0L }
    val parsedAmount = amount.toLatinDigits().toLongOrNull() ?: 0L
    val parsedBuy = buyPrice.toLatinDigits().toDoubleOrNull() ?: 0.0
    val parsedQuantity = quantity.toLatinDigits().toDoubleOrNull() ?: 0.0
    val parsedCurrent = currentPrice.toLatinDigits().toDoubleOrNull()
    val valid = typeId > 0L && parsedAmount > 0L && parsedBuy > 0.0 && parsedQuantity > 0.0 &&
        parsedBuy.isFinite() && parsedQuantity.isFinite() &&
        (parsedCurrent == null || (parsedCurrent > 0.0 && parsedCurrent.isFinite()))
    Scaffold(
        containerColor = Vsoft.colors.bg,
        topBar = { TopAppBar(title = { Text("ثبت سرمایه‌گذاری", fontWeight = FontWeight.Bold) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "بازگشت") } }) },
        bottomBar = {
            Button(onClick = {
                vm.save(InvestmentEntity(
                    typeId = typeId, title = title.trim(), investedAmount = parsedAmount,
                    buyPrice = parsedBuy, quantity = parsedQuantity, currentPrice = parsedCurrent,
                    priceUpdatedAtMillis = if (parsedCurrent != null) System.currentTimeMillis() else null,
                    buyDateMillis = System.currentTimeMillis(), note = note.trim()
                ))
                onBack()
            }, enabled = valid, modifier = Modifier.fillMaxWidth().padding(16.dp)) { Text("ذخیره سرمایه‌گذاری") }
        }
    ) { padding ->
        Column(Modifier.fillMaxWidth().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("نوع سرمایه", color = Vsoft.colors.textPrimary, style = MaterialTheme.typography.titleMedium)
            if (state.types.isEmpty()) Text("انواع پایه سرمایه‌گذاری در دسترس نیستند.", color = Vsoft.colors.textSecondary)
            else state.types.chunked(3).forEach { group ->
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    group.forEach { item ->
                        FilterChip(typeId == item.id, { typeId = item.id }, label = { Text(item.name) })
                    }
                }
            }
            OutlinedTextField(title, { title = it.take(60) }, modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text("عنوان یا توضیح کوتاه (اختیاری)") }, leadingIcon = { Icon(Icons.Default.TrendingUp, null) })
            Text("اطلاعات خرید", color = Vsoft.colors.textPrimary, style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(amount, { amount = it.toLatinDigits().filter(Char::isDigit).take(15) }, modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text("مبلغ کل سرمایه‌گذاری (تومان)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            OutlinedTextField(buyPrice, { buyPrice = it.toLatinDigits().filter { c -> c.isDigit() || c == '.' }.take(18) }, modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text("قیمت خرید هر واحد (تومان)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
            OutlinedTextField(quantity, { quantity = it.toLatinDigits().filter { c -> c.isDigit() || c == '.' }.take(18) }, modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text("مقدار / تعداد") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
            Text("قیمت فعلی", color = Vsoft.colors.textPrimary, style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(currentPrice, { currentPrice = it.toLatinDigits().filter { c -> c.isDigit() || c == '.' }.take(18) }, modifier = Modifier.fillMaxWidth(), singleLine = true, label = { Text("قیمت فعلی هر واحد (اختیاری)") }, supportingText = { Text("قیمت را فعلاً دستی وارد کنید؛ اتصال API بعداً اضافه می‌شود.") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
            OutlinedTextField(note, { note = it.take(250) }, modifier = Modifier.fillMaxWidth(), minLines = 3, label = { Text("یادداشت (اختیاری)") })
        }
    }
}
