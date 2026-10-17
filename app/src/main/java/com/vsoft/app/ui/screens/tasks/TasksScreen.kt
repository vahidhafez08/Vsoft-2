package com.vsoft.app.ui.screens.tasks

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vsoft.app.data.model.ProjectEntity
import com.vsoft.app.data.model.TaskEntity
import com.vsoft.app.ui.components.*
import com.vsoft.app.ui.theme.BrandBlue
import com.vsoft.app.ui.theme.BrandPurple
import com.vsoft.app.ui.theme.ExpenseRed
import com.vsoft.app.ui.theme.IncomeGreen
import com.vsoft.app.ui.theme.Vsoft
import com.vsoft.app.util.formatJalali
import com.vsoft.app.util.isOverdue
import com.vsoft.app.util.toPersianText
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TasksScreen(onOpenTask: (Long) -> Unit, onOpenJobs: () -> Unit, onOpenCalendar: () -> Unit, viewModel: TasksViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var selectedProject by rememberSaveable { mutableStateOf<Long?>(null) }
    var addingTask by rememberSaveable { mutableStateOf(false) }
    var addingProject by rememberSaveable { mutableStateOf(false) }
    var editingProject by remember { mutableStateOf<ProjectEntity?>(null) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val focus = LocalFocusManager.current

    LaunchedEffect(query, tab, selectedProject) {
        viewModel.setFilters(TaskFilters(query, tab, selectedProject))
    }

    fun deleteWithUndo(task: TaskEntity) {
        viewModel.deleteTask(task)
        scope.launch {
            snackbar.currentSnackbarData?.dismiss()
            val result = snackbar.showSnackbar("کار حذف شد", actionLabel = "واگرد", duration = SnackbarDuration.Long)
            if (result == SnackbarResult.ActionPerformed) viewModel.restoreLastDeleted()
        }
    }

    Box(Modifier.fillMaxSize().background(Vsoft.colors.bg)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "header") {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("کارها و پروژه‌ها", color = Vsoft.colors.textPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text(state.open.toPersianText() + " کار در حال انجام", color = Vsoft.colors.textSecondary, fontSize = 12.sp)
                }
            }

            item(key = "jobsEntry") {
                VCard {
                    Row(
                        Modifier.fillMaxWidth().bounceClick(onOpenJobs),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Construction, contentDescription = null, tint = BrandPurple, modifier = Modifier.size(28.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("شغل و کارگاه‌ها", color = Vsoft.colors.textPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text("گزارش روزانه، طبقه‌ها و پیشرفت کارگاه", color = Vsoft.colors.textSecondary, fontSize = 11.sp)
                        }
                        Icon(Icons.Filled.ChevronLeft, contentDescription = null, tint = Vsoft.colors.textSecondary)
                    }
                }
            }

            item(key = "calendarEntry") {
                VCard {
                    Row(
                        Modifier.fillMaxWidth().bounceClick(onOpenCalendar),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = BrandBlue, modifier = Modifier.size(28.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("تقویم", color = Vsoft.colors.textPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text("کارها و گزارش‌های هر روز", color = Vsoft.colors.textSecondary, fontSize = 11.sp)
                        }
                        Icon(Icons.Filled.ChevronLeft, contentDescription = null, tint = Vsoft.colors.textSecondary)
                    }
                }
            }

            item(key = "summary") {
                VCard {
                    Row(Modifier.fillMaxWidth()) {
                        StatCell("کل کارها", state.total, BrandBlue, Modifier.weight(1f))
                        StatCell("در حال انجام", state.open, BrandPurple, Modifier.weight(1f))
                        StatCell("انجام شده", state.done, IncomeGreen, Modifier.weight(1f))
                    }
                    Spacer(Modifier.size(12.dp))
                    Row(Modifier.fillMaxWidth()) {
                        Text("پیشرفت کلی", color = Vsoft.colors.textSecondary, fontSize = 12.sp, modifier = Modifier.weight(1f))
                        Text(
                            state.overallProgress.toPersianText() + "٪",
                            color = Vsoft.colors.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.size(6.dp))
                    AnimatedProgressBar(state.overallProgress / 100f, BrandBlue)
                }
            }

            item(key = "search") {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it.take(40) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    placeholder = { Text("جستجو در کارها...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = {
                        AnimatedVisibility(query.isNotEmpty(), enter = fadeIn() + scaleIn(), exit = fadeOut() + scaleOut()) {
                            IconButton(onClick = { query = "" }) { Icon(Icons.Filled.Close, contentDescription = "پاک کردن") }
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focus.clearFocus() })
                )
            }

            item(key = "tabs") {
                SegmentedTabs(items = listOf("همه", "در حال انجام", "انجام شده"), selected = tab, onSelect = { tab = it })
            }

            item(key = "projects") {
                if (state.projects.isEmpty()) {
                    VCard {
                        Text("هنوز پروژه‌ای ندارید. با دکمه + یک پروژه بسازید.", color = Vsoft.colors.textSecondary, fontSize = 12.sp)
                    }
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(state.projects, key = { it.project.id }) { item ->
                            ProjectCard(
                                item = item,
                                selected = selectedProject == item.project.id,
                                onClick = { selectedProject = if (selectedProject == item.project.id) null else item.project.id },
                                onEdit = { editingProject = item.project }
                            )
                        }
                    }
                }
            }

            item(key = "tasksTitle") {
                Text(
                    "کارها (" + state.tasks.size.toPersianText() + ")",
                    color = Vsoft.colors.textPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold
                )
            }

            if (state.loaded && state.tasks.isEmpty()) {
                item(key = "empty") {
                    val noFilters = query.isBlank() && tab == 0 && selectedProject == null
                    EmptyState(
                        icon = if (noFilters) Icons.Filled.TaskAlt else Icons.Filled.SearchOff,
                        title = if (noFilters) "هنوز کاری ثبت نشده" else "نتیجه‌ای پیدا نشد",
                        subtitle = "با دکمه + کار جدید بسازید یا فیلترها را تغییر دهید"
                    )
                }
            }

            items(state.tasks, key = { "task" + it.task.id }) { item ->
                TaskRow(
                    item = item,
                    modifier = Modifier.animateItemPlacement(),
                    onClick = { onOpenTask(item.task.id) },
                    onToggle = { viewModel.toggle(item.task) },
                    onDelete = { deleteWithUndo(item.task) }
                )
            }
        }

        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).padding(bottom = 88.dp))

        TasksFab(
            onNewTask = { addingTask = true },
            onNewProject = { addingProject = true },
            modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)
        )
    }

    if (addingTask) {
        TaskEditorSheet(
            initial = null,
            projects = state.projects.map { it.project },
            defaultProjectId = selectedProject,
            onDismiss = { addingTask = false },
            onSave = { viewModel.saveTask(it); addingTask = false }
        )
    }

    if (addingProject) {
        ProjectEditorSheet(
            initial = null,
            onDismiss = { addingProject = false },
            onSave = { viewModel.saveProject(it); addingProject = false }
        )
    }

    editingProject?.let { project ->
        ProjectEditorSheet(
            initial = project,
            onDismiss = { editingProject = null },
            onSave = { viewModel.saveProject(it); editingProject = null },
            onDelete = {
                if (selectedProject == project.id) selectedProject = null
                viewModel.deleteProject(project)
                editingProject = null
            }
        )
    }
}

@Composable
private fun StatCell(label: String, value: Int, color: Color, modifier: Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, color = Vsoft.colors.textSecondary, fontSize = 11.sp)
        AnimatedMoney(value.toLong(), color = color, fontSize = 20.sp)
    }
}

@Composable
private fun ProjectCard(item: ProjectItem, selected: Boolean, onClick: () -> Unit, onEdit: () -> Unit) {
    val shape = RoundedCornerShape(20.dp)
    val accent = if (item.progress >= 100) IncomeGreen else BrandBlue
    Column(
        Modifier
            .width(230.dp)
            .bounceClick(onClick)
            .clip(shape)
            .background(Vsoft.colors.surface)
            .border(if (selected) 2.dp else 1.dp, if (selected) BrandBlue else Vsoft.colors.outline, shape)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                item.project.title,
                color = Vsoft.colors.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold,
                maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f)
            )
            Box(
                Modifier.size(30.dp).clip(RoundedCornerShape(10.dp)).clickable(onClick = onEdit),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Edit, contentDescription = "ویرایش پروژه",
                    tint = Vsoft.colors.textSecondary, modifier = Modifier.size(16.dp)
                )
            }
        }
        AnimatedProgressBar(item.progress / 100f, accent)
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                item.progress.toPersianText() + "٪",
                color = accent, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)
            )
            Text(
                item.doneTasks.toPersianText() + " از " + item.totalTasks.toPersianText() + " کار",
                color = Vsoft.colors.textSecondary, fontSize = 11.sp
            )
        }
        item.project.dueDateMillis?.let {
            MiniChip(formatJalali(it), Vsoft.colors.textSecondary, icon = Icons.Filled.CalendarMonth)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun TaskRow(item: TaskItem, modifier: Modifier, onClick: () -> Unit, onToggle: () -> Unit, onDelete: () -> Unit) {
    val task = item.task
    val currentToggle by rememberUpdatedState(onToggle)
    val currentDelete by rememberUpdatedState(onDelete)
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.EndToStart -> { currentDelete(); true }
                SwipeToDismissBoxValue.StartToEnd -> { currentToggle(); false }
                else -> false
            }
        }
    )
    val shape = RoundedCornerShape(18.dp)

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        backgroundContent = {
            val target = dismissState.targetValue
            val bg by animateColorAsState(
                when (target) {
                    SwipeToDismissBoxValue.StartToEnd -> IncomeGreen
                    SwipeToDismissBoxValue.EndToStart -> ExpenseRed
                    else -> Color.Transparent
                },
                label = "taskSwipeBg"
            )
            Box(
                Modifier.fillMaxSize().clip(shape).background(bg).padding(horizontal = 22.dp),
                contentAlignment = if (target == SwipeToDismissBoxValue.StartToEnd) Alignment.CenterStart else Alignment.CenterEnd
            ) {
                when (target) {
                    SwipeToDismissBoxValue.StartToEnd ->
                        Icon(if (task.isDone) Icons.Filled.Replay else Icons.Filled.Check, contentDescription = null, tint = Color.White)
                    SwipeToDismissBoxValue.EndToStart ->
                        Icon(Icons.Filled.Delete, contentDescription = null, tint = Color.White)
                    else -> Unit
                }
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
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TaskCheck(done = task.isDone, onClick = onToggle)
            Spacer(Modifier.width(6.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    task.title,
                    color = if (task.isDone) Vsoft.colors.textSecondary else Vsoft.colors.textPrimary,
                    fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                    maxLines = 2, overflow = TextOverflow.Ellipsis,
                    textDecoration = if (task.isDone) TextDecoration.LineThrough else TextDecoration.None
                )
                item.projectTitle?.let { Text(it, color = Vsoft.colors.textSecondary, fontSize = 11.sp, maxLines = 1) }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    MiniChip(priorityLabel(task.priority), priorityColor(task.priority))
                    task.dueDateMillis?.let { due ->
                        MiniChip(
                            formatJalali(due),
                            if (isOverdue(due, task.isDone)) ExpenseRed else Vsoft.colors.textSecondary,
                            icon = Icons.Filled.CalendarMonth
                        )
                    }
                    if (item.subTotal > 0) {
                        MiniChip(
                            item.subDone.toPersianText() + "/" + item.subTotal.toPersianText(),
                            BrandPurple, icon = Icons.Filled.TaskAlt
                        )
                    }
                }
            }
        }
    }
}
