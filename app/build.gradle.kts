plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.flatcode.autoimageslider"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.flatcode.autoimageslider"
        minSdk = 23
        targetSdk = 37
        versionCode = 2
        versionName = "1.0.1"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(project(":autoimageslider"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    //Layout
    implementation(libs.androidx.constraintlayout)
    implementation(libs.material)
    //Image
    implementation(libs.coil)
}