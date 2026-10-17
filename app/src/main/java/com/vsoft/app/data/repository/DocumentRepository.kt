package com.vsoft.app.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import com.vsoft.app.data.db.DocumentDao
import com.vsoft.app.data.model.DocumentEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class DocumentRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: DocumentDao
) {
    val documents: Flow<List<DocumentEntity>> = dao.observeAll()

    suspend fun addFromUri(uri: Uri, title: String, category: Int, note: String): Result<Long> =
        withContext(Dispatchers.IO) {
            var partialFile: File? = null
            try {
                val metadata = queryMetadata(uri)
                if (metadata.second != null && metadata.second > MAX_FILE_BYTES) {
                    throw IllegalArgumentException("حجم فایل بیشتر از ۲۵ مگابایت است")
                }
                val displayName = metadata.first.ifBlank { "document" }
                val directory = File(context.filesDir, "documents")
                if (!directory.exists() && !directory.mkdirs()) {
                    throw IllegalStateException("ساخت پوشه‌ی اسناد ناموفق بود")
                }
                val safeName = sanitizeFileName(displayName)
                val storedName = "${System.currentTimeMillis()}_${Random.nextInt(1000, 10000)}_$safeName"
                val destination = File(directory, File(storedName).name)
                partialFile = destination
                val input = context.contentResolver.openInputStream(uri)
                    ?: throw IllegalStateException("خواندن فایل ناموفق بود")
                input.use { source ->
                    FileOutputStream(destination).use { output ->
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        var copied = 0L
                        while (true) {
                            val count = source.read(buffer)
                            if (count < 0) break
                            copied += count
                            if (copied > MAX_FILE_BYTES) {
                                throw IllegalArgumentException("حجم فایل بیشتر از ۲۵ مگابایت است")
                            }
                            output.write(buffer, 0, count)
                        }
                        output.flush()
                    }
                }
                val actualSize = destination.length()
                val extension = displayName.substringAfterLast('.', "").lowercase()
                val mimeType = context.contentResolver.getType(uri)
                    ?: MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
                    ?: "application/octet-stream"
                val finalTitle = title.trim().ifBlank { displayName.substringBeforeLast('.', displayName).ifBlank { displayName } }
                val id = dao.insert(
                    DocumentEntity(
                        title = finalTitle,
                        category = category.coerceIn(0, 4),
                        fileName = displayName,
                        mimeType = mimeType,
                        sizeBytes = actualSize,
                        storedName = storedName,
                        note = note.trim(),
                        createdAt = System.currentTimeMillis()
                    )
                )
                Result.success(id)
            } catch (cancelled: CancellationException) {
                partialFile?.let { runCatching { it.delete() } }
                throw cancelled
            } catch (exception: Exception) {
                partialFile?.let { runCatching { it.delete() } }
                Result.failure(exception)
            }
        }

    suspend fun update(document: DocumentEntity) {
        dao.update(document.copy(category = document.category.coerceIn(0, 4)))
    }

    suspend fun delete(document: DocumentEntity) {
        dao.delete(document)
        try {
            fileOf(document).delete()
        } catch (_: Exception) {
            // The row is removed even if the file has already disappeared or cannot be deleted.
        }
    }

    fun fileOf(document: DocumentEntity): File =
        File(File(context.filesDir, "documents"), File(document.storedName).name)

    fun fileExists(document: DocumentEntity): Boolean =
        try { fileOf(document).isFile } catch (_: Exception) { false }

    fun uriFor(document: DocumentEntity): Uri? =
        try {
            val file = fileOf(document)
            if (!file.isFile) null
            else FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (_: Exception) {
            null
        }

    private fun queryMetadata(uri: Uri): Pair<String, Long?> {
        var name = ""
        var size: Long? = null
        try {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)
                ?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIndex >= 0 && !cursor.isNull(nameIndex)) name = cursor.getString(nameIndex).orEmpty()
                        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                        if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) size = cursor.getLong(sizeIndex).takeIf { it >= 0L }
                    }
                }
        } catch (_: Exception) {
            // Some document providers don't expose metadata; the stream is still copied safely.
        }
        if (name.isBlank()) name = uri.lastPathSegment?.substringAfterLast('/')?.takeIf { it.isNotBlank() } ?: "document"
        return name to size
    }

    companion object {
        const val MAX_FILE_BYTES = 25L * 1024L * 1024L

        fun sanitizeFileName(fileName: String): String {
            val safe = fileName.replace(Regex("[^\\p{L}\\p{N}._-]"), "_").take(100)
            return safe.ifBlank { "document" }
        }
    }
}
