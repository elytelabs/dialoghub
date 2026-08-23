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
| **Presentation Styles** | Standard floating **Dialog** or modern Material **BottomSheet** (`PresentationStyle.BOTTOM_SHEET`) |
| **Kotlin DSL & Builders** | Declarative type-safe DSL extensions (`showColorPickerDialog { ... }`) and fluent builder APIs |
| **Coroutines Async API** | Non-blocking suspension functions (`val color = context.awaitColor(...)`) |
| **Universal Theming** | Fully compatible with both `Theme.Material3` / `Theme.MaterialComponents` and legacy `Theme.AppCompat` activities |
| **OOM Safe** | Downsampled `RGB_565` image decoding with `LruCache` for background thumbnails |
| **Smooth Motion** | Native Material 3 spring scale-in and fade-out window animations |

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
    implementation("com.github.elytelabs:dialoghub:1.3.0")
}
```

---

## Invocation Paradigms

DialogHub supports three flexible integration styles for each dialog:

### 1. Coroutines Asynchronous API (`await*`)
Suspend execution cleanly and receive user selections sequentially without nested callbacks:

```kotlin
lifecycleScope.launch {
    // 1. Await Color Picker
    val selectedColor = context.awaitColor(
        selectedColor = currentColor,
        presentationStyle = PresentationStyle.BOTTOM_SHEET
    )
    if (selectedColor != null) {
        rootLayout.setBackgroundColor(selectedColor)
    }

    // 2. Await Font Selector
    val fontResId = context.awaitFont(
        fonts = fontList,
        previewText = "اردو شاعری",
        presentationStyle = PresentationStyle.BOTTOM_SHEET
    )
    if (fontResId != null) {
        textView.typeface = ResourcesCompat.getFont(context, fontResId)
    }

    // 3. Await Background Image or Gallery
    when (val result = context.awaitBackground(backgroundList, enableGalleryPick = true)) {
        is SelectedBackground.Image -> rootLayout.setBackgroundResource(result.drawableResId)
        is SelectedBackground.Color -> rootLayout.setBackgroundColor(result.colorInt)
        is SelectedBackground.GalleryRequested -> galleryPicker.launch("image/*")
        null -> {} // Dismissed
    }
}
```

---

### 2. Kotlin DSL & Fluent Builders
Declarative type-safe syntax with full configuration chaining:

```kotlin
context.showColorPickerDialog {
    setSelectedColor(currentColor)
    setInitialTransparency(220)
    setPresentationStyle(PresentationStyle.BOTTOM_SHEET)
    setOnColorSelected { color ->
        rootLayout.setBackgroundColor(color)
    }
}

context.showFontStyleDialog {
    setFonts(fontList)
    setPreviewText("Sample Text")
    setPresentationStyle(PresentationStyle.BOTTOM_SHEET)
    setOnFontSelected { fontResId ->
        textView.typeface = ResourcesCompat.getFont(context, fontResId)
    }
}

context.showTextFormatDialog {
    setTextSize(22f)
    setAlignment(TextFormatDialog.TextAlignment.CENTER)
    setPresentationStyle(PresentationStyle.BOTTOM_SHEET)
    setOnFormatChanged { size, alignment ->
        textView.textSize = size
        textView.gravity = alignment.gravity
    }
}
```

---

### 3. Traditional Instance Callback API

```kotlin
ColorPickerDialog(context).show(
    selectedColor = currentColor,
    presentationStyle = PresentationStyle.DIALOG
) { color ->
    rootLayout.setBackgroundColor(color)
}
```

---

## Changelog

### v1.3.0
- **Fluent Builder Pattern & Kotlin DSL**: Added chainable `Builder` inner classes and top-level DSL extensions (`showColorPickerDialog`, `showFontStyleDialog`, etc.).
- **BottomSheet Presentation Mode**: Added `PresentationStyle.BOTTOM_SHEET` alongside standard floating `PresentationStyle.DIALOG`.
- **Coroutines Asynchronous API**: Added non-blocking suspension extensions (`awaitColor`, `awaitFont`, `awaitBackground`, `awaitTextFormat`, `awaitTextEffects`).
- **Demo Enhancement**: Updated demo sample activity to showcase dynamic presentation style toggling and all 3 invocation paradigms.

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
