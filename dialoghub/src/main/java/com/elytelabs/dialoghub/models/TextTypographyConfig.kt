package com.elytelabs.dialoghub.models

import android.graphics.Color
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.toColorInt
import com.elytelabs.dialoghub.dialogs.TextFormatDialog

/**
 * Encapsulates full typography, visual effects, and styling configuration for quotes and text editors.
 */
data class TextTypographyConfig(
    val fontResId: Int? = null,
    val textColor: Int = "#FFFFFF".toColorInt(),
    val textSizeSp: Float = 20f,
    val alignment: TextFormatDialog.TextAlignment = TextFormatDialog.TextAlignment.CENTER,
    val effectConfig: TextEffectConfig = TextEffectConfig(),
    val strokeConfig: TextStrokeConfig = TextStrokeConfig(),
    val highlightConfig: TextHighlightConfig = TextHighlightConfig()
) {
    /**
     * Applies the complete typography configuration to the target TextView.
     */
    fun applyTo(textView: TextView) {
        textView.textSize = textSizeSp
        textView.gravity = alignment.gravity
        textView.setTextColor(textColor)

        if (fontResId != null) {
            val tf = ResourcesCompat.getFont(textView.context, fontResId)
            textView.typeface = tf
        }

        effectConfig.applyTo(textView)

        if (strokeConfig.isEnabled && strokeConfig.strokeWidthDp > 0f) {
            strokeConfig.applyTo(textView)
        }

        if (highlightConfig.isEnabled && Color.alpha(highlightConfig.backgroundColor) > 0) {
            highlightConfig.applyTo(textView)
        } else {
            textView.background = null
        }
    }
}
