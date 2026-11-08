package com.vsoft.app.domain

data class DebtProgress(val paid:Long,val remaining:Long,val progress:Float,val autoSettled:Boolean)
fun calculateDebtProgress(totalAmount:Long,paid:Long):DebtProgress {
    val remaining=(totalAmount-paid).coerceAtLeast(0L)
    val progress=if(totalAmount<=0L)0f else (paid.toFloat()/totalAmount).coerceIn(0f,1f)
    return DebtProgress(paid,remaining,progress,totalAmount>0L && paid>=totalAmount)
}