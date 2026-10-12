@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.vahidnia.finance

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.Canvas
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { VsoftApp() }
    }
}

@Composable
fun VsoftApp() {
    val vm: AppVm = viewModel()
    val sysDark = isSystemInDarkTheme()
    val dark = when (vm.theme) {
        1 -> false
        2 -> true
        else -> sysDark
    }
    val colors = remember(dark, vm.glass) { makeVC(dark, vm.glass) }
    val font = when (vm.font) {
        1 -> FontFamily.SansSerif
        2 -> FontFamily.Serif
        else -> FontFamily.Default
    }
    val scheme = if (dark) darkColorScheme(
        primary = colors.accent, onPrimary = colors.onAccent, primaryContainer = colors.accentBg,
        background = colors.bg, onBackground = colors.text,
        surface = colors.card, onSurface = colors.text,
        surfaceVariant = colors.surfaceVar, onSurfaceVariant = colors.sub,
        outline = colors.border, outlineVariant = colors.divider,
        error = colors.error, errorContainer = colors.errorBg
    ) else lightColorScheme(
        primary = colors.accent, onPrimary = colors.onAccent, primaryContainer = colors.accentBg,
        background = colors.bg, onBackground = colors.text,
        surface = colors.card, onSurface = colors.text,
        surfaceVariant = colors.surfaceVar, onSurfaceVariant = colors.sub,
        outline = colors.border, outlineVariant = colors.divider,
        error = colors.error, errorContainer = colors.errorBg
    )

    val view = LocalView.current
    SideEffect {
        val w = (view.context as Activity).window
        w.statusBarColor = colors.bg.toArgb()
        w.navigationBarColor = colors.bg.toArgb()
        val ic = WindowCompat.getInsetsController(w, view)
        ic.isAppearanceLightStatusBars = !dark
        ic.isAppearanceLightNavigationBars = !dark
    }

    CompositionLocalProvider(
        LocalVC provides colors,
        LocalFont provides font,
        LocalLayoutDirection provides LayoutDirection.Rtl
    ) {
        MaterialTheme(colorScheme = scheme) {
            var splash by remember { mutableStateOf(true) }
            LaunchedEffect(Unit) {
                delay(1300)
                splash = false
            }
            Crossfade(targetState = splash, label = "splash") { s ->
                if (s) Splash() else MainShell(vm)
            }
        }
    }
}

@Composable
fun Splash() {
    val c = LocalVC.current
    val p = remember { Animatable(0f) }
    LaunchedEffect(Unit) { p.animateTo(1f, tween(1150)) }
    val m1 = c.accent.copy(alpha = 0.10f)
    val m2 = c.accent.copy(alpha = 0.18f)
    Box(Modifier.fillMaxSize().background(c.bg), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val a = Path().apply {
                moveTo(0f, h * 0.78f); lineTo(w * 0.22f, h * 0.60f); lineTo(w * 0.42f, h * 0.74f)
                lineTo(w * 0.66f, h * 0.55f); lineTo(w, h * 0.72f); lineTo(w, h); lineTo(0f, h); close()
            }
            val b = Path().apply {
                moveTo(0f, h * 0.88f); lineTo(w * 0.30f, h * 0.72f); lineTo(w * 0.55f, h * 0.86f)
                lineTo(w * 0.80f, h * 0.70f); lineTo(w, h * 0.84f); lineTo(w, h); lineTo(0f, h); close()
            }
            drawPath(a, m1)
            drawPath(b, m2)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            VLogo(96.dp)
            Spacer(Modifier.height(6.dp))
            VText("VSOFT", size = 30, weight = FontWeight.ExtraBold)
            VText("مدیریت مالی وحیدینیا", size = 14, color = c.sub, align = TextAlign.Center)
        }
        Column(
            Modifier.align(Alignment.BottomCenter).padding(bottom = 48.dp, start = 64.dp, end = 64.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            VText("مدیریت مالی، ساده‌تر از همیشه", size = 13, color = c.sub, align = TextAlign.Center)
            Box(Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(50)).background(c.border)) {
                Box(Modifier.fillMaxHeight().fillMaxWidth(p.value).clip(RoundedCornerShape(50)).background(c.accent))
            }
        }
    }
}

@Composable
fun MainShell(vm: AppVm) {
    val c = LocalVC.current
    val pager = rememberPagerState(pageCount = { 5 })
    val scope = rememberCoroutineScope()
    var showTx by remember { mutableStateOf(false) }
    var txIncome by remember { mutableStateOf(false) }
    var showCard by remember { mutableStateOf(false) }
    var openTx by remember { mutableStateOf<Tx?>(null) }
    var openPlace by rememberSaveable { mutableStateOf<String?>(null) }

    val go: (Int) -> Unit = { p ->
        scope.launch {
            if (kotlin.math.abs(p - pager.currentPage) > 1) pager.scrollToPage(p) else pager.animateScrollToPage(p)
        }
    }

    Column(Modifier.fillMaxSize().background(c.bg)) {
        HorizontalPager(state = pager, beyondBoundsPageCount = 1, modifier = Modifier.weight(1f).fillMaxWidth()) { page ->
            when (page) {
                0 -> Dashboard(vm, go, { txIncome = false; showTx = true }, { showCard = true }, { openTx = it })
                1 -> FinanceScreen(vm, { inc -> txIncome = inc; showTx = true }, { showCard = true }, { openTx = it })
                2 -> WorkScreen(vm, openPlace) { openPlace = it }
                3 -> ReportsScreen(vm)
                else -> SettingsScreen(vm)
            }
        }
        BottomBar(pager.currentPage) { go(it) }
    }

    if (showTx) AddTxDialog(vm, { showTx = false }, { showCard = true }, txIncome)
    if (showCard) AddCardDialog(vm) { showCard = false }
    val liveTx = openTx?.let { t -> vm.txs.firstOrNull { it.id == t.id } }
    if (liveTx != null) TxDetailDialog(vm, liveTx) { openTx = null }
}
