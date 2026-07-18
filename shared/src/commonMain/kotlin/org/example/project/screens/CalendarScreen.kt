package org.example.project.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.components.ReviewItem
import org.example.project.components.urgencyColor
import org.example.project.model.Warranty
import org.example.project.theme.AppTheme

@Composable
fun CalendarScreen(warranties: List<Warranty>) {
    val colors = AppTheme.colors
    val withDeadline = warranties.filter { it.urgencyDays != null }.sortedBy { it.urgencyDays }
    val thisWeek = withDeadline.filter { (it.urgencyDays ?: 0) <= 7 }
    val thisMonth = withDeadline.filter { (it.urgencyDays ?: 0) in 8..30 }
    val later = withDeadline.filter { (it.urgencyDays ?: 0) > 30 }
    val noDeadline = warranties.filter { it.urgencyDays == null }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding(),
        contentPadding = PaddingValues(start = 18.dp, top = 18.dp, end = 18.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Column {
                Text("Calendar", color = colors.ink, fontSize = 23.sp, fontWeight = FontWeight.SemiBold)
                Text("Warranty and return deadlines", color = colors.mutedInk, fontSize = 12.sp)
            }
        }
        if (thisWeek.isNotEmpty()) item { DeadlineSection("This week", thisWeek) }
        if (thisMonth.isNotEmpty()) item { DeadlineSection("This month", thisMonth) }
        if (later.isNotEmpty()) item { DeadlineSection("Later", later) }
        if (noDeadline.isNotEmpty()) item { NoDeadlineSection(noDeadline) }
        if (warranties.isEmpty()) {
            item {
                Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), color = colors.glass) {
                    Text(
                        "No purchases yet. Add a receipt to start tracking deadlines.",
                        color = colors.mutedInk,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DeadlineSection(title: String, items: List<Warranty>) {
    val colors = AppTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
        Text(title, color = colors.ink, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
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
                    ReviewItem(warranty.productName, warranty.store, "$days days", urgencyColor(days, colors))
                    if (index != items.lastIndex) {
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.border))
                    }
                }
            }
        }
    }
}

@Composable
private fun NoDeadlineSection(items: List<Warranty>) {
    val colors = AppTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
        Text("No deadline tracked", color = colors.ink, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
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
                    ReviewItem(warranty.productName, warranty.store, warranty.purchaseDateLabel, colors.mutedInk)
                    if (index != items.lastIndex) {
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(colors.border))
                    }
                }
            }
        }
    }
}
