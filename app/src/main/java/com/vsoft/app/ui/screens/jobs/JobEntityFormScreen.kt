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
import com.vsoft.app.util.toPersianText
import com.vsoft.app.util.formatJobQuantity
import kotlin.math.roundToInt
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
            val valid = when (kind) {
                "site", "floor" -> vm.name.trim().isNotBlank()
                "item" -> vm.title.trim().isNotBlank() && vm.floorId != null &&
                    (vm.unit == "درصد" || (vm.quantity.toLatinDigits().toIntOrNull() ?: 0) > 0)
                "log" -> {
                    val selected = vm.items.firstOrNull { it.id == vm.itemId }
                    val qty = vm.quantity.toLatinDigits().toIntOrNull() ?: 0
                    vm.itemId != null && (if (selected?.unit == "درصد") qty in 0..100 else qty > 0)
                }
                else -> (vm.amount.toLatinDigits().filter(Char::isDigit).toLongOrNull() ?: 0L) > 0L
            }
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
                        listOf("متر", "متر مربع", "متر مکعب", "درصد").forEach { FormChip(it, vm.unit == it, { vm.unit = it; if (it == "درصد") vm.quantity = "100" }) }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("عدد", "کیلوگرم", "روز").forEach { FormChip(it, vm.unit == it, { vm.unit = it }) }
                    }
                    if (vm.unit == "درصد") {
                        Text("هدف این کار همیشه ۱۰۰٪ است.", color = Vsoft.colors.textSecondary, fontSize = 12.sp)
                    } else {
                        FormNumberField(vm.quantity, { vm.quantity = it }, "مقدار هدف *", vm.unit, maxDigits = 9, allowDecimal = false)
                    }
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
                    if (item?.unit == "درصد") {
                        val current = (vm.quantity.toLatinDigits().toIntOrNull() ?: 0).coerceIn(0, 100)
                        Text("درصد پیشرفت", color = Vsoft.colors.textSecondary, fontSize = 12.sp)
                        Slider(
                            value = current.toFloat(),
                            onValueChange = { vm.quantity = it.roundToInt().coerceIn(0, 100).toString() },
                            valueRange = 0f..100f,
                            steps = 99
                        )
                        Text(formatJobQuantity(current, "درصد"), color = Vsoft.colors.textPrimary, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                    } else {
                        FormNumberField(vm.quantity, { vm.quantity = it }, "مقدار انجام‌شده *", item?.unit, maxDigits = 9, allowDecimal = false)
                    }
                    if (item != null) {
                        val logged = vm.logs.filter { it.itemId == item.id }
                        val completed = if (item.unit == "درصد") logged.maxWithOrNull(compareBy<WorkLogEntity> { it.dateMillis }.thenBy { it.id })?.qty?.coerceIn(0, 100) ?: 0 else logged.sumOf { it.qty }
                        Text(
                            "پیشرفت فعلی: " + (if (item.unit == "درصد") formatJobQuantity(completed, item.unit) else completed.toPersianText() + " از " + item.targetQty.toPersianText() + " " + item.unit),
                            color = Vsoft.colors.textSecondary,
                            fontSize = MaterialTheme.typography.bodySmall.fontSize
                        )
                    }
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
    if (floorSheet) FormOptionSheet("انتخاب طبقه", (if (isLog) vm.floors.filter { floor -> vm.items.any { it.floorId == floor.id } } else vm.floors).map { FormOption(it.id, it.name) }, vm.floorId, {
        vm.floorId = it
        if (isLog) vm.itemId = vm.items.firstOrNull { item -> item.floorId == it }?.id
        floorSheet = false
    }, { floorSheet = false })
    if (itemSheet) {
        val filtered = vm.items.filter { vm.floorId == null || it.floorId == vm.floorId }
        FormOptionSheet("انتخاب آیتم کار", filtered.map { item ->
            val itemLogs = vm.logs.filter { log -> log.itemId == item.id }
            val completed = if (item.unit == "درصد") itemLogs.maxWithOrNull(compareBy<WorkLogEntity> { it.dateMillis }.thenBy { it.id })?.qty?.coerceIn(0, 100) ?: 0 else itemLogs.sumOf { it.qty }
            FormOption(item.id, item.title, "واحد: ${item.unit} · " + (if (item.unit == "درصد") formatJobQuantity(completed, item.unit) else "انجام‌شده: $completed از ${item.targetQty} ${item.unit}"))
        }, vm.itemId, {
            vm.itemId = it
            val selected = filtered.firstOrNull { item -> item.id == it }
            if (selected != null) {
                vm.floorId = selected.floorId
                if (selected.unit == "درصد") {
                    vm.quantity = vm.logs.filter { log -> log.itemId == selected.id }
                        .maxWithOrNull(compareBy<WorkLogEntity> { it.dateMillis }.thenBy { it.id })
                        ?.qty?.coerceIn(0, 100)?.toString() ?: "0"
                }
            }
            itemSheet = false
        }, { itemSheet = false })
    }
}
