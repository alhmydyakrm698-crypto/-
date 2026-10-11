package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * كيان الجرعات والتعليمات الطبية للأدوية (MedicineDosageEntity)
 */
@Entity(tableName = "medicine_dosages")
data class MedicineDosageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val medicineId: Long = 0,
    val medicineName: String = "",
    val activeIngredient: String = "",
    val adultDosageText: String,             // جرعة البالغين (مثال: قرص واحد كل 8 ساعات بعد الأكل)
    val pediatricDosagePerKg: Double = 0.0,  // جرعة الأطفال بالملغم لكل كجم من وزن الطفل
    val usageInstructions: String,           // إرشادات الاستخدام
    val precautions: String                  // تحذيرات واحتياطات هامة
)
