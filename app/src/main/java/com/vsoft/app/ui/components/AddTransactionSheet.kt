package com.vsoft.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import com.vsoft.app.data.model.AccountEntity
import com.vsoft.app.ui.theme.BrandBlue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vsoft.app.data.model.ExpenseCategories
import com.vsoft.app.data.model.IncomeCategories
import com.vsoft.app.ui.theme.Vsoft
import com.vsoft.app.util.toLatinDigits
import com.vsoft.app.util.toMoney

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddTransactionSheet(
    isIncome: Boolean,
    accounts: List<AccountEntity> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (title: String, amount: Long, category: String, accountId: Long?) -> Unit
) {
    val categories = if (isIncome) IncomeCategories else ExpenseCategories
    var title by rememberSaveable { mutableStateOf("") }
    var amountText by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf(categories.first()) }
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("vsoft_prefs", Context.MODE_PRIVATE) }
    var accountId by rememberSaveable { mutableStateOf<Long?>(null) }
    androidx.compose.runtime.LaunchedEffect(accounts) {
        if (accountId == null) {
            val lastId = prefs.getLong("last_account_id", -1L)
            if (accounts.any { it.id == lastId }) accountId = lastId
        }
        if (accountId != null && accounts.none { it.id == accountId }) accountId = null
    }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val amount = amountText.toLongOrNull() ?: 0L

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Vsoft.colors.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
                .navigationBarsPadding()
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (isIncome) "ثبت درآمد" else "ثبت هزینه",
                color = Vsoft.colors.textPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
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
                        FilterChip(
                            selected = accountId == account?.id,
                            onClick = { accountId = account?.id },
                            label = { Text(account?.name ?: "بدون حساب") },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Vsoft.colors.surfaceHigh,
                                labelColor = Vsoft.colors.textPrimary,
                                selectedContainerColor = BrandBlue,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
            Text("دسته‌بندی", color = Vsoft.colors.textSecondary, fontSize = 13.sp)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                categories.forEach { c ->
                    FilterChip(
                        selected = c == category,
                        onClick = { category = c },
                        label = { Text(c) }
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Button(
                onClick = { onSave(title.trim().ifBlank { category }, amount, category, accountId?.takeIf { id -> accounts.any { it.id == id } }) },
                enabled = amount > 0L,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("ذخیره")
            }
        }
    }
}