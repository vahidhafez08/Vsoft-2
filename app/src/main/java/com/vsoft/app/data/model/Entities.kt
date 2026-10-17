package com.vsoft.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String, val amount: Long, val isIncome: Boolean, val category: String, val dateMillis: Long,
    val accountId: Long? = null
)

@Entity(tableName = "transfers")
data class TransferEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fromAccountId: Long,
    val toAccountId: Long,
    val amount: Long,
    val dateMillis: Long,
    val note: String = ""
)

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String, val last4: String, val balance: Long, val colorKey: String
)

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String, val progress: Int, val dueDateMillis: Long?
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val isDone: Boolean = false,
    val priority: Int = 1, // 0 low, 1 normal, 2 high
    val projectId: Long? = null,
    val dueDateMillis: Long? = null
)

@Entity(tableName = "subtasks")
data class SubtaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long, val title: String, val isDone: Boolean = false
)
@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: Int = 4,
    val fileName: String,
    val mimeType: String = "",
    val sizeBytes: Long = 0,
    val storedName: String,
    val note: String = "",
    val createdAt: Long
)
