package com.vsoft.app.ui.components

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.vsoft.app.data.model.ExpenseCategories
import com.vsoft.app.data.model.IncomeCategories

data class CategoryStyle(val color: Color, val icon: ImageVector)
val CategoryIconOptions: List<Pair<String, ImageVector>> = listOf(
    "restaurant" to Icons.Filled.Restaurant, "cafe" to Icons.Filled.LocalCafe,
    "cart" to Icons.Filled.ShoppingCart, "bag" to Icons.Filled.ShoppingBag,
    "car" to Icons.Filled.DirectionsCar, "fuel" to Icons.Filled.LocalGasStation,
    "home" to Icons.Filled.Home, "build" to Icons.Filled.Build,
    "bolt" to Icons.Filled.Bolt, "health" to Icons.Filled.LocalHospital,
    "school" to Icons.Filled.School, "flight" to Icons.Filled.Flight,
    "gift" to Icons.Filled.CardGiftcard, "heart" to Icons.Filled.Favorite,
    "pets" to Icons.Filled.Pets, "fitness" to Icons.Filled.FitnessCenter,
    "game" to Icons.Filled.SportsEsports, "phone" to Icons.Filled.PhoneIphone,
    "work" to Icons.Filled.Work, "savings" to Icons.Filled.Savings,
    "music" to Icons.Filled.MusicNote, "movie" to Icons.Filled.Movie,
    "child" to Icons.Filled.ChildCare, "star" to Icons.Filled.Star
)
val CategoryColorOptions = listOf("#EF4444", "#F59E0B", "#16A34A", "#0D9488", "#2563EB",
    "#7C3AED", "#DB2777", "#0284C7", "#B45309", "#475569")
fun categoryIconFor(key: String): ImageVector =
    CategoryIconOptions.firstOrNull { it.first == key }?.second ?: Icons.Filled.Category
private fun parseHex(hex: String): Color =
    runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrDefault(Color(0xFF64748B))
data class CustomCategory(val name: String, val iconKey: String, val colorHex: String, val isIncome: Boolean)

object CustomCategories {
    private const val PREFS = "vsoft_prefs"
    private const val KEY = "custom_categories_v1"
    val items = mutableStateListOf<CustomCategory>()
    private var loaded = false
    fun init(context: Context) {
        if (loaded) return
        loaded = true
        val raw = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, "") ?: ""
        items.addAll(raw.lines().mapNotNull { line ->
            val p = line.split("|")
            if (p.size == 4 && p[1].isNotBlank()) CustomCategory(p[1], p[2], p[3], p[0] == "1") else null
        })
    }
    private fun save(context: Context) {
        val raw = items.joinToString("\n") { "${if (it.isIncome) 1 else 0}|${it.name}|${it.iconKey}|${it.colorHex}" }
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY, raw).apply()
    }
    fun exists(name: String): Boolean {
        val n = name.trim()
        return ExpenseCategories.contains(n) || IncomeCategories.contains(n) || items.any { it.name == n }
    }
    fun add(context: Context, name: String, iconKey: String, colorHex: String, isIncome: Boolean): String? {
        init(context)
        val clean = name.replace("|", " ").replace("\n", " ").trim().take(20)
        if (clean.isBlank() || exists(clean)) return null
        items.add(CustomCategory(clean, iconKey, colorHex, isIncome)); save(context); return clean
    }
    fun remove(context: Context, name: String) { items.removeAll { it.name == name }; save(context) }
    fun styleOf(name: String): CategoryStyle? =
        items.firstOrNull { it.name == name }?.let { CategoryStyle(parseHex(it.colorHex), categoryIconFor(it.iconKey)) }
}
fun categoriesFor(isIncome: Boolean): List<String> {
    val base = if (isIncome) IncomeCategories else ExpenseCategories
    val custom = CustomCategories.items.filter { it.isIncome == isIncome }.map { it.name }
    return base.filter { it != "سایر" } + custom + "سایر"
}
object CategoryStyles {
    private val map: Map<String, CategoryStyle> = mapOf(
        "خوراک و آشامیدنی" to CategoryStyle(Color(0xFFEF4444), Icons.Filled.Restaurant),
        "سوپرمارکت و خواربار" to CategoryStyle(Color(0xFF16A34A), Icons.Filled.ShoppingCart),
        "رستوران و کافه" to CategoryStyle(Color(0xFFEA580C), Icons.Filled.LocalCafe),
        "حمل و نقل" to CategoryStyle(Color(0xFF3B82F6), Icons.Filled.DirectionsCar),
        "تاکسی اینترنتی" to CategoryStyle(Color(0xFF2563EB), Icons.Filled.DirectionsCar),
        "بنزین" to CategoryStyle(Color(0xFF0D9488), Icons.Filled.LocalGasStation),
        "نگهداری خودرو" to CategoryStyle(Color(0xFF0369A1), Icons.Filled.CarRepair),
        "پارکینگ و عوارض" to CategoryStyle(Color(0xFF4F46E5), Icons.Filled.LocalParking),
        "قبض‌ها" to CategoryStyle(Color(0xFF64748B), Icons.Filled.Receipt),
        "اینترنت و موبایل" to CategoryStyle(Color(0xFF0891B2), Icons.Filled.Wifi),
        "مسکن" to CategoryStyle(Color(0xFF22C55E), Icons.Filled.Home),
        "اجاره و قسط" to CategoryStyle(Color(0xFF15803D), Icons.Filled.Apartment),
        "شارژ ساختمان" to CategoryStyle(Color(0xFF0F766E), Icons.Filled.Domain),
        "سلامت و دارو" to CategoryStyle(Color(0xFFDC2626), Icons.Filled.LocalHospital),
        "دندانپزشکی" to CategoryStyle(Color(0xFF06B6D4), Icons.Filled.MedicalServices),
        "آموزش و دوره" to CategoryStyle(Color(0xFF7C3AED), Icons.Filled.School),
        "کتاب و لوازم‌التحریر" to CategoryStyle(Color(0xFF6366F1), Icons.Filled.MenuBook),
        "کودک و مدرسه" to CategoryStyle(Color(0xFFF472B6), Icons.Filled.ChildCare),
        "اشتراک‌ها" to CategoryStyle(Color(0xFF9333EA), Icons.Filled.Subscriptions),
        "خرید آنلاین" to CategoryStyle(Color(0xFFDB2777), Icons.Filled.ShoppingBag),
        "لوازم دیجیتال" to CategoryStyle(Color(0xFF1D4ED8), Icons.Filled.Devices),
        "سفر" to CategoryStyle(Color(0xFF0284C7), Icons.Filled.Flight),
        "هدیه و کادو" to CategoryStyle(Color(0xFFEC4899), Icons.Filled.CardGiftcard),
        "مهمانی و مراسم" to CategoryStyle(Color(0xFFF97316), Icons.Filled.Celebration),
        "خیریه و کمک" to CategoryStyle(Color(0xFF16A34A), Icons.Filled.Favorite),
        "تعمیرات" to CategoryStyle(Color(0xFFB45309), Icons.Filled.Build),
        "ابزار و تجهیزات" to CategoryStyle(Color(0xFFD97706), Icons.Filled.Construction),
        "مصالح و متریال" to CategoryStyle(Color(0xFF78716C), Icons.Filled.Inventory2),
        "دستمزد و پیمانکار" to CategoryStyle(Color(0xFF0E7490), Icons.Filled.Engineering),
        "بیمه" to CategoryStyle(Color(0xFF475569), Icons.Filled.Security),
        "مالیات و جریمه" to CategoryStyle(Color(0xFF991B1B), Icons.Filled.Gavel),
        "کارمزد بانکی" to CategoryStyle(Color(0xFF334155), Icons.Filled.CreditCard),
        "لوازم خانه" to CategoryStyle(Color(0xFF0F766E), Icons.Filled.Weekend),
        "ورزش و باشگاه" to CategoryStyle(Color(0xFF65A30D), Icons.Filled.FitnessCenter),
        "آرایشگاه و زیبایی" to CategoryStyle(Color(0xFFD946EF), Icons.Filled.ContentCut),
        "حیوان خانگی" to CategoryStyle(Color(0xFF92400E), Icons.Filled.Pets),
        "تفریح و سرگرمی" to CategoryStyle(Color(0xFF8B5CF6), Icons.Filled.SportsEsports),
        "پوشاک" to CategoryStyle(Color(0xFFF59E0B), Icons.Filled.Checkroom),
        "پس‌انداز" to CategoryStyle(Color(0xFF059669), Icons.Filled.Savings),
        "حقوق" to CategoryStyle(Color(0xFF22C55E), Icons.Filled.Payments),
        "فروش" to CategoryStyle(Color(0xFF14B8A6), Icons.Filled.Storefront),
        "فریلنس و پروژه" to CategoryStyle(Color(0xFF2563EB), Icons.Filled.Work),
        "دریافتی از مشتری" to CategoryStyle(Color(0xFF0EA5E9), Icons.Filled.RequestQuote),
        "کارمزد و پورسانت" to CategoryStyle(Color(0xFF7C3AED), Icons.Filled.Paid),
        "پاداش و عیدی" to CategoryStyle(Color(0xFFF59E0B), Icons.Filled.CardGiftcard),
        "سود سرمایه‌گذاری" to CategoryStyle(Color(0xFF0D9488), Icons.Filled.TrendingUp),
        "سود بانکی" to CategoryStyle(Color(0xFF1E40AF), Icons.Filled.AccountBalance),
        "اجاره دریافتی" to CategoryStyle(Color(0xFF16A34A), Icons.Filled.Home),
        "کمک و هدیه" to CategoryStyle(Color(0xFFDB2777), Icons.Filled.Favorite),
        "یارانه و حمایت" to CategoryStyle(Color(0xFF84CC16), Icons.Filled.VolunteerActivism),
        "فروش وسایل" to CategoryStyle(Color(0xFFEA580C), Icons.Filled.Sell),
        "وام دریافتی" to CategoryStyle(Color(0xFF4338CA), Icons.Filled.RequestQuote),
        "بازگشت وجه" to CategoryStyle(Color(0xFF0284C7), Icons.Filled.AccountBalance),
        "حساب" to CategoryStyle(Color(0xFF3B6CF6), Icons.Filled.AccountBalance)
    )
    fun of(name: String): CategoryStyle =
        map[name] ?: CustomCategories.styleOf(name) ?: CategoryStyle(Color(0xFF64748B), Icons.Filled.MoreHoriz)
}
