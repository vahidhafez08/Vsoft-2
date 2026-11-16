package com.vsoft.app.ui.screens.transactions

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import com.vsoft.app.data.model.TransactionEntity
import java.io.File
import java.io.FileOutputStream

object InvoiceImageGenerator {
    private const val MAX_ROWS = 90
    fun create(context: Context, transactions: List<TransactionEntity>): File {
        require(transactions.isNotEmpty()) { "هیچ تراکنشی انتخاب نشده است" }
        val shown = minOf(transactions.size, MAX_ROWS)
        val truncated = transactions.size - shown
        val meta = InvoiceRenderer.meta(transactions)
        val heightF = InvoiceRenderer.HEADER_H + shown * InvoiceRenderer.ROW_H +
            (if (truncated > 0) InvoiceRenderer.ROW_H else 0f) + InvoiceRenderer.SUMMARY_H + InvoiceRenderer.FOOTER_H
        val bitmap = Bitmap.createBitmap(InvoiceRenderer.W.toInt(), heightF.toInt(), Bitmap.Config.ARGB_8888)
        try {
            InvoiceRenderer.drawPage(Canvas(bitmap), transactions, 0 until shown, 1, 1, meta, heightF, truncated)
            val directory = File(context.cacheDir, "invoices").apply { mkdirs() }
            val output = File(directory, "invoice-${System.currentTimeMillis()}.png")
            FileOutputStream(output).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            return output
        } finally { bitmap.recycle() }
    }
}
