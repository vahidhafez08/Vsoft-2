package com.vsoft.app.ui.screens.tasks

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.vsoft.app.data.model.ProjectEntity
import com.vsoft.app.data.repository.TaskRepository
import com.vsoft.app.util.UiEvents
import com.vsoft.app.util.safeLaunch
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@HiltViewModel
class ProjectFormViewModel @Inject constructor(savedStateHandle: SavedStateHandle, private val repository: TaskRepository) : ViewModel() {
    private val id = savedStateHandle.get<Long>("id") ?: -1L
    var title by mutableStateOf("")
    var dueEnabled by mutableStateOf(false)
    var dueMillis by mutableStateOf(System.currentTimeMillis())
    var progress by mutableStateOf(0)
    var loading by mutableStateOf(true)
    var saving by mutableStateOf(false)
    private var original: ProjectEntity? = null
    private val _done = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val done = _done.asSharedFlow()
    init {
        safeLaunch {
            try {
                if (id >= 0L) {
                    original = repository.observeProjects().first().firstOrNull { it.id == id }
                    original?.let { title = it.title; progress = it.progress; dueEnabled = it.dueDateMillis != null; dueMillis = it.dueDateMillis ?: dueMillis }
                }
            } catch (cancelled: CancellationException) { throw cancelled }
            finally { loading = false }
        }
    }
    fun save() {
        if (saving || title.trim().isBlank()) return
        saving = true
        safeLaunch {
            try {
                repository.saveProject(ProjectEntity(id = original?.id ?: 0L, title = title.trim().take(60), progress = progress, dueDateMillis = if (dueEnabled) dueMillis else null))
                UiEvents.show("پروژه ذخیره شد ✅")
                _done.tryEmit(Unit)
            } catch (cancelled: CancellationException) { throw cancelled }
            finally { saving = false }
        }
    }
}
