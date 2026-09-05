package com.safesphere.core.simulation

import com.safesphere.data.model.LocationTelemetry
import com.safesphere.data.model.VolunteerResponder
import com.safesphere.data.model.VolunteerStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Controller for testing the emergency pipeline safely without real SMS or 911 calls.
 */
class EmergencySimulator {

    private val _isSimulationActive = MutableStateFlow(false)
    val isSimulationActive: StateFlow<Boolean> = _isSimulationActive.asStateFlow()

    private val _simulatedBattery = MutableStateFlow(85)
    val simulatedBattery: StateFlow<Int> = _simulatedBattery.asStateFlow()

    private val _simulatedResponders = MutableStateFlow<List<VolunteerResponder>>(emptyList())
    val simulatedResponders: StateFlow<List<VolunteerResponder>> = _simulatedResponders.asStateFlow()

    fun setSimulationActive(active: Boolean) {
        _isSimulationActive.value = active
        if (active && _simulatedResponders.value.isEmpty()) {
            _simulatedResponders.value = MockVolunteerGenerator.generateNearbyVolunteers(3)
        }
    }

    fun setSimulatedBattery(level: Int) {
        _simulatedBattery.value = level.coerceIn(1, 100)
    }

    /**
     * Simulates a volunteer accepting the SOS call ("I'm Responding").
     */
    fun simulateVolunteerAcceptance() {
        val currentList = _simulatedResponders.value
        val updated = currentList.mapIndexed { index, responder ->
            if (index == 0 && responder.status == VolunteerStatus.ALERTED) {
                responder.copy(status = VolunteerStatus.RESPONDING)
            } else if (index == 1 && responder.status == VolunteerStatus.ALERTED) {
                responder.copy(status = VolunteerStatus.RESPONDING)
            } else {
                responder
            }
        }
        _simulatedResponders.value = updated
    }

    fun createSimulatedTelemetry(lat: Double = 37.7749, lng: Double = -122.4194): LocationTelemetry {
        return LocationTelemetry(
            latitude = lat,
            longitude = lng,
            accuracyMeters = 5.0f,
            speedMps = 1.2f,
            altitudeMeters = 24.0,
            bearingDegrees = 180.0f,
            batteryPercentage = _simulatedBattery.value,
            isCharging = false,
            timestampMillis = System.currentTimeMillis()
        )
    }
}
