package com.vsoft.app.security

import android.content.Context
import android.content.SharedPreferences
import com.vsoft.app.util.toLatinDigits
import java.security.MessageDigest
import java.security.SecureRandom

/** Local PIN state. PINs are never stored or logged in plaintext. */
object PinStore {
    private const val PREFS = "vsoft_prefs"
    private const val KEY_HASH = "pin_hash"
    private const val KEY_SALT = "pin_salt"
    private const val KEY_FAIL_COUNT = "pin_fail_count"
    private const val KEY_LOCKED_UNTIL = "pin_locked_until"
    private const val KEY_SECURE_SCREEN = "secure_screen"
    private const val LOCKOUT_MS = 30_000L
    private const val MAX_FAILURES = 5

    private fun prefs(context: Context): SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun isEnabled(context: Context): Boolean = try {
        !prefs(context).getString(KEY_HASH, null).isNullOrEmpty()
    } catch (_: Exception) {
        false
    }

    fun isValidPin(pin: String): Boolean = try {
        val normalized = pin.toLatinDigits()
        normalized.length == 4 && normalized.all { it in '0'..'9' }
    } catch (_: Exception) {
        false
    }

    fun setPin(context: Context, pin: String): Boolean {
        return try {
            val normalized = pin.toLatinDigits()
            if (!isValidPin(normalized)) {
                false
            } else {
                val saltBytes = ByteArray(16)
                SecureRandom().nextBytes(saltBytes)
                val salt = saltBytes.toHex()
                val hash = digest("$salt$normalized").toHex()
                prefs(context).edit()
                    .putString(KEY_SALT, salt)
                    .putString(KEY_HASH, hash)
                    .putInt(KEY_FAIL_COUNT, 0)
                    .putLong(KEY_LOCKED_UNTIL, 0L)
                    .commit()
            }
        } catch (_: Exception) {
            false
        }
    }

    fun verify(context: Context, pin: String): Boolean {
        return try {
            val normalized = pin.toLatinDigits()
            if (!isValidPin(normalized)) {
                false
            } else {
                val p = prefs(context)
                val salt = p.getString(KEY_SALT, null)
                val storedHash = p.getString(KEY_HASH, null)
                val expected = storedHash?.hexToBytes()
                if (salt == null || expected == null) {
                    false
                } else {
                    val valid = MessageDigest.isEqual(expected, digest("$salt$normalized"))
                    if (valid) {
                        p.edit().putInt(KEY_FAIL_COUNT, 0).putLong(KEY_LOCKED_UNTIL, 0L).apply()
                    }
                    valid
                }
            }
        } catch (_: Exception) {
            false
        }
    }

    fun clear(context: Context): Boolean = try {
        prefs(context).edit()
            .remove(KEY_HASH)
            .remove(KEY_SALT)
            .remove(KEY_FAIL_COUNT)
            .remove(KEY_LOCKED_UNTIL)
            .commit()
    } catch (_: Exception) {
        false
    }

    fun registerFailure(context: Context) {
        try {
            val p = prefs(context)
            if (remainingLockMillis(context) > 0L) return
            val next = p.getInt(KEY_FAIL_COUNT, 0) + 1
            if (next >= MAX_FAILURES) {
                p.edit()
                    .putInt(KEY_FAIL_COUNT, 0)
                    .putLong(KEY_LOCKED_UNTIL, System.currentTimeMillis() + LOCKOUT_MS)
                    .apply()
            } else {
                p.edit().putInt(KEY_FAIL_COUNT, next).apply()
            }
        } catch (_: Exception) {
            // Fail closed at the caller; never expose sensitive data in logs.
        }
    }

    fun remainingLockMillis(context: Context): Long = try {
        (prefs(context).getLong(KEY_LOCKED_UNTIL, 0L) - System.currentTimeMillis()).coerceAtLeast(0L)
    } catch (_: Exception) {
        0L
    }

    fun isSecureScreen(context: Context): Boolean = try {
        prefs(context).getBoolean(KEY_SECURE_SCREEN, false)
    } catch (_: Exception) {
        false
    }

    fun setSecureScreen(context: Context, enabled: Boolean): Boolean = try {
        prefs(context).edit().putBoolean(KEY_SECURE_SCREEN, enabled).commit()
    } catch (_: Exception) {
        false
    }

    private fun digest(value: String): ByteArray =
        MessageDigest.getInstance("SHA-256").digest(value.toByteArray(Charsets.UTF_8))

    private fun ByteArray.toHex(): String = joinToString("") { byte ->
        (byte.toInt() and 0xff).toString(16).padStart(2, '0')
    }

    private fun String.hexToBytes(): ByteArray? {
        if (length % 2 != 0 || any { it !in '0'..'9' && it !in 'a'..'f' && it !in 'A'..'F' }) return null
        return try {
            ByteArray(length / 2) { index ->
                substring(index * 2, index * 2 + 2).toInt(16).toByte()
            }
        } catch (_: Exception) {
            null
        }
    }
}
