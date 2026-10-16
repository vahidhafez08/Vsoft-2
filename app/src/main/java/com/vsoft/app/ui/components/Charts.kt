package com.vsoft.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vsoft.app.ui.theme.ExpenseRed
import com.vsoft.app.ui.theme.IncomeGreen
import com.vsoft.app.ui.theme.Vsoft
import kotlin.math.max

data class ChartSlice(val value: Float, val color: Color)

@Composable
fun DonutChart(
    slices: List<ChartSlice>,
    centerTitle: String,
    centerValue: String,
    modifier: Modifier = Modifier
) {
    val trackColor = Vsoft.colors.outline
    Box(modifier.size(210.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize().padding(8.dp)) {
            val strokeWidth = 26.dp.toPx()
            val inset = strokeWidth / 2f
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
            val topLeft = Offset(inset, inset)
            val visible = slices.filter { it.value > 0f }
            val total = visible.sumOf { it.value.toDouble() }.toFloat()

            if (total <= 0f) {
                drawArc(
                    color = trackColor,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth)
                )
            } else {
                val gap = if (visible.size > 1) 3f else 0f
                var start = -90f
                visible.forEach { slice ->
                    val sweep = slice.value / total * 360f
                    val drawSweep = if (sweep > gap + 0.5f) sweep - gap else sweep
                    drawArc(
                        color = slice.color,
                        startAngle = start,
                        sweepAngle = drawSweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth)
                    )
                    start += sweep
                }
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(centerTitle, color = Vsoft.colors.textSecondary, fontSize = 11.sp)
            Text(
                centerValue,
                color = Vsoft.colors.textPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text("تومان", color = Vsoft.colors.textSecondary, fontSize = 11.sp)
        }
    }
}

@Composable
fun LineChart(
    incomeValues: List<Float>,
    expenseValues: List<Float>,
    labels: List<String>,
    modifier: Modifier = Modifier
) {
    val gridColor = Vsoft.colors.outline
    val labelColor = Vsoft.colors.textSecondary
    Column(modifier) {
        Canvas(Modifier.fillMaxWidth().height(140.dp)) {
            val w = size.width
            val h = size.height
            val maxV = max(
                1f,
                max(incomeValues.maxOrNull() ?: 0f, expenseValues.maxOrNull() ?: 0f)
            )
            for (i in 0..3) {
                val y = h * i / 3f
                drawLine(gridColor, Offset(0f, y), Offset(w, y), strokeWidth = 1f)
            }
            drawSeries(incomeValues, IncomeGreen, maxV)
            drawSeries(expenseValues, ExpenseRed, maxV)
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            labels.forEach { Text(it, color = labelColor, fontSize = 10.sp) }
        }
    }
}

// Index 0 (oldest) is drawn on the RIGHT side so the chart reads right-to-left (Persian).
private fun DrawScope.drawSeries(values: List<Float>, color: Color, maxV: Float) {
    if (values.size < 2) return
    val w = size.width
    val h = size.height
    val pad = h * 0.08f
    val count = values.size

    fun px(i: Int): Float = w - (w * i / (count - 1))
    fun py(v: Float): Float = h - pad - (v / maxV) * (h - 2 * pad)

    val line = Path()
    line.moveTo(px(0), py(values[0]))
    for (i in 1 until count) {
        val x0 = px(i - 1)
        val y0 = py(values[i - 1])
        val x1 = px(i)
        val y1 = py(values[i])
        val cx = (x0 + x1) / 2f
        line.cubicTo(cx, y0, cx, y1, x1, y1)
    }

    val fill = Path()
    fill.addPath(line)
    fill.lineTo(px(count - 1), h)
    fill.lineTo(px(0), h)
    fill.close()

    drawPath(fill, Brush.verticalGradient(listOf(color.copy(alpha = 0.25f), Color.Transparent)))
    drawPath(line, color, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
}