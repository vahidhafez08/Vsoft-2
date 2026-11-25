package com.vsoft.app

import com.vsoft.app.data.model.InvestmentEntity
import com.vsoft.app.domain.InvestmentCalculator
import com.vsoft.app.domain.calculateDebtProgress
import org.junit.Assert.*
import org.junit.Test
import java.math.BigDecimal

class FinanceFeatureCalculatorTest {
    private fun item(price:Double?,status:String="ACTIVE",sell:Double?=null,invested:Long=1_000_000L)=InvestmentEntity(typeId=1,title="",investedAmount=invested,buyPrice=100_000.0,quantity=10.0,currentPrice=price,buyDateMillis=0,status=status,sellPrice=sell)
    @Test fun profitIsCalculatedExactly() {
        val r=InvestmentCalculator.compute(item(120_000.0))
        assertEquals(0,BigDecimal("1200000").compareTo(r.currentValue!!))
        assertEquals(0,BigDecimal("200000").compareTo(r.profitLoss!!))
        assertEquals(0,BigDecimal("20.00").compareTo(r.profitLossPercent!!))
    }
    @Test fun lossIsCalculatedExactly() {
        val r=InvestmentCalculator.compute(item(80_000.0))
        assertEquals(0,BigDecimal("800000").compareTo(r.currentValue))
        assertEquals(0,BigDecimal("-200000").compareTo(r.profitLoss))
        assertEquals(0,BigDecimal("-20.00").compareTo(r.profitLossPercent))
    }
    @Test fun unpricedReturnsNulls() { val r=InvestmentCalculator.compute(item(null)); assertNull(r.currentValue);assertNull(r.profitLoss);assertNull(r.profitLossPercent) }
    @Test fun soldUsesSellPrice() { assertEquals(0,BigDecimal("900000").compareTo(InvestmentCalculator.compute(item(200_000.0,"SOLD",90_000.0)).currentValue)) }
    @Test fun zeroInvestedHasNoPercent() { val r=InvestmentCalculator.compute(item(120_000.0,invested=0L)); assertNull(r.profitLossPercent) }
    @Test fun quantityAndAmountRejectNonPositiveValues() { assertNull(InvestmentCalculator.quantityFrom(0,10.0));assertNull(InvestmentCalculator.quantityFrom(100,-1.0));assertNull(InvestmentCalculator.investedFrom(0.0,10.0));assertNull(InvestmentCalculator.investedFrom(1.0,0.0)) }
    @Test fun summaryExcludesUnpricedFromProfit() {
        val s=InvestmentCalculator.summarize(listOf(item(120_000.0),item(null,invested=500_000)))
        assertEquals(0,BigDecimal("1500000").compareTo(s.totalInvested))
        assertEquals(0,BigDecimal("1200000").compareTo(s.totalCurrentValue))
        assertEquals(0,BigDecimal("200000").compareTo(s.totalProfitLoss))
        assertEquals(1,s.pricedCount);assertEquals(1,s.unpricedCount)
    }
    @Test fun debtProgressClampsRemainingAndProgress() {
        assertEquals(40L,calculateDebtProgress(100,60).remaining)
        assertEquals(0L,calculateDebtProgress(100,100).remaining)
        assertEquals(0L,calculateDebtProgress(100,120).remaining)
        assertEquals(1f,calculateDebtProgress(100,120).progress,0.0001f)
        assertTrue(calculateDebtProgress(100,100).autoSettled)
    }
}