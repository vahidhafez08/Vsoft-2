package com.vsoft.app.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import com.vsoft.app.ui.components.VChip
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import com.vsoft.app.data.model.AccountEntity
import com.vsoft.app.ui.theme.BrandBlue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vsoft.app.data.model.ExpenseCategories
import com.vsoft.app.data.model.IncomeCategories
import com.vsoft.app.data.model.TransactionEntity
import com.vsoft.app.ui.theme.ExpenseRed
import com.vsoft.app.ui.theme.IncomeGreen
import com.vsoft.app.ui.theme.Vsoft
import com.vsoft.app.ui.theme.Dimens
import com.vsoft.app.util.DAY_MS
import com.vsoft.app.util.formatJalali
import com.vsoft.app.util.toLatinDigits
import com.vsoft.app.util.toMoney

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionEditorSheet(
    initial: TransactionEntity?,
    defaultIncome: Boolean,
    accounts: List<AccountEntity> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (TransactionEntity) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var isIncome by rememberSaveable { mutableStateOf(initial?.isIncome ?: defaultIncome) }
    val categories = if (isIncome) IncomeCategories else ExpenseCategories
    val haptics = LocalHapticFeedback.current
    var title by rememberSaveable { mutableStateOf(initial?.title ?: "") }
    var amountText by rememberSaveable { mutableStateOf(initial?.amount?.toString() ?: "") }
    var category by rememberSaveable { mutableStateOf(initial?.category ?: categories.first()) }
    var dayOffset by rememberSaveable { mutableStateOf<Int?>(if (initial == null) 0 else null) }
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("vsoft_prefs", Context.MODE_PRIVATE) }
    var accountId by rememberSaveable(initial?.id) { mutableStateOf(initial?.accountId) }
    LaunchedEffect(accounts, initial?.id) {
        if (initial == null && accountId == null) {
            val lastId = prefs.getLong("last_account_id", -1L)
            if (accounts.any { it.id == lastId }) accountId = lastId
        }
        if (accountId != null && accounts.none { it.id == accountId }) accountId = null
    }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val amount = amountText.toLongOrNull() ?: 0L
    val accent = if (isIncome) IncomeGreen else ExpenseRed
    val dayLabels = listOf("امروز", "دیروز", "۲ روز پیش", "۳ روز پیش")

    ModalBottomSheet(
        shape = RoundedCornerShape(Dimens.cornerSheet),
        dragHandle = { BottomSheetDefaults.DragHandle() },
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Vsoft.colors.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.xl)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (initial == null) "تراکنش جدید" else "ویرایش تراکنش",
                color = Vsoft.colors.textPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            SegmentedTabs(
                items = listOf("هزینه", "درآمد"),
                selected = if (isIncome) 1 else 0,
                onSelect = { index ->
                    isIncome = index == 1
                    category = (if (isIncome) IncomeCategories else ExpenseCategories).first()
                }
            )
            OutlinedTextField(
                value = title,
                onValueChange = { title = it.take(40) },
                label = { Text("عنوان (اختیاری)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = amountText,
                onValueChange = { v -> amountText = v.toLatinDigits().filter { it.isDigit() }.take(12) },
                label = { Text("مبلغ (تومان)") },
                supportingText = { if (amount > 0) Text(amount.toMoney()) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            if (accounts.isNotEmpty()) {
                Text("حساب", color = Vsoft.colors.textSecondary, fontSize = 13.sp)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (listOf<AccountEntity?>(null) + accounts).forEach { account ->
                        val selected = accountId == account?.id
                        VChip(text = account?.name ?: "بدون حساب", selected = selected, onClick = { accountId = account?.id })
                    }
                }
            }
            Text("دسته‌بندی", color = Vsoft.colors.textSecondary, fontSize = 13.sp)
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(Dimens.sm)) {
                categories.forEach { c -> VChip(text = c, selected = c == category, onClick = { category = c }) }
            }
            Text(
                text = if (initial == null) "تاریخ" else "تاریخ فعلی: " + formatJalali(initial.dateMillis),
                color = Vsoft.colors.textSecondary,
                fontSize = 13.sp
            )
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(Dimens.sm)) {
                dayLabels.forEachIndexed { index, label ->
                    VChip(text = label, selected = dayOffset == index, onClick = { dayOffset = index })
                }
            }
            Button(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    val off = dayOffset
                    val now = System.currentTimeMillis()
                    val date = if (off != null) now - off * DAY_MS else (initial?.dateMillis ?: now)
                    onSave(
                        TransactionEntity(
                            id = initial?.id ?: 0L,
                            title = title.trim().ifBlank { category },
                            amount = amount,
                            isIncome = isIncome,
                            category = category,
                            dateMillis = date,
                            accountId = accountId?.takeIf { id -> accounts.any { it.id == id } }
                        )
                    )
                },
                enabled = amount > 0L,
                colors = ButtonDefaults.buttonColors(containerColor = accent),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (initial == null) "ذخیره" else "ذخیره تغییرات")
            }
            if (onDelete != null) {
                TextButton(onClick = onDelete, modifier = Modifier.fillMaxWidth()) {
                    Text("حذف تراکنش", color = ExpenseRed)
                }
            }
        }
    }
}
