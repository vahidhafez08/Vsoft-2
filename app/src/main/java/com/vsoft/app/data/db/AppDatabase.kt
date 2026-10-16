package com.vsoft.app.data.db
import androidx.room.Database
import androidx.room.RoomDatabase
import com.vsoft.app.data.model.AccountEntity
import com.vsoft.app.data.model.ProjectEntity
import com.vsoft.app.data.model.TaskEntity
import com.vsoft.app.data.model.TransactionEntity
@Database(entities = [TransactionEntity::class, AccountEntity::class, ProjectEntity::class, TaskEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun accountDao(): AccountDao
    abstract fun projectDao(): ProjectDao
    abstract fun taskDao(): TaskDao
}
