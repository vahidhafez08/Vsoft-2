package com.vsoft.app.ui.screens.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vsoft.app.data.model.TaskEntity
import com.vsoft.app.data.repository.JobRepository
import com.vsoft.app.data.repository.TaskRepository
import com.vsoft.app.util.startOfDay
import com.vsoft.app.util.safeLaunch
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import com.vsoft.app.util.JCal
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

    private val initialDay = startOfDay(System.currentTimeMillis())
    private val initialJalali = JCal.toJalali(initialDay)
    private val _displayedMonth = MutableStateFlow(initialJalali[0] to initialJalali[1])
    val displayedMonth: StateFlow<Pair<Int, Int>> = _displayedMonth
    private val _selectedDay = MutableStateFlow(initialDay)
    val selectedDay: StateFlow<Long> = _selectedDay

    fun goToMonth(year: Int, month: Int) {
        val safeYear = year.coerceIn(1300, 1500)
        val safeMonth = month.coerceIn(1, 12)
        _displayedMonth.value = safeYear to safeMonth
        val today = startOfDay(System.currentTimeMillis())
        val todayJ = JCal.toJalali(today)
        _selectedDay.value = if (todayJ[0] == safeYear && todayJ[1] == safeMonth) {
            today
        } else {
            JCal.fromJalali(safeYear, safeMonth, 1)
        }
    }

    fun goToToday() {
        val today = startOfDay(System.currentTimeMillis())
        val j = JCal.toJalali(today)
        _displayedMonth.value = j[0] to j[1]
        _selectedDay.value = today
    }

    fun selectDay(dayMillis: Long) {
        _selectedDay.value = startOfDay(dayMillis)
    }

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
        safeLaunch { taskRepo.toggleTask(task) }
    }

    fun addTask(title: String, dayMillis: Long) {
        if (title.isBlank()) return
        safeLaunch {
            taskRepo.saveTask(TaskEntity(title = title.trim(), dueDateMillis = dayMillis))
        }
    }
}
