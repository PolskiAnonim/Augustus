plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    // Definiujemy, że ten moduł jest tylko dla desktopa
    jvm("desktop")

    sourceSets {
        val desktopMain by getting

        commonMain.dependencies {
            // Zależności Compose
            implementation(projects.uiCore)
            implementation(projects.navigation)
            implementation(composeLibs.components.resources)

            implementation(libs.kotlinx.coroutines.core)
        }

        desktopMain.dependencies {

            implementation(projects.formEngine)
            implementation(projects.reportEngine)
            implementation(projects.apiContract)
            implementation(projects.featureContract)
            implementation(project.dependencies.platform(libs.koin.bom))

            implementation(libs.koin.core)
            implementation(libs.koin.compose)
        }
    }
}
