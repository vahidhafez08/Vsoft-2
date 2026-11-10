package com.vsoft.app.ui.screens.finance

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vsoft.app.data.model.InvestmentEntity
import com.vsoft.app.ui.components.form.FormAmountField
import com.vsoft.app.ui.components.form.FormCard
import com.vsoft.app.ui.components.form.FormNumberField
import com.vsoft.app.ui.components.form.FormPrimaryButton
import com.vsoft.app.ui.components.form.FormScaffold
import com.vsoft.app.ui.components.form.FormTextField
import com.vsoft.app.ui.components.form.FormTile
import com.vsoft.app.ui.theme.Vsoft
import com.vsoft.app.util.toLatinDigits
import kotlinx.coroutines.launch

@Composable
fun InvestmentFormScreen(onBack: () -> Unit, vm: InvestmentsViewModel = hiltViewModel()) {
    val state by vm.state.collectAsState()
    val scope = rememberCoroutineScope()
    var typeId by remember { mutableLongStateOf(0L) }
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var buyPrice by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var currentPrice by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }

    LaunchedEffect(state.types) {
        if (typeId == 0L) typeId = state.types.firstOrNull()?.id ?: 0L
    }

    val selectedType = state.types.firstOrNull { it.id == typeId }
    val parsedAmount = amount.toLatinDigits().toLongOrNull() ?: 0L
    val parsedBuy = buyPrice.toLatinDigits().toDoubleOrNull() ?: 0.0
    val parsedQuantity = quantity.toLatinDigits().toDoubleOrNull() ?: 0.0
    val parsedCurrent = currentPrice.toLatinDigits().toDoubleOrNull()
    val valid = typeId > 0L && parsedAmount > 0L && parsedBuy > 0.0 && parsedQuantity > 0.0 &&
        parsedBuy.isFinite() && parsedQuantity.isFinite() &&
        (parsedCurrent == null || (parsedCurrent > 0.0 && parsedCurrent.isFinite()))

    FormScaffold(
        title = "ثبت سرمایه‌گذاری",
        subtitle = "افزودن دارایی به سبد سرمایه",
        icon = Icons.Default.TrendingUp,
        onBack = onBack,
        bottomBar = {
            FormPrimaryButton("ذخیره سرمایه‌گذاری", valid && !saving, saving) {
                scope.launch {
                    saving = true
                    try {
                        vm.save(
                            InvestmentEntity(
                                typeId = typeId,
                                title = title.trim(),
                                investedAmount = parsedAmount,
                                buyPrice = parsedBuy,
                                quantity = parsedQuantity,
                                currentPrice = parsedCurrent,
                                priceUpdatedAtMillis = if (parsedCurrent != null) System.currentTimeMillis() else null,
                                buyDateMillis = System.currentTimeMillis(),
                                note = note.trim()
                            )
                        )
                        onBack()
                    } finally {
                        saving = false
                    }
                }
            }
        }
    ) {
        FormCard(Icons.Default.Category, "نوع دارایی") {
            if (state.types.isEmpty()) {
                Text("انواع پایه سرمایه‌گذاری در دسترس نیستند.", color = Vsoft.colors.textSecondary)
            } else {
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    state.types.forEachIndexed { index, type ->
                        FormTile(
                            label = type.name,
                            icon = investmentTypeIcon(type.name),
                            tint = investmentTypeColor(index),
                            selected = typeId == type.id
                        ) { typeId = type.id }
                    }
                }
            }
            FormTextField(
                value = title,
                onValueChange = { title = it.take(60) },
                placeholder = "عنوان یا توضیح کوتاه (اختیاری)"
            )
        }

        FormCard(Icons.Default.MonetizationOn, "اطلاعات خرید") {
            FormAmountField(
                value = amount,
                onValueChange = { amount = it.toLatinDigits().filter(Char::isDigit).take(15) },
                placeholder = "مبلغ کل سرمایه‌گذاری",
                trailing = { Text("تومان", color = Vsoft.colors.textSecondary) }
            )
            FormNumberField(
                raw = buyPrice,
                onChange = { buyPrice = it },
                placeholder = "قیمت خرید هر واحد",
                unit = "تومان",
                maxDigits = 15
            )
            FormNumberField(
                raw = quantity,
                onChange = { quantity = it },
                placeholder = "مقدار / تعداد",
                unit = selectedType?.unitLabel?.takeIf { it.isNotBlank() },
                maxDigits = 15
            )
        }

        FormCard(Icons.Default.AttachMoney, "قیمت فعلی") {
            FormNumberField(
                raw = currentPrice,
                onChange = { currentPrice = it },
                placeholder = "قیمت فعلی هر واحد (اختیاری)",
                unit = "تومان",
                maxDigits = 15
            )
            Text(
                "قیمت را فعلاً دستی وارد کنید؛ اتصال به API در آینده اضافه می‌شود.",
                color = Vsoft.colors.textSecondary
            )
        }

        FormCard(Icons.Default.AccountBalance, "یادداشت (اختیاری)") {
            FormTextField(
                value = note,
                onValueChange = { note = it.take(250) },
                placeholder = "توضیحات یا جزئیات بیشتر...",
                singleLine = false,
                minLines = 3,
                minHeight = 96.dp
            )
            Text(
                "${note.length}/250",
                color = Vsoft.colors.textSecondary
            )
        }
    }
}

private fun investmentTypeIcon(name: String): ImageVector {
    val value = name.lowercase()
    return when {
        "طلا" in value || "سکه" in value || "جواهر" in value -> Icons.Default.Diamond
        "دلار" in value || "ارز" in value -> Icons.Default.AttachMoney
        "بانک" in value || "سپرده" in value -> Icons.Default.AccountBalance
        "سهام" in value || "بورس" in value -> Icons.Default.TrendingUp
        else -> Icons.Default.Category
    }
}

private fun investmentTypeColor(index: Int): Color {
    val colors = listOf(
        Color(0xFFD6A642),
        Color(0xFF4C9B83),
        Color(0xFF5C8FE8),
        Color(0xFF9A75C8),
        Color(0xFFDB8064)
    )
    return colors[index % colors.size]
}
