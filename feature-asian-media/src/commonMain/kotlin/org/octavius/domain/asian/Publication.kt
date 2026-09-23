package org.octavius.domain.asian

import io.github.octaviusframework.annotation.PgEnumType
import org.octavius.domain.EnumWithFormatter
import org.octavius.modules.asian.localization.AsianMediaTr

@PgEnumType
enum class PublicationStatus : EnumWithFormatter<PublicationStatus> {
    NotReading,
    Reading,
    Completed,
    PlanToRead;

    override fun toDisplayString(): String {
        return when (this) {
            NotReading -> AsianMediaTr.ReadingStatus.notReading()
            Reading -> AsianMediaTr.ReadingStatus.reading()
            Completed -> AsianMediaTr.ReadingStatus.completed()
            PlanToRead -> AsianMediaTr.ReadingStatus.toRead()
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
            Korean -> AsianMediaTr.PublicationLanguage.korean()
            Chinese -> AsianMediaTr.PublicationLanguage.chinese()
            Japanese -> AsianMediaTr.PublicationLanguage.japanese()
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
            Manga -> AsianMediaTr.PublicationType.manga()
            LightNovel -> AsianMediaTr.PublicationType.lightNovel()
            WebNovel -> AsianMediaTr.PublicationType.webNovel()
            PublishedNovel -> AsianMediaTr.PublicationType.publishedNovel()
            Webtoon -> AsianMediaTr.PublicationType.webtoon()
            Manhwa ->AsianMediaTr.PublicationType.manhwa()
            Manhua -> AsianMediaTr.PublicationType.manhua()
        }
    }
}
