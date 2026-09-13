package org.octavius.modules.asian.api

/**
 * Czy wszystkie litery w tekście są łacińskie.
 *
 * Sprawdzamy **skrypt** znaku, a nie jego zakres liczbowy, bo filtr pisany na zakresach ma dwie
 * wady naraz. Zawężony do ASCII wycina diakrytyki z tytułów najzupełniej łacińskich - "Fiancée",
 * "Ævintýri", "Đoạn", "Ben yanlız gelişirim". Pisany jako lista zakresów do odrzucenia przepuszcza
 * wszystko, czego autor nie wymienił: parsery we wtyczce odrzucają kanę, kanji, hangul, cyrylicę,
 * tajski i ormiański, ale grekę mają zakomentowaną - a na tytułach jednej serii wypadły też
 * arabski, dewanagari i gruziński.
 *
 * Stoi po stronie aplikacji, a nie wtyczki, bo `Character.UnicodeScript` jest z JDK i w Kotlin/JS
 * nie ma odpowiednika.
 *
 * Cyfry, spacje i interpunkcja nie są literami i nie wpływają na wynik.
 */
fun String.isLatinScript(): Boolean =
    codePoints().filter(Character::isLetter)
        .allMatch { Character.UnicodeScript.of(it) == Character.UnicodeScript.LATIN }
