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
        val desktopTest by getting

        commonMain.dependencies {
            implementation(projects.uiCore)

            api(libs.octavius.client)

            api(libs.octavius.i18n.core)

            implementation(libs.kotlinx.coroutines.core)

            implementation(libs.kotlinx.datetime)
        }

        desktopMain.dependencies {
            implementation(project.dependencies.platform(libs.koin.bom))

            implementation(libs.koin.core)
            implementation(libs.koin.compose)
        }

        desktopTest.dependencies {
            implementation(libs.junit.jupiter.api)
            runtimeOnly(libs.junit.jupiter.engine)
            runtimeOnly(libs.junit.platform.launcher)
            implementation(libs.mockk)
            implementation(libs.assertj.core)
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

octaviusI18n {
    generators {
        create("form") {
            targetPackage = "org.octavius.form.localization"
            objectName = "FormTr"
        }
    }
}
