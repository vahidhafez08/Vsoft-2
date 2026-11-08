package com.vsoft.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.Weekend
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class CategoryStyle(val color: Color, val icon: ImageVector)

object CategoryStyles {
    private val map: Map<String, CategoryStyle> = mapOf(
        "خوراک و آشامیدنی" to CategoryStyle(Color(0xFFEF4444), Icons.Filled.Restaurant),
        "سوپرمارکت و خواربار" to CategoryStyle(Color(0xFF16A34A), Icons.Filled.ShoppingCart),
        "رستوران و کافه" to CategoryStyle(Color(0xFFEA580C), Icons.Filled.LocalCafe),
        "حمل و نقل" to CategoryStyle(Color(0xFF3B82F6), Icons.Filled.DirectionsCar),
        "تاکسی اینترنتی" to CategoryStyle(Color(0xFF2563EB), Icons.Filled.DirectionsCar),
        "بنزین" to CategoryStyle(Color(0xFF0D9488), Icons.Filled.LocalGasStation),
        "قبض‌ها" to CategoryStyle(Color(0xFF64748B), Icons.Filled.Receipt),
        "اینترنت و موبایل" to CategoryStyle(Color(0xFF0891B2), Icons.Filled.Wifi),
        "مسکن" to CategoryStyle(Color(0xFF22C55E), Icons.Filled.Home),
        "اجاره و قسط" to CategoryStyle(Color(0xFF15803D), Icons.Filled.Apartment),
        "سلامت و دارو" to CategoryStyle(Color(0xFFDC2626), Icons.Filled.LocalHospital),
        "آموزش و دوره" to CategoryStyle(Color(0xFF7C3AED), Icons.Filled.School),
        "اشتراک‌ها" to CategoryStyle(Color(0xFF9333EA), Icons.Filled.Subscriptions),
        "خرید آنلاین" to CategoryStyle(Color(0xFFDB2777), Icons.Filled.ShoppingBag),
        "سفر" to CategoryStyle(Color(0xFF0284C7), Icons.Filled.Flight),
        "هدیه و کادو" to CategoryStyle(Color(0xFFEC4899), Icons.Filled.CardGiftcard),
        "خیریه و کمک" to CategoryStyle(Color(0xFF16A34A), Icons.Filled.Favorite),
        "تعمیرات" to CategoryStyle(Color(0xFFB45309), Icons.Filled.Build),
        "بیمه" to CategoryStyle(Color(0xFF475569), Icons.Filled.Security),
        "لوازم خانه" to CategoryStyle(Color(0xFF0F766E), Icons.Filled.Weekend),
        "ورزش و باشگاه" to CategoryStyle(Color(0xFF65A30D), Icons.Filled.FitnessCenter),
        "حیوان خانگی" to CategoryStyle(Color(0xFF92400E), Icons.Filled.Pets),
        "تفریح و سرگرمی" to CategoryStyle(Color(0xFF8B5CF6), Icons.Filled.SportsEsports),
        "پوشاک" to CategoryStyle(Color(0xFFF59E0B), Icons.Filled.Checkroom),
        "حقوق" to CategoryStyle(Color(0xFF22C55E), Icons.Filled.Payments),
        "فروش" to CategoryStyle(Color(0xFF14B8A6), Icons.Filled.Storefront),
        "فریلنس و پروژه" to CategoryStyle(Color(0xFF2563EB), Icons.Filled.Work),
        "پاداش و عیدی" to CategoryStyle(Color(0xFFF59E0B), Icons.Filled.CardGiftcard),
        "سود سرمایه‌گذاری" to CategoryStyle(Color(0xFF0D9488), Icons.Filled.TrendingUp),
        "اجاره دریافتی" to CategoryStyle(Color(0xFF16A34A), Icons.Filled.Home),
        "کمک و هدیه" to CategoryStyle(Color(0xFFDB2777), Icons.Filled.Favorite),
        "بازگشت وجه" to CategoryStyle(Color(0xFF0284C7), Icons.Filled.AccountBalance),
        "حساب" to CategoryStyle(Color(0xFF3B6CF6), Icons.Filled.AccountBalance)
    )

    fun of(name: String): CategoryStyle =
        map[name] ?: CategoryStyle(Color(0xFF64748B), Icons.Filled.MoreHoriz)
}