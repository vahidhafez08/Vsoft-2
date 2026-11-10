package com.vsoft.app.ui.screens.transactions

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextDirectionHeuristics
import android.text.TextPaint
import android.text.TextUtils
import com.vsoft.app.data.model.TransactionEntity
import com.vsoft.app.util.formatJalali
import com.vsoft.app.util.toMoney
import com.vsoft.app.util.toPersianDigits
import java.io.File
import java.io.FileOutputStream

object InvoicePdfGenerator {
    fun create(context: Context, transactions: List<TransactionEntity>): File {
        require(transactions.isNotEmpty()) { "هیچ تراکنشی انتخاب نشده است" }

        val document = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val margin = 36f

        fun paint(color: Int, size: Float, bold: Boolean = false) = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            textSize = size
            typeface = Typeface.create("sans-serif", if (bold) Typeface.BOLD else Typeface.NORMAL)
        }

        fun drawText(
            canvas: android.graphics.Canvas,
            text: String,
            left: Float,
            top: Float,
            width: Int,
            textPaint: TextPaint,
            rtl: Boolean = true,
            alignment: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL,
            maxLines: Int = 1
        ) {
            if (text.isBlank() || width <= 0) return
            val layout = StaticLayout.Builder.obtain(text, 0, text.length, textPaint, width)
                .setAlignment(alignment)
                .setTextDirection(if (rtl) TextDirectionHeuristics.RTL else TextDirectionHeuristics.LTR)
                .setIncludePad(false)
                .setMaxLines(maxLines)
                .setEllipsize(TextUtils.TruncateAt.END)
                .build()
            canvas.save()
            canvas.translate(left, top)
            layout.draw(canvas)
            canvas.restore()
        }

        val navy = android.graphics.Color.rgb(24, 42, 72)
        val muted = android.graphics.Color.rgb(112, 124, 142)
        val line = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFE2E8F0.toInt()
            strokeWidth = 1f
        }
        val income = transactions.filter { it.isIncome }.sumOf { it.amount }
        val expense = transactions.filter { !it.isIncome }.sumOf { it.amount }
        val rowsPerPage = 20
        val chunks = transactions.chunked(rowsPerPage)

        chunks.forEachIndexed { pageIndex, rows ->
            val page = document.startPage(
                PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageIndex + 1).create()
            )
            val canvas = page.canvas
            val titlePaint = paint(navy, 22f, true)
            val regular = paint(0xFF233047.toInt(), 10.5f)
            val small = paint(muted, 9f)
            val bold = paint(navy, 10f, true)

            drawText(canvas, "فاکتور تراکنش‌ها", margin, 38f, 400, titlePaint)
            drawText(
                "شماره فاکتور: ${(System.currentTimeMillis() % 1000000L).toString().toPersianDigits()}",
                margin, 76f, 240, regular
            )
            drawText("تاریخ صدور: ${formatJalali(System.currentTimeMillis())}", 300f, 76f, 250, regular)
            drawText(
                "صفحه ${(pageIndex + 1).toString().toPersianDigits()} از ${chunks.size.toString().toPersianDigits()}",
                margin, 98f, 180, small
            )

            var y = 137f
            canvas.drawRoundRect(
                margin, y - 8f, pageWidth - margin, y + 22f, 7f, 7f,
                Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFF1F5F9.toInt() }
            )
            drawText(canvas, "شرح تراکنش", 310f, y, 230, bold)
            drawText(canvas, "تاریخ", 190f, y, 105, bold)
            drawText(canvas, "مبلغ (تومان)", 48f, y, 125, bold, rtl = false)
            y += 38f

            rows.forEachIndexed { index, tx ->
                val rowIndex = pageIndex * rowsPerPage + index
                drawText(
                    canvas,
                    tx.title.ifBlank { "بدون عنوان" },
                    310f, y, 230, paint(0xFF233047.toInt(), 10f, true)
                )
                drawText(canvas, tx.category, 310f, y + 14f, 230, small)
                drawText(canvas, formatJalali(tx.dateMillis), 190f, y + 5f, 105, regular)
                drawText(
                    canvas,
                    tx.amount.toMoney(),
                    48f, y + 5f, 125,
                    paint(if (tx.isIncome) 0xFF278B62.toInt() else 0xFFC94C4C.toInt(), 10f, true),
                    rtl = false
                )
                drawText(
                    (rowIndex + 1).toString().toPersianDigits(),
                    548f, y + 5f, 10, small
                )
                canvas.drawLine(margin, y + 32f, pageWidth - margin, y + 32f, line)
                y += 35f
            }

            if (pageIndex == chunks.lastIndex) {
                y += 20f
                canvas.drawRoundRect(
                    margin, y - 10f, pageWidth - margin, y + 112f, 10f, 10f,
                    Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFF8FAFC.toInt() }
                )
                drawText(canvas, "خلاصه مالی", 330f, y + 2f, 210, paint(navy, 12f, true))
                drawText(canvas, "جمع درآمدها", 330f, y + 32f, 210, regular)
                drawText(canvas, income.toMoney() + " تومان", 48f, y + 32f, 220, paint(0xFF278B62.toInt(), 10f, true), rtl = false)
                drawText(canvas, "جمع هزینه‌ها", 330f, y + 58f, 210, regular)
                drawText(canvas, expense.toMoney() + " تومان", 48f, y + 58f, 220, paint(0xFFC94C4C.toInt(), 10f, true), rtl = false)
                drawText(canvas, "خالص", 330f, y + 84f, 210, bold)
                drawText(
                    (if (income - expense < 0) "−" else "") + kotlin.math.abs(income - expense).toMoney() + " تومان",
                    48f, y + 84f, 220, bold, rtl = false
                )
            }

            drawText(
                canvas,
                "این فاکتور توسط اپلیکیشن Vsoft تولید شده است.",
                margin, 810f, pageWidth - margin.toInt() * 2,
                small,
                alignment = Layout.Alignment.ALIGN_CENTER
            )
            document.finishPage(page)
        }

        val directory = File(context.cacheDir, "invoices").apply { mkdirs() }
        val output = File(directory, "invoice-${System.currentTimeMillis()}.pdf")
        FileOutputStream(output).use { document.writeTo(it) }
        document.close()
        return output
    }
}
