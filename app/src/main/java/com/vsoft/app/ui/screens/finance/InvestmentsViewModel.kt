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
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

data class InvestmentsUiState(val types:List<InvestmentTypeEntity> = emptyList(),val items:List<InvestmentEntity> = emptyList()) {
    val summary:PortfolioSummary get()=InvestmentCalculator.summarize(items)
}

data class MarketUiState(
    val prices: List<MarketPrice> = emptyList(),
    val allPrices: List<MarketPrice> = emptyList(),
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

    private val _marketState = MutableStateFlow(MarketUiState())
    val marketState = _marketState.asStateFlow()

    init {
        viewModelScope.launch {
            val existingTypes = repo.observeTypes().first()
            if (existingTypes.none { it.iconKey == "gold_milligram" }) {
                repo.saveType(
                    InvestmentTypeEntity(
                        name = "طلای میلی‌گرمی",
                        iconKey = "gold_milligram",
                        colorHex = "#F5B301",
                        unitLabel = "میلی‌گرم",
                        isDefault = true
                    )
                )
            }
        }
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
                            val symbol = item.marketSymbol ?: resolveMarketSymbol(type.name, item.title) ?: return@forEach
                            val price = priceBySymbol[symbol] ?: return@forEach
                            val normalizedPrice = if (type.iconKey == "gold_milligram" && symbol.startsWith("GOLD_")) {
                                price.price / 1000.0
                            } else price.price
                            if (!price.stale && price.currency in setOf("IRT", "IRR") && normalizedPrice > 0.0 && normalizedPrice.isFinite()) {
                                runCatching { repo.updatePrice(item.id, normalizedPrice) }
                            }
                        }
                    }
                    val freshPrices = snapshot.prices.filter { !it.stale && it.currency in setOf("IRT", "IRR") }
                    val visiblePrices = buildList {
                        addAll(freshPrices.filter { it.category.equals("currency", ignoreCase = true) }.sortedBy { it.name }.take(8))
                        addAll(freshPrices.filter { it.category.equals("gold", ignoreCase = true) }.sortedBy { it.name }.take(3))
                        addAll(freshPrices.filter { it.category.equals("coin", ignoreCase = true) }.sortedBy { it.name }.take(5))
                        addAll(freshPrices.filter { it.category.equals("crypto", ignoreCase = true) }.sortedBy { it.name }.take(4))
                    }
                    _marketState.value = MarketUiState(
                        prices = visiblePrices,
                        allPrices = snapshot.prices,
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
            "طلا", "طلای میلی‌گرمی" -> if (itemTitle.contains("۲۴") || itemTitle.contains("24")) "GOLD_24K_IRR" else "GOLD_18K_IRR"
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

    suspend fun save(
        item: InvestmentEntity,
        marketSymbol: String,
        purchasePrice: Double = item.buyPrice,
        priceDivisor: Double = 1.0
    ): Boolean {
        if (item.quantity <= 0.0 || !item.quantity.isFinite() || purchasePrice <= 0.0 || !purchasePrice.isFinite() ||
            priceDivisor <= 0.0 || !priceDivisor.isFinite()) {
            UiEvents.show("مقدار و قیمت خرید باید معتبر و بزرگ‌تر از صفر باشند")
            return false
        }
        return try {
            val snapshot = marketRepository.fetchLatest()
            if (snapshot.fromCache) {
                UiEvents.show("قیمت تازه دریافت نشد؛ اتصال اینترنت را بررسی و دوباره تلاش کنید")
                return false
            }
            val price = snapshot.prices.firstOrNull { it.symbol == marketSymbol }
            if (price == null || price.stale || price.currency !in setOf("IRT", "IRR") ||
                price.price <= 0.0 || !price.price.isFinite()) {
                UiEvents.show("قیمت معتبر و به‌روز این دارایی در بازار موجود نیست")
                return false
            }
            val total = item.quantity * purchasePrice
            if (!total.isFinite() || total <= 0.0 || total > Long.MAX_VALUE.toDouble()) {
                UiEvents.show("ارزش کل دارایی خارج از محدودهٔ قابل ذخیره است")
                return false
            }
            val totalAmount = BigDecimal.valueOf(total).setScale(0, RoundingMode.HALF_UP).longValueExact()
            val now = System.currentTimeMillis()
            repo.save(item.copy(
                title = if (priceDivisor == 1000.0) "طلای میلی‌گرمی" else price.name,
                investedAmount = totalAmount,
                buyPrice = purchasePrice,
                currentPrice = price.price / priceDivisor,
                priceUpdatedAtMillis = now,
                marketSymbol = price.symbol,
                marketUnit = if (priceDivisor == 1000.0) "میلی‌گرم" else price.unit
            ))
            UiEvents.show("سرمایه با قیمت روز ذخیره شد")
            true
        } catch (error: Exception) {
            UiEvents.show("ثبت انجام نشد؛ دریافت قیمت معتبر از بازار ممکن نبود")
            false
        }
    }

    fun updateInvestment(item: InvestmentEntity, quantity: Double, buyPrice: Double, note: String, onSaved: () -> Unit) = viewModelScope.launch {
        val invested = InvestmentCalculator.investedFrom(quantity, buyPrice)
        if (invested == null) {
            UiEvents.show("مقدار و قیمت خرید باید معتبر و بزرگ‌تر از صفر باشند")
            return@launch
        }
        runCatching {
            repo.save(item.copy(
                quantity = quantity,
                buyPrice = buyPrice,
                investedAmount = invested,
                note = note
            ))
        }.onSuccess {
            UiEvents.show("تغییرات سرمایه ذخیره شد")
            onSaved()
        }.onFailure {
            UiEvents.show("ذخیره تغییرات انجام نشد")
        }
    }

    fun delete(item:InvestmentEntity)=viewModelScope.launch { repo.delete(item) }
    fun updatePrice(item:InvestmentEntity,price:Double)=viewModelScope.launch { runCatching{repo.updatePrice(item.id,price)}.onFailure{UiEvents.show("قیمت معتبر نیست")} }
    fun updateGroup(typeId:Long,price:Double)=viewModelScope.launch { runCatching{repo.updateTypePrice(typeId,price)}.onFailure{UiEvents.show("قیمت معتبر نیست")} }
    fun sell(item:InvestmentEntity,price:Double)=viewModelScope.launch { runCatching{repo.sell(item,price)}.onFailure{UiEvents.show("قیمت فروش معتبر نیست")} }
}
