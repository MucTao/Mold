import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeCompiler)
    id("maven-publish")
}


kotlin {
    jvm()
    iosArm64()
    iosSimulatorArm64()
    android {
        namespace = "com.muc.eventbus"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.mold.minSdk.get().toInt()
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
        }
    }
    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.kotlin.logging)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.atomicfu)
        }
        androidMain.dependencies {
            implementation(libs.slf4j.simple)
        }
        jvmMain.dependencies {
            implementation(libs.slf4j.simple)
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("release") {
            from(components["kotlin"])
            groupId = libs.versions.groupId.get()
            artifactId = "eventBus"
            version = libs.versions.mold.version.get()
        }
    }
    repositories {
        maven { url = uri("F:/Android/WorkSpace/repo") }
    }
}