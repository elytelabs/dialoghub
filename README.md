# DialogHub for Android

[![Release](https://jitpack.io/v/elytelabs/dialoghub.svg)](https://jitpack.io/#elytelabs/dialoghub)

DialogHub is an Android library that allows users to customize their app's UI dynamically by selecting backgrounds, fonts, and colors\.

## Installation

### Step 1. Add the JitPack repository to your root `build.gradle`

```gradle
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

### Step 2. Add the dependency

```gradle
dependencies {
    implementation 'com.github.elytelabs:dialoghub:Tag'
}
```

Replace `Tag` with the latest version from the Releases page.

## Usage

### Background Selector

```kotlin
btnImageSelector.setOnClickListener {
    val selectorDialog = ImageSelectorDialog(this)
    selectorDialog.setBackgroundsList(backgrounds = backgrounds)
    selectorDialog.setImageSelectedListener(object : ImageSelectorDialog.ImagePickerListener {
        override fun onImageSelected(imageResource: Int) {
            rootLayout.setBackgroundResource(imageResource)
        }

        override fun onColorSelected(color: Int) {
            rootLayout.setBackgroundColor(color)
        }
    })
    selectorDialog.showImageSelectionDialog()
}
```

### Font Selector

```kotlin
btnFontSelector.setOnClickListener {
    val styleDialog = FontStyleDialog(this)
    styleDialog.setFontsList(fonts = fonts)
    styleDialog.setFontSelectedListener(object : FontStyleDialog.FontPickerListener {
        override fun onFontSelected(font: Int) {
            textView.typeface = ResourcesCompat.getFont(this@MainActivity, font)
        }
    })
    styleDialog.showFontSelectionDialog()
}
```

### Color Picker

```kotlin
btnColorSelector.setOnClickListener {
    val colorPickerDialog = ColorPickerDialog(this)
    colorPickerDialog.setColorSelectedListener(object : ColorPickerDialog.ColorPickerListener {
        override fun onColorSelected(color: Int) {
            rootLayout.setBackgroundColor(color)
        }
    })
    colorPickerDialog.showColorPickerDialog()
}
```

## Features

- **Dynamic Backgrounds**: Choose from a list of predefined drawable resources or colors.
- **Font Styling**: Select custom fonts from the app's resources.
- **Color Picker**: Set the background color dynamically.

## Requirements

- Android Studio with Kotlin support.
- Minimum SDK version: 24 (Android 7.0 Nougat).

## Contributing

Contributions are welcome! Feel free to submit pull requests or report issues.

## License

This project is licensed under the Apache License 2.0. For more details, see [Apache 2.0 License](https://www.apache.org/licenses/LICENSE-2.0).
