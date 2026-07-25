package org.example.project.components

import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.model.Warranty
import org.example.project.theme.AppTheme

@Composable
fun SearchField(query: String, onQueryChange: (String) -> Unit) {
    val colors = AppTheme.colors
    Surface(
        modifier = Modifier.fillMaxWidth().border(1.dp, colors.divider, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        color = colors.glass
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SearchIcon(colors.mutedInk)
            Spacer(Modifier.width(11.dp))
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = TextStyle(color = colors.ink, fontSize = 14.sp),
                cursorBrush = SolidColor(colors.primary),
                decorationBox = { inner ->
                    if (query.isEmpty()) {
                        Text("Search purchases, stores or receipts", color = colors.mutedInk, fontSize = 14.sp)
                    }
                    inner()
                }
            )
            FilterIcon(colors.ink)
        }
    }
}

@Composable
fun CategoryFilters(categories: List<String>, selected: String, onSelect: (String) -> Unit, title: String = "Your library") {
    val colors = AppTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
        Text(title, color = colors.ink, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { category ->
                FilterPill(category, category == selected, onClick = { onSelect(category) })
            }
        }
    }
}

@Composable
fun PurchaseLibrary(warranties: List<Warranty>, title: String = "Your purchases") {
    val colors = AppTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, color = colors.ink, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Text("${warranties.size} items", color = colors.mutedInk, fontSize = 13.sp)
        }
        if (warranties.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth().border(1.dp, colors.divider, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                color = colors.glass
            ) {
                Text(
                    "Nothing here yet. Try another search — or scan a receipt and we'll keep it safe.",
                    color = colors.mutedInk,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(18.dp)
                )
            }
        } else {
            warranties.chunked(2).forEach { rowItems ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    rowItems.forEach { warranty ->
                        Box(modifier = Modifier.weight(1f)) {
                            ProductTile(warranty)
                        }
                    }
                    if (rowItems.size == 1) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
