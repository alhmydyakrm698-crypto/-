package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PharmacyDao {

    // ==========================================
    // 1. Products & Smart Alternatives
    // ==========================================
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products ORDER BY id ASC")
    suspend fun getAllProductsStatic(): List<Product>

    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' OR barcode LIKE '%' || :query || '%' OR activeIngredient LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchProducts(query: String): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Long): Product?

    @Query("SELECT * FROM products WHERE barcode = :barcode LIMIT 1")
    suspend fun getProductByBarcode(barcode: String): Product?

    @Query("SELECT * FROM products WHERE name = :name LIMIT 1")
    suspend fun getProductByName(name: String): Product?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllProducts(products: List<Product>)

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Query("UPDATE products SET quantity = :newQuantity WHERE id = :productId")
    suspend fun updateProductQuantity(productId: Long, newQuantity: Int)

    @Query("SELECT COUNT(*) FROM products")
    suspend fun getProductCount(): Int

    // استعلام محرك البدائل المتطابقة مع نفس المادة الفعالة برصيد متوفر واستبعاد الدواء الحالي، مرتبة تصاعدياً حسب السعر
    @Query("SELECT * FROM products WHERE activeIngredient = :activeIngredient AND id != :currentId AND quantity > 0 ORDER BY unitPrice ASC")
    suspend fun getDrugAlternatives(activeIngredient: String, currentId: Long): List<Product>

    @Query("SELECT * FROM products WHERE activeIngredient = :activeIngredient AND id != :currentId AND quantity > 0 ORDER BY unitPrice ASC")
    fun getDrugAlternativesFlow(activeIngredient: String, currentId: Long): Flow<List<Product>>

    // ==========================================
    // 2. Drug Interaction Safety Checker
    // ==========================================
    @Query("SELECT * FROM drug_interactions")
    fun getAllInteractions(): Flow<List<DrugInteractionEntity>>

    @Query("""
        SELECT * FROM drug_interactions 
        WHERE (ingredientA IN (:ingredients) AND ingredientB IN (:ingredients))
    """)
    suspend fun findInteractions(ingredients: List<String>): List<DrugInteractionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInteraction(interaction: DrugInteractionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllInteractions(interactions: List<DrugInteractionEntity>)

    @Query("SELECT COUNT(*) FROM drug_interactions")
    suspend fun getInteractionCount(): Int

    // ==========================================
    // 3. Smart Dosage & Instructions
    // ==========================================
    @Query("SELECT * FROM medicine_dosages WHERE medicineId = :medicineId LIMIT 1")
    suspend fun getDosageForMedicine(medicineId: Long): MedicineDosageEntity?

    @Query("SELECT * FROM medicine_dosages WHERE activeIngredient = :activeIngredient LIMIT 1")
    suspend fun getDosageForIngredient(activeIngredient: String): MedicineDosageEntity?

    @Query("SELECT * FROM medicine_dosages")
    fun getAllDosages(): Flow<List<MedicineDosageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDosage(dosage: MedicineDosageEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllDosages(dosages: List<MedicineDosageEntity>)

    @Query("SELECT COUNT(*) FROM medicine_dosages")
    suspend fun getDosageCount(): Int

    // ==========================================
    // 4. Customers & Credit Ledger
    // ==========================================
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers ORDER BY id ASC")
    suspend fun getAllCustomersStatic(): List<CustomerEntity>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getCustomerById(id: Long): CustomerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCustomers(customers: List<CustomerEntity>)

    @Query("SELECT COUNT(*) FROM customers")
    suspend fun getCustomerCount(): Int

    @Update
    suspend fun updateCustomer(customer: CustomerEntity)

    @Delete
    suspend fun deleteCustomer(customer: CustomerEntity)

    @Query("SELECT * FROM customer_ledgers WHERE customerId = :customerId ORDER BY timestamp DESC")
    fun getCustomerLedger(customerId: Long): Flow<List<CustomerLedgerEntity>>

    @Query("SELECT * FROM customer_ledgers ORDER BY id ASC")
    suspend fun getAllLedgerEntriesStatic(): List<CustomerLedgerEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLedgerEntry(entry: CustomerLedgerEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllLedgerEntries(entries: List<CustomerLedgerEntity>)

    // ==========================================
    // 5. Refill Reminders for Chronic Diseases
    // ==========================================
    @Query("SELECT * FROM refill_reminders ORDER BY nextRefillDate ASC")
    fun getAllRefillReminders(): Flow<List<RefillReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRefillReminder(reminder: RefillReminderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllRefillReminders(reminders: List<RefillReminderEntity>)

    @Query("SELECT COUNT(*) FROM refill_reminders")
    suspend fun getRefillReminderCount(): Int

    @Update
    suspend fun updateRefillReminder(reminder: RefillReminderEntity)

    @Delete
    suspend fun deleteRefillReminder(reminder: RefillReminderEntity)

    // ==========================================
    // Users
    // ==========================================
    @Query("SELECT * FROM users ORDER BY id ASC")
    fun getAllUsers(): Flow<List<PharmacyUser>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: PharmacyUser): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllUsers(users: List<PharmacyUser>)

    @Update
    suspend fun updateUser(user: PharmacyUser)

    @Delete
    suspend fun deleteUser(user: PharmacyUser)

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int

    // ==========================================
    // Invoices & Sales
    // ==========================================
    @Query("SELECT * FROM sale_invoices ORDER BY timestamp DESC")
    fun getAllInvoices(): Flow<List<SaleInvoice>>

    @Query("SELECT * FROM sale_invoices ORDER BY id ASC")
    suspend fun getAllInvoicesStatic(): List<SaleInvoice>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: SaleInvoice): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleItems(items: List<SaleItem>)

    @Query("SELECT * FROM sale_items WHERE invoiceId = :invoiceId")
    suspend fun getSaleItemsForInvoice(invoiceId: Long): List<SaleItem>

    @Query("SELECT * FROM sale_items")
    suspend fun getAllSaleItemsStatic(): List<SaleItem>

    @Query("SELECT * FROM sale_items")
    fun getAllSaleItemsFlow(): Flow<List<SaleItem>>

    @Query("SELECT SUM(totalAmount) FROM sale_invoices")
    fun getTotalRevenueFlow(): Flow<Double?>

    @Query("SELECT COUNT(*) FROM sale_invoices")
    fun getTotalInvoiceCountFlow(): Flow<Int>

    // ==========================================
    // 6. Vital Dates & Stock Quantities Queries (التواريخ الحيوية وإدارة المخزون)
    // ==========================================
    @Query("SELECT * FROM products WHERE quantity <= minStockLevel ORDER BY quantity ASC")
    fun getLowStockProductsFlow(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE expiryDate <= :thresholdDate ORDER BY expiryDate ASC")
    fun getNearExpiryProductsFlow(thresholdDate: String): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE category = :category ORDER BY name ASC")
    fun getProductsByCategoryFlow(category: String): Flow<List<Product>>

    @Query("SELECT DISTINCT category FROM products ORDER BY category ASC")
    fun getAllCategoryNamesFlow(): Flow<List<String>>

    @Query("SELECT SUM(quantity) FROM products")
    fun getTotalStockQuantityFlow(): Flow<Int?>

    @Query("SELECT COUNT(*) FROM products WHERE quantity <= minStockLevel")
    fun getLowStockCountFlow(): Flow<Int>

    // ==========================================
    // 7. Medicine Categories (فئات الأدوية وتصنيفاتها)
    // ==========================================
    @Query("SELECT * FROM medicine_categories ORDER BY displayOrder ASC, name ASC")
    fun getAllCategories(): Flow<List<MedicineCategoryEntity>>

    @Query("SELECT * FROM medicine_categories ORDER BY id ASC")
    suspend fun getAllCategoriesStatic(): List<MedicineCategoryEntity>

    @Query("SELECT * FROM medicine_categories WHERE id = :id")
    suspend fun getCategoryById(id: Long): MedicineCategoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: MedicineCategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCategories(categories: List<MedicineCategoryEntity>)

    @Update
    suspend fun updateCategory(category: MedicineCategoryEntity)

    @Delete
    suspend fun deleteCategory(category: MedicineCategoryEntity)

    @Query("SELECT COUNT(*) FROM medicine_categories")
    suspend fun getCategoryCount(): Int

    // ==========================================
    // 8. Sales Returns & Revisions (إدارة المراجعات والمرتجعات)
    // ==========================================
    @Query("SELECT * FROM sale_returns ORDER BY timestamp DESC")
    fun getAllSaleReturns(): Flow<List<SaleReturn>>

    @Query("SELECT * FROM sale_returns WHERE invoiceNumber = :invoiceNumber ORDER BY timestamp DESC")
    fun getSaleReturnsByInvoice(invoiceNumber: String): Flow<List<SaleReturn>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleReturn(saleReturn: SaleReturn): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSaleReturns(saleReturns: List<SaleReturn>)

    @Delete
    suspend fun deleteSaleReturn(saleReturn: SaleReturn)

    @Query("SELECT SUM(amount) FROM sale_returns")
    fun getTotalReturnsAmountFlow(): Flow<Double?>
}
