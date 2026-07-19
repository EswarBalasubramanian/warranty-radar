package org.example.project.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.components.ReceiptPerforation
import org.example.project.components.ReviewItem
import org.example.project.components.friendlyTimeLeft
import org.example.project.components.urgencyColor
import org.example.project.components.urgencyFraction
import org.example.project.model.PolicyDeadline
import org.example.project.model.Warranty
import org.example.project.model.policyDeadlines
import org.example.project.model.todayEpochDay
import org.example.project.theme.AppTheme

@Composable
fun CalendarScreen(warranties: List<Warranty>) {
    val colors = AppTheme.colors
    val today = remember { todayEpochDay() }
    val deadlines = remember(warranties) { policyDeadlines(warranties, today) }

    val recentlyEnded = deadlines.filter { it.daysLeft < 0 }
    val thisWeek = deadlines.filter { it.daysLeft in 0..7 }
    val thisMonth = deadlines.filter { it.daysLeft in 8..30 }
    val later = deadlines.filter { it.daysLeft > 30 }
    val trackedIds = deadlines.map { it.warranty.id }.toSet()
    val noDeadline = warranties.filter { it.id !in trackedIds }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding(),
        contentPadding = PaddingValues(start = 18.dp, top = 18.dp, end = 18.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Column {
                Text("Calendar", color = colors.ink, fontSize = 23.sp, fontWeight = FontWeight.SemiBold)
                Text("Every warranty, replacement and return clock in one place", color = colors.mutedInk, fontSize = 13.sp)
            }
        }
        if (thisWeek.isNotEmpty()) item { DeadlineSection("This week", thisWeek) }
        if (thisMonth.isNotEmpty()) item { DeadlineSection("This month", thisMonth) }
        if (later.isNotEmpty()) item { DeadlineSection("Later", later) }
        if (recentlyEnded.isNotEmpty()) item { RecentlyEndedSection(recentlyEnded) }
        if (noDeadline.isNotEmpty()) item { NoDeadlineSection(noDeadline) }
        if (warranties.isEmpty()) {
            item {
                Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), color = colors.glass) {
                    Text(
                        "Nothing to watch yet — scan a receipt and we'll keep an eye on the dates for you.",
                        color = colors.mutedInk,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DeadlineSection(title: String, entries: List<PolicyDeadline>) {
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
                entries.forEachIndexed { index, entry ->
                    val subtitle = entry.policy?.let { policy ->
                        if (policy.provider != null) "${policy.title} · ${policy.provider}" else policy.title
                    } ?: entry.warranty.store
                    ReviewItem(
                        entry.warranty.productName,
                        subtitle,
                        friendlyTimeLeft(entry.daysLeft),
                        urgencyColor(entry.daysLeft, colors),
                        progress = urgencyFraction(entry.daysLeft)
                    )
                    if (index != entries.lastIndex) {
                        ReceiptPerforation()
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentlyEndedSection(entries: List<PolicyDeadline>) {
    val colors = AppTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
        Text("Just ended", color = colors.ink, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, colors.border, RoundedCornerShape(22.dp)),
            shape = RoundedCornerShape(22.dp),
            color = colors.glass,
            shadowElevation = 5.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                entries.forEachIndexed { index, entry ->
                    val daysAgo = -entry.daysLeft
                    ReviewItem(
                        entry.warranty.productName,
                        entry.policyTitle,
                        if (daysAgo == 1) "Ended yesterday" else "Ended $daysAgo days ago",
                        colors.mutedInk
                    )
                    if (index != entries.lastIndex) {
                        ReceiptPerforation()
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
                        ReceiptPerforation()
                    }
                }
            }
        }
    }
}
