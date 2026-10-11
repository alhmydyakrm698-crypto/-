package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.data.Product

/**
 * مدير نظام تنبيهات وإشعارات المخزون الحرج
 * يقوم بإرسال إشعارات نظام فورية (System Push Notifications) عند وصول رصيد الدواء
 * إلى الحد الأدنى المحدد مسبقاً أو أقل لضمان سرعة التوريد،
 * بالإضافة إلى دعم التنبيهات الدورية المجمعة.
 */
object StockAlertNotificationManager {

    const val CHANNEL_LOW_STOCK_ID = "saydali_low_stock_channel"
    private const val CHANNEL_NAME = "تنبيهات نواقص المخزون والتوريد السريع"
    private const val CHANNEL_DESC = "إشعارات فورية عند وصول كمية الدواء للحد الأدنى لطلب التوريد العاجل"

    /**
     * إنشاء قناة الإشعارات لنظام أندرويد 8.0 فما فوق
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_LOW_STOCK_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableVibration(true)
                setShowBadge(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * إرسال إشعار فوري عند وصول دواء معين إلى الحد الأدنى أو نفاده
     */
    fun notifyLowStockAlert(context: Context, product: Product) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("NAVIGATE_TO", "INVENTORY")
            putExtra("PRODUCT_ID", product.id)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            product.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val isOut = product.quantity <= 0
        val title = if (isOut) {
            "🚨 نفاد كامل للدواء: ${product.name}"
        } else {
            "⚠️ تنبيه مخزون حرج: ${product.name}"
        }

        val content = if (isOut) {
            "نفدت الكمية تماماً بالمخزن (0 ${product.unit}). يرجى سرعة التوريد فوراً من المورد ${product.preferredSupplier}."
        } else {
            "الرصيد المتبقي (${product.quantity} ${product.unit}) وصل للحد الأدنى المحدد (${product.minStockLevel} ${product.unit}). يلزم طلب توريد عاجل."
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_LOW_STOCK_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify((20000 + product.id).toInt(), builder.build())
        } catch (_: SecurityException) {
            // Handled when notification permission is not yet granted
        }
    }

    /**
     * إرسال إشعار تجميعي دوري بجميع الأصناف التي تتطلب توريداً سريعاً
     */
    fun notifyBatchLowStockAlert(context: Context, lowStockProducts: List<Product>) {
        if (lowStockProducts.isEmpty()) return
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("NAVIGATE_TO", "INVENTORY")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            9999,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val count = lowStockProducts.size
        val title = "⚡ تقرير التوريد العاجل: $count أدوية بحاجة لإعادة طلب"

        val inboxStyle = NotificationCompat.InboxStyle()
            .setBigContentTitle("قائمة الأدوية الواصلة للحد الأدنى:")
        
        lowStockProducts.take(5).forEach { prod ->
            inboxStyle.addLine("• ${prod.name}: رصيد ${prod.quantity} (الحد: ${prod.minStockLevel})")
        }
        if (count > 5) {
            inboxStyle.setSummaryText("+ ${count - 5} أصناف أخرى تتطلب التوريد")
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_LOW_STOCK_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText("يوجد $count أصناف بلغت الحد الأدنى للمخزون أو نفدت. اضغط لفتح التوريد والمخزن.")
            .setStyle(inboxStyle)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(1001, builder.build())
        } catch (_: SecurityException) {
            // Handled
        }
    }
}
