pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        // Repositório do Chaquopy
        maven { url = uri("https://chaquo.com/maven") }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Repositório do Chaquopy
        maven { url = uri("https://chaquo.com/maven") }
        // Repositório para MPAndroidChart
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "AndroidAppTemplate"
include(":app")
 