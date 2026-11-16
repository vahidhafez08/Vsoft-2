package com.vsoft.app.data.market

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MarketRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val cacheFile: File
        get() = File(context.filesDir, "market_latest_toman.json")

    suspend fun fetchLatest(): MarketSnapshot = withContext(Dispatchers.IO) {
        try {
            val connection = (URL(DATA_URL).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 15_000
                readTimeout = 20_000
                setRequestProperty("Accept", "application/json")
                setRequestProperty("User-Agent", "Vsoft-Android")
            }
            val body = try {
                if (connection.responseCode !in 200..299) {
                    throw IllegalStateException("HTTP " + connection.responseCode)
                }
                connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            } finally {
                connection.disconnect()
            }
            val snapshot = parse(body, fromCache = false)
            cacheFile.writeText(body, Charsets.UTF_8)
            snapshot
        } catch (error: Exception) {
            if (!cacheFile.isFile) throw error
            parse(cacheFile.readText(Charsets.UTF_8), fromCache = true)
        }
    }

    private fun parse(raw: String, fromCache: Boolean): MarketSnapshot {
        val root = JSONObject(raw)
        val data = root.getJSONObject("data")
        val categories = data.getJSONObject("categories")
        val meta = root.optJSONObject("meta")
        val prices = mutableListOf<MarketPrice>()
        val usdToIrt = categories.optJSONArray("currency")?.let { rows ->
            (0 until rows.length()).asSequence()
                .mapNotNull { rows.optJSONObject(it) }
                .firstOrNull {
                    it.optString("symbol") == "USD_IRR_FREE" &&
                        it.optString("currency") in setOf("IRT", "IRR") &&
                        !it.optBoolean("stale", true) &&
                        it.optDouble("price", 0.0) > 0.0
                }?.optDouble("price")
        }

        val categoryKeys = categories.keys()
        while (categoryKeys.hasNext()) {
            val categoryKey = categoryKeys.next()
            val rows = categories.optJSONArray(categoryKey) ?: continue
            for (index in 0 until rows.length()) {
                val item = rows.optJSONObject(index) ?: continue
                val currency = item.optString("currency")
                val category = item.optString("category", categoryKey)
                val isUsdCommodity = category.equals("commodity", ignoreCase = true) && currency == "USD"
                if (currency != "IRT" && currency != "IRR" && !(isUsdCommodity && usdToIrt != null)) continue

                val rawPrice = item.optDouble("price", Double.NaN)
                val price = if (isUsdCommodity) rawPrice * (usdToIrt ?: 0.0) else rawPrice
                if (!price.isFinite() || price <= 0.0) continue

                prices += MarketPrice(
                    symbol = item.optString("symbol"),
                    name = item.optString("name_fa").takeIf { it.isNotBlank() && it != "null" }
                        ?: item.optString("symbol"),
                    category = category,
                    price = price,
                    currency = if (isUsdCommodity) "IRT" else currency,
                    unit = item.optString("unit", "unit"),
                    changePercent = if (item.isNull("change_pct")) null else item.optDouble("change_pct"),
                    timestampJalali = item.optString("timestamp_jalali"),
                    source = item.optString("source"),
                    stale = item.optBoolean("stale", true)
                )
            }
        }

        val updatedAt = data.optString("generated_at_jalali")
            .takeIf { it.isNotBlank() && it != "null" }
            ?: meta?.optString("tehran_time").orEmpty()

        return MarketSnapshot(
            prices = prices,
            updatedAtJalali = updatedAt,
            source = meta?.optString("source")?.takeIf { it.isNotBlank() } ?: "Iran Market Data",
            fromCache = fromCache
        )
    }

    private companion object {
        const val DATA_URL =
            "https://raw.githubusercontent.com/iran-market/iran-market.github.io/main/data/latest-toman.json"
    }
}
