package org.example.project.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.components.ALL_CATEGORIES
import org.example.project.components.CategoryFilters
import org.example.project.components.PurchaseLibrary
import org.example.project.components.SearchField
import org.example.project.components.categoriesFor
import org.example.project.components.filterWarranties
import org.example.project.model.Warranty
import org.example.project.theme.AppTheme

@Composable
fun ItemsScreen(warranties: List<Warranty>) {
    val colors = AppTheme.colors
    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ALL_CATEGORIES) }

    val categories = remember(warranties) { categoriesFor(warranties) }
    val filtered = remember(warranties, query, selectedCategory) { filterWarranties(warranties, query, selectedCategory) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding(),
        contentPadding = PaddingValues(start = 18.dp, top = 18.dp, end = 18.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Text("All purchases", color = colors.ink, fontSize = 23.sp, fontWeight = FontWeight.SemiBold)
        }
        item { SearchField(query = query, onQueryChange = { query = it }) }
        item {
            CategoryFilters(
                categories = categories,
                selected = selectedCategory,
                onSelect = { selectedCategory = it },
                title = "Browse by category"
            )
        }
        item { PurchaseLibrary(filtered, title = "Matching items") }
    }
}
