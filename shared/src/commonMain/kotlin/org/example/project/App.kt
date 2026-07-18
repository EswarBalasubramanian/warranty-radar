package org.example.project

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Ink = Color(0xFF323748)
private val MutedInk = Color(0xFF858C9D)
private val Paper = Color(0xFFFFFFFF)
private val Glass = Color(0xFFFDFDFF)
private val Border = Color(0xFFFFFFFF).copy(alpha = 0.82f)
private val Primary = Color(0xFF287EEF)
private val SoftPrimary = Color(0xFFE9F2FF)
private val SoftLilac = Color(0xFFECE6FF)
private val SoftMint = Color(0xFFE2F3EC)
private val Mist = Color(0xFFDCEAF0)
private val Butter = Color(0xFFF8EBC8)
private val Blush = Color(0xFFF6DED8)
private val Alert = Color(0xFFEF6254)
private val Success = Color(0xFF45AD78)
private val Backdrop = Brush.linearGradient(
    colors = listOf(Color(0xFFDCC1FF), Color(0xFFCDEAFF), Color(0xFFE5F4F0))
)

@Composable
@Preview
fun App() {
    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize().background(Backdrop)) {
            HomeScreen()
        }
    }
}

@Composable
private fun HomeScreen() {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .safeContentPadding(),
            contentPadding = PaddingValues(start = 18.dp, top = 18.dp, end = 18.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item { LibraryHeader() }
            item { SearchField() }
            item { CoverageOverview() }
            item { CategoryFilters() }
            item { AttentionQueue() }
            item { PurchaseLibrary() }
        }
        BottomNavigation()
    }
}

@Composable
private fun LibraryHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("Good morning, Alex", color = Ink, fontSize = 23.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(3.dp))
            Text("Here's your purchase library", color = MutedInk, fontSize = 12.sp)
        }
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Glass),
            contentAlignment = Alignment.Center
        ) {
            Text("AM", color = Primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SearchField() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Glass,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SearchIcon(MutedInk)
            Spacer(Modifier.width(11.dp))
            Text("Search purchases, stores or receipts", color = MutedInk, fontSize = 13.sp, modifier = Modifier.weight(1f))
            FilterIcon(Ink)
        }
    }
}

@Composable
private fun CoverageOverview() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = Glass,
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Library overview", color = Ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                Text("•••", color = MutedInk, fontSize = 13.sp, letterSpacing = 1.sp)
            }
            Spacer(Modifier.height(15.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricTile(Modifier.weight(1f), "£4,260", "Covered value", "+8% this month", SoftPrimary, Primary)
                MetricTile(Modifier.weight(1f), "24", "Saved purchases", "8 stores", SoftMint, Success)
            }
        }
    }
}

@Composable
private fun MetricTile(modifier: Modifier, value: String, label: String, detail: String, background: Color, accent: Color) {
    Surface(modifier = modifier, shape = RoundedCornerShape(17.dp), color = background) {
        Column(modifier = Modifier.padding(13.dp)) {
            Text(value, color = Ink, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(3.dp))
            Text(label, color = MutedInk, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(8.dp))
            Text(detail, color = accent, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun CategoryFilters() {
    Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
        Text("Your library", color = Ink, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterPill("All items", true)
            FilterPill("Tech", false)
            FilterPill("Home", false)
            FilterPill("Work", false)
            FilterPill("Family", false)
        }
    }
}

@Composable
private fun FilterPill(label: String, selected: Boolean) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (selected) Primary else Glass,
        modifier = if (selected) Modifier else Modifier.border(1.dp, Border, RoundedCornerShape(50))
    ) {
        Text(
            text = label,
            color = if (selected) Paper else Ink,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 15.dp, vertical = 9.dp)
        )
    }
}

@Composable
private fun AttentionQueue() {
    Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Needs attention", color = Ink, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Text("2 items", color = Alert, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Border, RoundedCornerShape(22.dp)),
            shape = RoundedCornerShape(22.dp),
            color = Glass,
            shadowElevation = 5.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                ReviewItem("Sony WH-1000XM5", "Return window ends", "3 days", Alert)
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Border))
                ReviewItem("Ninja Air Fryer MAX", "Return window ends", "16 days", Color(0xFF9B7422))
            }
        }
    }
}

@Composable
private fun ReviewItem(title: String, subtitle: String, time: String, accent: Color) {
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
            Text(title, color = Ink, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(2.dp))
            Text(subtitle, color = MutedInk, fontSize = 11.sp)
        }
        Text(time, color = accent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun PurchaseLibrary() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Your purchases", color = Ink, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Text("24 items", color = MutedInk, fontSize = 12.sp)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                ProductTile("Apple Watch", "John Lewis", "Protected", Mist, ProductShape.Watch)
            }
            Box(modifier = Modifier.weight(1f)) {
                ProductTile("Desk lamp", "Habitat", "Protected", Butter, ProductShape.Lamp)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                ProductTile("MacBook Air", "Apple", "2 years left", SoftLilac, ProductShape.Laptop)
            }
            Box(modifier = Modifier.weight(1f)) {
                ProductTile("Coffee machine", "Currys", "Protected", Blush, ProductShape.Coffee)
            }
        }
    }
}

@Composable
private fun ProductTile(title: String, store: String, status: String, artworkColor: Color, shape: ProductShape) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp),
        color = Paper,
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
                ProductArtwork(shape, Ink.copy(alpha = 0.76f))
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Paper)
                    .padding(13.dp)
            ) {
                Text(title, color = Ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(3.dp))
                Text(store, color = MutedInk, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(9.dp))
                Text(status, color = Success, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

private enum class ProductShape { Watch, Lamp, Laptop, Coffee }

@Composable
private fun ProductArtwork(shape: ProductShape, color: Color) {
    Canvas(modifier = Modifier.size(74.dp)) {
        val stroke = Stroke(width = 2.dp.toPx())
        val thinStroke = Stroke(width = 1.4.dp.toPx())
        val w = size.width
        val h = size.height
        when (shape) {
            ProductShape.Watch -> {
                drawRoundRect(color, Offset(w * 0.36f, h * 0.07f), Size(w * 0.28f, h * 0.86f), CornerRadius(8.dp.toPx()), style = stroke)
                drawRoundRect(color, Offset(w * 0.20f, h * 0.27f), Size(w * 0.60f, h * 0.46f), CornerRadius(10.dp.toPx()), style = stroke)
                drawCircle(color, radius = w * 0.11f, center = Offset(w * 0.50f, h * 0.50f), style = thinStroke)
            }
            ProductShape.Lamp -> {
                drawLine(color, Offset(w * 0.50f, h * 0.38f), Offset(w * 0.50f, h * 0.77f), strokeWidth = stroke.width)
                drawLine(color, Offset(w * 0.28f, h * 0.77f), Offset(w * 0.72f, h * 0.77f), strokeWidth = stroke.width)
                val shade = Path().apply {
                    moveTo(w * 0.24f, h * 0.38f)
                    lineTo(w * 0.76f, h * 0.38f)
                    lineTo(w * 0.64f, h * 0.12f)
                    lineTo(w * 0.36f, h * 0.12f)
                    close()
                }
                drawPath(shade, color, style = stroke)
            }
            ProductShape.Laptop -> {
                drawRoundRect(color, Offset(w * 0.18f, h * 0.20f), Size(w * 0.64f, h * 0.44f), CornerRadius(5.dp.toPx()), style = stroke)
                drawLine(color, Offset(w * 0.10f, h * 0.76f), Offset(w * 0.90f, h * 0.76f), strokeWidth = stroke.width)
                drawLine(color, Offset(w * 0.18f, h * 0.64f), Offset(w * 0.10f, h * 0.76f), strokeWidth = stroke.width)
                drawLine(color, Offset(w * 0.82f, h * 0.64f), Offset(w * 0.90f, h * 0.76f), strokeWidth = stroke.width)
            }
            ProductShape.Coffee -> {
                drawRoundRect(color, Offset(w * 0.26f, h * 0.14f), Size(w * 0.48f, h * 0.66f), CornerRadius(8.dp.toPx()), style = stroke)
                drawCircle(color, radius = w * 0.11f, center = Offset(w * 0.50f, h * 0.41f), style = thinStroke)
                drawLine(color, Offset(w * 0.38f, h * 0.88f), Offset(w * 0.62f, h * 0.88f), strokeWidth = stroke.width)
            }
        }
    }
}

@Composable
private fun BottomNavigation() {
    Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 10.dp)) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            color = Glass,
            shadowElevation = 10.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavigationItem(Modifier.weight(1f), NavigationIcon.Home, true)
                NavigationItem(Modifier.weight(1f), NavigationIcon.Items, false)
                NavigationItem(Modifier.weight(1f), NavigationIcon.Add, false)
                NavigationItem(Modifier.weight(1f), NavigationIcon.Calendar, false)
                NavigationItem(Modifier.weight(1f), NavigationIcon.Profile, false)
            }
        }
    }
}

private enum class NavigationIcon { Home, Items, Add, Calendar, Profile }

@Composable
private fun NavigationItem(modifier: Modifier, icon: NavigationIcon, selected: Boolean) {
    Box(modifier = modifier.height(48.dp), contentAlignment = Alignment.Center) {
        if (icon == NavigationIcon.Add) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Primary),
                contentAlignment = Alignment.Center
            ) {
                NavigationGlyph(icon, Paper)
            }
        } else {
            NavigationGlyph(icon, if (selected) Primary else MutedInk)
        }
    }
}

@Composable
private fun NavigationGlyph(icon: NavigationIcon, color: Color) {
    Canvas(modifier = Modifier.size(23.dp)) {
        val stroke = Stroke(width = 1.7.dp.toPx())
        val inset = 3.dp.toPx()
        val middle = size.width / 2f
        when (icon) {
            NavigationIcon.Home -> {
                val house = Path().apply {
                    moveTo(inset, size.height * 0.47f)
                    lineTo(middle, inset)
                    lineTo(size.width - inset, size.height * 0.47f)
                    lineTo(size.width - inset, size.height - inset)
                    lineTo(inset, size.height - inset)
                    close()
                }
                drawPath(house, color, style = stroke)
            }
            NavigationIcon.Items -> {
                drawRoundRect(color, Offset(inset, inset), Size(size.width - inset * 2, size.height - inset * 2), CornerRadius(3.dp.toPx()), style = stroke)
                drawLine(color, Offset(inset, size.height * 0.42f), Offset(size.width - inset, size.height * 0.42f), strokeWidth = stroke.width)
                drawLine(color, Offset(size.width * 0.42f, inset), Offset(size.width * 0.42f, size.height - inset), strokeWidth = stroke.width)
            }
            NavigationIcon.Add -> {
                drawLine(color, Offset(middle, 5.dp.toPx()), Offset(middle, size.height - 5.dp.toPx()), strokeWidth = stroke.width)
                drawLine(color, Offset(5.dp.toPx(), middle), Offset(size.width - 5.dp.toPx(), middle), strokeWidth = stroke.width)
            }
            NavigationIcon.Calendar -> {
                drawRoundRect(color, Offset(inset, 5.dp.toPx()), Size(size.width - inset * 2, size.height - 8.dp.toPx()), CornerRadius(4.dp.toPx()), style = stroke)
                drawLine(color, Offset(inset, size.height * 0.40f), Offset(size.width - inset, size.height * 0.40f), strokeWidth = stroke.width)
                drawLine(color, Offset(size.width * 0.33f, 2.dp.toPx()), Offset(size.width * 0.33f, 8.dp.toPx()), strokeWidth = stroke.width)
                drawLine(color, Offset(size.width * 0.67f, 2.dp.toPx()), Offset(size.width * 0.67f, 8.dp.toPx()), strokeWidth = stroke.width)
            }
            NavigationIcon.Profile -> {
                drawCircle(color, radius = size.width * 0.18f, center = Offset(middle, size.height * 0.34f), style = stroke)
                drawArc(color, 200f, 140f, false, Offset(inset, size.height * 0.34f), Size(size.width - inset * 2, size.height * 0.63f), style = stroke)
            }
        }
    }
}

@Composable
private fun SearchIcon(color: Color) {
    Canvas(modifier = Modifier.size(18.dp)) {
        val stroke = Stroke(width = 1.8.dp.toPx())
        drawCircle(color, radius = size.width * 0.30f, center = Offset(size.width * 0.42f, size.height * 0.42f), style = stroke)
        drawLine(color, Offset(size.width * 0.64f, size.height * 0.64f), Offset(size.width * 0.90f, size.height * 0.90f), strokeWidth = stroke.width)
    }
}

@Composable
private fun FilterIcon(color: Color) {
    Canvas(modifier = Modifier.size(18.dp)) {
        val stroke = 1.6.dp.toPx()
        drawLine(color, Offset(1.dp.toPx(), size.height * 0.27f), Offset(size.width - 1.dp.toPx(), size.height * 0.27f), strokeWidth = stroke)
        drawLine(color, Offset(1.dp.toPx(), size.height * 0.73f), Offset(size.width - 1.dp.toPx(), size.height * 0.73f), strokeWidth = stroke)
        drawCircle(color, radius = 2.dp.toPx(), center = Offset(size.width * 0.68f, size.height * 0.27f))
        drawCircle(color, radius = 2.dp.toPx(), center = Offset(size.width * 0.33f, size.height * 0.73f))
    }
}
