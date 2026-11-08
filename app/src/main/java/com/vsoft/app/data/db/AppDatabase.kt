package com.vsoft.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.vsoft.app.data.model.*

@Database(
    entities = [
        TransactionEntity::class, DebtEntity::class, InvestmentTypeEntity::class, InvestmentEntity::class, AccountEntity::class, TransferEntity::class, DocumentEntity::class, ProjectEntity::class, TaskEntity::class, SubtaskEntity::class,
        SiteEntity::class, FloorEntity::class, WorkItemEntity::class, WorkLogEntity::class, SitePaymentEntity::class, ManualDailyReportEntity::class
    ],
    version = 9,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun debtDao(): DebtDao
    abstract fun investmentDao(): InvestmentDao
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

val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `transactions` ADD COLUMN `debtId` INTEGER")
        db.execSQL("CREATE TABLE IF NOT EXISTS `debts` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `personName` TEXT NOT NULL, `type` TEXT NOT NULL, `totalAmount` INTEGER NOT NULL, `dateMillis` INTEGER NOT NULL, `dueDateMillis` INTEGER, `note` TEXT NOT NULL, `isSettled` INTEGER NOT NULL, `isManuallySettled` INTEGER NOT NULL)")
    }
}

fun seedDefaultInvestmentTypes(db: SupportSQLiteDatabase) {
    val rows = listOf(
        arrayOf<Any>("طلا","gold","#F5B301","گرم"),
        arrayOf<Any>("سکه","coin","#E0A100","عدد"),
        arrayOf<Any>("دلار","dollar","#2E9E5B","دلار"),
        arrayOf<Any>("یورو","euro","#2F6FDE","یورو"),
        arrayOf<Any>("ارز دیجیتال","crypto","#F7931A","واحد"),
        arrayOf<Any>("سهام","stock","#7B4FE0","سهم"),
        arrayOf<Any>("صندوق سرمایه‌گذاری","fund","#00A3A3","واحد"),
        arrayOf<Any>("ملک","home","#8D6E63","متر"),
        arrayOf<Any>("خودرو","car","#546E7A","دستگاه")
    )
    rows.forEach { row ->
        db.execSQL("INSERT INTO `investment_types` (`name`,`iconKey`,`colorHex`,`unitLabel`,`isDefault`) VALUES (?,?,?,?,1)", row)
    }
}
val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `investment_types` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `iconKey` TEXT NOT NULL, `colorHex` TEXT NOT NULL, `unitLabel` TEXT NOT NULL, `isDefault` INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE IF NOT EXISTS `investments` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `typeId` INTEGER NOT NULL, `title` TEXT NOT NULL, `investedAmount` INTEGER NOT NULL, `buyPrice` REAL NOT NULL, `quantity` REAL NOT NULL, `currentPrice` REAL, `priceUpdatedAtMillis` INTEGER, `buyDateMillis` INTEGER NOT NULL, `note` TEXT NOT NULL, `status` TEXT NOT NULL, `sellPrice` REAL)")
        seedDefaultInvestmentTypes(db)
    }
}
