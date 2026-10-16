package com.vsoft.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class CategoryStyle(val color: Color, val icon: ImageVector)

object CategoryStyles {
    private val map: Map<String, CategoryStyle> = mapOf(
        "خوراک و آشامیدنی" to CategoryStyle(Color(0xFFEF4444), Icons.Filled.Restaurant),
        "حمل و نقل" to CategoryStyle(Color(0xFF3B82F6), Icons.Filled.DirectionsCar),
        "مسکن" to CategoryStyle(Color(0xFF22C55E), Icons.Filled.Home),
        "تفریح و سرگرمی" to CategoryStyle(Color(0xFF8B5CF6), Icons.Filled.SportsEsports),
        "پوشاک" to CategoryStyle(Color(0xFFF59E0B), Icons.Filled.Checkroom),
        "حقوق" to CategoryStyle(Color(0xFF22C55E), Icons.Filled.Payments),
        "فروش" to CategoryStyle(Color(0xFF14B8A6), Icons.Filled.Storefront),
        "حساب" to CategoryStyle(Color(0xFF3B6CF6), Icons.Filled.AccountBalance)
    )

    fun of(name: String): CategoryStyle =
        map[name] ?: CategoryStyle(Color(0xFF64748B), Icons.Filled.MoreHoriz)
}