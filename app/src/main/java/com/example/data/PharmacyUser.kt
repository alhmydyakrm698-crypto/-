package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class PharmacyUser(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val username: String,
    val phone: String,
    val role: String, // "مدير", "كاشير", "موظف مخزون"
    val pharmacyName: String = "صيدلية النخبة الطبية",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
