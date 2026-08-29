package org.octavius.app

import io.github.octaviusframework.driver.identifier.CaseConverter
import io.github.octaviusframework.identifier.CaseConvention
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonUnquotedLiteral
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.modules.SerializersModule
import org.octavius.domain.game.GameStatus
import java.math.BigDecimal
import kotlin.enums.EnumEntries

/**
 * `Json` używany do czytania i zapisywania ładunków `dynamic_dto`.
 *
 * Kolumna `dynamic_dto` niesie payload jako `jsonb`, więc typy, których JSON nie zna sam z siebie,
 * potrzebują serializatora. Stara biblioteka budowała taki moduł sama - z rejestru typów - i dlatego
 * DTO dashboardów są oznaczone `@Contextual`. Nowa niczego nie dokłada do `Json`, więc lista jest tutaj
 * i **rozszerza się razem z DTO**: enum, który trafi do payloadu, a nie ma tu wpisu, wywróci się dopiero
 * przy odczycie wiersza.
 */
val octaviusDynamicJson: Json = Json {
    serializersModule = SerializersModule {
        contextual(BigDecimal::class, BigDecimalAsNumberSerializer)
        contextual(GameStatus::class, PgEnumSerializer("GameStatus", GameStatus.entries))
    }
}

/**
 * Enum po stronie Kotlina jest `PascalCase`, a w bazie `SNAKE_CASE_UPPER` - i to samo rozejście
 * dotyczy etykiety zapisanej w payloadzie, bo buduje ją SQL z kolumny typu enum.
 *
 * Konwencje są te same, co domyślne w `@PgEnumType`, więc enum anotowany bez argumentów jest tu spójny
 * z tym, jak czyta go sterownik.
 */
class PgEnumSerializer<E : Enum<E>>(
    enumName: String,
    private val entries: EnumEntries<E>,
    private val pgConvention: CaseConvention = CaseConvention.SNAKE_CASE_UPPER,
    private val kotlinConvention: CaseConvention = CaseConvention.PASCAL_CASE
) : KSerializer<E> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("org.octavius.app.PgEnumSerializer.$enumName", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: E) {
        encoder.encodeString(CaseConverter.convert(value.name, kotlinConvention, pgConvention))
    }

    override fun deserialize(decoder: Decoder): E {
        val label = decoder.decodeString()
        val kotlinName = CaseConverter.convert(label, pgConvention, kotlinConvention)
        return entries.firstOrNull { it.name == kotlinName }
            ?: throw SerializationException("Unknown $descriptor name: $label (mapped from $kotlinName)")
    }
}

/**
 * Zapisuje `BigDecimal` jako gołą liczbę, a nie tekst - `jsonb` ma wtedy liczbę, po której da się
 * sortować i liczyć, zamiast napisu, który tylko wygląda jak liczba. Precyzja zostaje nietknięta.
 */
object BigDecimalAsNumberSerializer : KSerializer<BigDecimal> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("org.octavius.app.BigDecimalAsNumberSerializer", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: BigDecimal) {
        (encoder as JsonEncoder).encodeJsonElement(JsonUnquotedLiteral(value.toPlainString()))
    }

    override fun deserialize(decoder: Decoder): BigDecimal {
        val element = (decoder as JsonDecoder).decodeJsonElement()
        if (element is JsonNull) throw SerializationException("Unexpected null value for non-nullable BigDecimal")
        val content = element.jsonPrimitive.content
        return try {
            BigDecimal(content)
        } catch (e: NumberFormatException) {
            throw SerializationException("Invalid BigDecimal format: $content", e)
        }
    }
}
