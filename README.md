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
 <a href="https://kotlinlang.org"><img alt="Kotlin" src="https://img.shields.io/badge/Kotlin-2.4.20-blue?logo=kotlin&logoColor=white"/></a>
 <a href="https://www.apache.org/licenses/LICENSE-2.0"><img alt="License" src="https://img.shields.io/badge/License-Apache_2.0-CC9900?logo=apache&logoColor=white"/></a>
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

```kotlin
dependencies {
    implementation("io.github.selimdawa:auto-image-slider:1.0.1")
}
```

---

### Version Catalog (libs.versions.toml)

Add the version:

```toml
[versions]
autoImageSlider = "1.0.1"
```

Add the library:

```toml
[libraries]
auto-image-slider = { module = "io.github.selimdawa:auto-image-slider", version.ref = "autoImageSlider" }
```

Then use it in your module `build.gradle.kts`:

```kotlin
dependencies {
    implementation(libs.auto.image.slider)
}
```

---

## 🚀 Usage

### Add SliderView to your layout

Add the `SliderView` to your XML layout:

```xml
<io.selimdawa.autoimageslider.SliderView
    android:id="@+id/imageSlider"
    android:layout_width="match_parent"
    android:layout_height="300dp"
    app:sliderAnimationDuration="600"
    app:sliderAutoCycleDirection="back_and_forth"
    app:sliderAutoCycleEnabled="true"
    app:sliderIndicatorAnimationDuration="600"
    app:sliderIndicatorGravity="center_horizontal|bottom"
    app:sliderIndicatorMargin="15dp"
    app:sliderIndicatorOrientation="horizontal"
    app:sliderIndicatorPadding="3dp"
    app:sliderIndicatorRadius="2dp"
    app:sliderIndicatorSelectedColor="@color/white"
    app:sliderIndicatorUnselectedColor="@color/gray"
    app:sliderScrollTimeInSec="1"
    app:sliderStartAutoCycle="true" />
```

Initialize the slider in your Activity or Fragment:

```kotlin
val sliderView = findViewById<SliderView>(R.id.imageSlider)
```

---

## 🖼️ Set Adapter

Create your custom adapter extending `SliderViewAdapter` and set it:

```kotlin
val adapter = SliderAdapterExample(this)
sliderView.setSliderAdapter(adapter)
```

---

## ⚙️ Configure Slider

Customize the slider behavior:

```kotlin
sliderView.setIndicatorAnimation(IndicatorAnimationType.WORM)
sliderView.setSliderTransformAnimation(SliderAnimations.SIMPLE)
sliderView.autoCycleDirection = SliderView.AUTO_CYCLE_DIRECTION_BACK_AND_FORTH
sliderView.scrollTimeInSec = 3
sliderView.isAutoCycle = true

// Start auto cycle
sliderView.startAutoCycle()
```

Stop automatic sliding:

```kotlin
sliderView.stopAutoCycle()
```

---

## 🎨 Customization

`SliderView` provides flexible options to match your application design.

### Change Scroll Duration

```kotlin
sliderView.scrollTimeInSec = 3
```

---

### Enable Auto Cycle

```kotlin
sliderView.isAutoCycle = true
```

---

### Disable Auto Cycle

```kotlin
sliderView.isAutoCycle = false
```

---

### Change Animation Duration

```kotlin
sliderView.sliderAnimationDuration = 500
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
