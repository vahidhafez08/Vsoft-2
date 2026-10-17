package com.vsoft.app.ui.screens.documents

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vsoft.app.data.model.DocumentEntity
import com.vsoft.app.data.repository.DocumentRepository
import com.vsoft.app.util.UiEvents
import com.vsoft.app.util.safeLaunch
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class DocumentsViewModel @Inject constructor(
    private val repository: DocumentRepository
) : ViewModel() {
    val query = MutableStateFlow("")
    val categoryFilter = MutableStateFlow<Int?>(null)
    val busy = MutableStateFlow(false)

    val uiState: StateFlow<List<DocumentEntity>> = combine(
        repository.documents, query, categoryFilter
    ) { documents, search, category ->
        val q = search.trim()
        documents.filter { document ->
            (category == null || document.category == category) &&
                (q.isBlank() || document.title.contains(q, ignoreCase = true) ||
                    document.fileName.contains(q, ignoreCase = true) ||
                    document.note.contains(q, ignoreCase = true))
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun add(uri: Uri, title: String, category: Int, note: String) {
        if (busy.value) return
        busy.value = true
        safeLaunch {
            try {
                val result = repository.addFromUri(uri, title, category, note)
                result.fold(
                    onSuccess = { UiEvents.show("سند ذخیره شد ✅") },
                    onFailure = { UiEvents.show(it.message?.takeIf(String::isNotBlank) ?: "ذخیره‌ی سند ناموفق بود") }
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (exception: Exception) {
                UiEvents.show(exception.message?.takeIf(String::isNotBlank) ?: "ذخیره‌ی سند ناموفق بود")
            } finally {
                busy.value = false
            }
        }
    }

    fun save(document: DocumentEntity) {
        if (busy.value) return
        busy.value = true
        safeLaunch {
            try {
                repository.update(document)
                UiEvents.show("سند ذخیره شد ✅")
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (exception: Exception) {
                UiEvents.show(exception.message?.takeIf(String::isNotBlank) ?: "ذخیره‌ی سند ناموفق بود")
            } finally {
                busy.value = false
            }
        }
    }

    fun delete(document: DocumentEntity) {
        if (busy.value) return
        busy.value = true
        safeLaunch {
            try {
                repository.delete(document)
                UiEvents.show("حذف شد")
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (exception: Exception) {
                UiEvents.show(exception.message?.takeIf(String::isNotBlank) ?: "حذف سند ناموفق بود")
            } finally {
                busy.value = false
            }
        }
    }

    fun uriFor(document: DocumentEntity): Uri? = repository.uriFor(document)
    fun exists(document: DocumentEntity): Boolean = repository.fileExists(document)
    fun fileOf(document: DocumentEntity) = repository.fileOf(document)
}
