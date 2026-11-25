package com.vsoft.app.ui.screens

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.vsoft.app.R
import com.vsoft.app.ui.theme.Vsoft
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val LogoSize = 112.dp
private val LogoCorner = 32.dp
private const val START_SCALE = 0.86f
private const val REDUCED_MOTION_HOLD_MS = 450L

/** True when the user has turned system animations off. */
@Composable
fun rememberReduceMotion(): Boolean {
    val context = LocalContext.current
    return remember {
        try {
            Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f
            ) == 0f
        } catch (_: Exception) {
            false
        }
    }
}

/**
 * Minimal splash: flat theme background + the real Vesta logo (R.drawable.vesta_logo).
 * Fade-in + scale (0.86 -> 1), one subtle light sweep, ~1.05s before onFinished().
 */
@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val reduceMotion = rememberReduceMotion()
    val finished = rememberUpdatedState(onFinished)

    val logoAlpha = remember { Animatable(if (reduceMotion) 1f else 0f) }
    val logoScale = remember { Animatable(if (reduceMotion) 1f else START_SCALE) }
    val shine = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        if (reduceMotion) {
            delay(REDUCED_MOTION_HOLD_MS)
        } else {
            coroutineScope {
                launch { logoAlpha.animateTo(1f, tween(400, easing = FastOutSlowInEasing)) }
                launch { logoScale.animateTo(1f, tween(500, easing = FastOutSlowInEasing)) }
                launch {
                    delay(350)
                    shine.animateTo(1f, tween(550, easing = LinearOutSlowInEasing))
                }
            }
            delay(150)
        }
        finished.value()
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Vsoft.colors.bg),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.vesta_logo),
            contentDescription = "Vesta",
            modifier = Modifier
                .size(LogoSize)
                .graphicsLayer {
                    alpha = logoAlpha.value
                    scaleX = logoScale.value
                    scaleY = logoScale.value
                }
                .clip(RoundedCornerShape(LogoCorner))
                .drawWithContent {
                    drawContent()
                    val p = shine.value
                    if (p > 0f && p < 1f) {
                        val band = size.width * 0.7f
                        val x = -band + (size.width + band) * p
                        drawRect(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0f),
                                    Color.White.copy(alpha = 0.22f),
                                    Color.White.copy(alpha = 0f)
                                ),
                                start = Offset(x, 0f),
                                end = Offset(x + band, band * 0.35f)
                            )
                        )
                    }
                }
        )
    }
}
