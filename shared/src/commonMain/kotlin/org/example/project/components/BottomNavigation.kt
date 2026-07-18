package org.example.project.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import org.example.project.AppScreen
import org.example.project.theme.AppTheme

@Composable
fun BottomNavigation(current: AppScreen, onNavigate: (AppScreen) -> Unit, onAddReceipt: () -> Unit) {
    val colors = AppTheme.colors
    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 10.dp)) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            color = colors.glass,
            shadowElevation = 10.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavigationItem(Modifier.weight(1f), NavigationIcon.Home, current == AppScreen.Home) { onNavigate(AppScreen.Home) }
                NavigationItem(Modifier.weight(1f), NavigationIcon.Items, current == AppScreen.Items) { onNavigate(AppScreen.Items) }
                NavigationItem(Modifier.weight(1f), NavigationIcon.Add, false, onAddReceipt)
                NavigationItem(Modifier.weight(1f), NavigationIcon.Calendar, current == AppScreen.Calendar) { onNavigate(AppScreen.Calendar) }
                NavigationItem(Modifier.weight(1f), NavigationIcon.Profile, current == AppScreen.Profile) { onNavigate(AppScreen.Profile) }
            }
        }
    }
}

@Composable
private fun NavigationItem(modifier: Modifier, icon: NavigationIcon, selected: Boolean, onClick: () -> Unit) {
    val colors = AppTheme.colors
    val haptic = LocalHapticFeedback.current
    val clickWithFeedback: () -> Unit = {
        if (icon == NavigationIcon.Add) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        onClick()
    }
    Box(modifier = modifier.height(48.dp).clickable(onClick = clickWithFeedback), contentAlignment = Alignment.Center) {
        if (icon == NavigationIcon.Add) {
            Box(
                modifier = Modifier
                    .pressBounce(0.88f)
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(colors.primary),
                contentAlignment = Alignment.Center
            ) {
                NavigationGlyph(icon, colors.paper)
            }
        } else {
            NavigationGlyph(icon, if (selected) colors.primary else colors.mutedInk)
        }
    }
}
