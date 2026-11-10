package com.vsoft.app.ui.screens.transactions

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import com.vsoft.app.data.model.TransactionEntity
import com.vsoft.app.util.formatJalali
import com.vsoft.app.util.toMoney
import com.vsoft.app.util.toPersianDigits
import java.io.File
import java.io.FileOutputStream

object InvoiceImageGenerator {
    fun create(context: Context, transactions: List<TransactionEntity>): File {
        require(transactions.isNotEmpty()) { "هیچ تراکنشی انتخاب نشده است" }

        val width = 1200
        val margin = 72f
        val headerHeight = 360f
        val tableHeaderHeight = 82f
        val rowHeight = 76f
        val totalsHeight = 250f
        val footerHeight = 110f
        val height = (headerHeight + tableHeaderHeight + transactions.size * rowHeight + totalsHeight + footerHeight)
            .toInt().coerceAtMost(12000)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val navy = android.graphics.Color.rgb(24, 42, 72)
        val blue = android.graphics.Color.rgb(49, 105, 220)
        val ink = android.graphics.Color.rgb(35, 47, 65)
        val muted = android.graphics.Color.rgb(112, 124, 142)
        val line = android.graphics.Color.rgb(226, 232, 240)
        val pale = android.graphics.Color.rgb(246, 248, 252)
        val green = android.graphics.Color.rgb(37, 139, 98)
        val red = android.graphics.Color.rgb(201, 76, 76)

        fun paint(color: Int, size: Float, bold: Boolean = false, align: Paint.Align = Paint.Align.RIGHT) =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = color
                textSize = size
                textAlign = align
                typeface = Typeface.create("sans-serif", if (bold) Typeface.BOLD else Typeface.NORMAL)
            }
        val regular = paint(ink, 30f)
        val small = paint(muted, 24f)
        val bold = paint(ink, 28f, true)
        val rule = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = line; strokeWidth = 2f }
        val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.WHITE }
        val bgPaint = Paint().apply { color = android.graphics.Color.rgb(250, 251, 253) }

        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)
        canvas.drawRoundRect(RectF(margin, 48f, width - margin, headerHeight - 30f), 28f, 28f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = navy })
        canvas.drawRoundRect(RectF(margin + 34f, 82f, margin + 46f, 170f), 6f, 6f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = blue })
        canvas.drawText("فاکتور تراکنش‌ها", width - margin - 36f, 132f, paint(android.graphics.Color.WHITE, 52f, true))
        canvas.drawText("گزارش مالی و جزئیات تراکنش‌های انتخاب‌شده", width - margin - 36f, 184f,
            paint(0xFFD5DEEC.toInt(), 25f))
        val invoiceNo = (System.currentTimeMillis() % 1000000L).toString().padStart(6, '0').toPersianDigits()
        canvas.drawText("شماره فاکتور  $invoiceNo", width - margin - 36f, 260f,
            paint(android.graphics.Color.WHITE, 25f, true))
        canvas.drawText("تاریخ صدور  ${formatJalali(System.currentTimeMillis())}", margin + 36f, 260f,
            paint(0xFFD5DEEC.toInt(), 25f, false, Paint.Align.LEFT))

        val income = transactions.filter { it.isIncome }.sumOf { it.amount }
        val expense = transactions.filter { !it.isIncome }.sumOf { it.amount }
        val net = income - expense
        var y = headerHeight + 12f
        val tableLeft = margin
        val tableRight = width - margin
        canvas.drawRoundRect(RectF(tableLeft, y, tableRight, y + tableHeaderHeight), 18f, 18f, cardPaint)
        val colNo = tableRight - 36f
        val colTitle = tableRight - 130f
        val colCategory = tableRight - 500f
        val colDate = tableRight - 760f
        val colAmount = tableLeft + 36f
        val head = paint(muted, 24f, true)
        canvas.drawText("ردیف", colNo, y + 52f, head)
        canvas.drawText("شرح تراکنش", colTitle, y + 52f, head)
        canvas.drawText("دسته‌بندی", colCategory, y + 52f, head)
        canvas.drawText("تاریخ", colDate, y + 52f, head)
        canvas.drawText("مبلغ (تومان)", colAmount, y + 52f, paint(muted, 24f, true, Paint.Align.LEFT))
        y += tableHeaderHeight
        transactions.forEachIndexed { index, tx ->
            if (index % 2 == 0) canvas.drawRect(tableLeft, y, tableRight, y + rowHeight, cardPaint)
            else canvas.drawRect(tableLeft, y, tableRight, y + rowHeight, bgPaint)
            val rowPaint = paint(ink, 24f)
            canvas.drawText((index + 1).toString().toPersianDigits(), colNo, y + 48f, rowPaint)
            canvas.drawText(tx.title.take(28), colTitle, y + 48f, rowPaint)
            canvas.drawText(tx.category.take(18), colCategory, y + 48f, rowPaint)
            canvas.drawText(formatJalali(tx.dateMillis), colDate, y + 48f, rowPaint)
            canvas.drawText(tx.amount.toMoney(), colAmount, y + 48f,
                paint(if (tx.isIncome) green else red, 24f, true, Paint.Align.LEFT))
            canvas.drawLine(tableLeft, y + rowHeight, tableRight, y + rowHeight, rule)
            y += rowHeight
        }

        y += 28f
        val totalTop = y
        canvas.drawRoundRect(RectF(tableLeft, totalTop, tableRight, totalTop + totalsHeight - 24f), 22f, 22f, cardPaint)
        canvas.drawText("خلاصه مالی", tableRight - 32f, totalTop + 54f, paint(navy, 30f, true))
        canvas.drawLine(tableLeft + 30f, totalTop + 78f, tableRight - 30f, totalTop + 78f, rule)
        canvas.drawText("جمع درآمدها", tableRight - 34f, totalTop + 130f, regular)
        canvas.drawText(income.toMoney() + " تومان", tableLeft + 34f, totalTop + 130f,
            paint(green, 27f, true, Paint.Align.LEFT))
        canvas.drawText("جمع هزینه‌ها", tableRight - 34f, totalTop + 178f, regular)
        canvas.drawText(expense.toMoney() + " تومان", tableLeft + 34f, totalTop + 178f,
            paint(red, 27f, true, Paint.Align.LEFT))
        canvas.drawText("خالص", tableRight - 34f, totalTop + 226f, bold)
        canvas.drawText((if (net < 0) "−" else "") + kotlin.math.abs(net).toMoney() + " تومان",
            tableLeft + 34f, totalTop + 226f, paint(if (net >= 0) green else red, 28f, true, Paint.Align.LEFT))
        canvas.drawText("این فاکتور توسط اپلیکیشن Vsoft تولید شده است.", width / 2f, height - 42f,
            paint(muted, 21f, false, Paint.Align.CENTER))

        val directory = File(context.cacheDir, "invoices").apply { mkdirs() }
        val output = File(directory, "invoice-${System.currentTimeMillis()}.png")
        FileOutputStream(output).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
        return output
    }
}
