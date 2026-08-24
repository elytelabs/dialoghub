package com.elytelabs.dialoghub.utils

import androidx.core.graphics.toColorInt

/**
 * Universal curated color palettes for quotes, motivation, facts, stories, poetry, and typography apps.
 */
object ColorPalettes {

    val MOTIVATIONAL_BOLD = listOf(
        "#D50000".toColorInt(), // Crimson Red
        "#FF6D00".toColorInt(), // Blaze Orange
        "#FFD600".toColorInt(), // Radiant Gold
        "#E65100".toColorInt(), // Deep Amber
        "#C51162".toColorInt(), // Electric Magenta
        "#FF1744".toColorInt(), // Vivid Coral
        "#2E7D32".toColorInt(), // Power Green
        "#0D47A1".toColorInt(), // Deep Royal Blue
        "#212121".toColorInt(), // Jet Black
        "#FFFFFF".toColorInt()  // Pure White
    )

    val AESTHETIC_NEON = listOf(
        "#00E5FF".toColorInt(), // Electric Cyan
        "#00E676".toColorInt(), // Neon Mint
        "#FFEA00".toColorInt(), // Radiant Yellow
        "#FF1744".toColorInt(), // Electric Red
        "#F50057".toColorInt(), // Neon Fuchsia
        "#D500F9".toColorInt(), // Ultra Violet
        "#651FFF".toColorInt(), // Electric Indigo
        "#3D5AFE".toColorInt(), // Royal Glow
        "#00B0FF".toColorInt(), // Sky Glow
        "#1DE9B6".toColorInt()  // Turquoise
    )

    val NATURE_SUFI_CALM = listOf(
        "#004D40".toColorInt(), // Deep Emerald
        "#00695C".toColorInt(), // Islamic Teal
        "#00796B".toColorInt(), // Emerald Jade
        "#2E7D32".toColorInt(), // Rich Forest
        "#558B2F".toColorInt(), // Sage Leaf
        "#FFB300".toColorInt(), // Amber Gold
        "#FFA000".toColorInt(), // Royal Ochre
        "#FFD54F".toColorInt(), // Soft Gold
        "#0277BD".toColorInt(), // Azure Blue
        "#F5F5DC".toColorInt()  // Ivory Cream
    )

    val MELANCHOLY_DARK = listOf(
        "#263238".toColorInt(), // Deep Charcoal
        "#37474F".toColorInt(), // Blue Grey Slate
        "#455A64".toColorInt(), // Muted Steel
        "#1A237E".toColorInt(), // Midnight Navy
        "#283593".toColorInt(), // Indigo Night
        "#3F51B5".toColorInt(), // Moody Blue
        "#607D8B".toColorInt(), // Ash Gray
        "#78909C".toColorInt(), // Overcast Sky
        "#424242".toColorInt(), // Dark Smoke
        "#000000".toColorInt()  // Obsidian Black
    )

    val VINTAGE_EARTHY = listOf(
        "#3E2723".toColorInt(), // Espresso
        "#4E342E".toColorInt(), // Roasted Walnut
        "#5D4037".toColorInt(), // Warm Terracotta
        "#795548".toColorInt(), // Sepia Brown
        "#8D6E63".toColorInt(), // Sand Dust
        "#D7CCC8".toColorInt(), // Parchment Paper
        "#33691E".toColorInt(), // Deep Olive
        "#BF360C".toColorInt(), // Rust Clay
        "#D4AF37".toColorInt(), // Antique Gold
        "#6D4C41".toColorInt()  // Cocoa
    )

    val PASTEL_SOFT = listOf(
        "#F8BBD0".toColorInt(), // Blush Pink
        "#E1BEE7".toColorInt(), // Soft Lavender
        "#D1C4E9".toColorInt(), // Periwinkle
        "#C5CAE9".toColorInt(), // Soft Indigo
        "#BBDEFB".toColorInt(), // Baby Blue
        "#B2EBF2".toColorInt(), // Pastel Cyan
        "#B2DFDB".toColorInt(), // Mint Cream
        "#C8E6C9".toColorInt(), // Pale Green
        "#FFF9C4".toColorInt(), // Soft Lemon
        "#FFE0B2".toColorInt()  // Peach Sorbet
    )

    val ALL_CURATED: List<Int> by lazy {
        val set = linkedSetOf<Int>()
        set.addAll(MOTIVATIONAL_BOLD)
        set.addAll(AESTHETIC_NEON)
        set.addAll(NATURE_SUFI_CALM)
        set.addAll(MELANCHOLY_DARK)
        set.addAll(VINTAGE_EARTHY)
        set.addAll(PASTEL_SOFT)
        set.toList()
    }
}

/**
 * Backward compatibility alias for poetry apps.
 */
typealias PoetryPalettes = ColorPalettes
