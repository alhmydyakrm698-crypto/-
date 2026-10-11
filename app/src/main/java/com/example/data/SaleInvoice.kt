package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sale_invoices")
data class SaleInvoice(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val invoiceNumber: String,
    val timestamp: Long = System.currentTimeMillis(),
    val totalAmount: Double,
    val itemCount: Int,
    val paymentMethod: String = "نقدي", // "نقدي", "بطاقة مدى", "دفع آجل (حساب العميل)"
    val cashierName: String = "د. أحمد الفاروقي",
    val customerId: Long? = null,
    val customerName: String = "",
    val customerPhone: String = "",
    val isCreditSale: Boolean = false
)

@Entity(tableName = "sale_items")
data class SaleItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val invoiceId: Long,
    val productId: Long,
    val productName: String,
    val quantity: Int,
    val unitPrice: Double,
    val totalPrice: Double,
    val dosageInstructions: String = "", // إرشادات الجرعة للمريض
    val stickerLabel: String = ""        // نص الملصق الطبي اللاصق على العلبة
)
