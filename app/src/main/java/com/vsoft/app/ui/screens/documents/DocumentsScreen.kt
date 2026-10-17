package com.vsoft.app.ui.screens.documents

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vsoft.app.data.model.DocumentEntity
import com.vsoft.app.ui.components.EmptyState
import com.vsoft.app.ui.components.ScreenHeader
import com.vsoft.app.ui.components.VCard
import com.vsoft.app.ui.theme.BrandBlue
import com.vsoft.app.ui.theme.ExpenseRed
import com.vsoft.app.ui.theme.Vsoft
import com.vsoft.app.util.JCal
import com.vsoft.app.util.UiEvents
import com.vsoft.app.util.formatSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DocumentsScreen(
    onBack: () -> Unit,
    viewModel: DocumentsViewModel = hiltViewModel()
) {
    val documents by viewModel.uiState.collectAsStateWithLifecycle()
    val allDocuments by viewModel.allDocuments.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val categoryFilter by viewModel.categoryFilter.collectAsStateWithLifecycle()
    val busy by viewModel.busy.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var pendingUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var showNewSheet by rememberSaveable { mutableStateOf(false) }
    var editing by remember { mutableStateOf<DocumentEntity?>(null) }
    var pendingDelete by remember { mutableStateOf<DocumentEntity?>(null) }
    var submitRequested by remember { mutableStateOf(false) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            pendingUri = uri
            showNewSheet = true
        }
    }

    LaunchedEffect(busy, submitRequested) {
        if (submitRequested && !busy) {
            showNewSheet = false
            editing = null
            pendingUri = null
            submitRequested = false
        }
    }

    fun openDocument(document: DocumentEntity) {
        if (!viewModel.exists(document)) return
        try {
            val uri = viewModel.uriFor(document)
                ?: throw IllegalStateException("File URI unavailable")
            val mime = document.mimeType.ifBlank { "application/octet-stream" }
            val intent = Intent(Intent.ACTION_VIEW).setDataAndType(uri, mime)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            context.startActivity(Intent.createChooser(intent, "باز کردن سند"))
        } catch (_: ActivityNotFoundException) {
            UiEvents.show("برنامه‌ای برای باز کردن این فایل پیدا نشد")
        } catch (_: Exception) {
            UiEvents.show("برنامه‌ای برای باز کردن این فایل پیدا نشد")
        }
    }

    fun shareDocument(document: DocumentEntity) {
        if (!viewModel.exists(document)) return
        try {
            val uri = viewModel.uriFor(document)
                ?: throw IllegalStateException("File URI unavailable")
            val mime = document.mimeType.ifBlank { "application/octet-stream" }
            val intent = Intent(Intent.ACTION_SEND).setType(mime)
                .putExtra(Intent.EXTRA_STREAM, uri)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.clipData = ClipData.newUri(context.contentResolver, document.fileName, uri)
            context.startActivity(Intent.createChooser(intent, "اشتراک‌گذاری سند"))
        } catch (_: ActivityNotFoundException) {
            UiEvents.show("برنامه‌ای برای باز کردن این فایل پیدا نشد")
        } catch (_: Exception) {
            UiEvents.show("برنامه‌ای برای باز کردن این فایل پیدا نشد")
        }
    }

    Box(Modifier.fillMaxSize().background(Vsoft.colors.bg)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item(key = "documents_header") {
                ScreenHeader(
                    title = "اسناد",
                    subtitle = allDocuments.size.toString().toPersianDigits() + " سند",
                    onBack = onBack
                )
            }
            item(key = "documents_search") {
                OutlinedTextField(
                    value = query,
                    onValueChange = { viewModel.query.value = it.take(80) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    placeholder = { Text("جستجو در اسناد...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { viewModel.query.value = "" }) {
                                Icon(Icons.Filled.Close, contentDescription = "پاک کردن")
                            }
                        }
                    }
                )
            }
            item(key = "documents_categories") {
                Row(
                    Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = categoryFilter == null,
                        onClick = { viewModel.categoryFilter.value = null },
                        label = { Text("همه") },
                        colors = categoryChipColors()
                    )
                    DocumentCategories.all.forEachIndexed { index, category ->
                        FilterChip(
                            selected = categoryFilter == index,
                            onClick = { viewModel.categoryFilter.value = index },
                            label = { Text(category.title) },
                            colors = categoryChipColors()
                        )
                    }
                }
            }
            if (documents.isEmpty()) {
                item(key = "documents_empty") {
                    val noDocumentsAtAll = allDocuments.isEmpty()
                    EmptyState(
                        icon = if (noDocumentsAtAll) Icons.Filled.Folder else Icons.Filled.Search,
                        title = if (noDocumentsAtAll) "هنوز سندی اضافه نکردی" else "موردی پیدا نشد",
                        subtitle = if (noDocumentsAtAll) "فایل‌های مهمت رو اینجا نگه دار و دسته‌بندی کن" else "عبارت جستجو یا دسته‌بندی رو تغییر بده"
                    )
                }
            }
            items(documents, key = { it.id }) { document ->
                DocumentRow(
                    document = document,
                    exists = viewModel.exists(document),
                    file = viewModel.fileOf(document),
                    onOpen = { openDocument(document) },
                    onShare = { shareDocument(document) },
                    onEdit = { editing = document },
                    onDelete = { pendingDelete = document },
                    modifier = Modifier.animateItemPlacement()
                )
            }
        }

        ExtendedFloatingActionButton(
            onClick = { picker.launch(arrayOf("*/*")) },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            containerColor = BrandBlue,
            contentColor = Color.White,
            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            text = { Text("افزودن سند") }
        )
    }

    if (showNewSheet && pendingUri != null) {
        DocumentEditSheet(
            initial = null,
            busy = busy,
            onDismiss = { if (!busy) { showNewSheet = false; pendingUri = null } },
            onSave = { title, category, note ->
                val uri = pendingUri
                if (uri != null && !busy) {
                    viewModel.add(uri, title, category, note)
                    submitRequested = true
                }
            }
        )
    }

    editing?.let { document ->
        DocumentEditSheet(
            initial = document,
            busy = busy,
            onDismiss = { if (!busy) editing = null },
            onSave = { title, category, note ->
                if (!busy && title.isNotBlank()) {
                    viewModel.save(document.copy(title = title.trim(), category = category, note = note.trim()))
                    submitRequested = true
                }
            }
        )
    }

    pendingDelete?.let { document ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            containerColor = Vsoft.colors.surface,
            title = { Text("حذف سند", color = Vsoft.colors.textPrimary) },
            text = { Text("این سند و فایلش از برنامه حذف می‌شه. ادامه می‌دی؟", color = Vsoft.colors.textPrimary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.delete(document)
                        pendingDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = ExpenseRed)
                ) { Text("حذف") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("انصراف") }
            }
        )
    }
}

@Composable
private fun categoryChipColors() = FilterChipDefaults.filterChipColors(
    containerColor = Vsoft.colors.surfaceHigh,
    labelColor = Vsoft.colors.textPrimary,
    selectedContainerColor = BrandBlue,
    selectedLabelColor = Color.White
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DocumentRow(
    document: DocumentEntity,
    exists: Boolean,
    file: java.io.File,
    onOpen: () -> Unit,
    onShare: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value != SwipeToDismissBoxValue.Settled) {
                onDelete()
                false
            } else true
        }
    )
    val shape = RoundedCornerShape(18.dp)
    val categoryColor = DocumentCategories.color(document.category)
    val cachedBitmap = remember(document.id) { mutableStateOf<Bitmap?>(null) }
    val producedBitmap by produceState<Bitmap?>(
        initialValue = cachedBitmap.value,
        key1 = document.id,
        key2 = document.storedName,
        key3 = "${exists}:${document.mimeType}"
    ) {
        if (value == null && exists && document.mimeType.startsWith("image/", ignoreCase = true)) {
            value = withContext(Dispatchers.IO) {
                try {
                    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    BitmapFactory.decodeFile(file.absolutePath, bounds)
                    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
                        null
                    } else {
                        val maxSide = maxOf(bounds.outWidth, bounds.outHeight)
                        val sample = if (maxSide > 96) (maxSide / 96).coerceAtLeast(1) else 1
                        val options = BitmapFactory.Options().apply { inSampleSize = sample }
                        BitmapFactory.decodeFile(file.absolutePath, options)
                    }
                } catch (cancelled: kotlinx.coroutines.CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    null
                }
            }
            cachedBitmap.value = value
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        backgroundContent = {
            Row(
                Modifier.fillMaxSize().clip(shape).background(ExpenseRed.copy(alpha = 0.16f)).padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Delete, contentDescription = null, tint = ExpenseRed)
            }
        }
    ) {
        VCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = exists, onClick = onOpen)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val preview = producedBitmap ?: cachedBitmap.value
                if (preview != null) {
                    Image(
                        bitmap = preview.asImageBitmap(),
                        contentDescription = "پیش‌نمایش سند",
                        modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp))
                    )
                } else {
                    Box(
                        Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(categoryColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(DocumentCategories.icon(document.category), contentDescription = null, tint = categoryColor, modifier = Modifier.size(24.dp))
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        document.title,
                        color = Vsoft.colors.textPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    val jalali = JCal.toJalali(document.createdAt)
                    val jalaliDate = JCal.fa(jalali[0]) + "/" + JCal.fa(jalali[1]) + "/" + JCal.fa(jalali[2])
                    Text(
                        DocumentCategories.title(document.category) + " • " + formatSize(document.sizeBytes) + " • " + jalaliDate,
                        color = Vsoft.colors.textSecondary,
                        fontSize = 10.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (document.note.isNotBlank()) {
                        Text(
                            document.note,
                            color = Vsoft.colors.textSecondary,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (!exists) {
                        Text("فایل در دسترس نیست", color = ExpenseRed, fontSize = 11.sp)
                    }
                }
                IconButton(onClick = onShare, enabled = exists) {
                    Icon(Icons.Filled.Share, contentDescription = "اشتراک‌گذاری سند", tint = if (exists) Vsoft.colors.textSecondary else Vsoft.colors.textSecondary.copy(alpha = 0.35f))
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Filled.Edit, contentDescription = "ویرایش سند", tint = Vsoft.colors.textSecondary)
                }
            }
        }
    }
}
