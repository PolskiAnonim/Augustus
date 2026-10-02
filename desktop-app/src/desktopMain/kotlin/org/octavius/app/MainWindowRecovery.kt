package org.octavius.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.DefaultWindowExceptionHandlerFactory
import androidx.compose.ui.window.WindowExceptionHandler
import androidx.compose.ui.window.WindowExceptionHandlerFactory
import org.octavius.error.showUnhandledError
import org.octavius.navigation.AppRouter
import java.awt.Window
import javax.swing.SwingUtilities
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource

/**
 * Buduje główne okno od nowa po wyjątku, który dotarł do handlera okna Compose.
 *
 * Domyślny handler pokazuje komunikat i zamyka okno, a z nim całą aplikację. Samo połknięcie
 * wyjątku nie wystarcza: porażka korutyny z kompozycji anuluje po drodze Recomposer okna i zostaje
 * okno, które już się nie przerysuje. Dlatego okno powstaje od nowa przez zmianę [generation] w `key`.
 * Stan aplikacji to przeżywa, bo żyje poza kompozycją: stosy ekranów w [AppRouter], formularze w
 * swoich handlerach. Ginie tylko stan z `remember`, np. przewinięcie listy.
 *
 * Okno, które pada zaraz po zbudowaniu, padałoby w kółko. Wtedy przed odbudową zdejmowany jest
 * ekran ze szczytu aktywnej zakładki, bo to on najpewniej rzuca przy kompozycji. Gdy zostaje sam
 * ekran główny zakładki, sprawę przejmuje domyślny handler i aplikacja się zamyka.
 */
@OptIn(ExperimentalComposeUiApi::class)
internal class MainWindowRecovery {
    /** Numer bieżącego okna. Jego zmiana w `key` buduje okno od nowa. */
    var generation by mutableIntStateOf(0)
        private set

    // Oba pola czyta i zapisuje tylko onWindowFailure, zawsze w wątku UI.
    private var builtAt = TimeSource.Monotonic.markNow()
    private var closing = false

    /**
     * Fabryka handlera dla okna bieżącej generacji. Compose tworzy handler na nowo przy każdej
     * aktualizacji okna, dlatego stan odbudowy trzyma ta klasa, a nie handler.
     */
    fun exceptionHandlerFactory(): WindowExceptionHandlerFactory {
        val windowGeneration = generation
        return WindowExceptionHandlerFactory { window ->
            WindowExceptionHandler { error ->
                // Handler bywa wołany w trakcie rysowania, gdzie nie wolno zmieniać stanu, a dla
                // korutyny dokończonej poza wątkiem UI także z innego wątku.
                SwingUtilities.invokeLater { onWindowFailure(windowGeneration, window, error) }
            }
        }
    }

    private fun onWindowFailure(windowGeneration: Int, window: Window, error: Throwable) {
        // Okno, które już padło, zwykle zgłasza jeszcze kilka wyjątków, zanim zniknie.
        if (windowGeneration != generation || closing) return

        if (builtAt.elapsedNow() < RAPID_FAILURE) {
            val activeStack = AppRouter.state.value?.let { it.tabStacks[it.activeTab] }.orEmpty()
            if (activeStack.size <= 1) {
                closing = true
                // Pokazuje komunikat, zamyka okno i rzuca dalej. Do logu wyjątek trafia przez
                // domyślny UncaughtExceptionHandler ustawiony w main().
                DefaultWindowExceptionHandlerFactory.exceptionHandler(window).onException(error)
                return
            }
            AppRouter.goBack()
        }

        showUnhandledError(error)
        builtAt = TimeSource.Monotonic.markNow()
        generation = windowGeneration + 1
    }

    private companion object {
        /** Okno, które padło szybciej po zbudowaniu, uznajemy za padające przy każdej kompozycji. */
        val RAPID_FAILURE = 5.seconds
    }
}
