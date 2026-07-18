package org.example.project.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

enum class ThemeMode { System, Light, Dark }

data class AppColorScheme(
    val ink: Color,
    val mutedInk: Color,
    val paper: Color,
    val glass: Color,
    val border: Color,
    val divider: Color,
    val primary: Color,
    val softPrimary: Color,
    val softLilac: Color,
    val softMint: Color,
    val mist: Color,
    val butter: Color,
    val blush: Color,
    val alert: Color,
    val amber: Color,
    val success: Color,
    val backdrop: Brush,
    val screenSurface: Color,
    val accent: Color,
    val uploadBorder: Color,
    val uploadBackground: Color,
    val uploadIconBackground: Color,
    val secondaryButtonBackground: Color
)

val LightAppColors = AppColorScheme(
    ink = Color(0xFF37332B),
    mutedInk = Color(0xFF8D867A),
    paper = Color(0xFFFFFDF8),
    glass = Color(0xFFFFFDF6),
    border = Color(0xFFFFFFFF).copy(alpha = 0.82f),
    divider = Color(0xFFE7DFD2),
    primary = Color(0xFF287EEF),
    softPrimary = Color(0xFFE9F2FF),
    softLilac = Color(0xFFECE6FF),
    softMint = Color(0xFFE2F3EC),
    mist = Color(0xFFDCEAF0),
    butter = Color(0xFFF8EBC8),
    blush = Color(0xFFF6DED8),
    alert = Color(0xFFEF6254),
    amber = Color(0xFF9B7422),
    success = Color(0xFF45AD78),
    backdrop = Brush.linearGradient(colors = listOf(Color(0xFFF6DFC4), Color(0xFFFAF0DC), Color(0xFFE3EFE0))),
    screenSurface = Color(0xFFFBF7EF),
    accent = Color(0xFF0D766E),
    uploadBorder = Color(0xFF9BCFC9),
    uploadBackground = Color(0xFFDDF3F0),
    uploadIconBackground = Color(0xFFFFFDF8),
    secondaryButtonBackground = Color(0xFFEAE4D8)
)

val DarkAppColors = AppColorScheme(
    ink = Color(0xFFF0EBE2),
    mutedInk = Color(0xFFA69E90),
    paper = Color(0xFF211E19),
    glass = Color(0xFF2A261F),
    border = Color(0xFFFFFFFF).copy(alpha = 0.08f),
    divider = Color(0xFFFFFFFF).copy(alpha = 0.12f),
    primary = Color(0xFF5B9DFF),
    softPrimary = Color(0xFF1E2A3D),
    softLilac = Color(0xFF2A2438),
    softMint = Color(0xFF1E2E28),
    mist = Color(0xFF1B2A30),
    butter = Color(0xFF332D1B),
    blush = Color(0xFF332222),
    alert = Color(0xFFFF6B5C),
    amber = Color(0xFFE0A83D),
    success = Color(0xFF5FD08A),
    backdrop = Brush.linearGradient(colors = listOf(Color(0xFF2B2018), Color(0xFF241F16), Color(0xFF1B211A))),
    screenSurface = Color(0xFF171511),
    accent = Color(0xFF34B5A6),
    uploadBorder = Color(0xFF2E5C55),
    uploadBackground = Color(0xFF1B2E2B),
    uploadIconBackground = Color(0xFF2A261F),
    secondaryButtonBackground = Color(0xFF2E2A22)
)

val LocalAppColors = staticCompositionLocalOf { LightAppColors }

object AppTheme {
    val colors: AppColorScheme
        @Composable get() = LocalAppColors.current
}

@Composable
fun WarrantyRadarTheme(themeMode: ThemeMode, content: @Composable () -> Unit) {
    val useDarkColors = when (themeMode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }
    val appColors = if (useDarkColors) DarkAppColors else LightAppColors
    val materialColorScheme = if (useDarkColors) {
        darkColorScheme(
            primary = appColors.primary,
            background = appColors.screenSurface,
            surface = appColors.paper,
            onBackground = appColors.ink,
            onSurface = appColors.ink
        )
    } else {
        lightColorScheme(
            primary = appColors.primary,
            background = appColors.screenSurface,
            surface = appColors.paper,
            onBackground = appColors.ink,
            onSurface = appColors.ink
        )
    }
    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(colorScheme = materialColorScheme, content = content)
    }
}
