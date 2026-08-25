package com.elytelabs.dialoghub.models

import com.elytelabs.dialoghub.R

/**
 * Enumeration of available customization tabs inside [com.elytelabs.dialoghub.dialogs.TextStudioDialog].
 * Allows host applications to selectively enable or hide tabs to match their UI requirements.
 */
enum class StudioTab(val title: String, val iconResId: Int) {
    FONT("Font", R.drawable.ic_tab_font),
    COLOR("Color", R.drawable.ic_tile_palette),
    FORMAT("Format", R.drawable.ic_align_center),
    EFFECTS("Effects", R.drawable.ic_tab_effects);

    companion object {
        /**
         * Default streamlined 4-tab set: Font, Color, Format, Effects.
         */
        val DEFAULT: Set<StudioTab> = entries.toSet()

        /**
         * Full set of studio tabs.
         */
        val ALL: Set<StudioTab> = entries.toSet()
    }
}
