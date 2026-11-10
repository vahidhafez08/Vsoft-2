package com.vsoft.app.ui.screens.transactions

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
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
import java.io.File
import java.io.FileOutputStream

object InvoiceImageGenerator {
    fun create(context: Context, transactions: List<TransactionEntity>): File {
        require(transactions.isNotEmpty()) { "هیچ تراکنشی انتخاب نشده است" }

        val width = 1200
        val margin = 72f
        val headerHeight = 330f
        val tableHeaderHeight = 88f
        val rowHeight = 94f
        val totalsHeight = 260f
        val footerHeight = 100f
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

        fun paint(color: Int, size: Float, bold: Boolean = false) = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            textSize = size
            typeface = Typeface.create("sans-serif", if (bold) Typeface.BOLD else Typeface.NORMAL)
        }

        fun drawText(
            text: String,
            left: Float,
            top: Float,
            maxWidth: Int,
            textPaint: TextPaint,
            rtl: Boolean = true,
            alignment: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL,
            maxLines: Int = 1
        ) {
            if (text.isBlank() || maxWidth <= 0) return
            val layout = StaticLayout.Builder.obtain(text, 0, text.length, textPaint, maxWidth)
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

        val regular = paint(ink, 28f)
        val small = paint(muted, 23f)
        val bold = paint(ink, 29f, true)
        val rule = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = line; strokeWidth = 2f }
        val white = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.WHITE }
        val bgPaint = Paint().apply { color = android.graphics.Color.rgb(250, 251, 253) }

        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)
        canvas.drawRoundRect(
            RectF(margin, 42f, width - margin, headerHeight - 24f),
            28f, 28f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = navy }
        )
        canvas.drawRoundRect(
            RectF(width - margin - 48f, 76f, width - margin - 34f, 166f),
            7f, 7f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = blue }
        )
        drawText("فاکتور تراکنش‌ها", margin + 36f, 86f, 820, paint(android.graphics.Color.WHITE, 54f, true))
        drawText(
            "گزارش مالی و جزئیات تراکنش‌های انتخاب‌شده",
            margin + 36f, 158f, 820, paint(0xFFD5DEEC.toInt(), 26f)
        )
        val invoiceNo = (System.currentTimeMillis() % 1000000L).toString().padStart(6, '0').toPersianDigits()
        drawText("شماره فاکتور: $invoiceNo", margin + 36f, 235f, 430, paint(android.graphics.Color.WHITE, 25f, true))
        drawText(
            "تاریخ صدور: ${formatJalali(System.currentTimeMillis())}",
            width - margin - 500f, 235f, 460, paint(0xFFD5DEEC.toInt(), 25f)
        )

        val income = transactions.filter { it.isIncome }.sumOf { it.amount }
        val expense = transactions.filter { !it.isIncome }.sumOf { it.amount }
        val net = income - expense
        var y = headerHeight + 8f
        val tableLeft = margin
        val tableRight = width - margin
        canvas.drawRoundRect(RectF(tableLeft, y, tableRight, y + tableHeaderHeight), 18f, 18f, white)

        val indexLeft = tableRight - 94f
        val titleLeft = tableRight - 520f
        val dateLeft = tableRight - 730f
        val amountLeft = tableLeft + 28f
        drawText("ردیف", indexLeft, y + 30f, 66, small)
        drawText("شرح تراکنش", titleLeft, y + 30f, 390, small)
        drawText("تاریخ", dateLeft, y + 30f, 180, small)
        drawText("مبلغ (تومان)", amountLeft, y + 30f, 270, small, rtl = false, alignment = Layout.Alignment.ALIGN_NORMAL)
        y += tableHeaderHeight

        transactions.forEachIndexed { index, tx ->
            if (index % 2 == 0) canvas.drawRect(tableLeft, y, tableRight, y + rowHeight, white)
            else canvas.drawRect(tableLeft, y, tableRight, y + rowHeight, Paint().apply { color = pale })

            drawText((index + 1).toString().toPersianDigits(), indexLeft, y + 34f, 66, paint(ink, 23f))
            drawText(tx.title.ifBlank { "بدون عنوان" }, titleLeft, y + 20f, 390, paint(ink, 25f, true))
            drawText(tx.category, titleLeft, y + 54f, 390, paint(muted, 20f))
            drawText(formatJalali(tx.dateMillis), dateLeft, y + 36f, 180, paint(ink, 22f))
            drawText(
                tx.amount.toMoney(),
                amountLeft, y + 35f, 270,
                paint(if (tx.isIncome) green else red, 24f, true),
                rtl = false,
                alignment = Layout.Alignment.ALIGN_NORMAL
            )
            canvas.drawLine(tableLeft, y + rowHeight, tableRight, y + rowHeight, rule)
            y += rowHeight
        }

        y += 28f
        val totalTop = y
        canvas.drawRoundRect(
            RectF(tableLeft, totalTop, tableRight, totalTop + totalsHeight - 24f),
            22f, 22f, white
        )
        drawText("خلاصه مالی", tableRight - 330f, totalTop + 28f, 290, paint(navy, 30f, true))
        canvas.drawLine(tableLeft + 30f, totalTop + 78f, tableRight - 30f, totalTop + 78f, rule)

        drawText("جمع درآمدها", tableRight - 340f, totalTop + 102f, 300, regular)
        drawText(income.toMoney() + " تومان", tableLeft + 34f, totalTop + 102f, 330, paint(green, 26f, true), rtl = false)
        drawText("جمع هزینه‌ها", tableRight - 340f, totalTop + 154f, 300, regular)
        drawText(expense.toMoney() + " تومان", tableLeft + 34f, totalTop + 154f, 330, paint(red, 26f, true), rtl = false)
        drawText("خالص", tableRight - 340f, totalTop + 206f, 300, bold)
        drawText(
            (if (net < 0) "−" else "") + kotlin.math.abs(net).toMoney() + " تومان",
            tableLeft + 34f, totalTop + 206f, 330,
            paint(if (net >= 0) green else red, 27f, true),
            rtl = false
        )

        drawText(
            "این فاکتور توسط اپلیکیشن Vsoft تولید شده است.",
            margin, height - 58f, (width - margin * 2).toInt(),
            paint(muted, 21f),
            alignment = Layout.Alignment.ALIGN_CENTER
        )

        val directory = File(context.cacheDir, "invoices").apply { mkdirs() }
        val output = File(directory, "invoice-${System.currentTimeMillis()}.png")
        FileOutputStream(output).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
        return output
    }
}
