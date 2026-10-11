package com.example.data

import android.content.Context
import android.util.Log
import com.example.util.SecureMySqlPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement
import java.util.Properties
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * فئة المستودع للاتصال عن بُعد بقاعدة بيانات MySQL (MySqlRemoteRepository)
 * تدير جلسات الاتصال المباشر عبر برنامج تعريف MySQL (mysql-connector-java / JDBC)
 * وتوفر عمليات CRUD الكاملة لإدارة المخزون (الأدوية والمنتجات) وحركات المرتجعات.
 */
class MySqlRemoteRepository(
    private val context: Context,
    private var configProvider: () -> MySqlConfig = { SecureMySqlPreferences.loadConfig(context) }
) {
    companion object {
        private const val TAG = "MySqlRemoteRepo"
        private const val DRIVER_CLASS_NAME = "com.mysql.jdbc.Driver"

        init {
            try {
                Class.forName(DRIVER_CLASS_NAME)
                Log.d(TAG, "MySQL JDBC Driver loaded successfully: $DRIVER_CLASS_NAME")
            } catch (e: ClassNotFoundException) {
                Log.e(TAG, "MySQL JDBC Driver not found: ${e.message}", e)
            }
        }
    }

    /**
     * إنشاء اتصال JDBC مباشر بقاعدة بيانات MySQL البعيدة
     */
    @Throws(SQLException::class)
    private fun createConnection(): Connection {
        val config = configProvider()
        val jdbcUrl = "jdbc:mysql://${config.host.trim()}:${config.port}/${config.databaseName.trim()}?useUnicode=true&characterEncoding=UTF-8&connectTimeout=5000&socketTimeout=8000&autoReconnect=true&useSSL=false"

        val props = Properties().apply {
            put("user", config.username.trim())
            put("password", config.password)
            put("connectTimeout", "5000")
            put("socketTimeout", "8000")
        }

        return DriverManager.getConnection(jdbcUrl, props)
    }

    /**
     * فحص واختبار حالة الاتصال عن بُعد مع قياس زمن الاستجابة
     */
    suspend fun testConnection(): Result<Long> = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        try {
            createConnection().use { conn ->
                conn.createStatement().use { stmt ->
                    stmt.executeQuery("SELECT 1").use { rs ->
                        if (rs.next()) {
                            val latency = System.currentTimeMillis() - startTime
                            Result.success(latency)
                        } else {
                            Result.failure(Exception("لم تستجب قاعدة البيانات لطلب الفحص"))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Remote connection test failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * إنشاء الجداول والتريجرز عن بُعد في حال عدم وجودها تلقائياً
     */
    suspend fun ensureRemoteTablesExist(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            createConnection().use { conn ->
                conn.createStatement().use { stmt ->
                    // جدول products
                    stmt.executeUpdate(
                        """
                        CREATE TABLE IF NOT EXISTS `products` (
                            `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
                            `name` VARCHAR(255) NOT NULL,
                            `manufacturer` VARCHAR(255) DEFAULT '',
                            `activeIngredient` VARCHAR(255) DEFAULT '',
                            `dosageForm` VARCHAR(100) DEFAULT 'أقراص',
                            `strength` VARCHAR(100) DEFAULT '',
                            `unit` VARCHAR(50) DEFAULT 'عبوة',
                            `expiryDate` VARCHAR(50) DEFAULT '',
                            `unitPrice` DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
                            `wholesalePrice` DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
                            `barcode` VARCHAR(100) DEFAULT '',
                            `quantity` INT NOT NULL DEFAULT 0,
                            `minStockLevel` INT NOT NULL DEFAULT 5,
                            `category` VARCHAR(100) DEFAULT 'عام',
                            `preferredSupplier` VARCHAR(255) DEFAULT '',
                            `updatedAt` BIGINT DEFAULT 0,
                            INDEX `idx_barcode` (`barcode`),
                            INDEX `idx_name` (`name`)
                        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
                        """.trimIndent()
                    )

                    // جدول sale_returns
                    stmt.executeUpdate(
                        """
                        CREATE TABLE IF NOT EXISTS `sale_returns` (
                            `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
                            `invoiceNumber` VARCHAR(100) NOT NULL,
                            `productId` BIGINT NOT NULL DEFAULT 0,
                            `itemName` VARCHAR(255) NOT NULL,
                            `barcode` VARCHAR(100) DEFAULT '',
                            `expiryDate` VARCHAR(50) DEFAULT '',
                            `amount` DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
                            `smallestUnit` VARCHAR(50) DEFAULT 'قرص',
                            `returnedQuantity` INT NOT NULL DEFAULT 1,
                            `returnReason` VARCHAR(255) DEFAULT '',
                            `refundMethod` VARCHAR(100) DEFAULT 'نقدي (كاش)',
                            `customerName` VARCHAR(255) DEFAULT '',
                            `pharmacistName` VARCHAR(255) DEFAULT '',
                            `timestamp` BIGINT NOT NULL DEFAULT 0,
                            INDEX `idx_invoice` (`invoiceNumber`),
                            INDEX `idx_product` (`productId`)
                        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
                        """.trimIndent()
                    )

                    // جدول sale_invoices للمبيعات اليومية
                    stmt.executeUpdate(
                        """
                        CREATE TABLE IF NOT EXISTS `sale_invoices` (
                            `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
                            `invoiceNumber` VARCHAR(100) NOT NULL UNIQUE,
                            `timestamp` BIGINT NOT NULL DEFAULT 0,
                            `totalAmount` DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
                            `itemCount` INT NOT NULL DEFAULT 0,
                            `paymentMethod` VARCHAR(100) DEFAULT 'نقدي',
                            `cashierName` VARCHAR(255) DEFAULT '',
                            `customerId` BIGINT DEFAULT NULL,
                            `customerName` VARCHAR(255) DEFAULT '',
                            `customerPhone` VARCHAR(50) DEFAULT '',
                            `isCreditSale` TINYINT(1) DEFAULT 0,
                            INDEX `idx_timestamp` (`timestamp`),
                            INDEX `idx_inv_num` (`invoiceNumber`)
                        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
                        """.trimIndent()
                    )

                    // جدول sale_items للأصناف والبنود المباعة
                    stmt.executeUpdate(
                        """
                        CREATE TABLE IF NOT EXISTS `sale_items` (
                            `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
                            `invoiceId` BIGINT NOT NULL DEFAULT 0,
                            `productId` BIGINT NOT NULL DEFAULT 0,
                            `productName` VARCHAR(255) NOT NULL,
                            `quantity` INT NOT NULL DEFAULT 1,
                            `unitPrice` DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
                            `totalPrice` DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
                            `dosageInstructions` VARCHAR(255) DEFAULT '',
                            `stickerLabel` VARCHAR(255) DEFAULT '',
                            INDEX `idx_inv_id` (`invoiceId`),
                            INDEX `idx_product` (`productId`),
                            INDEX `idx_product_name` (`productName`)
                        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
                        """.trimIndent()
                    )
                }
            }
            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to ensure remote tables exist: ${e.message}", e)
            Result.failure(e)
        }
    }

    // =========================================================================
    // عمليات CRUD للمخزون والأصناف عن بُعد (Remote Products CRUD)
    // =========================================================================

    /**
     * إضافة صنف جديد إلى قاعدة بيانات MySQL البعيدة (Create)
     */
    suspend fun insertProduct(product: Product): Result<Long> = withContext(Dispatchers.IO) {
        val sql = """
            INSERT INTO `products` (
                name, manufacturer, activeIngredient, dosageForm, strength,
                unit, expiryDate, unitPrice, wholesalePrice, barcode,
                quantity, minStockLevel, category, preferredSupplier, updatedAt
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()

        try {
            createConnection().use { conn ->
                conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS).use { stmt ->
                    stmt.setString(1, product.name)
                    stmt.setString(2, product.manufacturer)
                    stmt.setString(3, product.activeIngredient)
                    stmt.setString(4, product.dosageForm)
                    stmt.setString(5, product.strength)
                    stmt.setString(6, product.unit)
                    stmt.setString(7, product.expiryDate)
                    stmt.setDouble(8, product.unitPrice)
                    stmt.setDouble(9, product.wholesalePrice)
                    stmt.setString(10, product.barcode)
                    stmt.setInt(11, product.quantity)
                    stmt.setInt(12, product.minStockLevel)
                    stmt.setString(13, product.category)
                    stmt.setString(14, product.preferredSupplier)
                    stmt.setLong(15, product.updatedAt)

                    stmt.executeUpdate()
                    val rs = stmt.generatedKeys
                    val newId = if (rs.next()) rs.getLong(1) else product.id
                    Result.success(newId)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting product remotely: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * استعلام وجلب كافة أصناف المخزون من خادم MySQL البعيد (Read All)
     */
    suspend fun getAllProducts(): Result<List<Product>> = withContext(Dispatchers.IO) {
        val list = mutableListOf<Product>()
        val sql = "SELECT * FROM `products` ORDER BY `id` DESC"

        try {
            createConnection().use { conn ->
                conn.createStatement().use { stmt ->
                    stmt.executeQuery(sql).use { rs ->
                        while (rs.next()) {
                            list.add(mapResultSetToProduct(rs))
                        }
                    }
                }
            }
            Result.success(list)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching all products remotely: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * جلب صنف محدد بواسطة المعرف (Read by ID)
     */
    suspend fun getProductById(id: Long): Result<Product?> = withContext(Dispatchers.IO) {
        val sql = "SELECT * FROM `products` WHERE `id` = ? LIMIT 1"
        try {
            createConnection().use { conn ->
                conn.prepareStatement(sql).use { stmt ->
                    stmt.setLong(1, id)
                    stmt.executeQuery().use { rs ->
                        if (rs.next()) {
                            Result.success(mapResultSetToProduct(rs))
                        } else {
                            Result.success(null)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching product by id remotely: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * البحث عن صنف بواسطة الباركود (Read by Barcode)
     */
    suspend fun getProductByBarcode(barcode: String): Result<Product?> = withContext(Dispatchers.IO) {
        val sql = "SELECT * FROM `products` WHERE `barcode` = ? LIMIT 1"
        try {
            createConnection().use { conn ->
                conn.prepareStatement(sql).use { stmt ->
                    stmt.setString(1, barcode.trim())
                    stmt.executeQuery().use { rs ->
                        if (rs.next()) {
                            Result.success(mapResultSetToProduct(rs))
                        } else {
                            Result.success(null)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching product by barcode remotely: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * تحديث بيانات صنف في قاعدة بيانات MySQL البعيدة (Update)
     */
    suspend fun updateProduct(product: Product): Result<Boolean> = withContext(Dispatchers.IO) {
        val sql = """
            UPDATE `products` SET
                name = ?, manufacturer = ?, activeIngredient = ?, dosageForm = ?,
                strength = ?, unit = ?, expiryDate = ?, unitPrice = ?,
                wholesalePrice = ?, barcode = ?, quantity = ?, minStockLevel = ?,
                category = ?, preferredSupplier = ?, updatedAt = ?
            WHERE id = ? OR (barcode = ? AND barcode != '')
        """.trimIndent()

        try {
            createConnection().use { conn ->
                conn.prepareStatement(sql).use { stmt ->
                    stmt.setString(1, product.name)
                    stmt.setString(2, product.manufacturer)
                    stmt.setString(3, product.activeIngredient)
                    stmt.setString(4, product.dosageForm)
                    stmt.setString(5, product.strength)
                    stmt.setString(6, product.unit)
                    stmt.setString(7, product.expiryDate)
                    stmt.setDouble(8, product.unitPrice)
                    stmt.setDouble(9, product.wholesalePrice)
                    stmt.setString(10, product.barcode)
                    stmt.setInt(11, product.quantity)
                    stmt.setInt(12, product.minStockLevel)
                    stmt.setString(13, product.category)
                    stmt.setString(14, product.preferredSupplier)
                    stmt.setLong(15, System.currentTimeMillis())
                    stmt.setLong(16, product.id)
                    stmt.setString(17, product.barcode)

                    val updatedRows = stmt.executeUpdate()
                    Result.success(updatedRows > 0)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating product remotely: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * تحديث رصيد وكمية الصنف فقط في الخادم البعيد
     */
    suspend fun updateProductStock(productId: Long, newQuantity: Int): Result<Boolean> = withContext(Dispatchers.IO) {
        val sql = "UPDATE `products` SET `quantity` = ?, `updatedAt` = ? WHERE `id` = ?"
        try {
            createConnection().use { conn ->
                conn.prepareStatement(sql).use { stmt ->
                    stmt.setInt(1, newQuantity)
                    stmt.setLong(2, System.currentTimeMillis())
                    stmt.setLong(3, productId)
                    val rows = stmt.executeUpdate()
                    Result.success(rows > 0)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating product stock remotely: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * حذف صنف من قاعدة بيانات MySQL البعيدة (Delete)
     */
    suspend fun deleteProduct(productId: Long): Result<Boolean> = withContext(Dispatchers.IO) {
        val sql = "DELETE FROM `products` WHERE `id` = ?"
        try {
            createConnection().use { conn ->
                conn.prepareStatement(sql).use { stmt ->
                    stmt.setLong(1, productId)
                    val rows = stmt.executeUpdate()
                    Result.success(rows > 0)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting product remotely: ${e.message}", e)
            Result.failure(e)
        }
    }

    // =========================================================================
    // عمليات CRUD للمرتجعات عن بُعد (Remote Sale Returns CRUD)
    // =========================================================================

    /**
     * إضافة حركة إرجاع جديدة إلى خادم MySQL البعيد مع استعادة رصيد الصنف (Create)
     */
    suspend fun insertSaleReturn(saleReturn: SaleReturn, restoreStock: Boolean = true): Result<Long> = withContext(Dispatchers.IO) {
        val insertSql = """
            INSERT INTO `sale_returns` (
                invoiceNumber, productId, itemName, barcode, expiryDate,
                amount, smallestUnit, returnedQuantity, returnReason,
                refundMethod, customerName, pharmacistName, timestamp
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()

        val updateStockSql = """
            UPDATE `products` SET `quantity` = `quantity` + ? WHERE `id` = ? OR (barcode = ? AND barcode != '')
        """.trimIndent()

        try {
            createConnection().use { conn ->
                conn.autoCommit = false
                try {
                    var returnId = saleReturn.id
                    conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS).use { stmt ->
                        stmt.setString(1, saleReturn.invoiceNumber)
                        stmt.setLong(2, saleReturn.productId)
                        stmt.setString(3, saleReturn.itemName)
                        stmt.setString(4, saleReturn.barcode)
                        stmt.setString(5, saleReturn.expiryDate)
                        stmt.setDouble(6, saleReturn.amount)
                        stmt.setString(7, saleReturn.smallestUnit)
                        stmt.setInt(8, saleReturn.returnedQuantity)
                        stmt.setString(9, saleReturn.returnReason)
                        stmt.setString(10, saleReturn.refundMethod)
                        stmt.setString(11, saleReturn.customerName)
                        stmt.setString(12, saleReturn.pharmacistName)
                        stmt.setLong(13, saleReturn.timestamp)

                        stmt.executeUpdate()
                        val rs = stmt.generatedKeys
                        if (rs.next()) {
                            returnId = rs.getLong(1)
                        }
                    }

                    if (restoreStock && saleReturn.returnedQuantity > 0) {
                        conn.prepareStatement(updateStockSql).use { stmt ->
                            stmt.setInt(1, saleReturn.returnedQuantity)
                            stmt.setLong(2, saleReturn.productId)
                            stmt.setString(3, saleReturn.barcode)
                            stmt.executeUpdate()
                        }
                    }

                    conn.commit()
                    Result.success(returnId)
                } catch (ex: Exception) {
                    conn.rollback()
                    throw ex
                } finally {
                    conn.autoCommit = true
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting sale return remotely: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * استعلام وجلب كافة المرتجعات المسجلة من خادم MySQL البعيد (Read All)
     */
    suspend fun getAllSaleReturns(): Result<List<SaleReturn>> = withContext(Dispatchers.IO) {
        val list = mutableListOf<SaleReturn>()
        val sql = "SELECT * FROM `sale_returns` ORDER BY `timestamp` DESC"

        try {
            createConnection().use { conn ->
                conn.createStatement().use { stmt ->
                    stmt.executeQuery(sql).use { rs ->
                        while (rs.next()) {
                            list.add(mapResultSetToSaleReturn(rs))
                        }
                    }
                }
            }
            Result.success(list)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching all sale returns remotely: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * جلب حركة إرجاع محددة بالمعرف (Read by ID)
     */
    suspend fun getSaleReturnById(id: Long): Result<SaleReturn?> = withContext(Dispatchers.IO) {
        val sql = "SELECT * FROM `sale_returns` WHERE `id` = ? LIMIT 1"
        try {
            createConnection().use { conn ->
                conn.prepareStatement(sql).use { stmt ->
                    stmt.setLong(1, id)
                    stmt.executeQuery().use { rs ->
                        if (rs.next()) {
                            Result.success(mapResultSetToSaleReturn(rs))
                        } else {
                            Result.success(null)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching sale return by id remotely: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * جلب المرتجعات الخاصة برقم فاتورة معينة (Read by Invoice Number)
     */
    suspend fun getSaleReturnsByInvoice(invoiceNumber: String): Result<List<SaleReturn>> = withContext(Dispatchers.IO) {
        val list = mutableListOf<SaleReturn>()
        val sql = "SELECT * FROM `sale_returns` WHERE `invoiceNumber` = ? ORDER BY `timestamp` DESC"
        try {
            createConnection().use { conn ->
                conn.prepareStatement(sql).use { stmt ->
                    stmt.setString(1, invoiceNumber.trim())
                    stmt.executeQuery().use { rs ->
                        while (rs.next()) {
                            list.add(mapResultSetToSaleReturn(rs))
                        }
                    }
                }
            }
            Result.success(list)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching sale returns by invoice remotely: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * تعديل بيانات حركة إرجاع مسجلة (Update)
     */
    suspend fun updateSaleReturn(saleReturn: SaleReturn): Result<Boolean> = withContext(Dispatchers.IO) {
        val sql = """
            UPDATE `sale_returns` SET
                invoiceNumber = ?, productId = ?, itemName = ?, barcode = ?,
                expiryDate = ?, amount = ?, smallestUnit = ?, returnedQuantity = ?,
                returnReason = ?, refundMethod = ?, customerName = ?, pharmacistName = ?
            WHERE id = ?
        """.trimIndent()

        try {
            createConnection().use { conn ->
                conn.prepareStatement(sql).use { stmt ->
                    stmt.setString(1, saleReturn.invoiceNumber)
                    stmt.setLong(2, saleReturn.productId)
                    stmt.setString(3, saleReturn.itemName)
                    stmt.setString(4, saleReturn.barcode)
                    stmt.setString(5, saleReturn.expiryDate)
                    stmt.setDouble(6, saleReturn.amount)
                    stmt.setString(7, saleReturn.smallestUnit)
                    stmt.setInt(8, saleReturn.returnedQuantity)
                    stmt.setString(9, saleReturn.returnReason)
                    stmt.setString(10, saleReturn.refundMethod)
                    stmt.setString(11, saleReturn.customerName)
                    stmt.setString(12, saleReturn.pharmacistName)
                    stmt.setLong(13, saleReturn.id)

                    val updatedRows = stmt.executeUpdate()
                    Result.success(updatedRows > 0)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating sale return remotely: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * حذف حركة إرجاع من خادم MySQL البعيد (Delete)
     */
    suspend fun deleteSaleReturn(returnId: Long): Result<Boolean> = withContext(Dispatchers.IO) {
        val sql = "DELETE FROM `sale_returns` WHERE `id` = ?"
        try {
            createConnection().use { conn ->
                conn.prepareStatement(sql).use { stmt ->
                    stmt.setLong(1, returnId)
                    val rows = stmt.executeUpdate()
                    Result.success(rows > 0)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting sale return remotely: ${e.message}", e)
            Result.failure(e)
        }
    }

    // =========================================================================
    // عمليات المزامنة الدفعية (Batch Synchronization)
    // =========================================================================

    /**
     * رفع ومزامنة قائمة المنتجات كاملة في عملية واحدة (Batch Upsert)
     */
    suspend fun batchSyncProducts(products: List<Product>): Result<Int> = withContext(Dispatchers.IO) {
        val upsertSql = """
            INSERT INTO `products` (
                id, name, manufacturer, activeIngredient, dosageForm, strength,
                unit, expiryDate, unitPrice, wholesalePrice, barcode,
                quantity, minStockLevel, category, preferredSupplier, updatedAt
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                name = VALUES(name),
                manufacturer = VALUES(manufacturer),
                activeIngredient = VALUES(activeIngredient),
                dosageForm = VALUES(dosageForm),
                strength = VALUES(strength),
                unit = VALUES(unit),
                expiryDate = VALUES(expiryDate),
                unitPrice = VALUES(unitPrice),
                wholesalePrice = VALUES(wholesalePrice),
                barcode = VALUES(barcode),
                quantity = VALUES(quantity),
                minStockLevel = VALUES(minStockLevel),
                category = VALUES(category),
                preferredSupplier = VALUES(preferredSupplier),
                updatedAt = VALUES(updatedAt)
        """.trimIndent()

        try {
            createConnection().use { conn ->
                conn.autoCommit = false
                try {
                    conn.prepareStatement(upsertSql).use { stmt ->
                        for (p in products) {
                            stmt.setLong(1, p.id)
                            stmt.setString(2, p.name)
                            stmt.setString(3, p.manufacturer)
                            stmt.setString(4, p.activeIngredient)
                            stmt.setString(5, p.dosageForm)
                            stmt.setString(6, p.strength)
                            stmt.setString(7, p.unit)
                            stmt.setString(8, p.expiryDate)
                            stmt.setDouble(9, p.unitPrice)
                            stmt.setDouble(10, p.wholesalePrice)
                            stmt.setString(11, p.barcode)
                            stmt.setInt(12, p.quantity)
                            stmt.setInt(13, p.minStockLevel)
                            stmt.setString(14, p.category)
                            stmt.setString(15, p.preferredSupplier)
                            stmt.setLong(16, p.updatedAt)
                            stmt.addBatch()
                        }
                        stmt.executeBatch()
                    }
                    conn.commit()
                    Result.success(products.size)
                } catch (ex: Exception) {
                    conn.rollback()
                    throw ex
                } finally {
                    conn.autoCommit = true
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Batch sync products failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * رفع ومزامنة قائمة المرتجعات كاملة (Batch Upsert)
     */
    suspend fun batchSyncReturns(returns: List<SaleReturn>): Result<Int> = withContext(Dispatchers.IO) {
        val upsertSql = """
            INSERT INTO `sale_returns` (
                id, invoiceNumber, productId, itemName, barcode, expiryDate,
                amount, smallestUnit, returnedQuantity, returnReason,
                refundMethod, customerName, pharmacistName, timestamp
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                invoiceNumber = VALUES(invoiceNumber),
                productId = VALUES(productId),
                itemName = VALUES(itemName),
                barcode = VALUES(barcode),
                expiryDate = VALUES(expiryDate),
                amount = VALUES(amount),
                smallestUnit = VALUES(smallestUnit),
                returnedQuantity = VALUES(returnedQuantity),
                returnReason = VALUES(returnReason),
                refundMethod = VALUES(refundMethod),
                customerName = VALUES(customerName),
                pharmacistName = VALUES(pharmacistName),
                timestamp = VALUES(timestamp)
        """.trimIndent()

        try {
            createConnection().use { conn ->
                conn.autoCommit = false
                try {
                    conn.prepareStatement(upsertSql).use { stmt ->
                        for (r in returns) {
                            stmt.setLong(1, r.id)
                            stmt.setString(2, r.invoiceNumber)
                            stmt.setLong(3, r.productId)
                            stmt.setString(4, r.itemName)
                            stmt.setString(5, r.barcode)
                            stmt.setString(6, r.expiryDate)
                            stmt.setDouble(7, r.amount)
                            stmt.setString(8, r.smallestUnit)
                            stmt.setInt(9, r.returnedQuantity)
                            stmt.setString(10, r.returnReason)
                            stmt.setString(11, r.refundMethod)
                            stmt.setString(12, r.customerName)
                            stmt.setString(13, r.pharmacistName)
                            stmt.setLong(14, r.timestamp)
                            stmt.addBatch()
                        }
                        stmt.executeBatch()
                    }
                    conn.commit()
                    Result.success(returns.size)
                } catch (ex: Exception) {
                    conn.rollback()
                    throw ex
                } finally {
                    conn.autoCommit = true
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Batch sync returns failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    // =========================================================================
    // تحويل نتائج ResultSet إلى كائنات البيانات
    // =========================================================================

    private fun mapResultSetToProduct(rs: ResultSet): Product {
        return Product(
            id = rs.getLong("id"),
            name = rs.getString("name") ?: "",
            manufacturer = rs.getString("manufacturer") ?: "",
            activeIngredient = rs.getString("activeIngredient") ?: "",
            dosageForm = rs.getString("dosageForm") ?: "أقراص",
            strength = rs.getString("strength") ?: "",
            unit = rs.getString("unit") ?: "عبوة",
            expiryDate = rs.getString("expiryDate") ?: "",
            unitPrice = rs.getDouble("unitPrice"),
            wholesalePrice = rs.getDouble("wholesalePrice"),
            barcode = rs.getString("barcode") ?: "",
            quantity = rs.getInt("quantity"),
            minStockLevel = rs.getInt("minStockLevel"),
            category = rs.getString("category") ?: "عام",
            preferredSupplier = rs.getString("preferredSupplier") ?: "",
            updatedAt = rs.getLong("updatedAt")
        )
    }

    private fun mapResultSetToSaleReturn(rs: ResultSet): SaleReturn {
        return SaleReturn(
            id = rs.getLong("id"),
            invoiceNumber = rs.getString("invoiceNumber") ?: "",
            productId = rs.getLong("productId"),
            itemName = rs.getString("itemName") ?: "",
            barcode = rs.getString("barcode") ?: "",
            expiryDate = rs.getString("expiryDate") ?: "",
            amount = rs.getDouble("amount"),
            smallestUnit = rs.getString("smallestUnit") ?: "قرص",
            returnedQuantity = rs.getInt("returnedQuantity"),
            returnReason = rs.getString("returnReason") ?: "",
            refundMethod = rs.getString("refundMethod") ?: "نقدي (كاش)",
            customerName = rs.getString("customerName") ?: "",
            pharmacistName = rs.getString("pharmacistName") ?: "",
            timestamp = rs.getLong("timestamp")
        )
    }

    // =========================================================================
    // عمليات مزامنة واستعلام المبيعات والأصناف الأكثر مبيعاً (Sales & Analytics)
    // =========================================================================

    /**
     * تسجيل عملية بيع جديدة مباشرة في خادم MySQL مع بنود الفاتورة
     */
    suspend fun insertSale(invoice: SaleInvoice, items: List<SaleItem>): Result<Long> = withContext(Dispatchers.IO) {
        val insertInvSql = """
            INSERT INTO `sale_invoices` (
                invoiceNumber, timestamp, totalAmount, itemCount, paymentMethod,
                cashierName, customerId, customerName, customerPhone, isCreditSale
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                totalAmount = VALUES(totalAmount),
                itemCount = VALUES(itemCount),
                paymentMethod = VALUES(paymentMethod)
        """.trimIndent()

        val insertItemSql = """
            INSERT INTO `sale_items` (
                invoiceId, productId, productName, quantity, unitPrice,
                totalPrice, dosageInstructions, stickerLabel
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()

        try {
            createConnection().use { conn ->
                conn.autoCommit = false
                try {
                    var generatedInvId = invoice.id
                    conn.prepareStatement(insertInvSql, Statement.RETURN_GENERATED_KEYS).use { stmt ->
                        stmt.setString(1, invoice.invoiceNumber)
                        stmt.setLong(2, invoice.timestamp)
                        stmt.setDouble(3, invoice.totalAmount)
                        stmt.setInt(4, invoice.itemCount)
                        stmt.setString(5, invoice.paymentMethod)
                        stmt.setString(6, invoice.cashierName)
                        if (invoice.customerId != null) stmt.setLong(7, invoice.customerId) else stmt.setNull(7, java.sql.Types.BIGINT)
                        stmt.setString(8, invoice.customerName)
                        stmt.setString(9, invoice.customerPhone)
                        stmt.setBoolean(10, invoice.isCreditSale)
                        stmt.executeUpdate()
                        val rs = stmt.generatedKeys
                        if (rs.next()) {
                            generatedInvId = rs.getLong(1)
                        }
                    }

                    if (items.isNotEmpty()) {
                        conn.prepareStatement(insertItemSql).use { stmt ->
                            for (it in items) {
                                stmt.setLong(1, if (generatedInvId > 0) generatedInvId else invoice.id)
                                stmt.setLong(2, it.productId)
                                stmt.setString(3, it.productName)
                                stmt.setInt(4, it.quantity)
                                stmt.setDouble(5, it.unitPrice)
                                stmt.setDouble(6, it.totalPrice)
                                stmt.setString(7, it.dosageInstructions)
                                stmt.setString(8, it.stickerLabel)
                                stmt.addBatch()
                            }
                            stmt.executeBatch()
                        }
                    }
                    conn.commit()
                    Result.success(generatedInvId)
                } catch (ex: Exception) {
                    conn.rollback()
                    throw ex
                } finally {
                    conn.autoCommit = true
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting sale into MySQL: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * مزامنة دفعية لكافة الفواتير وبنود المبيعات إلى MySQL (Batch Upsert)
     */
    suspend fun batchSyncSales(invoices: List<SaleInvoice>, items: List<SaleItem>): Result<Int> = withContext(Dispatchers.IO) {
        val upsertInvSql = """
            INSERT INTO `sale_invoices` (
                id, invoiceNumber, timestamp, totalAmount, itemCount, paymentMethod,
                cashierName, customerId, customerName, customerPhone, isCreditSale
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                totalAmount = VALUES(totalAmount),
                itemCount = VALUES(itemCount),
                paymentMethod = VALUES(paymentMethod),
                customerName = VALUES(customerName)
        """.trimIndent()

        val upsertItemSql = """
            INSERT INTO `sale_items` (
                id, invoiceId, productId, productName, quantity, unitPrice,
                totalPrice, dosageInstructions, stickerLabel
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                quantity = VALUES(quantity),
                unitPrice = VALUES(unitPrice),
                totalPrice = VALUES(totalPrice)
        """.trimIndent()

        try {
            createConnection().use { conn ->
                conn.autoCommit = false
                try {
                    conn.prepareStatement(upsertInvSql).use { stmt ->
                        for (inv in invoices) {
                            stmt.setLong(1, inv.id)
                            stmt.setString(2, inv.invoiceNumber)
                            stmt.setLong(3, inv.timestamp)
                            stmt.setDouble(4, inv.totalAmount)
                            stmt.setInt(5, inv.itemCount)
                            stmt.setString(6, inv.paymentMethod)
                            stmt.setString(7, inv.cashierName)
                            if (inv.customerId != null) stmt.setLong(8, inv.customerId) else stmt.setNull(8, java.sql.Types.BIGINT)
                            stmt.setString(9, inv.customerName)
                            stmt.setString(10, inv.customerPhone)
                            stmt.setBoolean(11, inv.isCreditSale)
                            stmt.addBatch()
                        }
                        stmt.executeBatch()
                    }

                    if (items.isNotEmpty()) {
                        conn.prepareStatement(upsertItemSql).use { stmt ->
                            for (it in items) {
                                stmt.setLong(1, it.id)
                                stmt.setLong(2, it.invoiceId)
                                stmt.setLong(3, it.productId)
                                stmt.setString(4, it.productName)
                                stmt.setInt(5, it.quantity)
                                stmt.setDouble(6, it.unitPrice)
                                stmt.setDouble(7, it.totalPrice)
                                stmt.setString(8, it.dosageInstructions)
                                stmt.setString(9, it.stickerLabel)
                                stmt.addBatch()
                            }
                            stmt.executeBatch()
                        }
                    }

                    conn.commit()
                    Result.success(invoices.size)
                } catch (ex: Exception) {
                    conn.rollback()
                    throw ex
                } finally {
                    conn.autoCommit = true
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Batch sync sales to MySQL failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * استعلام وتجميع المبيعات اليومية مباشرة من خادم MySQL (Daily Sales Stats)
     */
    suspend fun fetchDailySalesStats(daysLimit: Int = 7): Result<List<MySqlDailySalesSummary>> = withContext(Dispatchers.IO) {
        val sql = """
            SELECT 
                FROM_UNIXTIME(timestamp / 1000, '%Y-%m-%d') AS sale_date,
                SUM(totalAmount) AS total_revenue,
                COUNT(*) AS invoice_count,
                COALESCE(SUM(itemCount), 0) AS total_items,
                MAX(timestamp) AS max_timestamp
            FROM `sale_invoices`
            GROUP BY sale_date
            ORDER BY sale_date DESC
            LIMIT ?
        """.trimIndent()

        try {
            val list = mutableListOf<MySqlDailySalesSummary>()
            val arabicFormat = SimpleDateFormat("EEEE", Locale("ar"))
            val dateParser = SimpleDateFormat("yyyy-MM-dd", Locale.US)

            createConnection().use { conn ->
                conn.prepareStatement(sql).use { stmt ->
                    stmt.setInt(1, daysLimit)
                    stmt.executeQuery().use { rs ->
                        while (rs.next()) {
                            val dateStr = rs.getString("sale_date") ?: ""
                            val revenue = rs.getDouble("total_revenue")
                            val count = rs.getInt("invoice_count")
                            val items = rs.getInt("total_items")
                            val maxTs = rs.getLong("max_timestamp")

                            val dayName = try {
                                val parsedDate = dateParser.parse(dateStr)
                                if (parsedDate != null) arabicFormat.format(parsedDate) else "يوم"
                            } catch (e: Exception) {
                                "يوم"
                            }

                            list.add(
                                MySqlDailySalesSummary(
                                    dateString = dateStr,
                                    dayNameArabic = dayName,
                                    totalRevenue = revenue,
                                    invoiceCount = count,
                                    totalItems = items,
                                    timestamp = if (maxTs > 0) maxTs else System.currentTimeMillis()
                                )
                            )
                        }
                    }
                }
            }
            Result.success(list)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching daily sales stats from MySQL: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * استعلام الأصناف الأكثر مبيعاً مباشرة من خادم MySQL (Top Selling Items)
     */
    suspend fun fetchTopSellingItems(limit: Int = 10): Result<List<MySqlTopSellingItem>> = withContext(Dispatchers.IO) {
        val sql = """
            SELECT 
                productId,
                productName,
                SUM(quantity) AS total_qty,
                SUM(totalPrice) AS total_sales,
                COUNT(DISTINCT invoiceId) AS inv_count
            FROM `sale_items`
            GROUP BY productId, productName
            ORDER BY total_qty DESC
            LIMIT ?
        """.trimIndent()

        try {
            val list = mutableListOf<MySqlTopSellingItem>()
            var grandTotalQty = 0

            createConnection().use { conn ->
                conn.prepareStatement(sql).use { stmt ->
                    stmt.setInt(1, limit)
                    stmt.executeQuery().use { rs ->
                        while (rs.next()) {
                            val pid = rs.getLong("productId")
                            val pName = rs.getString("productName") ?: "منتج"
                            val qty = rs.getInt("total_qty")
                            val sales = rs.getDouble("total_sales")
                            val invCount = rs.getInt("inv_count")

                            grandTotalQty += qty
                            list.add(
                                MySqlTopSellingItem(
                                    productId = pid,
                                    productName = pName,
                                    totalQuantitySold = qty,
                                    totalRevenue = sales,
                                    invoiceCount = invCount,
                                    percentageOfTotal = 0f
                                )
                            )
                        }
                    }
                }
            }

            val adjusted = if (grandTotalQty > 0) {
                list.map { item ->
                    item.copy(percentageOfTotal = (item.totalQuantitySold.toFloat() / grandTotalQty.toFloat()) * 100f)
                }
            } else {
                list
            }
            Result.success(adjusted)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching top selling items from MySQL: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * إحصاءات ملخصة عامة للمبيعات من خادم MySQL
     */
    suspend fun fetchSalesOverview(): Result<MySqlSalesOverview> = withContext(Dispatchers.IO) {
        val sql = """
            SELECT 
                COUNT(*) AS total_invoices,
                COALESCE(SUM(totalAmount), 0.0) AS total_revenue,
                COALESCE(AVG(totalAmount), 0.0) AS avg_ticket,
                COALESCE(SUM(itemCount), 0) AS total_units,
                COALESCE(MAX(timestamp), 0) AS max_time
            FROM `sale_invoices`
        """.trimIndent()

        try {
            createConnection().use { conn ->
                conn.createStatement().use { stmt ->
                    stmt.executeQuery(sql).use { rs ->
                        if (rs.next()) {
                            val totalInvoices = rs.getInt("total_invoices")
                            val totalRevenue = rs.getDouble("total_revenue")
                            val avgTicket = rs.getDouble("avg_ticket")
                            val totalUnits = rs.getInt("total_units")
                            val maxTime = rs.getLong("max_time")

                            Result.success(
                                MySqlSalesOverview(
                                    totalInvoices = totalInvoices,
                                    totalRevenue = totalRevenue,
                                    avgInvoiceValue = avgTicket,
                                    totalUnitsSold = totalUnits,
                                    lastSaleTimestamp = maxTime,
                                    isFromRemoteMySql = true,
                                    sourceLabel = "خادم MySQL البعيد 🐬"
                                )
                            )
                        } else {
                            Result.success(
                                MySqlSalesOverview(
                                    totalInvoices = 0,
                                    totalRevenue = 0.0,
                                    avgInvoiceValue = 0.0,
                                    totalUnitsSold = 0,
                                    lastSaleTimestamp = 0L,
                                    isFromRemoteMySql = true,
                                    sourceLabel = "خادم MySQL البعيد 🐬 (فارغ)"
                                )
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching sales overview from MySQL: ${e.message}", e)
            Result.failure(e)
        }
    }
}
