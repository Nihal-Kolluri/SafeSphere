package com.safesphere.data.model

/**
 * In-Case-of-Emergency (ICE) medical details displayed to responders.
 */
data class MedicalProfile(
    val fullName: String = "John Doe",
    val bloodType: String = "O+",
    val allergies: String = "Penicillin, Peanuts",
    val medicalConditions: String = "Asthma",
    val currentMedications: String = "Albuterol Inhaler",
    val organDonor: Boolean = true,
    val emergencyNotes: String = "Carry inhaler in front backpack pocket"
)
