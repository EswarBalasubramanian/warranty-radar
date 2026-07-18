package org.example.project.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.components.formatCurrency
import org.example.project.model.Warranty
import org.example.project.theme.AppTheme
import org.example.project.theme.ThemeMode

@Composable
fun ProfileScreen(warranties: List<Warranty>, themeMode: ThemeMode, onThemeModeChange: (ThemeMode) -> Unit) {
    val colors = AppTheme.colors
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding(),
        contentPadding = PaddingValues(start = 18.dp, top = 18.dp, end = 18.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item { ProfileHeader() }
        item { StatsCard(warranties) }
        item { AppearanceCard(themeMode, onThemeModeChange) }
    }
}

@Composable
private fun ProfileHeader() {
    val colors = AppTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(colors.butter),
            contentAlignment = Alignment.Center
        ) {
            Text("NG", color = colors.amber, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text("Naveen Gunasekaran", color = colors.ink, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            Text("Keeping every purchase covered", color = colors.mutedInk, fontSize = 12.sp)
        }
    }
}

@Composable
private fun StatsCard(warranties: List<Warranty>) {
    val colors = AppTheme.colors
    val totalValue = warranties.sumOf { it.price ?: 0.0 }
    val storeCount = warranties.map { it.store }.distinct().size
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = colors.glass,
        shadowElevation = 6.dp
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text("Your library at a glance", color = colors.ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(14.dp))
            ProfileStatRow("Total covered value", formatCurrency(totalValue))
            ProfileStatRow("Purchases tracked", warranties.size.toString())
            ProfileStatRow("Stores", storeCount.toString())
        }
    }
}

@Composable
private fun ProfileStatRow(label: String, value: String) {
    val colors = AppTheme.colors
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text(label, color = colors.mutedInk, fontSize = 13.sp, modifier = Modifier.weight(1f))
        Text(value, color = colors.ink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun AppearanceCard(themeMode: ThemeMode, onThemeModeChange: (ThemeMode) -> Unit) {
    val colors = AppTheme.colors
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = colors.glass,
        shadowElevation = 6.dp
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text("Appearance", color = colors.ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text("Choose how Warranty Radar looks on this device", color = colors.mutedInk, fontSize = 12.sp)
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemeMode.entries.forEach { mode ->
                    ThemeOptionPill(
                        label = when (mode) {
                            ThemeMode.System -> "System"
                            ThemeMode.Light -> "Light"
                            ThemeMode.Dark -> "Dark"
                        },
                        selected = mode == themeMode,
                        onClick = { onThemeModeChange(mode) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeOptionPill(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = if (selected) colors.primary else colors.paper
    ) {
        Text(
            label,
            color = if (selected) colors.paper else colors.ink,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
        )
    }
}
