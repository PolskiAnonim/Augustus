rootProject.name = "Augustus"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        mavenLocal()
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("browserLibs") {
            from(files("browser-extension/gradle/libs.versions.toml"))
        }
        create("composeLibs") {
            from(files("ui-core/gradle/libs.versions.toml"))
        }
    }
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        mavenLocal()
    }
}

include(
    ":desktop-app",
    ":form-engine",
    ":report-engine",
    ":api-server",
    ":ui-core",
    ":api-contract",
    ":feature-contract",
    // Features
    ":feature-asian-media",
    ":feature-games",
    ":feature-books",
    ":feature-sandbox",
    // Browser extension
    ":browser-extension",
    ":browser-extension:content-script",
    ":browser-extension:popup",
    ":browser-extension:chrome-api",
)