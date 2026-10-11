package com.example.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.data.PharmacyDatabase
import com.example.data.Product
import com.example.util.StockAlertNotificationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * عامل الخلفية (Background Worker) لفحص مخزون الأدوية في قاعدة بيانات Room
 * يقوم بفحص الأصناف دورياً أو عند الطلب في الخلفية، ومقارنة الرصيد المتوفر
 * بالحد الأدنى المحدد مسبقاً (أو عتبة مخصصة)، ثم إرسال إشعار فوري للصيدلي
 * لضمان استمرارية التوريد ومنع نفاد الأدوية الحرجة.
 */
class LowStockCheckWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "LowStockCheckWorker"
        const val KEY_CUSTOM_THRESHOLD = "key_custom_threshold"
        const val KEY_RESULT_LOW_COUNT = "key_result_low_count"
        const val KEY_RESULT_MESSAGE = "key_result_message"

        const val PREFS_NAME = "saydali_preferences"
        const val PREF_LAST_RUN_TIME = "last_stock_worker_run_time"
        const val PREF_LAST_LOW_COUNT = "last_stock_worker_low_count"
        const val PREF_LAST_STATUS = "last_stock_worker_status"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting background stock threshold check...")

            // 1. استخراج العتبة المخصصة إن وجدت (-1 يعني استخدام الحد الأدنى المحدد لكل دواء)
            val customThreshold = inputData.getInt(KEY_CUSTOM_THRESHOLD, -1)

            // 2. قراءة أصناف الأدوية من قاعدة بيانات Room مباشرة
            val database = PharmacyDatabase.getInstance(applicationContext)
            val dao = database.pharmacyDao()
            val allProducts = dao.getAllProductsStatic()

            if (allProducts.isEmpty()) {
                Log.d(TAG, "No products found in Room database.")
                saveRunResults(0, "قاعدة البيانات فارغة حالياً")
                return@withContext Result.success(workDataOf(KEY_RESULT_LOW_COUNT to 0))
            }

            // 3. فلترة الأصناف التي وصلت إلى مستوى أدنى من أو يساوي الحد الأدنى
            val lowStockProducts = allProducts.filter { product ->
                if (customThreshold > 0) {
                    product.quantity <= customThreshold
                } else {
                    product.quantity <= product.minStockLevel
                }
            }

            Log.d(TAG, "Found ${lowStockProducts.size} items below threshold out of ${allProducts.size} items.")

            // 4. إرسال إشعار فوري للصيدلي في حال وجود نواقص
            if (lowStockProducts.isNotEmpty()) {
                if (lowStockProducts.size == 1) {
                    // إشعار منفرد مخصص للدواء
                    StockAlertNotificationManager.notifyLowStockAlert(
                        applicationContext,
                        lowStockProducts.first()
                    )
                } else {
                    // إشعار مجمع بكافة الأدوية المحتاجة للتوريد
                    StockAlertNotificationManager.notifyBatchLowStockAlert(
                        applicationContext,
                        lowStockProducts
                    )
                }
            }

            // 5. حفظ بيانات آخر فحص في SharedPreferences لتحديث الواجهة فوراً
            val timeFormatted = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
            val statusMsg = if (lowStockProducts.isNotEmpty()) {
                "تم رصد ${lowStockProducts.size} أدوية بلغت الحد الأدنى وتم إرسال الإشعار للصيدلي ($timeFormatted)"
            } else {
                "المخزون متزن بالكامل، ولا توجد أدوية تحت الحد الأدنى ($timeFormatted)"
            }

            saveRunResults(lowStockProducts.size, statusMsg)

            Result.success(
                workDataOf(
                    KEY_RESULT_LOW_COUNT to lowStockProducts.size,
                    KEY_RESULT_MESSAGE to statusMsg
                )
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error executing LowStockCheckWorker: ${e.message}", e)
            saveRunResults(-1, "فشل فحص المخزون في الخلفية: ${e.localizedMessage}")
            Result.retry()
        }
    }

    private fun saveRunResults(count: Int, status: String) {
        val prefs = applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putLong(PREF_LAST_RUN_TIME, System.currentTimeMillis())
            .putInt(PREF_LAST_LOW_COUNT, count)
            .putString(PREF_LAST_STATUS, status)
            .apply()
    }
}
