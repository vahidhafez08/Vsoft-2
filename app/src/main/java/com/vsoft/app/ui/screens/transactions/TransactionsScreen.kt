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
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vsoft.app.data.model.TransactionEntity
import com.vsoft.app.ui.components.AnimatedMoney
import com.vsoft.app.ui.components.CategoryStyles
import com.vsoft.app.ui.components.EmptyState
import com.vsoft.app.ui.components.ScreenHeader
import com.vsoft.app.ui.components.SegmentedTabs
import com.vsoft.app.ui.components.SpeedDialFab
import com.vsoft.app.ui.components.TransactionEditorSheet
import com.vsoft.app.ui.components.VCard
import com.vsoft.app.ui.theme.ExpenseRed
import com.vsoft.app.ui.theme.IncomeGreen
import com.vsoft.app.ui.theme.Vsoft
import com.vsoft.app.ui.theme.Dimens
import com.vsoft.app.util.formatTime
import com.vsoft.app.util.toMoney
import com.vsoft.app.util.toPersianText
import kotlinx.coroutines.launch
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TransactionsScreen(
    onBack: () -> Unit,
    viewModel: TransactionsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }
    var type by rememberSaveable { mutableIntStateOf(0) }
    var category by rememberSaveable { mutableStateOf<String?>(null) }
    var accountFilter by rememberSaveable { mutableStateOf<Long?>(null) }
    var newMode by rememberSaveable { mutableIntStateOf(0) } // 0 none, 1 income, 2 expense
    var editing by remember { mutableStateOf<TransactionEntity?>(null) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val focus: FocusManager = LocalFocusManager.current

    LaunchedEffect(query, type, category, accountFilter) {
        viewModel.setFilters(TxFilters(query, type, category, accountFilter))
    }

    fun deleteWithUndo(tx: TransactionEntity) {
        viewModel.delete(tx)
        scope.launch {
            snackbar.currentSnackbarData?.dismiss()
            val result = snackbar.showSnackbar(
                message = "تراکنش حذف شد",
                actionLabel = "واگرد",
                duration = SnackbarDuration.Long
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
            verticalArrangement = Arrangement.spacedBy(Dimens.listItemSpacing)
        ) {
            item(key = "header") {
                ScreenHeader(
                    title = "تراکنش‌ها",
                    subtitle = state.count.toPersianText() + " تراکنش",
                    onBack = onBack
                )
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
                                label = { Text("همه دسته‌ها") }
                            )
                        }
                        items(state.categories, key = { it }) { c ->
                            FilterChip(
                                selected = category == c,
                                onClick = { category = if (category == c) null else c },
                                label = { Text(c) }
                            )
                        }
                    }
                }
            }
            if (state.accounts.isNotEmpty()) {
                item(key = "accounts") {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item { FilterChip(selected = accountFilter == null, onClick = { accountFilter = null }, label = { Text("همه حساب‌ها") }) }
                        items(state.accounts, key = { it.id }) { account ->
                            FilterChip(selected = accountFilter == account.id, onClick = { accountFilter = if (accountFilter == account.id) null else account.id }, label = { Text(account.name) })
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
                        onClick = { editing = tx },
                        onDelete = { deleteWithUndo(tx) }
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbar,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 88.dp)
        )

        SpeedDialFab(
            onIncome = { newMode = 1 },
            onExpense = { newMode = 2 },
            modifier = Modifier.align(Alignment.BottomStart).padding(Dimens.lg)
        )
    }

    editing?.let { tx ->
        TransactionEditorSheet(
            initial = tx,
            defaultIncome = tx.isIncome,
            accounts = state.accounts,
            onDismiss = { editing = null },
            onSave = {
                viewModel.save(it)
                editing = null
            },
            onDelete = {
                deleteWithUndo(tx)
                editing = null
            }
        )
    }

    if (newMode != 0) {
        TransactionEditorSheet(
            initial = null,
            defaultIncome = newMode == 1,
            accounts = state.accounts,
            onDismiss = { newMode = 0 },
            onSave = {
                viewModel.save(it)
                newMode = 0
            }
        )
    }
}

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TxRow(
    tx: TransactionEntity,
    accountName: String?,
    modifier: Modifier,
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
    val style = CategoryStyles.of(tx.category)

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
                    .padding(horizontal = Dimens.xl),
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
                .background(Vsoft.colors.surface)
                .border(1.dp, Vsoft.colors.outline, shape)
                .clickable(onClick = onClick)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(style.color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(style.icon, contentDescription = null, tint = style.color, modifier = Modifier.size(22.dp))
            }
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
