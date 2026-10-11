package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * كيان مرتجع المبيعات (SaleReturnEntity)
 * يسجل حركة المرتجعات مع ربطها بالفاتورة الأصلية ورقم التشغيلة والوحدة الصغرى
 */
@Entity(tableName = "sale_returns")
data class SaleReturn(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val invoiceNumber: String,          // 1. رقم الفاتورة (Invoice Number)
    val productId: Long = 0,
    val itemName: String,               // 2. اسم الصنف (Item Name)
    val barcode: String = "",
    val expiryDate: String,             // 3. تاريخ الانتهاء (Expiry Date - لتحديد التشغيلة)
    val amount: Double,                 // 4. المبلغ المستحق للمراجعة (Amount)
    val smallestUnit: String,           // 5. الوحدة الأصغر (Smallest Unit: قرص، شريط، أمبولة، حبة...)
    val returnedQuantity: Int,          // 6. الكمية المراجعة (Returned Quantity)
    val returnReason: String = "طلب العميل",
    val refundMethod: String = "نقدي (كاش)", // نقدي، رصيد حساب عميل، شبكة
    val customerName: String = "",
    val pharmacistName: String = "الصيدلي المناوب",
    val timestamp: Long = System.currentTimeMillis()
)
