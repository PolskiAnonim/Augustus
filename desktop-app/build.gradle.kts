import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.shadow)
}

kotlin {
    jvm("desktop")

    sourceSets {
        val desktopMain by getting

        commonMain.dependencies {
            implementation(projects.uiCore)

            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.coroutines.core)
        }
        desktopMain.dependencies {
            implementation(libs.octavius.database.core)
            implementation(libs.octavius.database.flyway.integration)
            implementation(projects.featureContract)
            implementation(projects.formEngine)
            implementation(projects.reportEngine)
            implementation(projects.featureGames)
            implementation(projects.featureAsianMedia)
            implementation(projects.featureBooks)
            implementation(projects.featureSandbox)
            implementation(projects.apiServer)
            implementation(projects.apiContract)
            implementation(compose.desktop.currentOs)

            implementation(project.dependencies.platform(libs.koin.bom))

            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.slf4j)

            // Implementacja loggera
            implementation(libs.logback.classic)
        }
        desktopMain.resources.srcDir(layout.buildDirectory.dir("generated/translations"))
    }
}

compose.desktop {
    application {
        mainClass = "org.octavius.app.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Exe)
            includeAllModules = true
            packageName = "org.octavius"
            packageVersion = "1.0.0"
//            windows {
//                iconFile.set(project.file("icon.ico"))
//            }
        }
    }
}

tasks.named<ShadowJar>("shadowJar") {
    mergeServiceFiles {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }

    exclude("i18n/*.json")
    manifest {
        attributes["Main-Class"] = "org.octavius.app.MainKt"
    }
}