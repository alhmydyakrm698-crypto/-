package com.example.data

/**
 * إعدادات الاتصال بقاعدة بيانات MySQL
 * تدعم الاتصال بالخادم المحلي (Localhost / XAMPP / WampServer) أو السحابي
 */
data class MySqlConfig(
    val host: String = "192.168.1.100", // عنوان خادم MySQL أو IP الشبكة المحلية
    val port: Int = 3306,
    val databaseName: String = "saydali_pharmacy_db",
    val username: String = "root",
    val password: String = "",
    val apiUrl: String = "http://192.168.1.100/saydali_api", // عنوان بوابة REST API / PHP
    val isEnabled: Boolean = true,
    val isAutoSyncEnabled: Boolean = true,
    val autoSyncIntervalMinutes: Int = 5,
    val lastSyncTime: Long = 0L,
    val lastSyncStatus: String = "تزامن تلقائي نشط مع خادم MySQL",
    val lastPingMs: Long = 0L
)

data class MySqlTestResult(
    val isConnected: Boolean,
    val message: String,
    val latencyMs: Long = 0L,
    val connectionType: String = "Socket / Port 3306",
    val testedAt: Long = System.currentTimeMillis()
)

sealed class MySqlSyncResult {
    data class Success(val message: String, val syncedProducts: Int, val syncedReturns: Int, val timestamp: Long) : MySqlSyncResult()
    data class Failure(val error: String) : MySqlSyncResult()
}
