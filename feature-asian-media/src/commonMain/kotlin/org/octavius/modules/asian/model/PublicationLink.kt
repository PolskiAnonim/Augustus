package org.octavius.modules.asian.model

import kotlinx.serialization.Serializable
import org.octavius.domain.asian.PublicationType

/**
 * Seria ze strony to tytuł, który już jest w bazie - np. manga do powieści z NovelUpdates. Podpięcie
 * zapisuje identyfikator serii, dodaje publikację tego typu, jeśli tytuł jej nie ma, i dopisuje tytuły,
 * których w bazie brakuje.
 */
@Serializable
data class PublicationLinkRequest(
    val titleId: Int,
    val titles: List<String>,
    val type: PublicationType,
    val externalId: ExternalId? = null
)

@Serializable
data class PublicationLinkResponse(
    val success: Boolean,
    val message: String
)

@Serializable
data class TitlesAppendRequest(
    val titleId: Int,
    val titles: List<String>
)

@Serializable
data class TitlesAppendResponse(
    val added: Int
)

@Serializable
data class TitleOpenRequest(
    val titleId: Int
)
