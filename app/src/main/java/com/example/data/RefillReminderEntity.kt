package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * كيان التذكير بمواعيد إعادة صرف أدوية الأمراض المزمنة (RefillReminderEntity)
 */
@Entity(tableName = "refill_reminders")
data class RefillReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerId: Long = 0,
    val customerName: String,
    val customerPhone: String,
    val medicineName: String,
    val lastRefillDate: Long = System.currentTimeMillis(),
    val daysSupply: Int = 30, // عدد أيام الجرعة (المدة المقدرة لنفاد العلاج)
    val nextRefillDate: Long = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000),
    val isContacted: Boolean = false,
    val notes: String = "علاج شهري منتظم"
)
