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
import androidx.compose.foundation.layout.fillMaxHeight
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
import com.vsoft.app.ui.components.form.FormCard
import com.vsoft.app.ui.components.form.FormTextField
import com.vsoft.app.ui.components.form.FormAmountField
import com.vsoft.app.ui.components.form.FormPrimaryButton
import com.vsoft.app.ui.theme.BrandBlue
import com.vsoft.app.ui.theme.ExpenseRed
import com.vsoft.app.ui.theme.Vsoft
import com.vsoft.app.util.toLatinDigits
import com.vsoft.app.util.toMoney

@OptIn(ExperimentalLayoutApi::class)
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
    val balance = balanceText.toLongOrNull() ?: 0L

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        androidx.compose.foundation.layout.Box(
            Modifier.fillMaxWidth().fillMaxHeight().background(Vsoft.colors.bg)
        ) {
            com.vsoft.app.ui.components.form.FormScaffold(
                title = if (initial == null) "حساب جدید" else "ویرایش حساب",
                subtitle = "اطلاعات کارت و موجودی اولیه",
                icon = Icons.Filled.SwapVert,
                onBack = onDismiss,
                bottomBar = {
                    FormPrimaryButton(
                        text = if (initial == null) "افزودن حساب" else "ذخیره تغییرات",
                        enabled = name.isNotBlank(),
                        loading = false
                    ) {
                        onSave(
                            AccountEntity(
                                id = initial?.id ?: 0L,
                                name = name.trim(),
                                last4 = last4.ifBlank { "0000" },
                                balance = balance,
                                colorKey = colorKey
                            )
                        )
                    }
                }
            ) {
                FormCard(Icons.Filled.CreditCard, "پیش‌نمایش کارت") {
                    AccountCardView(name = name, last4 = last4, balance = balance, colorKey = colorKey)
                }
                FormCard(Icons.Filled.Badge, "اطلاعات حساب") {
                    FormTextField(name, { name = it.take(24) }, "نام بانک یا حساب")
                    FormTextField(
                        value = last4,
                        onValueChange = { v -> last4 = v.toLatinDigits().filter { it.isDigit() }.take(4) },
                        placeholder = "۴ رقم آخر کارت (اختیاری)",
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    )
                    FormAmountField(
                        value = balanceText,
                        onValueChange = { v -> balanceText = v.toLatinDigits().filter { it.isDigit() }.take(12) },
                        placeholder = "موجودی اولیه (تومان)"
                    )
                    Text("موجودی اولیه‌ی حساب؛ تراکنش‌ها جداگانه محاسبه می‌شوند.", color = Vsoft.colors.textSecondary, fontSize = 12.sp)
                }
                FormCard(Icons.Filled.Palette, "رنگ کارت") {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AccountColorKeys.forEach { key ->
                            val selected = key == colorKey
                            Box(
                                Modifier.size(42.dp).clip(CircleShape).background(accountBrush(key))
                                    .border(
                                        width = if (selected) 3.dp else 0.dp,
                                        color = if (selected) Vsoft.colors.textPrimary else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { colorKey = key },
                                contentAlignment = Alignment.Center
                            ) {
                                if (selected) Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
                if (onDelete != null) {
                    TextButton(onClick = { confirmDelete = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("حذف حساب", color = ExpenseRed)
                    }
                }
            }
        }
    }

    if (confirmDelete && onDelete != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            containerColor = Vsoft.colors.surface,
            title = { Text("حذف حساب؟", color = Vsoft.colors.textPrimary) },
            text = { Text("این حساب برای همیشه حذف می‌شود. انتقال‌های مربوط به این حساب هم حذف می‌شوند.", color = Vsoft.colors.textSecondary) },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; onDelete() }) { Text("حذف", color = ExpenseRed) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("انصراف") }
            }
        )
    }
}
