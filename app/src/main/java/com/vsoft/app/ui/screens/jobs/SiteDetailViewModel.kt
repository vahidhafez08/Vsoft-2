package com.vsoft.app.ui.screens.jobs

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vsoft.app.data.model.*
import com.vsoft.app.data.repository.JobRepository
import com.vsoft.app.util.jobProgress
import com.vsoft.app.util.safeLaunch
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ItemUi(val item: WorkItemEntity, val done: Int) {
    val remaining: Int get() = (item.targetQty - done).coerceAtLeast(0)
    val complete: Boolean get() = done >= item.targetQty
}

data class FloorUi(val floor: FloorEntity, val items: List<ItemUi>, val progress: Int)

data class LogUi(val log: WorkLogEntity, val floorName: String, val itemTitle: String, val unit: String)

data class SiteDetailUiState(
    val site: SiteEntity? = null,
    val floors: List<FloorUi> = emptyList(),
    val logs: List<LogUi> = emptyList(),
    val payments: List<SitePaymentEntity> = emptyList(),
    val progress: Int = 0,
    val itemsTotal: Int = 0,
    val itemsDone: Int = 0,
    val received: Long = 0L,
    val loaded: Boolean = false
)

@HiltViewModel
class SiteDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repo: JobRepository
) : ViewModel() {

    private val siteId: Long = savedStateHandle.get<Long>("id") ?: -1L

    val state: StateFlow<SiteDetailUiState> =
        combine(repo.site(siteId), repo.floors(siteId), repo.items(siteId), repo.logs(siteId), repo.payments(siteId)) { site, floors, items, logs, pays ->
            val doneByItem = logs.groupBy { it.itemId }.mapValues { e -> e.value.sumOf { it.qty } }
            val itemsByFloor = items.groupBy { it.floorId }
            val floorUis = floors.map { f ->
                val its = itemsByFloor[f.id].orEmpty()
                FloorUi(f, its.map { ItemUi(it, doneByItem[it.id] ?: 0) }, (jobProgress(its, doneByItem) * 100).toInt())
            }
            val floorNames = floors.associate { it.id to it.name }
            val itemMap = items.associateBy { it.id }
            val logUis = logs.map { l ->
                val item = itemMap[l.itemId]
                LogUi(l, floorNames[l.floorId] ?: "-", item?.title ?: "-", item?.unit ?: "")
            }
            SiteDetailUiState(
                site = site,
                floors = floorUis,
                logs = logUis,
                payments = pays,
                progress = (jobProgress(items, doneByItem) * 100).toInt(),
                itemsTotal = items.size,
                itemsDone = items.count { (doneByItem[it.id] ?: 0) >= it.targetQty },
                received = pays.sumOf { it.amount },
                loaded = true
            )
        }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SiteDetailUiState())

    fun saveSite(site: SiteEntity) { safeLaunch { repo.saveSite(site) } }
    fun deleteSite() { safeLaunch { repo.deleteSite(siteId) } }

    fun saveFloor(existing: FloorEntity?, name: String) {
        safeLaunch {
            repo.saveFloor(existing?.copy(name = name) ?: FloorEntity(siteId = siteId, name = name))
        }
    }

    fun deleteFloor(id: Long) { safeLaunch { repo.deleteFloor(id) } }

    fun saveItem(existing: WorkItemEntity?, floorId: Long, title: String, unit: String, target: Int) {
        safeLaunch {
            repo.saveItem(
                existing?.copy(title = title, unit = unit, targetQty = target)
                    ?: WorkItemEntity(siteId = siteId, floorId = floorId, title = title, unit = unit, targetQty = target)
            )
        }
    }

    fun deleteItem(id: Long) { safeLaunch { repo.deleteItem(id) } }

    fun addLog(item: WorkItemEntity, qty: Int, note: String, dateMillis: Long) {
        safeLaunch {
            repo.addLog(
                WorkLogEntity(siteId = siteId, floorId = item.floorId, itemId = item.id, qty = qty, note = note, dateMillis = dateMillis)
            )
        }
    }

    fun deleteLog(id: Long) { safeLaunch { repo.deleteLog(id) } }

    fun savePayment(amount: Long, note: String, dateMillis: Long) {
        safeLaunch {
            repo.savePayment(SitePaymentEntity(siteId = siteId, amount = amount, dateMillis = dateMillis, note = note))
        }
    }

    fun deletePayment(id: Long) { safeLaunch { repo.deletePayment(id) } }
}
