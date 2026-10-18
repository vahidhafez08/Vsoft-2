package com.vsoft.app.ui.theme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
data class VsoftColors(val bg: Color, val surface: Color, val surfaceHigh: Color, val outline: Color, val textPrimary: Color, val textSecondary: Color, val isDark: Boolean)
val LocalVsoftColors = staticCompositionLocalOf { VsoftColors(LightBg, LightSurface, LightSurfaceHigh, LightOutline, LightTextPrimary, LightTextSecondary, false) }
private val DarkScheme = darkColorScheme(primary = BrandBlue, secondary = BrandPurple, background = DarkBg, surface = DarkSurface, onBackground = DarkTextPrimary, onSurface = DarkTextPrimary)
private val LightScheme = lightColorScheme(primary = BrandBlue, secondary = BrandPurple, background = LightBg, surface = LightSurface, onBackground = LightTextPrimary, onSurface = LightTextPrimary)
@Composable
fun VsoftTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) VsoftColors(DarkBg, DarkSurface, DarkSurfaceHigh, DarkOutline, DarkTextPrimary, DarkTextSecondary, true) else VsoftColors(LightBg, LightSurface, LightSurfaceHigh, LightOutline, LightTextPrimary, LightTextSecondary, false)
    CompositionLocalProvider(LocalVsoftColors provides colors, LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(colorScheme = if (darkTheme) DarkScheme else LightScheme, typography = VsoftTypography, content = content)
    }
}
object Vsoft { val colors: VsoftColors @Composable get() = LocalVsoftColors.current }
