package com.elytelabs.dialoghub.monetization

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.content.edit

/**
 * Built-in Usage Quota and Rewarded Ad Credit Manager for DialogHub.
 * Tracks daily edit quotas, calendar midnight resets, bonus edits rewarded from ads,
 * and In-App Purchase VIP unlimited status.
 */
class UsageQuotaManager(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "dialoghub_usage_quota"
        private const val KEY_LAST_DATE = "key_last_date"
        private const val KEY_REMAINING_EDITS = "key_remaining_edits"
        private const val KEY_IS_PRO_USER = "key_is_pro_user"

        const val DEFAULT_DAILY_FREE_EDITS = 3
        const val BONUS_EDITS_PER_AD = 5
    }

    private val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.US)

    @Synchronized
    private fun getTodayDateString(): String = dateFormat.format(Date())

    /**
     * Checks whether the user is a VIP/PRO user with unlimited usage.
     */
    fun isProUser(): Boolean {
        return prefs.getBoolean(KEY_IS_PRO_USER, false)
    }

    /**
     * Sets whether the user has purchased VIP/PRO status.
     */
    fun setProUser(isPro: Boolean) {
        prefs.edit { putBoolean(KEY_IS_PRO_USER, isPro) }
    }

    /**
     * Returns the remaining number of edits for today.
     * Automatically resets to [dailyFreeLimit] if a new day has started.
     */
    fun getRemainingEdits(dailyFreeLimit: Int = DEFAULT_DAILY_FREE_EDITS): Int {
        if (isProUser()) return Int.MAX_VALUE

        val today = getTodayDateString()
        val lastDate = prefs.getString(KEY_LAST_DATE, "")

        if (today != lastDate) {
            // New day: reset quota
            prefs.edit {
                putString(KEY_LAST_DATE, today)
                    .putInt(KEY_REMAINING_EDITS, dailyFreeLimit)
            }
            return dailyFreeLimit
        }

        return prefs.getInt(KEY_REMAINING_EDITS, dailyFreeLimit)
    }

    /**
     * Attempts to consume 1 edit quota.
     * Returns true if allowed (and decrements remaining count), or false if quota is exhausted.
     */
    fun consumeEdit(dailyFreeLimit: Int = DEFAULT_DAILY_FREE_EDITS): Boolean {
        if (isProUser()) return true

        val remaining = getRemainingEdits(dailyFreeLimit)
        return if (remaining > 0) {
            prefs.edit { putInt(KEY_REMAINING_EDITS, remaining - 1) }
            true
        } else {
            false
        }
    }

    /**
     * Adds bonus edits (default +5) after the user successfully completes a Rewarded Ad.
     */
    fun addBonusEdits(bonusCount: Int = BONUS_EDITS_PER_AD, dailyFreeLimit: Int = DEFAULT_DAILY_FREE_EDITS): Int {
        if (isProUser()) return Int.MAX_VALUE

        val current = getRemainingEdits(dailyFreeLimit)
        val updated = current + bonusCount
        prefs.edit { putInt(KEY_REMAINING_EDITS, updated) }
        return updated
    }

    /**
     * Manually sets the remaining edits.
     */
    fun setRemainingEdits(edits: Int) {
        val today = getTodayDateString()
        prefs.edit {
            putString(KEY_LAST_DATE, today)
                .putInt(KEY_REMAINING_EDITS, edits)
        }
    }
}
