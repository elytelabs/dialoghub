package com.elytelabs.dialoghub.coroutines

import android.content.Context
import com.elytelabs.dialoghub.dialogs.ColorPickerDialog
import com.elytelabs.dialoghub.dialogs.FontStyleDialog
import com.elytelabs.dialoghub.dialogs.ImageSelectorDialog
import com.elytelabs.dialoghub.dialogs.TextEffectsDialog
import com.elytelabs.dialoghub.dialogs.TextFormatDialog
import com.elytelabs.dialoghub.models.PresentationStyle
import com.elytelabs.dialoghub.models.SelectedBackground
import com.elytelabs.dialoghub.models.TextEffectConfig
import com.elytelabs.dialoghub.models.TextFormatResult
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Suspends until the user selects or applies a color from the dialog.
 * Resumes with the selected colorInt (including alpha).
 * Returns null if the dialog is dismissed without applying a color.
 */
suspend fun Context.awaitColor(
    customColors: List<Int>? = null,
    selectedColor: Int? = null,
    initialTransparency: Int = 255,
    presentationStyle: PresentationStyle = PresentationStyle.DIALOG
): Int? = suspendCancellableCoroutine { continuation ->
    var chosenColor: Int? = selectedColor

    val dialog = ColorPickerDialog.Builder(this)
        .setPresentationStyle(presentationStyle)
        .setInitialTransparency(initialTransparency)
        .apply {
            if (customColors != null) setCustomColors(customColors)
            if (selectedColor != null) setSelectedColor(selectedColor)
        }
        .setOnColorSelected { color ->
            chosenColor = color
        }
        .setOnDismiss {
            if (continuation.isActive) {
                continuation.resume(chosenColor)
            }
        }
        .build()

    dialog.showColorPickerDialog()
}

/**
 * Suspends until the user selects a font resource.
 * Returns the font resource ID, or null if dismissed without selection.
 */
suspend fun Context.awaitFont(
    fonts: List<Int>,
    previewText: String? = null,
    selectedFontResId: Int? = null,
    presentationStyle: PresentationStyle = PresentationStyle.DIALOG
): Int? = suspendCancellableCoroutine { continuation ->
    var chosenFont: Int? = selectedFontResId

    val dialog = FontStyleDialog.Builder(this)
        .setFonts(fonts)
        .setPreviewText(previewText)
        .setSelectedFont(selectedFontResId)
        .setPresentationStyle(presentationStyle)
        .setOnFontSelected { fontResId ->
            chosenFont = fontResId
        }
        .setOnDismiss {
            if (continuation.isActive) {
                continuation.resume(chosenFont)
            }
        }
        .build()

    dialog.showFontSelectionDialog()
}

/**
 * Suspends until the user selects a background, color, or clicks the gallery tile.
 * Returns [SelectedBackground], or null if dismissed without selection.
 */
suspend fun Context.awaitBackground(
    backgrounds: List<Int> = emptyList(),
    selectedBackgroundResId: Int? = null,
    enableGalleryPick: Boolean = false,
    presentationStyle: PresentationStyle = PresentationStyle.DIALOG
): SelectedBackground? = suspendCancellableCoroutine { continuation ->
    var result: SelectedBackground? = null

    val dialog = ImageSelectorDialog.Builder(this)
        .setBackgrounds(backgrounds)
        .setSelectedBackground(selectedBackgroundResId)
        .setEnableGalleryPick(enableGalleryPick) {
            result = SelectedBackground.GalleryRequested
        }
        .setPresentationStyle(presentationStyle)
        .setOnImageSelected { resId ->
            result = SelectedBackground.Image(resId)
        }
        .setOnColorSelected { colorInt ->
            result = SelectedBackground.Color(colorInt)
        }
        .setOnDismiss {
            if (continuation.isActive) {
                continuation.resume(result)
            }
        }
        .build()

    dialog.showImageSelectionDialog()
}

/**
 * Suspends until the user completes text formatting.
 * Returns [TextFormatResult] with the final size and alignment upon dialog dismissal.
 */
suspend fun Context.awaitTextFormat(
    initialSizeSp: Float = TextFormatDialog.DEFAULT_SIZE_SP,
    initialAlignment: TextFormatDialog.TextAlignment = TextFormatDialog.TextAlignment.CENTER,
    previewText: String? = null,
    presentationStyle: PresentationStyle = PresentationStyle.DIALOG
): TextFormatResult = suspendCancellableCoroutine { continuation ->
    var currentSize = initialSizeSp
    var currentAlign = initialAlignment

    val dialog = TextFormatDialog.Builder(this)
        .setTextSize(initialSizeSp)
        .setAlignment(initialAlignment)
        .setPreviewText(previewText)
        .setPresentationStyle(presentationStyle)
        .setOnFormatChanged { size, align ->
            currentSize = size
            currentAlign = align
        }
        .setOnDismiss {
            if (continuation.isActive) {
                continuation.resume(TextFormatResult(currentSize, currentAlign))
            }
        }
        .build()

    dialog.showTextFormatDialog()
}

/**
 * Suspends until the user completes text effects adjustments.
 * Returns [TextEffectConfig] upon dialog dismissal.
 */
suspend fun Context.awaitTextEffects(
    initialConfig: TextEffectConfig = TextEffectConfig(),
    previewText: String? = null,
    presentationStyle: PresentationStyle = PresentationStyle.DIALOG
): TextEffectConfig = suspendCancellableCoroutine { continuation ->
    var config = initialConfig

    val dialog = TextEffectsDialog.Builder(this)
        .setConfig(initialConfig)
        .setPreviewText(previewText)
        .setPresentationStyle(presentationStyle)
        .setOnEffectsChanged { updatedConfig ->
            config = updatedConfig
        }
        .setOnDismiss {
            if (continuation.isActive) {
                continuation.resume(config)
            }
        }
        .build()

    dialog.showTextEffectsDialog()
}
