package com.example.util

import android.content.Context
import android.util.Log
import com.example.data.PharmacyDatabase
import com.google.firebase.FirebaseApp
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class BackupResult {
    data class Success(val message: String, val isCloud: Boolean, val timestamp: Long) : BackupResult()
    data class Failure(val error: String) : BackupResult()
}

enum class BackupFrequency(val titleAr: String, val intervalMillis: Long) {
    EVERY_12_HOURS("كل 12 ساعة (وردية)", 12 * 60 * 60 * 1000L),
    DAILY("يومياً (نهاية اليوم)", 24 * 60 * 60 * 1000L),
    WEEKLY("أسبوعياً (كل جمعة)", 7 * 24 * 60 * 60 * 1000L);

    companion object {
        fun fromName(name: String?): BackupFrequency {
            return entries.firstOrNull { it.name == name } ?: DAILY
        }
    }
}

object PharmacyBackupManager {

    private const val TAG = "PharmacyBackup"
    private const val DB_NAME = "saydali_pharmacy_db"

    /**
     * هل تم تهيئة Firebase بنجاح في التطبيق؟
     */
    fun isFirebaseConfigured(context: Context): Boolean {
        return try {
            FirebaseApp.getApps(context).isNotEmpty()
        } catch (_: Exception) {
            false
        }
    }

    /**
     * تنفيذ نسخ احتياطي فوري لقاعدة بيانات Room وتصديرها محلياً وسحابياً إذا توفر Firebase Storage
     */
    suspend fun performBackup(
        context: Context,
        database: PharmacyDatabase,
        uploadToCloud: Boolean = true
    ): BackupResult = withContext(Dispatchers.IO) {
        try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val backupsDir = File(context.filesDir, "backups").apply { if (!exists()) mkdirs() }

            // 1. نسخ ملف قاعدة بيانات Room SQLite المباشر
            val dbFile = context.getDatabasePath(DB_NAME)
            val backupDbFile = File(backupsDir, "saydali_backup_$timeStamp.db")

            if (dbFile.exists()) {
                // Checkpoint the SQLite WAL journal to ensure all data is in the main file
                try {
                    val db = database.openHelper.writableDatabase
                    db.query("PRAGMA wal_checkpoint(FULL)").close()
                } catch (e: Exception) {
                    Log.w(TAG, "WAL checkpoint warning: ${e.message}")
                }

                FileInputStream(dbFile).use { input ->
                    FileOutputStream(backupDbFile).use { output ->
                        input.copyTo(output)
                    }
                }
            }

            // 2. إنشاء نسخة احتياطية بصيغة JSON مقروءة لكافة السجلات الحساسة
            val backupJsonFile = File(backupsDir, "saydali_export_$timeStamp.json")
            val dao = database.pharmacyDao()

            val products = dao.getAllProductsStatic()
            val invoices = dao.getAllInvoicesStatic()
            val customers = dao.getAllCustomersStatic()
            val ledgers = dao.getAllLedgerEntriesStatic()

            val rootJson = JSONObject().apply {
                put("app", "Saydali Smart Pharmacy")
                put("version", "1.0.0")
                put("createdAt", System.currentTimeMillis())
                put("formattedDate", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date()))

                val productsArray = JSONArray()
                products.forEach { p ->
                    productsArray.put(JSONObject().apply {
                        put("id", p.id)
                        put("name", p.name)
                        put("manufacturer", p.manufacturer)
                        put("activeIngredient", p.activeIngredient)
                        put("unit", p.unit)
                        put("barcode", p.barcode)
                        put("quantity", p.quantity)
                        put("unitPrice", p.unitPrice)
                        put("wholesalePrice", p.wholesalePrice)
                        put("expiryDate", p.expiryDate)
                        put("category", p.category)
                        put("usageInstructions", p.usageInstructions)
                        put("contraindications", p.contraindications)
                        put("targetAgeGroup", p.targetAgeGroup)
                        put("drugInteractions", p.drugInteractions)
                    })
                }
                put("productsCount", products.size)
                put("products", productsArray)

                val invoicesArray = JSONArray()
                invoices.forEach { inv ->
                    invoicesArray.put(JSONObject().apply {
                        put("id", inv.id)
                        put("invoiceNumber", inv.invoiceNumber)
                        put("totalAmount", inv.totalAmount)
                        put("paymentMethod", inv.paymentMethod)
                        put("cashierName", inv.cashierName)
                        put("timestamp", inv.timestamp)
                    })
                }
                put("invoicesCount", invoices.size)
                put("invoices", invoicesArray)

                put("customersCount", customers.size)
                put("ledgerEntriesCount", ledgers.size)
            }

            backupJsonFile.writeText(rootJson.toString(2))

            // 3. رفع النسخة الاحتياطية إلى Firebase Storage في حال التمكين وتوفر الإعدادات
            var cloudUploaded = false
            if (uploadToCloud && isFirebaseConfigured(context)) {
                try {
                    val storage = FirebaseStorage.getInstance()
                    val storageRef = storage.reference
                    val backupRef = storageRef.child("pharmacy_backups/${backupDbFile.name}")

                    val metadata = StorageMetadata.Builder()
                        .setContentType("application/x-sqlite3")
                        .setCustomMetadata("app", "Saydali")
                        .setCustomMetadata("created_at", timeStamp)
                        .setCustomMetadata("products_count", products.size.toString())
                        .build()

                    val uploadTask = backupRef.putFile(android.net.Uri.fromFile(backupDbFile), metadata)
                    uploadTask.await()
                    cloudUploaded = true
                    Log.i(TAG, "Uploaded DB to Firebase Storage: ${backupRef.path}")
                } catch (e: Exception) {
                    Log.w(TAG, "Firebase Storage upload failed: ${e.message}")
                }
            }

            // Cleanup old local backups (keep last 7 copies)
            val allLocalDbFiles = backupsDir.listFiles { _, name -> name.endsWith(".db") }?.sortedByDescending { it.lastModified() }
            if (allLocalDbFiles != null && allLocalDbFiles.size > 7) {
                allLocalDbFiles.drop(7).forEach { it.delete() }
            }

            if (cloudUploaded) {
                BackupResult.Success(
                    message = "تم حفظ النسخة الاحتياطية ورفعها إلى Firebase Storage بنجاح ☁️ (${products.size} صنف)",
                    isCloud = true,
                    timestamp = System.currentTimeMillis()
                )
            } else {
                BackupResult.Success(
                    message = "تم حفظ النسخة الاحتياطية محلياً في ذاكرة النظام الآمنة 💾 (${products.size} صنف). لربط الرفع المباشر إلى Firebase Storage يرجى ربط google-services.json",
                    isCloud = false,
                    timestamp = System.currentTimeMillis()
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Backup failed: ${e.message}", e)
            BackupResult.Failure(e.localizedMessage ?: "حدث خطأ غير متوقع أثناء عملية النسخ الاحتياطي")
        }
    }
}
