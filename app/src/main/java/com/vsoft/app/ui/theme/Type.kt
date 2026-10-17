package com.vsoft.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.vsoft.app.R

val VazirFamily = FontFamily(
    Font(R.font.vazirmatn_regular, FontWeight.Normal),
    Font(R.font.vazirmatn_medium, FontWeight.Medium),
    Font(R.font.vazirmatn_bold, FontWeight.Bold)
)

private val DefaultTypography = Typography()
private fun textStyle(size: TextUnit, weight: FontWeight = FontWeight.Normal) =
    TextStyle(fontFamily = VazirFamily, fontWeight = weight, fontSize = size, lineHeight = size * 1.5f)

val VsoftTypography = DefaultTypography.copy(
    displayLarge = textStyle(32.sp, FontWeight.Bold),
    displayMedium = textStyle(28.sp, FontWeight.Bold),
    displaySmall = textStyle(24.sp, FontWeight.Bold),
    headlineLarge = textStyle(24.sp, FontWeight.Bold),
    headlineMedium = textStyle(22.sp, FontWeight.Bold),
    headlineSmall = textStyle(20.sp, FontWeight.Bold),
    titleLarge = textStyle(18.sp, FontWeight.Bold),
    titleMedium = textStyle(15.sp, FontWeight.Medium),
    titleSmall = textStyle(14.sp, FontWeight.Medium),
    bodyLarge = textStyle(16.sp),
    bodyMedium = textStyle(14.sp),
    bodySmall = textStyle(12.sp),
    labelLarge = textStyle(14.sp, FontWeight.Bold),
    labelMedium = textStyle(13.sp, FontWeight.Medium),
    labelSmall = textStyle(12.sp)
)