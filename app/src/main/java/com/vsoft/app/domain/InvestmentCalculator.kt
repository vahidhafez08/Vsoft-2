package com.vsoft.app.domain

import com.vsoft.app.data.model.InvestmentEntity
import com.vsoft.app.data.model.InvestmentStatus
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

data class InvestmentResult(val currentValue: BigDecimal?, val profitLoss: BigDecimal?, val profitLossPercent: BigDecimal?) {
    val isProfit get() = (profitLoss?.signum() ?: 0) > 0
    val isLoss get() = (profitLoss?.signum() ?: 0) < 0
}
data class PortfolioSummary(val totalInvested: BigDecimal, val totalCurrentValue: BigDecimal, val totalProfitLoss: BigDecimal, val totalPercent: BigDecimal?, val pricedCount: Int, val unpricedCount: Int)

object InvestmentCalculator {
    private val mc = MathContext.DECIMAL64
    fun effectivePrice(inv: InvestmentEntity): Double? = if (inv.status == InvestmentStatus.SOLD.name) inv.sellPrice else inv.currentPrice
    fun compute(inv: InvestmentEntity): InvestmentResult {
        val price = effectivePrice(inv) ?: return InvestmentResult(null,null,null)
        val invested = BigDecimal.valueOf(inv.investedAmount)
        val value = BigDecimal(inv.quantity.toString()).multiply(BigDecimal(price.toString()),mc).setScale(0,RoundingMode.HALF_UP)
        val pl=value.subtract(invested)
        val percent=if(invested.signum()==0)null else pl.multiply(BigDecimal(100),mc).divide(invested,2,RoundingMode.HALF_UP)
        return InvestmentResult(value,pl,percent)
    }
    fun quantityFrom(investedAmount:Long,buyPrice:Double):Double? {
        if(investedAmount<=0L || buyPrice<=0.0 || !buyPrice.isFinite()) return null
        return BigDecimal.valueOf(investedAmount).divide(BigDecimal(buyPrice.toString()),8,RoundingMode.HALF_UP).toDouble()
    }
    fun investedFrom(quantity:Double,buyPrice:Double):Long? {
        if(quantity<=0.0 || buyPrice<=0.0 || !quantity.isFinite() || !buyPrice.isFinite()) return null
        return BigDecimal(quantity.toString()).multiply(BigDecimal(buyPrice.toString()),mc).setScale(0,RoundingMode.HALF_UP).longValueExact()
    }
    fun summarize(list:List<InvestmentEntity>):PortfolioSummary {
        var invested=BigDecimal.ZERO; var value=BigDecimal.ZERO; var pricedInvested=BigDecimal.ZERO; var priced=0; var unpriced=0
        list.forEach { inv -> invested=invested.add(BigDecimal.valueOf(inv.investedAmount)); val r=compute(inv); if(r.currentValue!=null){value=value.add(r.currentValue);pricedInvested=pricedInvested.add(BigDecimal.valueOf(inv.investedAmount));priced++}else unpriced++ }
        val pl=value.subtract(pricedInvested)
        val percent=if(pricedInvested.signum()==0)null else pl.multiply(BigDecimal(100),mc).divide(pricedInvested,2,RoundingMode.HALF_UP)
        return PortfolioSummary(invested,value,pl,percent,priced,unpriced)
    }
}