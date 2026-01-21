@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        // Use official repositories; remove gradlePluginPortal because AGP 9 resolves correctly via Google
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

// plugins {
//    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
// }

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "CebolaoLotofacilGenerator"
include(":app")
