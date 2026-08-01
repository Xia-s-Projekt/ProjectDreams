package com.projectdreams.app.ui.theme

import android.os.Build
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.projectdreams.app.R


private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

private val MontserratFamily = FontFamily(
    Font(GoogleFont("Montserrat"), provider, weight = FontWeight.Normal),
    Font(GoogleFont("Montserrat"), provider, weight = FontWeight.Medium),
    Font(GoogleFont("Montserrat"), provider, weight = FontWeight.SemiBold),
    Font(GoogleFont("Montserrat"), provider, weight = FontWeight.Bold),
    Font(GoogleFont("Montserrat"), provider, weight = FontWeight.ExtraBold),
    Font(GoogleFont("Montserrat"), provider, weight = FontWeight.Black)
)

private val GoogleSansFamily = FontFamily(
    Font(GoogleFont("Google Sans"), provider, weight = FontWeight.Normal),
    Font(GoogleFont("Google Sans"), provider, weight = FontWeight.Medium),
    Font(GoogleFont("Google Sans"), provider, weight = FontWeight.SemiBold),
    Font(GoogleFont("Google Sans"), provider, weight = FontWeight.Bold)
)

val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = MontserratFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 57.sp,
        lineHeight = 64.sp
    ),
    displayMedium = TextStyle(
        fontFamily = MontserratFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 45.sp,
        lineHeight = 52.sp
    ),
    displaySmall = TextStyle(
        fontFamily = MontserratFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = MontserratFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = MontserratFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = MontserratFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    titleLarge = TextStyle(
        fontFamily = GoogleSansFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = GoogleSansFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = GoogleSansFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = GoogleSansFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = GoogleSansFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = GoogleSansFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontFamily = GoogleSansFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = GoogleSansFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = GoogleSansFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

/** Squircle shapes at 60% smoothness per Material 3 Expressive spec. */
val SquircleShapes = Shapes(
    extraSmall = AbsoluteSmoothCornerShape(12.dp, 60),
    small = AbsoluteSmoothCornerShape(16.dp, 60),
    medium = AbsoluteSmoothCornerShape(24.dp, 60),
    large = AbsoluteSmoothCornerShape(32.dp, 60),
    extraLarge = AbsoluteSmoothCornerShape(40.dp, 60)
)

/** Bouncy spring spec used for all interactive micro-animations. */
val BouncySpring: SpringSpec<Float> = spring(
    dampingRatio = 0.6f,
    stiffness = 400f
)

val LocalBouncySpring = staticCompositionLocalOf { BouncySpring }

private val LightColors = lightColorScheme(
    primary = Color(0xFF7C4DFF),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE9DDFF),
    onPrimaryContainer = Color(0xFF22005D),
    secondary = Color(0xFF03DAC6),
    onSecondary = Color(0xFF00201E),
    secondaryContainer = Color(0xFFCCF5F0),
    onSecondaryContainer = Color(0xFF002120),
    tertiary = Color(0xFFFF6B9D),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFD9E4),
    onTertiaryContainer = Color(0xFF3E001E),
    background = Color(0xFFFDFBFF),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFDFBFF),
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF79747E),
    outlineVariant = Color(0xFFCAC4D0)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFB89CFF),
    onPrimary = Color(0xFF2A0058),
    primaryContainer = Color(0xFF4A2A8E),
    onPrimaryContainer = Color(0xFFE9DDFF),
    secondary = Color(0xFF03DAC6),
    onSecondary = Color(0xFF003733),
    secondaryContainer = Color(0xFF005048),
    onSecondaryContainer = Color(0xFF70F7E1),
    tertiary = Color(0xFFFFB1C8),
    onTertiary = Color(0xFF5E1133),
    tertiaryContainer = Color(0xFF7A294A),
    onTertiaryContainer = Color(0xFFFFD9E4),
    background = Color(0xFF141218),
    onBackground = Color(0xFFE6E1E5),
    surface = Color(0xFF141218),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF938F99),
    outlineVariant = Color(0xFF49454F)
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProjectDreamsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = SquircleShapes
    ) {
        CompositionLocalProvider(
            LocalOverscrollFactory provides null,
            LocalBouncySpring provides BouncySpring
        ) {
            content()
        }
    }
}
