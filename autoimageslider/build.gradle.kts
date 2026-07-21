plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.vanniktech)
}

android {
    namespace = "io.selimdawa.autoimageslider"
    compileSdk = 37

    defaultConfig {
        minSdk = 23

        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

mavenPublishing {

    coordinates(
        groupId = "io.github.selimdawa", artifactId = "auto-image-slider", version = "1.0.1"
    )

    publishToMavenCentral(automaticRelease = true)

    signAllPublications()

    pom {
        name.set("Auto Image Slider")
        description.set("A lightweight and customizable Android image slider library with automatic sliding, smooth animations, indicators, and flexible image display options.")

        url.set("https://github.com/selimdawa/AutoImageSlider")

        licenses {
            license {
                name.set("Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }

        developers {
            developer {
                id.set("selimdawa")
                name.set("Selim Dawa")
                email.set("selimdawa@gmail.com")
            }
        }

        scm {
            url.set("https://github.com/selimdawa/AutoImageSlider")
            connection.set("scm:git:https://github.com/selimdawa/AutoImageSlider.git")
            developerConnection.set("scm:git:ssh://git@github.com:selimdawa/AutoImageSlider.git")
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.viewpager2)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.coil)
}