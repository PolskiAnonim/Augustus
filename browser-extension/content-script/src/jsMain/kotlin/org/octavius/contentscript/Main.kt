package org.octavius.contentscript

import kotlinx.browser.window
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.octavius.api.contract.ParseResult
import org.octavius.api.contract.ParsedData
import org.octavius.api.contract.Parser
import org.octavius.extension.util.chrome
import org.octavius.modules.asian.parser.MangaUpdatesParser
import org.octavius.modules.asian.parser.NovelUpdatesParser

@OptIn(DelicateCoroutinesApi::class)
fun main() {
    println("Augustus Content Script (z logiką) załadowany!")

    val availableParsers: List<Parser<*>> = listOf(
        MangaUpdatesParser,
        NovelUpdatesParser
    )

    chrome.runtime.onMessage.addListener { message, _, sendResponse ->
        if (message.action == "parsePage") {
            GlobalScope.launch { // Parsowanie może być asynchroniczne
                val url = window.location.href
                val parser = availableParsers.firstOrNull { it.canParse(url) }

                if (parser != null) {
                    val parsedData = parser.parse()
                    if (parsedData != null) {
                        @Suppress("UNCHECKED_CAST")
                        val dataJsonString = (parser as Parser<ParsedData>).serialize(parsedData)

                        val resultContainer = ParseResult(
                            moduleId = parser.moduleId,
                            dataJson = dataJsonString
                        )
                        val finalJsonString = Json.encodeToString(resultContainer)
                        // Tworzymy obiekt odpowiedzi JS, który zostanie wysłany.
                        val responsePayload = js("({})")
                        responsePayload.success = true
                        responsePayload.data = finalJsonString // JSON jako string w polu 'data'

                        sendResponse(responsePayload)
                    } else {
                        sendResponse(js("{ success: false, error: 'Parsing failed' }"))
                    }
                } else {
                    sendResponse(js("{ success: false, error: 'No suitable parser found' }"))
                }
            }
        }
        true
    }
}