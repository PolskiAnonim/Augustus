package org.octavius.modules.games.parser

import kotlinx.browser.document
import org.octavius.modules.games.model.SteamIgnore
import org.w3c.dom.Element
import org.w3c.dom.asList

/**
 * Odczyt gier ze stron SteamDB.
 */
object SteamDbParser {

    /**
     * Opisuje grę, którą użytkownik właśnie zignorował, z tego, co widać na bieżącej stronie: nagłówka
     * strony gry albo kafelka czy wiersza na liście (wyprzedaże, wykresy, wyszukiwarka). `null`, gdy tej
     * gry na stronie nie ma.
     */
    fun ignoredGame(appId: Int): SteamIgnore? {
        val name = appPageName(appId) ?: listName(appId) ?: return null
        return SteamIgnore(appId, name)
    }

    // Na stronie gry linki do jej podstron (/app/<id>/charts/ itd.) mają podpisy w rodzaju "Charts",
    // więc nazwę bierzemy tylko z nagłówka.
    private fun appPageName(appId: Int): String? =
        document.querySelector(".scope-app[data-appid='$appId'] h1[itemprop=name]")?.textContent?.normalized()

    // Kafelek to sam link z nazwą, a w wierszu tabeli pierwszy link do gry jest obrazkiem bez tekstu,
    // dlatego pierwszy link z tekstem.
    private fun listName(appId: Int): String? =
        document.querySelectorAll(".app[data-appid='$appId']").asList().firstNotNullOfOrNull { node ->
            val element = node as Element
            val links = listOf(element).filter { it.tagName == "A" } +
                element.querySelectorAll("a[href^='/app/$appId/']").asList().map { it as Element }
            links.firstNotNullOfOrNull { it.textContent?.normalized() }
        }

    private fun String.normalized(): String? = replace(Regex("\\s+"), " ").trim().ifEmpty { null }
}
