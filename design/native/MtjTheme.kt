package com.mtj.design

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Source: ../tokens.json v0.1.1, MTJ_NATIVE_FIRST_FLOW_20260909.
 * Geometry is dp, scalable text is sp, durations are ms. CSS px is NOT converted.
 * Product tokens follow the approved design-system-v1r1 contract.
 * No dynamic-color or production logo replacement is applied here.
 * Integration target: Compose BOM 2025.05.01, Material3, activity-compose 1.10.1.
 * Module wiring, compilation and device verification are pending with the integrator.
 */
object MtjTokens {
    val Canvas = Color(0xFFFAFBFA)
    val Surface = Color(0xFFFFFFFF)
    val GlassSurface = Surface
    val Ink = Color(0xFF151B18)
    val Muted = Color(0xFF536159)
    val Disabled = Color(0xFF6E7872)
    val Primary = Color(0xFF7B2345)
    val PrimarySoft = Color(0xFFF8E5EC)
    val Context = Color(0xFF226349)
    val ContextSoft = Color(0xFFD7E6DF)
    val WarmSignal = Color(0xFFDFC6CD)
    val QuietMint = Color(0xFFD7E6DF)
    val SurfaceMuted = Color(0xFFF0F3F1)
    val Border = Color(0xFF738178)
    val Divider = Color(0xFFDCE2DE)
    val Error = Color(0xFFAE2638)
    val LuminousLine = Primary
    val SoftShadow = Color(0x1F24323A)
    val DarkCanvas = Color(0xFF171B1A)
    val DarkSurface = Color(0xFF202623)
    val DarkSurfaceHigh = Color(0xFF28322C)
    val DarkInk = Color(0xFFF3F6F3)
    val DarkMuted = Color(0xFFBBC8C0)
    val DarkDisabled = Color(0xFF9AA79F)
    val DarkBorder = Color(0xFF87968D)
    val DarkDivider = Color(0xFF3B4740)
    val DarkPrimary = Color(0xFFF4BAD0)
    val DarkOnPrimary = Color(0xFF29151E)
    val DarkPrimaryContainer = Color(0xFF492D39)
    val DarkError = Color(0xFFFFACB4)
    val DarkSuccess = Color(0xFF96DCB7)

    val HeroCorner = 8.dp
    val PanelCorner = 8.dp
    val ActionCorner = 8.dp
    val ControlCorner = 8.dp
    val TarotFrameCorner = 6.dp
    val SectionCorner = 0.dp

    val SideCompact = 16.dp
    val SideMedium = 20.dp
    val SideWide = 24.dp
    val WideBreakpoint = 600.dp
    val MediumBreakpoint = 411.dp
    val ContentMaxWidth = 552.dp
    val TouchMin = 48.dp
    val ActionMinHeight = 52.dp
    val ActionVerticalPadding = 12.dp
    val ActionHorizontalPadding = 18.dp
    val NavigationMinHeight = 64.dp
    val ActionToNavigationGap = 8.dp
    val ContentToActionGap = 18.dp
    val StateLine = 1.dp

    val Brand = TextStyle(
        fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold,
        fontSize = 28.sp, lineHeight = 36.sp, letterSpacing = 0.sp,
    )
    val ScreenTitle = TextStyle(
        fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold,
        fontSize = 24.sp, lineHeight = 32.sp, letterSpacing = 0.sp,
    )
    val SectionTitle = TextStyle(
        fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold,
        fontSize = 20.sp, lineHeight = 28.sp, letterSpacing = 0.sp,
    )
    val Body = TextStyle(
        fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal,
        fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.sp,
    )
    val Label = TextStyle(
        fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.sp,
    )
    val Supporting = Label.copy(fontWeight = FontWeight.Normal)
    val PrimaryAction = TextStyle(
        fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.sp,
    )
    val Navigation = TextStyle(
        fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium,
        fontSize = 12.sp, lineHeight = 18.sp, letterSpacing = 0.sp,
    )
    val PillarGlyph = TextStyle(
        fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold,
        fontSize = 24.sp, lineHeight = 32.sp, letterSpacing = 0.sp,
    )
    val Ganji = TextStyle(
        fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold,
        fontSize = 48.sp, lineHeight = 60.sp, letterSpacing = 0.sp,
    )
    val LuckGlyph = PillarGlyph

    const val GatherMillis = 90
    const val SpreadMillis = 190
    const val SettleMillis = 80
    const val ReducedMotionMillis = 0
}

val MtjBottomSurfaceBrush: Brush = Brush.verticalGradient(
    listOf(MtjTokens.GlassSurface, MtjTokens.Surface),
)

val MtjDarkBottomSurfaceBrush: Brush = Brush.verticalGradient(
    listOf(MtjTokens.DarkSurfaceHigh, MtjTokens.DarkSurface),
)

val MtjScreenBrush: Brush = Brush.verticalGradient(
    listOf(MtjTokens.Canvas, MtjTokens.Canvas),
)

val MtjDarkScreenBrush: Brush = Brush.verticalGradient(
    listOf(MtjTokens.DarkCanvas, MtjTokens.DarkCanvas),
)

enum class MtjThemeMode { System, Light, Dark }

private val MtjLightColors = lightColorScheme(
    primary = MtjTokens.Primary,
    onPrimary = MtjTokens.Surface,
    primaryContainer = MtjTokens.PrimarySoft,
    onPrimaryContainer = MtjTokens.Primary,
    secondary = MtjTokens.Context,
    onSecondary = MtjTokens.Surface,
    secondaryContainer = MtjTokens.ContextSoft,
    onSecondaryContainer = MtjTokens.Ink,
    tertiary = MtjTokens.WarmSignal,
    onTertiary = MtjTokens.Ink,
    tertiaryContainer = MtjTokens.QuietMint,
    onTertiaryContainer = MtjTokens.Ink,
    background = MtjTokens.Canvas,
    onBackground = MtjTokens.Ink,
    surface = MtjTokens.Surface,
    onSurface = MtjTokens.Ink,
    surfaceVariant = MtjTokens.SurfaceMuted,
    onSurfaceVariant = MtjTokens.Muted,
    surfaceTint = MtjTokens.Surface,
    inverseSurface = MtjTokens.Ink,
    inverseOnSurface = MtjTokens.Surface,
    inversePrimary = MtjTokens.PrimarySoft,
    error = MtjTokens.Error,
    onError = MtjTokens.Surface,
    errorContainer = MtjTokens.PrimarySoft,
    onErrorContainer = MtjTokens.Primary,
    outline = MtjTokens.Border,
    outlineVariant = MtjTokens.Divider,
    scrim = MtjTokens.Ink,
    surfaceBright = MtjTokens.Surface,
    surfaceDim = MtjTokens.SurfaceMuted,
    surfaceContainer = MtjTokens.Canvas,
    surfaceContainerHigh = MtjTokens.GlassSurface,
    surfaceContainerHighest = MtjTokens.SurfaceMuted,
    surfaceContainerLow = MtjTokens.Canvas,
    surfaceContainerLowest = MtjTokens.Surface,
)

private val MtjDarkColors = darkColorScheme(
    primary = MtjTokens.DarkPrimary,
    onPrimary = MtjTokens.DarkOnPrimary,
    primaryContainer = MtjTokens.DarkPrimaryContainer,
    onPrimaryContainer = MtjTokens.DarkInk,
    secondary = MtjTokens.DarkSuccess,
    onSecondary = MtjTokens.DarkCanvas,
    secondaryContainer = MtjTokens.DarkSurfaceHigh,
    onSecondaryContainer = MtjTokens.DarkInk,
    tertiary = MtjTokens.WarmSignal,
    onTertiary = MtjTokens.Ink,
    tertiaryContainer = MtjTokens.DarkSurfaceHigh,
    onTertiaryContainer = MtjTokens.DarkInk,
    background = MtjTokens.DarkCanvas,
    onBackground = MtjTokens.DarkInk,
    surface = MtjTokens.DarkSurface,
    onSurface = MtjTokens.DarkInk,
    surfaceVariant = MtjTokens.DarkSurfaceHigh,
    onSurfaceVariant = MtjTokens.DarkMuted,
    surfaceTint = Color.Transparent,
    inverseSurface = MtjTokens.DarkInk,
    inverseOnSurface = MtjTokens.DarkCanvas,
    inversePrimary = MtjTokens.Primary,
    error = MtjTokens.DarkError,
    onError = MtjTokens.DarkOnPrimary,
    errorContainer = MtjTokens.DarkPrimaryContainer,
    onErrorContainer = MtjTokens.DarkInk,
    outline = MtjTokens.DarkBorder,
    outlineVariant = MtjTokens.DarkDivider,
    scrim = Color(0xFF000000),
    surfaceBright = MtjTokens.DarkSurfaceHigh,
    surfaceDim = MtjTokens.DarkCanvas,
    surfaceContainer = MtjTokens.DarkCanvas,
    surfaceContainerHigh = MtjTokens.DarkSurfaceHigh,
    surfaceContainerHighest = MtjTokens.DarkSurfaceHigh,
    surfaceContainerLow = MtjTokens.DarkCanvas,
    surfaceContainerLowest = MtjTokens.DarkCanvas,
)

private val MtjTypography = Typography(
    displayLarge = MtjTokens.Brand,
    displayMedium = MtjTokens.Brand,
    displaySmall = MtjTokens.Ganji,
    headlineLarge = MtjTokens.ScreenTitle,
    headlineMedium = MtjTokens.ScreenTitle,
    headlineSmall = MtjTokens.SectionTitle,
    titleLarge = MtjTokens.SectionTitle,
    titleMedium = MtjTokens.SectionTitle,
    titleSmall = MtjTokens.Label,
    bodyLarge = MtjTokens.Body,
    bodyMedium = MtjTokens.Body,
    bodySmall = MtjTokens.Supporting,
    labelLarge = MtjTokens.PrimaryAction,
    labelMedium = MtjTokens.Label,
    labelSmall = MtjTokens.Navigation,
)

@Composable
fun MtjTheme(themeMode: MtjThemeMode = MtjThemeMode.System, content: @Composable () -> Unit) {
    val darkTheme = when (themeMode) {
        MtjThemeMode.System -> isSystemInDarkTheme()
        MtjThemeMode.Light -> false
        MtjThemeMode.Dark -> true
    }
    MaterialTheme(
        colorScheme = if (darkTheme) MtjDarkColors else MtjLightColors,
        typography = MtjTypography,
        shapes = Shapes(
            extraSmall = RoundedCornerShape(MtjTokens.TarotFrameCorner),
            small = RoundedCornerShape(MtjTokens.ControlCorner),
            medium = RoundedCornerShape(MtjTokens.ActionCorner),
            large = RoundedCornerShape(MtjTokens.PanelCorner),
            extraLarge = RoundedCornerShape(MtjTokens.HeroCorner),
        ),
        content = content,
    )
}
