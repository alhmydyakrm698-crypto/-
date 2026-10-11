package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * كيان بيانات العميل والحد الائتماني (CustomerEntity)
 */
@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String,
    val currentBalance: Double = 0.0, // رصيد المديونية المستحق على العميل (ر.س)
    val creditLimit: Double = 1000.0,  // السقف الائتماني المسموح به للدفع الآجل (ر.س)
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
