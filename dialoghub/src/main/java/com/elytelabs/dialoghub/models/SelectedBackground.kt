package com.elytelabs.dialoghub.models

/**
 * Represents the result of a selection from [com.elytelabs.dialoghub.dialogs.ImageSelectorDialog].
 */
sealed class SelectedBackground {
    /**
     * User selected a background drawable image resource.
     */
    data class Image(val drawableResId: Int) : SelectedBackground()

    /**
     * User selected a solid/transparent color integer.
     */
    data class Color(val colorInt: Int) : SelectedBackground()

    /**
     * User clicked the "Pick from Gallery" action tile.
     */
    data object GalleryRequested : SelectedBackground()
}
