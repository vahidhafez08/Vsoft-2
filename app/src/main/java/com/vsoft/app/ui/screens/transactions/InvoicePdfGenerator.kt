package com.vsoft.app.ui.screens.transactions

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
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
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.BLACK
            textSize = 10f
            typeface = Typeface.create("sans-serif", Typeface.NORMAL)
            textAlign = Paint.Align.RIGHT
        }
        val bold = Paint(paint).apply { typeface = Typeface.create("sans-serif", Typeface.BOLD) }
        val line = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFB7C2D0.toInt(); strokeWidth = 1f }
        val income = transactions.filter { it.isIncome }.sumOf { it.amount }
        val expense = transactions.filter { !it.isIncome }.sumOf { it.amount }
        val rowsPerPage = 25
        val chunks = transactions.chunked(rowsPerPage)
        chunks.forEachIndexed { pageIndex, rows ->
            val page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageIndex + 1).create())
            val canvas = page.canvas
            var y = 54f
            canvas.drawText("فاکتور", pageWidth - margin, y, Paint(bold).apply { textSize = 22f; color = 0xFF2459C4.toInt() })
            y += 24f
            canvas.drawText("شماره فاکتور: ${(System.currentTimeMillis() % 1000000L).toString().toPersianDigits()}", pageWidth - margin, y, paint)
            y += 18f
            canvas.drawText("تاریخ: ${formatJalali(System.currentTimeMillis())}", pageWidth - margin, y, paint)
            y += 24f
            val headers = listOf("ردیف", "شرح", "دسته‌بندی", "تاریخ", "مبلغ")
            val xs = listOf(548f, 455f, 330f, 205f, 55f)
            headers.forEachIndexed { i, header -> canvas.drawText(header, xs[i], y, bold) }
            y += 8f
            canvas.drawLine(margin, y, pageWidth - margin, y, line)
            y += 18f
            rows.forEachIndexed { index, tx ->
                val values = listOf(
                    (pageIndex * rowsPerPage + index + 1).toPersianDigits(),
                    tx.title.take(26),
                    tx.category.take(18),
                    formatJalali(tx.dateMillis),
                    tx.amount.toMoney() + " تومان"
                )
                values.forEachIndexed { i, value -> canvas.drawText(value, xs[i], y, paint) }
                y += 22f
                canvas.drawLine(margin, y - 8f, pageWidth - margin, y - 8f, line)
            }
            if (pageIndex == chunks.lastIndex) {
                y += 24f
                canvas.drawText("جمع درآمدها: ${income.toMoney()} تومان", pageWidth - margin, y, bold); y += 20f
                canvas.drawText("جمع هزینه‌ها: ${expense.toMoney()} تومان", pageWidth - margin, y, bold); y += 20f
                canvas.drawText("مبلغ نهایی (خالص): ${(income - expense).toMoney()} تومان", pageWidth - margin, y, bold)
            }
            document.finishPage(page)
        }
        val directory = File(context.cacheDir, "invoices").apply { mkdirs() }
        val output = File(directory, "invoice-${System.currentTimeMillis()}.pdf")
        FileOutputStream(output).use { document.writeTo(it) }
        document.close()
        return output
    }
}
