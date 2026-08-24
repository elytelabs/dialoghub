# DialogHub for Android

[![Release](https://jitpack.io/v/elytelabs/dialoghub.svg)](https://jitpack.io/#elytelabs/dialoghub)
[![API](https://img.shields.io/badge/API-25%2B-brightgreen.svg)](https://android-arsenal.com/api?level=25)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)

A lightweight, modern Kotlin Android library providing customizable BottomSheet dialogs for dynamically selecting **background images**, **device gallery photos**, **custom fonts (with localized preview text)**, **palette colors with live transparency**, **text formatting (size & alignment)**, and **advanced typography effects (shadows, letter/line spacing, bold/italic/caps/underline, stroke, highlight ribbons)** — featuring built-in **Monetization Locking**, **12-Hour VIP Pass per Rewarded Ad**, and daily quota management with zero-leak protections.

---

## Features

| Dialog / Module | Description |
|---|---|
| **`TextStudioDialog`** | **All-In-One Unified Text Studio**: Combines Colors, Fonts, Sizing, Alignment, Effects, Stroke, and Highlight Ribbon with 3-way canvas contrast toggle (Dark, Light, Warm Parchment) and real-time live preview |
| **`ImageSelectorDialog`** | Pick from background drawables, device gallery photos, or launch the color picker |
| **`FontStyleDialog`** | Spacious font selector with live custom script/Urdu preview per card |
| **`ColorPickerDialog`** | Material palette color selector with curated mood categories (**All**, **Bold**, **Neon**, **Calm**, **Pastel**, **Dark**, **Vintage**), transparency slider, and custom hex code input |
| **`TextStrokeDialog`** | Configure text stroke / outline thickness (0dp to 16dp) and outline colors to ensure legibility over any wallpaper |
| **`TextHighlightDialog`** | Customizable background ribbon/highlight pill (corner radius, colors, alpha) behind text |
| **`TextFormatDialog`** | Interactive dialog with text size seekbar (12sp to 42sp) and alignment buttons (`LEFT`, `CENTER`, `RIGHT`) |
| **`TextEffectsDialog`** | Advanced typography effects dialog (Bold, Italic, Underline, Caps, Drop Shadow blur/colors, Letter Spacing, Line Spacing) |
| **Monetization & Locking Engine** | Integrated `ItemLockProvider`, `TimedPassManager` (12-Hour VIP pass per ad), and `UsageQuotaManager` (daily free edits + midnight reset). Decoupled from any ad SDK |
| **Modern BottomSheet UI** | All dialogs render exclusively in fluid, native Material BottomSheets with drag handles and rounded corners |
| **Kotlin DSL & Builders** | Declarative type-safe DSL extensions (`showTextStudioDialog { ... }`) and fluent builder APIs |
| **Coroutines Async API** | Non-blocking suspension functions (`val config = context.awaitTextStudio(...)`, `awaitColor(...)`, etc.) |
| **Universal Theming** | Fully compatible with both `Theme.Material3` / `Theme.MaterialComponents` and legacy `Theme.AppCompat` activities |
| **OOM Safe** | Downsampled `RGB_565` image decoding with `LruCache` for background thumbnails |

---

## Installation

### Step 1: Add JitPack repository

In your `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

### Step 2: Add to Version Catalog (`libs.versions.toml`)

In your `gradle/libs.versions.toml`:

```toml
[versions]
dialoghub = "1.5.0"

[libraries]
dialoghub = { module = "com.github.elytelabs:dialoghub", version.ref = "dialoghub" }
```

### Step 3: Add the dependency

In your module's `build.gradle.kts`:

```kotlin
dependencies {
    implementation(libs.dialoghub)
}
```

*(Or direct dependency without Version Catalog)*:

```kotlin
dependencies {
    implementation("com.github.elytelabs:dialoghub:1.5.0")
}
```

---

## Unified Text Studio (`TextStudioDialog`)

The flagship dialog that unifies all typography editing tools into a single, cohesive BottomSheet with live preview and canvas contrast switching:

```kotlin
context.showTextStudioDialog {
    setConfig(currentTypographyConfig)
    setPreviewText("Stay Hungry, Stay Foolish")
    setFonts(listOf(R.font.font1, R.font.font2, R.font.font3))

    // Optional: filter tabs (defaults to StudioTab.DEFAULT / ALL)
    setTabs(StudioTab.FONT, StudioTab.COLOR, StudioTab.FORMAT, StudioTab.STROKE, StudioTab.EFFECTS, StudioTab.RIBBON)

    // Optional: set custom preview wallpaper canvas
    setBackgroundRes(R.drawable.my_wallpaper)

    // Real-time live preview (updates TextView on screen as user tweaks controls)
    setOnLivePreviewListener { liveConfig ->
        liveConfig.applyTo(textView)
    }

    // Final confirmed result
    setOnTypographyApplied { config ->
        config.applyTo(textView)
    }
}
```

---

## Individual Dialog Usage

DialogHub also provides dedicated, standalone BottomSheets for specific styling workflows:

### 1. Font Style Dialog (`FontStyleDialog`)
```kotlin
context.showFontStyleDialog {
    setPreviewText("Sample Text / شاعری")
    setFonts(listOf(R.font.font1, R.font.font2, R.font.font3))
    setSelectedFont(R.font.font1)
    setOnFontSelected { fontResId ->
        textView.typeface = ResourcesCompat.getFont(context, fontResId)
    }
}
```

### 2. Color Picker Dialog (`ColorPickerDialog`)
```kotlin
context.showColorPickerDialog {
    setSelectedColor(Color.WHITE)
    setInitialTransparency(255) // 30 to 255
    setOnColorSelected { colorInt ->
        textView.setTextColor(colorInt)
    }
}
```

### 3. Background / Image Selector Dialog (`ImageSelectorDialog`)
```kotlin
context.showImageSelectorDialog {
    setBackgroundImages(listOf(R.drawable.bg1, R.drawable.bg2, R.drawable.bg3))
    setOnBackgroundSelected { selectedBackground ->
        when (selectedBackground) {
            is SelectedBackground.Image -> myRootLayout.setBackgroundResource(selectedBackground.drawableResId)
            is SelectedBackground.Color -> myRootLayout.setBackgroundColor(selectedBackground.colorInt)
            is SelectedBackground.GalleryRequested -> openPhotoPicker()
        }
    }
}
```

### 4. Text Stroke & Outline Dialog (`TextStrokeDialog`)
```kotlin
context.showTextStrokeDialog {
    setConfig(TextStrokeConfig(strokeWidthDp = 3f, strokeColor = Color.BLACK, isEnabled = true))
    setPreviewText("Outline Preview")
    setOnStrokeChanged { strokeConfig ->
        strokeConfig.applyTo(textView)
    }
}
```

### 5. Text Highlight / Ribbon Dialog (`TextHighlightDialog`)
```kotlin
context.showTextHighlightDialog {
    setConfig(TextHighlightConfig(backgroundColor = "#80000000".toColorInt(), cornerRadiusDp = 12f, isEnabled = true))
    setPreviewText("Ribbon Preview")
    setOnHighlightChanged { highlightConfig ->
        highlightConfig.applyTo(textView)
    }
}
```

### 6. Text Size & Alignment Dialog (`TextFormatDialog`)
```kotlin
context.showTextFormatDialog {
    setTextSize(22f)
    setAlignment(TextFormatDialog.TextAlignment.CENTER)
    setPreviewText("Format Preview")
    setOnFormatChanged { sizeSp, alignment ->
        textView.textSize = sizeSp
        textView.gravity = alignment.gravity
    }
}
```

### 7. Typography Effects Dialog (`TextEffectsDialog`)
```kotlin
context.showTextEffectsDialog {
    setConfig(currentEffectConfig)
    setPreviewText("Effects Preview")
    setOnEffectsChanged { effectConfig ->
        effectConfig.applyTo(textView)
    }
}
```

---

## Coroutines Asynchronous API (`await*`)

Suspend execution cleanly and receive user selections sequentially without callbacks:

```kotlin
lifecycleScope.launch {
    // Unified Studio
    val typography = context.awaitTextStudio(
        previewText = "Stay Hungry",
        fonts = fontList
    )
    typography?.applyTo(textView)

    // Standalone dialogs
    val selectedColor = context.awaitColor(selectedColor = Color.RED)
    selectedColor?.let { textView.setTextColor(it) }

    val selectedFont = context.awaitFont(previewText = "Preview", fonts = fontList)
    selectedFont?.let { textView.typeface = ResourcesCompat.getFont(context, it) }

    val stroke = context.awaitTextStroke()
    stroke?.applyTo(textView)

    val highlight = context.awaitTextHighlight()
    highlight?.applyTo(textView)

    val effects = context.awaitTextEffects()
    effects?.applyTo(textView)

    val format = context.awaitTextFormat()
    format?.let {
        textView.textSize = it.textSizeSp
        textView.gravity = it.alignment.gravity
    }
}
```

---

## Monetization & 12-Hour VIP Pass Integration

DialogHub is 100% independent and decoupled from Google AdMob or Google Play Billing SDKs. It provides lock badges, click interceptors, and persistent timed pass tracking:

```kotlin
// 1. Initialize lock provider and attach 12-Hour Timed Pass manager
val lockProvider = DefaultItemLockProvider()
    .attachTimedPass(this)
    .lockFonts(R.font.font_pro1, R.font.font_pro2) // Lock VIP fonts
    .lockBackgrounds(R.drawable.bg_premium)        // Lock VIP wallpapers
    .lockTab(StudioTab.RIBBON)                     // Lock premium tool tabs
    .lockHexInput(true)                            // Lock custom hex input

// 2. Launch Text Studio with lock provider and ad callback
context.showTextStudioDialog {
    setConfig(currentTypography)
    setPreviewText("Sample Quote / شاعری")
    setFonts(fontList)
    setLockProvider(lockProvider)

    // Intercept clicks on locked items
    setOnLockedItemClicked { item, onUnlocked ->
        // Show your Rewarded Video Ad:
        AdMobRewardedAd.show(this@MainActivity) {
            // User completed ad -> Grant 12-Hour VIP Pass!
            lockProvider.grantTimedPass(hours = 12)
            onUnlocked() // Lock badges disappear and item is immediately selected!
        }
    }

    setOnTypographyApplied { typography ->
        typography.applyTo(textView)
    }
}
```

### Daily Usage Quota Manager (`UsageQuotaManager`)
```kotlin
val quotaManager = UsageQuotaManager(context, defaultDailyQuota = 3)

// Check if user has quota remaining
if (quotaManager.consumeEdit()) {
    // Edit allowed
} else {
    // Prompt rewarded ad to unlock 5 bonus edits or 12-hour pass
    AdMobRewardedAd.show(activity) {
        quotaManager.addBonusEdits(5)
    }
}
```

---

## Requirements

- **Min SDK**: 25 (Android 7.1)
- **Compile/Target SDK**: 37
- **Language**: Kotlin 2.x
- **Build System**: Android Gradle Plugin (AGP) 9.x

---

## License

```
Copyright 2026 Elyte Labs

Licensed under the Apache License, Version 2.0
```
