package com.vsoft.app.ui.screens.jobs

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vsoft.app.ui.components.form.*
import com.vsoft.app.ui.theme.Vsoft
import com.vsoft.app.util.formatJalali
import com.vsoft.app.util.toLatinDigits
import kotlinx.coroutines.flow.collect

@Composable
fun JobEntityFormScreen(kind: String, onBack: () -> Unit, vm: JobFormViewModelBase) {
    var dateSheet by remember { mutableStateOf(false) }
    var timeSheet by remember { mutableStateOf(false) }
    var floorSheet by remember { mutableStateOf(false) }
    var itemSheet by remember { mutableStateOf(false) }
    LaunchedEffect(vm, kind) {
        vm.load(kind)
        vm.done.collect { onBack() }
    }
    val isSite = kind == "site"
    val isFloor = kind == "floor"
    val isItem = kind == "item"
    val isLog = kind == "log"
    val isPayment = kind == "payment"
    val title = when (kind) {
        "site" -> if (vm.id >= 0L) "ویرایش کارگاه" else "افزودن کارگاه"
        "floor" -> if (vm.id >= 0L) "ویرایش طبقه" else "افزودن طبقه"
        "item" -> if (vm.id >= 0L) "ویرایش آیتم کار" else "افزودن آیتم کار"
        "log" -> if (vm.id >= 0L) "ویرایش گزارش" else "ثبت گزارش روزانه"
        else -> "ثبت دریافتی"
    }
    FormScaffold(title = title, subtitle = if (isLog || isPayment) vm.siteName else "اطلاعات دقیق و قابل پیگیری",
        icon = when(kind) { "site" -> Icons.Filled.Construction; "floor" -> Icons.Filled.Layers; "item" -> Icons.Filled.Build; "log" -> Icons.Filled.EditNote; else -> Icons.Filled.Payments },
        onBack = onBack,
        bottomBar = {
            val valid = when(kind) { "site", "floor" -> vm.name.trim().isNotBlank(); "item" -> vm.title.trim().isNotBlank() && vm.floorId != null && (vm.quantity.toIntOrNull() ?: 0) > 0; "log" -> vm.itemId != null && (vm.quantity.toIntOrNull() ?: 0) > 0; else -> (vm.amount.toLatinDigits().filter(Char::isDigit).toLongOrNull() ?: 0L) > 0L }
            FormPrimaryButton("ذخیره", valid && !vm.saving && !vm.loading, vm.saving) { vm.save(kind) }
        }) {
        when {
            isSite -> {
                FormCard(Icons.Filled.Business, "مشخصات کارگاه") {
                    FormTextField(vm.name, { vm.name = it.take(80) }, "نام کارگاه *")
                    FormTextField(vm.address, { vm.address = it.take(200) }, "آدرس (اختیاری)", singleLine = false, minLines = 2)
                }
                FormCard(Icons.Filled.Person, "کارفرما") { FormTextField(vm.client, { vm.client = it.take(80) }, "نام کارفرما (اختیاری)") }
                FormCard(Icons.Filled.CalendarMonth, "تاریخ شروع") {
                    FormPickerRow(Icons.Filled.CalendarMonth, formatJalali(vm.dateMillis), "تاریخ شروع کارگاه", { dateSheet = true })
                }
                FormCard(Icons.Filled.Payments, "مبلغ قرارداد") {
                    FormAmountField(rawDigits = vm.amount, onChange = { vm.amount = it }, placeholder = "مبلغ قرارداد (اختیاری)", maxDigits = 15)
                }
            }
            isFloor -> FormCard(Icons.Filled.Layers, "مشخصات طبقه") {
                FormTextField(vm.name, { vm.name = it.take(80) }, "نام طبقه *")
                Text("مثلاً پارکینگ، همکف یا طبقه اول", color = Vsoft.colors.textSecondary)
            }
            isItem -> {
                FormCard(Icons.Filled.Build, "مشخصات آیتم کار") {
                    FormTextField(vm.title, { vm.title = it.take(100) }, "عنوان کار *")
                    Text("واحد اندازه‌گیری", color = Vsoft.colors.textSecondary)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("متر", "متر مربع", "متر مکعب").forEach { FormChip(it, vm.unit == it, { vm.unit = it }) }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("عدد", "کیلوگرم", "روز").forEach { FormChip(it, vm.unit == it, { vm.unit = it }) }
                    }
                    FormNumberField(vm.quantity, { vm.quantity = it }, "مقدار هدف *", vm.unit, maxDigits = 9, allowDecimal = false)
                }
                FormCard(Icons.Filled.Layers, "طبقه") {
                    FormPickerRow(Icons.Filled.Layers, vm.floors.firstOrNull { it.id == vm.floorId }?.name ?: "انتخاب طبقه", "این مدل داده به طبقه نیاز دارد", { floorSheet = true })
                }
            }
            isLog -> {
                FormCard(Icons.Filled.Layers, "طبقه") {
                    FormPickerRow(Icons.Filled.Layers, vm.floors.firstOrNull { it.id == vm.floorId }?.name ?: "انتخاب طبقه", onClick = { floorSheet = true })
                }
                FormCard(Icons.Filled.Build, "آیتم کار") {
                    val item = vm.items.firstOrNull { it.id == vm.itemId }
                    FormPickerRow(Icons.Filled.Build, item?.title ?: "انتخاب آیتم", item?.let { "واحد: ${it.unit} · هدف: ${it.targetQty}" }, { itemSheet = true })
                    FormNumberField(vm.quantity, { vm.quantity = it }, "مقدار انجام‌شده *", item?.unit, maxDigits = 9, allowDecimal = false)
                    if (item != null) Text("پیشرفت فعلی بر اساس گزارش‌های ثبت‌شده نمایش داده می‌شود.", color = Vsoft.colors.textSecondary, fontSize = MaterialTheme.typography.bodySmall.fontSize)
                }
                FormCard(Icons.Filled.CalendarMonth, "تاریخ گزارش") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FormPickerRow(Icons.Filled.CalendarMonth, formatJalali(vm.dateMillis), "تاریخ", { dateSheet = true }, Modifier.weight(1f))
                        val cal = java.util.Calendar.getInstance().apply { timeInMillis = vm.dateMillis }
                        FormPickerRow(Icons.Filled.Schedule, FormFormat.time(cal.get(java.util.Calendar.HOUR_OF_DAY), cal.get(java.util.Calendar.MINUTE)), "ساعت", { timeSheet = true }, Modifier.weight(1f))
                    }
                }
                FormCard(Icons.Filled.Notes, "توضیحات") {
                    FormTextField(vm.note, { vm.note = it.take(300) }, "شرح کار انجام‌شده", singleLine = false, minLines = 3)
                    FormCounterText(vm.note.length, 300)
                }
            }
            isPayment -> {
                FormCard(Icons.Filled.Payments, "مبلغ دریافتی") {
                    FormAmountField(rawDigits = vm.amount, onChange = { vm.amount = it }, placeholder = "مبلغ به تومان *", maxDigits = 15)
                }
                FormCard(Icons.Filled.CalendarMonth, "تاریخ دریافتی") {
                    FormPickerRow(Icons.Filled.CalendarMonth, formatJalali(vm.dateMillis), "تاریخ", { dateSheet = true })
                }
                FormCard(Icons.Filled.Notes, "توضیحات") {
                    FormTextField(vm.note, { vm.note = it.take(200) }, "توضیحات (اختیاری)", singleLine = false, minLines = 2)
                    FormCounterText(vm.note.length, 200)
                }
            }
        }
    }
    if (dateSheet) FormDatePickerSheet(vm.dateMillis, { day ->
        if (isLog || isPayment) {
            val old = java.util.Calendar.getInstance().apply { timeInMillis = vm.dateMillis }
            val next = java.util.Calendar.getInstance().apply { timeInMillis = day; set(java.util.Calendar.HOUR_OF_DAY, old.get(java.util.Calendar.HOUR_OF_DAY)); set(java.util.Calendar.MINUTE, old.get(java.util.Calendar.MINUTE)) }
            vm.dateMillis = next.timeInMillis
        } else vm.dateMillis = day
        dateSheet = false
    }, { dateSheet = false })
    if (timeSheet) {
        val cal = java.util.Calendar.getInstance().apply { timeInMillis = vm.dateMillis }
        FormTimePickerSheet(cal.get(java.util.Calendar.HOUR_OF_DAY), cal.get(java.util.Calendar.MINUTE), { h, m ->
            val next = java.util.Calendar.getInstance().apply { timeInMillis = vm.dateMillis; set(java.util.Calendar.HOUR_OF_DAY, h); set(java.util.Calendar.MINUTE, m); set(java.util.Calendar.SECOND, 0); set(java.util.Calendar.MILLISECOND, 0) }
            vm.dateMillis = next.timeInMillis
            timeSheet = false
        }, { timeSheet = false })
    }
    if (floorSheet) FormOptionSheet("انتخاب طبقه", vm.floors.map { FormOption(it.id, it.name) }, vm.floorId, {
        vm.floorId = it
        if (isLog) vm.itemId = vm.items.firstOrNull { item -> item.floorId == it }?.id
        floorSheet = false
    }, { floorSheet = false })
    if (itemSheet) {
        val filtered = vm.items.filter { vm.floorId == null || it.floorId == vm.floorId }
        FormOptionSheet("انتخاب آیتم کار", filtered.map { FormOption(it.id, it.title, "واحد: ${it.unit} · هدف: ${it.targetQty}") }, vm.itemId, {
            vm.itemId = it
            val selected = filtered.firstOrNull { item -> item.id == it }
            if (selected != null) vm.floorId = selected.floorId
            itemSheet = false
        }, { itemSheet = false })
    }
}
