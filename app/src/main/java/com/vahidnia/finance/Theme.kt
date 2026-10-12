package com.vahidnia.finance

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontFamily

/**
 * سیستم رنگ مرکزی Vsoft (آبی سرمه‌ای، سفید، خاکستری روشن).
 * همه رنگ‌های برنامه فقط از همین‌جا (LocalVC) خوانده می‌شوند.
 */
@Immutable
class VC(
    val dark: Boolean,
    val glass: Boolean,
    // سطوح
    val bg: Color,
    val card: Color,
    val surfaceVar: Color,
    val dialog: Color,
    val bar: Color,
    val border: Color,
    val divider: Color,
    // متن
    val text: Color,
    val sub: Color,
    // رنگ اصلی
    val primary: Color,
    val secondary: Color,
    val highlight: Color,
    val accent: Color,
    val onAccent: Color,
    val accentBg: Color,
    // رنگ‌های معنایی مالی
    val income: Color,
    val incomeBg: Color,
    val expense: Color,
    val expenseBg: Color,
    // هشدار / خطا / اطلاعات
    val warning: Color,
    val warningBg: Color,
    val error: Color,
    val errorBg: Color,
    val info: Color,
    val infoBg: Color,
    // کارت موجودی کل
    val hero1: Color,
    val hero2: Color,
    val onHero: Color
)

fun makeVC(dark: Boolean, glass: Boolean): VC =
    if (dark) VC(
        dark = true, glass = glass,
        bg = Color(0xFF10141C), card = Color(0xFF191F2B), surfaceVar = Color(0xFF252E3D),
        dialog = Color(0xFF222A38), bar = Color(0xFF191F2B),
        border = Color(0xFF30394A), divider = Color(0xFF2B3443),
        text = Color(0xFFF2F4F8), sub = Color(0xFFA9B2C2),
        primary = Color(0xFF8BAEF5), secondary = Color(0xFF8CA3C7), highlight = Color(0xFF6B8FD6),
        accent = Color(0xFF8BAEF5), onAccent = Color(0xFF0F1B33), accentBg = Color(0xFF253A5D),
        income = Color(0xFF55C995), incomeBg = Color(0xFF193C30),
        expense = Color(0xFFFF7777), expenseBg = Color(0xFF48272B),
        warning = Color(0xFFF0C36D), warningBg = Color(0xFF3A3020),
        error = Color(0xFFFF8585), errorBg = Color(0xFF45262A),
        info = Color(0xFF8CB5FF), infoBg = Color(0xFF1F2F4D),
        hero1 = Color(0xFF253A5D), hero2 = Color(0xFF1D2E4A), onHero = Color(0xFFF2F4F8)
    ) else VC(
        dark = false, glass = glass,
        bg = Color(0xFFF6F7FB), card = Color(0xFFFFFFFF), surfaceVar = Color(0xFFEDF0F6),
        dialog = Color(0xFFFFFFFF), bar = Color(0xFFFFFFFF),
        border = Color(0xFFE1E6EF), divider = Color(0xFFE8ECF2),
        text = Color(0xFF202938), sub = Color(0xFF667085),
        primary = Color(0xFF233B67), secondary = Color(0xFF536B91), highlight = Color(0xFF6B8FD6),
        accent = Color(0xFF233B67), onAccent = Color(0xFFFFFFFF), accentBg = Color(0xFFE8F0FC),
        income = Color(0xFF21865A), incomeBg = Color(0xFFE5F5EC),
        expense = Color(0xFFD94A4A), expenseBg = Color(0xFFFCEAEA),
        warning = Color(0xFFB7791F), warningBg = Color(0xFFFFF4D6),
        error = Color(0xFFC62828), errorBg = Color(0xFFFDECEC),
        info = Color(0xFF356AC3), infoBg = Color(0xFFEAF1FF),
        hero1 = Color(0xFF233B67), hero2 = Color(0xFF192B4D), onHero = Color(0xFFFFFFFF)
    )

val LocalVC = staticCompositionLocalOf { makeVC(true, true) }
val LocalFont = compositionLocalOf<FontFamily> { FontFamily.Default }

// ---------- وضعیت پروژه/فعالیت (جدا از رنگ درآمد و هزینه) ----------
enum class Status { Done, Active, Pending, Cancelled, Problem }

class StatusColors(val fg: Color, val bg: Color, val label: String)

fun VC.statusColors(s: Status): StatusColors = when (s) {
    Status.Done -> StatusColors(income, incomeBg, "انجام‌شده")
    Status.Active -> StatusColors(info, infoBg, "در حال انجام")
    Status.Pending -> StatusColors(warning, warningBg, "در انتظار")
    Status.Cancelled -> StatusColors(sub, surfaceVar, "لغوشده")
    Status.Problem -> StatusColors(error, errorBg, "دارای مشکل")
}

/** وضعیت از روی درصد پیشرفت (بدون تغییر مدل داده) */
fun progressStatus(p: Int): Status = when {
    p >= 100 -> Status.Done
    p > 0 -> Status.Active
    else -> Status.Pending
}

// ---------- رنگ بانک‌ها: [0] = رنگ اصلی، [1] = رنگ مکمل ----------
private val bankDarkBase = Color(0xFF10141C)

private fun bankPair(main: Long, comp: Long, dark: Boolean): List<Color> {
    val a = Color(main)
    val b = Color(comp)
    // در حالت تاریک هر دو رنگ کمی تیره‌تر می‌شوند تا متن سفید خوانا بماند
    return if (dark) listOf(lerp(a, bankDarkBase, 0.25f), lerp(b, bankDarkBase, 0.35f)) else listOf(a, b)
}

fun bankColors(bank: String, dark: Boolean = false): List<Color> = when {
    bank.contains("ملی") -> bankPair(0xFF164B8C, 0xFF3D75BA, dark)
    bank.contains("مسکن") -> bankPair(0xFF087E8B, 0xFF43B5BE, dark)
    bank.contains("بلو") -> bankPair(0xFF3C83F6, 0xFF76A9FA, dark)
    bank.contains("رد بانک") -> bankPair(0xFFB52B35, 0xFFE45B62, dark)
    bank.contains("مهر") -> bankPair(0xFF23865B, 0xFF56B987, dark)
    else -> bankPair(0xFF5B667A, 0xFF8A94A6, dark) // خنثی
}

/** مجموعه محدود رنگ برای نمودار دسته‌بندی‌ها */
val chartPalette = listOf(
    Color(0xFF233B67), Color(0xFF6B8FD6), Color(0xFF536B91),
    Color(0xFF21865A), Color(0xFFB7791F), Color(0xFF8A94A6)
)
