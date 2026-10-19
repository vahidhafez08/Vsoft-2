package com.vsoft.app.ui.screens.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vsoft.app.ui.components.priorityColor
import com.vsoft.app.ui.components.priorityLabel
import com.vsoft.app.ui.components.form.*
import com.vsoft.app.ui.theme.BrandBlue
import com.vsoft.app.ui.theme.Vsoft
import com.vsoft.app.util.formatJalali
import kotlinx.coroutines.flow.collect

@Composable
fun TaskFormScreen(onBack: () -> Unit, onOpenProjectForm: () -> Unit, viewModel: TaskFormViewModel = hiltViewModel()) {
    var subDraft by remember { mutableStateOf("") }
    var projectSheet by remember { mutableStateOf(false) }
    var dateSheet by remember { mutableStateOf(false) }
    var timeSheet by remember { mutableStateOf(false) }
    LaunchedEffect(viewModel) { viewModel.done.collect { onBack() } }
    FormScaffold(
        title = if (viewModel.isEditing) "ویرایش کار" else "افزودن کار",
        subtitle = "برنامه‌ریزی و پیگیری",
        icon = Icons.Filled.TaskAlt,
        onBack = onBack,
        bottomBar = { FormPrimaryButton("ذخیره کار", viewModel.title.trim().isNotBlank() && !viewModel.saving && !viewModel.loading, viewModel.saving) { viewModel.save() } }
    ) {
        FormCard(Icons.Filled.EditNote, "عنوان و توضیحات") {
            FormTextField(viewModel.title, { viewModel.title = it.take(100) }, "عنوان کار *")
            FormTextField(viewModel.description, { viewModel.description = it.take(500) }, "توضیحات (اختیاری)", singleLine = false, minLines = 3)
            FormCounterText(viewModel.description.length, 500)
        }
        FormCard(Icons.Filled.Flag, "اولویت") {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                FormTile(priorityLabel(0).removePrefix("اولویت "), Icons.Filled.Flag, priorityColor(0), viewModel.priority == 0) { viewModel.priority = 0 }
                FormTile(priorityLabel(1).removePrefix("اولویت "), Icons.Filled.Flag, priorityColor(1), viewModel.priority == 1) { viewModel.priority = 1 }
                FormTile(priorityLabel(2).removePrefix("اولویت "), Icons.Filled.PriorityHigh, priorityColor(2), viewModel.priority == 2) { viewModel.priority = 2 }
            }
        }
        FormCard(Icons.Filled.Folder, "پروژه") {
            val selected = viewModel.projects.firstOrNull { it.id == viewModel.projectId }
            FormPickerRow(Icons.Filled.Folder, selected?.title ?: "بدون پروژه", "برای اتصال این کار به یک پروژه انتخاب کن") { projectSheet = true }
            OutlinedButton(onClick = onOpenProjectForm, modifier = Modifier.fillMaxWidth()) { Text("پروژه‌ی جدید") }
        }
        FormCard(Icons.Filled.CalendarMonth, "سررسید") {
            FormSwitchRow("تعیین سررسید", "تاریخ و ساعت انجام کار", viewModel.dueEnabled) { viewModel.dueEnabled = it }
            if (viewModel.dueEnabled) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FormPickerRow(Icons.Filled.CalendarMonth, formatJalali(viewModel.dueMillis), "تاریخ", { dateSheet = true }, Modifier.weight(1f))
                    val cal = java.util.Calendar.getInstance().apply { timeInMillis = viewModel.dueMillis }
                    FormPickerRow(Icons.Filled.Schedule, FormFormat.time(cal.get(java.util.Calendar.HOUR_OF_DAY), cal.get(java.util.Calendar.MINUTE)), "ساعت", { timeSheet = true }, Modifier.weight(1f))
                }
            }
        }
        FormCard(Icons.Filled.ListAlt, "زیرکارها") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                FormTextField(subDraft, { subDraft = it.take(100) }, "عنوان زیرکار", modifier = Modifier.weight(1f))
                IconButton(onClick = { viewModel.addSubtask(subDraft); subDraft = "" }, enabled = subDraft.isNotBlank() && viewModel.draftSubtasks.size < 20, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Filled.AddCircle, contentDescription = "افزودن زیرکار", tint = BrandBlue)
                }
            }
            viewModel.draftSubtasks.forEach { (subKey, title) ->
                key(subKey) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(title, color = Vsoft.colors.textPrimary, modifier = Modifier.weight(1f))
                        IconButton(onClick = { viewModel.removeSubtask(subKey) }, modifier = Modifier.size(48.dp)) {
                            Icon(Icons.Filled.Close, contentDescription = "حذف زیرکار", tint = Vsoft.colors.textSecondary)
                        }
                    }
                }
            }
            FormCounterText(viewModel.draftSubtasks.size, 20)
        }
        FormCard(Icons.Filled.NotificationsActive, "تنظیم یادآوری") {
            FormSwitchRow("یادآوری آیتم‌ها", "یادآوری در نسخه‌های بعدی فعال می‌شود", viewModel.reminder) { viewModel.onReminderChanged(it) }
        }
    }
    if (projectSheet) {
        val options = listOf(FormOption(null, "بدون پروژه")) + viewModel.projects.map { FormOption(it.id, it.title, if (it.dueDateMillis != null) "موعد: ${formatJalali(it.dueDateMillis)}" else null) }
        FormOptionSheet("انتخاب پروژه", options, viewModel.projectId, { viewModel.projectId = it; projectSheet = false }, { projectSheet = false })
    }
    if (dateSheet) FormDatePickerSheet(viewModel.dueMillis, { viewModel.setDueDay(it); dateSheet = false }, { dateSheet = false })
    if (timeSheet) {
        val cal = java.util.Calendar.getInstance().apply { timeInMillis = viewModel.dueMillis }
        FormTimePickerSheet(cal.get(java.util.Calendar.HOUR_OF_DAY), cal.get(java.util.Calendar.MINUTE), { h, m -> viewModel.setDueTime(h, m); timeSheet = false }, { timeSheet = false })
    }
}
