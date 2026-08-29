package org.octavius.form.component

import io.github.octaviusframework.client.OctaviusClient
import io.github.octaviusframework.client.DataResult
import io.github.octaviusframework.identifier.CaseConvention
import io.github.octaviusframework.identifier.CaseConverter
import org.octavius.dialog.ErrorDialogConfig
import org.octavius.dialog.GlobalDialogManager

// --- Główne klasy DSL ---

data class FieldMapping(val controlName: String, val dbColumn: String)
data class ExistenceFlag(val controlName: String, val checkColumn: String)

interface MappingContainer {
    val relations: MutableList<RelationMapping>

    fun map(controlName: String, dbColumn: String? = null) {
        val finalDbColumn = dbColumn
            ?: CaseConverter.convert(controlName, CaseConvention.CAMEL_CASE, CaseConvention.SNAKE_CASE_LOWER)
        relations.add(SimpleMapping(FieldMapping(controlName, finalDbColumn)))
    }

    fun mapOneToOne(block: OneToOneMappingBuilder.() -> Unit) {
        val builder = OneToOneMappingBuilder().apply(block)
        builder.validate()
        relations.add(OneToOneMapping(builder.existenceFlag, builder.fromTable, builder.joinCondition, builder.relations))
    }

    fun mapRelatedList(controlName: String, block: RelatedDataMappingBuilder.() -> Unit) {
        val builder = RelatedDataMappingBuilder().apply(block)
        builder.validate()
        relations.add(RelatedDataMapping(controlName, builder))
    }
}

abstract class BaseTableMappingBuilder : MappingContainer {
    protected lateinit var tableName: String
    protected lateinit var tableAlias: String

    val fromTable: String
        get() = if (::tableName.isInitialized) "$tableName $tableAlias" else ""

    override val relations = mutableListOf<RelationMapping>()

    fun from(tableName: String, alias: String) {
        this.tableName = tableName
        this.tableAlias = alias
    }

    protected fun validateBase() {
        check(::tableName.isInitialized) { "Klauzula `from` jest wymagana w mapowaniu." }
    }
}

class OneToOneMappingBuilder : BaseTableMappingBuilder() {
    lateinit var joinCondition: String
    var existenceFlag: ExistenceFlag? = null
        private set

    fun existenceFlag(controlName: String, checkColumn: String) {
        this.existenceFlag = ExistenceFlag(controlName, checkColumn)
    }

    fun on(condition: String) {
        this.joinCondition = condition
    }

    fun validate() {
        validateBase()
        check(::joinCondition.isInitialized) { "Klauzula `on` jest wymagana w mapowaniu jeden-do-jednego." }
    }
}

class RelatedDataMappingBuilder : BaseTableMappingBuilder() {
    private lateinit var linkColumn: String
    private var parentColumn: String = "@id"

    fun linkedBy(foreignKeyColumn: String, parentColumn: String = "@id") {
        this.linkColumn = foreignKeyColumn
        this.parentColumn = parentColumn
    }

    fun validate() {
        validateBase()
        check(::linkColumn.isInitialized) { "Klauzula `linkedBy` jest wymagana w mapowaniu listy powiązanych danych." }
    }

    fun buildWhereClause(): String {
        return "$linkColumn = $parentColumn"
    }
}

// --- Reprezentacje wewnętrzne ---

sealed class RelationMapping
data class SimpleMapping(val mapping: FieldMapping) : RelationMapping()
data class OneToOneMapping(val existenceFlag: ExistenceFlag?, val table: String, val on: String, val relations: List<RelationMapping>) : RelationMapping()
data class RelatedDataMapping(val controlName: String, val builder: RelatedDataMappingBuilder) : RelationMapping()


// --- Helper do budowy SQL ---

class QueryScope(
    val isTopLevel: Boolean
) {
    val fieldExpressions = mutableListOf<String>()
    val joins = mutableListOf<String>()

    fun processRelations(relations: List<RelationMapping>) {
        relations.forEach { rel ->
            when (rel) {
                is SimpleMapping -> {
                    if (isTopLevel) {
                        fieldExpressions.add("${rel.mapping.dbColumn} AS ${rel.mapping.controlName}")
                    } else {
                        fieldExpressions.add("'${rel.mapping.controlName}', ${rel.mapping.dbColumn}")
                    }
                }
                is OneToOneMapping -> {
                    joins.add("LEFT JOIN ${rel.table} ON ${rel.on}")
                    rel.existenceFlag?.let { flag ->
                        val expr = "CASE WHEN ${flag.checkColumn} IS NOT NULL THEN TRUE ELSE FALSE END"
                        if (isTopLevel) {
                            fieldExpressions.add("$expr AS ${flag.controlName}")
                        } else {
                            fieldExpressions.add("'${flag.controlName}', $expr")
                        }
                    }
                    processRelations(rel.relations)
                }
                is RelatedDataMapping -> {
                    val builder = rel.builder
                    val subScope = QueryScope(isTopLevel = false)
                    subScope.processRelations(builder.relations)

                    val mapEntries = subScope.fieldExpressions.joinToString(",\n                            ")
                    val subJoins = subScope.joins.joinToString(" ")
                    
                    // ROW(klucz, wartość, ...) czyta się jako Map<String, Any?>, a tablica takich
                    // rekordów jako List<Map<String, Any?>> - i wartości idą przez pełny łańcuch
                    // konwerterów, więc enum wraca enumem, a nie tekstem.
                    val subquery = """
                        ARRAY(
                            SELECT ROW(
                                $mapEntries
                            )
                            FROM ${builder.fromTable} $subJoins
                            WHERE ${builder.buildWhereClause()}
                        )::record[]
                    """.trimIndent()

                    if (isTopLevel) {
                        fieldExpressions.add("($subquery) AS ${rel.controlName}")
                    } else {
                        fieldExpressions.add("'${rel.controlName}', ($subquery)")
                    }
                }
            }
        }
    }
}


// --- Główny Builder ---

class DataLoaderBuilder(private val db: OctaviusClient) : MappingContainer {
    private lateinit var mainTableName: String
    private lateinit var mainTableAlias: String
    private var idColumn: String = "id"
    override val relations = mutableListOf<RelationMapping>()

    private val mainTable: String
    get() = if (::mainTableName.isInitialized) "$mainTableName $mainTableAlias" else ""

    fun from(tableName: String, alias: String) {
        this.mainTableName = tableName
        this.mainTableAlias = alias
    }

    fun idColumn(name: String) {
        this.idColumn = name
    }

    fun execute(id: Any?): Map<String, Any?> {
        check(::mainTableName.isInitialized) { "Main table must be defined using from()" }
        if (id == null) return emptyMap()

        val topScope = QueryScope(isTopLevel = true)
        topScope.processRelations(relations)

        if (topScope.fieldExpressions.isEmpty()) return emptyMap()

        val joinsStr = topScope.joins.joinToString(" ")

        // SKŁADAMY OSTATECZNE, JEDNO ZAPYTANIE
        val query = db.select(*topScope.fieldExpressions.toTypedArray())
            .from("$mainTable $joinsStr".trim())
            .where("$mainTableAlias.$idColumn = @id")
            .asResult().fetchObject<Map<String, Any?>>("id" to id)

        return when (query) {
            is DataResult.Success -> query.value ?: emptyMap()
            is DataResult.Failure -> {
                GlobalDialogManager.show(ErrorDialogConfig(query.error))
                emptyMap()
            }
        }
    }
}