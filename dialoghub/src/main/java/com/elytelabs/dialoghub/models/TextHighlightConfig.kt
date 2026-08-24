package com.elytelabs.dialoghub.models

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View

/**
 * Configuration for Text Background Ribbon / Highlight Box.
 */
data class TextHighlightConfig(
    val backgroundColor: Int = Color.TRANSPARENT,
    val cornerRadiusDp: Float = 12f,
    val horizontalPaddingDp: Float = 16f,
    val verticalPaddingDp: Float = 8f,
    val isEnabled: Boolean = false
) {
    fun applyTo(view: View) {
        if (!isEnabled || Color.alpha(backgroundColor) == 0) {
            view.background = null
        } else {
            val density = view.resources.displayMetrics.density
            val radiusPx = cornerRadiusDp * density
            val hPadPx = (horizontalPaddingDp * density).toInt()
            val vPadPx = (verticalPaddingDp * density).toInt()

            val drawable = GradientDrawable().apply {
                setColor(backgroundColor)
                cornerRadius = radiusPx
            }
            view.background = drawable
            view.setPadding(hPadPx, vPadPx, hPadPx, vPadPx)
        }
    }
}
