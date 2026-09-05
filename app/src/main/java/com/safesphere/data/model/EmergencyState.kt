package com.safesphere.data.model

/**
 * State machine representation for an active or idle emergency incident.
 */
data class EmergencyState(
    val isActive: Boolean = false,
    val isSilentMode: Boolean = false,
    val isRescuePowerSaverActive: Boolean = false,
    val incidentId: String = "",
    val activeTier: EscalationTier = EscalationTier.TIER_1_FAMILY,
    val tierRemainingSeconds: Int = EscalationTier.TIER_1_FAMILY.defaultDurationSeconds,
    val latestTelemetry: LocationTelemetry? = null,
    val responders: List<VolunteerResponder> = emptyList(),
    val isDuressActive: Boolean = false,
    val isSimulated: Boolean = false,
    val dispatchedSmsLogs: List<String> = emptyList(),
    val eventLogs: List<String> = emptyList(),
    val isCountdownActive: Boolean = false,
    val countdownRemainingSeconds: Int = 5
)
