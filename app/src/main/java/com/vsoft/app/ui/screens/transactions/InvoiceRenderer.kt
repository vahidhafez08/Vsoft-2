package com.vsoft.app.ui.screens.transactions

import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextDirectionHeuristics
import android.text.TextPaint
import androidx.core.content.res.ResourcesCompat
import com.vsoft.app.R
import com.vsoft.app.data.model.TransactionEntity
import com.vsoft.app.util.formatJalali
import com.vsoft.app.util.toMoney
import com.vsoft.app.util.toPersianDigits
import kotlin.math.abs
import kotlin.math.max

internal object InvoiceRenderer {
    const val W = 1080f
    private const val M = 48f
    private const val GAP = 22f
    private const val CARD_W = (W - 2 * M - GAP) / 2f
    private const val HEADER_H = 225f
    private const val SUMMARY_H = 200f
    private const val TOTALS_H = 180f
    private const val FOOTER_H = 105f
    private const val BRAND = "Vesta"

    data class Palette(
        val bg: Int, val bg2: Int, val surface: Int, val surface2: Int,
        val outline: Int, val outlineBright: Int, val text: Int, val muted: Int,
        val accent: Int, val income: Int, val expense: Int, val glow: Int
    )

    fun palette(dark: Boolean) = if (dark) Palette(
        0xFF070D20.toInt(), 0xFF111C43.toInt(), 0xB52B3A70.toInt(), 0xD91B2852.toInt(),
        0x665E76D8, 0xAA9BAEFF.toInt(), 0xFFF5F6FF.toInt(), 0xFF9EACD4.toInt(),
        0xFF9B7BFF.toInt(), 0xFF20D66B.toInt(), 0xFFFF304D.toInt(), 0xFF647EFF.toInt()
    ) else Palette(
        0xFFF0F2FF.toInt(), 0xFFDDE3FF.toInt(), 0xBFFFFFFF.toInt(), 0xEAFBFCFF.toInt(),
        0x88FFFFFF.toInt(), 0xFFFFFFFF.toInt(), 0xFF090F28.toInt(), 0xFF687493.toInt(),
        0xFF8A63F6.toInt(), 0xFF16A765.toInt(), 0xFFE52E36.toInt(), 0xFF9DAAFF.toInt()
    )

    class Meta(val number: String, val dateText: String, val income: Long, val expense: Long) {
        val net: Long get() = income - expense
    }

    fun meta(tx: List<TransactionEntity>) = Meta(
        (System.currentTimeMillis() % 1000000L).toString().padStart(6, '0').toPersianDigits(),
        formatJalali(System.currentTimeMillis()).toPersianDigits(),
        tx.filter { it.isIncome }.sumOf { it.amount },
        tx.filter { !it.isIncome }.sumOf { it.amount }
    )

    private fun font(context: Context, bold: Boolean): Typeface =
        runCatching {
            ResourcesCompat.getFont(context, if (bold) R.font.vazirmatn_bold else R.font.vazirmatn_regular)
        }.getOrNull() ?: Typeface.create("sans-serif", if (bold) Typeface.BOLD else Typeface.NORMAL)

    private fun tp(context: Context, color: Int, size: Float, bold: Boolean = false) =
        TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            textSize = size
            typeface = font(context, bold)
        }

    private fun makeLayout(value: String, p: TextPaint, width: Float): StaticLayout {
        val safe = value.ifBlank { " " }
        return StaticLayout.Builder.obtain(safe, 0, safe.length, p, width.toInt().coerceAtLeast(1))
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setTextDirection(TextDirectionHeuristics.RTL)
            .setIncludePad(false)
            .setBreakStrategy(Layout.BREAK_STRATEGY_HIGH_QUALITY)
            .setMaxLines(Int.MAX_VALUE)
            .build()
    }

    private fun drawText(canvas: Canvas, value: String, x: Float, y: Float, width: Float, p: TextPaint): Int {
        val layout = makeLayout(value, p, width)
        canvas.save()
        canvas.translate(x, y)
        layout.draw(canvas)
        canvas.restore()
        return layout.height
    }

    private fun cardHeight(context: Context, tx: TransactionEntity): Float {
        val title = makeLayout(tx.title.ifBlank { tx.category }, tp(context, 0xFF000000.toInt(), 29f, true), CARD_W - 64f)
        return max(178f, title.height + 116f)
    }

    private fun rowHeights(context: Context, all: List<TransactionEntity>): List<Float> =
        all.chunked(2).map { row -> row.maxOf { cardHeight(context, it) } }

    fun imageHeight(context: Context, all: List<TransactionEntity>): Int {
        val rows = rowHeights(context, all)
        val transactionsHeight = rows.sum() + GAP * (rows.size - 1).coerceAtLeast(0)
        return (HEADER_H + SUMMARY_H + 32f + transactionsHeight + 36f + TOTALS_H + FOOTER_H).toInt()
    }

    fun paginate(context: Context, all: List<TransactionEntity>, pageHeight: Float): List<IntRange> {
        val rows = rowHeights(context, all)
        val result = mutableListOf<IntRange>()
        var start = 0
        var used = HEADER_H + SUMMARY_H + 30f
        rows.forEachIndexed { rowIndex, rowHeight ->
            val limit = pageHeight - FOOTER_H - 24f
            if (used + rowHeight + GAP + TOTALS_H > limit && rowIndex * 2 > start) {
                result.add(start until (rowIndex * 2).coerceAtMost(all.size))
                start = rowIndex * 2
                used = HEADER_H + 100f
            }
            used += rowHeight + GAP
        }
        if (start < all.size || result.isEmpty()) result.add(start until all.size)
        return result
    }

    private fun glassCard(canvas: Canvas, rect: RectF, c: Palette, radius: Float = 38f, strong: Boolean = false) {
        val shadow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = c.bg
            setShadowLayer(if (strong) 26f else 18f, 0f, 10f, 0x55000000)
        }
        canvas.drawRoundRect(rect, radius, radius, shadow)
        val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                rect.left, rect.top, rect.right, rect.bottom,
                intArrayOf(c.surface2, c.surface, c.bg2.copyAlpha(170)),
                floatArrayOf(0f, 0.52f, 1f), Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(rect, radius, radius, fill)
        val glow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(rect.left, rect.top, rect.right * 0.9f, rect.bottom,
                intArrayOf(c.outlineBright, c.outline, 0x00FFFFFF), null, Shader.TileMode.CLAMP)
            style = Paint.Style.STROKE
            strokeWidth = if (strong) 3.2f else 2.2f
        }
        canvas.drawRoundRect(rect, radius, radius, glow)
        canvas.save()
        val clip = Path().apply { addRoundRect(rect, radius, radius, Path.Direction.CW) }
        canvas.clipPath(clip)
        val shine = Path().apply {
            moveTo(rect.left + rect.width() * 0.28f, rect.top)
            lineTo(rect.left + rect.width() * 0.55f, rect.top)
            lineTo(rect.left + rect.width() * 0.34f, rect.bottom)
            lineTo(rect.left + rect.width() * 0.08f, rect.bottom)
            close()
        }
        canvas.drawPath(shine, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(rect.left, rect.top, rect.right * 0.55f, rect.bottom,
                intArrayOf(0x24FFFFFF, 0x08FFFFFF, 0x00FFFFFF), null, Shader.TileMode.CLAMP)
        })
        canvas.restore()
        val topLine = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(rect.left + radius, rect.top, rect.right - radius, rect.top,
                intArrayOf(0x00FFFFFF, 0x88FFFFFF, 0x00FFFFFF), null, Shader.TileMode.CLAMP)
            strokeWidth = 2f
        }
        canvas.drawLine(rect.left + radius, rect.top + 1f, rect.right - radius, rect.top + 1f, topLine)
    }

    private fun Int.copyAlpha(alpha: Int): Int = (this and 0x00FFFFFF) or (alpha.coerceIn(0, 255) shl 24)

    private fun drawBackground(canvas: Canvas, height: Float, c: Palette) {
        canvas.drawColor(c.bg)
        canvas.drawRect(0f, 0f, W, height, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(0f, 0f, W, height, c.bg, c.bg2, Shader.TileMode.CLAMP)
        })
        val top = Path().apply {
            moveTo(0f, 0f); lineTo(W * 0.53f, 0f); cubicTo(W * 0.43f, 130f, W * 0.28f, 205f, 0f, 320f); close()
        }
        canvas.drawPath(top, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(0f, 0f, W * 0.55f, 310f,
                intArrayOf(c.glow.copyAlpha(150), c.glow.copyAlpha(35), 0x00000000), null, Shader.TileMode.CLAMP)
        })
        val bottom = Path().apply {
            moveTo(W, height); lineTo(W * 0.52f, height); cubicTo(W * 0.74f, height - 80f, W * 0.90f, height - 90f, W, height - 230f); close()
        }
        canvas.drawPath(bottom, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(W, height, W * 0.58f, height - 240f,
                intArrayOf(c.glow.copyAlpha(175), c.glow.copyAlpha(38), 0x00000000), null, Shader.TileMode.CLAMP)
        })
        canvas.drawCircle(40f, 30f, 280f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(40f, 30f, 280f, intArrayOf(c.glow.copyAlpha(70), 0x00000000), null, Shader.TileMode.CLAMP)
        })
    }

    private fun drawHeader(context: Context, canvas: Canvas, meta: Meta, c: Palette) {
        val back = RectF(M + 16f, 58f, M + 128f, 170f)
        glassCard(canvas, back, c, 34f)
        val arrow = Path().apply {
            moveTo(back.centerX() + 14f, back.centerY() - 22f)
            lineTo(back.centerX() - 8f, back.centerY())
            lineTo(back.centerX() + 14f, back.centerY() + 22f)
        }
        canvas.drawPath(arrow, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = c.text; style = Paint.Style.STROKE; strokeWidth = 8f; strokeCap = Paint.Cap.ROUND; strokeJoin = Paint.Join.ROUND
        })
        drawText(canvas, BRAND, M + 150f, 87f, 260f, tp(context, c.text, 38f, true))
        drawText(canvas, "فاکتور", W - M - 390f, 62f, 390f, tp(context, c.text, 62f, true))
        drawText(canvas, "شماره " + meta.number + "  ·  " + meta.dateText,
            W - M - 420f, 150f, 420f, tp(context, c.muted, 25f))
    }

    private fun drawNetCard(context: Context, canvas: Canvas, y: Float, meta: Meta, c: Palette): Float {
        val rect = RectF(M, y, W - M, y + SUMMARY_H)
        glassCard(canvas, rect, c, 52f, true)
        val iconRect = RectF(M + 62f, y + 35f, M + 192f, y + 165f)
        glassCard(canvas, iconRect, c, 38f)
        val icon = Path().apply {
            moveTo(iconRect.left + 36f, iconRect.top + 32f)
            lineTo(iconRect.right - 34f, iconRect.top + 32f)
            lineTo(iconRect.right - 34f, iconRect.bottom - 28f)
            lineTo(iconRect.centerX(), iconRect.bottom - 48f)
            lineTo(iconRect.left + 36f, iconRect.bottom - 28f)
            close()
        }
        canvas.drawPath(icon, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = c.accent; style = Paint.Style.STROKE; strokeWidth = 9f; strokeJoin = Paint.Join.ROUND
        })
        for (i in 0..2) canvas.drawLine(iconRect.left + 54f, iconRect.top + 58f + i * 24f,
            iconRect.right - 54f, iconRect.top + 58f + i * 24f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = c.accent; strokeWidth = 7f; strokeCap = Paint.Cap.ROUND })
        drawText(canvas, "خالص (تومان)", M + 235f, y + 42f, W - 2 * M - 280f,
            tp(context, c.muted, 30f, true))
        val netColor = if (meta.net < 0) c.expense else c.income
        drawText(canvas, (if (meta.net < 0) "−" else "+") + abs(meta.net).toMoney(),
            M + 235f, y + 102f, W - 2 * M - 280f, tp(context, netColor, 62f, true))
        return y + SUMMARY_H
    }

    private fun drawCard(context: Context, canvas: Canvas, tx: TransactionEntity, x: Float, y: Float, h: Float, c: Palette) {
        val rect = RectF(x, y, x + CARD_W, y + h)
        glassCard(canvas, rect, c, 42f)
        val amountColor = if (tx.isIncome) c.income else c.expense
        drawText(canvas, (if (tx.isIncome) "+" else "−") + tx.amount.toMoney(),
            x + 28f, y + 26f, CARD_W - 56f, tp(context, amountColor, 34f, true))
        val title = tx.title.ifBlank { tx.category }
        val titleHeight = drawText(canvas, title, x + 28f, y + 82f, CARD_W - 56f,
            tp(context, c.text, 27f, true))
        if (tx.category.isNotBlank() && tx.category != title) {
            drawText(canvas, tx.category, x + 28f, y + 88f + titleHeight, CARD_W - 56f,
                tp(context, c.muted, 22f))
        }
        drawText(canvas, formatJalali(tx.dateMillis).toPersianDigits(),
            x + 28f, y + h - 48f, CARD_W - 56f, tp(context, c.muted, 22f))
    }

    private fun drawTotals(context: Context, canvas: Canvas, y: Float, meta: Meta, c: Palette): Float {
        val rect = RectF(M, y, W - M, y + TOTALS_H)
        glassCard(canvas, rect, c, 48f)
        drawText(canvas, "جمع درآمد", M + 42f, y + 22f, 360f, tp(context, c.text, 30f, true))
        drawText(canvas, "+" + meta.income.toMoney(), M + 42f, y + 24f, 360f,
            tp(context, c.income, 34f, true))
        drawText(canvas, "جمع هزینه", M + 42f, y + 92f, 360f, tp(context, c.text, 30f, true))
        drawText(canvas, "−" + meta.expense.toMoney(), M + 42f, y + 94f, 360f,
            tp(context, c.expense, 34f, true))
        drawText(canvas, "جمع درآمد", W - M - 390f, y + 22f, 350f, tp(context, c.text, 29f, true))
        drawText(canvas, "جمع هزینه", W - M - 390f, y + 92f, 350f, tp(context, c.text, 29f, true))
        canvas.drawLine(M + 42f, y + 82f, W - M - 42f, y + 82f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = c.outline; strokeWidth = 2f })
        val net = RectF(M, y + TOTALS_H - 6f, W - M, y + TOTALS_H + 116f)
        glassCard(canvas, net, c, 48f, true)
        drawText(canvas, "خالص", W - M - 220f, net.top + 31f, 170f, tp(context, c.text, 38f, true))
        drawText(canvas, (if (meta.net < 0) "−" else "+") + abs(meta.net).toMoney() + " تومان",
            M + 42f, net.top + 28f, W - 2 * M - 300f,
            tp(context, if (meta.net < 0) c.expense else c.income, 43f, true))
        return net.bottom
    }

    fun drawPage(context: Context, canvas: Canvas, all: List<TransactionEntity>, range: IntRange,
        pageNo: Int, pageCount: Int, meta: Meta, pageHeight: Float, c: Palette) {
        drawBackground(canvas, pageHeight, c)
        drawHeader(context, canvas, meta, c)
        var y = HEADER_H
        if (pageNo == 1) y = drawNetCard(context, canvas, y, meta, c) + 30f
        else {
            drawText(canvas, "ادامه تراکنش‌ها", M, y + 20f, W - 2 * M, tp(context, c.text, 30f, true))
            y += 95f
        }
        var index = if (range.isEmpty()) all.size else range.first.coerceAtLeast(0)
        val end = if (range.isEmpty()) index else (range.last + 1).coerceAtMost(all.size)
        while (index < end) {
            val first = all[index]
            val second = if (index + 1 < end) all[index + 1] else null
            val h = max(cardHeight(context, first), second?.let { cardHeight(context, it) } ?: 0f)
            drawCard(context, canvas, first, M, y, h, c)
            if (second != null) drawCard(context, canvas, second, M + CARD_W + GAP, y, h, c)
            y += h + GAP
            index += 2
        }
        if (pageNo == pageCount) y = drawTotals(context, canvas, y + 8f, meta, c) + 18f
        val footer = "ساخته‌شده با " + BRAND +
            if (pageCount > 1) "   ·   صفحه " + pageNo.toString().toPersianDigits() + " از " + pageCount.toString().toPersianDigits() else ""
        drawText(canvas, footer, M, minOf(y + 25f, pageHeight - 55f), W - 2 * M,
            tp(context, c.muted, 22f, true))
    }
}
