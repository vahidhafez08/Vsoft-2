package com.vsoft.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.vsoft.app.R

val VazirFamily = FontFamily(
    Font(R.font.vazirmatn_regular, FontWeight.Normal),
    Font(R.font.vazirmatn_medium, FontWeight.Medium),
    Font(R.font.vazirmatn_bold, FontWeight.Bold)
)

private val DefaultTypography = Typography()

val VsoftTypography = DefaultTypography.copy(
    displayLarge = DefaultTypography.displayLarge.copy(fontFamily = VazirFamily),
    displayMedium = DefaultTypography.displayMedium.copy(fontFamily = VazirFamily),
    displaySmall = DefaultTypography.displaySmall.copy(fontFamily = VazirFamily),
    headlineLarge = DefaultTypography.headlineLarge.copy(fontFamily = VazirFamily),
    headlineMedium = TextStyle(fontFamily = VazirFamily, fontWeight = FontWeight.Bold, fontSize = 24.sp),
    headlineSmall = DefaultTypography.headlineSmall.copy(fontFamily = VazirFamily),
    titleLarge = TextStyle(fontFamily = VazirFamily, fontWeight = FontWeight.Bold, fontSize = 20.sp),
    titleMedium = TextStyle(fontFamily = VazirFamily, fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
    titleSmall = DefaultTypography.titleSmall.copy(fontFamily = VazirFamily),
    bodyLarge = TextStyle(fontFamily = VazirFamily, fontSize = 15.sp),
    bodyMedium = TextStyle(fontFamily = VazirFamily, fontSize = 13.sp),
    bodySmall = DefaultTypography.bodySmall.copy(fontFamily = VazirFamily),
    labelLarge = DefaultTypography.labelLarge.copy(fontFamily = VazirFamily),
    labelMedium = DefaultTypography.labelMedium.copy(fontFamily = VazirFamily),
    labelSmall = TextStyle(fontFamily = VazirFamily, fontSize = 11.sp)
)
