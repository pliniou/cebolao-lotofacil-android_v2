@file:Suppress("UnstableApiUsage")

// Plugin management must be declared before any top-level `plugins` block.
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    // Toolchain resolver convention to enable auto-download of matching JDKs (e.g. Temurin 17)
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "CebolaoLotofacilGenerator"
include(":app")
