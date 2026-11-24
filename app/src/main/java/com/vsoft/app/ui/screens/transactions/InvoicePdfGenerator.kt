package com.vsoft.app.ui.screens.transactions

import android.content.Context
import android.graphics.pdf.PdfDocument
import com.vsoft.app.data.model.TransactionEntity
import java.io.File
import java.io.FileOutputStream

object InvoicePdfGenerator {
    fun create(context: Context, transactions: List<TransactionEntity>, darkTheme: Boolean = false): File {
        require(transactions.isNotEmpty()) { "هیچ تراکنشی انتخاب نشده است" }
        val pageWidth = 595
        val pageHeight = 842
        val scale = pageWidth / InvoiceRenderer.W
        val logicalHeight = pageHeight / scale
        val meta = InvoiceRenderer.meta(transactions)
        val colors = InvoiceRenderer.palette(darkTheme)
        val ranges = InvoiceRenderer.paginate(context, transactions, logicalHeight)
        val document = PdfDocument()
        try {
            ranges.forEachIndexed { index, range ->
                val page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, index + 1).create())
                page.canvas.scale(scale, scale)
                InvoiceRenderer.drawPage(context, page.canvas, transactions, range, index + 1, ranges.size,
                    meta, logicalHeight, colors)
                document.finishPage(page)
            }
            val dir = File(context.cacheDir, "invoices").apply { mkdirs() }
            val output = File(dir, "invoice-" + System.currentTimeMillis() + ".pdf")
            FileOutputStream(output).use { document.writeTo(it) }
            return output
        } finally {
            document.close()
        }
    }
}
