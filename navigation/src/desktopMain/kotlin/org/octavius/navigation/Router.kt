package org.octavius.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Stan całej nawigacji w aplikacji.
 *
 * Przechowuje informacje o aktywnej zakładce oraz stosach ekranów dla każdej zakładki.
 * Używany przez `AppRouter` jako model danych dla stanu nawigacji.
 *
 * @param activeTab Aktualnie wyświetlana zakładka
 * @param tabStacks Mapa stosów ekranów - klucz to indeks zakładki, wartość to lista ekranów od najstarszego do najnowszego
 */
data class AppNavigationState(
    val activeTab: Tab,
    val tabStacks: Map<Tab, List<Screen>>
)

/**
 * Centralny singleton zarządzający stanem nawigacji w całej aplikacji.
 *
 * `AppRouter` jest pojedynczym źródłem prawdy (Single Source of Truth) dla nawigacji
 * i zapewnia globalny dostęp do funkcji nawigacyjnych z dowolnego miejsca w aplikacji.
 *
 * ## Architektura
 *
 * - **Zakładki**: Główne sekcje aplikacji
 * - **Stosy nawigacji**: Każda zakładka ma własny stos ekranów z możliwością nawigacji w przód/tył
 * - **Reaktywny stan**: UI obserwuje zmiany stanu przez `StateFlow`
 * - **Bezpieczeństwo**: Zabezpieczenia przed nieprawidłowym stanem (np. cofanie z ekranu głównego)
 *
 * ## Przykłady użycia
 *
 * ### Przełączanie zakładek
 * ```kotlin
 * AppRouter.switchToTab(1u) // Przejdź do zakładki o indexie  1
 * ```
 *
 * ### Nawigacja do nowego ekranu
 * ```kotlin
 * AppRouter.navigateTo(GameFormScreen.create(entityId = 123))
 * ```
 *
 * ### Powrót do poprzedniego ekranu
 * ```kotlin
 * AppRouter.goBack() // Bezpieczne - nie cofnie z ekranu głównego zakładki
 * ```
 *
 * ### Obserwowanie stanu w UI
 * ```kotlin
 * @Composable
 * fun MyComponent() {
 *     val navState by AppRouter.state.collectAsState()
 *     navState?.let { state ->
 *         Text("Aktywna zakładka: ${state.activeTab.options.title}")
 *     }
 * }
 * ```
 *
 * ## Inicjalizacja
 *
 * Router musi być zainicjalizowany przed użyciem, zazwyczaj w `MainScreen`:
 * ```kotlin
 * val tabs = listOf(AsianMediaTab(), GameTab(), SettingsTab())
 * AppRouter.initialize(tabs)
 * ```
 *
 * ## Bezpieczeństwo typów
 *
 * - `goBack()` nie pozwala opuścić ekranu głównego zakładki
 * - Wszystkie operacje są null-safe względem stanu inicjalizacji
 *
 * @see Screen Interfejs implementowany przez wszystkie ekrany
 * @see Tab Interfejs implementowany przez wszystkie zakładki
 * @see AppNavigationState Model danych stanu nawigacji
 */
object AppRouter {

    private lateinit var initialTabs: List<Tab>
    private lateinit var tabsById: Map<String, Tab>
    /**
     * Inicjalizuje router z podaną listą zakładek.
     *
     * Musi być wywołane przed jakimkolwiek użyciem routera, zazwyczaj w `MainScreen.init`.
     * Tworzy początkowy stan nawigacji z pierwszą zakładką jako aktywną i ekranami głównymi
     * dla każdej zakładki w ich stosach.
     *
     * @param tabs Lista wszystkich zakładek aplikacji w kolejności wyświetlania
     * @throws IllegalStateException jeśli lista zakładek jest pusta
     */
    fun initialize(tabs: List<Tab>) {
        require(tabs.isNotEmpty()) { "Lista zakładek nie może być pusta" }

        initialTabs = tabs
        tabsById = tabs.associateBy { it.id }

        val initialStacks = tabs.associateWith { listOf(it.getInitialScreen()) }

        _state.value = AppNavigationState(
            activeTab = tabs.first(),
            tabStacks = initialStacks
        )
    }

    private val _state = MutableStateFlow<AppNavigationState?>(null)
    
    /**
     * Reaktywny stan nawigacji obserwowany przez UI.
     *
     * Emituje `null` przed inicjalizacją, następnie `AppNavigationState` po każdej zmianie.
     * UI powinno obserwować ten stan przez `collectAsState()` w Composable.
     */
    val state = _state.asStateFlow()

    /**
     * Przełącza na zakładkę na podstawie jej obiektu.
     */
    fun switchToTab(tab: Tab) {
        _state.update { currentState ->
            currentState?.copy(activeTab = tab)
        }
    }

    /**
     * Przełącza na zakładkę na podstawie jej tekstowego ID.
     * Wygodne do użytku z zewnątrz (np. API, event bus).
     */
    fun switchToTab(tabId: String) {
        tabsById[tabId]?.let { tab ->
            switchToTab(tab)
        } ?: println("Warning: Nie znaleziono zakładki o ID: $tabId")
    }

    /**
     * Nawiguje do nowego ekranu na aktywnej zakładce.
     *
     * Dodaje ekran na szczyt stosu nawigacji aktywnej zakładki. Jeśli ekran tego samego
     * typu już jest na szczycie stosu, operacja jest ignorowana (zabezpieczenie przed duplikatami).
     *
     * @param screen Nowy ekran do wyświetlenia
     */
    fun navigateTo(screen: Screen) {
        _state.update { currentState ->
            val activeTab = currentState!!.activeTab
            val currentStack = currentState.tabStacks[activeTab]!!

            val newStack = currentStack + screen
            currentState.copy(
                tabStacks = currentState.tabStacks + (activeTab to newStack)
            )
        }
    }

    /**
     * Atomowo przełącza zakładkę i nawiguje do nowego ekranu, używając ID zakładki.
     *
     * Ta metoda jest preferowanym sposobem nawigacji z zewnętrznych źródeł (np. API).
     *
     * @param screen Nowy ekran do wyświetlenia.
     * @param tabId ID zakładki, na której ma się odbyć nawigacja.
     */
    fun navigateTo(screen: Screen, tabId: String) {
        val targetTab = tabsById[tabId]
        if (targetTab == null) {
            println("Warning: Próba nawigacji do nieistniejącej zakładki o ID: $tabId")
            return
        }
        _state.update { currentState ->
            val stateWithSwitchedTab = currentState!!.copy(activeTab = targetTab)
            val targetStack = stateWithSwitchedTab.tabStacks[targetTab]!!

            val newStack = targetStack + screen
            stateWithSwitchedTab.copy(
                tabStacks = stateWithSwitchedTab.tabStacks + (targetTab to newStack)
            )
        }
    }

    /**
     * Cofa do poprzedniego ekranu na aktywnej zakładce.
     *
     * Usuwa ostatni ekran ze stosu nawigacji aktywnej zakładki. Jeśli stos zawiera
     * tylko jeden ekran (ekran główny zakładki), operacja jest ignorowana.
     */
    fun goBack() {
        _state.update { currentState ->
            val activeTab = currentState!!.activeTab
            val currentStack = currentState.tabStacks[activeTab]!!

            if (currentStack.size <= 1) return@update currentState

            val newStack = currentStack.dropLast(1)
            currentState.copy(
                tabStacks = currentState.tabStacks + (activeTab to newStack)
            )
        }
    }
}