package org.example.project.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.example.project.model.ProductShape

@Composable
fun SearchIcon(color: Color) {
    Canvas(modifier = Modifier.size(18.dp)) {
        val stroke = Stroke(width = 1.8.dp.toPx())
        drawCircle(color, radius = size.width * 0.30f, center = Offset(size.width * 0.42f, size.height * 0.42f), style = stroke)
        drawLine(color, Offset(size.width * 0.64f, size.height * 0.64f), Offset(size.width * 0.90f, size.height * 0.90f), strokeWidth = stroke.width)
    }
}

@Composable
fun FilterIcon(color: Color) {
    Canvas(modifier = Modifier.size(18.dp)) {
        val stroke = 1.6.dp.toPx()
        drawLine(color, Offset(1.dp.toPx(), size.height * 0.27f), Offset(size.width - 1.dp.toPx(), size.height * 0.27f), strokeWidth = stroke)
        drawLine(color, Offset(1.dp.toPx(), size.height * 0.73f), Offset(size.width - 1.dp.toPx(), size.height * 0.73f), strokeWidth = stroke)
        drawCircle(color, radius = 2.dp.toPx(), center = Offset(size.width * 0.68f, size.height * 0.27f))
        drawCircle(color, radius = 2.dp.toPx(), center = Offset(size.width * 0.33f, size.height * 0.73f))
    }
}

enum class NavigationIcon { Home, Items, Add, Calendar, Profile }

@Composable
fun NavigationGlyph(icon: NavigationIcon, color: Color) {
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
fun BellIcon(color: Color, modifier: Modifier = Modifier.size(18.dp)) {
    Canvas(modifier) {
        val stroke = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round)
        val w = size.width
        val h = size.height
        val body = Path().apply {
            moveTo(w * 0.22f, h * 0.66f)
            lineTo(w * 0.22f, h * 0.44f)
            cubicTo(w * 0.22f, h * 0.16f, w * 0.78f, h * 0.16f, w * 0.78f, h * 0.44f)
            lineTo(w * 0.78f, h * 0.66f)
        }
        drawPath(body, color, style = stroke)
        drawLine(color, Offset(w * 0.12f, h * 0.66f), Offset(w * 0.88f, h * 0.66f), strokeWidth = stroke.width, cap = StrokeCap.Round)
        drawCircle(color, radius = w * 0.07f, center = Offset(w * 0.50f, h * 0.82f))
    }
}

@Composable
fun ScanIcon(color: Color, modifier: Modifier = Modifier.size(18.dp)) {
    Canvas(modifier) {
        val strokeWidth = 1.7.dp.toPx()
        val w = size.width
        val h = size.height
        val corner = w * 0.26f
        val inset = 1.dp.toPx()
        val corners = Path().apply {
            moveTo(inset, inset + corner)
            lineTo(inset, inset)
            lineTo(inset + corner, inset)
            moveTo(w - inset - corner, inset)
            lineTo(w - inset, inset)
            lineTo(w - inset, inset + corner)
            moveTo(w - inset, h - inset - corner)
            lineTo(w - inset, h - inset)
            lineTo(w - inset - corner, h - inset)
            moveTo(inset + corner, h - inset)
            lineTo(inset, h - inset)
            lineTo(inset, h - inset - corner)
        }
        drawPath(corners, color, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
        drawLine(color, Offset(w * 0.24f, h * 0.5f), Offset(w * 0.76f, h * 0.5f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
    }
}

@Composable
fun ProductArtwork(shape: ProductShape, color: Color, iconSize: Dp = 74.dp) {
    Canvas(modifier = Modifier.size(iconSize)) {
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
            ProductShape.Other -> {
                drawRoundRect(color, Offset(w * 0.22f, h * 0.16f), Size(w * 0.56f, h * 0.68f), CornerRadius(10.dp.toPx()), style = stroke)
                drawLine(color, Offset(w * 0.34f, h * 0.40f), Offset(w * 0.66f, h * 0.40f), strokeWidth = thinStroke.width)
                drawLine(color, Offset(w * 0.34f, h * 0.56f), Offset(w * 0.60f, h * 0.56f), strokeWidth = thinStroke.width)
            }
        }
    }
}
