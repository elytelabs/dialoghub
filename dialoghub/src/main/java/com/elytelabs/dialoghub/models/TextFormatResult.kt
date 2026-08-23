package com.elytelabs.dialoghub.models

import com.elytelabs.dialoghub.dialogs.TextFormatDialog

/**
 * Holds the resulting text size and alignment from [com.elytelabs.dialoghub.dialogs.TextFormatDialog].
 */
data class TextFormatResult(
    val textSizeSp: Float,
    val alignment: TextFormatDialog.TextAlignment
)
