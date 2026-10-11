package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.Product
import com.example.data.SaleInvoice
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * مدير إنشاء وتصدير ومشاركة تقارير المخزون والمبيعات بتنسيق PDF
 */
object PdfReportManager {

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 36f

    /**
     * إنشاء تقرير المخزون بصيغة PDF وتخزينه في مسار الكاش
     */
    fun generateInventoryPdf(
        context: Context,
        pharmacyName: String,
        doctorName: String,
        products: List<Product>,
        totalValue: Double,
        lowStockCount: Int,
        expiredCount: Int
    ): File {
        val document = PdfDocument()
        val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
        val dateString = dateFormat.format(Date())

        val itemsPerPage = 20
        val totalPages = if (products.isEmpty()) 1 else (products.size + itemsPerPage - 1) / itemsPerPage

        var currentPageNumber = 1
        var itemIndex = 0

        while (currentPageNumber <= totalPages) {
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, currentPageNumber).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            val paint = Paint().apply { isAntiAlias = true }

            // 1. Header Background (Teal Banner)
            paint.color = Color.parseColor("#003B40")
            canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 110f, paint)

            // Header Bottom Accent Line
            paint.color = Color.parseColor("#12CFC5")
            canvas.drawRect(0f, 108f, PAGE_WIDTH.toFloat(), 112f, paint)

            // 2. Pharmacy and Doctor Header Text
            paint.color = Color.WHITE
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 20f
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(pharmacyName, PAGE_WIDTH - MARGIN, 42f, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 12f
            paint.color = Color.parseColor("#B9F0EA")
            canvas.drawText("إشراف الصيدلي: د. $doctorName", PAGE_WIDTH - MARGIN, 65f, paint)

            paint.textSize = 10f
            paint.color = Color.parseColor("#9BC9C7")
            canvas.drawText("تاريخ التقرير: $dateString", PAGE_WIDTH - MARGIN, 85f, paint)

            // Left Header Badge
            paint.color = Color.WHITE
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 16f
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText("تقرير المخزون والأصناف", MARGIN, 48f, paint)

            paint.textSize = 10f
            paint.color = Color.parseColor("#12CFC5")
            canvas.drawText("نظام صيدلي الذكي (SAYDALI OS)", MARGIN, 70f, paint)

            var currentY = 135f

            // 3. Stats Summary Boxes (Only on First Page)
            if (currentPageNumber == 1) {
                val boxWidth = (PAGE_WIDTH - (MARGIN * 2) - 30f) / 4f
                val boxHeight = 46f

                val stats = listOf(
                    Triple("إجمالي الأصناف", "${products.size}", "#003B40"),
                    Triple("قيمة المخزون", String.format(Locale.US, "%.0f ر.س", totalValue), "#004B50"),
                    Triple("مخزون منخفض", "$lowStockCount", "#A67C00"),
                    Triple("منتهي الصلاحية", "$expiredCount", "#9E2A2B")
                )

                var boxX = MARGIN
                stats.forEach { (title, value, colorHex) ->
                    paint.color = Color.parseColor(colorHex)
                    val rect = RectF(boxX, currentY, boxX + boxWidth, currentY + boxHeight)
                    canvas.drawRoundRect(rect, 8f, 8f, paint)

                    // Text inside box
                    paint.color = Color.WHITE
                    paint.textAlign = Paint.Align.CENTER
                    paint.textSize = 9f
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    canvas.drawText(title, boxX + (boxWidth / 2f), currentY + 16f, paint)

                    paint.textSize = 13f
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    canvas.drawText(value, boxX + (boxWidth / 2f), currentY + 36f, paint)

                    boxX += boxWidth + 10f
                }

                currentY += boxHeight + 20f
            }

            // 4. Table Header
            val tableTop = currentY
            val tableHeight = 24f
            paint.color = Color.parseColor("#004B50")
            canvas.drawRoundRect(RectF(MARGIN, tableTop, PAGE_WIDTH - MARGIN, tableTop + tableHeight), 6f, 6f, paint)

            paint.color = Color.parseColor("#B9F0EA")
            paint.textSize = 10f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

            // Column Positions
            val colStatus = MARGIN + 40f
            val colTotal = MARGIN + 120f
            val colPrice = MARGIN + 190f
            val colQty = MARGIN + 260f
            val colUnit = MARGIN + 330f
            val colName = PAGE_WIDTH - MARGIN - 10f

            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("الحالة", colStatus, tableTop + 16f, paint)
            canvas.drawText("الإجمالي", colTotal, tableTop + 16f, paint)
            canvas.drawText("السعر", colPrice, tableTop + 16f, paint)
            canvas.drawText("الكمية", colQty, tableTop + 16f, paint)
            canvas.drawText("الوحدة", colUnit, tableTop + 16f, paint)

            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText("اسم الصنف والشركة", colName, tableTop + 16f, paint)

            currentY += tableHeight + 6f

            // 5. Table Rows
            val pageEndIndex = minOf(itemIndex + itemsPerPage, products.size)
            for (i in itemIndex until pageEndIndex) {
                val product = products[i]
                val rowHeight = 26f

                // Alternating row background
                if (i % 2 == 0) {
                    paint.color = Color.parseColor("#F4FBFB")
                    canvas.drawRect(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + rowHeight, paint)
                }

                // Row border bottom
                paint.color = Color.parseColor("#E0EAE9")
                paint.strokeWidth = 0.5f
                canvas.drawLine(MARGIN, currentY + rowHeight, PAGE_WIDTH - MARGIN, currentY + rowHeight, paint)

                // Item Name & Manufacturer
                paint.color = Color.parseColor("#002E33")
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.textSize = 10f
                paint.textAlign = Paint.Align.RIGHT
                val nameText = if (product.name.length > 25) product.name.take(25) + "..." else product.name
                canvas.drawText(nameText, colName, currentY + 16f, paint)

                // Unit
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.textSize = 9f
                paint.textAlign = Paint.Align.CENTER
                paint.color = Color.parseColor("#556B6D")
                canvas.drawText(product.unit, colUnit, currentY + 16f, paint)

                // Quantity
                paint.color = Color.parseColor("#002E33")
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText("${product.quantity}", colQty, currentY + 16f, paint)

                // Unit Price
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.color = Color.parseColor("#333333")
                canvas.drawText(String.format(Locale.US, "%.1f", product.unitPrice), colPrice, currentY + 16f, paint)

                // Total Row Value
                val rowTotal = product.quantity * product.unitPrice
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.color = Color.parseColor("#008C84")
                canvas.drawText(String.format(Locale.US, "%.1f", rowTotal), colTotal, currentY + 16f, paint)

                // Status
                val statusText = when {
                    product.quantity == 0 -> "نفد"
                    product.quantity <= product.minStockLevel -> "منخفض"
                    else -> "متوفر"
                }
                val statusColor = when {
                    product.quantity == 0 -> Color.parseColor("#E65F62")
                    product.quantity <= product.minStockLevel -> Color.parseColor("#D49B00")
                    else -> Color.parseColor("#2E9F75")
                }
                paint.color = statusColor
                paint.textSize = 9f
                canvas.drawText(statusText, colStatus, currentY + 16f, paint)

                currentY += rowHeight
            }
            itemIndex = pageEndIndex

            // 6. Footer (Page info and official stamp note)
            val footerY = PAGE_HEIGHT - 30f
            paint.color = Color.parseColor("#999999")
            paint.strokeWidth = 0.5f
            canvas.drawLine(MARGIN, footerY - 10f, PAGE_WIDTH - MARGIN, footerY - 10f, paint)

            paint.textSize = 9f
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText("صادر إلكترونياً من صيدلية: $pharmacyName", PAGE_WIDTH - MARGIN, footerY + 4f, paint)

            paint.textAlign = Paint.Align.LEFT
            canvas.drawText("صفحة $currentPageNumber من $totalPages", MARGIN, footerY + 4f, paint)

            document.finishPage(page)
            currentPageNumber++
        }

        // Save PDF to cache directory
        val reportsDir = File(context.cacheDir, "reports").apply { mkdirs() }
        val pdfFile = File(reportsDir, "تقرير_المخزون_${System.currentTimeMillis()}.pdf")
        val outputStream = FileOutputStream(pdfFile)
        document.writeTo(outputStream)
        outputStream.close()
        document.close()

        return pdfFile
    }

    /**
     * إنشاء تقرير المبيعات والفواتير بصيغة PDF
     */
    fun generateSalesPdf(
        context: Context,
        pharmacyName: String,
        doctorName: String,
        invoices: List<SaleInvoice>,
        totalRevenue: Double
    ): File {
        val document = PdfDocument()
        val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
        val dateString = dateFormat.format(Date())

        val itemsPerPage = 22
        val totalPages = if (invoices.isEmpty()) 1 else (invoices.size + itemsPerPage - 1) / itemsPerPage

        var currentPageNumber = 1
        var invoiceIndex = 0

        while (currentPageNumber <= totalPages) {
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, currentPageNumber).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            val paint = Paint().apply { isAntiAlias = true }

            // 1. Header Banner
            paint.color = Color.parseColor("#003B40")
            canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 110f, paint)

            paint.color = Color.parseColor("#12CFC5")
            canvas.drawRect(0f, 108f, PAGE_WIDTH.toFloat(), 112f, paint)

            // Header Texts
            paint.color = Color.WHITE
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 20f
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(pharmacyName, PAGE_WIDTH - MARGIN, 42f, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 12f
            paint.color = Color.parseColor("#B9F0EA")
            canvas.drawText("إشراف: د. $doctorName", PAGE_WIDTH - MARGIN, 65f, paint)

            paint.textSize = 10f
            paint.color = Color.parseColor("#9BC9C7")
            canvas.drawText("تاريخ التقرير: $dateString", PAGE_WIDTH - MARGIN, 85f, paint)

            // Left Header Badge
            paint.color = Color.WHITE
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 16f
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText("تقرير المبيعات والفواتير", MARGIN, 48f, paint)

            paint.textSize = 10f
            paint.color = Color.parseColor("#12CFC5")
            canvas.drawText("فواتير معتمدة ومطابقة لـ ZATCA", MARGIN, 70f, paint)

            var currentY = 135f

            // 2. Stats Boxes (First Page)
            if (currentPageNumber == 1) {
                val boxWidth = (PAGE_WIDTH - (MARGIN * 2) - 20f) / 3f
                val boxHeight = 46f

                val cashSales = invoices.filter { it.paymentMethod.contains("نقد") }.sumOf { it.totalAmount }
                val cardSales = invoices.filter { !it.paymentMethod.contains("نقد") }.sumOf { it.totalAmount }

                val stats = listOf(
                    Triple("إجمالي الإيرادات", String.format(Locale.US, "%.2f ر.س", totalRevenue), "#004B50"),
                    Triple("دفع نقدي (كاش)", String.format(Locale.US, "%.2f ر.س", cashSales), "#003B40"),
                    Triple("مدى وشبكة", String.format(Locale.US, "%.2f ر.س", cardSales), "#00565B")
                )

                var boxX = MARGIN
                stats.forEach { (title, value, colorHex) ->
                    paint.color = Color.parseColor(colorHex)
                    val rect = RectF(boxX, currentY, boxX + boxWidth, currentY + boxHeight)
                    canvas.drawRoundRect(rect, 8f, 8f, paint)

                    paint.color = Color.WHITE
                    paint.textAlign = Paint.Align.CENTER
                    paint.textSize = 9f
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    canvas.drawText(title, boxX + (boxWidth / 2f), currentY + 16f, paint)

                    paint.textSize = 13f
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    canvas.drawText(value, boxX + (boxWidth / 2f), currentY + 36f, paint)

                    boxX += boxWidth + 10f
                }

                currentY += boxHeight + 20f
            }

            // 3. Table Header
            val tableTop = currentY
            val tableHeight = 24f
            paint.color = Color.parseColor("#004B50")
            canvas.drawRoundRect(RectF(MARGIN, tableTop, PAGE_WIDTH - MARGIN, tableTop + tableHeight), 6f, 6f, paint)

            paint.color = Color.parseColor("#B9F0EA")
            paint.textSize = 10f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

            val colAmount = MARGIN + 50f
            val colPayment = MARGIN + 140f
            val colCount = MARGIN + 220f
            val colDate = MARGIN + 320f
            val colInvoiceNo = PAGE_WIDTH - MARGIN - 10f

            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("المبلغ الإجمالي", colAmount, tableTop + 16f, paint)
            canvas.drawText("طريقة الدفع", colPayment, tableTop + 16f, paint)
            canvas.drawText("الأصناف", colCount, tableTop + 16f, paint)
            canvas.drawText("التاريخ والوقت", colDate, tableTop + 16f, paint)

            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText("رقم الفاتورة", colInvoiceNo, tableTop + 16f, paint)

            currentY += tableHeight + 6f

            // 4. Invoices Rows
            val pageEndIndex = minOf(invoiceIndex + itemsPerPage, invoices.size)
            for (i in invoiceIndex until pageEndIndex) {
                val inv = invoices[i]
                val rowHeight = 25f

                if (i % 2 == 0) {
                    paint.color = Color.parseColor("#F4FBFB")
                    canvas.drawRect(MARGIN, currentY, PAGE_WIDTH - MARGIN, currentY + rowHeight, paint)
                }

                paint.color = Color.parseColor("#E0EAE9")
                paint.strokeWidth = 0.5f
                canvas.drawLine(MARGIN, currentY + rowHeight, PAGE_WIDTH - MARGIN, currentY + rowHeight, paint)

                // Invoice Number
                paint.color = Color.parseColor("#002E33")
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.textSize = 10f
                paint.textAlign = Paint.Align.RIGHT
                canvas.drawText(inv.invoiceNumber, colInvoiceNo, currentY + 16f, paint)

                // Date
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                paint.textSize = 9f
                paint.textAlign = Paint.Align.CENTER
                paint.color = Color.parseColor("#556B6D")
                val invDateStr = SimpleDateFormat("MM/dd HH:mm", Locale.US).format(Date(inv.timestamp))
                canvas.drawText(invDateStr, colDate, currentY + 16f, paint)

                // Item count
                paint.color = Color.parseColor("#333333")
                canvas.drawText("${inv.itemCount}", colCount, currentY + 16f, paint)

                // Payment method
                paint.color = Color.parseColor("#004B50")
                canvas.drawText(inv.paymentMethod, colPayment, currentY + 16f, paint)

                // Amount
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.color = Color.parseColor("#008C84")
                canvas.drawText(String.format(Locale.US, "%.2f ر.س", inv.totalAmount), colAmount, currentY + 16f, paint)

                currentY += rowHeight
            }
            invoiceIndex = pageEndIndex

            // Footer
            val footerY = PAGE_HEIGHT - 30f
            paint.color = Color.parseColor("#999999")
            paint.strokeWidth = 0.5f
            canvas.drawLine(MARGIN, footerY - 10f, PAGE_WIDTH - MARGIN, footerY - 10f, paint)

            paint.textSize = 9f
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText("صادر إلكترونياً من صيدلية: $pharmacyName", PAGE_WIDTH - MARGIN, footerY + 4f, paint)

            paint.textAlign = Paint.Align.LEFT
            canvas.drawText("صفحة $currentPageNumber من $totalPages", MARGIN, footerY + 4f, paint)

            document.finishPage(page)
            currentPageNumber++
        }

        // Save PDF to cache directory
        val reportsDir = File(context.cacheDir, "reports").apply { mkdirs() }
        val pdfFile = File(reportsDir, "تقرير_المبيعات_${System.currentTimeMillis()}.pdf")
        val outputStream = FileOutputStream(pdfFile)
        document.writeTo(outputStream)
        outputStream.close()
        document.close()

        return pdfFile
    }

    /**
     * مشاركة ملف الـ PDF مباشرة مع التطبيقات الخارجية (واتساب، تيليجرام، البريد، الطباعة...)
     */
    fun sharePdf(context: Context, pdfFile: File, reportTitle: String) {
        try {
            val authority = "${context.packageName}.fileprovider"
            val fileUri = FileProvider.getUriForFile(context, authority, pdfFile)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, fileUri)
                putExtra(Intent.EXTRA_SUBJECT, reportTitle)
                putExtra(Intent.EXTRA_TEXT, "مرفق $reportTitle بصيغة PDF من نظام صيدلي.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "مشاركة تقرير PDF عبر")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
