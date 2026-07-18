package org.example.project.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.components.ALL_CATEGORIES
import org.example.project.components.CategoryFilters
import org.example.project.components.MetricTile
import org.example.project.components.PurchaseLibrary
import org.example.project.components.ReviewItem
import org.example.project.components.SearchField
import org.example.project.components.categoriesFor
import org.example.project.components.filterWarranties
import org.example.project.components.formatCurrency
import org.example.project.components.urgencyColor
import org.example.project.model.Warranty
import org.example.project.theme.AppTheme

@Composable
fun HomeScreen(warranties: List<Warranty>) {
    val colors = AppTheme.colors
    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ALL_CATEGORIES) }

    val categories = remember(warranties) { categoriesFor(warranties) }
    val filtered = remember(warranties, query, selectedCategory) { filterWarranties(warranties, query, selectedCategory) }
    val attentionItems = remember(warranties) { warranties.filter { it.urgencyDays != null }.sortedBy { it.urgencyDays } }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding(),
        contentPadding = PaddingValues(start = 18.dp, top = 18.dp, end = 18.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item { LibraryHeader() }
        item { SearchField(query = query, onQueryChange = { query = it }) }
        item { CoverageOverview(warranties) }
        item {
            CategoryFilters(
                categories = categories,
                selected = selectedCategory,
                onSelect = { selectedCategory = it }
            )
        }
        if (attentionItems.isNotEmpty()) {
            item { AttentionQueue(attentionItems) }
        }
        item { PurchaseLibrary(filtered) }
    }
}

@Composable
private fun LibraryHeader() {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("Good morning, Alex", color = colors.ink, fontSize = 23.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(3.dp))
            Text("Here's your purchase library", color = colors.mutedInk, fontSize = 12.sp)
        }
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(colors.glass),
            contentAlignment = Alignment.Center
        ) {
            Text("AM", color = colors.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun CoverageOverview(warranties: List<Warranty>) {
    val colors = AppTheme.colors
    val totalValue = warranties.sumOf { it.price ?: 0.0 }
    val storeCount = warranties.map { it.store }.distinct().size
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = colors.glass,
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Library overview", color = colors.ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                Text("•••", color = colors.mutedInk, fontSize = 13.sp, letterSpacing = 1.sp)
            }
            Spacer(Modifier.height(15.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricTile(Modifier.weight(1f), formatCurrency(totalValue), "Covered value", "${warranties.size} items", colors.softPrimary, colors.primary)
                MetricTile(Modifier.weight(1f), warranties.size.toString(), "Saved purchases", "$storeCount stores", colors.softMint, colors.success)
            }
        }
    }
}

@Composable
private fun AttentionQueue(items: List<Warranty>) {
    val colors = AppTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Needs attention", color = colors.ink, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Text("${items.size} items", color = colors.alert, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, colors.border, RoundedCornerShape(22.dp)),
            shape = RoundedCornerShape(22.dp),
            color = colors.glass,
            shadowElevation = 5.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                items.forEachIndexed { index, warranty ->
                    val days = warranty.urgencyDays ?: 0
                    val accent = urgencyColor(days, colors)
                    ReviewItem(warranty.productName, warranty.warrantyStatusLabel, "$days days", accent)
                    if (index != items.lastIndex) {
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.border))
                    }
                }
            }
        }
    }
}
