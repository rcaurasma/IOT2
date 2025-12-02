package com.ev.iot2.utils

import android.util.Patterns
import java.text.Normalizer

object Validators {
    
    fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    fun isValidName(name: String): Boolean {
        // Only letters (including accented) and spaces
        val namePattern = Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\\s]+$")
        return name.isNotBlank() && namePattern.matches(name)
    }
    
    fun isStrongPassword(password: String): PasswordValidationResult {
        val errors = mutableListOf<String>()
        
        if (password.length < 8) {
            errors.add("La contraseña debe tener al menos 8 caracteres")
        }
        if (!password.any { it.isUpperCase() }) {
            errors.add("La contraseña debe contener al menos una mayúscula")
        }
        if (!password.any { it.isLowerCase() }) {
            errors.add("La contraseña debe contener al menos una minúscula")
        }
        if (!password.any { it.isDigit() }) {
            errors.add("La contraseña debe contener al menos un número")
        }
        if (!password.any { !it.isLetterOrDigit() }) {
            errors.add("La contraseña debe contener al menos un carácter especial")
        }
        
        return PasswordValidationResult(errors.isEmpty(), errors)
    }
    
    fun passwordsMatch(password: String, confirmPassword: String): Boolean {
        return password == confirmPassword
    }
    
    fun isValidRecoveryCode(code: String): Boolean {
        return code.length == 5 && code.all { it.isDigit() }
    }
    
    fun generateRecoveryCode(): String {
        return (10000..99999).random().toString()
    }
    
    // Normalize string for search (remove accents and convert to lowercase)
    fun normalizeForSearch(text: String): String {
        val normalized = Normalizer.normalize(text, Normalizer.Form.NFD)
        return normalized.replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "").lowercase()
    }
}

data class PasswordValidationResult(
    val isValid: Boolean,
    val errors: List<String>
)

object Constants {
    const val RECOVERY_CODE_VALIDITY_MS = 60000L // 1 minute
    const val SENSOR_UPDATE_INTERVAL_MS = 2000L // 2 seconds
    const val SPLASH_DELAY_MS = 3000L // 3 seconds
    const val DATE_TIME_FORMAT = "dd/MM/yyyy HH:mm:ss"
}
