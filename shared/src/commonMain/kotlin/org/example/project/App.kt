package org.example.project

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
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
import org.example.project.components.ALL_CATEGORIES
import org.example.project.components.BottomNavigation
import org.example.project.components.SavedCelebration
import org.example.project.components.categoriesFor
import org.example.project.db.DatabaseDriverFactory
import org.example.project.db.createWarrantyRepository
import org.example.project.screens.CalendarScreen
import org.example.project.screens.HomeScreen
import org.example.project.screens.ItemsScreen
import org.example.project.screens.PasteReceiptScreen
import org.example.project.screens.ProfileScreen
import org.example.project.model.Warranty
import org.example.project.theme.AppTheme
import org.example.project.theme.ThemeMode
import org.example.project.theme.WarrantyRadarTheme

enum class AppScreen { Home, Items, Calendar, Profile, PasteReceipt }

@Composable
fun App(
    driverFactory: DatabaseDriverFactory,
    startScreen: AppScreen = AppScreen.Home,
    pendingEditWarrantyId: String? = null,
    onViewPhoto: (String) -> Unit = {},
    enabledReminderThresholds: Set<Int> = emptySet(),
    onReminderThresholdsChange: (Set<Int>) -> Unit = {},
    onExport: (List<Warranty>) -> Unit = {}
) {
    val repository = remember { createWarrantyRepository(driverFactory) }
    val warranties by repository.observeAll().collectAsState(initial = emptyList())
    val coroutineScope = rememberCoroutineScope()
    var screen by remember { mutableStateOf(startScreen) }
    var themeMode by remember { mutableStateOf(ThemeMode.System) }
    var celebrateSave by remember { mutableStateOf(false) }
    var editingWarranty by remember { mutableStateOf<Warranty?>(null) }
    var pendingId by remember { mutableStateOf(pendingEditWarrantyId) }
    val openEditor: (Warranty) -> Unit = { warranty ->
        editingWarranty = warranty
        screen = AppScreen.PasteReceipt
    }

    LaunchedEffect(celebrateSave) {
        if (celebrateSave) {
            delay(2400)
            celebrateSave = false
        }
    }

    LaunchedEffect(warranties, pendingId) {
        val id = pendingId ?: return@LaunchedEffect
        warranties.firstOrNull { it.id == id }?.let { warranty ->
            openEditor(warranty)
            pendingId = null
        }
    }

    WarrantyRadarTheme(themeMode) {
        Box(modifier = Modifier.fillMaxSize()) {
            when (screen) {
                AppScreen.Home -> AppScaffold(AppScreen.Home, onNavigate = { screen = it }, onAddReceipt = { editingWarranty = null; screen = AppScreen.PasteReceipt }) {
                    HomeScreen(warranties, onEditWarranty = openEditor)
                }
                AppScreen.Items -> AppScaffold(AppScreen.Items, onNavigate = { screen = it }, onAddReceipt = { editingWarranty = null; screen = AppScreen.PasteReceipt }) {
                    ItemsScreen(warranties, onEditWarranty = openEditor)
                }
                AppScreen.Calendar -> AppScaffold(AppScreen.Calendar, onNavigate = { screen = it }, onAddReceipt = { editingWarranty = null; screen = AppScreen.PasteReceipt }) {
                    CalendarScreen(warranties)
                }
                AppScreen.Profile -> AppScaffold(AppScreen.Profile, onNavigate = { screen = it }, onAddReceipt = { editingWarranty = null; screen = AppScreen.PasteReceipt }) {
                    ProfileScreen(
                        warranties,
                        themeMode,
                        onThemeModeChange = { themeMode = it },
                        enabledReminderThresholds = enabledReminderThresholds,
                        onReminderThresholdsChange = onReminderThresholdsChange,
                        onExport = { onExport(warranties) }
                    )
                }
                AppScreen.PasteReceipt -> PasteReceiptScreen(
                    existing = editingWarranty,
                    onSaved = { warranty ->
                        val wasEditing = editingWarranty != null
                        coroutineScope.launch {
                            if (wasEditing) repository.update(warranty) else repository.insert(warranty)
                        }
                        editingWarranty = null
                        screen = AppScreen.Home
                        celebrateSave = true
                    },
                    onCancel = { editingWarranty = null; screen = AppScreen.Home },
                    onDelete = editingWarranty?.let { warranty ->
                        {
                            coroutineScope.launch { repository.delete(warranty.id) }
                            editingWarranty = null
                            screen = AppScreen.Home
                        }
                    },
                    existingCategories = remember(warranties) { categoriesFor(warranties) - ALL_CATEGORIES },
                    onViewPhoto = onViewPhoto
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
    Box(modifier = Modifier.fillMaxSize().background(colors.screenSurface)) {
        Column(modifier = Modifier.fillMaxSize().navigationBarsPadding()) {
            Box(modifier = Modifier.weight(1f), content = content)
            BottomNavigation(current = current, onNavigate = onNavigate, onAddReceipt = onAddReceipt)
        }
    }
}
