package com.vsoft.app.data.market

data class MarketPrice(
    val symbol: String,
    val name: String,
    val category: String,
    val price: Double,
    val currency: String,
    val unit: String,
    val changePercent: Double?,
    val timestampJalali: String,
    val source: String,
    val stale: Boolean
)

data class MarketSnapshot(
    val prices: List<MarketPrice>,
    val updatedAtJalali: String,
    val source: String,
    val fromCache: Boolean
)
