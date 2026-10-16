package com.vsoft.app.data.repository

import com.vsoft.app.data.db.JobDao
import com.vsoft.app.data.model.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JobRepository @Inject constructor(private val dao: JobDao) {
    fun sites(): Flow<List<SiteEntity>> = dao.observeSites()
    fun site(id: Long): Flow<SiteEntity?> = dao.observeSite(id)
    fun floors(siteId: Long): Flow<List<FloorEntity>> = dao.observeFloors(siteId)
    fun allItems(): Flow<List<WorkItemEntity>> = dao.observeAllItems()
    fun items(siteId: Long): Flow<List<WorkItemEntity>> = dao.observeItems(siteId)
    fun allLogs(): Flow<List<WorkLogEntity>> = dao.observeAllLogs()
    fun logs(siteId: Long): Flow<List<WorkLogEntity>> = dao.observeLogs(siteId)
    fun allPayments(): Flow<List<SitePaymentEntity>> = dao.observeAllPayments()
    fun payments(siteId: Long): Flow<List<SitePaymentEntity>> = dao.observePayments(siteId)

    suspend fun saveSite(site: SiteEntity): Long = dao.upsertSite(site)
    suspend fun saveFloor(floor: FloorEntity): Long = dao.upsertFloor(floor)
    suspend fun saveItem(item: WorkItemEntity): Long = dao.upsertItem(item)
    suspend fun addLog(log: WorkLogEntity): Long = dao.upsertLog(log)
    suspend fun savePayment(p: SitePaymentEntity): Long = dao.upsertPayment(p)

    suspend fun deleteSite(id: Long) {
        dao.deleteLogsOfSite(id)
        dao.deleteItemsOfSite(id)
        dao.deleteFloorsOfSite(id)
        dao.deletePaymentsOfSite(id)
        dao.deleteSiteById(id)
    }

    suspend fun deleteFloor(id: Long) {
        dao.deleteLogsOfFloor(id)
        dao.deleteItemsOfFloor(id)
        dao.deleteFloorById(id)
    }

    suspend fun deleteItem(id: Long) {
        dao.deleteLogsOfItem(id)
        dao.deleteItemById(id)
    }

    suspend fun deleteLog(id: Long) = dao.deleteLogById(id)
    suspend fun deletePayment(id: Long) = dao.deletePaymentById(id)
}
