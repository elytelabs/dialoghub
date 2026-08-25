package com.elytelabs.dialoghub.monetization

import android.content.Context
import com.elytelabs.dialoghub.models.StudioTab

/**
 * Interface defining query methods to check whether fonts, backgrounds, colors,
 * features, or tools are locked behind IAP or Rewarded Ads.
 */
interface ItemLockProvider {
    fun isFontLocked(fontResId: Int): Boolean = false
    fun isBackgroundLocked(resId: Int): Boolean = false
    fun isColorLocked(colorInt: Int): Boolean = false
    fun isTabLocked(tab: StudioTab): Boolean = false
    fun isGalleryLocked(): Boolean = false
}

/**
 * Highly flexible, fluent implementation of [ItemLockProvider] that supports:
 * - 12-Hour / 24-Hour / Custom Timed VIP Passes rewarded via ads (`grantTimedPass(...)`)
 * - Single item locking (`lockFont(id)`, `lockBackground(id)`, etc.)
 * - Batch item collection locking (`lockFonts(ids)`, `lockBackgrounds(ids)`, etc.)
 * - Dynamic condition lambdas (`lockFontWhen { ... }`, etc.)
 * - Real-time in-session unlocking (`unlockFont(id)`, `unlockBackground(id)`, etc.)
 */
class DefaultItemLockProvider(
    private var timedPassManager: TimedPassManager? = null
) : ItemLockProvider {

    private val lockedFonts = mutableSetOf<Int>()
    private val lockedBackgrounds = mutableSetOf<Int>()
    private val lockedColors = mutableSetOf<Int>()
    private val lockedTabs = mutableSetOf<StudioTab>()
    private var lockGallery = false

    /**
     * Attaches a [TimedPassManager] to enable 12-hour / timed VIP passes.
     */
    fun attachTimedPass(context: Context) = apply {
        this.timedPassManager = TimedPassManager(context)
    }

    fun setTimedPassManager(manager: TimedPassManager?) = apply {
        this.timedPassManager = manager
    }

    /**
     * Checks if a timed pass is currently active.
     */
    fun isPassActive(): Boolean = timedPassManager?.isPassActive() == true

    /**
     * Grants a timed VIP pass for a specified number of hours (default 12 hours).
     */
    fun grantTimedPass(hours: Int = TimedPassManager.DEFAULT_PASS_HOURS): Long {
        return timedPassManager?.grantTimedPassHours(hours) ?: 0L
    }

    /**
     * Grants a timed VIP pass for custom milliseconds.
     */
    fun grantTimedPassMillis(durationMillis: Long): Long {
        return timedPassManager?.grantTimedPass(durationMillis) ?: 0L
    }

    /**
     * Returns formatted remaining pass duration string (e.g. "11h 45m").
     */
    fun getRemainingPassFormatted(): String = timedPassManager?.getRemainingFormatted() ?: "Expired"

    /**
     * Revokes active timed pass.
     */
    fun revokePass() = apply {
        timedPassManager?.revokePass()
    }

    // Font Lock Methods
    fun lockFont(fontResId: Int) = apply { lockedFonts.add(fontResId) }
    fun lockFonts(vararg fontResIds: Int) = apply { lockedFonts.addAll(fontResIds.toList()) }
    fun lockFonts(fontResIds: Collection<Int>) = apply { lockedFonts.addAll(fontResIds) }
    fun unlockFont(fontResId: Int) = apply { lockedFonts.remove(fontResId) }

    // Background Lock Methods
    fun lockBackground(resId: Int) = apply { lockedBackgrounds.add(resId) }
    fun lockBackgrounds(vararg resIds: Int) = apply { lockedBackgrounds.addAll(resIds.toList()) }
    fun lockBackgrounds(resIds: Collection<Int>) = apply { lockedBackgrounds.addAll(resIds) }
    fun unlockBackground(resId: Int) = apply { lockedBackgrounds.remove(resId) }

    // Color Lock Methods
    fun lockColor(colorInt: Int) = apply { lockedColors.add(colorInt) }
    fun lockColors(vararg colorInts: Int) = apply { lockedColors.addAll(colorInts.toList()) }
    fun lockColors(colorInts: Collection<Int>) = apply { lockedColors.addAll(colorInts) }
    fun unlockColor(colorInt: Int) = apply { lockedColors.remove(colorInt) }

    // Studio Tab Lock Methods
    fun lockTab(tab: StudioTab) = apply { lockedTabs.add(tab) }
    fun lockTabs(vararg tabs: StudioTab) = apply { lockedTabs.addAll(tabs.toList()) }
    fun lockTabs(tabs: Collection<StudioTab>) = apply { lockedTabs.addAll(tabs) }
    fun unlockTab(tab: StudioTab) = apply { lockedTabs.remove(tab) }

    // Action Tiles
    fun lockGallery(lock: Boolean = true) = apply { this.lockGallery = lock }

    // Reset / Unlock All
    fun unlockAll() = apply {
        lockedFonts.clear()
        lockedBackgrounds.clear()
        lockedColors.clear()
        lockedTabs.clear()
        lockGallery = false
    }

    override fun isFontLocked(fontResId: Int): Boolean {
        if (isPassActive()) return false
        return lockedFonts.contains(fontResId)
    }

    override fun isBackgroundLocked(resId: Int): Boolean {
        if (isPassActive()) return false
        return lockedBackgrounds.contains(resId)
    }

    override fun isColorLocked(colorInt: Int): Boolean {
        if (isPassActive()) return false
        return lockedColors.contains(colorInt)
    }

    override fun isTabLocked(tab: StudioTab): Boolean {
        if (isPassActive()) return false
        return lockedTabs.contains(tab)
    }

    override fun isGalleryLocked(): Boolean {
        if (isPassActive()) return false
        return lockGallery
    }
}
