package com.vsoft.app.ui.screens.transactions

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.clickable
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.combinedClickable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import com.vsoft.app.util.formatJalali
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vsoft.app.data.model.TransactionEntity
import com.vsoft.app.ui.components.AccountLogo
import com.vsoft.app.ui.components.AnimatedMoney
import com.vsoft.app.ui.components.CategoryLogo
import com.vsoft.app.ui.components.EmptyState
import com.vsoft.app.ui.components.ScreenHeader
import com.vsoft.app.ui.components.SegmentedTabs
import com.vsoft.app.ui.components.SpeedDialFab
import com.vsoft.app.ui.components.TransactionEditorSheet
import com.vsoft.app.ui.components.VCard
import com.vsoft.app.ui.theme.ExpenseRed
import com.vsoft.app.ui.theme.IncomeGreen
import com.vsoft.app.ui.theme.Vsoft
import com.vsoft.app.util.formatTime
import com.vsoft.app.util.toMoney
import com.vsoft.app.util.toPersianText
import kotlinx.coroutines.launch
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TransactionsScreen(
    onBack: () -> Unit,
    onOpenTransactionForm: (Long, String) -> Unit = { _, _ -> },
    viewModel: TransactionsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }
    var type by rememberSaveable { mutableIntStateOf(0) }
    var category by rememberSaveable { mutableStateOf<String?>(null) }
    var accountFilter by rememberSaveable { mutableStateOf<Long?>(null) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val focus: FocusManager = LocalFocusManager.current
    val context = LocalContext.current
    val selectedIds = remember { androidx.compose.runtime.mutableStateListOf<Long>() }
    var invoiceFile by remember { mutableStateOf<File?>(null) }
    var invoiceImageFile by remember { mutableStateOf<File?>(null) }
    var showInvoiceActions by remember { mutableStateOf(false) }
    val saveInvoiceLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri: Uri? ->
        val source = invoiceFile
        if (uri != null && source != null) {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use { output -> source.inputStream().use { input -> input.copyTo(output) } }
                    ?: error("فایل مقصد باز نشد")
            }.onSuccess { com.vsoft.app.util.UiEvents.show("فاکتور ذخیره شد") }
                .onFailure { com.vsoft.app.util.UiEvents.show("ذخیره فاکتور ناموفق بود") }
        }
    }
    val saveInvoiceImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("image/png")) { uri: Uri? ->
        val source = invoiceImageFile
        if (uri != null && source != null) {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use { output -> source.inputStream().use { input -> input.copyTo(output) } }
                    ?: error("فایل مقصد باز نشد")
            }.onSuccess { com.vsoft.app.util.UiEvents.show("تصویر فاکتور ذخیره شد") }
                .onFailure { com.vsoft.app.util.UiEvents.show("ذخیره تصویر فاکتور ناموفق بود") }
        }
    }
    val visibleTransactions = state.groups.flatMap { it.items }
    val selectedTransactions = visibleTransactions.filter { it.id in selectedIds }
    fun toggleSelection(tx: TransactionEntity) {
        if (tx.id in selectedIds) selectedIds.remove(tx.id) else selectedIds.add(tx.id)
    }
    fun createInvoice() {
        if (selectedTransactions.isEmpty()) return
        runCatching {
            val darkTheme = Vsoft.colors.isDark
            val pdf = InvoicePdfGenerator.create(context, selectedTransactions, darkTheme)
            val image = InvoiceImageGenerator.create(context, selectedTransactions, darkTheme)
            pdf to image
        }
            .onSuccess { (pdf, image) ->
                invoiceFile = pdf
                invoiceImageFile = image
                showInvoiceActions = true
                selectedIds.clear()
            }
            .onFailure { com.vsoft.app.util.UiEvents.show("ساخت فاکتور ناموفق بود") }
    }

    LaunchedEffect(query, type, category, accountFilter) {
        viewModel.setFilters(TxFilters(query, type, category, accountFilter))
    }

    fun deleteWithUndo(tx: TransactionEntity) {
        viewModel.delete(tx)
        scope.launch {
            snackbar.currentSnackbarData?.dismiss()
            val result = snackbar.showSnackbar(
                message = "تراکنش حذف شد",
                actionLabel = "بازگردانی",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.restore(tx)
            }
        }
    }

    Box(Modifier.fillMaxSize().background(Vsoft.colors.bg)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item(key = "header") {
                if (selectedIds.isEmpty()) {
                    ScreenHeader(
                        title = "تراکنش‌ها",
                        subtitle = state.count.toPersianText() + " تراکنش",
                        onBack = onBack
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { selectedIds.clear() }) { Icon(Icons.Filled.Close, contentDescription = "لغو انتخاب") }
                            Text(selectedIds.size.toPersianText() + " تراکنش انتخاب‌شده", Modifier.weight(1f), color = Vsoft.colors.textPrimary, fontWeight = FontWeight.Bold)
                            TextButton(onClick = {
                                selectedIds.clear()
                                selectedIds.addAll(visibleTransactions.map { it.id })
                            }) { Text("انتخاب همه") }
                            TextButton(onClick = { selectedIds.clear() }) { Text("لغو انتخاب") }
                        }
                    }
                }
            }
            item(key = "summary") {
                val net = state.income - state.expense
                VCard {
                    Row(Modifier.fillMaxWidth()) {
                        SummaryCell("درآمد", state.income, IncomeGreen, Modifier.weight(1f))
                        SummaryCell("هزینه", state.expense, ExpenseRed, Modifier.weight(1f))
                        SummaryCell("تراز", net, if (net >= 0) IncomeGreen else ExpenseRed, Modifier.weight(1f))
                    }
                }
            }
            item(key = "search") {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it.take(40) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    placeholder = { Text("جستجو در تراکنش‌ها...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = {
                        AnimatedVisibility(
                            visible = query.isNotEmpty(),
                            enter = fadeIn() + scaleIn(),
                            exit = fadeOut() + scaleOut()
                        ) {
                            IconButton(onClick = { query = "" }) {
                                Icon(Icons.Filled.Close, contentDescription = "پاک کردن")
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focus.clearFocus() })
                )
            }
            item(key = "type") {
                SegmentedTabs(
                    items = listOf("همه", "درآمد", "هزینه"),
                    selected = type,
                    onSelect = {
                        type = it
                        category = null
                    }
                )
            }
            if (state.categories.isNotEmpty()) {
                item(key = "cats") {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChip(
                                selected = category == null,
                                onClick = { category = null },
                                leadingIcon = { Icon(Icons.Filled.GridView, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                label = { Text("همه دسته‌ها") }
                            )
                        }
                        items(state.categories, key = { it }) { c ->
                            FilterChip(
                                selected = category == c,
                                onClick = { category = if (category == c) null else c },
                                leadingIcon = { CategoryLogo(c, 24.dp) },
                                label = { Text(c) }
                            )
                        }
                    }
                }
            }
            if (state.accounts.isNotEmpty()) {
                item(key = "accounts") {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChip(
                                selected = accountFilter == null,
                                onClick = { accountFilter = null },
                                leadingIcon = { AccountLogo("blue", 24.dp) },
                                label = { Text("همه حساب‌ها") }
                            )
                        }
                        items(state.accounts, key = { it.id }) { account ->
                            FilterChip(
                                selected = accountFilter == account.id,
                                onClick = { accountFilter = if (accountFilter == account.id) null else account.id },
                                leadingIcon = { AccountLogo(account.colorKey, 24.dp) },
                                label = { Text(account.name) }
                            )
                        }
                    }
                }
            }
            if (state.loaded && state.groups.isEmpty()) {
                item(key = "empty") {
                    val noFilters = query.isBlank() && type == 0 && category == null && accountFilter == null
                    EmptyState(
                        icon = if (noFilters) Icons.Filled.Inbox else Icons.Filled.SearchOff,
                        title = if (noFilters) "هنوز تراکنشی ثبت نشده" else "نتیجه‌ای پیدا نشد",
                        subtitle = "با دکمه + تراکنش جدید ثبت کنید یا فیلترها را تغییر دهید"
                    )
                }
            }
            state.groups.forEach { group ->
                item(key = "day" + group.key) {
                    DayHeader(group.label, group.net, Modifier.animateItemPlacement())
                }
                items(group.items, key = { "tx" + it.id }) { tx ->
                    TxRow(
                        tx = tx,
                        accountName = tx.accountId?.let { state.accountNames[it] },
                        modifier = Modifier.animateItemPlacement(),
                        selected = tx.id in selectedIds,
                        selectionMode = selectedIds.isNotEmpty(),
                        onLongClick = { if (selectedIds.isEmpty()) selectedIds.add(tx.id) else toggleSelection(tx) },
                        onClick = {
                            if (selectedIds.isNotEmpty()) toggleSelection(tx)
                            else onOpenTransactionForm(tx.id, if (tx.isIncome) "income" else "expense")
                        },
                        onDelete = { deleteWithUndo(tx) }
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbar,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 88.dp)
        )

        if (selectedIds.isEmpty()) {
            SpeedDialFab(
                onIncome = { onOpenTransactionForm(-1L, "income") },
                onExpense = { onOpenTransactionForm(-1L, "expense") },
                modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)
            )
        } else {
            InvoiceGlassFab(
                count = selectedIds.size,
                onClick = { createInvoice() },
                modifier = Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 92.dp)
            )
        }
    }

    if (showInvoiceActions && invoiceFile != null && invoiceImageFile != null) {
        fun share(file: File?, mime: String, chooserTitle: String) {
            val f = file ?: return
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", f)
            val intent = Intent(Intent.ACTION_SEND).apply {
                setType(mime)
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, chooserTitle))
            showInvoiceActions = false
        }
        InvoiceActionsSheet(
            onSavePdf = { saveInvoiceLauncher.launch("فاکتور-${System.currentTimeMillis()}.pdf"); showInvoiceActions = false },
            onSharePdf = { share(invoiceFile, "application/pdf", "اشتراک‌گذاری PDF") },
            onSaveImage = { saveInvoiceImageLauncher.launch("فاکتور-${System.currentTimeMillis()}.png"); showInvoiceActions = false },
            onShareImage = { share(invoiceImageFile, "image/png", "اشتراک‌گذاری تصویر فاکتور") },
            onDismiss = { showInvoiceActions = false }
        )
    }}

@Composable
private fun SummaryCell(label: String, value: Long, color: Color, modifier: Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, color = Vsoft.colors.textSecondary, fontSize = 11.sp)
        AnimatedMoney(value, color = color, fontSize = 13.sp)
        Text("تومان", color = Vsoft.colors.textSecondary, fontSize = 10.sp)
    }
}

@Composable
private fun DayHeader(label: String, net: Long, modifier: Modifier) {
    Row(modifier.fillMaxWidth().padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(
            label,
            color = Vsoft.colors.textSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Text(
            (if (net >= 0) "+" else "−") + abs(net).toMoney(),
            color = if (net >= 0) IncomeGreen else ExpenseRed,
            fontSize = 12.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun TxRow(
    tx: TransactionEntity,
    accountName: String?,
    modifier: Modifier,
    selected: Boolean,
    selectionMode: Boolean,
    onLongClick: () -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val currentDelete by rememberUpdatedState(onDelete)
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value != SwipeToDismissBoxValue.Settled) {
                currentDelete()
                true
            } else {
                false
            }
        }
    )
    val shape = RoundedCornerShape(18.dp)

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        backgroundContent = {
            val armed = dismissState.targetValue != SwipeToDismissBoxValue.Settled
            val bg by animateColorAsState(
                targetValue = if (armed) ExpenseRed else ExpenseRed.copy(alpha = 0.14f),
                label = "swipeBg"
            )
            val iconTint by animateColorAsState(
                targetValue = if (armed) Color.White else ExpenseRed,
                label = "swipeIcon"
            )
            Row(
                Modifier
                    .fillMaxSize()
                    .clip(shape)
                    .background(bg)
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(Icons.Filled.Delete, contentDescription = null, tint = iconTint)
                Icon(Icons.Filled.Delete, contentDescription = null, tint = iconTint)
            }
        }
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(if (selected) IncomeGreen.copy(alpha = 0.12f) else Vsoft.colors.surface)
                .border(if (selected) 2.dp else 1.dp, if (selected) IncomeGreen else Vsoft.colors.outline, shape)
                .combinedClickable(onClick = onClick, onLongClick = onLongClick)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selectionMode) {
                Box(Modifier.size(24.dp).clip(RoundedCornerShape(8.dp)).background(if (selected) IncomeGreen else Vsoft.colors.surfaceHigh).border(1.dp, if (selected) IncomeGreen else Vsoft.colors.outline, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                    if (selected) Icon(Icons.Filled.Close, null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
                Spacer(Modifier.width(8.dp))
            }
            CategoryLogo(tx.category, 44.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    tx.title,
                    color = Vsoft.colors.textPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    tx.category + " • " + formatTime(tx.dateMillis),
                    color = Vsoft.colors.textSecondary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (accountName != null) {
                    Text(accountName, color = Vsoft.colors.textSecondary, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    (if (tx.isIncome) "+" else "−") + tx.amount.toMoney(),
                    color = if (tx.isIncome) IncomeGreen else ExpenseRed,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text("تومان", color = Vsoft.colors.textSecondary, fontSize = 10.sp)
            }
        }
    }
}


@Composable
private fun InvoiceGlassFab(count: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = Vsoft.colors
    val shape = RoundedCornerShape(50)
    Row(
        modifier.clip(shape)
            .background(Brush.linearGradient(
                if (colors.isDark) listOf(Color(0xFF263C69), Color(0xFF463B79))
                else listOf(Color(0xFF527CEB), Color(0xFF7664D9))
            ))
            .border(1.dp, Color.White.copy(alpha = 0.45f), shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 19.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Icon(Icons.Filled.Receipt, contentDescription = null, tint = Color.White, modifier = Modifier.size(21.dp))
        Text("ساخت فاکتور", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Box(Modifier.size(24.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
            Text(count.toString(), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}
