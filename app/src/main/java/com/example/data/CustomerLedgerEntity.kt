package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * كيان كشف حساب وحركات ديون العميل (CustomerLedgerEntity)
 */
@Entity(tableName = "customer_ledgers")
data class CustomerLedgerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerId: Long,
    val invoiceId: Long? = null,
    val transactionType: String, // "DEBIT_SALE" (شراء آجل) أو "PAYMENT" (سداد دفعة)
    val amount: Double,          // قيمة المعاملة
    val balanceAfter: Double,    // الرصيد بعد الحركة
    val note: String = "",       // بيان الحركة
    val timestamp: Long = System.currentTimeMillis()
)
