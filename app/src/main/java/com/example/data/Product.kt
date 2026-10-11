package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * كيان الدواء والمنتج (MedicineEntity / Product)
 * يحتوي على المادة الفعالة والشكل الصيدلاني والتركيز لمحرك البدائل الذكي
 */
@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val manufacturer: String,
    val activeIngredient: String,
    val dosageForm: String = "أقراص", // الشكل الصيدلاني (أقراص، كبسولات، شراب، أمبولات...)
    val strength: String = "",       // التركيز (500mg, 1g, 250mg/5ml...)
    val unit: String,
    val expiryDate: String,
    val unitPrice: Double,
    val wholesalePrice: Double,
    val barcode: String,
    val quantity: Int,
    val minStockLevel: Int = 5,
    val category: String = "عام",
    val preferredSupplier: String = "الشركة الوطنية للدواء",
    val dailyBurnRate: Double = 1.2, // معدل الاستهلاك اليومي المقدر
    val usageInstructions: String = "", // 1 - الاستخدام ودواعي الاستعمال
    val contraindications: String = "", // 2 - الحالات التي يمنع استخدام الدواء فيها
    val targetAgeGroup: String = "",    // 3 - الفئة العمرية التي يستخدم لها
    val drugInteractions: String = "",  // 4 - التداخلات الدوائية
    // الحقول والتواريخ الحيوية وإدارة المخزون
    val productionDate: String = "",    // تاريخ الإنتاج الحيوي
    val batchNumber: String = "",       // رقم التشغيلة / الوجبة
    val alertDaysBeforeExpiry: Int = 90,// عدد الأيام للإنذار المبكر قبل انتهاء الصلاحية
    val storageLocation: String = "الرف A1", // موقع التخزين والرف في الصيدلية
    val maxStockLevel: Int = 100,       // الحد الأقصى للطاقة الاستيعابية للمخزون
    val updatedAt: Long = System.currentTimeMillis()
)

typealias MedicineEntity = Product
