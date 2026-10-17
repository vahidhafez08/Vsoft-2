package com.vsoft.app.data.backup

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.vsoft.app.data.db.AppDatabase
import com.vsoft.app.data.model.*
import com.vsoft.app.data.repository.SettingsRepository
import com.vsoft.app.util.JCal
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val db: AppDatabase,
    private val settings: SettingsRepository
) {
    suspend fun exportJson(): String = withContext(Dispatchers.IO) {
        val tables = JSONObject()
            .put("transactions", JSONArray().apply {
                db.transactionDao().getAllOnce().forEach { put(it.toJson()) }
            })
            .put("accounts", JSONArray().apply {
                db.accountDao().getAllOnce().forEach { put(it.toJson()) }
            })
            .put("transfers", JSONArray().apply {
                db.transferDao().getAllOnce().forEach { put(it.toJson()) }
            })
            .put("documents", JSONArray().apply {
                db.documentDao().getAllOnce().forEach { put(it.toJson()) }
            })
            .put("projects", JSONArray().apply {
                db.projectDao().getAllOnce().forEach { put(it.toJson()) }
            })
            .put("tasks", JSONArray().apply {
                db.taskDao().getAllOnce().forEach { put(it.toJson()) }
            })
            .put("subtasks", JSONArray().apply {
                db.subtaskDao().getAllOnce().forEach { put(it.toJson()) }
            })
            .put("sites", JSONArray().apply {
                db.jobDao().getAllSitesOnce().forEach { put(it.toJson()) }
            })
            .put("floors", JSONArray().apply {
                db.jobDao().getAllFloorsOnce().forEach { put(it.toJson()) }
            })
            .put("work_items", JSONArray().apply {
                db.jobDao().getAllWorkItemsOnce().forEach { put(it.toJson()) }
            })
            .put("work_logs", JSONArray().apply {
                db.jobDao().getAllWorkLogsOnce().forEach { put(it.toJson()) }
            })
            .put("site_payments", JSONArray().apply {
                db.jobDao().getAllSitePaymentsOnce().forEach { put(it.toJson()) }
            })

        JSONObject()
            .put("app", "VSOFT")
            .put("version", 1)
            .put("exportedAt", System.currentTimeMillis())
            .put(
                "settings",
                JSONObject()
                    .put("userName", settings.userName.value)
                    .put("themeMode", settings.themeMode.value)
                    .put("notifications", settings.notifications.value)
            )
            .put("tables", tables)
            .toString(2)
    }

    suspend fun importJson(json: String): Result<Unit> {
        return try {
            // Parse and validate every table before opening a write transaction.
            val parsed = withContext(Dispatchers.IO) { parseAndValidate(json) }
            withContext(Dispatchers.IO) {
                db.withTransaction {
                    // Delete children before their logical parents.
                    db.subtaskDao().deleteAllRaw()
                    db.jobDao().deleteAllWorkLogsRaw()
                    db.jobDao().deleteAllSitePaymentsRaw()
                    db.jobDao().deleteAllWorkItemsRaw()
                    db.jobDao().deleteAllFloorsRaw()
                    db.transactionDao().deleteAllRaw()
                    db.transferDao().deleteAllRaw()
                    db.documentDao().deleteAllRaw()
                    db.taskDao().deleteAllRaw()
                    db.projectDao().deleteAllRaw()
                    db.accountDao().deleteAllRaw()
                    db.jobDao().deleteAllSitesRaw()

                    // Insert parents before children, retaining original IDs.
                    db.accountDao().insertAllRaw(parsed.accounts)
                    db.transferDao().insertAllRaw(parsed.transfers)
                    db.documentDao().insertAllRaw(parsed.documents)
                    db.projectDao().insertAllRaw(parsed.projects)
                    db.jobDao().insertAllSitesRaw(parsed.sites)
                    db.transactionDao().insertAllRaw(parsed.transactions)
                    db.taskDao().insertAllRaw(parsed.tasks)
                    db.jobDao().insertAllFloorsRaw(parsed.floors)
                    db.jobDao().insertAllWorkItemsRaw(parsed.workItems)
                    db.subtaskDao().insertAllRaw(parsed.subtasks)
                    db.jobDao().insertAllWorkLogsRaw(parsed.workLogs)
                    db.jobDao().insertAllSitePaymentsRaw(parsed.sitePayments)
                }
            }

            // Preferences are changed only after the database transaction succeeds.
            parsed.userName?.let(settings::setUserName)
            parsed.themeMode?.let(settings::setThemeMode)
            parsed.notifications?.let(settings::setNotifications)
            Result.success(Unit)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    suspend fun exportTransactionsCsv(): String = withContext(Dispatchers.IO) {
        val rows = db.transactionDao().getAllOnce().sortedByDescending { it.dateMillis }
        val accountNames = db.accountDao().getAllOnce().associate { it.id to it.name }
        buildString {
            append('\uFEFF')
            append("تاریخ,نوع,دسته,حساب,مبلغ,توضیحات\r\n")
            rows.forEach { transaction ->
                val jalali = JCal.toJalali(transaction.dateMillis)
                val date = "%04d/%02d/%02d".format(Locale.US, jalali[0], jalali[1], jalali[2])
                val type = if (transaction.isIncome) "درآمد" else "هزینه"
                val fields = listOf(
                    date,
                    type,
                    transaction.category,
                    transaction.accountId?.let { accountNames[it] }.orEmpty(),
                    transaction.amount.toString(),
                    transaction.title
                )
                append(fields.joinToString(",") { it.csvEscape() })
                append("\r\n")
            }
        }
    }

    suspend fun writeText(uri: Uri, text: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val stream = context.contentResolver.openOutputStream(uri, "wt")
                ?: throw IllegalStateException("Unable to open output stream")
            stream.use { output ->
                OutputStreamWriter(output, StandardCharsets.UTF_8).use { writer ->
                    writer.write(text)
                    writer.flush()
                }
            }
            Result.success(Unit)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    suspend fun readText(uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            val stream = context.contentResolver.openInputStream(uri)
                ?: throw IllegalStateException("Unable to open input stream")
            val bytes = stream.use { input -> input.readLimited(MAX_IMPORT_BYTES) }
            Result.success(String(bytes, StandardCharsets.UTF_8).removePrefix("\uFEFF"))
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    private fun parseAndValidate(json: String): ParsedBackup {
        val root = JSONObject(json)
        require(root.optString("app") == "VSOFT") { "Not a VSOFT backup" }
        require(root.requiredInt("version") in MIN_VERSION..CURRENT_VERSION) { "Unsupported backup version" }
        root.requiredLong("exportedAt")

        val tableObject = root.requiredObject("tables")
        val transactions = tableObject.requiredArray("transactions").mapObjects { TransactionEntity(
            id = requiredLong("id"),
            title = requiredString("title"),
            amount = requiredLong("amount"),
            isIncome = requiredBoolean("isIncome"),
            category = requiredString("category"),
            dateMillis = requiredLong("dateMillis"),
            accountId = optionalNullableLong("accountId")
        ) }
        val accounts = tableObject.requiredArray("accounts").mapObjects { AccountEntity(
            id = requiredLong("id"),
            name = requiredString("name"),
            last4 = requiredString("last4"),
            balance = requiredLong("balance"),
            colorKey = requiredString("colorKey")
        ) }
        val transfers = if (!tableObject.has("transfers") || tableObject.isNull("transfers")) {
            emptyList()
        } else {
            tableObject.requiredArray("transfers").mapObjects { TransferEntity(
                id = requiredLong("id"),
                fromAccountId = requiredLong("fromAccountId"),
                toAccountId = requiredLong("toAccountId"),
                amount = requiredLong("amount"),
                dateMillis = requiredLong("dateMillis"),
                note = optionalString("note", "")
            ) }
        }.filter { transfer ->
            transfer.fromAccountId != transfer.toAccountId && transfer.amount > 0L &&
                accounts.any { it.id == transfer.fromAccountId } && accounts.any { it.id == transfer.toAccountId }
        }
        val documents = if (!tableObject.has("documents") || tableObject.isNull("documents")) {
            emptyList()
        } else {
            tableObject.requiredArray("documents").mapObjects { DocumentEntity(
                id = requiredLong("id"),
                title = requiredString("title"),
                category = optionalInt("category", 4).coerceIn(0, 4),
                fileName = requiredString("fileName"),
                mimeType = optionalString("mimeType", ""),
                sizeBytes = optionalLong("sizeBytes", 0L).coerceAtLeast(0L),
                storedName = requiredString("storedName"),
                note = optionalString("note", ""),
                createdAt = requiredLong("createdAt")
            ) }
        }
        val projects = tableObject.requiredArray("projects").mapObjects { ProjectEntity(
            id = requiredLong("id"),
            title = requiredString("title"),
            progress = requiredInt("progress"),
            dueDateMillis = optionalNullableLong("dueDateMillis")
        ) }
        val tasks = tableObject.requiredArray("tasks").mapObjects { TaskEntity(
            id = requiredLong("id"),
            title = requiredString("title"),
            description = optionalString("description", ""),
            isDone = optionalBoolean("isDone", false),
            priority = optionalInt("priority", 1),
            projectId = optionalNullableLong("projectId"),
            dueDateMillis = optionalNullableLong("dueDateMillis")
        ) }
        val subtasks = tableObject.requiredArray("subtasks").mapObjects { SubtaskEntity(
            id = requiredLong("id"),
            taskId = requiredLong("taskId"),
            title = requiredString("title"),
            isDone = optionalBoolean("isDone", false)
        ) }
        val sites = tableObject.requiredArray("sites").mapObjects { SiteEntity(
            id = requiredLong("id"),
            name = requiredString("name"),
            address = optionalString("address", ""),
            client = optionalString("client", ""),
            startDateMillis = requiredLong("startDateMillis"),
            contractAmount = optionalLong("contractAmount", 0L)
        ) }
        val floors = tableObject.requiredArray("floors").mapObjects { FloorEntity(
            id = requiredLong("id"),
            siteId = requiredLong("siteId"),
            name = requiredString("name")
        ) }
        val workItems = tableObject.requiredArray("work_items").mapObjects { WorkItemEntity(
            id = requiredLong("id"),
            siteId = requiredLong("siteId"),
            floorId = requiredLong("floorId"),
            title = requiredString("title"),
            unit = requiredString("unit"),
            targetQty = requiredInt("targetQty")
        ) }
        val workLogs = tableObject.requiredArray("work_logs").mapObjects { WorkLogEntity(
            id = requiredLong("id"),
            siteId = requiredLong("siteId"),
            floorId = requiredLong("floorId"),
            itemId = requiredLong("itemId"),
            qty = requiredInt("qty"),
            note = optionalString("note", ""),
            dateMillis = requiredLong("dateMillis")
        ) }
        val sitePayments = tableObject.requiredArray("site_payments").mapObjects { SitePaymentEntity(
            id = requiredLong("id"),
            siteId = requiredLong("siteId"),
            amount = requiredLong("amount"),
            dateMillis = requiredLong("dateMillis"),
            note = optionalString("note", "")
        ) }

        val settingsObject = if (root.has("settings") && !root.isNull("settings")) {
            root.requiredObject("settings")
        } else {
            JSONObject()
        }
        val userName = settingsObject.optionalStringOrNull("userName")
        val themeMode = settingsObject.optionalIntOrNull("themeMode")?.also {
            require(it in 0..2) { "Invalid theme mode" }
        }
        val notifications = settingsObject.optionalBooleanOrNull("notifications")

        return ParsedBackup(
            transactions.map { tx -> tx.copy(accountId = tx.accountId?.takeIf { id -> accounts.any { it.id == id } }) }, accounts, transfers, documents, projects, tasks, subtasks, sites, floors,
            workItems, workLogs, sitePayments, userName, themeMode, notifications
        )
    }

    private data class ParsedBackup(
        val transactions: List<TransactionEntity>,
        val accounts: List<AccountEntity>,
        val transfers: List<TransferEntity>,
        val documents: List<DocumentEntity>,
        val projects: List<ProjectEntity>,
        val tasks: List<TaskEntity>,
        val subtasks: List<SubtaskEntity>,
        val sites: List<SiteEntity>,
        val floors: List<FloorEntity>,
        val workItems: List<WorkItemEntity>,
        val workLogs: List<WorkLogEntity>,
        val sitePayments: List<SitePaymentEntity>,
        val userName: String?,
        val themeMode: Int?,
        val notifications: Boolean?
    )

    private fun TransactionEntity.toJson() = JSONObject()
        .put("id", id).put("title", title).put("amount", amount)
        .put("isIncome", isIncome).put("category", category).put("dateMillis", dateMillis)
        .put("accountId", accountId ?: JSONObject.NULL)

    private fun AccountEntity.toJson() = JSONObject()
        .put("id", id).put("name", name).put("last4", last4)
        .put("balance", balance).put("colorKey", colorKey)

    private fun TransferEntity.toJson() = JSONObject()
        .put("id", id).put("fromAccountId", fromAccountId).put("toAccountId", toAccountId)
        .put("amount", amount).put("dateMillis", dateMillis).put("note", note)

    private fun DocumentEntity.toJson() = JSONObject()
        .put("id", id).put("title", title).put("category", category)
        .put("fileName", fileName).put("mimeType", mimeType).put("sizeBytes", sizeBytes)
        .put("storedName", storedName).put("note", note).put("createdAt", createdAt)

    private fun ProjectEntity.toJson() = JSONObject()
        .put("id", id).put("title", title).put("progress", progress)
        .put("dueDateMillis", dueDateMillis ?: JSONObject.NULL)

    private fun TaskEntity.toJson() = JSONObject()
        .put("id", id).put("title", title).put("description", description)
        .put("isDone", isDone).put("priority", priority)
        .put("projectId", projectId ?: JSONObject.NULL)
        .put("dueDateMillis", dueDateMillis ?: JSONObject.NULL)

    private fun SubtaskEntity.toJson() = JSONObject()
        .put("id", id).put("taskId", taskId).put("title", title).put("isDone", isDone)

    private fun SiteEntity.toJson() = JSONObject()
        .put("id", id).put("name", name).put("address", address).put("client", client)
        .put("startDateMillis", startDateMillis).put("contractAmount", contractAmount)

    private fun FloorEntity.toJson() = JSONObject()
        .put("id", id).put("siteId", siteId).put("name", name)

    private fun WorkItemEntity.toJson() = JSONObject()
        .put("id", id).put("siteId", siteId).put("floorId", floorId)
        .put("title", title).put("unit", unit).put("targetQty", targetQty)

    private fun WorkLogEntity.toJson() = JSONObject()
        .put("id", id).put("siteId", siteId).put("floorId", floorId).put("itemId", itemId)
        .put("qty", qty).put("note", note).put("dateMillis", dateMillis)

    private fun SitePaymentEntity.toJson() = JSONObject()
        .put("id", id).put("siteId", siteId).put("amount", amount)
        .put("dateMillis", dateMillis).put("note", note)

    private fun JSONObject.requiredObject(key: String): JSONObject {
        require(has(key) && !isNull(key)) { "Missing object: $key" }
        return get(key) as? JSONObject ?: throw IllegalArgumentException("Invalid object: $key")
    }

    private fun JSONObject.requiredArray(key: String): JSONArray {
        require(has(key) && !isNull(key)) { "Missing array: $key" }
        return get(key) as? JSONArray ?: throw IllegalArgumentException("Invalid array: $key")
    }

    private fun JSONObject.requiredString(key: String): String {
        require(has(key) && !isNull(key)) { "Missing string: $key" }
        return get(key) as? String ?: throw IllegalArgumentException("Invalid string: $key")
    }

    private fun JSONObject.optionalString(key: String, default: String): String =
        if (!has(key) || isNull(key)) default else requiredString(key)

    private fun JSONObject.optionalStringOrNull(key: String): String? =
        if (!has(key) || isNull(key)) null else requiredString(key)

    private fun JSONObject.requiredLong(key: String): Long {
        require(has(key) && !isNull(key)) { "Missing integer: $key" }
        return get(key).toExactLong(key)
    }

    private fun JSONObject.optionalLong(key: String, default: Long): Long =
        if (!has(key) || isNull(key)) default else requiredLong(key)

    private fun JSONObject.optionalNullableLong(key: String): Long? =
        if (!has(key) || isNull(key)) null else requiredLong(key)

    private fun JSONObject.requiredInt(key: String): Int {
        val value = requiredLong(key)
        require(value in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong()) { "Integer out of range: $key" }
        return value.toInt()
    }

    private fun JSONObject.optionalInt(key: String, default: Int): Int =
        if (!has(key) || isNull(key)) default else requiredInt(key)

    private fun JSONObject.optionalIntOrNull(key: String): Int? =
        if (!has(key) || isNull(key)) null else requiredInt(key)

    private fun JSONObject.requiredBoolean(key: String): Boolean {
        require(has(key) && !isNull(key)) { "Missing boolean: $key" }
        return get(key) as? Boolean ?: throw IllegalArgumentException("Invalid boolean: $key")
    }

    private fun JSONObject.optionalBoolean(key: String, default: Boolean): Boolean =
        if (!has(key) || isNull(key)) default else requiredBoolean(key)

    private fun JSONObject.optionalBooleanOrNull(key: String): Boolean? =
        if (!has(key) || isNull(key)) null else requiredBoolean(key)

    private fun Any?.toExactLong(key: String): Long {
        val number = this as? Number ?: throw IllegalArgumentException("Invalid number: $key")
        val doubleValue = number.toDouble()
        require(doubleValue.isFinite() && doubleValue % 1.0 == 0.0) { "Invalid integer: $key" }
        require(doubleValue >= Long.MIN_VALUE.toDouble() && doubleValue <= Long.MAX_VALUE.toDouble()) {
            "Integer out of range: $key"
        }
        return number.toLong()
    }

    private fun <T> JSONArray.mapObjects(transform: JSONObject.() -> T): List<T> =
        (0 until length()).map { index ->
            val item = opt(index) as? JSONObject
                ?: throw IllegalArgumentException("Expected object at array index $index")
            item.transform()
        }

    private fun InputStream.readLimited(limit: Int): ByteArray {
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var total = 0
        while (true) {
            val read = read(buffer, 0, minOf(buffer.size, limit + 1 - total))
            if (read < 0) break
            total += read
            if (total > limit) throw IllegalArgumentException("Import file exceeds 20 MB")
            output.write(buffer, 0, read)
        }
        return output.toByteArray()
    }

    private fun String.csvEscape(): String =
        if (contains(',') || contains('"') || contains('\n') || contains('\r')) {
            "\"" + replace("\"", "\"\"") + "\""
        } else {
            this
        }

    companion object {
        private const val MAX_IMPORT_BYTES = 20 * 1024 * 1024
        private const val MIN_VERSION = 1
        private const val CURRENT_VERSION = 1
    }
}
