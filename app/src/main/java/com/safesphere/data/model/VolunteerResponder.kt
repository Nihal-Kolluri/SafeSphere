package com.safesphere.data.model

/**
 * Represents a community first-responder volunteer within 1-2 km.
 */
data class VolunteerResponder(
    val id: String,
    val name: String,
    val distanceMeters: Int,
    val estimatedArrivalMinutes: Int,
    val status: VolunteerStatus = VolunteerStatus.ALERTED,
    val hasFirstAidCertification: Boolean = false,
    val phoneMasked: String = "••• ••• 412"
)

enum class VolunteerStatus {
    ALERTED,
    RESPONDING,
    ARRIVED
}
