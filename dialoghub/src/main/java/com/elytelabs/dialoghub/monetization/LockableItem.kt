package com.elytelabs.dialoghub.monetization

import com.elytelabs.dialoghub.models.StudioTab

/**
 * Sealed representation of lockable entities in DialogHub dialogs.
 */
sealed class LockableItem {
    /**
     * Font resource locked behind VIP/IAP or Rewarded Ads.
     */
    data class Font(val fontResId: Int) : LockableItem()

    /**
     * Background drawable resource locked behind VIP/IAP or Rewarded Ads.
     */
    data class Background(val resId: Int) : LockableItem()

    /**
     * Color integer locked behind VIP/IAP or Rewarded Ads.
     */
    data class Color(val colorInt: Int) : LockableItem()

    /**
     * An entire Text Studio tab/tool locked behind VIP/IAP or Rewarded Ads.
     */
    data class StudioFeatureTab(val tab: StudioTab) : LockableItem()

    /**
     * Gallery picker option tile locked behind VIP/IAP or Rewarded Ads.
     */
    data object GalleryPicker : LockableItem()

    /**
     * Custom Hex Code input dialog locked behind VIP/IAP or Rewarded Ads.
     */
    data object CustomHexInput : LockableItem()
}
