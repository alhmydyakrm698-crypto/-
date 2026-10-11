package com.example.data

/**
 * نماذج البيانات التحليلية للمبيعات اليومية والأصناف الأكثر مبيعاً
 * المستندة إلى قاعدة بيانات MySQL (جداول sale_invoices و sale_items)
 */
data class MySqlDailySalesSummary(
    val dateString: String,           // تاريخ اليوم بالصيغة YYYY-MM-DD
    val dayNameArabic: String,        // اسم اليوم بالعربية (السبت، الأحد، ...)
    val totalRevenue: Double,         // إجمالي الإيراد المالي بالريال السعودي
    val invoiceCount: Int,            // عدد الفواتير المنفذة في هذا اليوم
    val totalItems: Int,              // إجمالي عدد العلب/الوحدات المباعة
    val timestamp: Long = System.currentTimeMillis()
)

data class MySqlTopSellingItem(
    val productId: Long,              // معرّف الصنف أو الدواء
    val productName: String,          // الاسم التجاري للصنف
    val totalQuantitySold: Int,       // إجمالي الكمية المباعة
    val totalRevenue: Double,         // إجمالي المبيعات المحققة بالريال السعودي
    val invoiceCount: Int,            // عدد الفواتير التي تضمنت هذا الصنف
    val percentageOfTotal: Float = 0f // النسبة المئوية من إجمالي كمية المبيعات
)

data class MySqlSalesOverview(
    val totalInvoices: Int,           // إجمالي عدد الفواتير
    val totalRevenue: Double,         // إجمالي المبيعات المالية بالريال
    val avgInvoiceValue: Double,      // متوسط قيمة الفاتورة الواحدة
    val totalUnitsSold: Int,          // إجمالي عدد الوحدات المباعة
    val lastSaleTimestamp: Long,      // توقيت آخر عملية بيع مسجلة
    val isFromRemoteMySql: Boolean,   // هل مصدر البيانات مباشر من خادم MySQL البعيد
    val sourceLabel: String           // وصف المصدر ("خادم MySQL البعيد 🐬" أو "محلي متزامن")
)
