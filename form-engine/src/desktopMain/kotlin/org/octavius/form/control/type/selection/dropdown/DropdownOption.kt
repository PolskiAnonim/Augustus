package org.octavius.form.control.type.selection.dropdown

/**
 * Reprezentuje pojedynczą opcję w liście rozwijanej (dropdown).
 *
 * Enkapsuluje wartość rzeczywistą wraz z tekstem wyświetlanym użytkownikowi.
 * Pozwala na oddzielenie logiki biznesowej (wartość) od prezentacji (tekst).
 *
 * @param T typ wartości przechowywanych w opcji
 * @property value rzeczywista wartość opcji używana w logice aplikacji
 * @property displayText tekst wyświetlany użytkownikowi w interfejsie
 * @property payload Dodatkowe dane, które przyszły razem z opcją (np. cały wiersz z bazy). Przy
 *   wyborze trafiają do `ActionContext.payload`, więc akcja może z nich wypełnić inne kontrolki.
 *   Przy wczytywaniu formularza ich nie ma - wartość przychodzi wtedy z zapisu, nie z menu.
 */
data class DropdownOption<T>(
    val value: T,
    val displayText: String,
    val payload: Any? = null
)