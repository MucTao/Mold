plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinxSerialization) apply false
}
tasks.withType<AbstractCompile>().configureEach {
    doFirst {
        val outputDir = destinationDirectory.get().asFile
        if (outputDir.exists()) {
            project.delete(destinationDirectory)
        }
        outputDir.mkdirs()
    }
}
tasks.register("publishAllCore") {
    group = "publishing"
    description = "Publish all core modules to Maven"

    subprojects.forEach { sub ->
        sub.plugins.withId("maven-publish") {
            dependsOn(sub.tasks.named("publishAllPublicationsToMavenRepository"))
        }
    }
}