package com.vsoft.app.ui.screens.transactions

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import com.vsoft.app.data.model.TransactionEntity
import java.io.File
import java.io.FileOutputStream

object InvoiceImageGenerator {
    fun create(context: Context, transactions: List<TransactionEntity>, darkTheme: Boolean = false): File {
        require(transactions.isNotEmpty()) { "هیچ تراکنشی انتخاب نشده است" }
        val meta = InvoiceRenderer.meta(transactions)
        val height = InvoiceRenderer.imageHeight(context, transactions)
        val bitmap = Bitmap.createBitmap(InvoiceRenderer.W.toInt(), height, Bitmap.Config.ARGB_8888)
        try {
            InvoiceRenderer.drawPage(context, Canvas(bitmap), transactions, 0 until transactions.size,
                1, 1, meta, height.toFloat(), InvoiceRenderer.palette(darkTheme))
            val dir = File(context.cacheDir, "invoices").apply { mkdirs() }
            val output = File(dir, "invoice-" + System.currentTimeMillis() + ".png")
            FileOutputStream(output).use { stream ->
                check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream))
            }
            return output
        } finally {
            bitmap.recycle()
        }
    }
}
