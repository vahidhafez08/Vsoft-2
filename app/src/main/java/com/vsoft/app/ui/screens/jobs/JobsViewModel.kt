package com.vsoft.app.ui.screens.jobs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vsoft.app.data.model.SiteEntity
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

data class SiteCardUi(val site: SiteEntity, val progress: Int, val itemsDone: Int, val itemsTotal: Int, val received: Long)

data class JobsUiState(val sites: List<SiteCardUi> = emptyList(), val loaded: Boolean = false)

@HiltViewModel
class JobsViewModel @Inject constructor(private val repo: JobRepository) : ViewModel() {

    val state: StateFlow<JobsUiState> =
        combine(repo.sites(), repo.allItems(), repo.allLogs(), repo.allPayments()) { sites, items, logs, pays ->
            val doneByItem = logs.groupBy { it.itemId }.mapValues { e -> e.value.sumOf { it.qty } }
            val itemsBySite = items.groupBy { it.siteId }
            val paidBySite = pays.groupBy { it.siteId }.mapValues { e -> e.value.sumOf { it.amount } }
            JobsUiState(
                sites = sites.map { s ->
                    val its = itemsBySite[s.id].orEmpty()
                    SiteCardUi(
                        site = s,
                        progress = (jobProgress(its, doneByItem) * 100).toInt(),
                        itemsDone = its.count { (doneByItem[it.id] ?: 0) >= it.targetQty },
                        itemsTotal = its.size,
                        received = paidBySite[s.id] ?: 0L
                    )
                },
                loaded = true
            )
        }.flowOn(Dispatchers.Default).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), JobsUiState())

    fun saveSite(site: SiteEntity) {
        safeLaunch { repo.saveSite(site) }
    }
}
