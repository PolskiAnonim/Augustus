plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization) // Potrzebne do DTO
}

kotlin {
    js {
        browser()
        binaries.executable()
    }
    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(projects.browserExtension.chromeApi)
                implementation(projects.apiContract)
                implementation(projects.featureAsianMedia)
                implementation(projects.featureGames)
                // Ale potrzebuje wrapperów do komunikacji
                implementation(browserLibs.kotlin.browser)
                implementation(libs.kotlinx.serialization.json)
            }
        }
    }
}