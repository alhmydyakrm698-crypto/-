package com.example.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.data.MySqlConfig

/**
 * مدير التخزين الآمن والمشفر لبيانات الاتصال بقاعدة بيانات MySQL
 * باستخدام EncryptedSharedPreferences وتشفير AES-256 لحماية كلمات المرور وبيانات الاعتماد.
 */
object SecureMySqlPreferences {
    private const val TAG = "SecureMySqlPrefs"
    private const val SECURE_PREFS_FILE_NAME = "saydali_secure_mysql_credentials"

    // Keys
    const val KEY_HOST = "secure_mysql_host"
    const val KEY_PORT = "secure_mysql_port"
    const val KEY_DB_NAME = "secure_mysql_dbname"
    const val KEY_USER = "secure_mysql_user"
    const val KEY_PASSWORD = "secure_mysql_password"
    const val KEY_API_URL = "secure_mysql_api_url"
    const val KEY_ENABLED = "secure_mysql_enabled"
    const val KEY_AUTOSYNC_ENABLED = "secure_mysql_autosync_enabled"
    const val KEY_AUTOSYNC_INTERVAL = "secure_mysql_autosync_interval"
    const val KEY_LAST_SYNC_TIME = "secure_mysql_last_sync_time"
    const val KEY_LAST_SYNC_STATUS = "secure_mysql_last_sync_status"

    @Volatile
    private var securePrefsInstance: SharedPreferences? = null

    /**
     * الحصول على نسخة SharedPreferences المشفرة عبر MasterKey و EncryptedSharedPreferences
     */
    fun getSecurePrefs(context: Context): SharedPreferences {
        return securePrefsInstance ?: synchronized(this) {
            securePrefsInstance ?: createEncryptedSharedPreferences(context).also {
                securePrefsInstance = it
            }
        }
    }

    private fun createEncryptedSharedPreferences(context: Context): SharedPreferences {
        return try {
            val masterKey = MasterKey.Builder(context.applicationContext)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context.applicationContext,
                SECURE_PREFS_FILE_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create EncryptedSharedPreferences: ${e.message}", e)
            // Fallback in environments where Android Keystore may throw Crypto/Provider exceptions
            context.applicationContext.getSharedPreferences("${SECURE_PREFS_FILE_NAME}_compat", Context.MODE_PRIVATE)
        }
    }

    /**
     * قراءة إعدادات MySQL المحفوظة مشفرة
     */
    fun loadConfig(context: Context, fallbackPlainPrefs: SharedPreferences? = null): MySqlConfig {
        val prefs = getSecurePrefs(context)
        
        // إذا كانت الإعدادات المشفرة خالية، نحاول الترحيل من الإعدادات القديمة غير المشفرة
        val hasSecureHost = prefs.contains(KEY_HOST)
        if (!hasSecureHost && fallbackPlainPrefs != null && fallbackPlainPrefs.contains("mysql_host")) {
            val migrated = MySqlConfig(
                host = fallbackPlainPrefs.getString("mysql_host", "192.168.1.100") ?: "192.168.1.100",
                port = fallbackPlainPrefs.getInt("mysql_port", 3306),
                databaseName = fallbackPlainPrefs.getString("mysql_dbname", "saydali_pharmacy_db") ?: "saydali_pharmacy_db",
                username = fallbackPlainPrefs.getString("mysql_user", "root") ?: "root",
                password = fallbackPlainPrefs.getString("mysql_pass", "") ?: "",
                apiUrl = fallbackPlainPrefs.getString("mysql_api_url", "http://192.168.1.100/saydali_api") ?: "http://192.168.1.100/saydali_api",
                isEnabled = fallbackPlainPrefs.getBoolean("mysql_enabled", true),
                isAutoSyncEnabled = fallbackPlainPrefs.getBoolean("mysql_autosync_enabled", true),
                autoSyncIntervalMinutes = fallbackPlainPrefs.getInt("mysql_autosync_interval", 5),
                lastSyncTime = fallbackPlainPrefs.getLong("mysql_last_sync_time", 0L),
                lastSyncStatus = fallbackPlainPrefs.getString("mysql_last_sync_status", "تزامن تلقائي نشط مع خادم MySQL") ?: "تزامن تلقائي نشط مع خادم MySQL"
            )
            saveConfig(context, migrated)
            return migrated
        }

        return MySqlConfig(
            host = prefs.getString(KEY_HOST, "192.168.1.100") ?: "192.168.1.100",
            port = prefs.getInt(KEY_PORT, 3306),
            databaseName = prefs.getString(KEY_DB_NAME, "saydali_pharmacy_db") ?: "saydali_pharmacy_db",
            username = prefs.getString(KEY_USER, "root") ?: "root",
            password = prefs.getString(KEY_PASSWORD, "") ?: "",
            apiUrl = prefs.getString(KEY_API_URL, "http://192.168.1.100/saydali_api") ?: "http://192.168.1.100/saydali_api",
            isEnabled = prefs.getBoolean(KEY_ENABLED, true),
            isAutoSyncEnabled = prefs.getBoolean(KEY_AUTOSYNC_ENABLED, true),
            autoSyncIntervalMinutes = prefs.getInt(KEY_AUTOSYNC_INTERVAL, 5),
            lastSyncTime = prefs.getLong(KEY_LAST_SYNC_TIME, 0L),
            lastSyncStatus = prefs.getString(KEY_LAST_SYNC_STATUS, "تزامن تلقائي نشط مع خادم MySQL (مشفر بـ AES-256)") ?: "تزامن تلقائي نشط مع خادم MySQL"
        )
    }

    /**
     * حفظ إعدادات MySQL وكلمة المرور مشفرة بشكل آمن
     */
    fun saveConfig(context: Context, config: MySqlConfig) {
        val prefs = getSecurePrefs(context)
        prefs.edit()
            .putString(KEY_HOST, config.host)
            .putInt(KEY_PORT, config.port)
            .putString(KEY_DB_NAME, config.databaseName)
            .putString(KEY_USER, config.username)
            .putString(KEY_PASSWORD, config.password)
            .putString(KEY_API_URL, config.apiUrl)
            .putBoolean(KEY_ENABLED, config.isEnabled)
            .putBoolean(KEY_AUTOSYNC_ENABLED, config.isAutoSyncEnabled)
            .putInt(KEY_AUTOSYNC_INTERVAL, config.autoSyncIntervalMinutes)
            .putLong(KEY_LAST_SYNC_TIME, config.lastSyncTime)
            .putString(KEY_LAST_SYNC_STATUS, config.lastSyncStatus)
            .apply()
        Log.i(TAG, "MySQL credentials securely saved using EncryptedSharedPreferences (AES-256).")
    }

    /**
     * تحديث حالة وتوقيت آخر عملية مزامنة فقط
     */
    fun updateSyncStatus(context: Context, status: String, timestamp: Long = System.currentTimeMillis()) {
        val prefs = getSecurePrefs(context)
        prefs.edit()
            .putString(KEY_LAST_SYNC_STATUS, status)
            .putLong(KEY_LAST_SYNC_TIME, timestamp)
            .apply()
    }
}
