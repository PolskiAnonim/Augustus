package io.github.octaviusframework.form.control.type.selection

/**
 * Reprezentuje pojedynczą opcję wyboru dla kontrolek grupujących.
 *
 * @param T typ wartości przechowywanych w opcji
 * @property value rzeczywista wartość opcji używana w logice aplikacji
 * @property displayText tekst wyświetlany użytkownikowi w interfejsie
 */
data class SelectionOption<T>(
    val value: T,
    val displayText: String
)
