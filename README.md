# DialogHub for Android

[![Release](https://jitpack.io/v/elytelabs/dialoghub.svg)](https://jitpack.io/#elytelabs/dialoghub)
[![API](https://img.shields.io/badge/API-25%2B-brightgreen.svg)](https://android-arsenal.com/api?level=25)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0)

A lightweight, modern Kotlin Android library providing customizable BottomSheet dialogs for dynamically selecting **background images**, **device gallery photos**, **custom fonts (with localized preview text)**, **curated palette colors with live transparency**, **text formatting (size, alignment, bold/italic/caps/underline, kerning & leading)**, and **advanced typography effects (stroke outline, drop shadow, ribbon highlight)** — featuring built-in **Monetization Locking**, **12-Hour VIP Pass per Rewarded Ad**, and daily quota management with zero-leak protections.

---

## Architecture & Dialogs

DialogHub provides two specialized, high-performance BottomSheet dialogs designed for creative typography and image editing apps:

| Dialog | Description |
|---|---|
| **`TextStudioDialog`** | **All-In-One Unified Text Studio**: 4 Canonical Tabs (**Font**, **Color**, **Format**, **Effects**), live interactive canvas preview with 3-way contrast toggle (Dark, Light, Warm Parchment), and comprehensive styling controls. |
| **`ImageSelectorDialog`** | Multi-purpose background picker supporting **drawable image resources**, **device gallery photos**, and **solid background colors**. |
| **Monetization & Locking Engine** | Integrated `ItemLockProvider`, `TimedPassManager` (12-Hour VIP pass per ad), and `UsageQuotaManager` (daily free edits + midnight reset). 100% decoupled from any ad SDK. |
| **Universal Theming** | Fully compatible with `Theme.Material3`, `Theme.MaterialComponents`, and legacy `Theme.AppCompat` activities. |

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
dialoghub = "1.5.2"

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
    implementation("com.github.elytelabs:dialoghub:1.5.2")
}
```

---

## 1. Unified Text Studio (`TextStudioDialog`)

The flagship dialog that unifies all typography editing tools into a single, cohesive BottomSheet with live preview and canvas contrast switching:

### Builder Pattern

```kotlin
TextStudioDialog.Builder(this)
    .setConfig(currentTypographyConfig)
    .setPreviewText("Stay Hungry, Stay Foolish")
    .setFonts(listOf(R.font.font1, R.font.font2, R.font.font3))
    
    // Optional: filter tabs or open focused on a specific tool (StudioTab.FONT, COLOR, FORMAT, EFFECTS)
    .setTabs(StudioTab.FONT, StudioTab.COLOR, StudioTab.FORMAT, StudioTab.EFFECTS)
    
    // Optional: set custom preview wallpaper canvas
    .setBackgroundRes(R.drawable.my_wallpaper)
    
    // Optional: attach monetization lock provider
    .setLockProvider(lockProvider)
    .setOnLockedItemClicked { item, onUnlocked ->
        handleRewardedAd(item, onUnlocked)
    }
    
    // Real-time live preview (updates TextView on screen as user tweaks sliders)
    .setOnLivePreviewListener { liveConfig ->
        liveConfig.applyTo(textView)
    }
    
    // Final confirmed result upon dismissal
    .setOnTypographyApplied { config ->
        config.applyTo(textView)
    }
    .show()
```

### 4 Canonical Studio Tabs

1. **`StudioTab.FONT`**: Browse custom font styles with dynamic custom preview text per font card.
2. **`StudioTab.COLOR`**: Curated color palettes with mood categories (**All**, **Bold**, **Neon**, **Calm**, **Pastel**, **Dark**, **Vintage**) and transparency slider.
3. **`StudioTab.FORMAT`**: Text size slider (12sp to 44sp), text alignment (`LEFT`, `CENTER`, `RIGHT`), style buttons (Bold, Italic, Underline, Caps), letter spacing (kerning), and line spacing (leading).
4. **`StudioTab.EFFECTS`**:
   - **Text Stroke / Outline**: Width slider (0dp to 16dp) + dedicated outline color palette.
   - **Drop Shadow**: Blur radius slider (0dp to 25dp) + dedicated shadow color palette.
   - **Ribbon / Highlight**: Background pill corner radius (0dp to 32dp) + dedicated ribbon color palette.

---

## 2. Background & Image Selector (`ImageSelectorDialog`)

Allows users to pick background images from drawables, select photos from device gallery, or choose solid background colors:

```kotlin
ImageSelectorDialog.Builder(this)
    .setBackgrounds(listOf(R.drawable.bg1, R.drawable.bg2, R.drawable.bg3))
    .setSelectedBackground(currentBackgroundResId)
    .setEnableGalleryPick(true) {
        galleryLauncher.launch("image/*")
    }
    .setLockProvider(lockProvider)
    .setOnLockedItemClicked { item, onUnlocked ->
        handleRewardedAd(item, onUnlocked)
    }
    .setOnImageSelected { drawableResId ->
        rootLayout.setBackgroundResource(drawableResId)
    }
    .setOnColorSelected { colorInt ->
        rootLayout.setBackgroundColor(colorInt)
    }
    .show()
```

---

## 3. Monetization & 12-Hour VIP Pass Integration

DialogHub is 100% independent and decoupled from Google AdMob or Google Play Billing SDKs. It provides lock badges, click interceptors, and persistent timed pass tracking:

```kotlin
// 1. Initialize lock provider and attach 12-Hour Timed Pass manager
val lockProvider = DefaultItemLockProvider()
    .attachTimedPass(this)
    .lockFonts(R.font.font_pro1, R.font.font_pro2) // Lock VIP fonts
    .lockBackgrounds(R.drawable.bg_premium)        // Lock VIP wallpapers
    .lockTab(StudioTab.EFFECTS)                    // Lock premium tool tabs

// 2. Launch Text Studio with lock provider and ad callback
TextStudioDialog.Builder(this)
    .setConfig(currentTypography)
    .setFonts(fontList)
    .setLockProvider(lockProvider)
    
    // Intercept clicks on locked items
    .setOnLockedItemClicked { item, onUnlocked ->
        // Show your Rewarded Video Ad:
        AdMobRewardedAd.show(this@MainActivity) {
            // User completed ad -> Grant 12-Hour VIP Pass!
            lockProvider.grantTimedPass(hours = 12)
            onUnlocked() // Lock badges disappear and item is immediately selected!
        }
    }
    .setOnTypographyApplied { typography ->
        typography.applyTo(textView)
    }
    .show()
```

### Daily Usage Quota Manager (`UsageQuotaManager`)

```kotlin
val quotaManager = UsageQuotaManager(context, defaultDailyQuota = 3)

// Check if user has quota remaining
if (quotaManager.consumeEdit()) {
    // Edit allowed
} else {
    // Prompt rewarded ad to unlock 5 bonus edits or a 12-hour pass
    AdMobRewardedAd.show(activity) {
        quotaManager.addBonusEdits(5)
    }
}
```

---

## 4. Universal Theme Independence

DialogHub is designed with **zero theme dependencies** and **crash-proof inflation safety**:

- **Automatic Context Wrapping**: `DialogThemeHelper` inspects your host Activity theme. If your app uses `Theme.AppCompat`, `Theme.AppCompat.Light`, or standard Android themes without MaterialComponents, DialogHub automatically wraps the context with `Theme.MaterialComponents.DayNight.NoActionBar` to prevent XML inflation crashes.
- **Dynamic Accent Color Extraction**: DialogHub automatically detects and applies your app's primary brand color (`colorAccent`, `colorPrimary`, or custom accent passed via `.setAccentColor(...)`).
- **Solid Surfaces**: All dialog surfaces and cards use opaque, high-contrast surfaces to guarantee crisp legibility regardless of host activity backgrounds or theme variants.

---

## Requirements

- **Min SDK**: 25 (Android 7.1)
- **Compile/Target SDK**: 37
- **Language**: Kotlin 2.x
- **Build System**: Android Gradle Plugin (AGP) 9.x / Gradle 9.x
- **Java**: JDK 21

---

## License

```
Copyright 2026 Elyte Labs

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
