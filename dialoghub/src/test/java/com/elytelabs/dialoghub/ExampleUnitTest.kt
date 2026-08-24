package com.elytelabs.dialoghub

import com.elytelabs.dialoghub.models.PresentationStyle
import com.elytelabs.dialoghub.models.TextHighlightConfig
import com.elytelabs.dialoghub.models.TextStrokeConfig
import com.elytelabs.dialoghub.models.TextTypographyConfig
import com.elytelabs.dialoghub.utils.ColorPalettes
import com.elytelabs.dialoghub.utils.PoetryPalettes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun textTypographyConfig_defaultValues_areValid() {
        val config = TextTypographyConfig()
        assertEquals(null, config.fontResId)
        assertEquals(20f, config.textSizeSp)
        assertFalse(config.strokeConfig.isEnabled)
        assertFalse(config.highlightConfig.isEnabled)
    }

    @Test
    fun textStrokeConfig_copyAndProperties_workCorrectly() {
        val config = TextStrokeConfig(strokeWidthDp = 4f, isEnabled = true)
        assertTrue(config.isEnabled)
        assertEquals(4f, config.strokeWidthDp)

        val updated = config.copy(strokeWidthDp = 6f)
        assertEquals(6f, updated.strokeWidthDp)
        assertTrue(updated.isEnabled)
    }

    @Test
    fun textHighlightConfig_copyAndProperties_workCorrectly() {
        val config = TextHighlightConfig(cornerRadiusDp = 12f, isEnabled = true)
        assertTrue(config.isEnabled)
        assertEquals(12f, config.cornerRadiusDp)

        val updated = config.copy(cornerRadiusDp = 20f)
        assertEquals(20f, updated.cornerRadiusDp)
    }

    @Test
    fun colorPalettes_returnsValidPalettes() {
        val allColors = ColorPalettes.ALL_CURATED
        assertTrue(allColors.isNotEmpty())

        val boldColors = ColorPalettes.MOTIVATIONAL_BOLD
        assertTrue(boldColors.isNotEmpty())

        val neonColors = ColorPalettes.AESTHETIC_NEON
        assertTrue(neonColors.isNotEmpty())

        val calmColors = ColorPalettes.NATURE_SUFI_CALM
        assertTrue(calmColors.isNotEmpty())

        val pastelColors = ColorPalettes.PASTEL_SOFT
        assertTrue(pastelColors.isNotEmpty())

        val darkColors = ColorPalettes.MELANCHOLY_DARK
        assertTrue(darkColors.isNotEmpty())

        val vintageColors = ColorPalettes.VINTAGE_EARTHY
        assertTrue(vintageColors.isNotEmpty())

        // Test backward compatibility alias
        assertEquals(ColorPalettes.ALL_CURATED, PoetryPalettes.ALL_CURATED)
    }

    @Test
    fun presentationStyle_defaultIsBottomSheet() {
        assertEquals(PresentationStyle.BOTTOM_SHEET, PresentationStyle.valueOf("BOTTOM_SHEET"))
    }

    @Test
    fun studioTab_allTabsAndCustomFiltering_workCorrectly() {
        val defaultTabs = com.elytelabs.dialoghub.models.StudioTab.DEFAULT
        assertEquals(6, defaultTabs.size)
        assertTrue(defaultTabs.contains(com.elytelabs.dialoghub.models.StudioTab.FONT))

        val allTabs = com.elytelabs.dialoghub.models.StudioTab.ALL
        assertEquals(6, allTabs.size)
        assertTrue(allTabs.contains(com.elytelabs.dialoghub.models.StudioTab.COLOR))
        assertTrue(allTabs.contains(com.elytelabs.dialoghub.models.StudioTab.FONT))
        assertTrue(allTabs.contains(com.elytelabs.dialoghub.models.StudioTab.FORMAT))
        assertTrue(allTabs.contains(com.elytelabs.dialoghub.models.StudioTab.EFFECTS))
        assertTrue(allTabs.contains(com.elytelabs.dialoghub.models.StudioTab.STROKE))
        assertTrue(allTabs.contains(com.elytelabs.dialoghub.models.StudioTab.RIBBON))

        // Custom subset
        val subset = setOf(com.elytelabs.dialoghub.models.StudioTab.FONT, com.elytelabs.dialoghub.models.StudioTab.COLOR)
        assertEquals(2, subset.size)
        assertFalse(subset.contains(com.elytelabs.dialoghub.models.StudioTab.STROKE))
    }
}