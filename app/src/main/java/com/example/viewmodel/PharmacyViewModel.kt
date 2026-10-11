package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CustomerEntity
import com.example.data.CustomerLedgerEntity
import com.example.data.DrugInteractionEntity
import com.example.data.MedicineCategoryEntity
import com.example.data.MedicineDosageEntity
import com.example.data.PharmacyDatabase
import com.example.data.PharmacyRepository
import com.example.data.PharmacyUser
import com.example.data.Product
import com.example.data.RefillReminderEntity
import com.example.data.SaleInvoice
import com.example.data.SaleItem
import com.example.data.SaleReturn
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.Dispatchers
import android.content.Context
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import com.example.util.PharmacyBackupManager
import com.example.util.BackupResult
import com.example.util.BackupFrequency
import com.example.util.StockAlertNotificationManager
import com.example.util.MySqlDatabaseSyncManager
import com.example.data.MySqlConfig
import com.example.data.MySqlSyncResult
import com.example.worker.LowStockCheckWorker
import com.example.worker.LowStockWorkScheduler
import com.example.util.SecureMySqlPreferences
import com.example.data.MySqlDailySalesSummary
import com.example.data.MySqlTopSellingItem
import com.example.data.MySqlSalesOverview
import com.example.data.MySqlRemoteRepository

enum class AppThemeMode {
    AUTO_SUNSET,  // وضع ليلي تلقائي حسب توقيت غروب الشمس
    ALWAYS_DARK,  // وضع داكن دائم
    ALWAYS_LIGHT  // وضع نهاري دائم
}

data class SunsetCityPreset(
    val id: String,
    val name: String,
    val sunsetHour: Int,
    val sunsetMinute: Int,
    val sunriseHour: Int,
    val sunriseMinute: Int
)

val AVAILABLE_SUNSET_CITIES = listOf(
    SunsetCityPreset("riyadh", "الرياض", 18, 0, 5, 45),
    SunsetCityPreset("makkah", "مكة المكرمة", 18, 25, 6, 10),
    SunsetCityPreset("jeddah", "جدة", 18, 26, 6, 11),
    SunsetCityPreset("madinah", "المدينة المنورة", 18, 20, 6, 5),
    SunsetCityPreset("dammam", "الدمام / الخبر", 17, 45, 5, 30),
    SunsetCityPreset("cairo", "القاهرة", 17, 50, 5, 50),
    SunsetCityPreset("dubai", "دبي / أبوظبي", 18, 10, 6, 15),
    SunsetCityPreset("custom", "توقيت مخصص", 18, 0, 6, 0)
)

enum class PharmacyScreen {
    LOGIN,
    DASHBOARD,
    SALES_CASHIER,
    NEW_STOCK_ENTRY,
    INVENTORY,
    REPORTS,
    USERS,
    SETTINGS,
    CUSTOMER_LEDGER,              // شاشة كشف حساب وديون العملاء
    CHRONIC_REFILLS_AND_PURCHASE, // شاشة التذكير بالمزمنة وأوامر الشراء
    RETURNS,                      // شاشة المراجعات والمرتجعات (الخانة السادسة)
    MYSQL_CONNECTIVITY,           // لوحة مؤشرات اتصال قاعدة بيانات MySQL اللحظية
    MYSQL_SETTINGS,               // واجهة إعدادات مخصصة لبيانات اتصال MySQL المشفرة بـ EncryptedSharedPreferences
    MYSQL_SALES_ANALYTICS         // لوحة تحكم رسوم بيانية تفاعلية Vico للمبيعات اليومية والأصناف الأكثر مبيعاً استناداً لبيانات MySQL
}

data class CartItem(
    val product: Product,
    val quantity: Int,
    val dosageInstruction: String = "",
    val stickerLabel: String = ""
) {
    val totalPrice: Double get() = quantity * product.unitPrice
}

data class SuggestedPurchaseItem(
    val product: Product,
    val hoursRemaining: Double,
    val suggestedQuantity: Int,
    val preferredSupplier: String
)

class PharmacyViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PharmacyRepository
    private val prefs by lazy {
        application.getSharedPreferences("saydali_preferences", Context.MODE_PRIVATE)
    }

    // App Preferences / Theme
    private val _themeMode = MutableStateFlow(
        try {
            AppThemeMode.valueOf(prefs.getString("theme_mode", AppThemeMode.AUTO_SUNSET.name) ?: AppThemeMode.AUTO_SUNSET.name)
        } catch (_: Exception) {
            AppThemeMode.AUTO_SUNSET
        }
    )
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    private val _selectedSunsetCity = MutableStateFlow(prefs.getString("sunset_city", "الرياض") ?: "الرياض")
    val selectedSunsetCity: StateFlow<String> = _selectedSunsetCity.asStateFlow()

    private val _sunsetHour = MutableStateFlow(prefs.getInt("sunset_h", 18))
    val sunsetHour: StateFlow<Int> = _sunsetHour.asStateFlow()

    private val _sunsetMinute = MutableStateFlow(prefs.getInt("sunset_m", 0))
    val sunsetMinute: StateFlow<Int> = _sunsetMinute.asStateFlow()

    private val _sunriseHour = MutableStateFlow(prefs.getInt("sunrise_h", 5))
    val sunriseHour: StateFlow<Int> = _sunriseHour.asStateFlow()

    private val _sunriseMinute = MutableStateFlow(prefs.getInt("sunrise_m", 45))
    val sunriseMinute: StateFlow<Int> = _sunriseMinute.asStateFlow()

    // Test preview simulator: null = real clock, true = test night, false = test day
    private val _simulatedDayNightPreview = MutableStateFlow<Boolean?>(null)
    val simulatedDayNightPreview: StateFlow<Boolean?> = _simulatedDayNightPreview.asStateFlow()

    // Reports Eye Comfort Mode (راحة العين أثناء الاستخدام الطويل)
    private val _reportsEyeComfortMode = MutableStateFlow(prefs.getBoolean("reports_eye_comfort", true))
    val reportsEyeComfortMode: StateFlow<Boolean> = _reportsEyeComfortMode.asStateFlow()

    private val _isDarkMode = MutableStateFlow(true)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _isSunsetPast = MutableStateFlow(false)
    val isSunsetPast: StateFlow<Boolean> = _isSunsetPast.asStateFlow()

    private val _sunsetStatusDescription = MutableStateFlow("")
    val sunsetStatusDescription: StateFlow<String> = _sunsetStatusDescription.asStateFlow()

    // ==========================================
    // Cloud & Local Scheduled Backup (Firebase Storage & Room)
    // ==========================================
    private val _isAutoBackupEnabled = MutableStateFlow(prefs.getBoolean("auto_backup_enabled", true))
    val isAutoBackupEnabled: StateFlow<Boolean> = _isAutoBackupEnabled.asStateFlow()

    private val _backupFrequency = MutableStateFlow(
        BackupFrequency.fromName(prefs.getString("backup_frequency", BackupFrequency.DAILY.name))
    )
    val backupFrequency: StateFlow<BackupFrequency> = _backupFrequency.asStateFlow()

    private val _lastBackupTime = MutableStateFlow(prefs.getLong("last_backup_time", 0L))
    val lastBackupTime: StateFlow<Long> = _lastBackupTime.asStateFlow()

    private val _lastBackupStatus = MutableStateFlow(
        prefs.getString("last_backup_status", "لم يتم إجراء نسخ احتياطي بعد") ?: "لم يتم إجراء نسخ احتياطي بعد"
    )
    val lastBackupStatus: StateFlow<String> = _lastBackupStatus.asStateFlow()

    private val _isBackupInProgress = MutableStateFlow(false)
    val isBackupInProgress: StateFlow<Boolean> = _isBackupInProgress.asStateFlow()

    // Background Low Stock Worker State & Settings
    private val _isStockWorkerEnabled = MutableStateFlow(
        prefs.getBoolean("stock_worker_enabled", true)
    )
    val isStockWorkerEnabled: StateFlow<Boolean> = _isStockWorkerEnabled.asStateFlow()

    private val _stockWorkerIntervalHours = MutableStateFlow(
        prefs.getLong("stock_worker_interval_hours", 6L)
    )
    val stockWorkerIntervalHours: StateFlow<Long> = _stockWorkerIntervalHours.asStateFlow()

    private val _stockWorkerThreshold = MutableStateFlow(
        prefs.getInt("stock_worker_threshold", 5)
    )
    val stockWorkerThreshold: StateFlow<Int> = _stockWorkerThreshold.asStateFlow()

    private val _lastStockWorkerRunTime = MutableStateFlow(
        prefs.getLong(LowStockCheckWorker.PREF_LAST_RUN_TIME, 0L)
    )
    val lastStockWorkerRunTime: StateFlow<Long> = _lastStockWorkerRunTime.asStateFlow()

    private val _lastStockWorkerStatus = MutableStateFlow(
        prefs.getString(LowStockCheckWorker.PREF_LAST_STATUS, "لم يتم تشغيل فحص الخلفية بعد") ?: "لم يتم تشغيل فحص الخلفية بعد"
    )
    val lastStockWorkerStatus: StateFlow<String> = _lastStockWorkerStatus.asStateFlow()

    private val _lastStockWorkerLowCount = MutableStateFlow(
        prefs.getInt(LowStockCheckWorker.PREF_LAST_LOW_COUNT, 0)
    )
    val lastStockWorkerLowCount: StateFlow<Int> = _lastStockWorkerLowCount.asStateFlow()

    private val _isStockWorkerRunningNow = MutableStateFlow(false)
    val isStockWorkerRunningNow: StateFlow<Boolean> = _isStockWorkerRunningNow.asStateFlow()

    // ==========================================
    // MySQL Real Database Integration StateFlows
    // ==========================================
    private val _mySqlConfig = MutableStateFlow(
        SecureMySqlPreferences.loadConfig(application, prefs)
    )
    val mySqlConfig: StateFlow<MySqlConfig> = _mySqlConfig.asStateFlow()

    private val _isMySqlSyncing = MutableStateFlow(false)
    val isMySqlSyncing: StateFlow<Boolean> = _isMySqlSyncing.asStateFlow()

    private val _isMySqlConnected = MutableStateFlow<Boolean?>(null)
    val isMySqlConnected: StateFlow<Boolean?> = _isMySqlConnected.asStateFlow()

    private val _mySqlConnectionStatusMsg = MutableStateFlow("")
    val mySqlConnectionStatusMsg: StateFlow<String> = _mySqlConnectionStatusMsg.asStateFlow()

    private val _isMySqlTesting = MutableStateFlow(false)
    val isMySqlTesting: StateFlow<Boolean> = _isMySqlTesting.asStateFlow()

    private val _mySqlTestDetail = MutableStateFlow<com.example.data.MySqlTestResult?>(null)
    val mySqlTestDetail: StateFlow<com.example.data.MySqlTestResult?> = _mySqlTestDetail.asStateFlow()

    // ==========================================
    // MySQL Real Database Sales & Analytics StateFlows
    // ==========================================
    private val _mySqlDailySales = MutableStateFlow<List<MySqlDailySalesSummary>>(emptyList())
    val mySqlDailySales: StateFlow<List<MySqlDailySalesSummary>> = _mySqlDailySales.asStateFlow()

    private val _mySqlTopSellingItems = MutableStateFlow<List<MySqlTopSellingItem>>(emptyList())
    val mySqlTopSellingItems: StateFlow<List<MySqlTopSellingItem>> = _mySqlTopSellingItems.asStateFlow()

    private val _mySqlSalesOverview = MutableStateFlow<MySqlSalesOverview?>(null)
    val mySqlSalesOverview: StateFlow<MySqlSalesOverview?> = _mySqlSalesOverview.asStateFlow()

    private val _isMySqlSalesLoading = MutableStateFlow(false)
    val isMySqlSalesLoading: StateFlow<Boolean> = _isMySqlSalesLoading.asStateFlow()

    private val _mySqlSalesDataSource = MutableStateFlow("جاري التحميل...")
    val mySqlSalesDataSource: StateFlow<String> = _mySqlSalesDataSource.asStateFlow()

    private val _mySqlSalesError = MutableStateFlow<String?>(null)
    val mySqlSalesError: StateFlow<String?> = _mySqlSalesError.asStateFlow()

    private val _isMySqlSyncingSales = MutableStateFlow(false)
    val isMySqlSyncingSales: StateFlow<Boolean> = _isMySqlSyncingSales.asStateFlow()

    init {
        val db = PharmacyDatabase.getDatabase(application, viewModelScope)
        repository = PharmacyRepository(db.pharmacyDao())
        viewModelScope.launch {
            repository.ensureSeeded()
            loadMySqlSalesAnalytics()
        }

        // Initialize and maintain automatic day/night tracking
        recalculateDayNightState()
        viewModelScope.launch {
            while (isActive) {
                recalculateDayNightState()
                delay(30_000) // update every 30 seconds
            }
        }

        // Automated Periodic Backup Scheduler
        viewModelScope.launch {
            delay(10_000) // slight initial delay after launch
            while (isActive) {
                checkAndTriggerScheduledBackup()
                delay(60_000) // check every minute
            }
        }

        // Initialize background Room low-stock threshold check worker
        if (_isStockWorkerEnabled.value) {
            LowStockWorkScheduler.schedulePeriodicCheck(
                application,
                _stockWorkerIntervalHours.value,
                if (_stockWorkerThreshold.value > 0) _stockWorkerThreshold.value else null
            )
        }

        // Automatic background MySQL synchronization loop
        viewModelScope.launch {
            delay(3500) // initial delay after app boot
            if (_mySqlConfig.value.isEnabled && _mySqlConfig.value.isAutoSyncEnabled) {
                testMySqlConnection()
                triggerAutoSyncToMySql("بدء تشغيل التطبيق", silent = true)
            }
            while (isActive) {
                val intervalMins = _mySqlConfig.value.autoSyncIntervalMinutes.coerceAtLeast(1)
                delay(intervalMins * 60_000L)
                if (_mySqlConfig.value.isEnabled && _mySqlConfig.value.isAutoSyncEnabled) {
                    triggerAutoSyncToMySql("المزامنة التلقائية الدورية", silent = true)
                }
            }
        }
    }

    fun recalculateDayNightState() {
        val cal = Calendar.getInstance()
        val curHour = cal.get(Calendar.HOUR_OF_DAY)
        val curMin = cal.get(Calendar.MINUTE)
        val curTotalMins = curHour * 60 + curMin

        val sunsetTotalMins = _sunsetHour.value * 60 + _sunsetMinute.value
        val sunriseTotalMins = _sunriseHour.value * 60 + _sunriseMinute.value

        // Is it currently night (after sunset or before sunrise)?
        val isNight = if (sunsetTotalMins > sunriseTotalMins) {
            curTotalMins >= sunsetTotalMins || curTotalMins < sunriseTotalMins
        } else {
            curTotalMins in sunsetTotalMins until sunriseTotalMins
        }
        _isSunsetPast.value = isNight

        val sH = _sunsetHour.value
        val s12 = if (sH % 12 == 0) 12 else sH % 12
        val sPeriod = if (sH >= 12) "م" else "ص"
        val sunsetFormatted = String.format(Locale.US, "%02d:%02d %s", s12, _sunsetMinute.value, sPeriod)

        val rH = _sunriseHour.value
        val r12 = if (rH % 12 == 0) 12 else rH % 12
        val rPeriod = if (rH >= 12) "م" else "ص"
        val sunriseFormatted = String.format(Locale.US, "%02d:%02d %s", r12, _sunriseMinute.value, rPeriod)

        _sunsetStatusDescription.value = if (isNight) {
            "الآن: بعد الغروب ($sunsetFormatted) - الوضع الليلي نشط تلقائياً 🌙"
        } else {
            "الآن: نهاراً (الغروب في $sunsetFormatted) - الوضع النهاري نشط تلقائياً ☀️"
        }

        // Determine effective dark mode
        val effectiveNight = when (_themeMode.value) {
            AppThemeMode.ALWAYS_DARK -> true
            AppThemeMode.ALWAYS_LIGHT -> false
            AppThemeMode.AUTO_SUNSET -> {
                _simulatedDayNightPreview.value ?: isNight
            }
        }
        _isDarkMode.value = effectiveNight
    }

    fun setThemeMode(mode: AppThemeMode) {
        _themeMode.value = mode
        prefs.edit().putString("theme_mode", mode.name).apply()
        recalculateDayNightState()
        val desc = when (mode) {
            AppThemeMode.AUTO_SUNSET -> "تم تفعيل الوضع الليلي التلقائي حسب توقيت الغروب 🌅"
            AppThemeMode.ALWAYS_DARK -> "تم تفعيل الوضع الليلي الدائم 🌙"
            AppThemeMode.ALWAYS_LIGHT -> "تم تفعيل الوضع النهاري الدائم ☀️"
        }
        emitMessage(desc)
    }

    fun selectSunsetCity(preset: SunsetCityPreset) {
        _selectedSunsetCity.value = preset.name
        _sunsetHour.value = preset.sunsetHour
        _sunsetMinute.value = preset.sunsetMinute
        _sunriseHour.value = preset.sunriseHour
        _sunriseMinute.value = preset.sunriseMinute
        prefs.edit()
            .putString("sunset_city", preset.name)
            .putInt("sunset_h", preset.sunsetHour)
            .putInt("sunset_m", preset.sunsetMinute)
            .putInt("sunrise_h", preset.sunriseHour)
            .putInt("sunrise_m", preset.sunriseMinute)
            .apply()
        recalculateDayNightState()
        emitMessage("تم ضبط توقيت الغروب والشروق على ${preset.name}")
    }

    fun updateCustomSunsetSunrise(sunsetH: Int, sunsetM: Int, sunriseH: Int, sunriseM: Int) {
        _selectedSunsetCity.value = "توقيت مخصص"
        _sunsetHour.value = sunsetH.coerceIn(0, 23)
        _sunsetMinute.value = sunsetM.coerceIn(0, 59)
        _sunriseHour.value = sunriseH.coerceIn(0, 23)
        _sunriseMinute.value = sunriseM.coerceIn(0, 59)
        prefs.edit()
            .putString("sunset_city", "توقيت مخصص")
            .putInt("sunset_h", _sunsetHour.value)
            .putInt("sunset_m", _sunsetMinute.value)
            .putInt("sunrise_h", _sunriseHour.value)
            .putInt("sunrise_m", _sunriseMinute.value)
            .apply()
        recalculateDayNightState()
        emitMessage("تم حفظ التوقيت المخصص للغروب والشروق بنجاح")
    }

    fun toggleDarkMode(enabled: Boolean) {
        setThemeMode(if (enabled) AppThemeMode.ALWAYS_DARK else AppThemeMode.ALWAYS_LIGHT)
    }

    fun testSimulateDayNight(simulatedNight: Boolean?) {
        _simulatedDayNightPreview.value = simulatedNight
        recalculateDayNightState()
        if (simulatedNight == null) {
            emitMessage("تم العودة لتوقيت غروب الشمس الفعلي الحقيقي")
        } else if (simulatedNight) {
            emitMessage("معاينة تجريبية: محاكاة بعد الغروب (وضع ليلي) 🌙")
        } else {
            emitMessage("معاينة تجريبية: محاكاة نهاراً (وضع نهاري) ☀️")
        }
    }

    fun toggleReportsEyeComfort(enabled: Boolean) {
        _reportsEyeComfortMode.value = enabled
        prefs.edit().putBoolean("reports_eye_comfort", enabled).apply()
        emitMessage(if (enabled) "تم تفعيل نمط راحة العين والتباين العالي للتقارير 👁️" else "تم إيقاف نمط راحة العين")
    }

    // ==========================================
    // Cloud & Local Backup Methods (Firebase Storage & Room)
    // ==========================================
    fun setAutoBackupEnabled(enabled: Boolean) {
        _isAutoBackupEnabled.value = enabled
        prefs.edit().putBoolean("auto_backup_enabled", enabled).apply()
        emitMessage(if (enabled) "تم تفعيل الجدولة التلقائية للنسخ الاحتياطي السحابي ☁️" else "تم إيقاف الجدولة التلقائية للنسخ الاحتياطي")
    }

    fun setBackupFrequency(frequency: BackupFrequency) {
        _backupFrequency.value = frequency
        prefs.edit().putString("backup_frequency", frequency.name).apply()
        emitMessage("تم ضبط وتيرة النسخ الاحتياطي: ${frequency.titleAr}")
    }

    fun performImmediateBackup(toFirebase: Boolean = true) {
        if (_isBackupInProgress.value) return
        _isBackupInProgress.value = true
        emitMessage("جارٍ إنشاء ورفع النسخة الاحتياطية لقاعدة البيانات...")

        viewModelScope.launch {
            val db = PharmacyDatabase.getDatabase(getApplication(), viewModelScope)
            val result = PharmacyBackupManager.performBackup(getApplication(), db, uploadToCloud = toFirebase)
            _isBackupInProgress.value = false

            when (result) {
                is BackupResult.Success -> {
                    _lastBackupTime.value = result.timestamp
                    _lastBackupStatus.value = result.message
                    prefs.edit()
                        .putLong("last_backup_time", result.timestamp)
                        .putString("last_backup_status", result.message)
                        .apply()
                    emitMessage(result.message)
                }
                is BackupResult.Failure -> {
                    _lastBackupStatus.value = "فشل النسخ الاحتياطي: ${result.error}"
                    emitMessage("فشل النسخ الاحتياطي: ${result.error}")
                }
            }
        }
    }

    private suspend fun checkAndTriggerScheduledBackup() {
        if (!_isAutoBackupEnabled.value) return
        val last = _lastBackupTime.value
        val now = System.currentTimeMillis()
        val interval = _backupFrequency.value.intervalMillis

        if (last == 0L || (now - last) >= interval) {
            val db = PharmacyDatabase.getDatabase(getApplication(), viewModelScope)
            val result = PharmacyBackupManager.performBackup(getApplication(), db, uploadToCloud = true)
            if (result is BackupResult.Success) {
                _lastBackupTime.value = result.timestamp
                _lastBackupStatus.value = "نسخ تلقائي مجدول: ${result.message}"
                prefs.edit()
                    .putLong("last_backup_time", result.timestamp)
                    .putString("last_backup_status", _lastBackupStatus.value)
                    .apply()
            }
        }
    }

    // ==========================================
    // Background Stock Threshold Worker Controls
    // ==========================================
    fun setStockWorkerEnabled(enabled: Boolean) {
        _isStockWorkerEnabled.value = enabled
        prefs.edit().putBoolean("stock_worker_enabled", enabled).apply()
        if (enabled) {
            LowStockWorkScheduler.schedulePeriodicCheck(
                getApplication(),
                _stockWorkerIntervalHours.value,
                if (_stockWorkerThreshold.value > 0) _stockWorkerThreshold.value else null
            )
            emitMessage("تم تفعيل فحص المخزون الدوري في الخلفية (كل ${_stockWorkerIntervalHours.value} ساعات) 🔄")
        } else {
            LowStockWorkScheduler.cancelPeriodicCheck(getApplication())
            emitMessage("تم إيقاف فحص المخزون الدوري في الخلفية")
        }
    }

    fun setStockWorkerInterval(hours: Long) {
        _stockWorkerIntervalHours.value = hours
        prefs.edit().putLong("stock_worker_interval_hours", hours).apply()
        if (_isStockWorkerEnabled.value) {
            LowStockWorkScheduler.schedulePeriodicCheck(
                getApplication(),
                hours,
                if (_stockWorkerThreshold.value > 0) _stockWorkerThreshold.value else null
            )
            emitMessage("تم تحديث جدولة فحص المخزون إلى كل $hours ساعات")
        }
    }

    fun setStockWorkerThreshold(threshold: Int) {
        _stockWorkerThreshold.value = threshold
        prefs.edit().putInt("stock_worker_threshold", threshold).apply()
        if (_isStockWorkerEnabled.value) {
            LowStockWorkScheduler.schedulePeriodicCheck(
                getApplication(),
                _stockWorkerIntervalHours.value,
                if (threshold > 0) threshold else null
            )
        }
        val text = if (threshold > 0) "$threshold عبوة أو أقل" else "حسب الحد الأدنى لكل دواء"
        emitMessage("تم ضبط عتبة تنبيه المخزون: $text")
    }

    fun triggerStockWorkerImmediate() {
        _isStockWorkerRunningNow.value = true
        LowStockWorkScheduler.triggerImmediateCheck(
            getApplication(),
            if (_stockWorkerThreshold.value > 0) _stockWorkerThreshold.value else null
        )
        emitMessage("جاري فحص قاعدة بيانات المخزون في الخلفية الآن ⚡")
        viewModelScope.launch {
            delay(1200)
            _lastStockWorkerRunTime.value = prefs.getLong(LowStockCheckWorker.PREF_LAST_RUN_TIME, System.currentTimeMillis())
            _lastStockWorkerStatus.value = prefs.getString(LowStockCheckWorker.PREF_LAST_STATUS, "اكتمل الفحص بنجاح") ?: "اكتمل الفحص بنجاح"
            _lastStockWorkerLowCount.value = prefs.getInt(LowStockCheckWorker.PREF_LAST_LOW_COUNT, 0)
            _isStockWorkerRunningNow.value = false
        }
    }

    // ==============================================================
    // MySQL Real Database Management & Sync (المخزون والمرتجعات)
    // ==============================================================
    fun updateMySqlConfig(
        host: String,
        port: Int,
        databaseName: String,
        username: String,
        password: String,
        apiUrl: String,
        isEnabled: Boolean,
        isAutoSyncEnabled: Boolean = true,
        autoSyncIntervalMinutes: Int = 5
    ) {
        val updated = _mySqlConfig.value.copy(
            host = host.trim(),
            port = port,
            databaseName = databaseName.trim(),
            username = username.trim(),
            password = password,
            apiUrl = apiUrl.trim(),
            isEnabled = isEnabled,
            isAutoSyncEnabled = isAutoSyncEnabled,
            autoSyncIntervalMinutes = autoSyncIntervalMinutes
        )
        _mySqlConfig.value = updated
        SecureMySqlPreferences.saveConfig(getApplication(), updated)
        emitMessage("تم حفظ بيانات اتصال MySQL وتشفيرها بأمان (AES-256) عبر EncryptedSharedPreferences")
    }

    fun toggleMySqlAutoSync(enabled: Boolean) {
        val updated = _mySqlConfig.value.copy(isAutoSyncEnabled = enabled)
        _mySqlConfig.value = updated
        SecureMySqlPreferences.saveConfig(getApplication(), updated)
        emitMessage(if (enabled) "تم تفعيل المزامنة التلقائية اللحظية مع MySQL" else "تم إيقاف المزامنة التلقائية مع MySQL")
    }

    /**
     * تشغيل المزامنة التلقائية اللحظية لبيانات المخزون والمرتجعات مع قاعدة بيانات MySQL
     */
    fun triggerAutoSyncToMySql(reason: String = "تحديث البيانات", silent: Boolean = true) {
        val cfg = _mySqlConfig.value
        if (!cfg.isEnabled || !cfg.isAutoSyncEnabled) return
        if (_isMySqlSyncing.value) return

        _isMySqlSyncing.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val productsList = allProducts.value
                val returnsList = allSaleReturns.value
                val result = MySqlDatabaseSyncManager.syncInventoryAndReturnsToMySql(
                    context = getApplication(),
                    config = cfg,
                    products = productsList,
                    returns = returnsList
                )
                _isMySqlSyncing.value = false

                when (result) {
                    is MySqlSyncResult.Success -> {
                        val updated = _mySqlConfig.value.copy(
                            lastSyncTime = result.timestamp,
                            lastSyncStatus = "✓ تزامن تلقائي ($reason): ${result.syncedProducts} صنف و ${result.syncedReturns} حركة"
                        )
                        _mySqlConfig.value = updated
                        SecureMySqlPreferences.updateSyncStatus(getApplication(), updated.lastSyncStatus, result.timestamp)
                        if (!silent) {
                            emitMessage(result.message)
                        }
                    }
                    is MySqlSyncResult.Failure -> {
                        val updated = _mySqlConfig.value.copy(
                            lastSyncStatus = "فشل التزامن: ${result.error}"
                        )
                        _mySqlConfig.value = updated
                        SecureMySqlPreferences.updateSyncStatus(getApplication(), updated.lastSyncStatus)
                        if (!silent) {
                            emitMessage(result.error)
                        }
                    }
                }
            } catch (e: Exception) {
                _isMySqlSyncing.value = false
            }
        }
    }

    fun testMySqlConnection() {
        if (_isMySqlTesting.value) return
        _isMySqlTesting.value = true
        viewModelScope.launch {
            _mySqlConnectionStatusMsg.value = "جارٍ فحص الاتصال بخادم MySQL (${_mySqlConfig.value.host}:${_mySqlConfig.value.port})..."
            emitMessage(_mySqlConnectionStatusMsg.value)
            val result = MySqlDatabaseSyncManager.testMySqlConnectionDetailed(_mySqlConfig.value)
            _isMySqlTesting.value = false
            _isMySqlConnected.value = result.isConnected
            _mySqlConnectionStatusMsg.value = result.message
            _mySqlTestDetail.value = result
            if (result.isConnected) {
                val updated = _mySqlConfig.value.copy(lastPingMs = result.latencyMs)
                _mySqlConfig.value = updated
            }
            emitMessage(result.message)
        }
    }

    fun syncInventoryAndReturnsToMySql() {
        if (_isMySqlSyncing.value) return
        _isMySqlSyncing.value = true
        emitMessage("جارٍ مزامنة بيانات المخزون والمرتجعات مع قاعدة بيانات MySQL...")

        viewModelScope.launch {
            val productsList = allProducts.value
            val returnsList = allSaleReturns.value
            val result = MySqlDatabaseSyncManager.syncInventoryAndReturnsToMySql(
                context = getApplication(),
                config = _mySqlConfig.value,
                products = productsList,
                returns = returnsList
            )
            _isMySqlSyncing.value = false

            when (result) {
                is MySqlSyncResult.Success -> {
                    val updated = _mySqlConfig.value.copy(
                        lastSyncTime = result.timestamp,
                        lastSyncStatus = result.message
                    )
                    _mySqlConfig.value = updated
                    SecureMySqlPreferences.updateSyncStatus(getApplication(), result.message, result.timestamp)
                    emitMessage(result.message)
                }
                is MySqlSyncResult.Failure -> {
                    val updated = _mySqlConfig.value.copy(
                        lastSyncStatus = "فشل: ${result.error}"
                    )
                    _mySqlConfig.value = updated
                    SecureMySqlPreferences.updateSyncStatus(getApplication(), updated.lastSyncStatus)
                    emitMessage(result.error)
                }
            }
        }
    }

    // ==============================================================
    // Remote MySQL Direct CRUD Operations (عبر mysql-connector-java)
    // ==============================================================
    val mySqlRemoteRepository: com.example.data.MySqlRemoteRepository by lazy {
        com.example.data.MySqlRemoteRepository(getApplication()) { _mySqlConfig.value }
    }

    /**
     * استعلام وجلب الأصناف من خادم MySQL البعيد مباشرة عبر JDBC
     */
    fun fetchRemoteProducts(onResult: (Result<List<Product>>) -> Unit = {}) {
        viewModelScope.launch {
            val result = mySqlRemoteRepository.getAllProducts()
            result.onSuccess { list ->
                emitMessage("تم جلب ${list.size} صنف بنجاح من خادم MySQL البعيد")
            }.onFailure { ex ->
                emitMessage("تعذر جلب الأصناف من MySQL البعيد: ${ex.message}")
            }
            onResult(result)
        }
    }

    /**
     * استعلام وجلب المرتجعات من خادم MySQL البعيد مباشرة عبر JDBC
     */
    fun fetchRemoteSaleReturns(onResult: (Result<List<SaleReturn>>) -> Unit = {}) {
        viewModelScope.launch {
            val result = mySqlRemoteRepository.getAllSaleReturns()
            result.onSuccess { list ->
                emitMessage("تم جلب ${list.size} حركة مرتجع بنجاح من خادم MySQL البعيد")
            }.onFailure { ex ->
                emitMessage("تعذر جلب المرتجعات من MySQL البعيد: ${ex.message}")
            }
            onResult(result)
        }
    }

    /**
     * إضافة صنف عن بُعد في MySQL مباشرة عبر JDBC (Create)
     */
    fun remoteInsertProduct(product: Product, onComplete: (Result<Long>) -> Unit = {}) {
        viewModelScope.launch {
            val result = mySqlRemoteRepository.insertProduct(product)
            result.onSuccess {
                emitMessage("تمت إضافة الصنف '${product.name}' إلى قاعدة بيانات MySQL البعيدة بنجاح")
            }.onFailure { ex ->
                emitMessage("فشل إضافة الصنف عن بُعد: ${ex.message}")
            }
            onComplete(result)
        }
    }

    /**
     * تحديث صنف عن بُعد في MySQL مباشرة عبر JDBC (Update)
     */
    fun remoteUpdateProduct(product: Product, onComplete: (Result<Boolean>) -> Unit = {}) {
        viewModelScope.launch {
            val result = mySqlRemoteRepository.updateProduct(product)
            result.onSuccess {
                emitMessage("تم تحديث الصنف '${product.name}' في قاعدة بيانات MySQL البعيدة بنجاح")
            }.onFailure { ex ->
                emitMessage("فشل تحديث الصنف عن بُعد: ${ex.message}")
            }
            onComplete(result)
        }
    }

    /**
     * حذف صنف عن بُعد من MySQL مباشرة عبر JDBC (Delete)
     */
    fun remoteDeleteProduct(productId: Long, onComplete: (Result<Boolean>) -> Unit = {}) {
        viewModelScope.launch {
            val result = mySqlRemoteRepository.deleteProduct(productId)
            result.onSuccess {
                emitMessage("تم حذف الصنف من قاعدة بيانات MySQL البعيدة بنجاح")
            }.onFailure { ex ->
                emitMessage("فشل حذف الصنف عن بُعد: ${ex.message}")
            }
            onComplete(result)
        }
    }

    /**
     * إضافة مرتجع عن بُعد في MySQL مباشرة عبر JDBC (Create)
     */
    fun remoteInsertSaleReturn(saleReturn: SaleReturn, onComplete: (Result<Long>) -> Unit = {}) {
        viewModelScope.launch {
            val result = mySqlRemoteRepository.insertSaleReturn(saleReturn)
            result.onSuccess {
                emitMessage("تم تسجيل المرتجع رقم ${saleReturn.invoiceNumber} في قاعدة بيانات MySQL البعيدة بنجاح")
            }.onFailure { ex ->
                emitMessage("فشل تسجيل المرتجع عن بُعد: ${ex.message}")
            }
            onComplete(result)
        }
    }

    /**
     * حذف مرتجع عن بُعد من MySQL مباشرة عبر JDBC (Delete)
     */
    fun remoteDeleteSaleReturn(returnId: Long, onComplete: (Result<Boolean>) -> Unit = {}) {
        viewModelScope.launch {
            val result = mySqlRemoteRepository.deleteSaleReturn(returnId)
            result.onSuccess {
                emitMessage("تم حذف المرتجع من قاعدة بيانات MySQL البعيدة بنجاح")
            }.onFailure { ex ->
                emitMessage("فشل حذف المرتجع عن بُعد: ${ex.message}")
            }
            onComplete(result)
        }
    }

    // ==============================================================
    // MySQL Real Database Sales & Analytics Engine (Vico Charts Backend)
    // ==============================================================

    /**
     * تحميل واستعلام إحصاءات المبيعات اليومية والأصناف الأكثر مبيعاً
     * من خادم MySQL البعيد مباشرة عبر JDBC مع دعم التبديل التلقائي للمحلي في حال عدم الاتصال.
     */
    fun loadMySqlSalesAnalytics(forceRemote: Boolean = false) {
        viewModelScope.launch {
            _isMySqlSalesLoading.value = true
            _mySqlSalesError.value = null

            val config = _mySqlConfig.value
            val isRemoteConfigured = config.isEnabled && config.host.isNotBlank()

            if (isRemoteConfigured) {
                try {
                    // 1. ضمان وجود الجداول عن بُعد
                    mySqlRemoteRepository.ensureRemoteTablesExist()

                    // 2. استعلام المبيعات اليومية والأصناف الأكثر مبيعاً
                    val dailyResult = mySqlRemoteRepository.fetchDailySalesStats(daysLimit = 7)
                    val topItemsResult = mySqlRemoteRepository.fetchTopSellingItems(limit = 10)
                    val overviewResult = mySqlRemoteRepository.fetchSalesOverview()

                    if (dailyResult.isSuccess && topItemsResult.isSuccess) {
                        val dailyList = dailyResult.getOrDefault(emptyList())
                        val topList = topItemsResult.getOrDefault(emptyList())
                        val overview = overviewResult.getOrNull()

                        if (dailyList.isNotEmpty() || topList.isNotEmpty()) {
                            _mySqlDailySales.value = dailyList
                            _mySqlTopSellingItems.value = topList
                            _mySqlSalesOverview.value = overview
                            _mySqlSalesDataSource.value = "قاعدة بيانات MySQL عن بُعد 🐬 (مباشر عبر JDBC)"
                            _isMySqlConnected.value = true
                            _isMySqlSalesLoading.value = false
                            return@launch
                        } else {
                            // الجداول البعيدة في MySQL فارغة، نقوم بمزامنة فواتير الغرفة المحلية إليها تلقائياً
                            val localInvoices = repository.getAllInvoicesStatic()
                            val localItems = repository.getAllSaleItemsStatic()
                            if (localInvoices.isNotEmpty()) {
                                mySqlRemoteRepository.batchSyncSales(localInvoices, localItems)
                                val refetchedDaily = mySqlRemoteRepository.fetchDailySalesStats(7).getOrDefault(emptyList())
                                val refetchedTop = mySqlRemoteRepository.fetchTopSellingItems(10).getOrDefault(emptyList())
                                val refetchedOverview = mySqlRemoteRepository.fetchSalesOverview().getOrNull()

                                _mySqlDailySales.value = refetchedDaily
                                _mySqlTopSellingItems.value = refetchedTop
                                _mySqlSalesOverview.value = refetchedOverview
                                _mySqlSalesDataSource.value = "قاعدة بيانات MySQL عن بُعد 🐬 (تمت مزامنتها الآن)"
                                _isMySqlConnected.value = true
                                _isMySqlSalesLoading.value = false
                                return@launch
                            }
                        }
                    } else {
                        _mySqlSalesError.value = dailyResult.exceptionOrNull()?.message ?: "تعذر قراءة بيانات المبيعات من MySQL"
                    }
                } catch (e: Exception) {
                    _mySqlSalesError.value = e.message ?: "خطأ في اتصال خادم MySQL"
                }
            }

            // Fallback: حساب المبيعات محلياً من الغرفة (Room) لضمان عدم توقف واجهة الرسوم البيانية
            computeLocalSalesAnalyticsFallback()
            _isMySqlSalesLoading.value = false
        }
    }

    /**
     * مزامنة ورفع كافة فواتير المبيعات وبنودها المحلية إلى قاعدة بيانات MySQL البعيدة
     */
    fun syncAllSalesToMySql(onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            _isMySqlSyncingSales.value = true
            try {
                val invoices = repository.getAllInvoicesStatic()
                val items = repository.getAllSaleItemsStatic()

                if (invoices.isEmpty()) {
                    emitMessage("لا توجد فواتير محلية للمزامنة حالياً")
                    _isMySqlSyncingSales.value = false
                    onComplete(false)
                    return@launch
                }

                mySqlRemoteRepository.ensureRemoteTablesExist()
                val result = mySqlRemoteRepository.batchSyncSales(invoices, items)
                result.onSuccess { count ->
                    emitMessage("تمت مزامنة ورفع $count فاتورة مبيعات بنجاح إلى MySQL 🐬")
                    loadMySqlSalesAnalytics(forceRemote = true)
                    onComplete(true)
                }.onFailure { ex ->
                    emitMessage("فشلت مزامنة المبيعات إلى MySQL: ${ex.message}")
                    onComplete(false)
                }
            } catch (e: Exception) {
                emitMessage("خطأ أثناء مزامنة المبيعات: ${e.message}")
                onComplete(false)
            } finally {
                _isMySqlSyncingSales.value = false
            }
        }
    }

    /**
     * حساب تحليلات المبيعات محلياً عند عدم توفر اتصال MySQL
     */
    private suspend fun computeLocalSalesAnalyticsFallback() {
        val invoices = repository.getAllInvoicesStatic()
        val items = repository.getAllSaleItemsStatic()

        val arabicDayFormat = SimpleDateFormat("EEEE", Locale("ar"))
        val dateParser = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        val dailyMap = mutableMapOf<String, MySqlDailySalesSummary>()
        for (i in 6 downTo 0) {
            val dayCal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -i)
            }
            val dateStr = dateParser.format(dayCal.time)
            val dayName = arabicDayFormat.format(dayCal.time)
            dailyMap[dateStr] = MySqlDailySalesSummary(
                dateString = dateStr,
                dayNameArabic = dayName,
                totalRevenue = 0.0,
                invoiceCount = 0,
                totalItems = 0,
                timestamp = dayCal.timeInMillis
            )
        }

        for (inv in invoices) {
            val dateStr = dateParser.format(Date(inv.timestamp))
            val existing = dailyMap[dateStr]
            if (existing != null) {
                dailyMap[dateStr] = existing.copy(
                    totalRevenue = existing.totalRevenue + inv.totalAmount,
                    invoiceCount = existing.invoiceCount + 1,
                    totalItems = existing.totalItems + inv.itemCount
                )
            } else {
                dailyMap[dateStr] = MySqlDailySalesSummary(
                    dateString = dateStr,
                    dayNameArabic = arabicDayFormat.format(Date(inv.timestamp)),
                    totalRevenue = inv.totalAmount,
                    invoiceCount = 1,
                    totalItems = inv.itemCount,
                    timestamp = inv.timestamp
                )
            }
        }

        val groupedItems = items.groupBy { it.productName }
        val grandQty = items.sumOf { it.quantity }
        val topList = groupedItems.map { (name, list) ->
            val totalQty = list.sumOf { it.quantity }
            val totalRev = list.sumOf { it.totalPrice }
            val pId = list.firstOrNull()?.productId ?: 0L
            val invCount = list.map { it.invoiceId }.distinct().size
            MySqlTopSellingItem(
                productId = pId,
                productName = name,
                totalQuantitySold = totalQty,
                totalRevenue = totalRev,
                invoiceCount = invCount,
                percentageOfTotal = if (grandQty > 0) (totalQty.toFloat() / grandQty.toFloat()) * 100f else 0f
            )
        }.sortedByDescending { it.totalQuantitySold }.take(10)

        val totalRev = invoices.sumOf { it.totalAmount }
        val totalInvCount = invoices.size
        val avgTicket = if (totalInvCount > 0) totalRev / totalInvCount else 0.0
        val totalUnits = items.sumOf { it.quantity }

        _mySqlDailySales.value = dailyMap.values.sortedBy { it.dateString }
        _mySqlTopSellingItems.value = topList
        _mySqlSalesOverview.value = MySqlSalesOverview(
            totalInvoices = totalInvCount,
            totalRevenue = totalRev,
            avgInvoiceValue = avgTicket,
            totalUnitsSold = totalUnits,
            lastSaleTimestamp = invoices.maxOfOrNull { it.timestamp } ?: System.currentTimeMillis(),
            isFromRemoteMySql = false,
            sourceLabel = if (_mySqlConfig.value.isEnabled) "محلي متزامن (في انتظار خادم MySQL)" else "محلي (خدمة MySQL معطلة بالإعدادات)"
        )
        _mySqlSalesDataSource.value = if (_mySqlConfig.value.isEnabled) "محلي متزامن (في انتظار خادم MySQL)" else "محلي (خدمة MySQL معطلة بالإعدادات)"
    }

    // Navigation & Screen Stack
    private val _currentScreen = MutableStateFlow(PharmacyScreen.LOGIN)
    val currentScreen: StateFlow<PharmacyScreen> = _currentScreen.asStateFlow()

    private val screenHistory = mutableListOf<PharmacyScreen>()

    fun navigateTo(screen: PharmacyScreen) {
        if (_currentScreen.value != screen) {
            screenHistory.add(_currentScreen.value)
            _currentScreen.value = screen
        }
    }

    fun navigateBack(): Boolean {
        if (screenHistory.isNotEmpty()) {
            _currentScreen.value = screenHistory.removeAt(screenHistory.lastIndex)
            return true
        }
        if (_currentScreen.value != PharmacyScreen.DASHBOARD && _currentScreen.value != PharmacyScreen.LOGIN) {
            _currentScreen.value = PharmacyScreen.DASHBOARD
            return true
        }
        return false
    }

    // Active User & Pharmacy Info
    private val _currentPharmacyName = MutableStateFlow("صيدلية النخبة الطبية")
    val currentPharmacyName: StateFlow<String> = _currentPharmacyName.asStateFlow()

    private val _doctorName = MutableStateFlow("د. أحمد الفاروقي")
    val doctorName: StateFlow<String> = _doctorName.asStateFlow()

    private val _doctorEmail = MutableStateFlow("dr.ahmed@saydali.com")
    val doctorEmail: StateFlow<String> = _doctorEmail.asStateFlow()

    private val _doctorPassword = MutableStateFlow("Pharm@2026")
    val doctorPassword: StateFlow<String> = _doctorPassword.asStateFlow()

    private val _pharmacyLocation = MutableStateFlow("الرياض - طريق الملك فهد - حي العليا")
    val pharmacyLocation: StateFlow<String> = _pharmacyLocation.asStateFlow()

    private val _loggedInUser = MutableStateFlow<PharmacyUser?>(
        PharmacyUser(
            name = "د. أحمد الفاروقي",
            username = "ahmed_admin",
            phone = "0501234567",
            role = "مدير",
            pharmacyName = "صيدلية النخبة الطبية"
        )
    )
    val loggedInUser: StateFlow<PharmacyUser?> = _loggedInUser.asStateFlow()

    fun updatePharmacySettings(
        name: String,
        location: String,
        doctor: String,
        email: String,
        pass: String
    ) {
        _currentPharmacyName.value = name
        _pharmacyLocation.value = location
        _doctorName.value = doctor
        _doctorEmail.value = email
        if (pass.isNotBlank()) _doctorPassword.value = pass
        emitMessage("تم حفظ إعدادات الصيدلية بنجاح")
    }

    fun login(
        docName: String,
        email: String,
        pass: String,
        loc: String,
        pName: String
    ) {
        if (docName.isNotBlank()) _doctorName.value = docName
        if (email.isNotBlank()) _doctorEmail.value = email
        if (pass.isNotBlank()) _doctorPassword.value = pass
        if (loc.isNotBlank()) _pharmacyLocation.value = loc
        if (pName.isNotBlank()) _currentPharmacyName.value = pName
        _loggedInUser.value = PharmacyUser(
            name = _doctorName.value,
            username = _doctorEmail.value.substringBefore("@"),
            phone = "0501234567",
            role = "مدير",
            pharmacyName = _currentPharmacyName.value
        )
        _currentScreen.value = PharmacyScreen.DASHBOARD
        emitMessage("أهلاً بك، ${_doctorName.value}")
    }

    fun logout() {
        _loggedInUser.value = null
        _currentScreen.value = PharmacyScreen.LOGIN
        emitMessage("تم تسجيل الخروج بنجاح")
    }

    fun updateDoctorProfile(
        name: String,
        email: String,
        password: String,
        pharmacyName: String,
        location: String
    ) {
        if (name.isNotBlank()) _doctorName.value = name
        if (email.isNotBlank()) _doctorEmail.value = email
        if (password.isNotBlank()) _doctorPassword.value = password
        if (pharmacyName.isNotBlank()) _currentPharmacyName.value = pharmacyName
        if (location.isNotBlank()) _pharmacyLocation.value = location
        _loggedInUser.value = _loggedInUser.value?.copy(
            name = _doctorName.value,
            pharmacyName = _currentPharmacyName.value
        )
        emitMessage("تم تحديث الملف الشخصي للطبيب بنجاح")
    }

    fun updatePharmacyName(name: String) {
        if (name.isNotBlank()) {
            _currentPharmacyName.value = name
            _loggedInUser.value = _loggedInUser.value?.copy(pharmacyName = name)
        }
    }

    // User feedback (Toast / Snackbar messages)
    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    fun emitMessage(msg: String) {
        viewModelScope.launch {
            _userMessage.emit(msg)
        }
    }

    // Database Flows
    val allProducts: StateFlow<List<Product>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCategories: StateFlow<List<MedicineCategoryEntity>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<PharmacyUser>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allInvoices: StateFlow<List<SaleInvoice>> = repository.allInvoices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalRevenue: StateFlow<Double?> = repository.totalRevenue
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalInvoiceCount: StateFlow<Int> = repository.totalInvoiceCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // ==========================================
    // 1. Smart Drug Alternatives Engine
    // ==========================================
    private val _selectedProductForAlternatives = MutableStateFlow<Product?>(null)
    val selectedProductForAlternatives: StateFlow<Product?> = _selectedProductForAlternatives.asStateFlow()

    private val _drugAlternatives = MutableStateFlow<List<Product>>(emptyList())
    val drugAlternatives: StateFlow<List<Product>> = _drugAlternatives.asStateFlow()

    fun loadDrugAlternatives(product: Product) {
        _selectedProductForAlternatives.value = product
        viewModelScope.launch {
            val alts = repository.getDrugAlternatives(product.activeIngredient, product.id)
            _drugAlternatives.value = alts
        }
    }

    fun clearDrugAlternatives() {
        _selectedProductForAlternatives.value = null
        _drugAlternatives.value = emptyList()
    }

    fun replaceCartItemWithAlternative(oldProduct: Product, alternative: Product) {
        val current = _cartItems.value.toMutableList()
        val idx = current.indexOfFirst { it.product.id == oldProduct.id }
        if (idx >= 0) {
            val oldQty = current[idx].quantity
            current[idx] = CartItem(
                product = alternative,
                quantity = minOf(oldQty, alternative.quantity),
                dosageInstruction = current[idx].dosageInstruction,
                stickerLabel = current[idx].stickerLabel
            )
            _cartItems.value = current
            emitMessage("تم استبدال '${oldProduct.name}' بالبديل المتوفر '${alternative.name}'")
        } else {
            addToCart(alternative)
        }
        clearDrugAlternatives()
        checkCartInteractions()
    }

    // ==========================================
    // 2. Drug Interaction Safety Checker
    // ==========================================
    private val _cartInteractions = MutableStateFlow<List<DrugInteractionEntity>>(emptyList())
    val cartInteractions: StateFlow<List<DrugInteractionEntity>> = _cartInteractions.asStateFlow()

    private fun checkCartInteractions() {
        val ingredients = _cartItems.value.map { it.product.activeIngredient }.distinct()
        if (ingredients.size < 2) {
            _cartInteractions.value = emptyList()
            return
        }
        viewModelScope.launch {
            val found = repository.checkInteractions(ingredients)
            _cartInteractions.value = found
        }
    }

    // ==========================================
    // 3. Smart Dosage & Instructions
    // ==========================================
    suspend fun getSuggestedDosage(product: Product): MedicineDosageEntity? {
        return repository.getDosageForMedicine(product.id)
            ?: repository.getDosageForIngredient(product.activeIngredient)
    }

    fun calculatePediatricDose(dosePerKg: Double, weightKg: Double): String {
        if (dosePerKg <= 0.0 || weightKg <= 0.0) return "جرعة الأطفال غير محددة أو تؤخذ حسب إرشاد الطبيب"
        val totalDoseMg = dosePerKg * weightKg
        return String.format(Locale.US, "الجرعة المحسوبة: %.1f ملجم لكل جرعة (بمعدل %.1f ملجم/كجم لوزن %.1f كجم)", totalDoseMg, dosePerKg, weightKg)
    }

    fun updateCartItemDosage(productId: Long, instruction: String, sticker: String) {
        val current = _cartItems.value.toMutableList()
        val idx = current.indexOfFirst { it.product.id == productId }
        if (idx >= 0) {
            current[idx] = current[idx].copy(
                dosageInstruction = instruction,
                stickerLabel = sticker
            )
            _cartItems.value = current
            emitMessage("تم تحديث تعليمات وملصق الجرعة للصنف")
        }
    }

    // ==========================================
    // 4. Customers & Credit Ledger (الدفع الآجل)
    // ==========================================
    val allCustomers: StateFlow<List<CustomerEntity>> = repository.allCustomers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedCustomerForLedger = MutableStateFlow<CustomerEntity?>(null)
    val selectedCustomerForLedger: StateFlow<CustomerEntity?> = _selectedCustomerForLedger.asStateFlow()

    private val _customerLedgerEntries = MutableStateFlow<List<CustomerLedgerEntity>>(emptyList())
    val customerLedgerEntries: StateFlow<List<CustomerLedgerEntity>> = _customerLedgerEntries.asStateFlow()

    fun selectCustomerForLedger(customer: CustomerEntity) {
        _selectedCustomerForLedger.value = customer
        viewModelScope.launch {
            repository.getCustomerLedger(customer.id).collect {
                _customerLedgerEntries.value = it
            }
        }
    }

    fun clearSelectedCustomerForLedger() {
        _selectedCustomerForLedger.value = null
        _customerLedgerEntries.value = emptyList()
    }

    fun saveCustomer(
        id: Long = 0,
        name: String,
        phone: String,
        creditLimit: Double,
        notes: String
    ) {
        viewModelScope.launch {
            val customer = CustomerEntity(
                id = id,
                name = name.trim(),
                phone = phone.trim(),
                currentBalance = if (id > 0) (_selectedCustomerForLedger.value?.currentBalance ?: 0.0) else 0.0,
                creditLimit = creditLimit,
                notes = notes.trim()
            )
            val newId = repository.saveCustomer(customer)
            emitMessage("تم حفظ بيانات العميل '${customer.name}' بنجاح")
            if (_selectedCustomerForLedger.value?.id == id) {
                _selectedCustomerForLedger.value = customer.copy(id = newId)
            }
        }
    }

    fun recordCustomerPayment(customerId: Long, amount: Double, note: String) {
        if (amount <= 0.0) {
            emitMessage("يرجى إدخال مبلغ سداد صحيح")
            return
        }
        viewModelScope.launch {
            repository.recordCustomerPayment(customerId, amount, note)
            emitMessage("تم تسجيل سداد مبلغ $amount ر.س بنجاح")
            // Refresh customer
            val updated = repository.getCustomerById(customerId)
            if (updated != null && _selectedCustomerForLedger.value?.id == customerId) {
                _selectedCustomerForLedger.value = updated
            }
        }
    }

    // ==========================================
    // 5. Refill Reminders & Smart Purchase Orders
    // ==========================================
    val allRefillReminders: StateFlow<List<RefillReminderEntity>> = repository.allRefillReminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveRefillReminder(
        id: Long = 0,
        customerId: Long = 0,
        customerName: String,
        customerPhone: String,
        medicineName: String,
        daysSupply: Int,
        notes: String
    ) {
        viewModelScope.launch {
            val nextDate = System.currentTimeMillis() + (daysSupply.toLong() * 24 * 60 * 60 * 1000)
            val reminder = RefillReminderEntity(
                id = id,
                customerId = customerId,
                customerName = customerName.trim(),
                customerPhone = customerPhone.trim(),
                medicineName = medicineName.trim(),
                lastRefillDate = System.currentTimeMillis(),
                daysSupply = daysSupply,
                nextRefillDate = nextDate,
                isContacted = false,
                notes = notes.trim()
            )
            repository.saveRefillReminder(reminder)
            emitMessage("تمت جدولة موعد التذكير بالدواء للعميل $customerName")
        }
    }

    fun markReminderContacted(reminder: RefillReminderEntity) {
        viewModelScope.launch {
            repository.markReminderContacted(reminder)
            emitMessage("تم تحديث حالة التذكير للعميل: تم التواصل بنجاح")
        }
    }

    fun deleteRefillReminder(reminder: RefillReminderEntity) {
        viewModelScope.launch {
            repository.deleteRefillReminder(reminder)
            emitMessage("تم حذف موعد التذكير")
        }
    }

    // Burn Rate & Suggested Purchase Orders (أصناف معرضة للنفاد خلال 72 ساعة)
    val suggestedPurchaseOrders: StateFlow<List<SuggestedPurchaseItem>> = combine(allProducts) { productsArray ->
        val products = productsArray[0]
        products.filter { product ->
            val hoursRemaining = if (product.dailyBurnRate > 0) {
                (product.quantity / product.dailyBurnRate) * 24
            } else 999.0
            hoursRemaining <= 72.0 || product.quantity <= product.minStockLevel
        }.map { product ->
            val hours = if (product.dailyBurnRate > 0) {
                (product.quantity / product.dailyBurnRate) * 24
            } else 0.0
            val suggestedQty = ((product.dailyBurnRate * 14).toInt() + product.minStockLevel).coerceAtLeast(10)
            SuggestedPurchaseItem(
                product = product,
                hoursRemaining = hours,
                suggestedQuantity = suggestedQty,
                preferredSupplier = product.preferredSupplier
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ==========================================
    // Cashier & Sales Logic
    // ==========================================
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _cashierSearch = MutableStateFlow("")
    val cashierSearch: StateFlow<String> = _cashierSearch.asStateFlow()
    val cashierSearchQuery: StateFlow<String> = _cashierSearch.asStateFlow()

    private val _cashierCategory = MutableStateFlow("الكل")
    val cashierCategory: StateFlow<String> = _cashierCategory.asStateFlow()

    fun setCashierSearch(query: String) {
        _cashierSearch.value = query
    }

    fun setCashierCategory(category: String) {
        _cashierCategory.value = category
    }

    fun addToCart(product: Product) {
        if (product.quantity <= 0) {
            emitMessage("عفواً، الصنف '${product.name}' نفد من المخزون! جاري البحث عن البدائل المتطابقة...")
            loadDrugAlternatives(product)
            return
        }
        val currentList = _cartItems.value.toMutableList()
        val index = currentList.indexOfFirst { it.product.id == product.id }
        if (index >= 0) {
            val existing = currentList[index]
            if (existing.quantity >= product.quantity) {
                emitMessage("الكمية المطلوبة تتجاوز المتوفر في المخزون (${product.quantity})")
                return
            }
            currentList[index] = existing.copy(quantity = existing.quantity + 1)
        } else {
            // Auto fetch suggested dosage instruction
            viewModelScope.launch {
                val dosage = getSuggestedDosage(product)
                val defaultInstruction = dosage?.adultDosageText ?: "حسب إرشادات الطبيب أو الصيدلي"
                val sticker = "دواء: ${product.name}\nالمادة: ${product.activeIngredient}\nالجرعة: $defaultInstruction"
                val updated = _cartItems.value.toMutableList()
                val idx = updated.indexOfFirst { it.product.id == product.id }
                if (idx >= 0) {
                    updated[idx] = updated[idx].copy(
                        dosageInstruction = defaultInstruction,
                        stickerLabel = sticker
                    )
                    _cartItems.value = updated
                }
            }
            currentList.add(
                CartItem(
                    product = product,
                    quantity = 1,
                    dosageInstruction = "حسب إرشادات الطبيب أو الصيدلي",
                    stickerLabel = "دواء: ${product.name} - يؤخذ بانتظام"
                )
            )
        }
        _cartItems.value = currentList
        checkCartInteractions()
    }

    suspend fun getProductByBarcode(barcode: String): Product? {
        val cleanBarcode = barcode.trim()
        val inMemory = allProducts.value.firstOrNull { it.barcode.equals(cleanBarcode, ignoreCase = true) }
        return inMemory ?: repository.getProductByBarcode(cleanBarcode)
    }

    fun addProductToCartByBarcode(barcode: String, onResult: (Product?) -> Unit = {}) {
        viewModelScope.launch {
            val product = getProductByBarcode(barcode)
            if (product != null) {
                if (product.quantity > 0) {
                    addToCart(product)
                    emitMessage("تم مسح وإضافة '${product.name}' إلى السلة بنجاح ✓")
                } else {
                    emitMessage("الصنف '${product.name}' نفد من المخزون! جاري عرض البدائل...")
                    loadDrugAlternatives(product)
                }
                onResult(product)
            } else {
                emitMessage("لم يتم العثور على صنف برقم الباركود: $barcode")
                onResult(null)
            }
        }
    }

    fun updateCartItemQuantity(productId: Long, delta: Int) {
        val currentList = _cartItems.value.toMutableList()
        val index = currentList.indexOfFirst { it.product.id == productId }
        if (index >= 0) {
            val item = currentList[index]
            val newQty = item.quantity + delta
            if (newQty <= 0) {
                currentList.removeAt(index)
            } else if (newQty > item.product.quantity) {
                emitMessage("الكمية المتاحة في المخزون هي ${item.product.quantity}")
                return
            } else {
                currentList[index] = item.copy(quantity = newQty)
            }
            _cartItems.value = currentList
            checkCartInteractions()
        }
    }

    fun removeFromCart(productId: Long) {
        _cartItems.value = _cartItems.value.filter { it.product.id != productId }
        checkCartInteractions()
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        _cartInteractions.value = emptyList()
    }

    // Complete Sale (Cash, Card, or Credit Sale with Customer limit check)
    fun completeSale(
        paymentMethod: String,
        selectedCustomer: CustomerEntity? = null
    ): Boolean {
        val currentCart = _cartItems.value
        if (currentCart.isEmpty()) {
            emitMessage("سلة البيع فارغة!")
            return false
        }

        val total = currentCart.sumOf { it.totalPrice }
        val isCredit = paymentMethod.contains("آجل") || paymentMethod.contains("حساب العميل")

        if (isCredit) {
            if (selectedCustomer == null) {
                emitMessage("يرجى اختيار العميل لإتمام عملية البيع الآجل (الدين)!")
                return false
            }
            val expectedBalance = selectedCustomer.currentBalance + total
            if (expectedBalance > selectedCustomer.creditLimit) {
                emitMessage("تنبيه: المبلغ الإجمالي ($total ر.س) يتجاوز السقف الائتماني للعميل (${selectedCustomer.creditLimit} ر.س)!")
                return false
            }
        }

        viewModelScope.launch {
            val invNumber = "INV-" + (10000 + (Math.random() * 89999).toInt())
            val count = currentCart.sumOf { it.quantity }

            val invoice = SaleInvoice(
                invoiceNumber = invNumber,
                timestamp = System.currentTimeMillis(),
                totalAmount = total,
                itemCount = count,
                paymentMethod = paymentMethod,
                cashierName = _loggedInUser.value?.name ?: "الكاشير",
                customerId = selectedCustomer?.id,
                customerName = selectedCustomer?.name ?: "",
                customerPhone = selectedCustomer?.phone ?: "",
                isCreditSale = isCredit
            )

            val saleItems = currentCart.map {
                SaleItem(
                    invoiceId = 0,
                    productId = it.product.id,
                    productName = it.product.name,
                    quantity = it.quantity,
                    unitPrice = it.product.unitPrice,
                    totalPrice = it.totalPrice,
                    dosageInstructions = it.dosageInstruction,
                    stickerLabel = it.stickerLabel
                )
            }

            repository.processSale(invoice, saleItems, selectedCustomer?.id)
            _cartItems.value = emptyList()
            _cartInteractions.value = emptyList()

            // مزامنة تلقائية لحظية لعملية البيع إلى خادم MySQL
            if (_mySqlConfig.value.isEnabled) {
                viewModelScope.launch(Dispatchers.IO) {
                    try {
                        mySqlRemoteRepository.insertSale(invoice, saleItems)
                        loadMySqlSalesAnalytics(forceRemote = true)
                    } catch (e: Exception) {
                        android.util.Log.e("PharmacyViewModel", "Auto sync sale to MySQL failed: ${e.message}")
                    }
                }
            }

            val successMsg = if (isCredit) {
                "تم تسجيل البيع الآجل بنجاح وقيد المبلغ على حساب العميل '${selectedCustomer?.name}'!"
            } else {
                "تم تسجيل عملية البيع بنجاح! رقم الفاتورة: $invNumber"
            }
            emitMessage(successMsg)
        }
        return true
    }

    // ==========================================
    // Inventory & Products Management
    // ==========================================
    private val _inventorySearch = MutableStateFlow("")
    val inventorySearch: StateFlow<String> = _inventorySearch.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow("الكل")
    val selectedCategoryFilter: StateFlow<String> = _selectedCategoryFilter.asStateFlow()

    fun setInventorySearch(query: String) {
        _inventorySearch.value = query
    }

    fun setCategoryFilter(category: String) {
        _selectedCategoryFilter.value = category
    }

    private val _editingProduct = MutableStateFlow<Product?>(null)
    val editingProduct: StateFlow<Product?> = _editingProduct.asStateFlow()

    fun startEditingProduct(product: Product) {
        _editingProduct.value = product
        navigateTo(PharmacyScreen.NEW_STOCK_ENTRY)
    }

    fun clearEditingProduct() {
        _editingProduct.value = null
    }

    fun saveProduct(
        id: Long = 0,
        name: String,
        manufacturer: String,
        activeIngredient: String,
        unit: String,
        expiryDate: String,
        unitPrice: Double,
        wholesalePrice: Double,
        barcode: String,
        quantity: Int,
        category: String,
        dosageForm: String = "أقراص عادية",
        strength: String = "",
        preferredSupplier: String = "الشركة الوطنية للدواء",
        usageInstructions: String = "",
        contraindications: String = "",
        targetAgeGroup: String = "",
        drugInteractions: String = ""
    ): Boolean {
        if (name.isBlank()) {
            emitMessage("يرجى إدخال اسم الصنف")
            return false
        }
        if (unitPrice <= 0) {
            emitMessage("يرجى إدخال سعر بيع صحيح")
            return false
        }
        val safeBarcode = if (barcode.isBlank()) {
            "628" + (100000000 + (Math.random() * 899999999).toLong()).toString()
        } else barcode

        val product = Product(
            id = id,
            name = name.trim(),
            manufacturer = manufacturer.ifBlank { "عام" }.trim(),
            activeIngredient = activeIngredient.ifBlank { "غير محدد" }.trim(),
            dosageForm = dosageForm.ifBlank { "أقراص عادية" }.trim(),
            strength = strength.trim(),
            unit = unit.ifBlank { "علبة" }.trim(),
            expiryDate = expiryDate.ifBlank { "2027-01-01" }.trim(),
            unitPrice = unitPrice,
            wholesalePrice = if (wholesalePrice > 0) wholesalePrice else (unitPrice * 0.75),
            barcode = safeBarcode.trim(),
            quantity = quantity.coerceAtLeast(0),
            category = category.ifBlank { "عام" },
            preferredSupplier = preferredSupplier.ifBlank { "المستودع الرئيسي" },
            usageInstructions = usageInstructions.trim(),
            contraindications = contraindications.trim(),
            targetAgeGroup = targetAgeGroup.trim(),
            drugInteractions = drugInteractions.trim(),
            updatedAt = System.currentTimeMillis()
        )

        viewModelScope.launch {
            if (id > 0) {
                repository.updateProduct(product)
                emitMessage("تم تحديث الصنف '${product.name}' بنجاح")
            } else {
                repository.insertProduct(product)
                emitMessage("تمت إضافة الصنف '${product.name}' إلى المخزون بنجاح")
            }
            _editingProduct.value = null
            navigateTo(PharmacyScreen.INVENTORY)

            // مزامنة تلقائية فورية مع قاعدة بيانات MySQL الأساسية
            if (_mySqlConfig.value.isEnabled && _mySqlConfig.value.isAutoSyncEnabled) {
                viewModelScope.launch(Dispatchers.IO) {
                    MySqlDatabaseSyncManager.pushSingleProductToMySql(_mySqlConfig.value, product, "upsert")
                    triggerAutoSyncToMySql("إدخال/تعديل صنف: ${product.name}", silent = true)
                }
            }
        }
        return true
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product)
            emitMessage("تم حذف الصنف '${product.name}' من المخزون")

            // مزامنة تلقائية فورية مع قاعدة بيانات MySQL الأساسية
            if (_mySqlConfig.value.isEnabled && _mySqlConfig.value.isAutoSyncEnabled) {
                viewModelScope.launch(Dispatchers.IO) {
                    MySqlDatabaseSyncManager.pushSingleProductToMySql(_mySqlConfig.value, product, "delete")
                    triggerAutoSyncToMySql("حذف صنف: ${product.name}", silent = true)
                }
            }
        }
    }

    fun adjustProductStock(product: Product, delta: Int) {
        viewModelScope.launch {
            val newQty = (product.quantity + delta).coerceAtLeast(0)
            repository.updateProductQuantity(product.id, newQty)
            emitMessage("تم تعديل كمية '${product.name}' إلى $newQty")

            // مزامنة تلقائية فورية مع قاعدة بيانات MySQL الأساسية
            if (_mySqlConfig.value.isEnabled && _mySqlConfig.value.isAutoSyncEnabled) {
                viewModelScope.launch(Dispatchers.IO) {
                    val updated = product.copy(quantity = newQty)
                    MySqlDatabaseSyncManager.pushSingleProductToMySql(_mySqlConfig.value, updated, "upsert")
                    triggerAutoSyncToMySql("تعديل كمية صنف: ${product.name}", silent = true)
                }
            }
        }
    }

    // ==========================================
    // Users Management
    // ==========================================
    fun saveUser(
        id: Long = 0,
        name: String,
        username: String,
        phone: String,
        role: String,
        isActive: Boolean = true
    ): Boolean {
        if (name.isBlank() || username.isBlank()) {
            emitMessage("يرجى ملء الاسم واسم المستخدم")
            return false
        }
        val user = PharmacyUser(
            id = id,
            name = name.trim(),
            username = username.trim(),
            phone = phone.trim(),
            role = role,
            pharmacyName = _currentPharmacyName.value,
            isActive = isActive
        )
        viewModelScope.launch {
            if (id > 0) {
                repository.updateUser(user)
                emitMessage("تم تحديث بيانات المستخدم '${user.name}'")
            } else {
                repository.insertUser(user)
                emitMessage("تمت إضافة المستخدم '${user.name}' بنجاح")
            }
        }
        return true
    }

    fun toggleUserStatus(user: PharmacyUser) {
        viewModelScope.launch {
            val updated = user.copy(isActive = !user.isActive)
            repository.updateUser(updated)
            val status = if (updated.isActive) "تفعيل" else "تعطيل"
            emitMessage("تم $status حساب المستخدم '${user.name}'")
        }
    }

    fun deleteUser(user: PharmacyUser) {
        viewModelScope.launch {
            repository.deleteUser(user)
            emitMessage("تم حذف المستخدم '${user.name}'")
        }
    }

    fun isProductExpired(expiryDateStr: String): Boolean {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = sdf.parse(expiryDateStr)
            date != null && date.before(Date())
        } catch (_: Exception) {
            false
        }
    }

    // ==========================================
    // Returns & Revisions (إدارة المراجعات والمرتجعات)
    // ==========================================
    val allSaleReturns: StateFlow<List<SaleReturn>> = repository.allSaleReturns
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalReturnsAmount: StateFlow<Double?> = repository.totalReturnsAmount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    /**
     * دالة ViewModel لربط شاشة المرتجعات بقاعدة بيانات المخزون (Room):
     * - توثيق العملية في سجل حركات المرتجعات (sale_returns)
     * - زيادة رصيد الصنف في جدول المخزون (products)
     * - إشعار المستخدم والمشغل بتحديث الرصيد الجديد
     */
    fun processSaleReturn(
        invoiceNumber: String,
        product: Product?,
        itemName: String,
        expiryDate: String,
        amount: Double,
        smallestUnit: String,
        returnedQuantity: Int,
        returnReason: String = "طلب العميل",
        refundMethod: String = "نقدي (كاش)"
    ) {
        viewModelScope.launch {
            val returnRecord = SaleReturn(
                invoiceNumber = invoiceNumber.trim(),
                productId = product?.id ?: 0,
                itemName = itemName.trim(),
                barcode = product?.barcode ?: "",
                expiryDate = expiryDate.trim(),
                amount = amount,
                smallestUnit = smallestUnit,
                returnedQuantity = returnedQuantity,
                returnReason = returnReason,
                refundMethod = refundMethod,
                pharmacistName = _loggedInUser.value?.name ?: _doctorName.value
            )

            // تنفيذ العملية الشاملة عبر الـ Repository لتوثيق الحركة وزيادة رصيد الصنف في Room
            val (_, updatedProduct) = repository.processSaleReturnTransaction(
                saleReturn = returnRecord,
                targetProduct = product
            )

            // مزامنة فورية لحركة المرتجع وتحديث رصيد الصنف في قاعدة بيانات MySQL الحقيقية إذا كانت مفعلة
            if (_mySqlConfig.value.isEnabled) {
                viewModelScope.launch {
                    val pushed = MySqlDatabaseSyncManager.pushSingleReturnToMySql(
                        config = _mySqlConfig.value,
                        saleReturn = returnRecord,
                        updatedProduct = updatedProduct
                    )
                    triggerAutoSyncToMySql("تسجيل مرتجع: ${returnRecord.itemName}", silent = true)
                    if (pushed) {
                        emitMessage("✓ تم ترحيل المرتجع وتحديث الرصيد في قاعدة بيانات MySQL تلقائياً 🐬")
                    }
                }
            }
        }
    }

    fun deleteSaleReturn(saleReturn: SaleReturn) {
        viewModelScope.launch {
            repository.deleteSaleReturn(saleReturn)
            emitMessage("تم حذف سجل المرتجع")

            if (_mySqlConfig.value.isEnabled && _mySqlConfig.value.isAutoSyncEnabled) {
                triggerAutoSyncToMySql("حذف سجل مرتجع: ${saleReturn.itemName}", silent = true)
            }
        }
    }
}
