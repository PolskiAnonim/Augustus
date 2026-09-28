package org.octavius.modules.asian.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.octavius.api.contract.ParsedData
import org.octavius.domain.asian.PublicationLanguage
import org.octavius.domain.asian.PublicationType

@Serializable
@SerialName("AsianPublicationData")
data class AsianPublicationData(
    override val source: String,
    val titles: List<String>,
    val type: PublicationType,
    val language: PublicationLanguage,
    val externalId: ExternalId? = null
) : ParsedData
