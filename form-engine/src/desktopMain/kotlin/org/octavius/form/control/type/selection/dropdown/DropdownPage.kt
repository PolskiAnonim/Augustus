package org.octavius.form.control.type.selection.dropdown

/**
 * Jedna porcja opcji dla kontrolki dropdown ładującej dane stronami.
 *
 * Nie ma tu liczby stron ani liczby wyników i nie powinno być: żeby je podać, źródło musiałoby
 * policzyć cały zbiór, czyli wykonać drugie zapytanie obok tego, które i tak leci - a przy
 * doczytywaniu w trakcie przewijania nikt tej liczby nie ogląda. Wystarczy wiedzieć, czy jest
 * co doczytywać, a to widać po nadmiarowym wierszu pobranym ponad rozmiar strony.
 *
 * @param T typ wartości opcji.
 * @property options Opcje tej strony, już przycięte do rozmiaru strony.
 * @property hasMore Czy za tą stroną jest coś jeszcze.
 */
data class DropdownPage<T>(
    val options: List<DropdownOption<T>>,
    val hasMore: Boolean
)
