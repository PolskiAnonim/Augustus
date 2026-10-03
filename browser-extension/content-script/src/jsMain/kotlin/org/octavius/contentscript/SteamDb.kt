package org.octavius.contentscript

import kotlinx.browser.window
import kotlinx.serialization.json.Json
import org.octavius.extension.util.chrome
import org.octavius.modules.games.parser.SteamDbParser
import org.w3c.dom.MessageEvent

/**
 * Podsłuch ignorowania gier na SteamDB. Przycisk „Ignore” (w nagłówku strony gry i w dymku nad grą na
 * listach) wysyła `postMessage` do wtyczki SteamDB, ta woła sklep Steama i odsyła wynik tym samym kanałem
 * jako `steamdb:extension-response`. Ten wynik widać też tutaj, więc reagujemy dopiero na udane
 * zignorowanie.
 *
 * Tak jak przy NovelUpdates przez `webRequest` się nie da: zapytanie do sklepu wychodzi z tła wtyczki
 * SteamDB, a zapytań innych wtyczek `webRequest` nie pokazuje.
 */
fun listenToSteamDbIgnores() {
    window.addEventListener("message", { event ->
        event as MessageEvent
        if (event.origin != window.location.origin) return@addEventListener
        val data: dynamic = event.data
        if (data == null || data.type != "steamdb:extension-response" || data.response?.success != true) return@addEventListener
        if (data.request?.contentScriptQuery != "StoreIgnore") return@addEventListener
        val appId = (data.request.appid as? Number)?.toInt() ?: return@addEventListener

        val ignore = SteamDbParser.ignoredGame(appId)
        if (ignore == null) {
            showToast("nie rozpoznałem gry $appId na tej stronie, nic nie wysłano", false)
            return@addEventListener
        }
        // Tło trzyma kolejkę na czas, gdy aplikacja nie działa, i odpowiada komunikatem przez showAugustusToast.
        val message: dynamic = js("({})")
        message.action = "mirrorSteamIgnore"
        message.ignore = Json.encodeToString(ignore)
        chrome.runtime.sendMessage(message) { chrome.runtime.lastError }
    })
}
