package com.vsoft.app.ui.screens.tasks

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.vsoft.app.data.model.ProjectEntity
import com.vsoft.app.data.model.SubtaskEntity
import com.vsoft.app.data.model.TaskEntity
import com.vsoft.app.data.repository.TaskRepository
import com.vsoft.app.util.DAY_MS
import com.vsoft.app.util.UiEvents
import com.vsoft.app.util.safeLaunch
import com.vsoft.app.util.startOfDay
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: TaskRepository
) : ViewModel() {
    private val taskId = savedStateHandle.get<Long>("id") ?: -1L
    val isEditing: Boolean get() = taskId >= 0L
    private val requestedProjectId = savedStateHandle.get<Long>("projectId") ?: -1L
    private val requestedDue = savedStateHandle.get<Long>("due") ?: -1L
    var title by mutableStateOf("")
    var description by mutableStateOf("")
    var priority by mutableStateOf(1)
    var projectId by mutableStateOf<Long?>(requestedProjectId.takeIf { it >= 0L })
    var dueEnabled by mutableStateOf(requestedDue >= 0L)
    var dueMillis by mutableStateOf<Long>(if (requestedDue >= 0L) requestedDue + 9L * 60L * 60L * 1000L else System.currentTimeMillis())
    var reminder by mutableStateOf(false)
    var subtasks by mutableStateOf<List<SubtaskEntity>>(emptyList())
    var draftSubtasks by mutableStateOf<List<Pair<Long, String>>>(emptyList())
    var projects by mutableStateOf<List<ProjectEntity>>(emptyList())
    var loading by mutableStateOf(true)
    var saving by mutableStateOf(false)
    private var original: TaskEntity? = null
    private val _done = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val done = _done.asSharedFlow()

    init {
        safeLaunch {
            try {
                projects = repository.observeProjects().first()
                if (taskId >= 0L) {
                    original = repository.observeTask(taskId).first()
                    original?.let { task ->
                        title = task.title
                        description = task.description
                        priority = task.priority.coerceIn(0, 2)
                        projectId = task.projectId
                        dueEnabled = task.dueDateMillis != null
                        dueMillis = task.dueDateMillis ?: dueMillis
                        subtasks = repository.getSubtasks(task.id)
                        draftSubtasks = subtasks.map { it.id to it.title }
                    }
                }
            } catch (cancelled: CancellationException) { throw cancelled }
            finally { loading = false }
        }
    }

    fun addSubtask(value: String) {
        val clean = value.trim().take(100)
        if (clean.isBlank() || draftSubtasks.size >= 20) return
        draftSubtasks = draftSubtasks + ((System.nanoTime()) to clean)
    }
    fun removeSubtask(key: Long) { draftSubtasks = draftSubtasks.filterNot { it.first == key } }
    fun setDueDay(day: Long) {
        val old = java.util.Calendar.getInstance().apply { timeInMillis = dueMillis }
        val selected = startOfDay(day)
        val next = java.util.Calendar.getInstance().apply {
            timeInMillis = selected
            set(java.util.Calendar.HOUR_OF_DAY, old.get(java.util.Calendar.HOUR_OF_DAY))
            set(java.util.Calendar.MINUTE, old.get(java.util.Calendar.MINUTE))
        }
        dueMillis = next.timeInMillis
        dueEnabled = true
    }
    fun setDueTime(hour: Int, minute: Int) {
        val cal = java.util.Calendar.getInstance().apply { timeInMillis = dueMillis; set(java.util.Calendar.HOUR_OF_DAY, hour); set(java.util.Calendar.MINUTE, minute); set(java.util.Calendar.SECOND, 0); set(java.util.Calendar.MILLISECOND, 0) }
        dueMillis = cal.timeInMillis
    }
    fun onReminderChanged(enabled: Boolean) {
        if (enabled) UiEvents.show("یادآوری آیتم‌ها به‌زودی فعال می‌شه")
        reminder = false
    }
    fun save() {
        if (saving || title.trim().isBlank()) return
        saving = true
        safeLaunch {
            try {
                val current = original
                val savedId = repository.saveTask(TaskEntity(
                    id = current?.id ?: 0L, title = title.trim().take(100), description = description.trim().take(500),
                    isDone = current?.isDone ?: false, priority = priority.coerceIn(0, 2), projectId = projectId,
                    dueDateMillis = if (dueEnabled) dueMillis else null
                ))
                val taskKey = if (current != null) current.id else savedId
                val existing = if (current != null) subtasks else emptyList()
                val desiredExistingIds = draftSubtasks.mapNotNull { pair -> pair.first.takeIf { id -> existing.any { it.id == id } } }.toSet()
                existing.filterNot { it.id in desiredExistingIds }.forEach { repository.deleteSubtask(it) }
                draftSubtasks.forEach { (key, subTitle) ->
                    val old = existing.firstOrNull { it.id == key }
                    if (old == null) repository.addSubtask(taskKey, subTitle.trim().take(100))
                    else if (old.title != subTitle.trim()) {
                        repository.deleteSubtask(old)
                        repository.addSubtask(taskKey, subTitle.trim().take(100))
                        if (old.isDone) {
                            val newest = repository.observeSubtasksFor(taskKey).first().lastOrNull { it.title == subTitle.trim() }
                            if (newest != null && !newest.isDone) repository.toggleSubtask(newest)
                        }
                    }
                }
                if (current != null) repository.saveTask(current.copy(isDone = current.isDone))
                UiEvents.show("کار ذخیره شد ✅")
                _done.tryEmit(Unit)
            } catch (cancelled: CancellationException) { throw cancelled }
            finally { saving = false }
        }
    }
}
