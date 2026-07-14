package br.com.ysenerbyte.comandospro.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val ElectricBlue = Color(0xFF35B9FF)
val SignalGreen = Color(0xFF36D98B)
val SafetyAmber = Color(0xFFFFB547)
val AlarmRed = Color(0xFFFF5D66)
val DeepNavy = Color(0xFF06111E)
val PanelNavy = Color(0xFF0C2032)
val SteelBlue = Color(0xFF17364D)

private val DarkColors = darkColorScheme(
    primary = ElectricBlue,
    onPrimary = Color(0xFF002D42),
    primaryContainer = Color(0xFF06496A),
    onPrimaryContainer = Color(0xFFBDE9FF),
    secondary = SignalGreen,
    onSecondary = Color(0xFF003824),
    secondaryContainer = Color(0xFF07553A),
    onSecondaryContainer = Color(0xFFA8F5CC),
    tertiary = SafetyAmber,
    onTertiary = Color(0xFF412800),
    tertiaryContainer = Color(0xFF5E3C00),
    onTertiaryContainer = Color(0xFFFFDDAA),
    error = AlarmRed,
    background = DeepNavy,
    onBackground = Color(0xFFE2F1FA),
    surface = Color(0xFF0A1927),
    onSurface = Color(0xFFE2F1FA),
    surfaceVariant = PanelNavy,
    onSurfaceVariant = Color(0xFFB9CEDB),
    outline = Color(0xFF597384)
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF00658E),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC4E7FF),
    onPrimaryContainer = Color(0xFF001E2D),
    secondary = Color(0xFF006C4B),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF8CF8C4),
    onSecondaryContainer = Color(0xFF002115),
    tertiary = Color(0xFF805600),
    background = Color(0xFFF5FAFD),
    onBackground = Color(0xFF172026),
    surface = Color(0xFFF5FAFD),
    onSurface = Color(0xFF172026),
    surfaceVariant = Color(0xFFDCEAF2),
    onSurfaceVariant = Color(0xFF3D4A52),
    error = Color(0xFFBA1A1A)
)

private val AppTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Black,
        fontSize = 34.sp,
        lineHeight = 38.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 25.sp,
        lineHeight = 30.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 25.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 21.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp
    )
)

@Composable
fun ComandosProTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        shapes = Shapes(
            extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
            small = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
            medium = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
            large = androidx.compose.foundation.shape.RoundedCornerShape(26.dp),
            extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(32.dp)
        ),
        content = content
    )
}
