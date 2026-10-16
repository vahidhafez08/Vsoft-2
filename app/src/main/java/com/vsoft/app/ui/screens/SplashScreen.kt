package com.vsoft.app.ui.screens
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vsoft.app.ui.theme.BrandBlue
import kotlinx.coroutines.delay
@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val finished = rememberUpdatedState(onFinished)
    LaunchedEffect(Unit) { delay(1800); finished.value() }
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF05070F), Color(0xFF0B1020)))), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text("V", fontSize = 84.sp, fontWeight = FontWeight.Black, color = BrandBlue)
            Spacer(Modifier.height(8.dp))
            Text("VSOFT", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = 6.sp)
            Spacer(Modifier.height(10.dp))
            Text("مدیریت مالی و کارهای روزمره", fontSize = 14.sp, color = Color(0xFF94A3B8), textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 24.dp))
        }
    }
}
