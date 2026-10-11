package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * كيان فئات وتصنيفات الأدوية في قاعدة بيانات Room (Medicine Categories Table)
 * لإدارة فئات الأدوية، التصنيفات الدوائية، شروط التخزين، وأذونات الصرف
 */
@Entity(tableName = "medicine_categories")
data class MedicineCategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,                           // اسم الفئة (مثل: مسكنات وخافضات حرارة، مضادات حيوية...)
    val code: String,                           // كود الفئة (مثل: ANALGESIC, ANTIBIOTIC, CARDIO...)
    val description: String = "",               // الوصف الصيدلاني وطريقة عمل الفئة
    val iconName: String = "category",          // رمز الأيقونة
    val colorHex: String = "#10B981",           // لون التمييز البصري
    val requiresPrescription: Boolean = false,  // هل تتطلب وصفة طبية إلزامية (Rx Only)
    val storageConditions: String = "يحفظ بدرجة حرارة الغرفة (أقل من 25° مئوية)", // شروط التخزين الحيوية
    val displayOrder: Int = 0                   // ترتيب العرض
)
