package com.safesphere.data.repository

import com.safesphere.core.security.PinValidationResult
import com.safesphere.data.local.entity.EmergencyContactEntity
import com.safesphere.data.local.entity.IncidentLogEntity
import com.safesphere.data.model.EmergencyState
import com.safesphere.data.model.MedicalProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface EmergencyRepository {
    val emergencyState: StateFlow<EmergencyState>
    val contacts: Flow<List<EmergencyContactEntity>>
    val incidentHistory: Flow<List<IncidentLogEntity>>
    val medicalProfile: StateFlow<MedicalProfile>
    val isVolunteerModeEnabled: StateFlow<Boolean>

    fun startCountdown(isSilent: Boolean, isSimulated: Boolean)
    fun abortCountdown()
    fun triggerEmergencyDirectly(isSilent: Boolean, isSimulated: Boolean)
    fun validatePinAndCancel(pin: String): PinValidationResult
    fun escalateImmediatelyToAuthorities()
    fun toggleRescuePowerSaver()
    fun setVolunteerModeEnabled(enabled: Boolean)

    suspend fun addContact(contact: EmergencyContactEntity): Long
    suspend fun deleteContact(contact: EmergencyContactEntity)
    suspend fun updateMedicalProfile(profile: MedicalProfile)

    // Simulation hooks
    fun setSimulatedBattery(level: Int)
    fun simulateVolunteerAcceptance()
}
