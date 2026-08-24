package com.elytelabs.dialoghub.models

import android.graphics.Color
import android.widget.TextView

/**
 * Configuration for Text Stroke / Outline effect.
 */
data class TextStrokeConfig(
    val strokeWidthDp: Float = 0f,
    val strokeColor: Int = Color.BLACK,
    val isEnabled: Boolean = false
) {
    /**
     * Applies stroke/outline effect to a TextView using outline shadow diffusion.
     */
    fun applyTo(textView: TextView) {
        if (!isEnabled || strokeWidthDp <= 0f) {
            textView.setShadowLayer(0f, 0f, 0f, 0)
        } else {
            textView.setLayerType(android.view.View.LAYER_TYPE_SOFTWARE, null)
            val density = textView.resources.displayMetrics.density
            val radius = (strokeWidthDp * density).coerceAtLeast(1f)
            textView.setShadowLayer(radius, 0f, 0f, strokeColor)
        }
    }
}
