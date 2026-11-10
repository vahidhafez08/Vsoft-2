package com.vsoft.app.ui.screens.finance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.rememberCoroutineScope
import com.vsoft.app.data.model.DebtType
import com.vsoft.app.ui.components.form.FormAmountField
import com.vsoft.app.ui.components.form.FormCard
import com.vsoft.app.ui.components.form.FormChip
import com.vsoft.app.ui.components.form.FormPrimaryButton
import com.vsoft.app.ui.components.form.FormScaffold
import com.vsoft.app.ui.components.form.FormTextField
import com.vsoft.app.ui.theme.Vsoft
import com.vsoft.app.util.toLatinDigits
import kotlinx.coroutines.launch

@Composable
fun DebtFormScreen(onBack: () -> Unit, vm: DebtsViewModel = hiltViewModel()) {
    var person by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(DebtType.I_OWE.name) }
    val scope = rememberCoroutineScope()
    val parsedAmount = amount.toLatinDigits().toLongOrNull() ?: 0L
    val valid = person.isNotBlank() && parsedAmount > 0L
    var saving by remember { mutableStateOf(false) }

    FormScaffold(
        title = "ثبت بدهی یا طلب",
        subtitle = "افزودن حساب شخصی",
        icon = Icons.Default.AccountBalanceWallet,
        onBack = onBack,
        bottomBar = {
            FormPrimaryButton("ذخیره بدهی یا طلب", valid && !saving, saving) {
                scope.launch {
                    saving = true
                    try {
                        vm.save(person, type, parsedAmount, null, note)
                        onBack()
                    } finally {
                        saving = false
                    }
                }
            }
        }
    ) {
        FormCard(Icons.Default.People, "اطلاعات شخص") {
            FormTextField(
                value = person,
                onValueChange = { person = it.take(60) },
                placeholder = "نام شخص یا مجموعه"
            )
        }

        FormCard(Icons.Default.AccountBalanceWallet, "نوع حساب") {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FormChip(
                    text = "من بدهکارم",
                    selected = type == DebtType.I_OWE.name,
                    onClick = { type = DebtType.I_OWE.name },
                    modifier = Modifier.weight(1f)
                )
                FormChip(
                    text = "از من طلبکارند",
                    selected = type == DebtType.OWED_TO_ME.name,
                    onClick = { type = DebtType.OWED_TO_ME.name },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        FormCard(Icons.Default.Payments, "مبلغ") {
            FormAmountField(
                value = amount,
                onValueChange = { amount = it.toLatinDigits().filter(Char::isDigit).take(15) },
                placeholder = "مبلغ بدهی یا طلب",
                trailing = { Text("تومان", color = Vsoft.colors.textSecondary, fontSize = 13.sp) }
            )
        }

        FormCard(Icons.Default.Description, "توضیحات (اختیاری)") {
            FormTextField(
                value = note,
                onValueChange = { note = it.take(250) },
                placeholder = "توضیحات بیشتر...",
                singleLine = false,
                minLines = 3,
                minHeight = 96.dp
            )
            Text(
                "${note.length}/250",
                color = Vsoft.colors.textSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
