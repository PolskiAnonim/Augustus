package org.octavius.domain.asian

import io.github.octaviusframework.annotation.PgEnumType

/** Strona, z której pochodzi identyfikator serii w `asian_media.title_external_ids`. */
@PgEnumType
enum class SourceSite {
    NovelUpdates,
    MangaUpdates
}
