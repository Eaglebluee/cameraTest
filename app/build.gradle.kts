import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    id("mykim.android.application")
    id("mykim.android.hilt")
    id("org.jetbrains.kotlin.android")
}

android {

    defaultConfig {
        applicationId = "com.example.cameratest"
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.bundles.android)
    implementation(libs.bundles.androidx)
    implementation(libs.bundles.junit)

    implementation(libs.hilt.android)
    implementation(libs.bundles.glide)
    kapt(libs.hilt.compiler)

    implementation(project(":core-data"))
    implementation(project(":core-model"))
    implementation(project(":common-base"))
    implementation(project(":common-util"))
}