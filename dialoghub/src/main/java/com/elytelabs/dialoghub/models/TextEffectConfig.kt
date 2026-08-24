package com.elytelabs.dialoghub.models

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.widget.TextView
import androidx.core.graphics.toColorInt

/**
 * Configuration holder for advanced text effects (shadow, letter spacing, line spacing, styles).
 */
data class TextEffectConfig(
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val isUnderline: Boolean = false,
    val isAllCaps: Boolean = false,
    val letterSpacing: Float = 0f,
    val lineSpacingMultiplier: Float = 1.0f,
    val shadowRadius: Float = 0f,
    val shadowDx: Float = 2f,
    val shadowDy: Float = 2f,
    val shadowColor: Int = "#80000000".toColorInt()
) {
    /**
     * Applies this configuration directly to the target TextView.
     */
    fun applyTo(textView: TextView) {
        // 1. Text Styles (Bold / Italic)
        val style = when {
            isBold && isItalic -> Typeface.BOLD_ITALIC
            isBold -> Typeface.BOLD
            isItalic -> Typeface.ITALIC
            else -> Typeface.NORMAL
        }
        val currentTypeface = textView.typeface
        if (currentTypeface != null) {
            textView.setTypeface(currentTypeface, style)
        } else {
            textView.setTypeface(null, style)
        }

        // 2. Underline
        if (isUnderline) {
            textView.paintFlags = textView.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        } else {
            textView.paintFlags = textView.paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv()
        }

        // 3. All Caps
        textView.isAllCaps = isAllCaps

        // 4. Letter Spacing
        textView.letterSpacing = letterSpacing

        // 5. Line Spacing Multiplier
        textView.setLineSpacing(0f, lineSpacingMultiplier)

        // 6. Shadow Effect
        if (shadowRadius > 0f) {
            textView.setLayerType(android.view.View.LAYER_TYPE_SOFTWARE, null)
            val density = textView.resources.displayMetrics.density
            val radiusPx = (shadowRadius * density).coerceAtLeast(1f)
            val dxPx = shadowDx * density
            val dyPx = shadowDy * density
            textView.setShadowLayer(radiusPx, dxPx, dyPx, shadowColor)
        } else {
            textView.setShadowLayer(0f, 0f, 0f, Color.TRANSPARENT)
        }
    }
}
