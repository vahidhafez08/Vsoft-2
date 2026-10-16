package com.vsoft.app.ui.screens
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.vsoft.app.ui.theme.Vsoft
@Composable
fun PlaceholderScreen(title: String) {
    Box(Modifier.fillMaxSize().background(Vsoft.colors.bg), contentAlignment = Alignment.Center) {
        Text(title, color = Vsoft.colors.textPrimary, fontSize = 22.sp)
    }
}
