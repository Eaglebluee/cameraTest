plugins {
    id("mykim.android.library")
    id("mykim.android.hilt")
}

android {
    namespace = "com.example.core_data"
}

dependencies {

    implementation(libs.bundles.android)
    implementation(libs.bundles.androidx)
    implementation(libs.bundles.junit)

    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    implementation(project(":core-model"))
    implementation(project(":core-network"))
}