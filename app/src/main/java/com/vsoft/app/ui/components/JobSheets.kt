package com.vsoft.app.ui.components

import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.vsoft.app.data.model.*
import com.vsoft.app.ui.screens.jobs.FloorUi
import com.vsoft.app.ui.screens.jobs.ItemUi
import com.vsoft.app.ui.theme.ExpenseRed
import com.vsoft.app.ui.theme.Vsoft
import com.vsoft.app.util.*

private val dayLabels = listOf("امروز", "دیروز", "۲ روز پیش", "۳ روز پیش")
private fun dayStart(offset: Int): Long = startOfDay(System.currentTimeMillis()) - offset * DAY_MS

private val itemSuggestions = listOf("لوله‌کشی", "سیم‌کشی پریز", "سیم‌کشی روشنایی", "نصب کلید و پریز", "نصب تابلو برق", "کابل‌کشی آنتن و تلفن")
private val itemUnits = listOf("متر", "نقطه", "عدد", "مورد")

@Composable
private fun JobNumberField(value: String, label: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { onChange(it.take(12)) },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun JobConfirmDialog(title: String, text: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        shape = RoundedCornerShape(20.dp),
        onDismissRequest = onDismiss,
        containerColor = Vsoft.colors.surface,
        title = { Text(title, color = Vsoft.colors.textPrimary) },
        text = { Text(text, color = Vsoft.colors.textSecondary) },
        confirmButton = { TextButton(onClick = onConfirm) { Text("حذف", color = ExpenseRed) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } }
    )
}

/** Generic one-field sheet with optional suggestion chips (used for floors). */
@Composable
fun JobNameSheet(
    title: String,
    label: String,
    initial: String,
    suggestions: List<String>,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var text by rememberSaveable { mutableStateOf(initial) }
    FormSheet(onDismiss) {
        SheetTitle(title)
        OutlinedTextField(
            value = text, onValueChange = { text = it.take(40) },
            label = { Text(label) }, singleLine = true, modifier = Modifier.fillMaxWidth()
        )
        if (suggestions.isNotEmpty()) {
            SheetLabel("پیشنهاد")
            ChipGroup(suggestions, { text == suggestions[it] }, { text = suggestions[it] })
        }
        Button(onClick = { onConfirm(text.trim()) }, enabled = text.isNotBlank(), modifier = Modifier.fillMaxWidth()) {
            Text(confirmLabel)
        }
    }
}

@Composable
fun JobSiteSheet(
    initial: SiteEntity?,
    onDismiss: () -> Unit,
    onSave: (SiteEntity) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var name by rememberSaveable { mutableStateOf(initial?.name ?: "") }
    var client by rememberSaveable { mutableStateOf(initial?.client ?: "") }
    var address by rememberSaveable { mutableStateOf(initial?.address ?: "") }
    var amount by rememberSaveable { mutableStateOf(initial?.contractAmount?.takeIf { it > 0L }?.toString() ?: "") }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }

    FormSheet(onDismiss) {
        SheetTitle(if (initial == null) "کارگاه جدید" else "ویرایش کارگاه")
        OutlinedTextField(
            value = name, onValueChange = { name = it.take(50) },
            label = { Text("نام کارگاه (مثلاً ساختمان آقای احمدی)") }, singleLine = true, modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = client, onValueChange = { client = it.take(40) },
            label = { Text("کارفرما (اختیاری)") }, singleLine = true, modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = address, onValueChange = { address = it.take(120) },
            label = { Text("آدرس (اختیاری)") }, maxLines = 2, modifier = Modifier.fillMaxWidth()
        )
        JobNumberField(amount, "مبلغ قرارداد به تومان (اختیاری)") { amount = it }
        parseJobNumber(amount)?.let { SheetLabel(it.toMoney() + " تومان") }
        Button(
            onClick = {
                onSave(
                    SiteEntity(
                        id = initial?.id ?: 0L,
                        name = name.trim(),
                        address = address.trim(),
                        client = client.trim(),
                        startDateMillis = initial?.startDateMillis ?: startOfDay(System.currentTimeMillis()),
                        contractAmount = parseJobNumber(amount) ?: 0L
                    )
                )
            },
            enabled = name.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (initial == null) "افزودن کارگاه" else "ذخیره تغییرات") }
        if (onDelete != null) {
            TextButton(onClick = { confirmDelete = true }, modifier = Modifier.fillMaxWidth()) {
                Text("حذف کارگاه", color = ExpenseRed)
            }
        }
    }

    if (confirmDelete && onDelete != null) {
        JobConfirmDialog(
            title = "حذف کارگاه؟",
            text = "همه‌ی طبقه‌ها، کارها، گزارش‌ها و دریافتی‌های این کارگاه برای همیشه حذف می‌شوند.",
            onConfirm = { confirmDelete = false; onDelete() },
            onDismiss = { confirmDelete = false }
        )
    }
}

@Composable
fun JobItemSheet(
    initial: WorkItemEntity?,
    onDismiss: () -> Unit,
    onSave: (title: String, unit: String, target: Int) -> Unit
) {
    var title by rememberSaveable { mutableStateOf(initial?.title ?: "") }
    var unitIdx by rememberSaveable { mutableIntStateOf(initial?.let { itemUnits.indexOf(it.unit) }?.takeIf { it >= 0 } ?: 1) }
    var target by rememberSaveable { mutableStateOf(initial?.targetQty?.toString() ?: "") }
    val targetQty = parseJobNumber(target)?.takeIf { it in 1L..1_000_000L }?.toInt()

    FormSheet(onDismiss) {
        SheetTitle(if (initial == null) "کار جدید در این طبقه" else "ویرایش کار")
        OutlinedTextField(
            value = title, onValueChange = { title = it.take(50) },
            label = { Text("نوع کار") }, singleLine = true, modifier = Modifier.fillMaxWidth()
        )
        ChipGroup(itemSuggestions, { title == itemSuggestions[it] }, { title = itemSuggestions[it] })
        SheetLabel("واحد")
        ChipGroup(itemUnits, { unitIdx == it }, { unitIdx = it })
        JobNumberField(target, "مقدار کل لازم (" + itemUnits[unitIdx] + ")") { target = it }
        Button(
            onClick = { if (targetQty != null) onSave(title.trim(), itemUnits[unitIdx], targetQty) },
            enabled = title.isNotBlank() && targetQty != null,
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (initial == null) "افزودن کار" else "ذخیره تغییرات") }
    }
}

@Composable
fun JobLogSheet(
    floors: List<FloorUi>,
    preselectItemId: Long?,
    onDismiss: () -> Unit,
    onSave: (ItemUi, Int, String, Long) -> Unit
) {
    val withItems = floors.filter { it.items.isNotEmpty() }
    var floorId by rememberSaveable {
        mutableStateOf<Long?>(
            (withItems.firstOrNull { f -> f.items.any { it.item.id == preselectItemId } } ?: withItems.firstOrNull())?.floor?.id
        )
    }
    var itemId by rememberSaveable {
        mutableStateOf<Long?>(preselectItemId ?: withItems.firstOrNull()?.items?.firstOrNull()?.item?.id)
    }
    var qty by rememberSaveable { mutableStateOf("") }
    var note by rememberSaveable { mutableStateOf("") }
    var dayIdx by rememberSaveable { mutableIntStateOf(0) }

    val floor = withItems.firstOrNull { it.floor.id == floorId }
    val items = floor?.items.orEmpty()
    val selected = items.firstOrNull { it.item.id == itemId }
    val qtyValue = parseJobNumber(qty)?.takeIf { it in 1L..1_000_000L }?.toInt()

    FormSheet(onDismiss) {
        SheetTitle("ثبت گزارش کار")
        if (withItems.isEmpty()) {
            SheetLabel("هنوز برای طبقه‌ها کاری تعریف نکرده‌ای. اول داخل هر طبقه دکمه + را بزن و کار اضافه کن.")
        } else {
            SheetLabel("طبقه")
            ChipGroup(
                withItems.map { it.floor.name },
                { withItems[it].floor.id == floorId },
                {
                    floorId = withItems[it].floor.id
                    itemId = withItems.getOrNull(it)?.items?.firstOrNull()?.item?.id
                }
            )
            SheetLabel("کار انجام‌شده")
            ChipGroup(items.map { it.item.title }, { items[it].item.id == itemId }, { itemId = items[it].item.id })
            if (selected != null) {
                SheetLabel(
                    "هدف: " + selected.item.targetQty.toString() + " " + selected.item.unit +
                        " | انجام‌شده: " + selected.done.toString() + " | مانده: " + selected.remaining.toString()
                )
            }
            JobNumberField(qty, "مقدار انجام‌شده (" + (selected?.item?.unit ?: "") + ")") { qty = it }
            SheetLabel("تاریخ")
            ChipGroup(dayLabels, { dayIdx == it }, { dayIdx = it })
            OutlinedTextField(
                value = note, onValueChange = { note = it.take(200) },
                label = { Text("یادداشت (اختیاری)") }, minLines = 2, maxLines = 4, modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = { if (selected != null && qtyValue != null) onSave(selected, qtyValue, note.trim(), dayStart(dayIdx)) },
                enabled = selected != null && qtyValue != null,
                modifier = Modifier.fillMaxWidth()
            ) { Text("ثبت گزارش") }
        }
    }
}

@Composable
fun JobPaymentSheet(onDismiss: () -> Unit, onSave: (amount: Long, note: String, dateMillis: Long) -> Unit) {
    var amount by rememberSaveable { mutableStateOf("") }
    var note by rememberSaveable { mutableStateOf("") }
    var dayIdx by rememberSaveable { mutableIntStateOf(0) }
    val value = parseJobNumber(amount)?.takeIf { it > 0L }

    FormSheet(onDismiss) {
        SheetTitle("ثبت دریافتی از کارفرما")
        JobNumberField(amount, "مبلغ (تومان)") { amount = it }
        value?.let { SheetLabel(it.toMoney() + " تومان") }
        SheetLabel("تاریخ")
        ChipGroup(dayLabels, { dayIdx == it }, { dayIdx = it })
        OutlinedTextField(
            value = note, onValueChange = { note = it.take(100) },
            label = { Text("یادداشت (اختیاری)") }, singleLine = true, modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = { if (value != null) onSave(value, note.trim(), dayStart(dayIdx)) },
            enabled = value != null,
            modifier = Modifier.fillMaxWidth()
        ) { Text("ثبت") }
    }
}
