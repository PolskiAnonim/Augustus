package org.octavius.report.filter.data.type

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import org.octavius.report.DateTimeFilterDataType
import org.octavius.report.FilterMode
import org.octavius.report.NullHandling
import org.octavius.report.filter.data.FilterData
import org.octavius.util.DateTimeAdapter
import kotlin.reflect.KClass

data class DateTimeFilterData<T : Any>(
    val kClass: KClass<T>,
    private val adapter: DateTimeAdapter<T>, // Adapter potrzebny do serializacji/deserializacji wartości
    override val mode: FilterMode = FilterMode.Single,
    override val nullHandling: NullHandling = NullHandling.Ignore,
    val filterType: DateTimeFilterDataType = DateTimeFilterDataType.Equals,
    val value: T? = null,
    val minValue: T? = null,
    val maxValue: T? = null
) : FilterData {

    override fun isActive(): Boolean {
        return value != null || minValue != null || maxValue != null || nullHandling != NullHandling.Ignore
    }

    override fun serialize(): JsonObject {
        return buildJsonObject {
            put("filterType", filterType.name)
            put("value", value?.let { adapter.serialize(it) } )
            put("minValue", minValue?.let { adapter.serialize(it) } )
            put("maxValue", maxValue?.let { adapter.serialize(it) } )
            put("nullHandling", nullHandling.name)
            put("mode", mode.name)
        }
    }

    companion object {
        fun <T : Any> deserialize(data: JsonObject, kClass: KClass<T>, adapter: DateTimeAdapter<T>): DateTimeFilterData<T> {
            return DateTimeFilterData(
                kClass = kClass,
                adapter = adapter,
                filterType = data["filterType"]!!.jsonPrimitive.content.let { DateTimeFilterDataType.valueOf(it) },
                value = data["value"]?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotEmpty() }?.let { adapter.deserialize(it) }, // Użyj adaptera do parsowania ze stringa
                minValue = data["minValue"]?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotEmpty() }?.let { adapter.deserialize(it) },
                maxValue = data["maxValue"]?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotEmpty() }?.let { adapter.deserialize(it) },
                nullHandling = data["nullHandling"]!!.jsonPrimitive.content.let { NullHandling.valueOf(it) },
                mode = data["mode"]!!.jsonPrimitive.content.let { FilterMode.valueOf(it) }
            )
        }
    }

    override fun withMode(newMode: FilterMode): FilterData {
        return this.copy(mode = newMode)
    }

    override fun withNullHandling(newNullHandling: NullHandling): FilterData {
        return this.copy(nullHandling = newNullHandling)
    }
}