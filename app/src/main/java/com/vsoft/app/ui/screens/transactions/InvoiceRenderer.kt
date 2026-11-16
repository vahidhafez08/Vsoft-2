package com.vsoft.app.ui.screens.transactions

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextDirectionHeuristics
import android.text.TextPaint
import android.text.TextUtils
import com.vsoft.app.data.model.TransactionEntity
import com.vsoft.app.util.formatJalali
import com.vsoft.app.util.toMoney
import com.vsoft.app.util.toPersianDigits
import kotlin.math.abs

internal object InvoiceRenderer {
    const val W = 1080f
    const val ROW_H = 118f
    const val HEADER_H = 540f
    const val HEADER_NEXT_H = 170f
    const val SUMMARY_H = 340f
    const val FOOTER_H = 110f
    private const val M = 84f
    private const val BRAND = "Vesta"
    private val INK = 0xFF111827.toInt()
    private val MUTED = 0xFF8A8F98.toInt()
    private val HAIR = 0xFFE5E7EB.toInt()
    private val GREEN = 0xFF1F9D63.toInt()
    private val RED = 0xFFD9423E.toInt()
    private val ACCENT = 0xFF2F6BFF.toInt()
    class Meta(val number: String, val dateText: String, val income: Long, val expense: Long) { val net: Long get() = income - expense }
    fun meta(tx: List<TransactionEntity>) = Meta(
        (System.currentTimeMillis() % 1000000L).toString().padStart(6, '0').toPersianDigits(),
        formatJalali(System.currentTimeMillis()).toPersianDigits(),
        tx.filter { it.isIncome }.sumOf { it.amount }, tx.filter { !it.isIncome }.sumOf { it.amount })
    fun paginate(n: Int, pageH: Float): List<IntRange> {
        val c1 = ((pageH - HEADER_H - FOOTER_H) / ROW_H).toInt().coerceAtLeast(1)
        val cn = ((pageH - HEADER_NEXT_H - FOOTER_H) / ROW_H).toInt().coerceAtLeast(1)
        val out = mutableListOf<IntRange>(); var start = 0; var cap = c1
        while (start < n) { val end = minOf(n, start + cap); out += (start until end); start = end; cap = cn }
        val headerH = if (out.size == 1) HEADER_H else HEADER_NEXT_H
        val used = headerH + out.last().count() * ROW_H + SUMMARY_H + FOOTER_H
        if (used > pageH) out += (n until n)
        return out
    }
    private fun tp(color: Int, size: Float, bold: Boolean = false) = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color; textSize = size; typeface = Typeface.create("sans-serif", if (bold) Typeface.BOLD else Typeface.NORMAL)
    }
    private fun text(canvas: Canvas, s: String, left: Float, top: Float, width: Float, p: TextPaint,
        rtl: Boolean = true, align: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL) {
        if (s.isBlank() || width <= 0f) return
        val layout = StaticLayout.Builder.obtain(s, 0, s.length, p, width.toInt()).setAlignment(align)
            .setTextDirection(if (rtl) TextDirectionHeuristics.RTL else TextDirectionHeuristics.LTR)
            .setIncludePad(false).setMaxLines(1).setEllipsize(TextUtils.TruncateAt.END).build()
        canvas.save(); canvas.translate(left, top); layout.draw(canvas); canvas.restore()
    }
    private fun money(v: Long) = abs(v).toMoney()
    private fun signed(v: Long) = (if (v < 0) "−" else if (v > 0) "+" else "") + abs(v).toMoney()
    fun drawPage(canvas: Canvas, all: List<TransactionEntity>, range: IntRange, pageNo: Int, pageCount: Int,
        meta: Meta, pageH: Float, truncated: Int = 0) {
        val right = W - M; val full = W - 2 * M
        canvas.drawRect(0f, 0f, W, pageH, Paint().apply { color = 0xFFFFFFFF.toInt() })
        val hair = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = HAIR; strokeWidth = 2f }
        var y: Float
        if (pageNo == 1) {
            text(canvas, BRAND, M, 96f, 300f, tp(ACCENT, 34f, true), rtl = false)
            text(canvas, "فاکتور", M, 150f, full, tp(INK, 78f, true))
            text(canvas, "شمارهٔ ${meta.number}  ·  ${meta.dateText}", M, 262f, full, tp(MUTED, 30f))
            text(canvas, "خالص (تومان)", M, 350f, full, tp(MUTED, 30f))
            text(canvas, signed(meta.net), M, 394f, full, tp(if (meta.net < 0) RED else INK, 92f, true), rtl = false, align = Layout.Alignment.ALIGN_OPPOSITE)
            y = HEADER_H
        } else { text(canvas, "فاکتور  ·  شمارهٔ ${meta.number}", M, 80f, full, tp(MUTED, 30f)); y = HEADER_NEXT_H }
        canvas.drawLine(M, y - 24f, right, y - 24f, hair)
        val titlePaint = tp(INK, 36f, true); val subPaint = tp(MUTED, 27f)
        for (i in range) {
            val tx = all[i]; val date = formatJalali(tx.dateMillis).toPersianDigits()
            val sub = if (tx.title.isBlank() || tx.title == tx.category) date else "${tx.category}  ·  $date"
            text(canvas, tx.title.ifBlank { tx.category }, M + 320f, y + 22f, full - 320f, titlePaint)
            text(canvas, sub, M + 320f, y + 70f, full - 320f, subPaint)
            text(canvas, (if (tx.isIncome) "+" else "−") + tx.amount.toMoney(), M, y + 36f, 300f, tp(if (tx.isIncome) GREEN else RED, 38f, true), rtl = false)
            canvas.drawLine(M, y + ROW_H - 1f, right, y + ROW_H - 1f, hair); y += ROW_H
        }
        if (pageNo == pageCount) {
            if (truncated > 0) { text(canvas, "و ${truncated.toString().toPersianDigits()} تراکنش دیگر در نسخهٔ PDF", M, y + 20f, full, tp(MUTED, 28f)); y += ROW_H }
            y += 30f
            fun line(label: String, value: String, color: Int, size: Float, bold: Boolean, top: Float) {
                text(canvas, label, M + 420f, top, full - 420f, tp(if (bold) INK else MUTED, size, bold))
                text(canvas, value, M, top, 420f, tp(color, size, true), rtl = false)
            }
            line("جمع درآمد", "+" + money(meta.income) + " تومان", GREEN, 32f, false, y)
            line("جمع هزینه", "−" + money(meta.expense) + " تومان", RED, 32f, false, y + 84f)
            canvas.drawLine(M, y + 176f, right, y + 176f, hair)
            line("خالص", signed(meta.net) + " تومان", if (meta.net < 0) RED else INK, 40f, true, y + 206f)
        }
        val footer = "ساخته‌شده با $BRAND" + if (pageCount > 1) "  ·  صفحه ${pageNo.toString().toPersianDigits()} از ${pageCount.toString().toPersianDigits()}" else ""
        text(canvas, footer, M, pageH - 70f, full, tp(MUTED, 24f), align = Layout.Alignment.ALIGN_CENTER)
    }
}
