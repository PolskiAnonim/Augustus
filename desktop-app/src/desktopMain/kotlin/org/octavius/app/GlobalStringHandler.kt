package org.octavius.app

import io.github.octaviusframework.driver.converter.parameter.mapper.ParameterConverter
import io.github.octaviusframework.driver.converter.parameter.mapper.SerializationContext
import kotlin.reflect.KClass

/**
 * Czyści każdy tekst wychodzący do bazy z apostrofów i cudzysłowów typograficznych.
 *
 * Rejestrowany na typeManagerze, więc łapie zarówno parametry pojedyncze, jak i teksty zagnieżdżone
 * w kompozytach i tablicach - te ostatnie i tak przechodzą przez ten sam łańcuch konwerterów.
 * Zwraca `String`, więc typ deklarowany zostaje `text` i `getDefaultTypeName` nie ma czego nadpisywać.
 */
object CleanStringParameterConverter : ParameterConverter<String> {
    override val supportedClass: KClass<String> = String::class

    override fun convert(source: String, expectedOid: Int, context: SerializationContext): Any = source.clean()
}

/**
 * Simple function cleaning String from typographic apostrophes and quotes
 */
fun String.clean(): String {
    return buildString(this.length) {
        for (c in this@clean) {
            when (c) {
                '‘', '’' -> append('\'')
                '“', '”' -> append('"')
                else    -> append(c)
            }
        }
    }
}
