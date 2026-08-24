import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.kotlinxSerialization)
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
        namespace = "org.muc.mold.datakv"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.mold.minSdk.get().toInt()
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
        }
    }
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlin.logging)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.androidx.dataStore)
            implementation(libs.androidx.dataStore.preferences)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.atomicfu)
        }
        androidMain.dependencies {
            implementation(libs.slf4j.simple)
        }
        if (!isJitpack) {
            jvmMain.dependencies {
                implementation(libs.slf4j.simple)
            }
        }
    }
}
publishing {
    publications {
        create<MavenPublication>("release") {
            from(components["kotlin"])
            groupId = libs.versions.groupId.get() + ".Mold"
            artifactId = "dataKV"
            version = libs.versions.mold.version.get()
        }
    }
    repositories {
        maven { url = uri("F:/Android/WorkSpace/repo") }
    }
}