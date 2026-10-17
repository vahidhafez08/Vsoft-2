package com.vsoft.app.data.db

import androidx.room.*
import com.vsoft.app.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY dateMillis DESC") fun observeAll(): Flow<List<TransactionEntity>>
    @Query("SELECT * FROM transactions") suspend fun getAllOnce(): List<TransactionEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAllRaw(items: List<TransactionEntity>)
    @Query("DELETE FROM transactions") suspend fun deleteAllRaw()
    @Query("UPDATE transactions SET accountId = NULL WHERE accountId = :id") suspend fun detachAccount(id: Long)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(item: TransactionEntity): Long
    @Delete suspend fun delete(item: TransactionEntity)
}

@Dao
interface TransferDao {
    @Query("SELECT * FROM transfers ORDER BY dateMillis DESC, id DESC") fun observeAll(): Flow<List<TransferEntity>>
    @Insert suspend fun insert(item: TransferEntity): Long
    @Delete suspend fun delete(item: TransferEntity)
    @Query("DELETE FROM transfers WHERE fromAccountId = :id OR toAccountId = :id") suspend fun deleteForAccount(id: Long)
    @Query("SELECT * FROM transfers") suspend fun getAllOnce(): List<TransferEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAllRaw(items: List<TransferEntity>)
    @Query("DELETE FROM transfers") suspend fun deleteAllRaw()
}

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts ORDER BY id ASC") fun observeAll(): Flow<List<AccountEntity>>
    @Query("SELECT * FROM accounts") suspend fun getAllOnce(): List<AccountEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAllRaw(items: List<AccountEntity>)
    @Query("DELETE FROM accounts") suspend fun deleteAllRaw()
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(item: AccountEntity): Long
    @Delete suspend fun delete(item: AccountEntity)
}

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY id DESC") fun observeAll(): Flow<List<ProjectEntity>>
    @Query("SELECT * FROM projects") suspend fun getAllOnce(): List<ProjectEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAllRaw(items: List<ProjectEntity>)
    @Query("DELETE FROM projects") suspend fun deleteAllRaw()
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(item: ProjectEntity): Long
    @Delete suspend fun delete(item: ProjectEntity)
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY isDone ASC, id DESC") fun observeAll(): Flow<List<TaskEntity>>
    @Query("SELECT * FROM tasks WHERE id = :id") fun observeById(id: Long): Flow<TaskEntity?>
    @Query("SELECT * FROM tasks WHERE id = :id") suspend fun getById(id: Long): TaskEntity?
    @Query("SELECT * FROM tasks") suspend fun getAllOnce(): List<TaskEntity>
    @Query("SELECT COUNT(*) FROM tasks WHERE isDone = 0 AND dueDateMillis IS NOT NULL AND dueDateMillis < :endExclusive") suspend fun countDueUntil(endExclusive: Long): Int
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAllRaw(items: List<TaskEntity>)
    @Query("DELETE FROM tasks") suspend fun deleteAllRaw()
    @Query("UPDATE tasks SET projectId = NULL WHERE projectId = :projectId") suspend fun detachProject(projectId: Long)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(item: TaskEntity): Long
    @Update suspend fun update(item: TaskEntity)
    @Delete suspend fun delete(item: TaskEntity)
}

@Dao
interface SubtaskDao {
    @Query("SELECT * FROM subtasks ORDER BY id ASC") fun observeAll(): Flow<List<SubtaskEntity>>
    @Query("SELECT * FROM subtasks WHERE taskId = :taskId ORDER BY id ASC") fun observeForTask(taskId: Long): Flow<List<SubtaskEntity>>
    @Query("SELECT * FROM subtasks WHERE taskId = :taskId ORDER BY id ASC") suspend fun getForTask(taskId: Long): List<SubtaskEntity>
    @Query("SELECT * FROM subtasks") suspend fun getAllOnce(): List<SubtaskEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAllRaw(items: List<SubtaskEntity>)
    @Query("DELETE FROM subtasks") suspend fun deleteAllRaw()
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(item: SubtaskEntity): Long
    @Delete suspend fun delete(item: SubtaskEntity)
    @Query("DELETE FROM subtasks WHERE taskId = :taskId") suspend fun deleteForTask(taskId: Long)
    @Query("UPDATE subtasks SET isDone = :done WHERE taskId = :taskId") suspend fun setAllDone(taskId: Long, done: Boolean)
}

@Dao
interface DocumentDao {
    @Query("SELECT * FROM documents ORDER BY createdAt DESC, id DESC")
    fun observeAll(): Flow<List<DocumentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: DocumentEntity): Long

    @Update
    suspend fun update(item: DocumentEntity)

    @Delete
    suspend fun delete(item: DocumentEntity)

    @Query("SELECT * FROM documents WHERE id = :id")
    suspend fun getById(id: Long): DocumentEntity?

    @Query("SELECT * FROM documents")
    suspend fun getAllOnce(): List<DocumentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllRaw(items: List<DocumentEntity>)

    @Query("DELETE FROM documents")
    suspend fun deleteAllRaw()
}
