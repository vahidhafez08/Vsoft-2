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
    val primary: Color,
    val accent: Color,
    val onAccent: Color,
    val income: Color,
    val expense: Color,
    val hero1: Color,
    val hero2: Color,
    val onHero: Color,
    val bar: Color,
    val dialog: Color
)

/** پالت Titanium: شب (گرافیتی) و روز (خاکستری روشن) */
fun makeVC(dark: Boolean, glass: Boolean): VC =
    if (dark) VC(
        dark = true, glass = glass,
        bg = Color(0xFF111318), bg2 = Color(0xFF161A22), card = Color(0xFF1C2028),
        border = Color(0xFF2C323D), text = Color(0xFFF5F6F8), sub = Color(0xFF9AA1AE),
        primary = Color(0xFFC0C5CE), accent = Color(0xFF8DA9FF), onAccent = Color(0xFF0C1530),
        income = Color(0xFF35C991), expense = Color(0xFFFF6B78),
        hero1 = Color(0xFF30363F), hero2 = Color(0xFF1C2028), onHero = Color(0xFFF5F6F8),
        bar = Color(0xFF151820), dialog = Color(0xFF1C2028)
    ) else VC(
        dark = false, glass = glass,
        bg = Color(0xFFF1F2F5), bg2 = Color(0xFFE8EAEF), card = Color(0xFFFFFFFF),
        border = Color(0xFFDADDE4), text = Color(0xFF14171C), sub = Color(0xFF667085),
        primary = Color(0xFF6B7380), accent = Color(0xFF4A6CD9), onAccent = Color(0xFFFFFFFF),
        income = Color(0xFF0F9F6E), expense = Color(0xFFE04858),
        hero1 = Color(0xFFCBD0DA), hero2 = Color(0xFFEAEDF2), onHero = Color(0xFF14171C),
        bar = Color(0xFFFFFFFF), dialog = Color(0xFFFFFFFF)
    )

val LocalVC = staticCompositionLocalOf { makeVC(true, true) }
val LocalFont = compositionLocalOf<FontFamily> { FontFamily.Default }

fun bankColors(bank: String): List<Color> = when {
    bank.contains("ملی") -> listOf(Color(0xFF0B7A8F), Color(0xFF0A3F5C))
    bank.contains("بلو") -> listOf(Color(0xFF3A74FF), Color(0xFF1B3FA8))
    bank.contains("مسکن") -> listOf(Color(0xFFEA8A45), Color(0xFFB4501A))
    else -> listOf(Color(0xFF7466E8), Color(0xFF3F3590))
}
