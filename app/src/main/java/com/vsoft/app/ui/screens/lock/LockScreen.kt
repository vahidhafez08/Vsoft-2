package com.vsoft.app.ui.screens.lock

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.CompositionLocalProvider
import com.vsoft.app.security.PinStore
import com.vsoft.app.ui.components.bounceClick
import com.vsoft.app.ui.theme.BrandBlue
import com.vsoft.app.ui.theme.BrandPurple
import com.vsoft.app.ui.theme.ExpenseRed
import com.vsoft.app.ui.theme.Vsoft
import com.vsoft.app.util.toLatinDigits
import com.vsoft.app.util.toPersianDigits
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun LockScreen(onUnlocked: () -> Unit) {
    val context = LocalContext.current
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    var errorCount by remember { mutableIntStateOf(0) }
    var remaining by remember { mutableLongStateOf(PinStore.remainingLockMillis(context)) }
    val shake = remember { Animatable(0f) }

    androidx.activity.compose.BackHandler {
        (context as? Activity)?.moveTaskToBack(true)
    }

    LaunchedEffect(errorCount) {
        if (errorCount > 0) {
            repeat(3) {
                shake.animateTo(7f, tween(45))
                shake.animateTo(-7f, tween(45))
            }
            shake.animateTo(0f, tween(45))
        }
    }
    LaunchedEffect(remaining > 0L) {
        while (remaining > 0L) {
            delay(1_000L)
            remaining = PinStore.remainingLockMillis(context)
        }
    }

    fun submit(candidate: String) {
        if (remaining > 0L) return
        if (PinStore.verify(context, candidate)) {
            error = false
            pin = ""
            onUnlocked()
        } else {
            PinStore.registerFailure(context)
            remaining = PinStore.remainingLockMillis(context)
            error = true
            errorCount++
            pin = ""
        }
    }

    val digits = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "", "0", "backspace")
    Column(
        modifier = Modifier.fillMaxSize().background(Vsoft.colors.bg)
            .windowInsetsPadding(WindowInsets.safeDrawing).padding(horizontal = 28.dp, vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(Modifier.height(4.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier.size(86.dp).background(Brush.linearGradient(listOf(BrandBlue, BrandPurple)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("V", color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.height(14.dp))
            Text("Vesta", color = Vsoft.colors.textPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold, letterSpacing = 4.sp)
            Spacer(Modifier.height(8.dp))
            Text("رمز چهار رقمی رو وارد کن", color = Vsoft.colors.textSecondary, fontSize = 14.sp)
            Spacer(Modifier.height(26.dp))
            Row(
                modifier = Modifier.offsetForShake(shake.value),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(4) { index ->
                    val filled = index < pin.length
                    val scale by animateFloatAsState(if (filled) 1.08f else 1f, animationSpec = tween(160), label = "pin-dot-$index")
                    Box(
                        Modifier.size(14.dp).scale(scale)
                            .background(if (filled) BrandBlue else Color.Transparent, CircleShape)
                            .then(if (filled) Modifier else Modifier.border(1.dp, Vsoft.colors.outline, CircleShape))
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            val message = when {
                remaining > 0L -> "تا ${((remaining + 999L) / 1_000L).toInt().toString().toPersianDigits()} ثانیه دیگه صبر کن"
                error -> "رمز اشتباهه"
                else -> " "
            }
            Text(message, color = if (error || remaining > 0L) ExpenseRed else Vsoft.colors.textSecondary, fontSize = 12.sp)
        }

        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Column(
                modifier = Modifier.padding(bottom = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                digits.chunked(3).forEach { rowDigits ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        rowDigits.forEach { digit ->
                            if (digit.isEmpty()) {
                                Spacer(Modifier.size(72.dp))
                            } else {
                                val enabled = remaining <= 0L
                                Surface(
                                    modifier = Modifier.size(72.dp).then(
                                        if (enabled) Modifier.bounceClick {
                                            error = false
                                            when (digit) {
                                                "backspace" -> {
                                                    if (pin.isNotEmpty()) pin = pin.dropLast(1)
                                                }
                                                else -> {
                                                    if (pin.length < 4) {
                                                        val next = pin + digit.toLatinDigits()
                                                        if (next.length == 4) submit(next) else pin = next
                                                    }
                                                }
                                            }
                                        } else Modifier
                                    ),
                                    shape = CircleShape,
                                    color = Vsoft.colors.surfaceHigh,
                                    contentColor = Vsoft.colors.textPrimary,
                                    shadowElevation = 0.dp
                                ) {
                                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        if (digit == "backspace") {
                                            Icon(Icons.Filled.Backspace, contentDescription = "حذف", tint = Vsoft.colors.textPrimary)
                                        } else {
                                            Text(digit.toPersianDigits(), fontSize = 24.sp, fontWeight = FontWeight.Medium)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        Text(
            "رمز رو فراموش کردی؟ فقط با پاک‌کردن اطلاعات برنامه از تنظیمات گوشی ممکنه",
            color = Vsoft.colors.textSecondary,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 8.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

private fun Modifier.offsetForShake(value: Float): Modifier =
    this.then(Modifier.offset { IntOffset(value.roundToInt(), 0) })
