package com.vsoft.app.ui.screens.reports

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vsoft.app.ui.theme.ExpenseRed
import com.vsoft.app.ui.theme.IncomeGreen
import com.vsoft.app.ui.theme.Vsoft

/**
 * Income/expense line chart. Time runs right-to-left (RTL app): the first slot is on the right.
 * Series may be shorter than labels (future slots are simply not drawn).
 */
@Composable
fun ReportLines(labels: List<String>, income: List<Long>, expense: List<Long>, modifier: Modifier = Modifier) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(labels, income, expense) {
        progress.snapTo(0f)
        progress.animateTo(1f, tween(800, easing = FastOutSlowInEasing))
    }
    val grid = Vsoft.colors.outline
    val labelColor = Vsoft.colors.textSecondary
    val maxV = maxOf(income.maxOrNull() ?: 0L, expense.maxOrNull() ?: 0L).coerceAtLeast(1L)
    val slots = labels.size.coerceAtLeast(1)
    val step = (slots + 5) / 6

    Column(modifier.fillMaxWidth()) {
        Canvas(Modifier.fillMaxWidth().height(160.dp)) {
            val padV = 8.dp.toPx()
            val chartH = size.height - 2 * padV
            for (g in 0..3) {
                val y = padV + chartH * g / 3f
                drawLine(grid, Offset(0f, y), Offset(size.width, y), 1.dp.toPx())
            }
            clipRect(left = size.width * (1f - progress.value)) {
                drawSeries(income, maxV, slots, IncomeGreen, padV, chartH)
                drawSeries(expense, maxV, slots, ExpenseRed, padV, chartH)
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(Modifier.fillMaxWidth()) {
            labels.forEachIndexed { i, l ->
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    if (i % step == 0) {
                        Text(
                            l,
                            color = labelColor,
                            fontSize = 10.sp,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.wrapContentWidth(unbounded = true)
                        )
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawSeries(values: List<Long>, maxV: Long, slots: Int, color: Color, padV: Float, chartH: Float) {
    if (values.isEmpty()) return
    fun x(i: Int): Float = size.width * (1f - (i + 0.5f) / slots)
    fun y(v: Long): Float = padV + chartH * (1f - v.toFloat() / maxV)
    val path = Path()
    values.forEachIndexed { i, v ->
        if (i == 0) path.moveTo(x(i), y(v)) else path.lineTo(x(i), y(v))
    }
    drawPath(
        path,
        color,
        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
    if (values.size <= 12) {
        values.forEachIndexed { i, v -> drawCircle(color, 3.5.dp.toPx(), Offset(x(i), y(v))) }
    }
}

/** Animated donut. slices = (amount, color). */
@Composable
fun ReportDonut(slices: List<Pair<Long, Color>>, modifier: Modifier = Modifier) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(slices) {
        progress.snapTo(0f)
        progress.animateTo(1f, tween(800, easing = FastOutSlowInEasing))
    }
    val track = Vsoft.colors.outline
    val visible = slices.filter { it.first > 0L }
    val total = visible.sumOf { it.first.toDouble() }
    Canvas(modifier) {
        val stroke = 22.dp.toPx()
        val arcSize = Size(size.width - stroke, size.height - stroke)
        val topLeft = Offset(stroke / 2, stroke / 2)
        if (total <= 0.0 || !total.isFinite()) {
            drawArc(track, 0f, 360f, false, topLeft, arcSize, style = Stroke(stroke))
        } else {
            var start = -90f
            visible.forEach { (v, c) ->
                val full = (v.toDouble() / total * 360.0).toFloat()
                drawArc(c, start, full * progress.value, false, topLeft, arcSize, style = Stroke(stroke))
                start += full
            }
        }
    }
}