package com.elytelabs.dialoghub.dsl

import android.content.Context
import com.elytelabs.dialoghub.dialogs.ColorPickerDialog
import com.elytelabs.dialoghub.dialogs.FontStyleDialog
import com.elytelabs.dialoghub.dialogs.ImageSelectorDialog
import com.elytelabs.dialoghub.dialogs.TextEffectsDialog
import com.elytelabs.dialoghub.dialogs.TextFormatDialog
import com.elytelabs.dialoghub.dialogs.TextHighlightDialog
import com.elytelabs.dialoghub.dialogs.TextStrokeDialog
import com.elytelabs.dialoghub.dialogs.TextStudioDialog

inline fun Context.showColorPickerDialog(block: ColorPickerDialog.Builder.() -> Unit): ColorPickerDialog {
    return ColorPickerDialog.Builder(this).apply(block).show()
}

inline fun Context.showFontStyleDialog(block: FontStyleDialog.Builder.() -> Unit): FontStyleDialog {
    return FontStyleDialog.Builder(this).apply(block).show()
}

inline fun Context.showImageSelectorDialog(block: ImageSelectorDialog.Builder.() -> Unit): ImageSelectorDialog {
    return ImageSelectorDialog.Builder(this).apply(block).show()
}

inline fun Context.showTextFormatDialog(block: TextFormatDialog.Builder.() -> Unit): TextFormatDialog {
    return TextFormatDialog.Builder(this).apply(block).show()
}

inline fun Context.showTextEffectsDialog(block: TextEffectsDialog.Builder.() -> Unit): TextEffectsDialog {
    return TextEffectsDialog.Builder(this).apply(block).show()
}

inline fun Context.showTextStrokeDialog(block: TextStrokeDialog.Builder.() -> Unit): TextStrokeDialog {
    return TextStrokeDialog.Builder(this).apply(block).show()
}

inline fun Context.showTextHighlightDialog(block: TextHighlightDialog.Builder.() -> Unit): TextHighlightDialog {
    return TextHighlightDialog.Builder(this).apply(block).show()
}

inline fun Context.showTextStudioDialog(block: TextStudioDialog.Builder.() -> Unit): TextStudioDialog {
    return TextStudioDialog.Builder(this).apply(block).show()
}
