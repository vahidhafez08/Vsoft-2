package com.vsoft.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String, val amount: Long, val isIncome: Boolean, val category: String, val dateMillis: Long,
    val accountId: Long? = null,
    val debtId: Long? = null
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


enum class DebtType { I_OWE, OWED_TO_ME }

@Entity(tableName = "debts")
data class DebtEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val personName: String,
    val type: String,
    val totalAmount: Long,
    val dateMillis: Long,
    val dueDateMillis: Long? = null,
    val note: String = "",
    val isSettled: Boolean = false,
    val isManuallySettled: Boolean = false
)


@Entity(tableName = "investment_types")
data class InvestmentTypeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val iconKey: String,
    val colorHex: String,
    val unitLabel: String,
    val isDefault: Boolean = false
)

@Entity(tableName = "investments")
data class InvestmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val typeId: Long,
    val title: String = "",
    val investedAmount: Long,
    val buyPrice: Double,
    val quantity: Double,
    val currentPrice: Double? = null,
    val priceUpdatedAtMillis: Long? = null,
    val buyDateMillis: Long,
    val note: String = "",
    val status: String = "ACTIVE",
    val sellPrice: Double? = null
)
enum class InvestmentStatus { ACTIVE, SOLD }

