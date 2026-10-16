package com.vsoft.app.data.db
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.vsoft.app.data.model.AccountEntity
import com.vsoft.app.data.model.ProjectEntity
import com.vsoft.app.data.model.TaskEntity
import com.vsoft.app.data.model.TransactionEntity
import kotlinx.coroutines.flow.Flow
@Dao interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY dateMillis DESC") fun observeAll(): Flow<List<TransactionEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(item: TransactionEntity): Long
    @Delete suspend fun delete(item: TransactionEntity)
}
@Dao interface AccountDao {
    @Query("SELECT * FROM accounts ORDER BY id ASC") fun observeAll(): Flow<List<AccountEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(item: AccountEntity): Long
    @Delete suspend fun delete(item: AccountEntity)
}
@Dao interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY id DESC") fun observeAll(): Flow<List<ProjectEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(item: ProjectEntity): Long
    @Delete suspend fun delete(item: ProjectEntity)
}
@Dao interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY isDone ASC, id DESC") fun observeAll(): Flow<List<TaskEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(item: TaskEntity): Long
    @Update suspend fun update(item: TaskEntity)
    @Delete suspend fun delete(item: TaskEntity)
}
