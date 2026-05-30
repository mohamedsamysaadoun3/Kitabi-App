package com.kitabi.app.core.designsystem

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * ألوان كتابي المخصصة
 * تمديد لألوان Material 3 مع إضافة ألوان خاصة
 */
data class KitabiColors(
    // الألوان الأساسية
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,

    // الألوان الثانوية
    val secondary: Color,
    val onSecondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,

    // الألوان الثالثية
    val tertiary: Color,
    val onTertiary: Color,
    val tertiaryContainer: Color,
    val onTertiaryContainer: Color,

    // ألوان الخلفية
    val background: Color,
    val onBackground: Color,

    // ألوان السطح
    val surface: Color,
    val onSurface: Color,
    val surfaceVariant: Color,
    val onSurfaceVariant: Color,

    // ألوان الخطأ
    val error: Color,
    val onError: Color,
    val errorContainer: Color,
    val onErrorContainer: Color,

    // ألوان الحدود
    val outline: Color,
    val outlineVariant: Color,

    // ألوان خاصة بكتابي
    val bookCover: Color,
    val bookPage: Color,
    val bookText: Color,
    val bookmark: Color,
    val ratingStar: Color,
    val progress: Color,
)

/**
 * مزود محلي لألوان كتابي
 */
private val LocalKitabiColors = compositionLocalOf { lightKitabiColors() }

/**
 * الوصول لألوان كتابي من أي مكان في التطبيق
 */
object KitabiTheme {
    val colors: KitabiColors
        @Composable
        @ReadOnlyComposable
        get() = LocalKitabiColors.current
}

/**
 * إنشاء ألوان كتابي للوضع الفاتح
 */
fun lightKitabiColors(): KitabiColors = KitabiColors(
    primary = PrimaryLight,
    onPrimary = OnPrimaryLight,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = OnPrimaryContainerLight,
    secondary = SecondaryLight,
    onSecondary = OnSecondaryLight,
    secondaryContainer = SecondaryContainerLight,
    onSecondaryContainer = OnSecondaryContainerLight,
    tertiary = TertiaryLight,
    onTertiary = OnTertiaryLight,
    tertiaryContainer = TertiaryContainerLight,
    onTertiaryContainer = OnTertiaryContainerLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    error = ErrorLight,
    onError = OnErrorLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight,
    outline = Color(0xFF727972),
    outlineVariant = Color(0xFFC2C9C0),
    bookCover = BookCoverColor,
    bookPage = BookPageColor,
    bookText = BookTextColor,
    bookmark = BookmarkColor,
    ratingStar = RatingStarColor,
    progress = ProgressColor,
)

/**
 * إنشاء ألوان كتابي للوضع الداكن
 */
fun darkKitabiColors(): KitabiColors = KitabiColors(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    tertiary = TertiaryDark,
    onTertiary = OnTertiaryDark,
    tertiaryContainer = TertiaryContainerDark,
    onTertiaryContainer = OnTertiaryContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,
    outline = Color(0xFF8C938C),
    outlineVariant = Color(0xFF414942),
    bookCover = Color(0xFF1A252F),
    bookPage = Color(0xFF2D2D2D),
    bookText = ReaderTextDark,
    bookmark = Color(0xFFE8C44A),
    ratingStar = Color(0xFFE8C44A),
    progress = Color(0xFF80CBC4),
)

/**
 * مخطط الألوان الفاتح لـ Material 3
 */
private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimaryLight,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = OnPrimaryContainerLight,
    secondary = SecondaryLight,
    onSecondary = OnSecondaryLight,
    secondaryContainer = SecondaryContainerLight,
    onSecondaryContainer = OnSecondaryContainerLight,
    tertiary = TertiaryLight,
    onTertiary = OnTertiaryLight,
    tertiaryContainer = TertiaryContainerLight,
    onTertiaryContainer = OnTertiaryContainerLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    error = ErrorLight,
    onError = OnErrorLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight,
    outline = Color(0xFF727972),
    outlineVariant = Color(0xFFC2C9C0),
)

/**
 * مخطط الألوان الداكن لـ Material 3
 */
private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    tertiary = TertiaryDark,
    onTertiary = OnTertiaryDark,
    tertiaryContainer = TertiaryContainerDark,
    onTertiaryContainer = OnTertiaryContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,
    outline = Color(0xFF8C938C),
    outlineVariant = Color(0xFF414942),
)

/**
 * السمة الرئيسية لتطبيق كتابي
 * تدعم الوضع الفاتح والداكن مع ألوان عربية دافئة
 *
 * @param darkTheme هل نستخدم الوضع الداكن
 * @param content المحتوى الذي سيتم تطبيق السمة عليه
 */
@Composable
fun KitabiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // اختيار ألوان كتابي المناسبة
    val kitabiColors = if (darkTheme) darkKitabiColors() else lightKitabiColors()

    // اختيار مخطط Material 3 المناسب
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    // إعداد شريط الحالة
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    // توفير ألوان كتابي عبر CompositionLocal
    CompositionLocalProvider(LocalKitabiColors provides kitabiColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = KitabiTypography,
            content = content
        )
    }
}
