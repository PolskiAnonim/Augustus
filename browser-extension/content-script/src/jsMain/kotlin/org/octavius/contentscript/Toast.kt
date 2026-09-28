package org.octavius.contentscript

import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLElement

private const val CONTAINER_ID = "augustus-toasts"

/**
 * Komunikat w rogu strony, bez przechodzenia do popupu. Przy wywalaniu całej strony do kosza
 * przychodzi ich kilkadziesiąt naraz, więc układają się jeden nad drugim; te o niezapisanych
 * przeniesieniach wiszą dłużej, bo to na nie trzeba zareagować.
 */
fun showToast(text: String, saved: Boolean) {
    val container = document.getElementById(CONTAINER_ID) as? HTMLElement
        ?: (document.createElement("div") as HTMLElement).also {
            it.id = CONTAINER_ID
            it.style.cssText = "position:fixed;right:16px;bottom:16px;z-index:2147483647;" +
                "display:flex;flex-direction:column;gap:6px;max-width:420px"
            document.body?.appendChild(it)
        }

    val toast = document.createElement("div") as HTMLElement
    toast.textContent = "Augustus: $text"
    toast.style.cssText = "padding:8px 12px;border-radius:6px;font:13px/1.4 sans-serif;color:#fff;" +
        "box-shadow:0 2px 6px rgba(0,0,0,.3);background:" + (if (saved) "#2e7d32" else "#c62828")
    container.appendChild(toast)

    window.setTimeout({ toast.remove() }, if (saved) 3000 else 10000)
}
