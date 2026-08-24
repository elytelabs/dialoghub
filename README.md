# DialogHub for Android

[![Release](https://jitpack.io/v/elytelabs/dialoghub.svg)](https://jitpack.io/#elytelabs/dialoghub)
[![API](https://img.shields.io/badge/API-25%2B-brightgreen.svg)](https://android-arsenal.com/api?level=25)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)

A lightweight Kotlin Android library that provides customizable dialogs for dynamically selecting **background images**, **device gallery photos**, **custom fonts (with localized preview text)**, **palette colors with live transparency**, **text formatting (size & alignment)**, and **advanced text effects (shadows, letter/line spacing, bold/italic/caps/underline)** — built with memory caching and zero-leak protections.

---

## Features

| Dialog | Description                                                                                                                                                                           |
|---|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **`TextStudioDialog`**  | All-In-One Unified Text Studio**: Combines Presets, Colors, Fonts, Sizing, Alignment, Effects, Stroke, and Ribbon with 3-way canvas toggle (☀️/🌙/📜) and real-time live preview      |
| **`ImageSelectorDialog`** | Pick from background drawables, device gallery photos, or launch the color picker                                                                                                     |
| **`FontStyleDialog`** | Spacious font selector with live custom script/Urdu preview per card                                                                                                                  |
| **`ColorPickerDialog`** | Material palette color selector with curated universal mood categories (**All**, **Bold**, **Neon**, **Calm**, **Pastel**, **Dark**, **Vintage**), transparency slider, and hex input |
| **`TextStrokeDialog`** | Configure text stroke / outline thickness (0dp to 16dp) and outline colors to ensure legibility over any wallpaper                                                                    |
| **`TextHighlightDialog`** | Customizable background ribbon/highlight pill (corner radius, colors, alpha) behind quotes                                                                                            |
| **`StylePresetsDialog`** | 1-Tap complete typography presets for Quotes, Facts, Motivation, Jokes, Stories, and Poetry with auto-contrast preview canvases                                                       |
| **`TextFormatDialog`** | Interactive dialog with text size seekbar (12sp to 44sp) and alignment buttons (`LEFT`, `CENTER`, `RIGHT`)                                                                            |
| **`TextEffectsDialog`** | Advanced typography effects dialog (Bold, Italic, Underline, Caps, Drop Shadow blur/colors, Letter Spacing, Line Spacing)                                                             |
| **Modern BottomSheet UI** | All dialogs render exclusively in fluid, native Material BottomSheets with drag handles and rounded corners                                                                           |
| **Kotlin DSL & Builders** | Declarative type-safe DSL extensions (`showTextStudioDialog { ... }`) and fluent builder APIs                                                                                         |
| **Coroutines Async API** | Non-blocking suspension functions (`val config = context.awaitTextStudio(...)`, `awaitColor(...)`, etc.)                                                                              |
| **Universal Theming** | Fully compatible with both `Theme.Material3` / `Theme.MaterialComponents` and legacy `Theme.AppCompat` activities                                                                     |
| **OOM Safe** | Downsampled `RGB_565` image decoding with `LruCache` for background thumbnails                                                                                                        |
| **Smooth Motion** | Native Material 3 spring scale-in and fade-out animations                                                                                                                             |

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

## Invocation Paradigms

DialogHub supports three modern, flexible integration styles:

### 1. Coroutines Asynchronous API (`await*`)
Suspend execution cleanly and receive user selections sequentially without callbacks:

```kotlin
lifecycleScope.launch {
    //  All-In-One Unified Text Studio
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

### 2. Kotlin DSL & Fluent Builders
Declarative type-safe syntax with live update listeners and full configuration chaining:

```kotlin
//  All-In-One Text Studio (with real-time live preview sync)
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

## Changelog

### v1.4.0
- **Modern BottomSheet Architecture**: Converted all dialogs to native Material `BottomSheetDialog` instances with drag handles and smooth sheet expansion.
- **Unified All-In-One Text Studio (`TextStudioDialog`)**: A single cohesive studio combining 1-tap style presets, color palettes, fonts, sizing & alignment, styling effects & drop shadows, text stroke / outline, and background ribbons.
- **Live Preview Sync & 3-Way Canvas Toggle**: Added real-time live preview callback (`onLivePreview` / `setOnLivePreviewListener`) and a 3-way canvas contrast toggle (Dark `#1E293B`, Light `#F8FAFC`, Warm Parchment `#FEF3C7`).
- **Smooth Auto-Centering Tabs**: Horizontal tabs smoothly scroll and center on selection.
- **Universal Multi-App Palettes & Presets**: Generalized palettes and presets across Motivation, Facts, Cyber Neon, Stories, Modern Minimal, Romance, Humor/Jokes, and Poetry.
- **`String.toColorInt()` Migration**: Replaced all `Color.parseColor` occurrences with Android KTX `String.toColorInt()`.
- **Text Stroke & Outline Dialog (`TextStrokeDialog`)**: Adjust outline thickness and colors to guarantee text legibility over any photo wallpaper.
- **Text Background Ribbon Dialog (`TextHighlightDialog`)**: Add custom highlight boxes and rounded background pills behind text.
- **Streamlined Font Selector**: Removed redundant top preview card to maximize screen space for the full font grid.
- **Modern Dialog Headers**: Removed obsolete `<` back button in favor of clean title layout and top-right `✕` dismiss button.
- **Fluent Builder Pattern & Kotlin DSL**: Added chainable `Builder` inner classes and top-level DSL extensions (`showColorPickerDialog`, `showFontStyleDialog`, `showTextStudioDialog`, etc.).
- **Coroutines Asynchronous API**: Added non-blocking suspension extensions (`awaitTextStudio`, `awaitColor`, `awaitFont`, `awaitBackground`, `awaitTextFormat`, `awaitTextEffects`, `awaitTextStroke`, `awaitTextHighlight`, `awaitStylePreset`).

### v1.2.1
- **Universal Theme Compatibility**: Added `DialogThemeHelper` to automatically detect host themes and safely wrap legacy `Theme.AppCompat` contexts without crashes.
- **Brand Color Preservation**: Dialogs dynamically inherit host app's `colorPrimary` and styling when running under `Theme.MaterialComponents` / `Theme.Material3`.

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
