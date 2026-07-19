package org.example.project.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.example.project.components.ALL_CATEGORIES
import org.example.project.components.BellIcon
import org.example.project.components.CategoryFilters
import org.example.project.components.ProductArtwork
import org.example.project.components.PurchaseLibrary
import org.example.project.components.ReceiptPerforation
import org.example.project.components.SearchField
import org.example.project.components.WarrantyRing
import org.example.project.components.categoriesFor
import org.example.project.components.coverageSubtitle
import org.example.project.components.endsPhrase
import org.example.project.components.filterWarranties
import org.example.project.components.formatCurrency
import org.example.project.components.friendlyTimeLeft
import org.example.project.components.greetingFor
import org.example.project.components.heroStatusLine
import org.example.project.components.policyKindColor
import org.example.project.components.urgencyColor
import org.example.project.model.Warranty
import org.example.project.model.daysLeft
import org.example.project.model.durationLabel
import org.example.project.model.kindLabel
import org.example.project.model.kindNoun
import org.example.project.model.nearestPolicyDeadline
import org.example.project.model.todayEpochDay
import org.example.project.theme.AppTheme

@Composable
fun HomeScreen(warranties: List<Warranty>) {
    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ALL_CATEGORIES) }

    val categories = remember(warranties) { categoriesFor(warranties) }
    val filtered = remember(warranties, query, selectedCategory) { filterWarranties(warranties, query, selectedCategory) }
    val attentionItems = remember(warranties) {
        warranties.filter { (it.urgencyDays ?: Int.MAX_VALUE) <= 30 }.sortedBy { it.urgencyDays }
    }
    val heroItem = remember(warranties) {
        warranties.maxByOrNull { it.urgencyDays ?: Int.MAX_VALUE }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding(),
        contentPadding = PaddingValues(start = 18.dp, top = 18.dp, end = 18.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item { LibraryHeader(subtitle = coverageSubtitle(warranties.size, attentionItems.size)) }
        if (heroItem != null) {
            item { HeroCard(heroItem) }
        }
        if (attentionItems.isNotEmpty()) {
            item { AttentionBanner(attentionItems) }
        }
        item { SearchField(query = query, onQueryChange = { query = it }) }
        item {
            CategoryFilters(
                categories = categories,
                selected = selectedCategory,
                onSelect = { selectedCategory = it }
            )
        }
        item { PurchaseLibrary(filtered) }
    }
}

@Composable
private fun LibraryHeader(subtitle: String) {
    val colors = AppTheme.colors
    val hour = remember { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).hour }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                greetingFor(hour, "Naveen"),
                color = colors.ink,
                fontSize = 23.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Serif
            )
            Spacer(Modifier.height(3.dp))
            Text(subtitle, color = colors.mutedInk, fontSize = 12.sp)
        }
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(colors.butter),
            contentAlignment = Alignment.Center
        ) {
            Text("NG", color = colors.amber, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun HeroCard(warranty: Warranty) {
    val colors = AppTheme.colors
    val days = warranty.urgencyDays
    val fraction = if (days != null) (days / 365f).coerceIn(0.08f, 1f) else 1f
    val ringColor = if (days != null) urgencyColor(days, colors) else colors.success
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = colors.glass,
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(58.dp), contentAlignment = Alignment.Center) {
                    WarrantyRing(
                        fraction = fraction,
                        color = ringColor,
                        modifier = Modifier.size(58.dp),
                        strokeWidth = 5.dp
                    )
                    ProductArtwork(warranty.shape, colors.ink.copy(alpha = 0.8f), iconSize = 28.dp)
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        warranty.productName,
                        color = colors.ink,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(heroStatusLine(days), color = ringColor, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
            Spacer(Modifier.height(14.dp))
            ReceiptPerforation()
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "${warranty.store} · ${warranty.purchaseDateLabel}",
                    color = colors.mutedInk,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (warranty.price != null) {
                    Text(formatCurrency(warranty.price), color = colors.ink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            if (warranty.policies.isNotEmpty()) {
                Spacer(Modifier.height(11.dp))
                val today = remember { todayEpochDay() }
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    warranty.policies.forEach { policy ->
                        val left = policy.daysLeft(today)
                        val detail = when {
                            left != null && left >= 0 -> friendlyTimeLeft(left)
                            left != null -> "ended"
                            policy.durationDays != null -> durationLabel(policy.durationDays)
                            else -> "no deadline"
                        }
                        Surface(shape = RoundedCornerShape(50), color = policyKindColor(policy.kind, colors)) {
                            Text(
                                "${kindLabel(policy.kind)} · $detail",
                                color = colors.ink,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AttentionBanner(items: List<Warranty>) {
    val colors = AppTheme.colors
    val urgent = items.first()
    val days = urgent.urgencyDays ?: 0
    val today = remember { todayEpochDay() }
    val noun = nearestPolicyDeadline(urgent.policies, today)?.first?.kind?.let { kindNoun(it) } ?: "warranty"
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = colors.blush
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BellIcon(colors.alert, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(11.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Your ${urgent.productName}'s $noun ${endsPhrase(days)}. Worth a quick look?",
                    color = colors.ink,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
                if (items.size > 1) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        if (items.size == 2) "…and 1 more to check" else "…and ${items.size - 1} more to check",
                        color = colors.mutedInk,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
