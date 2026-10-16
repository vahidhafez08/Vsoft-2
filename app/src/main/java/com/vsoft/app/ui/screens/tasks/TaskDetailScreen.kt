package com.vsoft.app.ui.screens.tasks

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vsoft.app.ui.components.*
import com.vsoft.app.ui.theme.BrandBlue
import com.vsoft.app.ui.theme.ExpenseRed
import com.vsoft.app.ui.theme.IncomeGreen
import com.vsoft.app.ui.theme.Vsoft
import com.vsoft.app.util.formatJalali
import com.vsoft.app.util.isOverdue
import com.vsoft.app.util.toPersianText

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TaskDetailScreen(onBack: () -> Unit, viewModel: TaskDetailViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val task = state.task
    var handledMissing by rememberSaveable { mutableStateOf(false) }
    var editing by rememberSaveable { mutableStateOf(false) }
    var addingSub by rememberSaveable { mutableStateOf(false) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }

    // If the task disappears (deleted), leave this screen exactly once.
    LaunchedEffect(state.loaded, task == null) {
        if (state.loaded && task == null && !handledMissing) {
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
            .padding(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        ScreenHeader(title = "جزئیات کار", subtitle = null, onBack = onBack) {
            if (task != null) {
                Box(
                    Modifier.size(40.dp).bounceClick { confirmDelete = true }.clip(CircleShape).background(ExpenseRed.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Filled.DeleteOutline, contentDescription = "حذف کار", tint = ExpenseRed) }
            }
        }

        if (task != null) {
            val subs = state.subtasks
            val doneCount = subs.count { it.isDone }

            Appear(0) {
                VCard {
                    Text(task.title, color = Vsoft.colors.textPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(Modifier.bounceClick { viewModel.toggleTask(task) }) {
                            MiniChip(
                                text = if (task.isDone) "انجام شد" else "در حال انجام",
                                color = if (task.isDone) IncomeGreen else BrandBlue,
                                icon = if (task.isDone) Icons.Filled.CheckCircle else Icons.Filled.Schedule
                            )
                        }
                        MiniChip(priorityLabel(task.priority), priorityColor(task.priority))
                    }
                    Spacer(Modifier.height(14.dp))
                    InfoRow(
                        Icons.Filled.CalendarMonth, "موعد",
                        task.dueDateMillis?.let { formatJalali(it) } ?: "بدون موعد",
                        if (isOverdue(task.dueDateMillis, task.isDone)) ExpenseRed else Vsoft.colors.textPrimary
                    )
                    Spacer(Modifier.height(8.dp))
                    InfoRow(Icons.Filled.Folder, "پروژه", state.project?.title ?: "بدون پروژه", Vsoft.colors.textPrimary)
                }
            }

            if (subs.isNotEmpty()) {
                Appear(1) {
                    VCard {
                        val fraction = doneCount.toFloat() / subs.size
                        Row(Modifier.fillMaxWidth()) {
                            Text("پیشرفت زیروظایف", color = Vsoft.colors.textSecondary, fontSize = 13.sp, modifier = Modifier.weight(1f))
                            Text(
                                (fraction * 100).toInt().toPersianText() + "٪",
                                color = Vsoft.colors.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        AnimatedProgressBar(fraction, if (fraction >= 1f) IncomeGreen else BrandBlue)
                    }
                }
            }

            Appear(2) {
                VCard {
                    Text("توضیحات", color = Vsoft.colors.textPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        if (task.description.isBlank()) "توضیحی ثبت نشده است" else task.description,
                        color = Vsoft.colors.textSecondary, fontSize = 13.sp, lineHeight = 22.sp
                    )
                }
            }

            Appear(3) {
                VCard {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "زیروظایف (" + doneCount.toPersianText() + "/" + subs.size.toPersianText() + ")",
                            color = Vsoft.colors.textPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Box(
                            Modifier.size(34.dp).bounceClick { addingSub = true }.clip(CircleShape).background(BrandBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "افزودن زیروظیفه", tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                    if (subs.isEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text("زیروظیفه‌ای ندارد. با دکمه + یکی اضافه کنید.", color = Vsoft.colors.textSecondary, fontSize = 12.sp)
                    } else {
                        Spacer(Modifier.height(6.dp))
                        subs.forEachIndexed { index, sub ->
                            key(sub.id) {
                                if (index > 0) Box(Modifier.fillMaxWidth().height(1.dp).background(Vsoft.colors.outline))
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { viewModel.toggleSubtask(sub) }
                                        .padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TaskCheck(done = sub.isDone, onClick = { viewModel.toggleSubtask(sub) })
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        sub.title,
                                        color = if (sub.isDone) Vsoft.colors.textSecondary else Vsoft.colors.textPrimary,
                                        fontSize = 14.sp,
                                        textDecoration = if (sub.isDone) TextDecoration.LineThrough else TextDecoration.None,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Box(
                                        Modifier.size(36.dp).clip(CircleShape).clickable { viewModel.deleteSubtask(sub) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Filled.Close, contentDescription = "حذف زیروظیفه",
                                            tint = Vsoft.colors.textSecondary, modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Appear(4) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = { editing = true }, modifier = Modifier.fillMaxWidth()) { Text("ویرایش کار") }
                    OutlinedButton(onClick = { viewModel.toggleTask(task) }, modifier = Modifier.fillMaxWidth()) {
                        Text(if (task.isDone) "بازگرداندن به در حال انجام" else "علامت‌گذاری به‌عنوان انجام‌شده")
                    }
                }
            }
        }
    }

    if (editing && task != null) {
        TaskEditorSheet(
            initial = task,
            projects = state.projects,
            defaultProjectId = task.projectId,
            onDismiss = { editing = false },
            onSave = { viewModel.saveTask(it); editing = false }
        )
    }

    if (addingSub) {
        SingleInputSheet(
            title = "زیروظیفه جدید",
            label = "عنوان زیروظیفه",
            confirmLabel = "افزودن",
            onDismiss = { addingSub = false },
            onConfirm = { viewModel.addSubtask(it); addingSub = false }
        )
    }

    if (confirmDelete && task != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            containerColor = Vsoft.colors.surface,
            title = { Text("حذف کار؟", color = Vsoft.colors.textPrimary) },
            text = { Text("این کار و زیروظایفش برای همیشه حذف می‌شوند.", color = Vsoft.colors.textSecondary) },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; viewModel.deleteTask(task) }) { Text("حذف", color = ExpenseRed) }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("انصراف") } }
        )
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String, valueColor: Color) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Vsoft.colors.textSecondary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(label, color = Vsoft.colors.textSecondary, fontSize = 13.sp, modifier = Modifier.weight(1f))
        Text(value, color = valueColor, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}
