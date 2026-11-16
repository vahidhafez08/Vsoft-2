package com.vsoft.app.ui.screens.finance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vsoft.app.ui.components.form.FormCard
import com.vsoft.app.ui.components.form.FormNumberField
import com.vsoft.app.ui.components.form.FormPrimaryButton
import com.vsoft.app.ui.components.form.FormScaffold
import com.vsoft.app.ui.components.form.FormTextField
import com.vsoft.app.ui.theme.Vsoft
import com.vsoft.app.util.toGroupedNumber
import com.vsoft.app.util.toLatinDigits
import com.vsoft.app.util.toMoney
import com.vsoft.app.util.toPersianDigits
import com.vsoft.app.util.toPlainNumber
import java.math.RoundingMode

@Composable
fun InvestmentDetailScreen(
    investmentId: Long,
    onBack: () -> Unit,
    vm: InvestmentsViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()
    val marketState by vm.marketState.collectAsState()
    val item = state.items.firstOrNull { it.id == investmentId }
    var quantity by remember(item?.id) { mutableStateOf(item?.quantity?.toString().orEmpty()) }
    var buyPrice by remember(item?.id) { mutableStateOf(item?.buyPrice?.toPlainNumber().orEmpty()) }
    var note by remember(item?.id) { mutableStateOf(item?.note.orEmpty()) }
    var saving by remember { mutableStateOf(false) }

    LaunchedEffect(item?.id) {
        if (item != null && item.status == "ACTIVE") vm.refreshMarketPrices()
    }

    val parsedQuantity = quantity.toLatinDigits().toDoubleOrNull() ?: 0.0
    val parsedBuyPrice = buyPrice.toLatinDigits().replace(",", "").toDoubleOrNull() ?: 0.0
    val valid = item != null && parsedQuantity > 0.0 && parsedQuantity.isFinite() &&
        parsedBuyPrice > 0.0 && parsedBuyPrice.isFinite() &&
        com.vsoft.app.domain.InvestmentCalculator.investedFrom(parsedQuantity, parsedBuyPrice) != null
    val result = item?.let { com.vsoft.app.domain.InvestmentCalculator.compute(it) }
    val type = item?.let { value -> state.types.firstOrNull { it.id == value.typeId } }

    FormScaffold(
        title = item?.title?.ifBlank { type?.name ?: "جزئیات سرمایه" } ?: "جزئیات سرمایه",
        subtitle = "مدیریت دارایی و محاسبهٔ بازدهی",
        icon = Icons.Default.TrendingUp,
        onBack = onBack,
        bottomBar = {
            FormPrimaryButton("ذخیره تغییرات", valid && !saving, saving) {
                val current = item
                if (current != null) {
                    saving = true
                    vm.updateInvestment(current, parsedQuantity, parsedBuyPrice, note.trim()) {
                        saving = false
                        onBack()
                    }
                }
            }
        }
    ) {
        if (item == null) {
            Text("این سرمایه پیدا نشد.", color = Vsoft.colors.textSecondary)
        } else {
            Column(
                Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                FormCard(Icons.Default.AccountBalance, "وضعیت دارایی") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        InvestmentTypeLogo(
                            investmentIconKeyForMarketSymbol(item.marketSymbol, type?.iconKey ?: ""),
                            type?.colorHex ?: "#5C8FE8",
                            54.dp
                        )
                        Column(Modifier.weight(1f).padding(start = 12.dp)) {
                            Text(item.title.ifBlank { type?.name ?: "سرمایه" }, color = Vsoft.colors.textPrimary, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                            Text("${item.quantity.toGroupedNumber()} ${item.marketUnit?.let(::marketUnitLabel) ?: type?.unitLabel.orEmpty()}", color = Vsoft.colors.textSecondary, fontSize = 12.sp)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("قیمت فعلی هر واحد", color = Vsoft.colors.textSecondary, fontSize = 11.sp)
                            Text("${item.currentPrice?.toLong()?.toMoney() ?: "—"} تومان", color = Vsoft.colors.textPrimary, fontWeight = FontWeight.SemiBold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("ارزش روز دارایی", color = Vsoft.colors.textSecondary, fontSize = 11.sp)
                            Text("${result?.currentValue?.setScale(0, RoundingMode.HALF_UP)?.toLong()?.toMoney() ?: "—"} تومان", color = Vsoft.colors.textPrimary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    TextButton(onClick = { vm.refreshMarketPrices() }, enabled = !marketState.isLoading) {
                        if (marketState.isLoading) androidx.compose.material3.CircularProgressIndicator(Modifier.height(14.dp), strokeWidth = 2.dp)
                        else androidx.compose.material3.Icon(Icons.Default.Refresh, contentDescription = null)
                        Text("به‌روزرسانی قیمت بازار")
                    }
                }

                FormCard(Icons.Default.TrendingUp, "سود و زیان") {
                    val profit = result?.profitLoss
                    val percent = result?.profitLossPercent
                    val profitColor = when {
                        profit == null -> Vsoft.colors.textSecondary
                        profit.signum() >= 0 -> Color(0xFF239B72)
                        else -> Color(0xFFE16B63)
                    }
                    Text(
                        when {
                            profit == null -> "قیمت فعلی برای محاسبه در دسترس نیست"
                            else -> (if (profit.signum() > 0) "+" else "") + profit.setScale(0, RoundingMode.HALF_UP).toLong().toMoney() + " تومان"
                        },
                        color = profitColor,
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        percent?.let { (if (it.signum() > 0) "+" else "") + it.setScale(2, RoundingMode.HALF_UP).toPlainString().toPersianDigits() + "٪" } ?: "—",
                        color = profitColor,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text("محاسبه بر اساس قیمت خرید و آخرین قیمت ثبت‌شدهٔ بازار", color = Vsoft.colors.textSecondary, fontSize = 11.sp)
                }

                FormCard(Icons.Default.AccountBalance, "ویرایش اطلاعات خرید") {
                    FormNumberField(raw = quantity, onChange = { quantity = it }, placeholder = "مقدار دارایی", unit = item.marketUnit?.let(::marketUnitLabel) ?: type?.unitLabel, maxDigits = 15, groupThousands = true)
                    FormNumberField(raw = buyPrice, onChange = { buyPrice = it }, placeholder = "قیمت خرید هر واحد", unit = "تومان", maxDigits = 15, groupThousands = true)
                    val updatedInvested = if (valid) com.vsoft.app.domain.InvestmentCalculator.investedFrom(parsedQuantity, parsedBuyPrice) else null
                    Text(
                        updatedInvested?.let { "مجموع مبلغ خرید: ${it.toMoney()} تومان" } ?: "مقدار و قیمت خرید معتبر وارد کنید",
                        color = Vsoft.colors.textSecondary,
                        fontSize = 12.sp
                    )
                    FormTextField(value = note, onValueChange = { note = it.take(250) }, placeholder = "یادداشت (اختیاری)", singleLine = false, minLines = 2, minHeight = 80.dp)
                }
                if (item.status == "SOLD") {
                    Text("این دارایی فروخته‌شده است؛ ویرایش آن فقط اطلاعات ثبت‌شده را تغییر می‌دهد.", color = Vsoft.colors.textSecondary, fontSize = 11.sp)
                }
            }
        }
    }
}
