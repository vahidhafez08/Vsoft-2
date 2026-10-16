package com.vsoft.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sites")
data class SiteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val address: String = "",
    val client: String = "",
    val startDateMillis: Long,
    val contractAmount: Long = 0
)

@Entity(tableName = "floors")
data class FloorEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val siteId: Long,
    val name: String
)

@Entity(tableName = "work_items")
data class WorkItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val siteId: Long,
    val floorId: Long,
    val title: String,
    val unit: String,
    val targetQty: Int
)

@Entity(tableName = "work_logs")
data class WorkLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val siteId: Long,
    val floorId: Long,
    val itemId: Long,
    val qty: Int,
    val note: String = "",
    val dateMillis: Long
)

@Entity(tableName = "site_payments")
data class SitePaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val siteId: Long,
    val amount: Long,
    val dateMillis: Long,
    val note: String = ""
)
