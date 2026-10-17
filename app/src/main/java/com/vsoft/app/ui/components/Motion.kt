package com.vsoft.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.vsoft.app.ui.theme.Vsoft
import com.vsoft.app.util.toMoney
import kotlinx.coroutines.delay

/** Click with a springy "press" effect. Put it FIRST in the modifier chain (before clip/background). */
fun Modifier.bounceClick(onClick: () -> Unit): Modifier = composed {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val s by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "bounce"
    )
    this.scale(s).clickable(interactionSource = interaction, indication = null, role = Role.Button, onClick = onClick)
}

/** Fades + slides content up when it first appears. Does not change layout size. */
@Composable
fun Appear(index: Int = 0, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(index.coerceIn(0, 7) * 25L)
        progress.animateTo(1f, tween(250, easing = FastOutSlowInEasing))
    }
    Box(
        modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = progress.value
                translationY = (1f - progress.value) * 48f
            }
    ) { content() }
}

/** Number that counts smoothly from its old value to the new one. */
@Composable
fun AnimatedMoney(
    target: Long,
    modifier: Modifier = Modifier,
    color: Color = Vsoft.colors.textPrimary,
    fontSize: TextUnit = 18.sp,
    fontWeight: FontWeight = FontWeight.Bold
) {
    var from by remember { mutableLongStateOf(0L) }
    var to by remember { mutableLongStateOf(target) }
    val progress = remember { Animatable(0f) }

    LaunchedEffect(target) {
        val current = from + ((to - from) * progress.value.toDouble()).toLong()
        from = current
        to = target
        progress.snapTo(0f)
        progress.animateTo(1f, tween(320, easing = FastOutSlowInEasing))
    }

    val shown = from + ((to - from) * progress.value.toDouble()).toLong()
    Text(
        text = shown.toMoney(),
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontWeight = fontWeight,
        maxLines = 1
    )
}
