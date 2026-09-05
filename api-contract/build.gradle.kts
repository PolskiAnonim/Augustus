plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    // Definiujemy targety dla obu platform
    jvm("desktop")
    js {
        browser()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(libs.ktor.server.core)
                implementation(projects.navigation)
            }
        }
        val desktopMain by getting
        val jsMain by getting
    }
}