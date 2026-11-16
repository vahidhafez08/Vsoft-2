package com.vsoft.app.ui.screens.transactions

import android.content.Context
import android.graphics.pdf.PdfDocument
import com.vsoft.app.data.model.TransactionEntity
import java.io.File
import java.io.FileOutputStream

object InvoicePdfGenerator {
    fun create(context: Context, transactions: List<TransactionEntity>): File {
        require(transactions.isNotEmpty()) { "هیچ تراکنشی انتخاب نشده است" }
        val pageW = 595; val pageH = 842
        val scale = pageW / InvoiceRenderer.W
        val unitsH = pageH / scale
        val pages = InvoiceRenderer.paginate(transactions.size, unitsH)
        val meta = InvoiceRenderer.meta(transactions)
        val document = PdfDocument()
        try {
            pages.forEachIndexed { i, range ->
                val page = document.startPage(PdfDocument.PageInfo.Builder(pageW, pageH, i + 1).create())
                page.canvas.scale(scale, scale)
                InvoiceRenderer.drawPage(page.canvas, transactions, range, i + 1, pages.size, meta, unitsH)
                document.finishPage(page)
            }
            val directory = File(context.cacheDir, "invoices").apply { mkdirs() }
            val output = File(directory, "invoice-${System.currentTimeMillis()}.pdf")
            FileOutputStream(output).use { document.writeTo(it) }
            return output
        } finally { document.close() }
    }
}
