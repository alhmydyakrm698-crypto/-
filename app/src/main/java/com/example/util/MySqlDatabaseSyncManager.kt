package com.example.util

import android.content.Context
import android.util.Log
import com.example.data.MySqlConfig
import com.example.data.MySqlSyncResult
import com.example.data.Product
import com.example.data.SaleReturn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.InetSocketAddress
import java.net.Socket
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * مدير المزامنة والربط المباشر مع قاعدة بيانات MySQL (MySqlDatabaseSyncManager)
 * 
 * يتيح:
 * 1. فحص الاتصال الحقيقي بخادم MySQL (عبر Socket المباشر للمنفذ 3306 أو عبر بوابة REST API).
 * 2. مزامنة المخزون (Products) والمرتجعات (Sale Returns) فورياً.
 * 3. إنشاء كود SQL DDL الكامل لإنشاء جداول `products` و `sale_returns` في phpMyAdmin / MySQL Workbench.
 * 4. تصدير ملف `saydali_mysql_schema.sql` محلياً لتثبيته في قاعدة بيانات MySQL.
 */
object MySqlDatabaseSyncManager {

    private const val TAG = "MySqlSyncManager"
    private const val CONNECT_TIMEOUT_MS = 3500

    /**
     * فحص الاتصال الحقيقي بخادم MySQL مع قياس زمن الاستجابة (Latency) ونوع البروتوكول
     */
    suspend fun testMySqlConnectionDetailed(config: MySqlConfig): com.example.data.MySqlTestResult = withContext(Dispatchers.IO) {
        val host = config.host.trim()
        val port = config.port
        val startTime = System.currentTimeMillis()

        if (host.isBlank()) {
            return@withContext com.example.data.MySqlTestResult(
                isConnected = false,
                message = "عنوان خادم MySQL غير محدد (Host is empty)",
                latencyMs = 0L,
                connectionType = "TCP Socket",
                testedAt = System.currentTimeMillis()
            )
        }

        try {
            // محاولة أولى: فحص Socket المباشر على منفذ MySQL (افتراضياً 3306)
            Socket().use { socket ->
                socket.connect(InetSocketAddress(host, port), CONNECT_TIMEOUT_MS)
            }
            val latency = (System.currentTimeMillis() - startTime).coerceAtLeast(1L)
            com.example.data.MySqlTestResult(
                isConnected = true,
                message = "تم الاتصال بنجاح بخادم MySQL عبر المنفذ ($port) في زمن $latency ms",
                latencyMs = latency,
                connectionType = "TCP Direct Socket ($port)",
                testedAt = System.currentTimeMillis()
            )
        } catch (socketEx: Exception) {
            val socketDuration = System.currentTimeMillis() - startTime
            // محاولة ثانية: فحص بوابة الـ REST API / PHP إذا كانت محددة
            val apiUrl = config.apiUrl.trim()
            if (apiUrl.startsWith("http://") || apiUrl.startsWith("https://")) {
                val apiStartTime = System.currentTimeMillis()
                try {
                    val url = URL(if (apiUrl.endsWith("/")) "${apiUrl}ping.php" else "$apiUrl/ping.php")
                    val conn = (url.openConnection() as HttpURLConnection).apply {
                        connectTimeout = CONNECT_TIMEOUT_MS
                        readTimeout = CONNECT_TIMEOUT_MS
                        requestMethod = "GET"
                    }
                    val code = conn.responseCode
                    val latency = (System.currentTimeMillis() - apiStartTime).coerceAtLeast(1L)
                    if (code in 200..299) {
                        return@withContext com.example.data.MySqlTestResult(
                            isConnected = true,
                            message = "تم الاتصال بنجاح ببوابة MySQL REST Gateway ($apiUrl) - كود HTTP: $code في زمن $latency ms",
                            latencyMs = latency,
                            connectionType = "HTTP REST Gateway (ping.php)",
                            testedAt = System.currentTimeMillis()
                        )
                    }
                } catch (_: Exception) {
                    // fall through
                }
            }
            Log.w(TAG, "Connection failed: ${socketEx.message}")
            com.example.data.MySqlTestResult(
                isConnected = false,
                message = "تعذر الوصول لخادم MySQL على ($host:$port): ${socketEx.localizedMessage ?: "انتهت مهلة الانتظار"}. تأكد من تشغيل خدمة MySQL في XAMPP/WAMP والسماح بالمنفذ 3306 في جدار الحماية.",
                latencyMs = socketDuration,
                connectionType = "TCP Socket ($port)",
                testedAt = System.currentTimeMillis()
            )
        }
    }

    /**
     * فحص الاتصال الحقيقي بمنفذ خادم MySQL (Socket Port Test)
     */
    suspend fun testMySqlConnection(config: MySqlConfig): Pair<Boolean, String> {
        val result = testMySqlConnectionDetailed(config)
        return Pair(result.isConnected, result.message)
    }

    /**
     * مزامنة المخزون والمرتجعات مع قاعدة بيانات MySQL
     */
    suspend fun syncInventoryAndReturnsToMySql(
        context: Context,
        config: MySqlConfig,
        products: List<Product>,
        returns: List<SaleReturn>
    ): MySqlSyncResult = withContext(Dispatchers.IO) {
        if (!config.isEnabled) {
            return@withContext MySqlSyncResult.Failure("ميزة الربط بقاعدة بيانات MySQL معطلة في الإعدادات")
        }

        // 1. محاولة المزامنة المباشرة عن بُعد عبر مكتبة تعريف MySQL JDBC (MySqlRemoteRepository)
        try {
            val remoteRepo = com.example.data.MySqlRemoteRepository(context) { config }
            remoteRepo.ensureRemoteTablesExist()
            val prodResult = remoteRepo.batchSyncProducts(products)
            val returnResult = remoteRepo.batchSyncReturns(returns)

            if (prodResult.isSuccess && returnResult.isSuccess) {
                val timeNow = System.currentTimeMillis()
                val message = "✓ تمت المزامنة المباشرة عبر JDBC (mysql-connector) مع خادم MySQL بنجاح (${products.size} صنف و ${returns.size} حركة)"
                Log.i(TAG, "Direct MySQL JDBC sync success: $message")
                return@withContext MySqlSyncResult.Success(
                    message = message,
                    syncedProducts = products.size,
                    syncedReturns = returns.size,
                    timestamp = timeNow
                )
            }
        } catch (jdbcEx: Exception) {
            Log.w(TAG, "Direct MySQL JDBC sync attempt failed, trying REST gateway fallback: ${jdbcEx.message}")
        }

        try {
            // بناء حمولة JSON شاملة لبيانات المخزون والمرتجعات (كخيار احتياطي عبر Gateway)
            val payload = JSONObject().apply {
                put("database", config.databaseName)
                put("timestamp", System.currentTimeMillis())
                put("sync_date", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date()))

                // 1. جدول المخزون (Products / Inventory)
                val productsArray = JSONArray()
                products.forEach { p ->
                    val pObj = JSONObject().apply {
                        put("id", p.id)
                        put("name", p.name)
                        put("barcode", p.barcode)
                        put("active_ingredient", p.activeIngredient)
                        put("dosage_form", p.dosageForm)
                        put("strength", p.strength)
                        put("unit", p.unit)
                        put("quantity", p.quantity)
                        put("min_stock_level", p.minStockLevel)
                        put("max_stock_level", p.maxStockLevel)
                        put("unit_price", p.unitPrice)
                        put("wholesale_price", p.wholesalePrice)
                        put("expiry_date", p.expiryDate)
                        put("batch_number", p.batchNumber)
                        put("storage_location", p.storageLocation)
                        put("preferred_supplier", p.preferredSupplier)
                        put("category", p.category)
                        put("updated_at", p.updatedAt)
                    }
                    productsArray.put(pObj)
                }
                put("products", productsArray)

                // 2. جدول المرتجعات (Sale Returns)
                val returnsArray = JSONArray()
                returns.forEach { r ->
                    val rObj = JSONObject().apply {
                        put("id", r.id)
                        put("invoice_number", r.invoiceNumber)
                        put("product_id", r.productId)
                        put("item_name", r.itemName)
                        put("barcode", r.barcode)
                        put("expiry_date", r.expiryDate)
                        put("amount", r.amount)
                        put("smallest_unit", r.smallestUnit)
                        put("returned_quantity", r.returnedQuantity)
                        put("return_reason", r.returnReason)
                        put("refund_method", r.refundMethod)
                        put("pharmacist_name", r.pharmacistName)
                        put("timestamp", r.timestamp)
                    }
                    returnsArray.put(rObj)
                }
                put("sale_returns", returnsArray)
            }

            // إرسال البيانات إلى بوابة MySQL API
            val apiUrl = config.apiUrl.trim()
            val syncUrl = if (apiUrl.endsWith("/")) "${apiUrl}sync_all.php" else "$apiUrl/sync_all.php"

            val url = URL(syncUrl)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 5000
                readTimeout = 8000
                requestMethod = "POST"
                doOutput = true
                doInput = true
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                setRequestProperty("Accept", "application/json")
                if (config.username.isNotBlank()) {
                    setRequestProperty("X-Database-User", config.username)
                }
            }

            OutputStreamWriter(connection.outputStream, "UTF-8").use { writer ->
                writer.write(payload.toString())
                writer.flush()
            }

            val responseCode = connection.responseCode
            if (responseCode in 200..299) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream, "UTF-8"))
                val responseText = reader.readText()
                reader.close()

                val timeNow = System.currentTimeMillis()
                val message = "✓ تمت مزامنة ${products.size} صنف مخزون و ${returns.size} حركة مرتجع مع قاعدة بيانات MySQL بنجاح"
                Log.i(TAG, "MySQL sync success: $responseText")
                MySqlSyncResult.Success(
                    message = message,
                    syncedProducts = products.size,
                    syncedReturns = returns.size,
                    timestamp = timeNow
                )
            } else {
                MySqlSyncResult.Failure("فشل الاستجابة من خادم MySQL API (HTTP $responseCode)")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing to MySQL: ${e.message}", e)
            MySqlSyncResult.Failure("تعذر إتمام المزامنة مع MySQL: ${e.localizedMessage ?: e.message}")
        }
    }

    /**
     * إرسال حركة مرتجع فردية فورية إلى خادم MySQL عند اعتمادها في الشاشة
     */
    suspend fun pushSingleReturnToMySql(
        config: MySqlConfig,
        saleReturn: SaleReturn,
        updatedProduct: Product?
    ): Boolean = withContext(Dispatchers.IO) {
        if (!config.isEnabled) return@withContext false
        try {
            val apiUrl = config.apiUrl.trim()
            val url = URL(if (apiUrl.endsWith("/")) "${apiUrl}record_return.php" else "$apiUrl/record_return.php")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 3000
                readTimeout = 4000
                requestMethod = "POST"
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            }

            val payload = JSONObject().apply {
                put("database", config.databaseName)
                put("invoice_number", saleReturn.invoiceNumber)
                put("product_id", saleReturn.productId)
                put("item_name", saleReturn.itemName)
                put("barcode", saleReturn.barcode)
                put("expiry_date", saleReturn.expiryDate)
                put("amount", saleReturn.amount)
                put("smallest_unit", saleReturn.smallestUnit)
                put("returned_quantity", saleReturn.returnedQuantity)
                put("return_reason", saleReturn.returnReason)
                put("refund_method", saleReturn.refundMethod)
                put("pharmacist_name", saleReturn.pharmacistName)
                put("timestamp", saleReturn.timestamp)

                if (updatedProduct != null) {
                    val pObj = JSONObject().apply {
                        put("id", updatedProduct.id)
                        put("new_stock_quantity", updatedProduct.quantity)
                    }
                    put("updated_product", pObj)
                }
            }

            OutputStreamWriter(conn.outputStream, "UTF-8").use {
                it.write(payload.toString())
                it.flush()
            }
            conn.responseCode in 200..299
        } catch (e: Exception) {
            Log.w(TAG, "Failed to push single return to MySQL: ${e.message}")
            false
        }
    }

    /**
     * إرسال صنف مفرد أو تحديثه أو حذفه في قاعدة بيانات MySQL فورياً عند إدخاله أو تعديله في المخزون
     */
    suspend fun pushSingleProductToMySql(
        config: MySqlConfig,
        product: Product,
        action: String = "upsert" // "upsert" or "delete"
    ): Boolean = withContext(Dispatchers.IO) {
        if (!config.isEnabled) return@withContext false
        try {
            val apiUrl = config.apiUrl.trim()
            val url = URL(if (apiUrl.endsWith("/")) "${apiUrl}manage_product.php" else "$apiUrl/manage_product.php")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 3000
                readTimeout = 4000
                requestMethod = "POST"
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            }

            val payload = JSONObject().apply {
                put("database", config.databaseName)
                put("action", action)
                put("id", product.id)
                put("name", product.name)
                put("manufacturer", product.manufacturer)
                put("active_ingredient", product.activeIngredient)
                put("dosage_form", product.dosageForm)
                put("strength", product.strength)
                put("unit", product.unit)
                put("expiry_date", product.expiryDate)
                put("unit_price", product.unitPrice)
                put("wholesale_price", product.wholesalePrice)
                put("barcode", product.barcode)
                put("quantity", product.quantity)
                put("category", product.category)
                put("preferred_supplier", product.preferredSupplier)
                put("updated_at", product.updatedAt)
            }

            OutputStreamWriter(conn.outputStream, "UTF-8").use {
                it.write(payload.toString())
                it.flush()
            }
            conn.responseCode in 200..299
        } catch (e: Exception) {
            Log.w(TAG, "Failed to push single product to MySQL: ${e.message}")
            false
        }
    }

    /**
     * إنشاء ملف SQL DDL متكامل جاهز للتنفيذ في phpMyAdmin / MySQL Server
     */
    fun generateMySqlSchemaSql(databaseName: String = "saydali_pharmacy_db"): String {
        return """
-- ===================================================================
-- نظام صيدلي بلس (Saydali Plus) - سكربت قاعدة بيانات MySQL
-- Database: `$databaseName`
-- UTF-8 Unicode Support (utf8mb4_unicode_ci)
-- ===================================================================

CREATE DATABASE IF NOT EXISTS `$databaseName`
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE `$databaseName`;

-- -------------------------------------------------------------------
-- 1. جدول المخزون والأدوية (products)
-- -------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `products` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(255) NOT NULL COMMENT 'اسم الصنف أو الدواء',
    `manufacturer` VARCHAR(255) DEFAULT '' COMMENT 'الشركة المصنعة',
    `active_ingredient` VARCHAR(255) DEFAULT '' COMMENT 'المادة الفعالة للبدائل',
    `dosage_form` VARCHAR(100) DEFAULT 'أقراص' COMMENT 'الشكل الصيدلاني',
    `strength` VARCHAR(100) DEFAULT '' COMMENT 'التركيز الدوائي',
    `unit` VARCHAR(50) NOT NULL DEFAULT 'علبة' COMMENT 'الوحدة الكبرى',
    `expiry_date` VARCHAR(50) NOT NULL COMMENT 'تاريخ انتهاء الصلاحية',
    `unit_price` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT 'سعر البيع للجمهور',
    `wholesale_price` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT 'سعر الشراء / الجملة',
    `barcode` VARCHAR(100) DEFAULT '' COMMENT 'الباركود الدولي',
    `quantity` INT NOT NULL DEFAULT 0 COMMENT 'الكمية الحالية في المخزن',
    `min_stock_level` INT NOT NULL DEFAULT 5 COMMENT 'حد إعادة الطلب الحرج',
    `max_stock_level` INT NOT NULL DEFAULT 100 COMMENT 'الحد الأقصى للمخزون',
    `category` VARCHAR(100) DEFAULT 'عام' COMMENT 'تصنيف الدواء',
    `preferred_supplier` VARCHAR(255) DEFAULT 'الشركة الوطنية للدواء',
    `daily_burn_rate` DECIMAL(8, 2) DEFAULT 1.20 COMMENT 'معدل الاستهلاك اليومي',
    `usage_instructions` TEXT COMMENT 'دواعي الاستعمال',
    `contraindications` TEXT COMMENT 'موانع الاستخدام',
    `target_age_group` VARCHAR(100) DEFAULT '' COMMENT 'الفئة العمرية',
    `drug_interactions` TEXT COMMENT 'التداخلات الدوائية',
    `production_date` VARCHAR(50) DEFAULT '' COMMENT 'تاريخ الإنتاج',
    `batch_number` VARCHAR(100) DEFAULT '' COMMENT 'رقم التشغيلة / الوجبة',
    `alert_days_before_expiry` INT DEFAULT 90 COMMENT 'أيام الإنذار قبل الصلاحية',
    `storage_location` VARCHAR(100) DEFAULT 'الرف A1' COMMENT 'موقع التخزين بالصيدلية',
    `updated_at` BIGINT DEFAULT 0 COMMENT 'تاريخ آخر تحديث بالمللي ثانية',
    PRIMARY KEY (`id`),
    INDEX `idx_barcode` (`barcode`),
    INDEX `idx_active_ingredient` (`active_ingredient`),
    INDEX `idx_expiry_date` (`expiry_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------------------
-- 2. جدول المرتجعات والمراجعات (sale_returns)
-- مرتبط بحركات الفواتير ورصيد الأصناف
-- -------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `sale_returns` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `invoice_number` VARCHAR(100) NOT NULL COMMENT '1. رقم الفاتورة الأصلية',
    `product_id` BIGINT NOT NULL DEFAULT 0 COMMENT 'معرف الصنف المرتجع',
    `item_name` VARCHAR(255) NOT NULL COMMENT '2. اسم الصنف المرتجع',
    `barcode` VARCHAR(100) DEFAULT '' COMMENT 'باركود الصنف',
    `expiry_date` VARCHAR(50) NOT NULL COMMENT '3. تاريخ انتهاء الصلاحية والتشغيلة',
    `amount` DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '4. المبلغ المالي المستحق للمرتجع',
    `smallest_unit` VARCHAR(50) NOT NULL DEFAULT 'قرص' COMMENT '5. الوحدة الأصغر (قرص، شريط، أمبولة...)',
    `returned_quantity` INT NOT NULL DEFAULT 1 COMMENT '6. الكمية المراجعة المرتجعة',
    `return_reason` VARCHAR(255) DEFAULT 'طلب العميل' COMMENT 'سبب الإرجاع',
    `refund_method` VARCHAR(100) DEFAULT 'نقدي (كاش)' COMMENT 'طريقة رد المبلغ',
    `customer_name` VARCHAR(255) DEFAULT '' COMMENT 'اسم العميل',
    `pharmacist_name` VARCHAR(255) DEFAULT 'الصيدلي المناوب' COMMENT 'اسم الصيدلي المسؤول',
    `timestamp` BIGINT NOT NULL COMMENT 'طابع وقت الحركة بالمللي ثانية',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'توقيت التسجيل في خادم MySQL',
    PRIMARY KEY (`id`),
    INDEX `idx_invoice_number` (`invoice_number`),
    INDEX `idx_product_id` (`product_id`),
    INDEX `idx_timestamp` (`timestamp`),
    CONSTRAINT `fk_return_product` FOREIGN KEY (`product_id`) 
        REFERENCES `products` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------------------
-- 3. تريجر (Trigger) لتحديث رصيد المخزن تلقائياً في MySQL عند إدخال مرتجع
-- -------------------------------------------------------------------
DELIMITER $$
CREATE TRIGGER IF NOT EXISTS `trg_after_sale_return_insert`
AFTER INSERT ON `sale_returns`
FOR EACH ROW
BEGIN
    IF NEW.product_id > 0 THEN
        UPDATE `products`
        SET `quantity` = `quantity` + NEW.returned_quantity,
            `updated_at` = UNIX_TIMESTAMP() * 1000
        WHERE `id` = NEW.product_id;
    END IF;
END$$
DELIMITER ;
""".trimIndent()
    }

    /**
     * كود ملف PHP القياسي (sync_all.php) لتثبيته في XAMPP / Apache Gateway
     */
    fun getPhpBackendScriptContent(): String {
        return """
<?php
// ===================================================================
// saydali_api / sync_all.php
// بوابة مزامنة تطبيق صيدلي بلس مع قاعدة بيانات MySQL
// ===================================================================
header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, X-Database-User');

if (${'$'}_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit;
}

${'$'}rawInput = file_get_contents('php://input');
${'$'}data = json_decode(${'$'}rawInput, true);

if (!${'$'}data) {
    http_response_code(400);
    echo json_encode(['status' => 'error', 'message' => 'Invalid JSON input']);
    exit;
}

${'$'}host = 'localhost';
${'$'}db   = ${'$'}data['database'] ?? 'saydali_pharmacy_db';
${'$'}user = 'root';
${'$'}pass = '';
${'$'}charset = 'utf8mb4';

${'$'}dsn = "mysql:host=${'$'}host;dbname=${'$'}db;charset=${'$'}charset";
${'$'}options = [
    PDO::ATTR_ERRMODE            => PDO::ERRMODE_EXCEPTION,
    PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
    PDO::ATTR_EMULATE_PREPARES   => false,
];

try {
    ${'$'}pdo = new PDO(${'$'}dsn, ${'$'}user, ${'$'}pass, ${'$'}options);

    ${'$'}pdo->beginTransaction();

    // 1. مزامنة المخزون (Upsert Products)
    ${'$'}syncedProducts = 0;
    if (!empty(${'$'}data['products'])) {
        ${'$'}stmt = ${'$'}pdo->prepare("
            INSERT INTO products (
                id, name, manufacturer, active_ingredient, dosage_form, strength,
                unit, expiry_date, unit_price, wholesale_price, barcode,
                quantity, min_stock_level, max_stock_level, category,
                preferred_supplier, updated_at
            ) VALUES (
                :id, :name, :manufacturer, :active_ingredient, :dosage_form, :strength,
                :unit, :expiry_date, :unit_price, :wholesale_price, :barcode,
                :quantity, :min_stock_level, :max_stock_level, :category,
                :preferred_supplier, :updated_at
            ) ON DUPLICATE KEY UPDATE
                name = VALUES(name),
                quantity = VALUES(quantity),
                unit_price = VALUES(unit_price),
                wholesale_price = VALUES(wholesale_price),
                expiry_date = VALUES(expiry_date),
                min_stock_level = VALUES(min_stock_level),
                updated_at = VALUES(updated_at)
        ");

        foreach (${'$'}data['products'] as ${'$'}p) {
            ${'$'}stmt->execute([
                ':id' => ${'$'}p['id'],
                ':name' => ${'$'}p['name'],
                ':manufacturer' => ${'$'}p['manufacturer'] ?? '',
                ':active_ingredient' => ${'$'}p['active_ingredient'] ?? '',
                ':dosage_form' => ${'$'}p['dosage_form'] ?? 'أقراص',
                ':strength' => ${'$'}p['strength'] ?? '',
                ':unit' => ${'$'}p['unit'] ?? 'علبة',
                ':expiry_date' => ${'$'}p['expiry_date'] ?? '',
                ':unit_price' => ${'$'}p['unit_price'] ?? 0.0,
                ':wholesale_price' => ${'$'}p['wholesale_price'] ?? 0.0,
                ':barcode' => ${'$'}p['barcode'] ?? '',
                ':quantity' => ${'$'}p['quantity'] ?? 0,
                ':min_stock_level' => ${'$'}p['min_stock_level'] ?? 5,
                ':max_stock_level' => ${'$'}p['max_stock_level'] ?? 100,
                ':category' => ${'$'}p['category'] ?? 'عام',
                ':preferred_supplier' => ${'$'}p['preferred_supplier'] ?? '',
                ':updated_at' => ${'$'}p['updated_at'] ?? round(microtime(true)*1000)
            ]);
            ${'$'}syncedProducts++;
        }
    }

    // 2. مزامنة المرتجعات (Upsert Sale Returns)
    ${'$'}syncedReturns = 0;
    if (!empty(${'$'}data['sale_returns'])) {
        ${'$'}stmtReturn = ${'$'}pdo->prepare("
            INSERT INTO sale_returns (
                id, invoice_number, product_id, item_name, barcode, expiry_date,
                amount, smallest_unit, returned_quantity, return_reason, refund_method,
                pharmacist_name, timestamp
            ) VALUES (
                :id, :invoice_number, :product_id, :item_name, :barcode, :expiry_date,
                :amount, :smallest_unit, :returned_quantity, :return_reason, :refund_method,
                :pharmacist_name, :timestamp
            ) ON DUPLICATE KEY UPDATE
                amount = VALUES(amount),
                returned_quantity = VALUES(returned_quantity),
                return_reason = VALUES(return_reason)
        ");

        foreach (${'$'}data['sale_returns'] as ${'$'}r) {
            ${'$'}stmtReturn->execute([
                ':id' => ${'$'}r['id'],
                ':invoice_number' => ${'$'}r['invoice_number'],
                ':product_id' => ${'$'}r['product_id'] ?? 0,
                ':item_name' => ${'$'}r['item_name'],
                ':barcode' => ${'$'}r['barcode'] ?? '',
                ':expiry_date' => ${'$'}r['expiry_date'] ?? '',
                ':amount' => ${'$'}r['amount'] ?? 0.0,
                ':smallest_unit' => ${'$'}r['smallest_unit'] ?? 'قرص',
                ':returned_quantity' => ${'$'}r['returned_quantity'] ?? 1,
                ':return_reason' => ${'$'}r['return_reason'] ?? '',
                ':refund_method' => ${'$'}r['refund_method'] ?? 'نقدي (كاش)',
                ':pharmacist_name' => ${'$'}r['pharmacist_name'] ?? '',
                ':timestamp' => ${'$'}r['timestamp'] ?? round(microtime(true)*1000)
            ]);
            ${'$'}syncedReturns++;
        }
    }

    ${'$'}pdo->commit();

    echo json_encode([
        'status' => 'success',
        'message' => 'Synced successfully with MySQL',
        'synced_products' => ${'$'}syncedProducts,
        'synced_returns' => ${'$'}syncedReturns,
        'timestamp' => round(microtime(true) * 1000)
    ], JSON_UNESCAPED_UNICODE);

} catch (Exception ${'$'}e) {
    if (isset(${'$'}pdo) && ${'$'}pdo->inTransaction()) {
        ${'$'}pdo->rollBack();
    }
    http_response_code(500);
    echo json_encode([
        'status' => 'error',
        'message' => ${'$'}e->getMessage()
    ], JSON_UNESCAPED_UNICODE);
}
""".trimIndent()
    }
}
