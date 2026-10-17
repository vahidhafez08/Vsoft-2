package com.vsoft.app.ui.screens.accounts

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vsoft.app.data.model.AccountEntity
import com.vsoft.app.ui.components.AccountCardView
import com.vsoft.app.ui.components.AccountEditorSheet
import com.vsoft.app.ui.components.AnimatedMoney
import com.vsoft.app.ui.components.Appear
import com.vsoft.app.ui.components.ScreenHeader
import com.vsoft.app.ui.components.TransferSheet
import com.vsoft.app.ui.components.bounceClick
import com.vsoft.app.ui.theme.BrandBlue
import com.vsoft.app.ui.theme.BrandPurple
import com.vsoft.app.ui.theme.IncomeGreen
import com.vsoft.app.ui.theme.ExpenseRed
import com.vsoft.app.ui.theme.Vsoft
import com.vsoft.app.ui.theme.Dimens
import com.vsoft.app.util.toPersianText
import com.vsoft.app.util.formatJalali
import com.vsoft.app.util.toMoney
import com.vsoft.app.util.toPersianDigits

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AccountsScreen(
    onBack: () -> Unit,
    viewModel: AccountsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var adding by rememberSaveable { mutableStateOf(false) }
    var transferring by rememberSaveable { mutableStateOf(false) }
    var editing by remember { mutableStateOf<AccountEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Vsoft.colors.bg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Dimens.screenHorizontal)
            .padding(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        ScreenHeader(
            title = "کارت‌ها و حساب‌ها",
            subtitle = state.accounts.size.toPersianText() + " حساب فعال",
            onBack = onBack
        ) {
            Box(
                Modifier
                    .size(40.dp)
                    .bounceClick { adding = true }
                    .clip(CircleShape)
                    .background(BrandBlue),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Add, contentDescription = "حساب جدید", tint = Color.White)
            }
        }

        Appear(0) { TotalCard(state.total, state.accounts.size) }

        Appear(1) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ActionPill("انتقال وجه", Icons.Filled.SwapHoriz, BrandBlue, Modifier.weight(1f)) {
                    transferring = true
                }
                ActionPill("حساب جدید", Icons.Filled.Add, IncomeGreen, Modifier.weight(1f)) {
                    adding = true
                }
            }
        }

        state.accounts.forEachIndexed { index, acc ->
            key(acc.id) {
                Appear(index + 2) {
                    AccountCardView(
                        name = acc.name,
                        last4 = acc.last4,
                        balance = state.balances[acc.id] ?: acc.balance,
                        colorKey = acc.colorKey,
                        modifier = Modifier.bounceClick { editing = acc }
                    )
                }
            }
        }

        Appear(state.accounts.size + 2) {
            AddDashedCard { adding = true }
        }

        Text("آخرین انتقال‌ها", color = Vsoft.colors.textPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        if (state.transfers.isEmpty()) {
            Text("هنوز انتقالی ثبت نشده", color = Vsoft.colors.textSecondary, fontSize = 13.sp, modifier = Modifier.fillMaxWidth().padding(vertical = 18.dp))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 480.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.transfers.take(10), key = { it.id }) { transfer ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { value ->
                            if (value != SwipeToDismissBoxValue.Settled) {
                                viewModel.deleteTransfer(transfer) { viewModel.restoreTransfer(transfer) }
                                true
                            } else false
                        }
                    )
                    SwipeToDismissBox(
                        state = dismissState,
                        modifier = Modifier.animateItemPlacement(),
                        backgroundContent = {
                            Row(
                                Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(ExpenseRed).padding(Dimens.lg),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Delete, contentDescription = "حذف انتقال", tint = Color.White)
                            }
                        }
                    ) {
                        Column(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Vsoft.colors.surfaceHigh).padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                (state.accountNames[transfer.fromAccountId] ?: "حساب حذف‌شده") + " ← " +
                                    (state.accountNames[transfer.toAccountId] ?: "حساب حذف‌شده"),
                                color = Vsoft.colors.textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(transfer.amount.toMoney() + " تومان", color = BrandBlue, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(formatJalali(transfer.dateMillis).toPersianDigits(), color = Vsoft.colors.textSecondary, fontSize = 11.sp)
                            if (transfer.note.isNotBlank()) Text(transfer.note, color = Vsoft.colors.textSecondary, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    if (adding) {
        AccountEditorSheet(
            initial = null,
            onDismiss = { adding = false },
            onSave = {
                viewModel.save(it)
                adding = false
            }
        )
    }

    editing?.let { acc ->
        AccountEditorSheet(
            initial = acc,
            onDismiss = { editing = null },
            onSave = {
                viewModel.save(it)
                editing = null
            },
            onDelete = {
                viewModel.delete(acc)
                editing = null
            }
        )
    }

    if (transferring) {
        TransferSheet(
            accounts = state.accounts,
            balances = state.balances,
            onDismiss = { transferring = false },
            onTransfer = { fromId, toId, amount, dateMillis, note ->
                viewModel.addTransfer(fromId, toId, amount, dateMillis, note)
                transferring = false
            }
        )
    }
}

@Composable
private fun TotalCard(total: Long, count: Int) {
    val shape = RoundedCornerShape(24.dp)
    Column(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Brush.linearGradient(listOf(BrandBlue, BrandPurple)))
            .drawBehind {
                drawCircle(
                    Color.White.copy(alpha = 0.08f),
                    radius = size.height * 0.9f,
                    center = Offset(size.width * 0.1f, -size.height * 0.1f)
                )
                drawCircle(
                    Color.White.copy(alpha = 0.06f),
                    radius = size.height * 0.55f,
                    center = Offset(size.width * 0.95f, size.height)
                )
            }
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text("مجموع موجودی", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
        Row(verticalAlignment = Alignment.Bottom) {
            AnimatedMoney(total, color = Color.White, fontSize = 30.sp)
            Spacer(Modifier.width(6.dp))
            Text(
                "تومان",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        Text(
            count.toPersianText() + " کارت و حساب فعال",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 12.sp
        )
    }
}

@Composable
private fun ActionPill(
    label: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier
            .bounceClick(onClick)
            .clip(shape)
            .background(tint.copy(alpha = 0.12f))
            .border(1.dp, tint.copy(alpha = 0.3f), shape)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(label, color = tint, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun AddDashedCard(onClick: () -> Unit) {
    val dashColor = Vsoft.colors.textSecondary.copy(alpha = 0.5f)
    val shape = RoundedCornerShape(Dimens.cornerCard)
    Box(
        Modifier
            .fillMaxWidth()
            .bounceClick(onClick)
            .clip(shape)
            .drawBehind {
                val sw = 2.dp.toPx()
                drawRoundRect(
                    color = dashColor,
                    topLeft = Offset(sw / 2f, sw / 2f),
                    size = Size(size.width - sw, size.height - sw),
                    cornerRadius = CornerRadius(20.dp.toPx(), 20.dp.toPx()),
                    style = Stroke(width = sw, pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 12f), 0f))
                )
            }
            .padding(vertical = 22.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Add, contentDescription = null, tint = Vsoft.colors.textSecondary)
            Spacer(Modifier.width(8.dp))
            Text("افزودن کارت یا حساب جدید", color = Vsoft.colors.textSecondary, fontSize = 13.sp)
        }
    }
}
