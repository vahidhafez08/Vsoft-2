package com.vsoft.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vsoft.app.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface JobDao {
    @Query("SELECT * FROM sites ORDER BY id DESC") fun observeSites(): Flow<List<SiteEntity>>
    @Query("SELECT * FROM sites WHERE id = :id") fun observeSite(id: Long): Flow<SiteEntity?>
    @Query("SELECT * FROM floors WHERE siteId = :siteId ORDER BY id ASC") fun observeFloors(siteId: Long): Flow<List<FloorEntity>>
    @Query("SELECT * FROM work_items ORDER BY id ASC") fun observeAllItems(): Flow<List<WorkItemEntity>>
    @Query("SELECT * FROM work_items WHERE siteId = :siteId ORDER BY id ASC") fun observeItems(siteId: Long): Flow<List<WorkItemEntity>>
    @Query("SELECT * FROM work_logs ORDER BY dateMillis DESC, id DESC") fun observeAllLogs(): Flow<List<WorkLogEntity>>
    @Query("SELECT * FROM work_logs WHERE siteId = :siteId ORDER BY dateMillis DESC, id DESC") fun observeLogs(siteId: Long): Flow<List<WorkLogEntity>>
    @Query("SELECT * FROM site_payments ORDER BY dateMillis DESC, id DESC") fun observeAllPayments(): Flow<List<SitePaymentEntity>>
    @Query("SELECT * FROM site_payments WHERE siteId = :siteId ORDER BY dateMillis DESC, id DESC") fun observePayments(siteId: Long): Flow<List<SitePaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertSite(item: SiteEntity): Long
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertFloor(item: FloorEntity): Long
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertItem(item: WorkItemEntity): Long
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertLog(item: WorkLogEntity): Long
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertPayment(item: SitePaymentEntity): Long

    @Query("DELETE FROM sites WHERE id = :id") suspend fun deleteSiteById(id: Long)
    @Query("DELETE FROM floors WHERE siteId = :siteId") suspend fun deleteFloorsOfSite(siteId: Long)
    @Query("DELETE FROM work_items WHERE siteId = :siteId") suspend fun deleteItemsOfSite(siteId: Long)
    @Query("DELETE FROM work_logs WHERE siteId = :siteId") suspend fun deleteLogsOfSite(siteId: Long)
    @Query("DELETE FROM site_payments WHERE siteId = :siteId") suspend fun deletePaymentsOfSite(siteId: Long)

    @Query("DELETE FROM floors WHERE id = :id") suspend fun deleteFloorById(id: Long)
    @Query("DELETE FROM work_items WHERE floorId = :floorId") suspend fun deleteItemsOfFloor(floorId: Long)
    @Query("DELETE FROM work_logs WHERE floorId = :floorId") suspend fun deleteLogsOfFloor(floorId: Long)

    @Query("DELETE FROM work_items WHERE id = :id") suspend fun deleteItemById(id: Long)
    @Query("DELETE FROM work_logs WHERE itemId = :itemId") suspend fun deleteLogsOfItem(itemId: Long)

    @Query("DELETE FROM work_logs WHERE id = :id") suspend fun deleteLogById(id: Long)
    @Query("DELETE FROM site_payments WHERE id = :id") suspend fun deletePaymentById(id: Long)
}
