package org.example.project

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.example.project.components.BottomNavigation
import org.example.project.components.SavedCelebration
import org.example.project.db.DatabaseDriverFactory
import org.example.project.db.createWarrantyRepository
import org.example.project.screens.CalendarScreen
import org.example.project.screens.HomeScreen
import org.example.project.screens.ItemsScreen
import org.example.project.screens.PasteReceiptScreen
import org.example.project.screens.ProfileScreen
import org.example.project.theme.AppTheme
import org.example.project.theme.ThemeMode
import org.example.project.theme.WarrantyRadarTheme

enum class AppScreen { Home, Items, Calendar, Profile, PasteReceipt }

@Composable
fun App(driverFactory: DatabaseDriverFactory, startScreen: AppScreen = AppScreen.Home) {
    val repository = remember { createWarrantyRepository(driverFactory) }
    val warranties by repository.observeAll().collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()
    var screen by remember { mutableStateOf(startScreen) }
    var themeMode by remember { mutableStateOf(ThemeMode.System) }
    var celebrateSave by remember { mutableStateOf(false) }

    LaunchedEffect(celebrateSave) {
        if (celebrateSave) {
            delay(2400)
            celebrateSave = false
        }
    }

    WarrantyRadarTheme(themeMode) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (screen) {
                AppScreen.Home -> AppScaffold(AppScreen.Home, onNavigate = { screen = it }, onAddReceipt = { screen = AppScreen.PasteReceipt }) {
                    HomeScreen(warranties)
                }
                AppScreen.Items -> AppScaffold(AppScreen.Items, onNavigate = { screen = it }, onAddReceipt = { screen = AppScreen.PasteReceipt }) {
                    ItemsScreen(warranties)
                }
                AppScreen.Calendar -> AppScaffold(AppScreen.Calendar, onNavigate = { screen = it }, onAddReceipt = { screen = AppScreen.PasteReceipt }) {
                    CalendarScreen(warranties)
                }
                AppScreen.Profile -> AppScaffold(AppScreen.Profile, onNavigate = { screen = it }, onAddReceipt = { screen = AppScreen.PasteReceipt }) {
                    ProfileScreen(warranties, themeMode, onThemeModeChange = { themeMode = it })
                }
                AppScreen.PasteReceipt -> PasteReceiptScreen(
                    onSaved = { warranty ->
                        coroutineScope.launch { repository.insert(warranty) }
                        screen = AppScreen.Home
                        celebrateSave = true
                    },
                    onCancel = { screen = AppScreen.Home }
                )
            }
            SavedCelebration(
                visible = celebrateSave,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 118.dp)
            )
        }
    }
}

@Composable
private fun AppScaffold(
    current: AppScreen,
    onNavigate: (AppScreen) -> Unit,
    onAddReceipt: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    val colors = AppTheme.colors
    Box(modifier = Modifier.fillMaxSize().background(colors.backdrop)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f), content = content)
            BottomNavigation(current = current, onNavigate = onNavigate, onAddReceipt = onAddReceipt)
        }
    }
}
