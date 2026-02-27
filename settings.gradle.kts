pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // İŞTE KOTLIN FORMATINDA JITPACK:
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "MotoWeather2"
include(":app")