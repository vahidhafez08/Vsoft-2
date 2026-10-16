package com.vsoft.app.ui.screens.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vsoft.app.data.model.*
import com.vsoft.app.data.repository.TaskRepository
import com.vsoft.app.util.taskFraction
import com.vsoft.app.util.safeLaunch
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TaskFilters(val query: String = "", val tab: Int = 0, val projectId: Long? = null) // tab: 0 all, 1 open, 2 done

data class TaskItem(val task: TaskEntity, val subDone: Int, val subTotal: Int, val projectTitle: String?)

data class ProjectItem(val project: ProjectEntity, val progress: Int, val doneTasks: Int, val totalTasks: Int)

data class TasksUiState(
    val projects: List<ProjectItem> = emptyList(),
    val tasks: List<TaskItem> = emptyList(),
    val total: Int = 0,
    val open: Int = 0,
    val done: Int = 0,
    val overallProgress: Int = 0,
    val loaded: Boolean = false
)

@HiltViewModel
class TasksViewModel @Inject constructor(private val repository: TaskRepository) : ViewModel() {

    private val filters = MutableStateFlow(TaskFilters())
    private var lastDeleted: Pair<TaskEntity, List<SubtaskEntity>>? = null

    val state: StateFlow<TasksUiState> =
        combine(repository.observeTasks(), repository.observeProjects(), repository.observeSubtasks(), filters) { t, p, s, f ->
            build(t, p, s, f)
        }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TasksUiState())

    fun setFilters(f: TaskFilters) { filters.value = f }
    fun toggle(task: TaskEntity) { safeLaunch { repository.toggleTask(task) } }
    fun saveTask(task: TaskEntity) { safeLaunch { repository.saveTask(task) } }
    fun saveProject(project: ProjectEntity) { safeLaunch { repository.saveProject(project) } }
    fun deleteProject(project: ProjectEntity) { safeLaunch { repository.deleteProject(project) } }

    fun deleteTask(task: TaskEntity) {
        safeLaunch {
            lastDeleted = task to repository.getSubtasks(task.id)
            repository.deleteTask(task)
        }
    }

    fun restoreLastDeleted() {
        safeLaunch {
            lastDeleted?.let { (task, subs) ->
                repository.restoreTask(task, subs)
                lastDeleted = null
            }
        }
    }

    private fun percent(ts: List<TaskEntity>, subs: Map<Long, List<SubtaskEntity>>): Int =
        if (ts.isEmpty()) 0 else (ts.sumOf { taskFraction(it, subs[it.id].orEmpty()).toDouble() } / ts.size * 100).toInt()

    private fun build(tasks: List<TaskEntity>, projects: List<ProjectEntity>, subs: List<SubtaskEntity>, f: TaskFilters): TasksUiState {
        val subsByTask = subs.groupBy { it.taskId }
        val tasksByProject = tasks.groupBy { it.projectId }

        val projectItems = projects.map { p ->
            val ts = tasksByProject[p.id].orEmpty()
            ProjectItem(p, percent(ts, subsByTask), ts.count { it.isDone }, ts.size)
        }

        val titles = projects.associate { it.id to it.title }
        val pid = f.projectId?.takeIf { id -> projects.any { it.id == id } }
        val q = f.query.trim().lowercase()

        val items = tasks
            .filter { t ->
                val tabOk = f.tab == 0 || (f.tab == 1 && !t.isDone) || (f.tab == 2 && t.isDone)
                val projOk = pid == null || t.projectId == pid
                val qOk = q.isEmpty() || t.title.lowercase().contains(q) || t.description.lowercase().contains(q)
                tabOk && projOk && qOk
            }
            .sortedWith(compareBy<TaskEntity>({ it.isDone }, { -it.priority }, { it.dueDateMillis ?: Long.MAX_VALUE }))
            .map { t ->
                val s = subsByTask[t.id].orEmpty()
                TaskItem(t, s.count { it.isDone }, s.size, t.projectId?.let { titles[it] })
            }

        return TasksUiState(
            projects = projectItems,
            tasks = items,
            total = tasks.size,
            open = tasks.count { !it.isDone },
            done = tasks.count { it.isDone },
            overallProgress = percent(tasks, subsByTask),
            loaded = true
        )
    }
}
