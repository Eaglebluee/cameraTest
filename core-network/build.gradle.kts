plugins {
    id("mykim.android.library")
    id("mykim.android.hilt")
}

android {
    namespace = "com.example.core_network"
}

dependencies {
    implementation(libs.bundles.android)
    implementation(libs.bundles.androidx)
    implementation(libs.bundles.junit)
    implementation(libs.hilt.android)
    implementation(libs.bundles.retrofit)
    kapt(libs.hilt.compiler)

    implementation(project(":core-model"))
}