plugins {
    id("mykim.android.library")
    id("mykim.android.hilt")
}

android {
    namespace = "com.example.common_util"
}

dependencies {

    implementation(libs.bundles.android)
    implementation(libs.bundles.androidx)
    implementation(libs.bundles.junit)
    implementation(libs.bundles.glide)

    implementation(project(":core-model"))
}