import com.android.build.api.dsl.ApplicationExtension
import com.mykim.convention.VersionConstants
import com.mykim.convention.configureBuildFeatures
import com.mykim.convention.configureKotlinAndroid
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.application")
                apply("org.jetbrains.kotlin.android")
                apply("kotlin-kapt")
            }

            extensions.configure<ApplicationExtension> {
                defaultConfig.targetSdk = VersionConstants.TARGET_SDK
                configureKotlinAndroid(this)
                configureBuildFeatures()
            }
        }
    }
}