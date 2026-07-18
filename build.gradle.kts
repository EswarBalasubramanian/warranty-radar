plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.sqldelight) apply false
    id("com.google.gms.google-services") version "4.5.0" apply false
}

tasks.register("androidBuild") {
    group = "android"
    description = "Builds the Android debug APK."
    dependsOn(":androidApp:assembleDebug")
}

tasks.register("androidRun") {
    group = "android"
    description = "Builds and installs the Android debug app on a connected device or emulator."
    dependsOn(":androidApp:installDebug")
}
