package com.vsoft.app.ui.screens.jobs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vsoft.app.data.model.*
import com.vsoft.app.ui.components.*
import com.vsoft.app.ui.theme.BrandBlue
import com.vsoft.app.ui.theme.ExpenseRed
import com.vsoft.app.ui.theme.IncomeGreen
import com.vsoft.app.ui.theme.Vsoft
import com.vsoft.app.ui.theme.WarningOrange
import com.vsoft.app.util.formatJalali
import com.vsoft.app.util.toMoney
import com.vsoft.app.util.toPersianText

private val floorSuggestions = listOf("پارکینگ", "همکف", "طبقه ۱", "طبقه ۲", "طبقه ۳", "طبقه ۴", "پشت‌بام")

@Composable
fun SiteDetailScreen(onBack: () -> Unit, onEditSite: (Long) -> Unit, onAddFloor: (Long) -> Unit, onEditFloor: (Long, Long) -> Unit, onAddItem: (Long, Long) -> Unit, onEditItem: (Long, Long) -> Unit, onAddLog: (Long, Long) -> Unit, onEditLog: (Long, Long) -> Unit, onAddPayment: (Long, Long) -> Unit, viewModel: SiteDetailViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val site = state.site
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var handledMissing by rememberSaveable { mutableStateOf(false) }
    var confirmText by remember { mutableStateOf<String?>(null) }
    var confirmAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    fun ask(text: String, action: () -> Unit) {
        confirmText = text
        confirmAction = action
    }

    // If the site disappears (deleted), leave this screen exactly once.
    LaunchedEffect(state.loaded, site == null) {
        if (state.loaded && site == null && !handledMissing) {
            handledMissing = true
            onBack()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Vsoft.colors.bg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        ScreenHeader(
            title = site?.name ?: "کارگاه",
            subtitle = site?.client?.takeIf { it.isNotBlank() },
            onBack = onBack
        ) {
            if (site != null) {
                Box(
                    Modifier.size(40.dp).bounceClick { onEditSite(site.id) }.clip(CircleShape).background(BrandBlue.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Filled.Edit, contentDescription = "ویرایش کارگاه", tint = BrandBlue) }
            }
        }

        if (site != null) {
            VCard {
                Row(Modifier.fillMaxWidth()) {
                    Text("پیشرفت کل کارگاه", color = Vsoft.colors.textSecondary, fontSize = 13.sp, modifier = Modifier.weight(1f))
                    Text(
                        state.progress.toPersianText() + "٪",
                        color = Vsoft.colors.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(8.dp))
                AnimatedProgressBar(state.progress / 100f, if (state.progress >= 100) IncomeGreen else BrandBlue)
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth()) {
                    MiniStat("کل کارها", state.itemsTotal, BrandBlue, Modifier.weight(1f))
                    MiniStat("تمام‌شده", state.itemsDone, IncomeGreen, Modifier.weight(1f))
                    MiniStat("مانده", (state.itemsTotal - state.itemsDone).coerceAtLeast(0), WarningOrange, Modifier.weight(1f))
                }
            }

            SegmentedTabs(items = listOf("طبقه‌ها", "گزارش روزانه", "مالی"), selected = tab, onSelect = { tab = it })

            when (tab) {
                0 -> FloorsTab(
                    floors = state.floors,
                    onAddFloor = { onAddFloor(site.id) },
                    onAddItem = { onAddItem(site.id, it.id) },
                    onRename = { onEditFloor(site.id, it.id) },
                    onDeleteFloor = { f -> ask("طبقه «" + f.name + "» با همه‌ی کارها و گزارش‌هایش حذف شود؟") { viewModel.deleteFloor(f.id) } },
                    onLog = { onAddLog(site.id, it.id) },
                    onEditItem = { onEditItem(site.id, it.id) },
                    onDeleteItem = { i -> ask("کار «" + i.title + "» با گزارش‌هایش حذف شود؟") { viewModel.deleteItem(i.id) } }
                )
                1 -> LogsTab(
                    logs = state.logs,
                    onNewLog = { onAddLog(site.id, -1L) },
                    onEdit = { l -> onEditLog(site.id, l.id) },
                    onDelete = { l -> ask("این گزارش حذف شود؟") { viewModel.deleteLog(l.id) } }
                )
                else -> FinanceTab(
                    site = site,
                    received = state.received,
                    payments = state.payments,
                    onAddPayment = { onAddPayment(site.id, -1L) },
                    onEdit = { p -> onAddPayment(site.id, p.id) },
                    onDelete = { p -> ask("این دریافتی حذف شود؟") { viewModel.deletePayment(p.id) } }
                )
            }
        }
    }

    val text = confirmText
    val action = confirmAction
    if (text != null && action != null) {
        JobConfirmDialog(
            title = "حذف؟",
            text = text,
            onConfirm = { confirmText = null; confirmAction = null; action() },
            onDismiss = { confirmText = null; confirmAction = null }
        )
    }
}

@Composable
private fun MiniStat(label: String, value: Int, color: Color, modifier: Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, color = Vsoft.colors.textSecondary, fontSize = 11.sp)
        AnimatedMoney(value.toLong(), color = color, fontSize = 20.sp)
    }
}

@Composable
private fun SmallIconButton(icon: ImageVector, description: String, onClick: () -> Unit) {
    Box(
        Modifier.size(34.dp).clip(CircleShape).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = description, tint = Vsoft.colors.textSecondary, modifier = Modifier.size(17.dp))
    }
}

@Composable
private fun FloorsTab(
    floors: List<FloorUi>,
    onAddFloor: () -> Unit,
    onAddItem: (FloorEntity) -> Unit,
    onRename: (FloorEntity) -> Unit,
    onDeleteFloor: (FloorEntity) -> Unit,
    onLog: (WorkItemEntity) -> Unit,
    onEditItem: (WorkItemEntity) -> Unit,
    onDeleteItem: (WorkItemEntity) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        OutlinedButton(onClick = onAddFloor, modifier = Modifier.fillMaxWidth()) { Text("افزودن طبقه") }

        if (floors.isEmpty()) {
            EmptyState(
                icon = Icons.Filled.Layers,
                title = "طبقه‌ای ثبت نشده",
                subtitle = "اول طبقه‌ها را اضافه کن، بعد کارهای هر طبقه را تعریف کن"
            )
        }

        floors.forEach { fl ->
            key(fl.floor.id) {
                VCard {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            fl.floor.name, color = Vsoft.colors.textPrimary, fontSize = 16.sp,
                            fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)
                        )
                        Text(
                            fl.progress.toPersianText() + "٪",
                            color = if (fl.progress >= 100) IncomeGreen else BrandBlue,
                            fontSize = 13.sp, fontWeight = FontWeight.Bold
                        )
                        SmallIconButton(Icons.Filled.Add, "افزودن کار") { onAddItem(fl.floor) }
                        SmallIconButton(Icons.Filled.Edit, "ویرایش طبقه") { onRename(fl.floor) }
                        SmallIconButton(Icons.Filled.DeleteOutline, "حذف طبقه") { onDeleteFloor(fl.floor) }
                    }
                    Spacer(Modifier.height(8.dp))
                    AnimatedProgressBar(fl.progress / 100f, if (fl.progress >= 100) IncomeGreen else BrandBlue)

                    val remaining = fl.items.filter { !it.complete }
                    val finished = fl.items.filter { it.complete }

                    if (fl.items.isEmpty()) {
                        Spacer(Modifier.height(10.dp))
                        Text("کاری برای این طبقه تعریف نشده. با + اضافه کن.", color = Vsoft.colors.textSecondary, fontSize = 12.sp)
                    }

                    if (remaining.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "مانده (" + remaining.size.toPersianText() + ")",
                            color = WarningOrange, fontSize = 13.sp, fontWeight = FontWeight.Bold
                        )
                        remaining.forEach { ui ->
                            key(ui.item.id) {
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { onLog(ui.item) }
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(ui.item.title, color = Vsoft.colors.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                        Text(
                                            ui.done.toPersianText() + " از " + ui.item.targetQty.toPersianText() + " " + ui.item.unit +
                                                " | مانده: " + ui.remaining.toPersianText(),
                                            color = Vsoft.colors.textSecondary, fontSize = 11.sp
                                        )
                                        AnimatedProgressBar(ui.done.toFloat() / ui.item.targetQty, BrandBlue, height = 5.dp)
                                    }
                                    SmallIconButton(Icons.Filled.Edit, "ویرایش کار") { onEditItem(ui.item) }
                                    SmallIconButton(Icons.Filled.Close, "حذف کار") { onDeleteItem(ui.item) }
                                }
                            }
                        }
                    }

                    if (finished.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "انجام‌شده (" + finished.size.toPersianText() + ")",
                            color = IncomeGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold
                        )
                        finished.forEach { ui ->
                            key(ui.item.id) {
                                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = IncomeGreen, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        ui.item.title, color = Vsoft.colors.textSecondary, fontSize = 13.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        ui.done.toPersianText() + " " + ui.item.unit,
                                        color = Vsoft.colors.textSecondary, fontSize = 11.sp
                                    )
                                    SmallIconButton(Icons.Filled.Edit, "ویرایش کار") { onEditItem(ui.item) }
                                    SmallIconButton(Icons.Filled.Close, "حذف کار") { onDeleteItem(ui.item) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LogsTab(logs: List<LogUi>, onNewLog: () -> Unit, onEdit: (WorkLogEntity) -> Unit, onDelete: (WorkLogEntity) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Button(onClick = onNewLog, modifier = Modifier.fillMaxWidth()) { Text("ثبت گزارش کار") }

        if (logs.isEmpty()) {
            EmptyState(
                icon = Icons.Filled.Edit,
                title = "هنوز گزارشی ثبت نشده",
                subtitle = "هر روز بنویس چه کاری، کجا و چقدر انجام دادی"
            )
        }

        logs.groupBy { it.log.dateMillis }.forEach { (date, dayLogs) ->
            key(date) {
                VCard {
                    Text(formatJalali(date), color = Vsoft.colors.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    dayLogs.forEachIndexed { index, l ->
                        key(l.log.id) {
                            Spacer(Modifier.height(6.dp))
                            if (index > 0) Box(Modifier.fillMaxWidth().height(1.dp).background(Vsoft.colors.outline))
                            Row(Modifier.fillMaxWidth().padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        l.itemTitle + " - " + l.floorName,
                                        color = Vsoft.colors.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        l.log.qty.toPersianText() + " " + l.unit,
                                        color = IncomeGreen, fontSize = 12.sp, fontWeight = FontWeight.Medium
                                    )
                                    if (l.log.note.isNotBlank()) {
                                        Text(l.log.note, color = Vsoft.colors.textSecondary, fontSize = 12.sp)
                                    }
                                }
                                SmallIconButton(Icons.Filled.Edit, "ویرایش گزارش") { onEdit(l.log) }
                                SmallIconButton(Icons.Filled.DeleteOutline, "حذف گزارش") { onDelete(l.log) }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MoneyRow(label: String, value: Long, color: Color) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = Vsoft.colors.textSecondary, fontSize = 13.sp, modifier = Modifier.weight(1f))
        Text(value.toMoney() + " تومان", color = color, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun FinanceTab(
    site: SiteEntity,
    received: Long,
    payments: List<SitePaymentEntity>,
    onAddPayment: () -> Unit,
    onEdit: (SitePaymentEntity) -> Unit,
    onDelete: (SitePaymentEntity) -> Unit
) {
    val contract = site.contractAmount
    val remaining = (contract - received).coerceAtLeast(0L)
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        VCard {
            MoneyRow("مبلغ قرارداد", contract, Vsoft.colors.textPrimary)
            MoneyRow("دریافتی", received, IncomeGreen)
            MoneyRow("مانده از کارفرما", remaining, WarningOrange)
            Spacer(Modifier.height(8.dp))
            if (contract > 0L) {
                AnimatedProgressBar(received.toFloat() / contract, IncomeGreen)
            } else {
                Text(
                    "مبلغ قرارداد را از دکمه ویرایش بالای صفحه وارد کن.",
                    color = Vsoft.colors.textSecondary, fontSize = 12.sp
                )
            }
        }

        Button(onClick = onAddPayment, modifier = Modifier.fillMaxWidth()) { Text("ثبت دریافتی") }

        if (payments.isEmpty()) {
            EmptyState(
                icon = Icons.Filled.Payments,
                title = "دریافتی ثبت نشده",
                subtitle = "هر پولی که از کارفرما گرفتی را اینجا ثبت کن"
            )
        } else {
            VCard {
                payments.forEachIndexed { index, p ->
                    key(p.id) {
                        if (index > 0) Box(Modifier.fillMaxWidth().height(1.dp).background(Vsoft.colors.outline))
                        Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    p.amount.toMoney() + " تومان",
                                    color = IncomeGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold
                                )
                                Text(
                                    formatJalali(p.dateMillis) + if (p.note.isNotBlank()) " - " + p.note else "",
                                    color = Vsoft.colors.textSecondary, fontSize = 12.sp
                                )
                            }
                            SmallIconButton(Icons.Filled.Edit, "ویرایش دریافتی") { onEdit(p) }
                            SmallIconButton(Icons.Filled.DeleteOutline, "حذف دریافتی") { onDelete(p) }
                        }
                    }
                }
            }
        }
    }
}
