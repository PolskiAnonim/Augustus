plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

/**
 * Moduł nawigacji aplikacji.
 *
 * Świadomie NIE zależy od `ui-core` ani od silników (`form-engine`, `report-engine`) - zależność
 * idzie wyłącznie w drugą stronę. Dzięki temu silniki można wydzielić do osobnego repozytorium
 * razem z ich częściami wspólnymi, a nawigacja zostaje po stronie aplikacji.
 */
kotlin {
    jvm("desktop")
    js {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            // Screen.Content() jest @Composable, a TabOptions wystawia Painter - obie zależności
            // są częścią publicznego API modułu, stąd api() zamiast implementation()
            api(composeLibs.runtime)
            api(composeLibs.ui)

            // StateFlow w AppRouter, SharedFlow w NavigationEventBus
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}
