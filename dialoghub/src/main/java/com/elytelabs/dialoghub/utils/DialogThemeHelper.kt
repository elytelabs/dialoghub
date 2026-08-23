package com.elytelabs.dialoghub.utils

import android.content.Context
import android.util.TypedValue
import androidx.appcompat.view.ContextThemeWrapper

internal object DialogThemeHelper {
    /**
     * Returns the original context if the host app already uses a MaterialComponents theme,
     * preserving the host app's primary/accent colors, fonts, and styling.
     * Otherwise wraps the context with Theme.MaterialComponents to prevent inflation crashes.
     */
    fun getThemedContext(context: Context): Context {
        val typedValue = TypedValue()
        // Check for public Material theme attribute (colorSurface), which only exists in MaterialComponents/Material3
        val isMaterial = context.theme.resolveAttribute(
            com.google.android.material.R.attr.colorSurface,
            typedValue,
            true
        )

        return if (isMaterial) {
            context
        } else {
            ContextThemeWrapper(
                context,
                com.google.android.material.R.style.Theme_MaterialComponents_DayNight_NoActionBar
            )
        }
    }
}
