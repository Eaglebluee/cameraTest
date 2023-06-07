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
            excludes += "META-INF/gradle/incremental.annotation.processors"
        }
    }
}

// import DownloadModels task
project.extra["ASSET_DIR"] = projectDir.toString() + "/src/main/assets"

// Download default models; if you wish to use your own models then
// place them in the "assets" directory and comment out this line.
apply(from = "download_models.gradle")
apply(from = "download_tasks.gradle")

dependencies {

    implementation(libs.bundles.android)
    implementation(libs.bundles.androidx)
    implementation(libs.bundles.junit)

    implementation(libs.hilt.android)
    implementation(libs.bundles.glide)
    kapt(libs.hilt.compiler)

    implementation(libs.bundles.mediapipe)
    implementation(libs.bundles.camerax)
    implementation(libs.bundles.retrofit)
    implementation(libs.lottie)

    implementation(project(":core-data"))
    implementation(project(":core-model"))
    implementation(project(":common-base"))
    implementation(project(":common-util"))
}