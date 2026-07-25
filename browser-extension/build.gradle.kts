
evaluationDependsOn(":browser-extension:popup")

plugins {
    base
}

// 2. Główny task do składania wtyczki
tasks.register("assembleBrowserExtension") {
    group = "Augustus Extension"
    description = "Assembles the final browser extension into build/extension"

    // Zależności: najpierw budujemy JS, potem mergujemy tłumaczenia
    dependsOn(
        project(":browser-extension:popup").tasks.named("jsBrowserProductionWebpack"),
        project(":browser-extension:content-script").tasks.named("jsBrowserProductionWebpack"),
    )

    val extensionDir = project.layout.buildDirectory.dir("extension")
    outputs.dir(extensionDir)

    doLast {
        println(">>>>>> STARTING assembleBrowserExtension <<<<<<")

        // 1. Czyszczenie
        project.delete(extensionDir) // Użyjmy project.delete dla pewności
        extensionDir.get().asFile.mkdirs()
        println("  -> Cleaned destination directory: ${extensionDir.get().asFile.path}")

        // Użyjmy wbudowanego w Gradle API do kopiowania - jest bardziej zwięzłe
        copy {
            from(project(":browser-extension:popup").layout.buildDirectory.dir("kotlin-webpack/js/productionExecutable"))
            into(extensionDir)
            println("  -> Copied files from popup JS build")
        }
        copy {
            from(project(":browser-extension:content-script").layout.buildDirectory.dir("kotlin-webpack/js/productionExecutable")) {
                include("content-script.js")
            }
            into(extensionDir)
            println("  -> Copied files from content-script JS build")
        }
        copy {
            from(project(":browser-extension:popup").file("src/jsMain/resources"))
            into(extensionDir)
            println("  -> Copied static resources")
        }

        println(">>>>>> FINISHED. Extension is ready in: ${extensionDir.get().asFile.path} <<<<<<")
    }
}