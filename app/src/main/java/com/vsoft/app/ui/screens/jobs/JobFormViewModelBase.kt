package com.vsoft.app.ui.screens.jobs

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.vsoft.app.data.model.*
import com.vsoft.app.data.repository.JobRepository
import com.vsoft.app.util.UiEvents
import com.vsoft.app.util.safeLaunch
import com.vsoft.app.util.startOfDay
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

open class JobFormViewModelBase(
    protected val handle: SavedStateHandle,
    protected val repository: JobRepository
) : ViewModel() {
    val id: Long = handle.get<Long>("id") ?: -1L
    val siteId: Long = handle.get<Long>("siteId") ?: -1L
    var title by mutableStateOf("")
    var name by mutableStateOf("")
    var address by mutableStateOf("")
    var client by mutableStateOf("")
    var amount by mutableStateOf("")
    var quantity by mutableStateOf("")
    var unit by mutableStateOf("عدد")
    var note by mutableStateOf("")
    var floorId by mutableStateOf<Long?>(null)
    var itemId by mutableStateOf<Long?>(null)
    var dateMillis by mutableStateOf(startOfDay(System.currentTimeMillis()))
    var floors by mutableStateOf<List<FloorEntity>>(emptyList())
    var items by mutableStateOf<List<WorkItemEntity>>(emptyList())
    var siteName by mutableStateOf("")
    var loading by mutableStateOf(true)
    var saving by mutableStateOf(false)
    private var loadedKind: String? = null
    private var originalSite: SiteEntity? = null
    private var originalFloor: FloorEntity? = null
    private var originalItem: WorkItemEntity? = null
    private var originalLog: WorkLogEntity? = null
    private var originalPayment: SitePaymentEntity? = null
    private val _done = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val done = _done.asSharedFlow()

    fun load(kind: String) {
        if (loadedKind == kind) return
        loadedKind = kind
        safeLaunch {
            try {
                val allSites = repository.sites().first()
                val site = if (kind == "site") allSites.firstOrNull { it.id == id } else allSites.firstOrNull { it.id == siteId }
                originalSite = site
                siteName = site?.name.orEmpty()
                val effectiveSiteId = if (kind == "site") id else siteId
                floors = if (effectiveSiteId >= 0L) repository.floors(effectiveSiteId).first() else emptyList()
                items = if (effectiveSiteId >= 0L) repository.items(effectiveSiteId).first() else emptyList()
                when (kind) {
                    "site" -> site?.let { name = it.name; address = it.address; client = it.client; amount = if (it.contractAmount > 0L) it.contractAmount.toString() else ""; dateMillis = it.startDateMillis }
                    "floor" -> if (id >= 0L) repository.floors(siteId).first().firstOrNull { it.id == id }?.let { originalFloor = it; name = it.name }
                    "item" -> if (id >= 0L) items.firstOrNull { it.id == id }?.let { originalItem = it; title = it.title; unit = it.unit; quantity = it.targetQty.toString(); floorId = it.floorId }
                    "log" -> if (id >= 0L) repository.logs(siteId).first().firstOrNull { it.id == id }?.let { originalLog = it; floorId = it.floorId; itemId = it.itemId; quantity = it.qty.toString(); note = it.note; dateMillis = it.dateMillis }
                    "payment" -> if (id >= 0L) repository.payments(siteId).first().firstOrNull { it.id == id }?.let { originalPayment = it; amount = it.amount.toString(); note = it.note; dateMillis = it.dateMillis }
                }
                if (kind == "item" && floorId == null) floorId = floors.firstOrNull()?.id
                if (kind == "log" && itemId == null) {
                    itemId = items.firstOrNull()?.id
                    floorId = items.firstOrNull()?.floorId ?: floors.firstOrNull()?.id
                }
            } catch (cancelled: CancellationException) { throw cancelled }
            finally { loading = false }
        }
    }

    fun save(kind: String) {
        if (saving) return
        val cleanName = name.trim()
        val cleanTitle = title.trim()
        if ((kind == "site" || kind == "floor") && cleanName.isBlank()) return
        if ((kind == "item") && cleanTitle.isBlank()) return
        if (kind == "item" && floorId == null) { UiEvents.show("ابتدا یک طبقه بساز"); return }
        if (kind == "log" && (itemId == null || (quantity.toIntOrNull() ?: 0) <= 0)) return
        if (kind == "payment" && (amount.toLatinDigits().filter(Char::isDigit).toLongOrNull() ?: 0L) <= 0L) return
        saving = true
        safeLaunch {
            try {
                val effectiveSiteId = if (kind == "site") (originalSite?.id ?: 0L) else siteId
                when (kind) {
                    "site" -> repository.saveSite(SiteEntity(id = originalSite?.id ?: 0L, name = cleanName.take(80), address = address.trim().take(200), client = client.trim().take(80), startDateMillis = dateMillis, contractAmount = amount.toLatinDigits().filter(Char::isDigit).take(15).toLongOrNull() ?: 0L))
                    "floor" -> repository.saveFloor(FloorEntity(id = originalFloor?.id ?: 0L, siteId = siteId, name = cleanName.take(80)))
                    "item" -> repository.saveItem(WorkItemEntity(id = originalItem?.id ?: 0L, siteId = siteId, floorId = floorId ?: return@safeLaunch, title = cleanTitle.take(100), unit = unit.take(30), targetQty = quantity.toLatinDigits().filter(Char::isDigit).take(9).toIntOrNull() ?: 0))
                    "log" -> repository.addLog(WorkLogEntity(id = originalLog?.id ?: 0L, siteId = siteId, floorId = floorId ?: return@safeLaunch, itemId = itemId ?: return@safeLaunch, qty = quantity.toLatinDigits().filter(Char::isDigit).take(9).toIntOrNull() ?: 0, note = note.trim().take(300), dateMillis = dateMillis))
                    "payment" -> repository.savePayment(SitePaymentEntity(id = originalPayment?.id ?: 0L, siteId = siteId, amount = amount.toLatinDigits().filter(Char::isDigit).take(15).toLongOrNull() ?: 0L, dateMillis = dateMillis, note = note.trim().take(200)))
                }
                UiEvents.show(when(kind) { "site" -> "کارگاه ذخیره شد ✅"; "floor" -> "طبقه ذخیره شد ✅"; "item" -> "آیتم کار ذخیره شد ✅"; "log" -> "گزارش ذخیره شد ✅"; else -> "دریافتی ذخیره شد ✅" })
                _done.tryEmit(Unit)
            } catch (cancelled: CancellationException) { throw cancelled }
            finally { saving = false }
        }
    }
}
