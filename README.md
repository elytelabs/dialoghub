# DialogHub for Android

[![Release](https://jitpack.io/v/elytelabs/dialoghub.svg)](https://jitpack.io/#elytelabs/dialoghub)
[![API](https://img.shields.io/badge/API-25%2B-brightgreen.svg)](https://android-arsenal.com/api?level=25)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)

A lightweight Kotlin Android library that provides customizable dialogs for dynamically selecting **background images**, **device gallery photos**, **custom fonts (with localized preview text)**, **palette colors with live transparency**, **text formatting (size & alignment)**, and **advanced text effects (shadows, letter/line spacing, bold/italic/caps/underline)** — built with memory caching and zero-leak protections.

---

## Features

| Dialog | Description |
|---|---|
| **`ImageSelectorDialog`** | Pick from background drawables, pick from device gallery photos, or launch the color picker |
| **`FontStyleDialog`** | Select and preview custom font resources with custom localized script preview text (e.g. Urdu/Arabic) |
| **`ColorPickerDialog`** | Material palette color selector with live transparency slider across swatches and custom hex code input/copying |
| **`TextFormatDialog`** | Interactive dialog with text size seekbar (12sp to 42sp) and alignment buttons (`LEFT`, `CENTER`, `RIGHT`) |
| **`TextEffectsDialog`** | Advanced typography effects dialog (Bold, Italic, Underline, Caps, Drop Shadow blur/colors, Letter Spacing, Line Spacing) |
| **Universal Theming** | Fully compatible with both `Theme.Material3` / `Theme.MaterialComponents` and legacy `Theme.AppCompat` activities |
| **OOM Safe** | Downsampled `RGB_565` image decoding with `LruCache` for background thumbnails |
| **Smooth Motion** | Native Material 3 spring scale-in and fade-out window animations |
| **Modern Kotlin DSL** | Clean lambda callbacks alongside traditional interface listeners |

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
    implementation("com.github.elytelabs:dialoghub:1.2.1")
}
```

---

## Usage Guide

### 1. Background Image, Gallery & Color Selector (`ImageSelectorDialog`)

```kotlin
val backgrounds = listOf(
    R.drawable.bg_pattern_1,
    R.drawable.bg_pattern_2,
    R.drawable.bg_gradient_1
)

ImageSelectorDialog(this).show(
    backgrounds = backgrounds,
    selectedBackgroundResId = currentBackgroundRes,
    onPickFromGallery = {
        galleryLauncher.launch("image/*")
    },
    onImageSelected = { drawableRes ->
        rootLayout.setBackgroundResource(drawableRes)
    },
    onColorSelected = { colorInt ->
        rootLayout.setBackgroundColor(colorInt)
    }
)
```

---

### 2. Font Style Selector with Custom Preview Text (`FontStyleDialog`)

```kotlin
val fonts = listOf(
    R.font.righteous,
    R.font.salsa,
    R.font.urdu_nastaliq
)

FontStyleDialog(this).show(
    fonts = fonts,
    previewText = "اردو شاعری", // Or "Sample Text"
    selectedFontResId = currentFontRes
) { fontResId ->
    textView.typeface = ResourcesCompat.getFont(this, fontResId)
}
```

---

### 3. Color Picker with Live Transparency & Hex Input (`ColorPickerDialog`)

```kotlin
ColorPickerDialog(this).show(
    customColors = listOf(Color.BLACK, Color.WHITE, Color.RED, Color.BLUE), // Optional custom palette
    selectedColor = currentColor,                                            // Optional active color
    initialTransparency = 220                                                // 30 to 255
) { colorInt ->
    // colorInt includes alpha transparency selected via the slider
    rootLayout.setBackgroundColor(colorInt)
}
```

---

### 4. Text Formatting Dialog (`TextFormatDialog`)

```kotlin
TextFormatDialog(this).show(
    initialSizeSp = 22f,
    initialAlignment = TextFormatDialog.TextAlignment.CENTER,
    previewText = textView.text.toString()
) { newSizeSp, newAlignment ->
    textView.textSize = newSizeSp
    textView.gravity = newAlignment.gravity
}
```

---

### 5. Advanced Text Effects Dialog (`TextEffectsDialog`)

```kotlin
TextEffectsDialog(this).show(
    initialConfig = currentEffectsConfig,
    previewText = textView.text.toString()
) { config ->
    // Applies styles (Bold/Italic/Underline/Caps), Drop Shadow, Letter Spacing & Line Spacing
    config.applyTo(textView)
}
```

---

## Changelog

### v1.2.1
- **Universal Theme Compatibility**: Added `DialogThemeHelper` to automatically detect host themes and safely wrap legacy `Theme.AppCompat` contexts without crashes.
- **Brand Color Preservation**: Dialogs dynamically inherit host app's `colorPrimary` and styling when running under `Theme.MaterialComponents` / `Theme.Material3`.
- **Demo Enhancement**: Updated demo sample activity to support live testing with both Material and AppCompat contexts.

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
