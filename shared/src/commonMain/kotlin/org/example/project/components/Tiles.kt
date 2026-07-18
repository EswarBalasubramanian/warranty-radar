package org.example.project.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.model.ProductShape
import org.example.project.model.Warranty
import org.example.project.theme.AppColorScheme
import org.example.project.theme.AppTheme

fun urgencyColor(days: Int, colors: AppColorScheme): Color = when {
    days <= 3 -> colors.alert
    days <= 14 -> colors.amber
    else -> colors.success
}

fun tileColorFor(shape: ProductShape, colors: AppColorScheme): Color = when (shape) {
    ProductShape.Watch -> colors.mist
    ProductShape.Lamp -> colors.butter
    ProductShape.Laptop -> colors.softLilac
    ProductShape.Coffee -> colors.blush
    ProductShape.Other -> colors.softMint
}

fun formatCurrency(amount: Double): String {
    val rounded = amount.toLong()
    val grouped = rounded.toString().reversed().chunked(3).joinToString(",").reversed()
    return "£$grouped"
}

@Composable
fun MetricTile(modifier: Modifier, value: String, label: String, detail: String, background: Color, accent: Color) {
    val colors = AppTheme.colors
    Surface(modifier = modifier, shape = RoundedCornerShape(17.dp), color = background) {
        Column(modifier = Modifier.padding(13.dp)) {
            Text(value, color = colors.ink, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(3.dp))
            Text(label, color = colors.mutedInk, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(8.dp))
            Text(detail, color = accent, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun FilterPill(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = AppTheme.colors
    Surface(
        shape = RoundedCornerShape(50),
        color = if (selected) colors.primary else colors.glass,
        modifier = (if (selected) Modifier else Modifier.border(1.dp, colors.border, RoundedCornerShape(50)))
            .clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            color = if (selected) colors.paper else colors.ink,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 15.dp, vertical = 9.dp)
        )
    }
}

@Composable
fun ReviewItem(title: String, subtitle: String, time: String, accent: Color) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .clip(CircleShape)
                .background(accent)
        )
        Spacer(Modifier.width(11.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = colors.ink, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(2.dp))
            Text(subtitle, color = colors.mutedInk, fontSize = 11.sp)
        }
        Text(time, color = accent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun ProductTile(warranty: Warranty) {
    val colors = AppTheme.colors
    val artworkColor = tileColorFor(warranty.shape, colors)
    val statusColor = if (warranty.urgencyDays != null) urgencyColor(warranty.urgencyDays, colors) else colors.success
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp),
        color = colors.paper,
        shadowElevation = 5.dp
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.24f)
                    .background(artworkColor),
                contentAlignment = Alignment.Center
            ) {
                ProductArtwork(warranty.shape, colors.ink.copy(alpha = 0.76f))
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.paper)
                    .padding(13.dp)
            ) {
                Text(warranty.productName, color = colors.ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(3.dp))
                Text(warranty.store, color = colors.mutedInk, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(9.dp))
                Text(warranty.warrantyStatusLabel, color = statusColor, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}
