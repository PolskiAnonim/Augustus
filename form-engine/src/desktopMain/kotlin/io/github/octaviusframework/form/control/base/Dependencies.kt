package io.github.octaviusframework.form.control.base

/**
 * Definuje zależność między kontrolkami.
 *
 * @param controlPath ścieżka do kontrolki (względna lub bezwzględna)
 * @param value wartość kontrolki która powoduje aktywację zależności
 * @param dependencyType typ zależności (widoczność lub wymagalność)
 * @param comparisonType sposób porównania wartości
 */
data class ControlDependency<T>(
    val controlPath: String,
    val value: T,
    val dependencyType: DependencyType,
    val comparisonType: ComparisonType
)

/**
 * Typ zależności między kontrolkami.
 */
enum class DependencyType {
    /** Kontrolka jest widoczna/niewidoczna w zależności od wartości innej kontrolki */
    Visible,

    /** Kontrolka jest wymagana/niewymagana w zależności od wartości innej kontrolki */
    Required,
}

/**
 * Sposób porównania wartości w zależnościach.
 */
enum class ComparisonType {
    /** Wartość musi być jedną z podanych wartości */
    OneOf,

    /** Wartość musi być równa podanej wartości */
    Equals,

    /** Wartość musi być różna od podanej wartości */
    NotEquals
}