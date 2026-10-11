package com.example.data

import kotlinx.coroutines.flow.Flow

class PharmacyRepository(private val dao: PharmacyDao) {

    val allProducts: Flow<List<Product>> = dao.getAllProducts()
    val allUsers: Flow<List<PharmacyUser>> = dao.getAllUsers()
    val allInvoices: Flow<List<SaleInvoice>> = dao.getAllInvoices()
    val totalRevenue: Flow<Double?> = dao.getTotalRevenueFlow()
    val totalInvoiceCount: Flow<Int> = dao.getTotalInvoiceCountFlow()

    // 1. Smart Drug Alternatives Engine
    suspend fun getDrugAlternatives(activeIngredient: String, currentId: Long): List<Product> =
        dao.getDrugAlternatives(activeIngredient, currentId)

    // 2. Drug Interaction Safety Checker
    val allInteractions: Flow<List<DrugInteractionEntity>> = dao.getAllInteractions()

    suspend fun checkInteractions(ingredients: List<String>): List<DrugInteractionEntity> {
        if (ingredients.size < 2) return emptyList()
        return dao.findInteractions(ingredients)
    }

    // 3. Smart Dosage & Instructions
    suspend fun getDosageForMedicine(medicineId: Long): MedicineDosageEntity? =
        dao.getDosageForMedicine(medicineId)

    suspend fun getDosageForIngredient(activeIngredient: String): MedicineDosageEntity? =
        dao.getDosageForIngredient(activeIngredient)

    val allDosages: Flow<List<MedicineDosageEntity>> = dao.getAllDosages()

    // 4. Customers & Credit Ledger
    val allCustomers: Flow<List<CustomerEntity>> = dao.getAllCustomers()

    fun getCustomerLedger(customerId: Long): Flow<List<CustomerLedgerEntity>> =
        dao.getCustomerLedger(customerId)

    suspend fun getCustomerById(id: Long): CustomerEntity? = dao.getCustomerById(id)

    suspend fun saveCustomer(customer: CustomerEntity): Long {
        return if (customer.id > 0) {
            dao.updateCustomer(customer)
            customer.id
        } else {
            dao.insertCustomer(customer)
        }
    }

    suspend fun recordCustomerPayment(customerId: Long, amount: Double, note: String) {
        val customer = dao.getCustomerById(customerId) ?: return
        val newBalance = (customer.currentBalance - amount).coerceAtLeast(0.0)
        dao.updateCustomer(customer.copy(currentBalance = newBalance))

        dao.insertLedgerEntry(
            CustomerLedgerEntity(
                customerId = customerId,
                transactionType = "PAYMENT",
                amount = amount,
                balanceAfter = newBalance,
                note = note.ifBlank { "سداد دفعة نقدية" }
            )
        )
    }

    // 5. Refill Reminders for Chronic Diseases
    val allRefillReminders: Flow<List<RefillReminderEntity>> = dao.getAllRefillReminders()

    suspend fun saveRefillReminder(reminder: RefillReminderEntity): Long {
        return if (reminder.id > 0) {
            dao.updateRefillReminder(reminder)
            reminder.id
        } else {
            dao.insertRefillReminder(reminder)
        }
    }

    suspend fun markReminderContacted(reminder: RefillReminderEntity) {
        dao.updateRefillReminder(reminder.copy(isContacted = true))
    }

    suspend fun deleteRefillReminder(reminder: RefillReminderEntity) {
        dao.deleteRefillReminder(reminder)
    }

    // Standard product operations
    fun searchProducts(query: String): Flow<List<Product>> = dao.searchProducts(query)

    suspend fun getProductById(id: Long): Product? = dao.getProductById(id)

    suspend fun getProductByBarcode(barcode: String): Product? = dao.getProductByBarcode(barcode)

    suspend fun insertProduct(product: Product): Long = dao.insertProduct(product)

    suspend fun updateProduct(product: Product) = dao.updateProduct(product)

    suspend fun deleteProduct(product: Product) = dao.deleteProduct(product)

    suspend fun updateProductQuantity(productId: Long, newQuantity: Int) =
        dao.updateProductQuantity(productId, newQuantity)

    // Standard user operations
    suspend fun insertUser(user: PharmacyUser): Long = dao.insertUser(user)

    suspend fun updateUser(user: PharmacyUser) = dao.updateUser(user)

    suspend fun deleteUser(user: PharmacyUser) = dao.deleteUser(user)

    // Sale processing (Cash, Card, or Credit Debt)
    suspend fun processSale(
        invoice: SaleInvoice,
        items: List<SaleItem>,
        customerId: Long? = null
    ): Long {
        val invoiceId = dao.insertInvoice(invoice)
        val itemsWithId = items.map { it.copy(invoiceId = invoiceId) }
        dao.insertSaleItems(itemsWithId)

        // Deduct quantities from products
        for (item in items) {
            val product = dao.getProductById(item.productId)
            if (product != null) {
                val newQty = (product.quantity - item.quantity).coerceAtLeast(0)
                dao.updateProductQuantity(product.id, newQty)
            }
        }

        // If Credit sale, update customer debt balance and record in ledger
        if (invoice.isCreditSale && customerId != null) {
            val customer = dao.getCustomerById(customerId)
            if (customer != null) {
                val newBal = customer.currentBalance + invoice.totalAmount
                dao.updateCustomer(customer.copy(currentBalance = newBal))
                dao.insertLedgerEntry(
                    CustomerLedgerEntity(
                        customerId = customerId,
                        invoiceId = invoiceId,
                        transactionType = "DEBIT_SALE",
                        amount = invoice.totalAmount,
                        balanceAfter = newBal,
                        note = "مشتريات فاتورة آجل #${invoice.invoiceNumber}"
                    )
                )
            }
        }

        return invoiceId
    }

    suspend fun ensureSeeded() {
        if (dao.getProductCount() == 0 || dao.getInteractionCount() == 0 || dao.getCustomerCount() == 0 || dao.getCategoryCount() == 0) {
            PharmacyDatabase.populateInitialData(dao)
        } else {
            PharmacyDatabase.seedInitialInvoicesIfEmpty(dao)
        }
    }

    // ==========================================
    // Medicine Categories (فئات الأدوية)
    // ==========================================
    val allCategories: Flow<List<MedicineCategoryEntity>> = dao.getAllCategories()
    suspend fun getAllCategoriesStatic(): List<MedicineCategoryEntity> = dao.getAllCategoriesStatic()
    suspend fun getCategoryById(id: Long): MedicineCategoryEntity? = dao.getCategoryById(id)
    suspend fun insertCategory(category: MedicineCategoryEntity): Long = dao.insertCategory(category)
    suspend fun updateCategory(category: MedicineCategoryEntity) = dao.updateCategory(category)
    suspend fun deleteCategory(category: MedicineCategoryEntity) = dao.deleteCategory(category)

    // ==========================================
    // Vital Dates & Inventory Stock Quantities
    // ==========================================
    val lowStockProducts: Flow<List<Product>> = dao.getLowStockProductsFlow()
    fun getNearExpiryProducts(thresholdDate: String): Flow<List<Product>> = dao.getNearExpiryProductsFlow(thresholdDate)
    fun getProductsByCategory(category: String): Flow<List<Product>> = dao.getProductsByCategoryFlow(category)
    val allCategoryNames: Flow<List<String>> = dao.getAllCategoryNamesFlow()
    val totalStockQuantity: Flow<Int?> = dao.getTotalStockQuantityFlow()
    val lowStockCount: Flow<Int> = dao.getLowStockCountFlow()

    // ==========================================
    // Sales Returns & Revisions (إدارة المراجعات والمرتجعات)
    // ==========================================
    val allSaleReturns: Flow<List<SaleReturn>> = dao.getAllSaleReturns()
    fun getSaleReturnsByInvoice(invoiceNumber: String): Flow<List<SaleReturn>> = dao.getSaleReturnsByInvoice(invoiceNumber)
    suspend fun insertSaleReturn(saleReturn: SaleReturn): Long = dao.insertSaleReturn(saleReturn)
    suspend fun insertAllSaleReturns(saleReturns: List<SaleReturn>) = dao.insertAllSaleReturns(saleReturns)
    suspend fun deleteSaleReturn(saleReturn: SaleReturn) = dao.deleteSaleReturn(saleReturn)
    val totalReturnsAmount: Flow<Double?> = dao.getTotalReturnsAmountFlow()

    /**
     * دالة معالجة وحفظ المرتجع في قاعدة البيانات:
     * 1. توثيق العملية في سجل حركات المرتجعات (sale_returns)
     * 2. زيادة رصيد الصنف المسترجع في جدول المخزون (products)
     * 3. إرجاع بيانات الصنف المحدث مع الرصيد الجديد
     */
    suspend fun processSaleReturnTransaction(
        saleReturn: SaleReturn,
        targetProduct: Product? = null
    ): Pair<Long, Product?> {
        // 1. توثيق حركة المرتجع في جدول المرتجعات
        val returnId = dao.insertSaleReturn(saleReturn)

        // 2. العثور على الصنف في قاعدة بيانات المخزون
        val product = targetProduct
            ?: if (saleReturn.productId > 0) dao.getProductById(saleReturn.productId) else null
            ?: if (saleReturn.barcode.isNotBlank()) dao.getProductByBarcode(saleReturn.barcode) else null
            ?: dao.getProductByName(saleReturn.itemName)

        var updatedProduct: Product? = null

        // 3. زيادة رصيد الصنف في جدول المخزون
        if (product != null) {
            val newQuantity = product.quantity + saleReturn.returnedQuantity
            val productToUpdate = product.copy(
                quantity = newQuantity,
                updatedAt = System.currentTimeMillis()
            )
            dao.updateProduct(productToUpdate)
            updatedProduct = productToUpdate
        }

        return Pair(returnId, updatedProduct)
    }

    suspend fun getAllInvoicesStatic(): List<SaleInvoice> = dao.getAllInvoicesStatic()
    suspend fun getAllSaleItemsStatic(): List<SaleItem> = dao.getAllSaleItemsStatic()
}
