plugins {
    alias(libs.plugins.octaviusI18n)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    jvm("desktop")
    // Dla wtyczki: DTO API we wspólnym kodzie, odczyt stron SteamDB w jsMain
    js {
        browser()
    }

    sourceSets {
        val desktopMain by getting
        val jsMain by getting

        commonMain.dependencies {
            implementation(projects.uiCore)
            implementation(projects.navigation)
            implementation(composeLibs.components.resources)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)

            implementation(libs.octavius.pg.model)
        }

        desktopMain.dependencies {
            implementation(projects.formEngine)
            implementation(projects.reportEngine)
            implementation(projects.featureContract)
            implementation(projects.apiContract)

            implementation(project.dependencies.platform(libs.koin.bom))

            implementation(libs.koin.core)
            implementation(libs.koin.compose)

            implementation(libs.ktor.server.core)
        }
    }
}

octaviusI18n {
    generators {
        create("games") {
            targetPackage = "org.octavius.modules.games.localization"
            objectName = "GamesTr"
        }
    }
}
