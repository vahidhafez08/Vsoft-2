package com.vsoft.app.ui.screens.transactions

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vsoft.app.ui.components.CategoryStyles
import com.vsoft.app.ui.components.form.*
import com.vsoft.app.ui.theme.Vsoft
import com.vsoft.app.util.UiEvents
import com.vsoft.app.util.toLatinDigits
import com.vsoft.app.util.toPersianDigits

@Composable
fun TransactionFormScreen(onBack: () -> Unit, vm: TransactionFormViewModel = hiltViewModel()) {
    val c = Vsoft.colors
    var showDate by remember { mutableStateOf(false) }
    var showTime by remember { mutableStateOf(false) }
    var showAccounts by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { vm.done.collect { onBack() } }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> if (uri != null) vm.setAttachment(uri) }
    val accounts by vm.accounts.collectAsState()
    val selectedAccount = accounts.firstOrNull { it.id == vm.accountId }
    val categoryNames = vm.categoryNames()

    FormScaffold(
        title = if (vm.isEdit) "ویرایش تراکنش" else "افزودن تراکنش",
        subtitle = "ثبت درآمد و هزینه",
        icon = Icons.Default.AccountBalanceWallet,
        onBack = onBack,
        bottomBar = { FormPrimaryButton("ذخیره تراکنش", vm.canSave, vm.saving) { vm.save() } }
    ) {
        FormTypeToggle(vm.isIncome) { vm.isIncome = it; vm.category = null }
        FormCard(Icons.Default.GridView, "دسته‌بندی") {
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                categoryNames.forEach { name ->
                    val style = CategoryStyles.of(name)
                    FormTile(name, style.icon, style.color, vm.category == name) { vm.category = name }
                }
            }
        }
        if (accounts.isNotEmpty()) FormCard(Icons.Default.AccountBalanceWallet, "حساب") {
            FormPickerRow(Icons.Default.AccountBalance, selectedAccount?.name ?: "بدون حساب",
                selectedAccount?.let { "موجودی: ${it.balanceText} تومان" }, { showAccounts = true })
        }
        FormCard(Icons.Default.MonetizationOn, "مبلغ") {
            FormAmountField(
                value = vm.amountRaw,
                onValueChange = { s -> vm.amountRaw = s.toLatinDigits().filter { it.isDigit() }.take(15) },
                placeholder = "مبلغ را وارد کنید",
                trailing = { Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("تومان", color = c.textPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.width(8.dp))
                    Box(Modifier.size(20.dp).clip(CircleShape).background(Color(0xFF2E7D4F)))
                } }
            )
        }
        FormCard(Icons.Default.CalendarMonth, "تاریخ و زمان") {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FormPickerRow(Icons.Default.Schedule, FormFormat.time(vm.hour, vm.minute), null, { showTime = true }, Modifier.weight(1f))
                FormPickerRow(Icons.Default.CalendarMonth, FormFormat.date(vm.dayMillis), FormFormat.weekday(vm.dayMillis), { showDate = true }, Modifier.weight(1.2f))
            }
        }
        FormCard(Icons.Default.Description, "توضیحات (اختیاری)") {
            Column {
                FormTextField(vm.note, { vm.note = it.take(250) }, "توضیحات تراکنش را وارد کنید...", singleLine = false, minLines = 3, minHeight = 96.dp)
                Spacer(Modifier.height(6.dp))
                Text("${vm.note.length}/250".toPersianDigits(), color = c.textSecondary, fontSize = 12.sp)
            }
        }
        if (!vm.isEdit) FormCard(Icons.Default.AttachFile, "فایل ضمیمه (اختیاری)") {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                val shape = RoundedCornerShape(14.dp)
                Box(Modifier.weight(1f).heightIn(min = 52.dp).clip(shape).dashedBorder(c.outline, shape).padding(horizontal = 12.dp), contentAlignment = Alignment.CenterStart) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Image, null, tint = c.textSecondary); Spacer(Modifier.width(8.dp))
                        Text(vm.attachmentName ?: "هیچ فایلی انتخاب نشده", color = c.textSecondary, fontSize = 13.sp, maxLines = 1, modifier = Modifier.weight(1f, fill = false))
                        if (vm.attachmentUri != null) { Spacer(Modifier.width(6.dp)); Icon(Icons.Default.Close, "حذف فایل", tint = c.textSecondary, modifier = Modifier.size(20.dp).clickable { vm.clearAttachment() }) }
                    }
                }
                Row(Modifier.weight(1f).heightIn(min = 52.dp).clip(shape).border(1.dp, c.outline.copy(alpha=.7f), shape).clickable { picker.launch(arrayOf("*/*")) }.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Box(Modifier.size(24.dp).clip(CircleShape).background(FormStyle.Blue1), contentAlignment = Alignment.Center) { Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(16.dp)) }
                    Spacer(Modifier.width(8.dp)); Text("افزودن تصویر یا فایل", color = c.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }
            Text("فایل در بخش اسناد ذخیره می‌شه", color = c.textSecondary, fontSize = 12.sp)
        }
        FormCard(Icons.Default.Notifications, "تنظیم یادآوری") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("برای این تراکنش یادآوری تنظیم شود", Modifier.weight(1f), color = c.textSecondary, fontSize = 13.sp)
                Switch(checked = false, onCheckedChange = { UiEvents.show("یادآوری آیتم‌ها به‌زودی فعال می‌شه") })
            }
        }
    }
    if (showDate) FormDatePickerSheet(vm.dayMillis, { vm.dayMillis = it; showDate = false }, { showDate = false })
    if (showTime) FormTimePickerSheet(vm.hour, vm.minute, { h, m -> vm.hour = h; vm.minute = m; showTime = false }, { showTime = false })
    if (showAccounts) FormAccountSheet(accounts, vm.accountId, { vm.accountId = it; showAccounts = false }, { showAccounts = false })
}
private fun Modifier.dashedBorder(color: Color, shape: RoundedCornerShape): Modifier = drawBehind {
    val r = shape.topStart.toPx(size, this)
    drawRoundRect(color = color.copy(alpha=.8f), cornerRadius = CornerRadius(r, r),
        style = Stroke(width = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f))))
}
