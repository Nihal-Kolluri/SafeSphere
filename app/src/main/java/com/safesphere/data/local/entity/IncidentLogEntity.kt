package com.safesphere.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Historical record of a triggered emergency incident.
 */
@Entity(tableName = "incident_logs")
data class IncidentLogEntity(
    @PrimaryKey
    val incidentId: String,
    val startTimestamp: Long,
    val endTimestamp: Long? = null,
    val maxEscalationTierReached: Int,
    val isSilentMode: Boolean,
    val isDuressTriggered: Boolean,
    val finalLatitude: Double?,
    val finalLongitude: Double?,
    val finalBatteryPercentage: Int,
    val volunteerCountAlerted: Int,
    val wasSimulated: Boolean = false
)
