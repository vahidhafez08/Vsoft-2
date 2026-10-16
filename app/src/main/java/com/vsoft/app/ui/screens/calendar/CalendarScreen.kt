package com.vsoft.app.ui.screens.calendar

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vsoft.app.data.model.TaskEntity
import com.vsoft.app.ui.components.*
import com.vsoft.app.ui.theme.BrandBlue
import com.vsoft.app.ui.theme.ExpenseRed
import com.vsoft.app.ui.theme.IncomeGreen
import com.vsoft.app.ui.theme.Vsoft
import com.vsoft.app.util.JCal
import com.vsoft.app.util.formatJalali
import com.vsoft.app.util.startOfDay

@Composable
fun CalendarScreen(
    onBack: () -> Unit,
    onOpenTask: (Long) -> Unit,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val data by viewModel.data.collectAsStateWithLifecycle()
    val todayStart = remember { startOfDay(System.currentTimeMillis()) }
    val todayJ = remember { JCal.toJalali(todayStart) }
    var jy by rememberSaveable { mutableIntStateOf(todayJ[0]) }
    var jm by rememberSaveable { mutableIntStateOf(todayJ[1]) }
    var selected by rememberSaveable { mutableLongStateOf(todayStart) }
    var adding by rememberSaveable { mutableStateOf(false) }

    val tasksByDay = remember(data) {
        data.tasks
            .mapNotNull { t -> t.dueDateMillis?.let { startOfDay(it) to t } }
            .groupBy({ it.first }, { it.second })
    }
    val logsByDay = remember(data) { data.logs.groupBy { it.dayMillis } }

    fun shift(delta: Int) {
        val idx = (jy * 12 + (jm - 1) + delta).coerceIn(1300 * 12, 1700 * 12 + 11)
        jy = idx / 12
        jm = idx % 12 + 1
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
        ScreenHeader(title = "تقویم", subtitle = "کارها و گزارش‌های هر روز", onBack = onBack) {}

        VCard {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { shift(-1) }) {
                    Icon(Icons.Filled.ChevronRight, contentDescription = "ماه قبل", tint = Vsoft.colors.textPrimary)
                }
                Text(
                    JCal.monthNames[(jm - 1).coerceIn(0, 11)] + " " + JCal.fa(jy),
                    color = Vsoft.colors.textPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = {
                    jy = todayJ[0]
                    jm = todayJ[1]
                    selected = todayStart
                }) { Text("امروز") }
                IconButton(onClick = { shift(1) }) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = "ماه بعد", tint = Vsoft.colors.textPrimary)
                }
            }
            Spacer(Modifier.height(8.dp))
            AnimatedContent(
                targetState = jy * 12 + (jm - 1),
                transitionSpec = { fadeIn(tween(200)).togetherWith(fadeOut(tween(120))) },
                label = "calendarMonth"
            ) { ym ->
                MonthGrid(
                    jy = ym / 12,
                    jm = ym % 12 + 1,
                    todayStart = todayStart,
                    selected = selected,
                    tasksByDay = tasksByDay,
                    logsByDay = logsByDay,
                    onSelect = { selected = it }
                )
            }
        }

        val dayTasks = tasksByDay[selected].orEmpty()
        val dayLogs = logsByDay[selected].orEmpty()
        val weekday = JCal.weekdayNames[JCal.weekdayIndex(selected).coerceIn(0, 6)]

        VCard {
            Text(
                weekday + "، " + formatJalali(selected),
                color = Vsoft.colors.textPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(10.dp))

            Text("کارهای این روز", color = BrandBlue, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            if (dayTasks.isEmpty()) {
                Spacer(Modifier.height(6.dp))
                Text("کاری با موعد این روز ثبت نشده.", color = Vsoft.colors.textSecondary, fontSize = 12.sp)
            } else {
                dayTasks.forEach { task ->
                    key(task.id) {
                        DayTaskRow(
                            task = task,
                            onToggle = { viewModel.toggle(task) },
                            onOpen = { onOpenTask(task.id) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            Text("گزارش شغل این روز", color = IncomeGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            if (dayLogs.isEmpty()) {
                Spacer(Modifier.height(6.dp))
                Text("گزارشی برای این روز ثبت نشده.", color = Vsoft.colors.textSecondary, fontSize = 12.sp)
            } else {
                dayLogs.forEach { l ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Construction, contentDescription = null, tint = IncomeGreen, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                l.title + " - " + JCal.fa(l.qty) + " " + l.unit,
                                color = Vsoft.colors.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium
                            )
                            if (l.note.isNotBlank()) {
                                Text(l.note, color = Vsoft.colors.textSecondary, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            Button(onClick = { adding = true }, modifier = Modifier.fillMaxWidth()) {
                Text("کار جدید برای این روز")
            }
        }
    }

    if (adding) {
        SingleInputSheet(
            title = "کار جدید",
            label = "عنوان کار",
            confirmLabel = "افزودن",
            onDismiss = { adding = false },
            onConfirm = {
                viewModel.addTask(it, selected)
                adding = false
            }
        )
    }
}

@Composable
private fun DayTaskRow(task: TaskEntity, onToggle: () -> Unit, onOpen: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onOpen),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TaskCheck(done = task.isDone, onClick = onToggle)
        Spacer(Modifier.width(6.dp))
        Text(
            task.title,
            color = if (task.isDone) Vsoft.colors.textSecondary else Vsoft.colors.textPrimary,
            fontSize = 14.sp,
            textDecoration = if (task.isDone) TextDecoration.LineThrough else TextDecoration.None,
            modifier = Modifier.weight(1f)
        )
        MiniChip(priorityLabel(task.priority), priorityColor(task.priority))
    }
}

@Composable
private fun MonthGrid(
    jy: Int,
    jm: Int,
    todayStart: Long,
    selected: Long,
    tasksByDay: Map<Long, List<TaskEntity>>,
    logsByDay: Map<Long, List<CalLog>>,
    onSelect: (Long) -> Unit
) {
    val starts = remember(jy, jm) {
        List(JCal.monthLength(jy, jm)) { startOfDay(JCal.fromJalali(jy, jm, it + 1)) }
    }
    val offset = remember(starts) { JCal.weekdayIndex(starts.firstOrNull() ?: todayStart) }
    val rows = (offset + starts.size + 6) / 7

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(Modifier.fillMaxWidth()) {
            JCal.dayHeaders.forEachIndexed { i, h ->
                Text(
                    h,
                    color = if (i == 6) ExpenseRed else Vsoft.colors.textSecondary,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        for (r in 0 until rows) {
            Row(Modifier.fillMaxWidth()) {
                for (c in 0 until 7) {
                    val idx = r * 7 + c - offset
                    val day = starts.getOrNull(idx)
                    if (day == null) {
                        Spacer(Modifier.weight(1f).aspectRatio(1f))
                    } else {
                        DayCell(
                            label = JCal.fa(idx + 1),
                            isToday = day == todayStart,
                            isSelected = day == selected,
                            isFriday = c == 6,
                            hasTask = tasksByDay[day].orEmpty().isNotEmpty(),
                            hasLog = logsByDay[day].orEmpty().isNotEmpty(),
                            onClick = { onSelect(day) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.DayCell(
    label: String,
    isToday: Boolean,
    isSelected: Boolean,
    isFriday: Boolean,
    hasTask: Boolean,
    hasLog: Boolean,
    onClick: () -> Unit
) {
    val bg by animateColorAsState(if (isSelected) BrandBlue else Color.Transparent, label = "calCellBg")
    Box(
        Modifier
            .weight(1f)
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(bg)
            .then(if (isToday && !isSelected) Modifier.border(1.5.dp, BrandBlue, CircleShape) else Modifier)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(
                label,
                color = when {
                    isSelected -> Color.White
                    isFriday -> ExpenseRed
                    else -> Vsoft.colors.textPrimary
                },
                fontSize = 14.sp,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
            )
            Row(Modifier.height(6.dp), horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
                if (hasTask) Dot(if (isSelected) Color.White else BrandBlue)
                if (hasLog) Dot(if (isSelected) Color.White else IncomeGreen)
            }
        }
    }
}

@Composable
private fun Dot(color: Color) {
    Box(Modifier.size(4.dp).clip(CircleShape).background(color))
}
