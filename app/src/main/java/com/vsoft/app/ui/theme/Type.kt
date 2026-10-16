package com.vsoft.app.ui.theme
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
val AppFontFamily: FontFamily = FontFamily.Default
val VsoftTypography = Typography(
    headlineMedium = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Bold, fontSize = 24.sp),
    titleLarge = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.Bold, fontSize = 20.sp),
    titleMedium = TextStyle(fontFamily = AppFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
    bodyLarge = TextStyle(fontFamily = AppFontFamily, fontSize = 15.sp),
    bodyMedium = TextStyle(fontFamily = AppFontFamily, fontSize = 13.sp),
    labelSmall = TextStyle(fontFamily = AppFontFamily, fontSize = 11.sp)
)
