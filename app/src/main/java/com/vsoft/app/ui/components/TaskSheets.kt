package com.vsoft.app.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.vsoft.app.data.model.ProjectEntity
import com.vsoft.app.data.model.TaskEntity
import com.vsoft.app.ui.theme.ExpenseRed
import com.vsoft.app.ui.theme.Vsoft
import com.vsoft.app.util.DAY_MS
import com.vsoft.app.util.formatJalali
import com.vsoft.app.util.startOfDay

private val taskDueLabels = listOf("بدون موعد", "امروز", "فردا", "۳ روز دیگر", "یک هفته دیگر")
private val taskDueOffsets = listOf<Int?>(null, 0, 1, 3, 7)
private val projectDueLabels = listOf("بدون موعد", "دو هفته دیگر", "یک ماه دیگر", "سه ماه دیگر")
private val projectDueOffsets = listOf<Int?>(null, 14, 30, 90)

/** idx == null means "keep the existing due date". */
private fun resolveDue(idx: Int?, offsets: List<Int?>, current: Long?): Long? =
    if (idx == null) current else offsets[idx]?.let { startOfDay(System.currentTimeMillis()) + it * DAY_MS }

private fun dueTitle(idx: Int?, current: Long?, default: String): String =
    if (idx == null && current != null) "موعد فعلی: " + formatJalali(current) else default

@Composable
fun TaskEditorSheet(
    initial: TaskEntity?,
    projects: List<ProjectEntity>,
    defaultProjectId: Long?,
    onDismiss: () -> Unit,
    onSave: (TaskEntity) -> Unit
) {
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var desc by remember { mutableStateOf(initial?.description ?: "") }
    var priority by remember { mutableIntStateOf(initial?.priority ?: 1) }
    var projectId by remember { mutableStateOf(initial?.projectId ?: defaultProjectId) }
    var dueIndex by remember { mutableStateOf<Int?>(if (initial?.dueDateMillis != null) null else 0) }

    FormSheet(onDismiss) {
        SheetTitle(if (initial == null) "کار جدید" else "ویرایش کار")
        OutlinedTextField(
            value = title, onValueChange = { title = it.take(60) },
            label = { Text("عنوان کار") }, singleLine = true, modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = desc, onValueChange = { desc = it.take(400) },
            label = { Text("توضیحات (اختیاری)") }, minLines = 2, maxLines = 4, modifier = Modifier.fillMaxWidth()
        )
        SheetLabel("اولویت")
        ChipGroup(listOf(0, 1, 2).map { priorityLabel(it) }, { priority == it }, { priority = it })
        SheetLabel("پروژه")
        ChipGroup(
            labels = listOf("بدون پروژه") + projects.map { it.title },
            isSelected = { i -> if (i == 0) projectId == null else projectId == projects[i - 1].id },
            onSelect = { i -> projectId = if (i == 0) null else projects[i - 1].id }
        )
        SheetLabel(dueTitle(dueIndex, initial?.dueDateMillis, "موعد"))
        ChipGroup(taskDueLabels, { dueIndex == it }, { dueIndex = it })
        Button(
            onClick = {
                onSave(
                    TaskEntity(
                        id = initial?.id ?: 0L,
                        title = title.trim(),
                        description = desc.trim(),
                        isDone = initial?.isDone ?: false,
                        priority = priority,
                        projectId = projectId,
                        dueDateMillis = resolveDue(dueIndex, taskDueOffsets, initial?.dueDateMillis)
                    )
                )
            },
            enabled = title.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (initial == null) "افزودن کار" else "ذخیره تغییرات") }
    }
}

@Composable
fun ProjectEditorSheet(
    initial: ProjectEntity?,
    onDismiss: () -> Unit,
    onSave: (ProjectEntity) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var dueIndex by remember { mutableStateOf<Int?>(if (initial?.dueDateMillis != null) null else 0) }
    var confirmDelete by remember { mutableStateOf(false) }

    FormSheet(onDismiss) {
        SheetTitle(if (initial == null) "پروژه جدید" else "ویرایش پروژه")
        OutlinedTextField(
            value = title, onValueChange = { title = it.take(40) },
            label = { Text("نام پروژه") }, singleLine = true, modifier = Modifier.fillMaxWidth()
        )
        SheetLabel(dueTitle(dueIndex, initial?.dueDateMillis, "موعد پروژه"))
        ChipGroup(projectDueLabels, { dueIndex == it }, { dueIndex = it })
        Button(
            onClick = {
                onSave(
                    ProjectEntity(
                        id = initial?.id ?: 0L,
                        title = title.trim(),
                        progress = initial?.progress ?: 0,
                        dueDateMillis = resolveDue(dueIndex, projectDueOffsets, initial?.dueDateMillis)
                    )
                )
            },
            enabled = title.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) { Text(if (initial == null) "افزودن پروژه" else "ذخیره تغییرات") }
        if (onDelete != null) {
            TextButton(onClick = { confirmDelete = true }, modifier = Modifier.fillMaxWidth()) {
                Text("حذف پروژه", color = ExpenseRed)
            }
        }
    }

    if (confirmDelete && onDelete != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            containerColor = Vsoft.colors.surface,
            title = { Text("حذف پروژه؟", color = Vsoft.colors.textPrimary) },
            text = { Text("پروژه حذف می‌شود ولی کارهای آن باقی می‌مانند.", color = Vsoft.colors.textSecondary) },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; onDelete() }) { Text("حذف", color = ExpenseRed) }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("انصراف") } }
        )
    }
}
