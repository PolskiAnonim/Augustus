package org.octavius.modules.asian.model

import kotlinx.serialization.Serializable
import org.octavius.domain.asian.PublicationType

/**
 * Przeniesienie serii na jedną z list lektur na NovelUpdates, podsłuchane przez wtyczkę.
 *
 * [org] to język tak, jak zapisuje go NovelUpdates (cn, jp, kr, id...) - na enum mapuje aplikacja.
 * [type] jest tylko wtedy, gdy strona go pokazuje: strona serii tak, wiersz w series finderze nie.
 */
@Serializable
data class NovelUpdatesListMove(
    val sid: String,
    val listId: Int,
    val title: String,
    val org: String? = null,
    val type: PublicationType? = null
)

/**
 * [saved] mówi wtyczce, czy przeniesienie trafiło do bazy, żeby komunikat na stronie było widać
 * od razu - niejednoznaczne dopasowanie i nieznana lista niczego nie zapisują.
 */
@Serializable
data class NovelUpdatesListMoveResponse(
    val saved: Boolean,
    val message: String
)
