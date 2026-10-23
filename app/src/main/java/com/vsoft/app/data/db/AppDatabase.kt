package com.vsoft.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.vsoft.app.data.model.*

@Database(
    entities = [
        TransactionEntity::class, AccountEntity::class, TransferEntity::class, DocumentEntity::class, ProjectEntity::class, TaskEntity::class, SubtaskEntity::class,
        SiteEntity::class, FloorEntity::class, WorkItemEntity::class, WorkLogEntity::class, SitePaymentEntity::class, ManualDailyReportEntity::class
    ],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun accountDao(): AccountDao
    abstract fun transferDao(): TransferDao
    abstract fun documentDao(): DocumentDao
    abstract fun projectDao(): ProjectDao
    abstract fun taskDao(): TaskDao
    abstract fun subtaskDao(): SubtaskDao
    abstract fun jobDao(): JobDao
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `subtasks` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`taskId` INTEGER NOT NULL, `title` TEXT NOT NULL, `isDone` INTEGER NOT NULL)"
        )
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `transactions` ADD COLUMN `accountId` INTEGER")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `sites` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`name` TEXT NOT NULL, `address` TEXT NOT NULL, `client` TEXT NOT NULL, " +
                "`startDateMillis` INTEGER NOT NULL, `contractAmount` INTEGER NOT NULL)"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `floors` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`siteId` INTEGER NOT NULL, `name` TEXT NOT NULL)"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `work_items` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`siteId` INTEGER NOT NULL, `floorId` INTEGER NOT NULL, `title` TEXT NOT NULL, " +
                "`unit` TEXT NOT NULL, `targetQty` INTEGER NOT NULL)"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `work_logs` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`siteId` INTEGER NOT NULL, `floorId` INTEGER NOT NULL, `itemId` INTEGER NOT NULL, " +
                "`qty` INTEGER NOT NULL, `note` TEXT NOT NULL, `dateMillis` INTEGER NOT NULL)"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `site_payments` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`siteId` INTEGER NOT NULL, `amount` INTEGER NOT NULL, `dateMillis` INTEGER NOT NULL, " +
                "`note` TEXT NOT NULL)"
        )
    }
}


val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `transfers` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `fromAccountId` INTEGER NOT NULL, `toAccountId` INTEGER NOT NULL, `amount` INTEGER NOT NULL, `dateMillis` INTEGER NOT NULL, `note` TEXT NOT NULL)")
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `documents` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `category` INTEGER NOT NULL, `fileName` TEXT NOT NULL, `mimeType` TEXT NOT NULL, `sizeBytes` INTEGER NOT NULL, `storedName` TEXT NOT NULL, `note` TEXT NOT NULL, `createdAt` INTEGER NOT NULL)")
    }
}


val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `manual_daily_reports` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`siteId` INTEGER NOT NULL, " +
                "`dateMillis` INTEGER NOT NULL, " +
                "`content` TEXT NOT NULL)"
        )
    }
}
