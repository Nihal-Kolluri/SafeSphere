package com.safesphere.core.simulation

import com.safesphere.data.model.VolunteerResponder
import com.safesphere.data.model.VolunteerStatus
import java.util.UUID

/**
 * Generates mock nearby community volunteers for testing and development.
 */
object MockVolunteerGenerator {

    private val sampleNames = listOf(
        "Dr. Sarah Jenkins (CPR Certified)",
        "Marcus Vance (Community Responder)",
        "Elena Rostova (EMT Volunteer)",
        "David Chen (Neighborhood Watch)",
        "Amina Al-Mansoor (First Aider)"
    )

    fun generateNearbyVolunteers(count: Int = 3): List<VolunteerResponder> {
        return sampleNames.take(count).mapIndexed { index, name ->
            VolunteerResponder(
                id = "vol_${UUID.randomUUID().toString().take(8)}",
                name = name,
                distanceMeters = 250 + (index * 280),
                estimatedArrivalMinutes = 2 + (index * 2),
                status = VolunteerStatus.ALERTED,
                hasFirstAidCertification = index % 2 == 0,
                phoneMasked = "••• ••• ${100 + index * 37}"
            )
        }
    }
}
