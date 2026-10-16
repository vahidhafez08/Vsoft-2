package com.vsoft.app.data.repository

import android.content.SharedPreferences
import com.vsoft.app.data.db.ProjectDao
import com.vsoft.app.data.db.SubtaskDao
import com.vsoft.app.data.db.TaskDao
import com.vsoft.app.data.model.*
import com.vsoft.app.util.DAY_MS
import com.vsoft.app.util.startOfDay
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(
    private val taskDao: TaskDao,
    private val projectDao: ProjectDao,
    private val subtaskDao: SubtaskDao
) {
    fun observeTasks(): Flow<List<TaskEntity>> = taskDao.observeAll()
    fun observeProjects(): Flow<List<ProjectEntity>> = projectDao.observeAll()
    fun observeSubtasks(): Flow<List<SubtaskEntity>> = subtaskDao.observeAll()
    fun observeTask(id: Long): Flow<TaskEntity?> = taskDao.observeById(id)
    fun observeSubtasksFor(taskId: Long): Flow<List<SubtaskEntity>> = subtaskDao.observeForTask(taskId)

    suspend fun getSubtasks(taskId: Long): List<SubtaskEntity> = subtaskDao.getForTask(taskId)
    suspend fun saveTask(task: TaskEntity): Long = taskDao.upsert(task)
    suspend fun saveProject(project: ProjectEntity): Long = projectDao.upsert(project)

    suspend fun deleteTask(task: TaskEntity) {
        subtaskDao.deleteForTask(task.id)
        taskDao.delete(task)
    }

    suspend fun restoreTask(task: TaskEntity, subs: List<SubtaskEntity>) {
        taskDao.upsert(task)
        subs.forEach { subtaskDao.upsert(it) }
    }

    suspend fun toggleTask(task: TaskEntity) {
        val current = taskDao.getById(task.id) ?: return
        val newDone = !current.isDone
        taskDao.upsert(current.copy(isDone = newDone))
        subtaskDao.setAllDone(current.id, newDone)
    }

    suspend fun deleteProject(project: ProjectEntity) {
        taskDao.detachProject(project.id)
        projectDao.delete(project)
    }

    suspend fun addSubtask(taskId: Long, title: String) {
        subtaskDao.upsert(SubtaskEntity(taskId = taskId, title = title))
        syncStatus(taskId)
    }

    suspend fun toggleSubtask(sub: SubtaskEntity) {
        subtaskDao.upsert(sub.copy(isDone = !sub.isDone))
        syncStatus(sub.taskId)
    }

    suspend fun deleteSubtask(sub: SubtaskEntity) {
        subtaskDao.delete(sub)
        syncStatus(sub.taskId)
    }

    /** A task with subtasks is done only when all of them are done. */
    private suspend fun syncStatus(taskId: Long) {
        val task = taskDao.getById(taskId) ?: return
        val subs = subtaskDao.getForTask(taskId)
        if (subs.isEmpty()) return
        val allDone = subs.all { it.isDone }
        if (task.isDone != allDone) taskDao.upsert(task.copy(isDone = allDone))
    }

    /** Inserts demo projects/tasks once. */
    suspend fun seedDemoDataIfNeeded(prefs: SharedPreferences) {
        if (prefs.getBoolean(KEY_SEEDED, false)) return
        val today = startOfDay(System.currentTimeMillis())
        suspend fun project(title: String, days: Int?) =
            projectDao.upsert(ProjectEntity(title = title, progress = 0, dueDateMillis = days?.let { today + it * DAY_MS }))

        val web = project("پروژه وبسایت", 14)
        val app = project("پروژه اپلیکیشن", 30)
        val personal = project("کارهای شخصی", null)
        val study = project("مطالعه و یادگیری", 60)

        suspend fun addTask(
            title: String, desc: String, priority: Int, projectId: Long?, dueDays: Int?,
            done: Boolean = false, subs: List<Pair<String, Boolean>> = emptyList()
        ) {
            val id = taskDao.upsert(
                TaskEntity(
                    title = title, description = desc, isDone = done, priority = priority,
                    projectId = projectId, dueDateMillis = dueDays?.let { today + it * DAY_MS }
                )
            )
            subs.forEach { (t, d) -> subtaskDao.upsert(SubtaskEntity(taskId = id, title = t, isDone = d)) }
        }

        addTask(
            "طراحی رابط کاربری اپلیکیشن",
            "طراحی رابط کاربری اپلیکیشن مدیریت مالی و کارهای روزمره با رعایت اصول UI/UX و تجربه کاربری ساده و مدرن.",
            2, app, 3,
            subs = listOf("طراحی وایرفریم" to true, "طراحی UI" to true, "تست و اصلاح" to false, "تحویل نهایی" to false)
        )
        addTask("جلسه با تیم", "هماهنگی کارهای هفته با اعضای تیم", 1, web, 0)
        addTask("طراحی صفحه اصلی وبسایت", "", 1, web, -2, done = true)
        addTask("تکمیل گزارش ماهانه", "جمع‌بندی گزارش عملکرد ماه گذشته", 1, web, -1)
        addTask("پاسخ به ایمیل‌های مشتری", "", 1, null, 0)
        addTask("بررسی گزارش‌های مالی", "مرور هزینه‌ها و درآمدهای این ماه", 2, null, 1)
        addTask("خرید تجهیزات", "", 0, personal, 5)
        addTask(
            "مطالعه کتاب مدیریت زمان", "", 0, study, 7,
            subs = listOf("فصل اول" to true, "فصل دوم" to false, "فصل سوم" to false)
        )
        addTask("ثبت‌نام دوره آموزشی", "", 1, study, null, done = true)

        prefs.edit().putBoolean(KEY_SEEDED, true).apply()
    }

    private companion object {
        const val KEY_SEEDED = "tasks_seeded"
    }
}
