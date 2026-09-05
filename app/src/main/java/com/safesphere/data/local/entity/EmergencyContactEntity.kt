package com.safesphere.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Persisted emergency contact (Family, Guardian, Close Friend).
 */
@Entity(tableName = "emergency_contacts")
data class EmergencyContactEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phoneNumber: String,
    val relationship: String = "Family",
    val notifyViaSms: Boolean = true,
    val notifyViaCall: Boolean = true,
    val isPrimary: Boolean = false
)
