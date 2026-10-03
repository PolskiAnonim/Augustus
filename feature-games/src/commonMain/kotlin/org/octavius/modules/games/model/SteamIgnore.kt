package org.octavius.modules.games.model

import kotlinx.serialization.Serializable

/**
 * Gra zignorowana na SteamDB, podsłuchana przez wtyczkę. [name] to nazwa tak, jak pokazuje ją SteamDB,
 * razem ze znakami ™, ® i emoji - zdejmuje je aplikacja.
 */
@Serializable
data class SteamIgnore(
    val appId: Int,
    val name: String
)

/**
 * Pola jak w odpowiedzi na przeniesienie na NovelUpdates, bo tło wtyczki obsługuje obie jednakowo:
 * [saved] = false to czerwony komunikat, na który trzeba zareagować.
 */
@Serializable
data class SteamIgnoreResponse(
    val saved: Boolean,
    val message: String
)
