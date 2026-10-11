package com.example.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit

/**
 * مدير جدولة وتشغيل عامل فحص نواقص المخزون في الخلفية
 * يوفر تحكماً كاملاً لجدولة الفحص التلقائي الدوري (Periodic Work)
 * أو الفحص الفوري المباشر (One-time Immediate Work) وإلغاء الجدولة.
 */
object LowStockWorkScheduler {

    const val PERIODIC_WORK_TAG = "saydali_periodic_stock_check"
    const val IMMEDIATE_WORK_TAG = "saydali_immediate_stock_check"

    /**
     * جدولة الفحص الدوري للمخزون في الخلفية
     * @param context سياق التطبيق
     * @param intervalHours عدد الساعات بين كل فحص (افتراضياً 6 ساعات)
     * @param customThreshold عتبة مخصصة اختيارية
     */
    fun schedulePeriodicCheck(
        context: Context,
        intervalHours: Long = 6,
        customThreshold: Int? = null
    ) {
        val workManager = WorkManager.getInstance(context)

        val inputDataBuilder = Data.Builder()
        if (customThreshold != null && customThreshold > 0) {
            inputDataBuilder.putInt(LowStockCheckWorker.KEY_CUSTOM_THRESHOLD, customThreshold)
        }

        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(false)
            .build()

        // الحد الأدنى المسموح به في WorkManager هو 15 دقيقة
        val safeIntervalMinutes = (intervalHours * 60).coerceAtLeast(15)

        val periodicWorkRequest = PeriodicWorkRequestBuilder<LowStockCheckWorker>(
            safeIntervalMinutes,
            TimeUnit.MINUTES,
            15, // Flex interval
            TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setInputData(inputDataBuilder.build())
            .addTag(PERIODIC_WORK_TAG)
            .build()

        workManager.enqueueUniquePeriodicWork(
            PERIODIC_WORK_TAG,
            ExistingPeriodicWorkPolicy.UPDATE,
            periodicWorkRequest
        )
    }

    /**
     * تشغيل فحص فوري للمخزون في الخلفية الآن بضغطة زر
     */
    fun triggerImmediateCheck(
        context: Context,
        customThreshold: Int? = null
    ) {
        val workManager = WorkManager.getInstance(context)

        val inputDataBuilder = Data.Builder()
        if (customThreshold != null && customThreshold > 0) {
            inputDataBuilder.putInt(LowStockCheckWorker.KEY_CUSTOM_THRESHOLD, customThreshold)
        }

        val immediateWorkRequest = OneTimeWorkRequestBuilder<LowStockCheckWorker>()
            .setInputData(inputDataBuilder.build())
            .addTag(IMMEDIATE_WORK_TAG)
            .build()

        workManager.enqueueUniqueWork(
            IMMEDIATE_WORK_TAG,
            ExistingWorkPolicy.REPLACE,
            immediateWorkRequest
        )
    }

    /**
     * إلغاء الفحص الدوري
     */
    fun cancelPeriodicCheck(context: Context) {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelUniqueWork(PERIODIC_WORK_TAG)
    }

    /**
     * تدفق حالة تشغيل العامل الفوري
     */
    fun getImmediateWorkState(context: Context): Flow<Boolean> {
        return WorkManager.getInstance(context)
            .getWorkInfosForUniqueWorkFlow(IMMEDIATE_WORK_TAG)
            .map { list ->
                list.any { it.state == WorkInfo.State.RUNNING || it.state == WorkInfo.State.ENQUEUED }
            }
    }
}
