package org.octavius.extension.util

import kotlin.js.Promise


// Definiujemy globalny obiekt `chrome`
external val chrome: Chrome

// Definiujemy strukturę API, której potrzebujemy
external interface Chrome {
    val runtime: Runtime
    val tabs: Tabs
    val webRequest: WebRequest
    val storage: Storage
}

external interface Runtime {
    val onMessage: OnMessage
    /** Z content scriptu trafia do tła wtyczki. */
    fun sendMessage(message: dynamic, responseCallback: (dynamic) -> Unit)
    /** Ustawiane na czas callbacku, gdy wywołanie się nie udało - np. karta, do której pisaliśmy, jest już zamknięta. */
    val lastError: dynamic
}

external interface OnMessage {
    fun addListener(listener: (message: dynamic, sender: MessageSender, sendResponse: (dynamic) -> Unit) -> Boolean)
}

external interface MessageSender {
    val tab: Tab?
    val id: String?
}

external interface Tabs {
    fun query(queryInfo: QueryInfo, callback: (Array<Tab>) -> Unit)
    fun sendMessage(tabId: Int, message: dynamic, responseCallback: (dynamic) -> Unit)
}

external interface Tab {
    val id: Int?
    val url: String?
    val title: String?
}

// Dostępne tylko w tle wtyczki (service worker), nie w content scripcie.
external interface WebRequest {
    val onCompleted: WebRequestEvent
}

external interface WebRequestEvent {
    /** [filter] to obiekt `{ urls: [...] }` z wzorcami adresów. */
    fun addListener(callback: (details: WebRequestDetails) -> Unit, filter: dynamic)
}

external interface WebRequestDetails {
    val url: String
    val tabId: Int
    val statusCode: Int
}

external interface Storage {
    val local: StorageArea
}

external interface StorageArea {
    fun get(key: String): Promise<dynamic>
    fun set(items: dynamic): Promise<Unit>
}

// To jest klasa pomocnicza, a nie deklaracja `external`,
// bo tworzymy jej instancje w naszym kodzie.
data class QueryInfo(
    val active: Boolean,
    val currentWindow: Boolean
)
