package com.vsoft.app.ui.screens.finance

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vsoft.app.data.model.InvestmentEntity
import com.vsoft.app.ui.components.form.FormCard
import com.vsoft.app.ui.components.form.FormNumberField
import com.vsoft.app.ui.components.form.FormPrimaryButton
import com.vsoft.app.ui.components.form.FormScaffold
import com.vsoft.app.ui.components.form.FormTextField
import com.vsoft.app.ui.theme.Vsoft
import com.vsoft.app.util.toLatinDigits
import com.vsoft.app.util.toMoney
import com.vsoft.app.util.toPlainNumber
import kotlinx.coroutines.launch

@Composable
fun InvestmentFormScreen(onBack: () -> Unit, vm: InvestmentsViewModel = hiltViewModel()) {
    val state by vm.state.collectAsState()
    val marketState by vm.marketState.collectAsState()
    val scope = rememberCoroutineScope()
    var typeId by remember { mutableLongStateOf(0L) }
    var quantity by remember { mutableStateOf("") }
    var buyPriceInput by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedSymbol by remember { mutableStateOf("") }
    var search by remember { mutableStateOf("") }
    var menuExpanded by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    var showTypeSheet by remember { mutableStateOf(false) }
    var pendingTypeName by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(state.types) {
        if (typeId == 0L) typeId = state.types.firstOrNull()?.id ?: 0L
        pendingTypeName?.let { name ->
            state.types.firstOrNull { it.name == name }?.let { typeId = it.id; pendingTypeName = null }
        }
    }

    val selectedType = state.types.firstOrNull { it.id == typeId }
    val allowedCategories = when (selectedType?.iconKey) {
        "gold", "gold_milligram" -> setOf("gold")
        "coin" -> setOf("coin")
        "dollar", "euro" -> setOf("currency")
        "crypto" -> setOf("crypto")
        "stock" -> setOf("stock", "stocks", "equity", "index")
        "fund" -> setOf("fund", "funds")
        "box", "savings" -> setOf("commodity", "commodities")
        else -> emptySet()
    }
    val availablePrices = marketState.allPrices.filter {
        !it.stale && it.currency in setOf("IRT", "IRR") &&
            it.price > 0.0 && it.price.isFinite() &&
            it.category.lowercase() in allowedCategories &&
            when (selectedType?.name) {
                "دلار" -> it.symbol == "USD_IRR_FREE"
                "یورو" -> it.symbol == "EUR_IRR_FREE"
                else -> true
            }
    }.sortedBy { it.name }.map { price ->
        if (selectedType?.iconKey == "gold_milligram" && price.symbol.startsWith("GOLD_")) {
            price.copy(price = price.price / 1000.0, unit = "میلی‌گرم")
        } else price
    }
    val selectedPrice = availablePrices.firstOrNull { it.symbol == selectedSymbol }
        ?: availablePrices.firstOrNull()
    LaunchedEffect(availablePrices) {
        if (availablePrices.none { it.symbol == selectedSymbol }) {
            selectedSymbol = availablePrices.firstOrNull()?.symbol.orEmpty()
        }
    }

    LaunchedEffect(selectedPrice?.symbol) {
        buyPriceInput = selectedPrice?.price?.toPlainNumber().orEmpty()
    }
    val parsedQuantity = quantity.toLatinDigits().toDoubleOrNull() ?: 0.0
    val parsedBuyPrice = buyPriceInput.toLatinDigits().replace(",", "").toDoubleOrNull() ?: 0.0
    val totalValue = selectedPrice?.let { parsedQuantity * it.price } ?: 0.0
    val investedValue = parsedQuantity * parsedBuyPrice
    val valid = selectedType != null && selectedPrice != null && !marketState.isCached &&
        parsedQuantity > 0.0 && parsedQuantity.isFinite() &&
        parsedBuyPrice > 0.0 && parsedBuyPrice.isFinite() &&
        investedValue > 0.0 && investedValue.isFinite() && investedValue <= Long.MAX_VALUE.toDouble() &&
        totalValue > 0.0 && totalValue.isFinite()

    FormScaffold(
        title = "ثبت سرمایه‌گذاری",
        subtitle = "محاسبه ارزش دارایی با قیمت تازه بازار",
        icon = Icons.Default.TrendingUp,
        onBack = onBack,
        bottomBar = {
            FormPrimaryButton("ذخیره سرمایه‌گذاری", valid && !saving, saving) {
                scope.launch {
                    saving = true
                    try {
                        val price = selectedPrice
                        val type = selectedType
                        if (price != null && type != null) {
                            val saved = vm.save(
                                InvestmentEntity(
                                    typeId = type.id,
                                    title = price.name,
                                    investedAmount = investedValue.toLong(),
                                    buyPrice = parsedBuyPrice,
                                    quantity = parsedQuantity,
                                    currentPrice = price.price,
                                    priceUpdatedAtMillis = System.currentTimeMillis(),
                                    buyDateMillis = System.currentTimeMillis(),
                                    note = note.trim(),
                                    marketSymbol = price.symbol
                                ),
                                price.symbol,
                                parsedBuyPrice,
                                priceDivisor = if (type.iconKey == "gold_milligram") 1000.0 else 1.0
                            )
                            if (saved) onBack()
                        }
                    } finally {
                        saving = false
                    }
                }
            }
        }
    ) {
        FormCard(Icons.Default.Category, "نوع دارایی") {
            if (state.types.isEmpty()) {
                Text("انواع پایه سرمایه‌گذاری در دسترس نیستند.", color = Vsoft.colors.textSecondary)
            } else {
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    state.types.forEach { type ->
                        InvestmentTypeTile(type, typeId == type.id) {
                            typeId = type.id
                            selectedSymbol = ""
                            search = ""
                        }
                    }
                    InvestmentAddTypeTile { showTypeSheet = true }
                }
            }
        }

        FormCard(Icons.Default.TrendingUp, "انتخاب دارایی بازار") {
            if (marketState.isLoading && marketState.allPrices.isEmpty()) {
                Text("در حال دریافت فهرست دارایی‌ها و قیمت‌ها...", color = Vsoft.colors.textSecondary)
            } else if (availablePrices.isEmpty()) {
                Text(
                    if (marketState.isCached) "قیمت تازه برای این دسته در دسترس نیست. برای ثبت، اتصال اینترنت و قیمت به‌روز لازم است."
                    else "برای این نوع دارایی، قیمت ریالی تازه‌ای در منبع بازار پیدا نشد.",
                    color = Vsoft.colors.textSecondary
                )
                TextButton(onClick = { vm.refreshMarketPrices() }, enabled = !marketState.isLoading) {
                    Text("دریافت دوباره قیمت‌ها")
                }
            } else {
                Box {
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable { menuExpanded = true },
                        color = Vsoft.colors.surface,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Vsoft.colors.outline)
                    ) {
                        Column(Modifier.fillMaxWidth().padding(14.dp)) {
                            Text("دارایی انتخاب‌شده", color = Vsoft.colors.textSecondary, fontSize = 11.sp)
                            Text(selectedPrice?.name.orEmpty(), color = Vsoft.colors.textPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text("واحد قیمت: ${selectedPrice?.unit?.let(::marketUnitLabel).orEmpty()} · برای تغییر لمس کنید", color = Vsoft.colors.textSecondary, fontSize = 11.sp)
                        }
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        modifier = Modifier.heightIn(max = 420.dp)
                    ) {
                        FormTextField(
                            value = search,
                            onValueChange = { search = it.take(40) },
                            placeholder = "جست‌وجوی نام دارایی"
                        )
                        availablePrices.filter { search.isBlank() || it.name.contains(search, ignoreCase = true) }
                            .forEach { price ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(price.name, color = Vsoft.colors.textPrimary)
                                            Text("${price.price.toLong().toMoney()} تومان · ${marketUnitLabel(price.unit)}", color = Vsoft.colors.textSecondary, fontSize = 11.sp)
                                        }
                                    },
                                    onClick = {
                                        selectedSymbol = price.symbol
                                        menuExpanded = false
                                        search = ""
                                    }
                                )
                            }
                    }
                }
                selectedPrice?.let { price ->
                    Text(
                        "قیمت هر ${marketUnitLabel(price.unit)}: ${price.price.toLong().toMoney()} تومان",
                        color = Vsoft.colors.textSecondary,
                        fontSize = 13.sp
                    )
                    if (marketState.isCached) {
                        Text("قیمت نمایش‌داده‌شده از حافظهٔ دستگاه است و برای ثبت پذیرفته نمی‌شود؛ دریافت تازه لازم است.", color = Vsoft.colors.textSecondary, fontSize = 11.sp)
                        TextButton(onClick = { vm.refreshMarketPrices() }, enabled = !marketState.isLoading) {
                            Text("تلاش برای دریافت قیمت تازه")
                        }
                    }
                }
            }
        }

        FormCard(Icons.Default.TrendingUp, "قیمت خرید هر واحد") {
            FormNumberField(
                raw = buyPriceInput,
                onChange = { buyPriceInput = it },
                placeholder = "قیمت واقعی خرید هر واحد",
                unit = "تومان",
                maxDigits = 15,
                groupThousands = true
            )
            Text(
                if (parsedQuantity > 0.0 && parsedBuyPrice > 0.0 && investedValue.isFinite())
                    "مجموع مبلغ خرید: ${investedValue.toLong().toMoney()} تومان"
                else "قیمت خرید را وارد کنید تا سود و زیان دقیق محاسبه شود.",
                color = Vsoft.colors.textSecondary,
                fontSize = 12.sp
            )
            Text("قیمت فعلی بازار: ${selectedPrice?.price?.toLong()?.toMoney() ?: "—"} تومان", color = Vsoft.colors.textSecondary, fontSize = 11.sp)
        }

        FormCard(Icons.Default.AccountBalance, "مقدار دارایی") {
            FormNumberField(
                raw = quantity,
                onChange = { quantity = it },
                placeholder = "مقدار / تعداد",
                unit = selectedPrice?.unit?.let(::marketUnitLabel) ?: selectedType?.unitLabel?.takeIf { it.isNotBlank() },
                maxDigits = 15,
                groupThousands = true
            )
            Text(
                if (selectedPrice != null && parsedQuantity > 0.0 && totalValue.isFinite())
                    "ارزش کل بر اساس قیمت فعلی: ${totalValue.toLong().toMoney()} تومان"
                else "پس از واردکردن مقدار، ارزش کل با قیمت بازار محاسبه می‌شود.",
                color = Vsoft.colors.textPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                "قیمت و ارزش کل هنگام ذخیره دوباره از منبع آنلاین بررسی می‌شوند. اگر قیمت تازه در دسترس نباشد، دارایی ذخیره نمی‌شود.",
                color = Vsoft.colors.textSecondary,
                fontSize = 11.sp
            )
        }

        FormCard(Icons.Default.AccountBalance, "یادداشت (اختیاری)") {
            FormTextField(
                value = note,
                onValueChange = { note = it.take(250) },
                placeholder = "توضیحات یا جزئیات بیشتر...",
                singleLine = false,
                minLines = 3,
                minHeight = 96.dp
            )
            Text("${note.length}/250", color = Vsoft.colors.textSecondary)
        }
    }

    if (showTypeSheet) {
        AddInvestmentTypeSheet(
            existing = state.types,
            onSave = { name, unit, icon, color ->
                pendingTypeName = name
                vm.saveType(name, unit, icon, color)
                showTypeSheet = false
            },
            onDelete = { vm.deleteType(it) },
            onDismiss = { showTypeSheet = false }
        )
    }
}
