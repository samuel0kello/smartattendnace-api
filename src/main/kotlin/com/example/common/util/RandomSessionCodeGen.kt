package com.example.common.util

import java.security.SecureRandom

private val secureRandom = SecureRandom()
private const val DEFAULT_CODE_LENGTH = 6
private const val CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"

/**
 * Generates a session code of configurable length using a secure random generator.
 * The generated code will be based on an alphanumeric character set.
 *
 * @param length The length of the session code to generate (default is 6).
 * @param characterSet The set of characters to use for code generation (default is alphanumeric).
 * @return A randomly generated session code.
 * @throws IllegalArgumentException If length is less than 1 or characterSet is empty.
 */
fun generateSessionCode(
    length: Int = DEFAULT_CODE_LENGTH,
    characterSet: String = CHARACTERS
): String {
    require(length > 0) { "Session code length must be greater than 0." }
    require(characterSet.isNotEmpty()) { "Character set must not be empty." }

    return (1..length)
        .map { characterSet[secureRandom.nextInt(characterSet.length)] }
        .joinToString("")
}