package com.safesphere.data.model

/**
 * Represents the tiered escalation steps in SafeSphere.
 */
enum class EscalationTier(val stepNumber: Int, val title: String, val defaultDurationSeconds: Int) {
    TIER_1_FAMILY(
        stepNumber = 1,
        title = "Family & Trusted Contacts",
        defaultDurationSeconds = 30
    ),
    TIER_2_VOLUNTEERS(
        stepNumber = 2,
        title = "Nearby Community Volunteers",
        defaultDurationSeconds = 60
    ),
    TIER_3_AUTHORITIES(
        stepNumber = 3,
        title = "Emergency Services (Police / Fire / Medical)",
        defaultDurationSeconds = 0 // Final tier - stays active until resolved
    )
}
