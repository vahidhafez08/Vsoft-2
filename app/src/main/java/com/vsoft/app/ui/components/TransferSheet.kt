package com.vsoft.app.ui.components

import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vsoft.app.data.model.AccountEntity
import com.vsoft.app.ui.theme.BrandBlue
import com.vsoft.app.ui.theme.ExpenseRed
import com.vsoft.app.ui.theme.Vsoft
import com.vsoft.app.ui.theme.Dimens
import com.vsoft.app.ui.theme.WarningOrange
import com.vsoft.app.util.JCal
import com.vsoft.app.util.startOfDay
import com.vsoft.app.util.toLatinDigits
import com.vsoft.app.util.toMoney
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferSheet(
    accounts: List<AccountEntity>,
    balances: Map<Long, Long>,
    onDismiss: () -> Unit,
    onTransfer: (fromId: Long, toId: Long, amount: Long, dateMillis: Long, note: String) -> Unit
) {
    var fromId by rememberSaveable(accounts) { mutableStateOf(accounts.firstOrNull()?.id ?: -1L) }
    var toId by rememberSaveable(accounts) { mutableStateOf(accounts.getOrNull(1)?.id ?: -1L) }
    var amountText by rememberSaveable { mutableStateOf("") }
    var note by rememberSaveable { mutableStateOf("") }
    var dateMillis by rememberSaveable { mutableStateOf(startOfDay(System.currentTimeMillis())) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val amount = amountText.toLongOrNull() ?: 0L
    val from = accounts.firstOrNull { it.id == fromId }
    val to = accounts.firstOrNull { it.id == toId }
    val fromBalance = from?.let { balances[it.id] ?: it.balance } ?: 0L
    val sameAccount = fromId == toId
    val amountMissing = amount <= 0L

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(Dimens.cornerSheet),
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = Vsoft.colors.surface
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().imePadding()
                .verticalScroll(rememberScrollState()).padding(horizontal = Dimens.xl).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("انتقال بین حساب‌ها", color = Vsoft.colors.textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            if (accounts.size < 2) {
                Text("برای انتقال حداقل دو حساب لازمه", color = Vsoft.colors.textSecondary, fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 28.dp))
            } else {
                Text("از حساب", color = Vsoft.colors.textSecondary, fontSize = 13.sp)
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    accounts.forEach { account ->
                        val disabled = account.id == toId
                        FilterChip(
                            selected = account.id == fromId, enabled = !disabled,
                            onClick = {
                                fromId = account.id
                                if (toId == account.id) toId = accounts.firstOrNull { it.id != account.id }?.id ?: toId
                            },
                            label = { Text(account.name) }, modifier = Modifier.alpha(if (disabled) 0.4f else 1f),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Vsoft.colors.surfaceHigh, labelColor = Vsoft.colors.textPrimary,
                                selectedContainerColor = BrandBlue, selectedLabelColor = Color.White
                            )
                        )
                    }
                }
                Text("به حساب", color = Vsoft.colors.textSecondary, fontSize = 13.sp)
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    accounts.forEach { account ->
                        val disabled = account.id == fromId
                        FilterChip(
                            selected = account.id == toId, enabled = !disabled,
                            onClick = {
                                toId = account.id
                                if (fromId == account.id) fromId = accounts.firstOrNull { it.id != account.id }?.id ?: fromId
                            },
                            label = { Text(account.name) }, modifier = Modifier.alpha(if (disabled) 0.4f else 1f),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Vsoft.colors.surfaceHigh, labelColor = Vsoft.colors.textPrimary,
                                selectedContainerColor = BrandBlue, selectedLabelColor = Color.White
                            )
                        )
                    }
                }
                if (from != null) Text("موجودی فعلی مبدأ: " + fromBalance.toMoney() + " تومان", color = Vsoft.colors.textSecondary, fontSize = 12.sp)
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { value -> amountText = value.toLatinDigits().filter { it.isDigit() }.take(18) },
                    label = { Text("مبلغ (تومان)") }, supportingText = { if (amount > 0L) Text(amount.toMoney()) },
                    singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                if (sameAccount) Text("حساب مبدأ و مقصد باید متفاوت باشن", color = ExpenseRed, fontSize = 12.sp)
                if (amountMissing) Text("مبلغ رو وارد کن", color = ExpenseRed, fontSize = 12.sp)
                if (amount > 0L && amount > fromBalance) Text("موجودی حساب مبدأ منفی می‌شه", color = WarningOrange, fontSize = 12.sp)
                Text("تاریخ انتقال", color = Vsoft.colors.textSecondary, fontSize = 13.sp)
                Row(
                    modifier = Modifier.fillMaxWidth().background(Vsoft.colors.surfaceHigh, RoundedCornerShape(12.dp)).padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = { dateMillis = Calendar.getInstance().apply { timeInMillis = dateMillis; add(Calendar.DAY_OF_YEAR, -1) }.timeInMillis }) {
                        Icon(Icons.Filled.ChevronRight, contentDescription = "روز قبل", tint = Vsoft.colors.textPrimary)
                    }
                    val jalali = JCal.toJalali(dateMillis)
                    Text(JCal.fa(jalali[2]) + " " + JCal.monthNames.getOrNull(jalali[1] - 1).orEmpty() + " " + JCal.fa(jalali[0]),
                        color = Vsoft.colors.textPrimary, fontSize = 14.sp)
                    IconButton(onClick = { dateMillis = Calendar.getInstance().apply { timeInMillis = dateMillis; add(Calendar.DAY_OF_YEAR, 1) }.timeInMillis }) {
                        Icon(Icons.Filled.ChevronLeft, contentDescription = "روز بعد", tint = Vsoft.colors.textPrimary)
                    }
                }
                OutlinedTextField(value = note, onValueChange = { note = it.take(160) },
                    label = { Text("یادداشت (اختیاری)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Button(
                    onClick = { haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove); from?.let { source -> to?.let { destination -> onTransfer(source.id, destination.id, amount, dateMillis, note) } } },
                    enabled = !sameAccount && !amountMissing && from != null && to != null,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandBlue), modifier = Modifier.fillMaxWidth()
                ) { Text("ثبت انتقال") }
            }
            Spacer(Modifier.padding(bottom = 4.dp))
        }
    }
}
