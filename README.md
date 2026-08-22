# DialogHub for Android

[![Release](https://jitpack.io/v/elytelabs/dialoghub.svg)](https://jitpack.io/#elytelabs/dialoghub)
[![API](https://img.shields.io/badge/API-25%2B-brightgreen.svg)](https://android-arsenal.com/api?level=25)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)

A lightweight Kotlin Android library that provides customizable dialogs for dynamically selecting **background images**, **custom fonts**, and **colors with transparency** — built with memory caching and zero-leak protections.

---

## Features

| Dialog | Description |
|---|---|
| **`ImageSelectorDialog`** | Pick from background drawable resources or launch the color picker |
| **`FontStyleDialog`** | Select and preview custom font resources (`.ttf`/`.otf`/XML) dynamically |
| **`ColorPickerDialog`** | Material palette color selector with dynamic alpha transparency slider |
| **OOM Safe** | Downsampled `RGB_565` image decoding with `LruCache` for background thumbnails |
| **Font Caching** | Memory-cached `Typeface` lookup eliminating UI stutter during scrolling |
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
    implementation("com.github.elytelabs:dialoghub:1.2.0")
}
```

---

## Usage Guide

### 1. Background Image & Color Selector (`ImageSelectorDialog`)

```kotlin
val backgrounds = listOf(
    R.drawable.bg_pattern_1,
    R.drawable.bg_pattern_2,
    R.drawable.bg_gradient_1
)

ImageSelectorDialog(this).show(
    backgrounds = backgrounds,
    onImageSelected = { drawableRes ->
        rootLayout.setBackgroundResource(drawableRes)
    },
    onColorSelected = { colorInt ->
        rootLayout.setBackgroundColor(colorInt)
    }
)
```

---

### 2. Font Style Selector (`FontStyleDialog`)

```kotlin
val fonts = listOf(
    R.font.righteous,
    R.font.salsa,
    R.font.schoolbell,
    R.font.sofadi_one
)

FontStyleDialog(this).show(fonts = fonts) { fontResId ->
    textView.typeface = ResourcesCompat.getFont(this, fontResId)
}
```

---

### 3. Color Picker with Transparency (`ColorPickerDialog`)

```kotlin
ColorPickerDialog(this).show { colorInt ->
    // colorInt includes alpha transparency selected via the slider
    rootLayout.setBackgroundColor(colorInt)
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
