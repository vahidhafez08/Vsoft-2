package com.vahidnia.finance

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily

@Immutable
class VC(
    val dark: Boolean,
    val glass: Boolean,
    val bg: Color,
    val bg2: Color,
    val card: Color,
    val border: Color,
    val text: Color,
    val sub: Color,
    val accent: Color,
    val onAccent: Color,
    val income: Color,
    val expense: Color
)

fun makeVC(dark: Boolean, glass: Boolean): VC =
    if (dark) VC(
        dark = true, glass = glass,
        bg = Color(0xFF0A1416), bg2 = Color(0xFF0F2224), card = Color(0xFF14272A),
        border = Color(0xFF263D41), text = Color(0xFFF2F7F6), sub = Color(0xFF8FA6A6),
        accent = Color(0xFF1FCB93), onAccent = Color(0xFF04201A),
        income = Color(0xFF2FD39B), expense = Color(0xFFFF5A5F)
    ) else VC(
        dark = false, glass = glass,
        bg = Color(0xFFF3F6F6), bg2 = Color(0xFFE6EEED), card = Color(0xFFFFFFFF),
        border = Color(0xFFDCE5E4), text = Color(0xFF0F1D1E), sub = Color(0xFF667777),
        accent = Color(0xFF0FA678), onAccent = Color(0xFFFFFFFF),
        income = Color(0xFF0E9F6E), expense = Color(0xFFE5484D)
    )

val LocalVC = staticCompositionLocalOf { makeVC(true, true) }
val LocalFont = compositionLocalOf<FontFamily> { FontFamily.Default }

fun bankColors(bank: String): List<Color> = when {
    bank.contains("ملی") -> listOf(Color(0xFF0B7A8F), Color(0xFF0A3F5C))
    bank.contains("بلو") -> listOf(Color(0xFF3A74FF), Color(0xFF1B3FA8))
    bank.contains("مسکن") -> listOf(Color(0xFFEA8A45), Color(0xFFB4501A))
    else -> listOf(Color(0xFF7466E8), Color(0xFF3F3590))
}
