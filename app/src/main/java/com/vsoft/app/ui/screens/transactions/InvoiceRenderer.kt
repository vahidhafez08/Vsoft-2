package com.vsoft.app.ui.screens.transactions

import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
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
    private const val M = 64f
    private const val GAP = 22f
    private const val CARD_W = (W - 2 * M - GAP) / 2f
    private const val FIRST_HEADER = 390f
    private const val NEXT_HEADER = 140f
    private const val SUMMARY_H = 270f
    private const val FOOTER_H = 70f
    private const val BRAND = "Vesta"

    data class Palette(val bg: Int, val surface: Int, val outline: Int, val text: Int, val muted: Int, val accent: Int, val income: Int, val expense: Int)

    fun palette(dark: Boolean) = if (dark) Palette(
        0xFF101827.toInt(), 0xE52A3850.toInt(), 0x665F7EBD, 0xFFF2F5FF.toInt(),
        0xFFB2BED4.toInt(), 0xFF91ABFF.toInt(), 0xFF5BD6A0.toInt(), 0xFFFF8585.toInt()
    ) else Palette(
        0xFFF2F5FC.toInt(), 0xEFFFFFFF.toInt(), 0x66899BC2, 0xFF17233B.toInt(),
        0xFF74819A.toInt(), 0xFF466FE8.toInt(), 0xFF15966A.toInt(), 0xFFD84B55.toInt()
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
        val title = makeLayout(tx.title.ifBlank { tx.category }, tp(context, 0xFF000000.toInt(), 28f, true), CARD_W - 52f)
        return max(178f, title.height + 128f)
    }

    private fun rowHeights(context: Context, all: List<TransactionEntity>): List<Float> =
        all.chunked(2).map { row -> row.maxOf { cardHeight(context, it) } }

    fun imageHeight(context: Context, all: List<TransactionEntity>): Int {
        val rows = rowHeights(context, all)
        return (FIRST_HEADER + rows.sum() + GAP * (rows.size - 1).coerceAtLeast(0) + SUMMARY_H + FOOTER_H).toInt()
    }

    fun paginate(context: Context, all: List<TransactionEntity>, pageHeight: Float): List<IntRange> {
        val rows = rowHeights(context, all)
        val result = mutableListOf<IntRange>()
        var start = 0
        var used = FIRST_HEADER
        rows.forEachIndexed { rowIndex, rowHeight ->
            val limit = pageHeight - FOOTER_H
            if (used + rowHeight + GAP > limit && rowIndex * 2 > start) {
                result.add(start until (rowIndex * 2).coerceAtMost(all.size))
                start = rowIndex * 2
                used = NEXT_HEADER
            }
            used += rowHeight + GAP
        }
        if (start < all.size || result.isEmpty()) result.add(start until all.size)
        val lastRange = result.last()
        val lastStart = if (lastRange.isEmpty()) all.size else lastRange.first
        val lastEnd = if (lastRange.isEmpty()) all.size else (lastRange.last + 1).coerceAtMost(all.size)
        val lastRows = rowHeights(context, all.subList(lastStart, lastEnd)).sum()
        val header = if (result.size == 1) FIRST_HEADER else NEXT_HEADER
        if (header + lastRows + SUMMARY_H + FOOTER_H > pageHeight) result.add(all.size until all.size)
        return result
    }

    private fun drawCard(context: Context, canvas: Canvas, tx: TransactionEntity, x: Float, y: Float, h: Float, c: Palette) {
        val rect = RectF(x, y, x + CARD_W, y + h)
        val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(x, y, x + CARD_W, y + h, c.surface, c.bg, Shader.TileMode.CLAMP)
        }
        canvas.drawRoundRect(rect, 28f, 28f, fill)
        canvas.drawRoundRect(rect, 28f, 28f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = c.outline
            style = Paint.Style.STROKE
            strokeWidth = 2f
        })
        val titleHeight = drawText(canvas, tx.title.ifBlank { tx.category }, x + 26f, y + 22f, CARD_W - 52f, tp(context, c.text, 28f, true))
        drawText(canvas, tx.category + "  ·  " + formatJalali(tx.dateMillis).toPersianDigits(),
            x + 26f, y + 32f + titleHeight, CARD_W - 52f, tp(context, c.muted, 21f))
        drawText(canvas, (if (tx.isIncome) "+" else "−") + tx.amount.toMoney(),
            x + 26f, y + h - 52f, CARD_W - 52f, tp(context, if (tx.isIncome) c.income else c.expense, 27f, true))
        canvas.drawRoundRect(RectF(x + CARD_W - 12f, y + 22f, x + CARD_W - 7f, y + h - 22f), 3f, 3f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = if (tx.isIncome) c.income else c.expense; alpha = 90 })
    }

    fun drawPage(context: Context, canvas: Canvas, all: List<TransactionEntity>, range: IntRange,
        pageNo: Int, pageCount: Int, meta: Meta, pageHeight: Float, c: Palette) {
        canvas.drawColor(c.bg)
        val full = W - 2 * M
        val header = if (pageNo == 1) FIRST_HEADER else NEXT_HEADER
        if (pageNo == 1) {
            drawText(canvas, BRAND, M, 44f, 280f, tp(context, c.accent, 29f, true))
            drawText(canvas, "فاکتور تراکنش‌ها", M, 100f, full, tp(context, c.text, 58f, true))
            drawText(canvas, "شماره " + meta.number + "   ·   " + meta.dateText, M, 188f, full, tp(context, c.muted, 24f))
            drawText(canvas, "خالص (تومان)", M, 250f, full, tp(context, c.muted, 23f))
            drawText(canvas, (if (meta.net < 0) "−" else "+") + abs(meta.net).toMoney(),
                M, 288f, full, tp(context, if (meta.net < 0) c.expense else c.text, 54f, true))
        } else {
            drawText(canvas, "فاکتور  ·  " + meta.number, M, 48f, full, tp(context, c.text, 29f, true))
            drawText(canvas, "ادامه تراکنش‌ها", M, 91f, full, tp(context, c.muted, 21f))
        }

        var y = header
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

        if (pageNo == pageCount) {
            y += 2f
            val rect = RectF(M, y, W - M, y + SUMMARY_H - 12f)
            canvas.drawRoundRect(rect, 28f, 28f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = c.surface })
            canvas.drawRoundRect(rect, 28f, 28f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = c.outline; style = Paint.Style.STROKE; strokeWidth = 2f
            })
            drawText(canvas, "جمع درآمد", M + 26f, y + 25f, 400f, tp(context, c.muted, 23f))
            drawText(canvas, "+" + meta.income.toMoney(), M + 26f, y + 62f, 400f, tp(context, c.income, 28f, true))
            drawText(canvas, "جمع هزینه", M + 470f, y + 25f, 400f, tp(context, c.muted, 23f))
            drawText(canvas, "−" + meta.expense.toMoney(), M + 470f, y + 62f, 400f, tp(context, c.expense, 28f, true))
            drawText(canvas, "تراز نهایی", M + 26f, y + 142f, 400f, tp(context, c.muted, 23f))
            drawText(canvas, (if (meta.net < 0) "−" else "+") + abs(meta.net).toMoney() + " تومان",
                M + 26f, y + 180f, full - 52f, tp(context, c.text, 34f, true))
        }
        val footer = "ساخته‌شده با " + BRAND +
            if (pageCount > 1) "   ·   صفحه " + pageNo.toString().toPersianDigits() + " از " + pageCount.toString().toPersianDigits() else ""
        drawText(canvas, footer, M, pageHeight - 45f, full, tp(context, c.muted, 19f))
    }
}
