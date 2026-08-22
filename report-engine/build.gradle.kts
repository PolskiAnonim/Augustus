plugins {
    alias(libs.plugins.octaviusI18n)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    // Definiujemy, że ten moduł jest tylko dla desktopa
    jvm("desktop")

    sourceSets {
        val desktopMain by getting

        commonMain.dependencies {
            implementation(projects.uiCore)

            api(libs.octavius.database.api)

            api(libs.octavius.i18n.core)

            implementation(libs.kotlinx.coroutines.core)

            implementation(libs.kotlinx.serialization.json)

            implementation(libs.kotlinx.datetime)
        }

        desktopMain.dependencies {
            implementation(project.dependencies.platform(libs.koin.bom))

            implementation(libs.koin.core)
            implementation(libs.koin.compose)
        }
    }
}

octaviusI18n {
    generators {
        create("report") {
            sourceProject = project(":report-engine")
            targetPackage = "org.octavius.report.localization"
            objectName = "ReportTr"
        }
    }
}
