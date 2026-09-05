package com.safesphere

import com.safesphere.core.security.PinValidationResult
import org.junit.Assert.assertEquals
import org.junit.Test
import java.security.MessageDigest

class SecurityPinManagerTest {

    private val salt = "SafeSphereSecureSalt_2026"

    private fun hash(pin: String): String {
        val bytes = (pin + salt).toByteArray(Charsets.UTF_8)
        val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }

    private fun validate(enteredPin: String, safeHash: String, duressHash: String): PinValidationResult {
        val inputHash = hash(enteredPin)
        return when {
            inputHash == safeHash -> PinValidationResult.ValidSafePin
            inputHash == duressHash -> PinValidationResult.ValidDuressPin
            else -> PinValidationResult.InvalidPin
        }
    }

    @Test
    fun testSafePinSuccess() {
        val safeHash = hash("1234")
        val duressHash = hash("9999")

        val result = validate("1234", safeHash, duressHash)
        assertEquals(PinValidationResult.ValidSafePin, result)
    }

    @Test
    fun testDuressPinCoercionDetection() {
        val safeHash = hash("1234")
        val duressHash = hash("9999")

        val result = validate("9999", safeHash, duressHash)
        assertEquals(PinValidationResult.ValidDuressPin, result)
    }

    @Test
    fun testInvalidPinRejection() {
        val safeHash = hash("1234")
        val duressHash = hash("9999")

        val result = validate("0000", safeHash, duressHash)
        assertEquals(PinValidationResult.InvalidPin, result)
    }
}
