package com.vsoft.app.ui.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vsoft.app.data.model.TaskEntity
import com.vsoft.app.data.repository.JobRepository
import com.vsoft.app.data.repository.TaskRepository
import com.vsoft.app.util.startOfDay
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CalLog(val dayMillis: Long, val title: String, val qty: Int, val unit: String, val note: String)

data class CalendarData(
    val tasks: List<TaskEntity> = emptyList(),
    val logs: List<CalLog> = emptyList()
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val taskRepo: TaskRepository,
    private val jobRepo: JobRepository
) : ViewModel() {

    val data: StateFlow<CalendarData> =
        combine(taskRepo.observeTasks(), jobRepo.allItems(), jobRepo.allLogs()) { tasks, items, logs ->
            val itemMap = items.associateBy { it.id }
            CalendarData(
                tasks = tasks,
                logs = logs.map { l ->
                    val item = itemMap[l.itemId]
                    CalLog(startOfDay(l.dateMillis), item?.title ?: "کار حذف‌شده", l.qty, item?.unit ?: "", l.note)
                }
            )
        }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CalendarData())

    fun toggle(task: TaskEntity) {
        viewModelScope.launch { taskRepo.toggleTask(task) }
    }

    fun addTask(title: String, dayMillis: Long) {
        if (title.isBlank()) return
        viewModelScope.launch {
            taskRepo.saveTask(TaskEntity(title = title.trim(), dueDateMillis = dayMillis))
        }
    }
}
