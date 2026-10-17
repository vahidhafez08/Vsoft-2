package com.vsoft.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vsoft.app.data.model.AccountEntity
import com.vsoft.app.ui.theme.BrandBlue
import com.vsoft.app.ui.theme.ExpenseRed
import com.vsoft.app.ui.theme.Vsoft
import com.vsoft.app.util.toLatinDigits
import com.vsoft.app.util.toMoney

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AccountEditorSheet(
    initial: AccountEntity?,
    onDismiss: () -> Unit,
    onSave: (AccountEntity) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var name by rememberSaveable { mutableStateOf(initial?.name ?: "") }
    var last4 by rememberSaveable { mutableStateOf(initial?.last4 ?: "") }
    var balanceText by rememberSaveable { mutableStateOf(initial?.balance?.toString() ?: "") }
    var colorKey by rememberSaveable { mutableStateOf(initial?.colorKey ?: "blue") }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val balance = balanceText.toLongOrNull() ?: 0L

    ModalBottomSheet(
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
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (initial == null) "حساب جدید" else "ویرایش حساب",
                color = Vsoft.colors.textPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            AccountCardView(name = name, last4 = last4, balance = balance, colorKey = colorKey)
            OutlinedTextField(
                value = name,
                onValueChange = { name = it.take(24) },
                label = { Text("نام بانک یا حساب") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = last4,
                onValueChange = { v -> last4 = v.toLatinDigits().filter { it.isDigit() }.take(4) },
                label = { Text("۴ رقم آخر کارت (اختیاری)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = balanceText,
                onValueChange = { v -> balanceText = v.toLatinDigits().filter { it.isDigit() }.take(12) },
                label = { Text("موجودی (تومان)") },
                supportingText = { if (balance > 0) Text(balance.toMoney()) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Text("موجودی اولیه‌ی حساب (تراکنش‌ها جداگانه حساب می‌شن)", color = Vsoft.colors.textSecondary, fontSize = 11.sp)
            Text("رنگ کارت", color = Vsoft.colors.textSecondary, fontSize = 13.sp)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AccountColorKeys.forEach { key ->
                    val selected = key == colorKey
                    Box(
                        Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(accountBrush(key))
                            .border(
                                width = if (selected) 3.dp else 0.dp,
                                color = if (selected) Vsoft.colors.textPrimary else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { colorKey = key },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selected) {
                            Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
            Button(
                onClick = {
                    onSave(
                        AccountEntity(
                            id = initial?.id ?: 0L,
                            name = name.trim(),
                            last4 = last4.ifBlank { "0000" },
                            balance = balance,
                            colorKey = colorKey
                        )
                    )
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (initial == null) "افزودن حساب" else "ذخیره تغییرات")
            }
            if (onDelete != null) {
                TextButton(onClick = { confirmDelete = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("حذف حساب", color = ExpenseRed)
                }
            }
        }
    }

    if (confirmDelete && onDelete != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            containerColor = Vsoft.colors.surface,
            title = { Text("حذف حساب؟", color = Vsoft.colors.textPrimary) },
            text = { Text("این حساب برای همیشه حذف می‌شود.", color = Vsoft.colors.textSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    onDelete()
                }) { Text("حذف", color = ExpenseRed) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("انصراف") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TransferSheet(
    accounts: List<AccountEntity>,
    onDismiss: () -> Unit,
    onTransfer: (fromId: Long, toId: Long, amount: Long) -> Unit
) {
    if (accounts.size < 2) return

    var fromId by rememberSaveable { mutableLongStateOf(accounts.first().id) }
    var toId by rememberSaveable { mutableLongStateOf(accounts[1].id) }
    var amountText by rememberSaveable { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val amount = amountText.toLongOrNull() ?: 0L
    val from = accounts.firstOrNull { it.id == fromId }
    val to = accounts.firstOrNull { it.id == toId }

    val error: String? = when {
        fromId == toId -> "حساب مبدأ و مقصد نباید یکی باشد"
        from == null || to == null -> "حساب انتخاب‌شده در دسترس نیست"
        amount > 0L && amount > from.balance -> "موجودی حساب مبدأ کافی نیست"
        else -> null
    }

    ModalBottomSheet(
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
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "انتقال وجه",
                color = Vsoft.colors.textPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text("از حساب", color = Vsoft.colors.textSecondary, fontSize = 13.sp)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                accounts.forEach { a ->
                    FilterChip(
                        selected = a.id == fromId,
                        onClick = {
                            fromId = a.id
                            if (toId == a.id) {
                                toId = accounts.firstOrNull { it.id != a.id }?.id ?: toId
                            }
                        },
                        label = { Text(a.name) }
                    )
                }
            }
            if (from != null) {
                Text(
                    "موجودی مبدأ: " + from.balance.toMoney() + " تومان",
                    color = Vsoft.colors.textSecondary,
                    fontSize = 12.sp
                )
            }
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Box(
                    Modifier
                        .size(40.dp)
                        .bounceClick {
                            val t = fromId
                            fromId = toId
                            toId = t
                        }
                        .clip(CircleShape)
                        .background(BrandBlue.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.SwapVert, contentDescription = "جابه‌جایی", tint = BrandBlue)
                }
            }
            Text("به حساب", color = Vsoft.colors.textSecondary, fontSize = 13.sp)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                accounts.forEach { a ->
                    FilterChip(
                        selected = a.id == toId,
                        onClick = {
                            toId = a.id
                            if (fromId == a.id) {
                                fromId = accounts.firstOrNull { it.id != a.id }?.id ?: fromId
                            }
                        },
                        label = { Text(a.name) }
                    )
                }
            }
            OutlinedTextField(
                value = amountText,
                onValueChange = { v -> amountText = v.toLatinDigits().filter { it.isDigit() }.take(12) },
                label = { Text("مبلغ (تومان)") },
                supportingText = {
                    if (error != null) Text(error, color = ExpenseRed)
                    else if (amount > 0) Text(amount.toMoney())
                },
                isError = error != null,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = { onTransfer(fromId, toId, amount) },
                enabled = amount > 0L && error == null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("انتقال")
            }
        }
    }
}
