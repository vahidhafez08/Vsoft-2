package com.vsoft.app.ui.screens.tasks

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vsoft.app.data.model.*
import com.vsoft.app.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TaskDetailUiState(
    val task: TaskEntity? = null,
    val subtasks: List<SubtaskEntity> = emptyList(),
    val project: ProjectEntity? = null,
    val projects: List<ProjectEntity> = emptyList(),
    val loaded: Boolean = false
)

@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: TaskRepository
) : ViewModel() {

    private val taskId: Long = savedStateHandle.get<Long>("id") ?: -1L

    val state: StateFlow<TaskDetailUiState> =
        combine(repository.observeTask(taskId), repository.observeSubtasksFor(taskId), repository.observeProjects()) { task, subs, projects ->
            TaskDetailUiState(
                task = task,
                subtasks = subs,
                project = projects.firstOrNull { it.id == task?.projectId },
                projects = projects,
                loaded = true
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TaskDetailUiState())

    fun toggleTask(task: TaskEntity) { viewModelScope.launch { repository.toggleTask(task) } }
    fun saveTask(task: TaskEntity) { viewModelScope.launch { repository.saveTask(task) } }
    fun deleteTask(task: TaskEntity) { viewModelScope.launch { repository.deleteTask(task) } }
    fun toggleSubtask(sub: SubtaskEntity) { viewModelScope.launch { repository.toggleSubtask(sub) } }
    fun deleteSubtask(sub: SubtaskEntity) { viewModelScope.launch { repository.deleteSubtask(sub) } }

    fun addSubtask(title: String) {
        if (title.isBlank()) return
        viewModelScope.launch { repository.addSubtask(taskId, title.trim()) }
    }
}
