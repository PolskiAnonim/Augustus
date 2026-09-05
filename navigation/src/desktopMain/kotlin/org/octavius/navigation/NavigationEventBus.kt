package org.octavius.navigation

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Kontrakt dla zdarzeń nawigacyjnych, które mogą być wywołane z zewnątrz (np. przez API).
 */
sealed interface NavigationEvent {
    /**
     * Zdarzenie żądania nawigacji. Może oznaczać:
     * 1. Przełączenie na zakładkę (jeśli `screenId` jest `null`).
     * 2. Przełączenie na zakładkę ORAZ nawigację do konkretnego ekranu na tej zakładce
     *    (jeśli `screenId` jest podany).
     *
     * @property tabId Id zakładki docelowej.
     * @property screenId Opcjonalny, unikalny identyfikator ekranu docelowego.
     * @property payload Opcjonalne dane do przekazania do ekranu docelowego.
     */
    data class Navigate(
        val tabId: String,
        val screenId: String? = null,
        val payload: Map<String, Any>? = null
    ) : NavigationEvent
}

/**
 * Globalna, bezpieczna wątkowo magistrala zdarzeń do komunikacji między-modułowej.
 *
 * Umożliwia dowolnej części aplikacji zgłoszenie żądania zmiany nawigacji bez
 * posiadania bezpośredniej referencji do kontrolera nawigacji.
 */
object NavigationEventBus {
    private val _events = MutableSharedFlow<NavigationEvent>()
    /**
     * Publiczny strumień zdarzeń [NavigationEvent], który można subskrybować,
     * aby reagować na żądania nawigacyjne.
     */
    val events = _events.asSharedFlow()

    /**
     * Publikuje nowe zdarzenie [NavigationEvent] w magistrali.
     * @param event Zdarzenie nawigacyjne do wyemitowania.
     */
    suspend fun post(event: NavigationEvent) {
        _events.emit(event)
    }
}