package com.elytelabs.dialoghub.monetization

import android.content.Context
import android.content.SharedPreferences
import java.util.concurrent.TimeUnit

/**
 * Manages timed full-access VIP passes (e.g. 12-hour or 24-hour passes) rewarded via ads or promotions.
 * Persists the expiry timestamp in [SharedPreferences] so passes survive app restarts and dialog closures.
 */
class TimedPassManager(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "dialoghub_timed_pass_prefs"
        private const val KEY_PASS_EXPIRY_MS = "key_pass_expiry_ms"
        const val DEFAULT_PASS_HOURS = 12
        const val DEFAULT_PASS_MS = 12 * 60 * 60 * 1000L // 12 hours in millis
    }

    /**
     * Checks if a timed VIP pass is currently active.
     */
    fun isPassActive(): Boolean {
        val expiry = prefs.getLong(KEY_PASS_EXPIRY_MS, 0L)
        return System.currentTimeMillis() < expiry
    }

    /**
     * Grants a timed VIP pass for a custom duration in milliseconds (defaults to 12 hours).
     * If a pass is already active, extends the pass by the new duration.
     */
    fun grantTimedPass(durationMillis: Long = DEFAULT_PASS_MS): Long {
        val now = System.currentTimeMillis()
        val currentExpiry = prefs.getLong(KEY_PASS_EXPIRY_MS, 0L)
        val newExpiry = if (now < currentExpiry) {
            currentExpiry + durationMillis
        } else {
            now + durationMillis
        }
        prefs.edit().putLong(KEY_PASS_EXPIRY_MS, newExpiry).apply()
        return newExpiry
    }

    /**
     * Grants a timed VIP pass for a specified number of hours (default: 12 hours).
     */
    fun grantTimedPassHours(hours: Int = DEFAULT_PASS_HOURS): Long {
        return grantTimedPass(TimeUnit.HOURS.toMillis(hours.toLong()))
    }

    /**
     * Grants a timed VIP pass for a specified number of minutes.
     */
    fun grantTimedPassMinutes(minutes: Int): Long {
        return grantTimedPass(TimeUnit.MINUTES.toMillis(minutes.toLong()))
    }

    /**
     * Returns remaining active milliseconds, or 0 if expired.
     */
    fun getRemainingMillis(): Long {
        val expiry = prefs.getLong(KEY_PASS_EXPIRY_MS, 0L)
        val diff = expiry - System.currentTimeMillis()
        return if (diff > 0) diff else 0L
    }

    /**
     * Returns a human-friendly formatted remaining time string (e.g. "11h 45m", "45m 12s", or "Expired").
     */
    fun getRemainingFormatted(): String {
        val remaining = getRemainingMillis()
        if (remaining <= 0) return "Expired"

        val hours = TimeUnit.MILLISECONDS.toHours(remaining)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(remaining) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(remaining) % 60

        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes}m ${seconds}s"
            else -> "${seconds}s"
        }
    }

    /**
     * Revokes any active timed pass immediately.
     */
    fun revokePass() {
        prefs.edit().remove(KEY_PASS_EXPIRY_MS).apply()
    }
}
