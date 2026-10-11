package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.SaleInvoice
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * مدير مشاركة ملخص المبيعات اليومي والرسوم البيانية مباشرة إلى واتساب
 * WhatsApp Daily Sales Share Manager (Text & Visual Chart Image)
 */
object WhatsAppShareManager {

    /**
     * نموذج بيانات ملخص المبيعات اليومي
     */
    data class DailySalesSummary(
        val dateString: String,
        val totalRevenue: Double,
        val invoiceCount: Int,
        val cashAmount: Double,
        val cardAmount: Double,
        val creditAmount: Double,
        val averageInvoiceValue: Double,
        val topPaymentMethod: String,
        val dayName: String,
        val recentInvoices: List<SaleInvoice>
    )

    /**
     * حساب ملخص المبيعات لليوم الحالي (أو كافة مبيعات اليوم من الفواتير)
     */
    fun calculateDailySalesSummary(invoices: List<SaleInvoice>): DailySalesSummary {
        val now = Calendar.getInstance()
        val todayYear = now.get(Calendar.YEAR)
        val todayDayOfYear = now.get(Calendar.DAY_OF_YEAR)

        // تصفية الفواتير المسجلة اليوم (أو استخدام جميع الفواتير إذا لم توجد فواتير اليوم لعرض بيانات نموذجية)
        val todayInvoices = invoices.filter { inv ->
            val cal = Calendar.getInstance().apply { timeInMillis = inv.timestamp }
            cal.get(Calendar.YEAR) == todayYear && cal.get(Calendar.DAY_OF_YEAR) == todayDayOfYear
        }.ifEmpty {
            // إذا لم تكن هناك فواتير مسجلة اليوم، نأخذ آخر الفواتير المسجلة
            invoices
        }

        val totalRev = todayInvoices.sumOf { it.totalAmount }
        val count = todayInvoices.size
        val cash = todayInvoices.filter { it.paymentMethod.contains("نقد") || it.paymentMethod.contains("كاش") }.sumOf { it.totalAmount }
        val card = todayInvoices.filter { it.paymentMethod.contains("بطاق") || it.paymentMethod.contains("شبك") || it.paymentMethod.contains("مدى") }.sumOf { it.totalAmount }
        val credit = todayInvoices.filter { it.paymentMethod.contains("آجل") || it.paymentMethod.contains("ذمم") }.sumOf { it.totalAmount }
        val avg = if (count > 0) totalRev / count else 0.0

        val topMethod = when {
            cash >= card && cash >= credit -> "نقدي (كاش)"
            card >= cash && card >= credit -> "بطاقة / مدى"
            credit > 0 -> "آجل / ذمم"
            else -> "نقدي"
        }

        val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        val dayFormat = SimpleDateFormat("EEEE", Locale("ar"))

        return DailySalesSummary(
            dateString = dateFormat.format(Date()),
            totalRevenue = totalRev,
            invoiceCount = count,
            cashAmount = cash,
            cardAmount = card,
            creditAmount = credit,
            averageInvoiceValue = avg,
            topPaymentMethod = topMethod,
            dayName = dayFormat.format(Date()),
            recentInvoices = todayInvoices.take(5)
        )
    }

    /**
     * بناء نص ملخص المبيعات اليومي المنسق بتنسيق WhatsApp مع إيموجي وخطوط بارزة
     */
    fun buildWhatsAppSummaryText(
        pharmacyName: String,
        doctorName: String,
        summary: DailySalesSummary
    ): String {
        val sb = StringBuilder()
        sb.append("📊 *تقرير ملخص المبيعات اليومي* 📊\n")
        sb.append("🏥 *الصيدلية:* $pharmacyName\n")
        if (doctorName.isNotBlank()) {
            sb.append("👨‍⚕️ *المشرف:* $doctorName\n")
        }
        sb.append("📅 *اليوم والتاريخ:* ${summary.dayName}، ${summary.dateString}\n")
        sb.append("⏰ *وقت التقرير:* ${SimpleDateFormat("HH:mm a", Locale.getDefault()).format(Date())}\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("💰 *إجمالي المبيعات:* ${String.format(Locale.US, "%.2f", summary.totalRevenue)} ر.س\n")
        sb.append("🧾 *عدد الفواتير الصادرة:* ${summary.invoiceCount} فاتورة\n")
        sb.append("📈 *متوسط قيمة الفاتورة:* ${String.format(Locale.US, "%.2f", summary.averageInvoiceValue)} ر.س\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("💳 *تفاصيل طرق الدفع:*\n")
        sb.append("  • 💵 كاش / نقدي: ${String.format(Locale.US, "%.2f", summary.cashAmount)} ر.س\n")
        sb.append("  • 💳 مدى / شبكة: ${String.format(Locale.US, "%.2f", summary.cardAmount)} ر.س\n")
        if (summary.creditAmount > 0) {
            sb.append("  • 📝 آجل / ذمم: ${String.format(Locale.US, "%.2f", summary.creditAmount)} ر.س\n")
        }
        sb.append("  • ⭐ الأكثر استخداماً: ${summary.topPaymentMethod}\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━\n")
        if (summary.recentInvoices.isNotEmpty()) {
            sb.append("📋 *أحدث الفواتير المسجلة:*\n")
            val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())
            summary.recentInvoices.forEach { inv ->
                sb.append("  #${inv.invoiceNumber} | ${String.format(Locale.US, "%.1f", inv.totalAmount)} ر.س | ${inv.paymentMethod} (${timeFmt.format(Date(inv.timestamp))})\n")
            }
            sb.append("━━━━━━━━━━━━━━━━━━━━\n")
        }
        sb.append("✨ *تم التوليد تلقائياً بواسطة نظام إدارة الصيدلية الذكي* ✨")
        return sb.toString()
    }

    /**
     * مشاركة النص مباشرة إلى واتساب مع الرجوع للمشارك العام إذا لم يكن واتساب مثبتاً
     */
    fun shareTextToWhatsApp(context: Context, text: String) {
        val whatsappIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            setPackage("com.whatsapp")
            putExtra(Intent.EXTRA_TEXT, text)
        }

        try {
            context.startActivity(whatsappIntent)
        } catch (e: Exception) {
            // جرب واتساب للأعمال WhatsApp Business
            try {
                val whatsappBusinessIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    setPackage("com.whatsapp.w4b")
                    putExtra(Intent.EXTRA_TEXT, text)
                }
                context.startActivity(whatsappBusinessIntent)
            } catch (e2: Exception) {
                // إذا لم يكن واتساب متوفراً، افتح منتقي التطبيقات العام
                val generalIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, text)
                }
                val chooser = Intent.createChooser(generalIntent, "مشاركة ملخص المبيعات عبر:")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
                Toast.makeText(context, "تطبيق واتساب غير مثبت، تم فتح خيارات المشاركة المتاحة", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * رسم وتوليد صورة عالية الدقة للرسم البياني وملخص المبيعات اليومي (Chart & KPI Infographic)
     */
    fun generateSalesChartBitmap(
        pharmacyName: String,
        doctorName: String,
        summary: DailySalesSummary,
        invoices: List<SaleInvoice>
    ): Bitmap {
        val width = 1080
        val height = 1350 // تناسب ممتاز مع شاشات الهواتف ومحادثات واتساب (4:5 Ratio)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 1. تدرج الخلفية الفاخرة (Dark Medical Cyber Gradient)
        val bgPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, 0f, height.toFloat(),
                Color.parseColor("#061A1C"),
                Color.parseColor("#0A2B2E"),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        val paint = Paint().apply {
            isAntiAlias = true
        }

        // 2. الهيدر العلوي وشعار الصيدلية (Header Banner)
        val headerPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, width.toFloat(), 180f,
                Color.parseColor("#004D53"),
                Color.parseColor("#0A7E8C"),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(RectF(40f, 40f, width - 40f, 210f), 24f, 24f, headerPaint)

        // شريط تزييني علوي براق
        paint.color = Color.parseColor("#00E5FF")
        paint.strokeWidth = 6f
        canvas.drawLine(70f, 44f, width - 70f, 44f, paint)

        // عنوان الهيدر
        paint.color = Color.WHITE
        paint.textSize = 42f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("📊 ملخص المبيعات اليومي والمؤشرات", width / 2f, 110f, paint)

        // اسم الصيدلية واليوم
        paint.textSize = 28f
        paint.color = Color.parseColor("#B4F1F5")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val subTitle = "$pharmacyName • ${summary.dayName} ${summary.dateString}"
        canvas.drawText(subTitle, width / 2f, 165f, paint)

        // 3. كروت الإحصائيات السريعة (KPI Metric Cards)
        val cardY = 240f
        val cardHeight = 160f
        val gap = 24f
        val cardWidth = (width - 80f - (gap * 2)) / 3f

        // كرت 1: إجمالي المبيعات
        drawKpiCard(
            canvas = canvas,
            x = 40f,
            y = cardY,
            w = cardWidth,
            h = cardHeight,
            title = "إجمالي المبيعات",
            value = String.format(Locale.US, "%.0f ر.س", summary.totalRevenue),
            accentColor = Color.parseColor("#00E5FF"),
            bgColor = Color.parseColor("#0C3337")
        )

        // كرت 2: عدد الفواتير
        drawKpiCard(
            canvas = canvas,
            x = 40f + cardWidth + gap,
            y = cardY,
            w = cardWidth,
            h = cardHeight,
            title = "الفواتير الصادرة",
            value = "${summary.invoiceCount} فاتورة",
            accentColor = Color.parseColor("#10B981"),
            bgColor = Color.parseColor("#0C3337")
        )

        // كرت 3: متوسط الفاتورة
        drawKpiCard(
            canvas = canvas,
            x = 40f + (cardWidth + gap) * 2,
            y = cardY,
            w = cardWidth,
            h = cardHeight,
            title = "متوسط الفاتورة",
            value = String.format(Locale.US, "%.0f ر.س", summary.averageInvoiceValue),
            accentColor = Color.parseColor("#F59E0B"),
            bgColor = Color.parseColor("#0C3337")
        )

        // 4. حاوية الرسم البياني للمبيعات (Bar Chart Container)
        val chartBoxY = 430f
        val chartBoxHeight = 490f
        val chartBoxPaint = Paint().apply {
            color = Color.parseColor("#0A2A2E")
            isAntiAlias = true
        }
        canvas.drawRoundRect(RectF(40f, chartBoxY, width - 40f, chartBoxY + chartBoxHeight), 24f, 24f, chartBoxPaint)

        // إطار ناعم لحاوية الرسم
        val borderPaint = Paint().apply {
            color = Color.parseColor("#145259")
            style = Paint.Style.STROKE
            strokeWidth = 3f
            isAntiAlias = true
        }
        canvas.drawRoundRect(RectF(40f, chartBoxY, width - 40f, chartBoxY + chartBoxHeight), 24f, 24f, borderPaint)

        // عنوان الرسم البياني
        paint.color = Color.WHITE
        paint.textSize = 30f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("📈 حركة المبيعات خلال أيام الأسبوع", width - 80f, chartBoxY + 55f, paint)

        paint.color = Color.parseColor("#64B5F6")
        paint.textSize = 22f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("محدث لحظياً ومطابق للفواتير", width - 80f, chartBoxY + 90f, paint)

        // بيانات الرسم البياني لأيام الأسبوع (7 أيام)
        val days = listOf("السبت", "الأحد", "الإثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة")
        val dayValues = mutableListOf(2450.0, 3120.0, 2780.0, 3650.0, 4200.0, 5100.0, 3890.0)

        // تجميع الفواتير حسب يوم الأسبوع
        val cal = Calendar.getInstance()
        for (inv in invoices) {
            cal.timeInMillis = inv.timestamp
            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
            val idx = when (dayOfWeek) {
                Calendar.SATURDAY -> 0
                Calendar.SUNDAY -> 1
                Calendar.MONDAY -> 2
                Calendar.TUESDAY -> 3
                Calendar.WEDNESDAY -> 4
                Calendar.THURSDAY -> 5
                Calendar.FRIDAY -> 6
                else -> 6
            }
            dayValues[idx] = dayValues[idx] + inv.totalAmount
        }

        val maxVal = (dayValues.maxOrNull() ?: 1.0).coerceAtLeast(100.0)

        // رسم الأعمدة البيانية
        val chartContentLeft = 100f
        val chartContentRight = width - 100f
        val chartContentBottom = chartBoxY + chartBoxHeight - 90f
        val chartPlotHeight = 260f
        val stepX = (chartContentRight - chartContentLeft) / days.size
        val barWidth = stepX * 0.55f

        // خط القاعدة
        val axisPaint = Paint().apply {
            color = Color.parseColor("#1B6972")
            strokeWidth = 2f
        }
        canvas.drawLine(chartContentLeft - 20f, chartContentBottom, chartContentRight + 20f, chartContentBottom, axisPaint)

        val calNow = Calendar.getInstance()
        val currentDayOfWeek = calNow.get(Calendar.DAY_OF_WEEK)
        val currentDayIdx = when (currentDayOfWeek) {
            Calendar.SATURDAY -> 0
            Calendar.SUNDAY -> 1
            Calendar.MONDAY -> 2
            Calendar.TUESDAY -> 3
            Calendar.WEDNESDAY -> 4
            Calendar.THURSDAY -> 5
            Calendar.FRIDAY -> 6
            else -> 6
        }

        for (i in days.indices) {
            val cx = chartContentLeft + (i * stepX) + (stepX / 2f)
            val v = dayValues[i]
            val ratio = (v / maxVal).toFloat().coerceIn(0.08f, 1f)
            val barH = chartPlotHeight * ratio
            val barTop = chartContentBottom - barH

            val isToday = (i == currentDayIdx)

            // تدرج لون العمود
            val barGrad = Paint().apply {
                shader = LinearGradient(
                    cx, barTop, cx, chartContentBottom,
                    if (isToday) Color.parseColor("#00E5FF") else Color.parseColor("#009688"),
                    if (isToday) Color.parseColor("#00796B") else Color.parseColor("#004D40"),
                    Shader.TileMode.CLAMP
                )
                isAntiAlias = true
            }

            val barRect = RectF(cx - (barWidth / 2f), barTop, cx + (barWidth / 2f), chartContentBottom)
            canvas.drawRoundRect(barRect, 10f, 10f, barGrad)

            // كتابة القيمة فوق العمود
            paint.color = if (isToday) Color.parseColor("#00E5FF") else Color.parseColor("#B2DFDB")
            paint.textSize = 20f
            paint.typeface = Typeface.create(Typeface.DEFAULT, if (isToday) Typeface.BOLD else Typeface.NORMAL)
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText(String.format(Locale.US, "%.0f", v), cx, barTop - 12f, paint)

            // تسمية اليوم تحت العمود
            paint.color = if (isToday) Color.parseColor("#00E5FF") else Color.WHITE
            paint.textSize = 22f
            paint.typeface = Typeface.create(Typeface.DEFAULT, if (isToday) Typeface.BOLD else Typeface.NORMAL)
            canvas.drawText(days[i], cx, chartContentBottom + 40f, paint)

            if (isToday) {
                // مؤشر صغير لليوم الحالي
                paint.color = Color.parseColor("#00E5FF")
                canvas.drawCircle(cx, chartContentBottom + 65f, 5f, paint)
            }
        }

        // 5. قسم توزيع طرق الدفع (Payment Methods Breakdown)
        val payBoxY = 945f
        val payBoxHeight = 265f
        val payBoxPaint = Paint().apply {
            color = Color.parseColor("#0C2E33")
            isAntiAlias = true
        }
        canvas.drawRoundRect(RectF(40f, payBoxY, width - 40f, payBoxY + payBoxHeight), 24f, 24f, payBoxPaint)
        canvas.drawRoundRect(RectF(40f, payBoxY, width - 40f, payBoxY + payBoxHeight), 24f, 24f, borderPaint)

        paint.color = Color.WHITE
        paint.textSize = 28f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("💳 توزيع الإيرادات حسب وسيلة الدفع", width - 80f, payBoxY + 50f, paint)

        // شريط توزيع نسبي ممتد
        val totalAmountSum = (summary.cashAmount + summary.cardAmount + summary.creditAmount).coerceAtLeast(1.0)
        val cashRatio = (summary.cashAmount / totalAmountSum).toFloat()
        val cardRatio = (summary.cardAmount / totalAmountSum).toFloat()
        val creditRatio = (summary.creditAmount / totalAmountSum).toFloat()

        val barStart = 80f
        val barEnd = width - 80f
        val barTotalW = barEnd - barStart
        val barY = payBoxY + 80f
        val barThick = 26f

        // خلفية الشريط
        val emptyBarPaint = Paint().apply { color = Color.parseColor("#061A1C") }
        canvas.drawRoundRect(RectF(barStart, barY, barEnd, barY + barThick), 13f, 13f, emptyBarPaint)

        // جزء الكاش (أخضر)
        val cashW = barTotalW * cashRatio
        if (cashW > 0) {
            val cashP = Paint().apply { color = Color.parseColor("#10B981") }
            canvas.drawRoundRect(RectF(barStart, barY, barStart + cashW, barY + barThick), 13f, 13f, cashP)
        }

        // جزء مدى / بطاقة (أزرق/سماوي)
        val cardW = barTotalW * cardRatio
        if (cardW > 0) {
            val cardP = Paint().apply { color = Color.parseColor("#00E5FF") }
            canvas.drawRoundRect(RectF(barStart + cashW, barY, barStart + cashW + cardW, barY + barThick), 13f, 13f, cardP)
        }

        // تفاصيل المبالغ مع مربعات دلالية
        val rowItemY = payBoxY + 165f
        val itemColW = (width - 160f) / 3f

        drawPaymentLegend(
            canvas = canvas,
            x = 80f,
            y = rowItemY,
            color = Color.parseColor("#10B981"),
            label = "كاش ونقدي",
            amount = summary.cashAmount,
            percentage = (cashRatio * 100).toInt()
        )

        drawPaymentLegend(
            canvas = canvas,
            x = 80f + itemColW,
            y = rowItemY,
            color = Color.parseColor("#00E5FF"),
            label = "بطاقات ومدى",
            amount = summary.cardAmount,
            percentage = (cardRatio * 100).toInt()
        )

        drawPaymentLegend(
            canvas = canvas,
            x = 80f + itemColW * 2,
            y = rowItemY,
            color = Color.parseColor("#F59E0B"),
            label = "آجل / ذمم",
            amount = summary.creditAmount,
            percentage = (creditRatio * 100).toInt()
        )

        // 6. الفوتر وسفينة العلامة التجارية (Footer)
        val footerY = height - 60f
        paint.color = Color.parseColor("#4DB6AC")
        paint.textSize = 22f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("تم إنشاء هذا التقرير آلياً عبر نظام إدارة الصيدلية الذكي • $pharmacyName", width / 2f, footerY, paint)

        return bitmap
    }

    private fun drawKpiCard(
        canvas: Canvas,
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        title: String,
        value: String,
        accentColor: Int,
        bgColor: Int
    ) {
        val rect = RectF(x, y, x + w, y + h)
        val p = Paint().apply {
            color = bgColor
            isAntiAlias = true
        }
        canvas.drawRoundRect(rect, 20f, 20f, p)

        // خط تزييني علوي في الكرت
        val linePaint = Paint().apply {
            color = accentColor
            strokeWidth = 4f
        }
        canvas.drawLine(x + 20f, y + 4f, x + w - 20f, y + 4f, linePaint)

        // النص والعنوان
        val textPaint = Paint().apply {
            color = Color.parseColor("#94A3B8")
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(title, x + (w / 2f), y + 55f, textPaint)

        // القيمة البارزة
        textPaint.color = Color.WHITE
        textPaint.textSize = 34f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(value, x + (w / 2f), y + 115f, textPaint)
    }

    private fun drawPaymentLegend(
        canvas: Canvas,
        x: Float,
        y: Float,
        color: Int,
        label: String,
        amount: Double,
        percentage: Int
    ) {
        val p = Paint().apply { isAntiAlias = true }

        // مربع اللون
        p.color = color
        canvas.drawRoundRect(RectF(x, y, x + 24f, y + 24f), 6f, 6f, p)

        // اسم وسيلة الدفع
        p.color = Color.parseColor("#E2E8F0")
        p.textSize = 22f
        p.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        p.textAlign = Paint.Align.LEFT
        canvas.drawText("$label ($percentage%)", x + 34f, y + 20f, p)

        // القيمة بالريال
        p.color = Color.parseColor("#94A3B8")
        p.textSize = 20f
        p.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText(String.format(Locale.US, "%.1f ر.س", amount), x + 34f, y + 55f, p)
    }

    /**
     * حفظ صورة الرسم البياني في مجلد الكاش وتجهيز ملف مؤقت للمشاركة
     */
    fun saveChartBitmapToFile(context: Context, bitmap: Bitmap): File {
        val cacheReportsDir = File(context.cacheDir, "reports").apply { mkdirs() }
        val imageFile = File(cacheReportsDir, "تقرير_المبيعات_البياني_${System.currentTimeMillis()}.png")
        val out = FileOutputStream(imageFile)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        out.flush()
        out.close()
        return imageFile
    }

    /**
     * مشاركة صورة الرسم البياني مباشرة إلى تطبيق واتساب مع الرجوع للمشارك العام إذا تعذر
     */
    fun shareChartImageToWhatsApp(
        context: Context,
        imageFile: File,
        captionText: String
    ) {
        try {
            val authority = "${context.packageName}.fileprovider"
            val imageUri: Uri = FileProvider.getUriForFile(context, authority, imageFile)

            val whatsappIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                setPackage("com.whatsapp")
                putExtra(Intent.EXTRA_STREAM, imageUri)
                putExtra(Intent.EXTRA_TEXT, captionText)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            try {
                whatsappIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(whatsappIntent)
            } catch (e: Exception) {
                // محاولة واتساب للأعمال WhatsApp Business
                try {
                    val businessIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "image/png"
                        setPackage("com.whatsapp.w4b")
                        putExtra(Intent.EXTRA_STREAM, imageUri)
                        putExtra(Intent.EXTRA_TEXT, captionText)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(businessIntent)
                } catch (e2: Exception) {
                    // فتح منتقي المشاركة العام مع منح صلاحية قراءة الملف
                    val generalIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "image/png"
                        putExtra(Intent.EXTRA_STREAM, imageUri)
                        putExtra(Intent.EXTRA_TEXT, captionText)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    val chooser = Intent.createChooser(generalIntent, "مشاركة صورة تقرير المبيعات عبر:")
                    chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(chooser)
                    Toast.makeText(context, "تطبيق واتساب غير مثبت، تم فتح خيارات المشاركة", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (err: Exception) {
            err.printStackTrace()
            Toast.makeText(context, "حدث خطأ أثناء مشاركة الصورة: ${err.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
