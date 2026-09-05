package com.safesphere.core.security

/**
 * Result of validating an emergency deactivation PIN.
 */
sealed class PinValidationResult {
    data object ValidSafePin : PinValidationResult()
    data object ValidDuressPin : PinValidationResult()
    data object InvalidPin : PinValidationResult()
}
