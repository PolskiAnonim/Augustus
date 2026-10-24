package org.octavius.form.component

/**
 * Dane z `initData` nie pasują do schematu formularza: wartość leży pod ścieżką, której nie wskazuje
 * żadna kontrolka, albo kontrolka jej nie przyjmuje (zły typ, wartość spoza opcji).
 *
 * @param path Ścieżka wartości w schemacie formularza, np. `"basic_info/status"`, a dla pola wierszy
 *   listy `"publications/status"`.
 */
class InitDataException(val path: String, reason: String) : RuntimeException("Dane początkowe pod '$path': $reason")
