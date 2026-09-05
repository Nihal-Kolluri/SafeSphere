package com.safesphere.core.security

import android.content.Context
import android.content.SharedPreferences
import java.security.MessageDigest

/**
 * Manages secure storage and verification of the user's Safe PIN (true cancellation)
 * and Duress PIN (simulates cancellation while covertly continuing live tracking).
 */
class SecurityPinManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("safesphere_security_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_SAFE_PIN_HASH = "key_safe_pin_hash"
        private const val KEY_DURESS_PIN_HASH = "key_duress_pin_hash"
        private const val DEFAULT_SAFE_PIN = "1234"
        private const val DEFAULT_DURESS_PIN = "9999"
        private const val SALT = "SafeSphereSecureSalt_2026"
    }

    init {
        // Initialize default PINs if not yet configured
        if (!prefs.contains(KEY_SAFE_PIN_HASH)) {
            setSafePin(DEFAULT_SAFE_PIN)
        }
        if (!prefs.contains(KEY_DURESS_PIN_HASH)) {
            setDuressPin(DEFAULT_DURESS_PIN)
        }
    }

    fun setSafePin(pin: String) {
        prefs.edit().putString(KEY_SAFE_PIN_HASH, hashPin(pin)).apply()
    }

    fun setDuressPin(pin: String) {
        prefs.edit().putString(KEY_DURESS_PIN_HASH, hashPin(pin)).apply()
    }

    /**
     * Validates input PIN against stored Safe and Duress hashes.
     */
    fun validatePin(enteredPin: String): PinValidationResult {
        val inputHash = hashPin(enteredPin)
        val safeHash = prefs.getString(KEY_SAFE_PIN_HASH, "")
        val duressHash = prefs.getString(KEY_DURESS_PIN_HASH, "")

        return when {
            inputHash == safeHash -> PinValidationResult.ValidSafePin
            inputHash == duressHash -> PinValidationResult.ValidDuressPin
            else -> PinValidationResult.InvalidPin
        }
    }

    private fun hashPin(pin: String): String {
        val bytes = (pin + SALT).toByteArray(Charsets.UTF_8)
        val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }
}
