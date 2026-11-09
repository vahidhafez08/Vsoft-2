package com.vsoft.app.ui.screens.finance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vsoft.app.data.model.InvestmentEntity
import com.vsoft.app.data.model.InvestmentTypeEntity
import com.vsoft.app.data.repository.InvestmentRepository
import com.vsoft.app.domain.InvestmentCalculator
import com.vsoft.app.domain.PortfolioSummary
import com.vsoft.app.util.UiEvents
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InvestmentsUiState(val types:List<InvestmentTypeEntity> = emptyList(),val items:List<InvestmentEntity> = emptyList()) {
    val summary:PortfolioSummary get()=InvestmentCalculator.summarize(items)
}
@HiltViewModel
class InvestmentsViewModel @Inject constructor(private val repo:InvestmentRepository):ViewModel() {
    val state=combine(repo.observeTypes(),repo.observeInvestments()){types,items->InvestmentsUiState(types,items)}.stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),InvestmentsUiState())
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