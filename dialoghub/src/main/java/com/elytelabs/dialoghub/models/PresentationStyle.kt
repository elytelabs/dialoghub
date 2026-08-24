package com.elytelabs.dialoghub.models

/**
 * Presentation mode for dialogs in DialogHub.
 * DialogHub modern architecture uses Material BottomSheet for all dialogs.
 */
enum class PresentationStyle {
    /**
     * Modern Material BottomSheet with top-rounded corners and drag handle.
     */
    BOTTOM_SHEET,

    /**
     * @deprecated DialogHub 1.4+ exclusively uses modern BottomSheet presentation for all dialogs.
     */
    @Deprecated("DialogHub now uses modern BottomSheet for all dialogs.")
    DIALOG
}
