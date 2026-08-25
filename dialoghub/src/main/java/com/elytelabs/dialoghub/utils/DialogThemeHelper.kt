package com.elytelabs.dialoghub.utils

import android.content.Context
import android.util.TypedValue
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.graphics.toColorInt

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

    /**
     * Dynamically resolves the host application's primary accent color at runtime.
     * Checks colorAccent, colorPrimary, and falls back gracefully.
     */
    fun resolveThemeAccentColor(context: Context): Int {
        val typedValue = TypedValue()
        val theme = context.theme

        // 1. Check colorAccent
        if (theme.resolveAttribute(androidx.appcompat.R.attr.colorAccent, typedValue, true)) {
            if (typedValue.type in TypedValue.TYPE_FIRST_COLOR_INT..TypedValue.TYPE_LAST_COLOR_INT) {
                return typedValue.data
            }
        }

        // 2. Check androidx colorPrimary
        if (theme.resolveAttribute(androidx.appcompat.R.attr.colorPrimary, typedValue, true)) {
            if (typedValue.type in TypedValue.TYPE_FIRST_COLOR_INT..TypedValue.TYPE_LAST_COLOR_INT) {
                return typedValue.data
            }
        }

        // 3. Check framework colorPrimary
        if (theme.resolveAttribute(android.R.attr.colorPrimary, typedValue, true)) {
            if (typedValue.type in TypedValue.TYPE_FIRST_COLOR_INT..TypedValue.TYPE_LAST_COLOR_INT) {
                return typedValue.data
            }
        }

        return "#10B981".toColorInt()
    }
}
