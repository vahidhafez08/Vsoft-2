package com.vsoft.app.ui.screens.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vsoft.app.ui.components.form.*
import com.vsoft.app.util.formatJalali
import kotlinx.coroutines.flow.collect

@Composable
fun ProjectFormScreen(onBack: () -> Unit, viewModel: ProjectFormViewModel = hiltViewModel()) {
    var dateSheet by remember { mutableStateOf(false) }\n    var confirmDelete by remember { mutableStateOf(false) }
    LaunchedEffect(viewModel) { viewModel.done.collect { onBack() } }
    FormScaffold(
        title = if (viewModel.isEditing) "ویرایش پروژه" else "افزودن پروژه",
        subtitle = "مدیریت کارها و موعد پروژه",
        icon = Icons.Filled.Folder,
        onBack = onBack,
        bottomBar = { FormPrimaryButton("ذخیره پروژه", viewModel.title.trim().isNotBlank() && !viewModel.saving && !viewModel.loading, viewModel.saving) { viewModel.save() } }
    ) {
        FormCard(Icons.Filled.Folder, "مشخصات پروژه") {
            FormTextField(viewModel.title, { viewModel.title = it.take(60) }, "نام پروژه *")
            FormSwitchRow("تعیین موعد پروژه", "موعد اختیاری", viewModel.dueEnabled) { viewModel.dueEnabled = it }
            if (viewModel.dueEnabled) FormPickerRow(Icons.Filled.CalendarMonth, formatJalali(viewModel.dueMillis), "تاریخ موعد", { dateSheet = true })\n            if (viewModel.isEditing) OutlinedButton(onClick = { confirmDelete = true }, modifier = Modifier.fillMaxWidth(), enabled = !viewModel.saving) { Text("حذف پروژه") }
        }
    }
    if (confirmDelete) AlertDialog(onDismissRequest = { confirmDelete = false }, title = { Text("حذف پروژه؟") }, text = { Text("کارهای متصل به پروژه باقی می‌مانند.") }, confirmButton = { TextButton(onClick = { confirmDelete = false; viewModel.delete() }) { Text("حذف") } }, dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("انصراف") } })\n    if (dateSheet) FormDatePickerSheet(viewModel.dueMillis, { viewModel.dueMillis = it; dateSheet = false }, { dateSheet = false })
}
