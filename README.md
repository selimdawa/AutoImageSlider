# Auto Image Slider

<p align="center">
  <b>A lightweight and customizable Android image slider library with auto scrolling, smooth animations, and easy integration.</b>
</p>

<p align="center">
  AutoImageSlider helps you create beautiful image carousels with automatic sliding, smooth transitions, and flexible customization options.
</p>

<p align="center">
 <a><img alt="Min SDK" src="https://img.shields.io/badge/Min SDK-23-020290?logo=android&logoColor=white"/></a>
 <a><img alt="Target SDK" src="https://img.shields.io/badge/Target SDK-37-0EB265?logo=android&logoColor=0EB265"/></a>
 <a href="https://kotlinlang.org"><img alt="Kotlin" src="https://img.shields.io/badge/Kotlin-2.4.0-blue?logo=kotlin&logoColor=white"/></a>
</p>
---

## ✨ Features

- 🖼️ Automatic image sliding
- 🌊 Smooth transition animations
- 🎨 Customizable appearance
- ⚡ Lightweight and optimized
- 📱 Easy Android integration
- 🔄 Supports dynamic image lists
- 🎯 Custom slide duration
- 🔘 Indicator support
- 🛠 Kotlin friendly
- 📦 Simple dependency setup

---

## 📦 Installation

### Gradle

Add the dependency:

```gradle
dependencies {
    implementation("io.selimdawa:auto-image-slider:VERSION")
}
```

---

### Version Catalog (libs.versions.toml)

Add the version:

```toml
[versions]
autoImageSlider = "VERSION"
```

Add the library:

```toml
[libraries]
auto-image-slider = { module = "io.selimdawa:auto-image-slider", version.ref = "autoImageSlider" }
```

Then use it in your module `build.gradle.kts`:

```kotlin
dependencies {
    implementation(libs.auto.image.slider)
}
```

---

## 🚀 Usage

### Add AutoImageSlider to your layout

Add the `AutoImageSlider` view to your XML layout:

```xml
<com.selimdawa.autoimageslider.AutoImageSlider
    android:id="@+id/autoImageSlider"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"/>
```

Initialize the slider in your Activity or Fragment:

```kotlin
val autoImageSlider = findViewById<AutoImageSlider>(R.id.autoImageSlider)
```

---

## 🖼️ Set Images

Add your images to the slider:

```kotlin
autoImageSlider.setImages(
    listOf(
        R.drawable.image_one,
        R.drawable.image_two,
        R.drawable.image_three
    )
)
```

---

## ⚙️ Configure Slider

Customize the slider behavior:

```kotlin
autoImageSlider.startAutoSlide()
```

Stop automatic sliding:

```kotlin
autoImageSlider.stopAutoSlide()
```

---

## 🎨 Customization

AutoImageSlider provides flexible options to match your application design.

### Change Slide Duration

```kotlin
autoImageSlider.slideDuration = 3000L
```

---

### Enable Auto Sliding

```kotlin
autoImageSlider.isAutoSlideEnabled = true
```

---

### Disable Auto Sliding

```kotlin
autoImageSlider.isAutoSlideEnabled = false
```

---

### Change Animation Duration

```kotlin
autoImageSlider.animationDuration = 500L
```

---

### Customize Indicators

Change indicator appearance:

```kotlin
autoImageSlider.showIndicators = true
```

Hide indicators:

```kotlin
autoImageSlider.showIndicators = false
```

---

## 📱 Requirements

| Requirement | Version |
|---|---|
| Minimum SDK | API 23+ |
| AndroidX | Supported |
| Kotlin | Supported |

---

## 🤝 Contributing

Contributions are welcome!

1. Fork this repository
2. Create your feature branch

```bash
git checkout -b feature/new-feature
```

3. Commit your changes

```bash
git commit -m "Add new feature"
```

4. Push your branch

```bash
git push origin feature/new-feature
```

5. Open a Pull Request

---

## 🐛 Issues

If you find any issues, please provide:

- Android version
- Device information
- Error logs
- Steps to reproduce

---

## 📄 License

```
Copyright (c) 2026 Selim Dawa

Licensed under the Apache License, Version 2.0
```

See the [LICENSE](LICENSE) file for more information.

---

## ⭐ Support

If you like this library, consider giving it a ⭐ on GitHub.

Your support helps improve and maintain this project.
