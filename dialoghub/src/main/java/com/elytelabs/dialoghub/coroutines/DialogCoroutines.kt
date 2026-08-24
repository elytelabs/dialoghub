package com.elytelabs.dialoghub.coroutines

import android.content.Context
import com.elytelabs.dialoghub.dialogs.ColorPickerDialog
import com.elytelabs.dialoghub.dialogs.FontStyleDialog
import com.elytelabs.dialoghub.dialogs.ImageSelectorDialog
import com.elytelabs.dialoghub.dialogs.TextEffectsDialog
import com.elytelabs.dialoghub.dialogs.TextFormatDialog
import com.elytelabs.dialoghub.dialogs.TextHighlightDialog
import com.elytelabs.dialoghub.dialogs.TextStrokeDialog
import com.elytelabs.dialoghub.dialogs.TextStudioDialog
import com.elytelabs.dialoghub.models.PresentationStyle
import com.elytelabs.dialoghub.models.SelectedBackground
import com.elytelabs.dialoghub.models.StudioTab
import com.elytelabs.dialoghub.models.TextEffectConfig
import com.elytelabs.dialoghub.models.TextFormatResult
import com.elytelabs.dialoghub.models.TextHighlightConfig
import com.elytelabs.dialoghub.models.TextStrokeConfig
import com.elytelabs.dialoghub.models.TextTypographyConfig
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
    presentationStyle: PresentationStyle = PresentationStyle.BOTTOM_SHEET
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
    presentationStyle: PresentationStyle = PresentationStyle.BOTTOM_SHEET
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
    presentationStyle: PresentationStyle = PresentationStyle.BOTTOM_SHEET
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
    presentationStyle: PresentationStyle = PresentationStyle.BOTTOM_SHEET
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
    presentationStyle: PresentationStyle = PresentationStyle.BOTTOM_SHEET
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

/**
 * Suspends until the user completes text stroke adjustments.
 * Returns [TextStrokeConfig] upon dialog dismissal.
 */
suspend fun Context.awaitTextStroke(
    initialConfig: TextStrokeConfig = TextStrokeConfig(),
    previewText: String? = null,
    presentationStyle: PresentationStyle = PresentationStyle.BOTTOM_SHEET
): TextStrokeConfig = suspendCancellableCoroutine { continuation ->
    var config = initialConfig

    val dialog = TextStrokeDialog.Builder(this)
        .setConfig(initialConfig)
        .setPreviewText(previewText)
        .setPresentationStyle(presentationStyle)
        .setOnStrokeChanged { updatedConfig ->
            config = updatedConfig
        }
        .setOnDismiss {
            if (continuation.isActive) {
                continuation.resume(config)
            }
        }
        .build()

    dialog.showTextStrokeDialog()
}

/**
 * Suspends until the user completes text background highlight adjustments.
 * Returns [TextHighlightConfig] upon dialog dismissal.
 */
suspend fun Context.awaitTextHighlight(
    initialConfig: TextHighlightConfig = TextHighlightConfig(),
    previewText: String? = null,
    presentationStyle: PresentationStyle = PresentationStyle.BOTTOM_SHEET
): TextHighlightConfig = suspendCancellableCoroutine { continuation ->
    var config = initialConfig

    val dialog = TextHighlightDialog.Builder(this)
        .setConfig(initialConfig)
        .setPreviewText(previewText)
        .setPresentationStyle(presentationStyle)
        .setOnHighlightChanged { updatedConfig ->
            config = updatedConfig
        }
        .setOnDismiss {
            if (continuation.isActive) {
                continuation.resume(config)
            }
        }
        .build()

    dialog.showTextHighlightDialog()
}

/**
 * Suspends until the user finishes customizing typography in the All-In-One Text Studio.
 * Returns the configured [TextTypographyConfig] upon dialog dismissal.
 */
suspend fun Context.awaitTextStudio(
    initialConfig: TextTypographyConfig = TextTypographyConfig(),
    previewText: String? = null,
    fonts: List<Int>? = null,
    enabledTabs: Set<StudioTab> = StudioTab.DEFAULT,
    showPreviewPane: Boolean = false,
    presentationStyle: PresentationStyle = PresentationStyle.BOTTOM_SHEET
): TextTypographyConfig = suspendCancellableCoroutine { continuation ->
    var resultConfig = initialConfig

    val dialog = TextStudioDialog.Builder(this)
        .setConfig(initialConfig)
        .setPreviewText(previewText)
        .setTabs(enabledTabs)
        .setShowPreviewPane(showPreviewPane)
        .apply {
            if (fonts != null) setFonts(fonts)
        }
        .setPresentationStyle(presentationStyle)
        .setOnTypographyApplied { applied ->
            resultConfig = applied
        }
        .setOnDismiss {
            if (continuation.isActive) {
                continuation.resume(resultConfig)
            }
        }
        .build()

    dialog.showTextStudioDialog()
}
