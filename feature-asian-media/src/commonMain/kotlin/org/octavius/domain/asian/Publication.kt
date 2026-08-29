package org.octavius.domain.asian

import io.github.octaviusframework.annotation.PgEnumType
import org.octavius.domain.EnumWithFormatter
import org.octavius.localization.Tr

@PgEnumType
enum class PublicationStatus : EnumWithFormatter<PublicationStatus> {
    NotReading,
    Reading,
    Completed,
    PlanToRead;

    override fun toDisplayString(): String {
        return when (this) {
            NotReading -> Tr.AsianMedia.ReadingStatus.notReading()
            Reading -> Tr.AsianMedia.ReadingStatus.reading()
            Completed -> Tr.AsianMedia.ReadingStatus.completed()
            PlanToRead -> Tr.AsianMedia.ReadingStatus.toRead()
        }
    }
}

@PgEnumType
enum class PublicationLanguage : EnumWithFormatter<PublicationLanguage> {
    Korean,
    Chinese,
    Japanese;

    override fun toDisplayString(): String {
        return when (this) {
            Korean -> Tr.AsianMedia.PublicationLanguage.korean()
            Chinese -> Tr.AsianMedia.PublicationLanguage.chinese()
            Japanese -> Tr.AsianMedia.PublicationLanguage.japanese()
        }
    }
}

@PgEnumType
enum class PublicationType : EnumWithFormatter<PublicationType> {
    Manga,
    LightNovel,
    WebNovel,
    PublishedNovel,
    Webtoon,
    Manhwa,
    Manhua;

    override fun toDisplayString(): String {
        return when (this) {
            Manga -> Tr.AsianMedia.PublicationType.manga()
            LightNovel -> Tr.AsianMedia.PublicationType.lightNovel()
            WebNovel -> Tr.AsianMedia.PublicationType.webNovel()
            PublishedNovel -> Tr.AsianMedia.PublicationType.publishedNovel()
            Webtoon -> Tr.AsianMedia.PublicationType.webtoon()
            Manhwa ->Tr.AsianMedia.PublicationType.manhwa()
            Manhua -> Tr.AsianMedia.PublicationType.manhua()
        }
    }
}
