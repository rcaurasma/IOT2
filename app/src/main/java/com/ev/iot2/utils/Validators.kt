package com.ev.iot2.utils

import java.text.Normalizer

object Validators {
    private val EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()

    fun isValidEmail(email: String): Boolean {
        return EMAIL_REGEX.matches(email)
    }

    fun normalizeForSearch(input: String): String {
        val tmp = Normalizer.normalize(input.lowercase(), Normalizer.Form.NFD)
        return tmp.replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "").trim()
    }
}
