package org.octavius.modules.asian.model

import kotlinx.serialization.Serializable
import org.octavius.domain.asian.PublicationStatus
import org.octavius.domain.asian.PublicationType

@Serializable
data class PublicationCheckResponse(
    val found: Boolean,
    val titleId: Int? = null,
    val matchedTitle: String? = null,
    /**
     * Znaleziony po identyfikatorze serii, więc to na pewno ta sama seria. Bez tego [found] znaczy tylko,
     * że któryś tytuł jest podobny.
     */
    val byExternalId: Boolean = false,
    val publications: List<PublicationSummary> = emptyList()
)

@Serializable
data class PublicationSummary(
    val type: PublicationType,
    val status: PublicationStatus
)

@Serializable
data class PublicationCheckRequest(
    val titles: List<String>,
    val externalId: ExternalId? = null
)
