package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * كيان التعارضات والتفاعلات الدوائية (DrugInteractionEntity)
 */
@Entity(tableName = "drug_interactions")
data class DrugInteractionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ingredientA: String,
    val ingredientB: String,
    val severityLevel: String, // "HIGH", "MEDIUM", "LOW"
    val description: String,
    val recommendation: String
)
