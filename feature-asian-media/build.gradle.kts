plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    // Definiujemy, że ten moduł jest tylko dla desktopa
    jvm("desktop")
    js {
        browser()
    }

    sourceSets {
        val desktopMain by getting
        val jsMain by getting

        commonMain.dependencies {
            implementation(projects.uiCore)
            implementation(composeLibs.components.resources)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)

            implementation(projects.apiContract)
            implementation(libs.octavius.annotations)
        }

        desktopMain.dependencies {
            implementation(projects.formEngine)
            implementation(projects.reportEngine)
            implementation(projects.featureContract)
            implementation(project.dependencies.platform(libs.koin.bom))

            implementation(libs.koin.core)
            implementation(libs.koin.compose)

            implementation(libs.ktor.server.core)
        }

        jsMain.dependencies {
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.js) // Implementacja dla środowiska JS
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
        }
    }
}
