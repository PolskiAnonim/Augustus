package org.octavius.modules.asian.model

import kotlinx.serialization.Serializable
import org.octavius.domain.asian.SourceSite

/** Identyfikator serii na stronie źródłowej - `sid` z NovelUpdates, identyfikator z adresu na MangaUpdates. */
@Serializable
data class ExternalId(
    val site: SourceSite,
    val id: String
)
