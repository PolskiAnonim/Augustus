package org.octavius.feature.books.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import org.octavius.feature.books.home.ui.BooksHomeScreen
import org.octavius.feature.books.localization.BooksTr
import org.octavius.navigation.Screen
import org.octavius.navigation.Tab
import org.octavius.navigation.TabOptions

class BooksTab : Tab {

    override val id: String = "books"

    override val options: TabOptions
        @Composable get() = TabOptions(
            title = BooksTr.tab(),
            icon = rememberVectorPainter(Icons.AutoMirrored.Filled.LibraryBooks)
        )

    override fun getInitialScreen(): Screen {
        return BooksHomeScreen.create()
    }
}