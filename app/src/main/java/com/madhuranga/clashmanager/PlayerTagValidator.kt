package com.madhuranga.clashmanager

/** Normalizes and validates Clash of Clans player tags without accepting arbitrary text. */
object PlayerTagValidator {
    private const val ALPHABET = "0289PYLQGRJCUV"

    fun normalize(input: String): String {
        val hasPrefix = input.trim().startsWith("#")
        val body = input.uppercase().filter { it in ALPHABET }.take(15)
        return (if (hasPrefix) "#" else "") + body
    }

    fun isValid(input: String): Boolean {
        val body = input.removePrefix("#")
        return body.length in 3..15 && body.all { it in ALPHABET }
    }
}
