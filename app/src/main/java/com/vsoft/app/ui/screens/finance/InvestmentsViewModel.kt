package com.vsoft.app.ui.screens.finance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vsoft.app.data.market.MarketPrice
import com.vsoft.app.data.market.MarketRepository
import com.vsoft.app.data.model.InvestmentEntity
import com.vsoft.app.data.model.InvestmentTypeEntity
import com.vsoft.app.data.repository.InvestmentRepository
import com.vsoft.app.domain.InvestmentCalculator
import com.vsoft.app.domain.PortfolioSummary
import com.vsoft.app.util.UiEvents
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InvestmentsUiState(val types:List<InvestmentTypeEntity> = emptyList(),val items:List<InvestmentEntity> = emptyList()) {
    val summary:PortfolioSummary get()=InvestmentCalculator.summarize(items)
}

data class MarketUiState(
    val prices: List<MarketPrice> = emptyList(),
    val updatedAtJalali: String = "",
    val source: String = "",
    val isLoading: Boolean = false,
    val isCached: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class InvestmentsViewModel @Inject constructor(
    private val repo: InvestmentRepository,
    private val marketRepository: MarketRepository
):ViewModel() {
    val state=combine(repo.observeTypes(),repo.observeInvestments()){types,items->InvestmentsUiState(types,items)}.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),InvestmentsUiState())

    private val _marketState = MutableStateFlow(MarketUiState(isLoading = true))
    val marketState = _marketState.asStateFlow()

    init {
        refreshMarketPrices()
    }

    fun refreshMarketPrices() {
        if (_marketState.value.isLoading) return
        viewModelScope.launch {
            _marketState.value = _marketState.value.copy(isLoading = true, error = null)
            runCatching { marketRepository.fetchLatest() }
                .onSuccess { snapshot ->
                    val priceBySymbol = snapshot.prices.associateBy { it.symbol }
                    if (!snapshot.fromCache) {
                        val types = repo.observeTypes().first()
                        val items = repo.observeInvestments().first()
                        items.filter { it.status == "ACTIVE" }.forEach { item ->
                            val type = types.firstOrNull { it.id == item.typeId } ?: return@forEach
                            val symbol = resolveMarketSymbol(type.name, item.title) ?: return@forEach
                            val price = priceBySymbol[symbol] ?: return@forEach
                            if (!price.stale && price.price > 0.0 && price.price.isFinite()) {
                                runCatching { repo.updatePrice(item.id, price.price) }
                            }
                        }
                    }
                    val visibleSymbols = setOf(
                        "USD_IRR_FREE", "EUR_IRR_FREE", "GBP_IRR_FREE",
                        "GOLD_18K_IRR", "GOLD_24K_IRR",
                        "COIN_EMAMI_IRR", "COIN_HALF_IRR", "COIN_QUARTER_IRR",
                        "USDT_IRR"
                    )
                    _marketState.value = MarketUiState(
                        prices = snapshot.prices.filter { it.symbol in visibleSymbols },
                        updatedAtJalali = snapshot.updatedAtJalali,
                        source = snapshot.source,
                        isLoading = false,
                        isCached = snapshot.fromCache
                    )
                }
                .onFailure {
                    _marketState.value = _marketState.value.copy(
                        isLoading = false,
                        error = "دریافت قیمت‌ها ممکن نشد؛ اتصال اینترنت را بررسی کنید."
                    )
                }
        }
    }

    private fun resolveMarketSymbol(typeName: String, title: String): String? {
        val type = typeName.trim()
        val itemTitle = title.trim()
        return when (type) {
            "طلا" -> if (itemTitle.contains("۲۴") || itemTitle.contains("24")) "GOLD_24K_IRR" else "GOLD_18K_IRR"
            "سکه" -> when {
                itemTitle.contains("نیم") -> "COIN_HALF_IRR"
                itemTitle.contains("ربع") -> "COIN_QUARTER_IRR"
                itemTitle.contains("گرمی") -> "COIN_GRAMI_IRR"
                itemTitle.contains("بهار") -> "COIN_BAHAR_IRR"
                else -> "COIN_EMAMI_IRR"
            }
            "دلار" -> "USD_IRR_FREE"
            "یورو" -> "EUR_IRR_FREE"
            "ارز دیجیتال" -> if (itemTitle.contains("تتر")) "USDT_IRR" else null
            else -> null
        }
    }

    fun saveType(name:String,unit:String,icon:String,color:String)=viewModelScope.launch {
        if(name.isBlank()||unit.isBlank()){UiEvents.show("نام و واحد نوع سرمایه را وارد کنید");return@launch}
        repo.saveType(InvestmentTypeEntity(name=name.trim(),unitLabel=unit.trim(),iconKey=icon,colorHex=color,isDefault=false))
    }
    fun deleteType(type:InvestmentTypeEntity)=viewModelScope.launch {
        if(!repo.deleteType(type)) UiEvents.show("نوع پیش‌فرض یا نوع دارای سرمایه قابل حذف نیست")
    }
    suspend fun save(item:InvestmentEntity) {
        if(item.investedAmount<=0L||item.buyPrice<=0.0||item.quantity<=0.0||!item.buyPrice.isFinite()||!item.quantity.isFinite()){UiEvents.show("مبلغ، قیمت و مقدار باید بزرگ‌تر از صفر باشند");return}
        repo.save(item);UiEvents.show("سرمایه ذخیره شد")
    }
    fun delete(item:InvestmentEntity)=viewModelScope.launch { repo.delete(item) }
    fun updatePrice(item:InvestmentEntity,price:Double)=viewModelScope.launch { runCatching{repo.updatePrice(item.id,price)}.onFailure{UiEvents.show("قیمت معتبر نیست")} }
    fun updateGroup(typeId:Long,price:Double)=viewModelScope.launch { runCatching{repo.updateTypePrice(typeId,price)}.onFailure{UiEvents.show("قیمت معتبر نیست")} }
    fun sell(item:InvestmentEntity,price:Double)=viewModelScope.launch { runCatching{repo.sell(item,price)}.onFailure{UiEvents.show("قیمت فروش معتبر نیست")} }
}
