
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeMultiplatform)
    id("maven-publish")
}


kotlin {
    jvm()
    iosArm64()
    iosSimulatorArm64()
    android {
        namespace = "com.muc.network"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.mold.minSdk.get().toInt()
    }
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlin.logging)
            implementation(libs.atomicfu)
            implementation(libs.kotlinx.coroutines.core)

            implementation(libs.ktor.client.core)
//            implementation(libs.ktor.client.content.negotiation)
//            implementation(libs.ktor.client.logging)
//            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.compose.ui)
        }
        androidMain.dependencies {
            implementation(libs.slf4j.simple)
            implementation(libs.ktor.client.okhttp )
        }
        jvmMain.dependencies {
            implementation(libs.slf4j.simple)
            implementation(libs.ktor.client.cio)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("release") {
            from(components["kotlin"])
            groupId = libs.versions.groupId.get()
            artifactId = "dataRequest"
            version = libs.versions.mold.version.get()
        }
    }
    repositories {
        maven { url = uri("F:/Android/WorkSpace/repo") }
    }
}