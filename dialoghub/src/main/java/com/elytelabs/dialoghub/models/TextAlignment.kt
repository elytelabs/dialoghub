package com.elytelabs.dialoghub.models

import android.view.Gravity

/**
 * Text alignment options (LEFT, CENTER, RIGHT).
 */
enum class TextAlignment(val gravity: Int) {
    LEFT(Gravity.START or Gravity.CENTER_VERTICAL),
    CENTER(Gravity.CENTER),
    RIGHT(Gravity.END or Gravity.CENTER_VERTICAL)
}
