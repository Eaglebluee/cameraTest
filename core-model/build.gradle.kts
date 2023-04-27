plugins {
    id("mykim.android.library")
    id("mykim.android.hilt")
}

android {
    namespace = "com.example.core_model"
}

dependencies {
    implementation(libs.bundles.android)
    implementation(libs.bundles.androidx)
    implementation(libs.bundles.junit)
    implementation(libs.bundles.retrofit)
}