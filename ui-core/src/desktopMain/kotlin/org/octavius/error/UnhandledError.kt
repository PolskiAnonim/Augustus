package org.octavius.error

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import org.octavius.dialog.ErrorDialogConfig
import org.octavius.dialog.GlobalDialogManager

private val logger = KotlinLogging.logger {}

/**
 * Obsługuje wyjątek, którego nikt po drodze nie obsłużył: zapisuje go ze stack trace'em do logu
 * (konsola i `logs/octavius.log`) i pokazuje w globalnym dialogu błędu.
 *
 * To jedyne miejsce, do którego trafiają takie wyjątki, niezależnie od drogi: korutyna z
 * [rememberSupervisedCoroutineScope], akcja raportu, handler okna albo wątek w tle.
 */
fun showUnhandledError(error: Throwable) {
    logger.error(error) { "Unhandled exception" }
    GlobalDialogManager.show(ErrorDialogConfig(error))
}

/**
 * [rememberCoroutineScope], w którym wyjątek z korutyny kończy się [showUnhandledError], a nie
 * zamknięciem okna.
 *
 * Zwykły scope z kompozycji nie nadaje się do odpalania kodu, który może rzucić. Jego job, job
 * efektów Recomposera i job sceny okna to zwykłe `Job`, więc porażka jednej korutyny anuluje cały
 * łańcuch, a z nim Recomposer okna. Okno już się wtedy nie przerysuje, niezależnie od tego, co zrobi
 * handler wyjątków. Samego joba nie da się podmienić, bo `rememberCoroutineScope` odrzuca kontekst
 * z `Job`. Dlatego tutaj pod jobem kompozycji wisi [SupervisorJob]: porażka dziecka nie idzie w górę,
 * tylko do [CoroutineExceptionHandler]. Cykl życia zostaje ten sam, bo opuszczenie kompozycji
 * dalej anuluje wszystkie korutyny.
 */
@Composable
fun rememberSupervisedCoroutineScope(): CoroutineScope {
    val compositionScope = rememberCoroutineScope()
    return remember(compositionScope) {
        val context = compositionScope.coroutineContext
        CoroutineScope(
            context + SupervisorJob(context[Job]) + CoroutineExceptionHandler { _, error -> showUnhandledError(error) }
        )
    }
}
