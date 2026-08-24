# DialogHub for Android

[![Release](https://jitpack.io/v/elytelabs/dialoghub.svg)](https://jitpack.io/#elytelabs/dialoghub)
[![API](https://img.shields.io/badge/API-25%2B-brightgreen.svg)](https://android-arsenal.com/api?level=25)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)

A lightweight, modern Kotlin Android library that provides customizable dialogs for dynamically selecting **background images**, **device gallery photos**, **custom fonts (with localized preview text)**, **palette colors with live transparency**, **text formatting (size & alignment)**, and **advanced typography effects (shadows, letter/line spacing, bold/italic/caps/underline, stroke, ribbons)** — featuring built-in **Monetization Locking**, **12-Hour VIP Pass per Rewarded Ad**, and daily quota management with zero-leak protections.

---

## Features

| Dialog / Module | Description |
|---|---|
| **`TextStudioDialog`**  | **All-In-One Unified Text Studio**: Combines Colors, Fonts, Sizing, Alignment, Effects, Stroke, and Highlight Ribbon with 3-way canvas contrast toggle (Dark, Light, Warm Parchment) and real-time live preview |
| **`ImageSelectorDialog`** | Pick from background drawables, device gallery photos, or launch the color picker |
| **`FontStyleDialog`** | Spacious font selector with live custom script/Urdu preview per card |
| **`ColorPickerDialog`** | Material palette color selector with curated universal mood categories (**All**, **Bold**, **Neon**, **Calm**, **Pastel**, **Dark**, **Vintage**), transparency slider, and custom hex code input |
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

### Step 2: Add the dependency

In your module's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.github.elytelabs:dialoghub:1.4.0")
}
```

---

## Monetization & 12-Hour VIP Pass Integration

DialogHub is 100% independent and decoupled from Google AdMob or Google Play Billing SDKs. It provides lock badges, click interceptors, and persistent timed pass tracking:

```kotlin
// 1. Initialize lock provider and attach 12-Hour Timed Pass manager
val lockProvider = DefaultItemLockProvider()
    .attachTimedPass(this)
    .lockFonts(R.font.righteous, R.font.sofadi_one) // Lock VIP fonts
    .lockBackgrounds(R.drawable.bg5)                // Lock VIP wallpapers
    .lockTab(StudioTab.RIBBON)                      // Lock premium tool tabs

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

---

## Invocation Paradigms

DialogHub supports three modern, flexible integration styles:

### 1. Kotlin DSL & Fluent Builders
Declarative type-safe syntax with live update listeners and full configuration chaining:

```kotlin
// All-In-One Text Studio (with real-time live preview sync)
context.showTextStudioDialog {
    setPreviewText("Sample Quote / شاعری")
    setFonts(fontList)
    setOnLivePreviewListener { liveConfig ->
        // Updates on-screen TextView in real-time as user slides controls
        liveConfig.applyTo(textView)
    }
    setOnTypographyApplied { typography ->
        typography.applyTo(textView)
    }
}

// Or launch individual dialogs:
context.showTextStrokeDialog {
    setPreviewText("Your Quote")
    setOnStrokeChanged { config -> config.applyTo(textView) }
}
```

---

### 2. Coroutines Asynchronous API (`await*`)
Suspend execution cleanly and receive user selections sequentially without callbacks:

```kotlin
lifecycleScope.launch {
    // All-In-One Unified Text Studio
    val typography = context.awaitTextStudio(
        previewText = "Stay Hungry, Stay Foolish",
        fonts = fontList
    )
    typography.applyTo(textView)

    // Or launch individual bottom sheets:
    val stroke = context.awaitTextStroke()
    stroke.applyTo(textView)
}
```

---

### 3. Traditional Instance Callback API

```kotlin
TextStudioDialog(context).show(
    previewText = textView.text.toString(),
    fonts = fontList,
    onLivePreview = { liveConfig ->
        liveConfig.applyTo(textView)
    }
) { typography ->
    typography.applyTo(textView)
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
