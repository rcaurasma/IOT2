package com.ev.iot2.utils

import java.text.Normalizer

object Validators {
    private val EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()

    data class ValidationResult(val isValid: Boolean, val errors: List<String>)

    fun isValidEmail(email: String): Boolean {
        return EMAIL_REGEX.matches(email)
    }

    fun normalizeForSearch(input: String): String {
        val tmp = Normalizer.normalize(input.lowercase(), Normalizer.Form.NFD)
        return tmp.replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "").trim()
    }

    fun isStrongPassword(pw: String): ValidationResult {
        val errors = mutableListOf<String>()
        if (pw.length < 6) errors.add("La contraseña debe tener al menos 6 caracteres")
        if (!pw.any { it.isLetter() }) errors.add("La contraseña debe contener al menos una letra")
        if (!pw.any { it.isDigit() }) errors.add("La contraseña debe contener al menos un número")
        return ValidationResult(errors.isEmpty(), errors)
    }

    fun passwordsMatch(a: String, b: String): Boolean = a == b

    fun isValidName(name: String): Boolean = name.trim().length >= 2

    fun isValidRecoveryCode(code: String): Boolean = code.trim().length >= 4
}
