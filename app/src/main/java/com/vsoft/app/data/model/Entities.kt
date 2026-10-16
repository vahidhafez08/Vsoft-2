package com.vsoft.app.data.model
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "transactions")
data class TransactionEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val title: String, val amount: Long, val isIncome: Boolean, val category: String, val dateMillis: Long)
@Entity(tableName = "accounts")
data class AccountEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val name: String, val last4: String, val balance: Long, val colorKey: String)
@Entity(tableName = "projects")
data class ProjectEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val title: String, val progress: Int, val dueDateMillis: Long?)
@Entity(tableName = "tasks")
data class TaskEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val title: String, val description: String = "", val isDone: Boolean = false, val priority: Int = 1, val projectId: Long? = null, val dueDateMillis: Long? = null)
