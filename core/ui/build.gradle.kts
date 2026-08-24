import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeMultiplatform)
    id("maven-publish")
}


kotlin {
    val isJitpack = System.getenv("JITPACK") == "true"
    if (!isJitpack) {
        jvm()
        iosArm64()
        iosSimulatorArm64()
    }
    android {
        namespace = "org.muc.mold.ui"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.mold.minSdk.get().toInt()
        androidResources {
            enable = true
        }
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
        withHostTest {
            isIncludeAndroidResources = true
        }
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }
    sourceSets {
        commonMain {
            resources.srcDir("src/commonMain/composeResources")
        }
        commonMain.dependencies {
            implementation(libs.kotlin.logging)
            implementation(libs.atomicfu)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
            implementation(libs.compose.ui)
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.material3)
            implementation(libs.compose.material.icons)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.materialKolor)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)
            implementation(libs.ktor.client.core)
            api(libs.androidx.navigation3.ui)
            implementation(libs.androidx.compose.material3.adaptive.navigation3)
            api(libs.androidx.lifecycle.viewModel.navigation3)
            api(libs.reorderable)
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.compose.uiTooling)
            implementation(libs.slf4j.simple)
        }
        if (!isJitpack) {
            jvmMain.dependencies {
                implementation(libs.slf4j.simple)
                implementation(libs.ktor.client.cio)
            }
            iosMain.dependencies {
                implementation(libs.slf4j.simple)
                implementation(libs.ktor.client.darwin)
            }
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("release") {
            from(components["kotlin"])
            groupId = libs.versions.groupId.get()
            artifactId = "mold-ui"
            version = libs.versions.mold.version.get()
        }
    }
    repositories {
        maven { url = uri("F:/Android/WorkSpace/repo") }
    }
}