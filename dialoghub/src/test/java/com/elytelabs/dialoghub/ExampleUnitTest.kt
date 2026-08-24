package com.elytelabs.dialoghub

import com.elytelabs.dialoghub.models.StudioTab
import com.elytelabs.dialoghub.models.TextHighlightConfig
import com.elytelabs.dialoghub.models.TextStrokeConfig
import com.elytelabs.dialoghub.models.TextTypographyConfig
import com.elytelabs.dialoghub.monetization.DefaultItemLockProvider
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
    fun studioTab_allTabsAndCustomFiltering_workCorrectly() {
        val defaultTabs = StudioTab.DEFAULT
        assertEquals(6, defaultTabs.size)
        assertTrue(defaultTabs.contains(StudioTab.FONT))

        val allTabs = StudioTab.ALL
        assertEquals(6, allTabs.size)
        assertTrue(allTabs.contains(StudioTab.COLOR))
        assertTrue(allTabs.contains(StudioTab.FONT))
        assertTrue(allTabs.contains(StudioTab.FORMAT))
        assertTrue(allTabs.contains(StudioTab.EFFECTS))
        assertTrue(allTabs.contains(StudioTab.STROKE))
        assertTrue(allTabs.contains(StudioTab.RIBBON))

        // Custom subset
        val subset = setOf(StudioTab.FONT, StudioTab.COLOR)
        assertEquals(2, subset.size)
        assertFalse(subset.contains(StudioTab.STROKE))
    }

    @Test
    fun itemLockProvider_lockingAndUnlocking_works() {
        val provider = DefaultItemLockProvider()
        assertFalse(provider.isFontLocked(101))

        provider.lockFont(101)
        assertTrue(provider.isFontLocked(101))
        assertFalse(provider.isFontLocked(102))

        provider.unlockFont(101)
        assertFalse(provider.isFontLocked(101))

        provider.lockBackgrounds(201, 202)
        assertTrue(provider.isBackgroundLocked(201))
        assertTrue(provider.isBackgroundLocked(202))
        assertFalse(provider.isBackgroundLocked(203))

        provider.lockTabs(StudioTab.RIBBON)
        assertTrue(provider.isTabLocked(StudioTab.RIBBON))
        assertFalse(provider.isTabLocked(StudioTab.FONT))

        provider.lockGallery(true)
        assertTrue(provider.isGalleryLocked())

        provider.unlockAll()
        assertFalse(provider.isBackgroundLocked(201))
        assertFalse(provider.isTabLocked(StudioTab.RIBBON))
        assertFalse(provider.isGalleryLocked())
    }

    @Test
    fun selectedBackground_types_workCorrectly() {
        val img = com.elytelabs.dialoghub.models.SelectedBackground.Image(12345)
        assertEquals(12345, img.drawableResId)

        val color = com.elytelabs.dialoghub.models.SelectedBackground.Color(0xFF0000)
        assertEquals(0xFF0000, color.colorInt)

        val gallery = com.elytelabs.dialoghub.models.SelectedBackground.GalleryRequested
        assertEquals(com.elytelabs.dialoghub.models.SelectedBackground.GalleryRequested, gallery)
    }

    @Test
    fun itemLockProvider_colorAndPredicate_works() {
        val provider = DefaultItemLockProvider()
        provider.lockColor(0xFF112233.toInt())
        assertTrue(provider.isColorLocked(0xFF112233.toInt()))
        assertFalse(provider.isColorLocked(0xFFFFFFFF.toInt()))

        provider.lockColorWhen { it == 0xFF445566.toInt() }
        assertTrue(provider.isColorLocked(0xFF445566.toInt()))

        provider.unlockColor(0xFF112233.toInt())
        assertFalse(provider.isColorLocked(0xFF112233.toInt()))
    }
}