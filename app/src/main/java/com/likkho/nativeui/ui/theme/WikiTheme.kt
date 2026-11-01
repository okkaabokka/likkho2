@file:OptIn(ExperimentalTextApi::class)

package com.likkho.nativeui.ui.theme

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.core.view.WindowCompat

/** Colour tokens — 1:1 with the CSS variables (--wiki-*) of the web app. */
@Immutable
class WikiColors(
    val bg: Color,
    val surface: Color,
    val border: Color,
    val hairline: Color,
    val text: Color,
    val muted: Color,
    val link: Color,
    val isDark: Boolean,
) {
    val primary = Color(0xFF3366CC)
    val primaryPressed = Color(0xFF2A56B0)
    val danger = Color(0xFFB32424)
    val dangerPressed = Color(0xFF941D1D)
    val success = Color(0xFF14866D)
    val sun = Color(0xFFAC6600)
}

val LightWikiColors = WikiColors(
    bg = Color(0xFFFFFFFF),
    surface = Color(0xFFF8F9FA),
    border = Color(0xFFA2A9B1),
    hairline = Color(0xFFEAECF0),
    text = Color(0xFF202122),
    muted = Color(0xFF54595D),
    link = Color(0xFF3366CC),
    isDark = false,
)

val DarkWikiColors = WikiColors(
    bg = Color(0xFF101418),
    surface = Color(0xFF1A1F24),
    border = Color(0xFF3A4047),
    hairline = Color(0xFF272D34),
    text = Color(0xFFEAECF0),
    muted = Color(0xFF9AA0A6),
    link = Color(0xFF6699FF),
    isDark = true,
)

/** Cormorant Garamond (headings) · Lora (prose) · Plus Jakarta Sans (UI) · IBM Plex Mono (stamps). */
@Immutable
class WikiFonts(
    val serif: FontFamily,
    val prose: FontFamily,
    val sans: FontFamily,
    val mono: FontFamily,
    val shortStack: FontFamily = prose,
)

private fun assetFont(
    ctx: Context,
    file: String,
    weight: FontWeight,
    style: FontStyle = FontStyle.Normal,
): Font? {
    val exists = try {
        ctx.assets.list("fonts")?.contains(file) == true
    } catch (e: Exception) {
        false
    }
    if (!exists) return null
    return Font(path = "fonts/$file", assetManager = ctx.assets, weight = weight, style = style)
}

private fun family(fallback: FontFamily, fonts: List<Font?>): FontFamily {
    val real = fonts.filterNotNull()
    return if (real.isEmpty()) fallback else FontFamily(real)
}

/**
 * Fonts are read from assets/fonts or res/font. If a file is missing the app silently
 * falls back to a system serif / sans family, so the build never breaks.
 */
fun buildWikiFonts(ctx: Context): WikiFonts {
    val weights = listOf(
        FontWeight.Normal, FontWeight.Medium, FontWeight.SemiBold, FontWeight.Bold,
    )
    val serif = family(
        FontFamily.Serif,
        listOf(
            assetFont(ctx, "CormorantGaramond-Regular.ttf", FontWeight.Normal, FontStyle.Normal)
                ?: assetFont(ctx, "CormorantGaramond-Variable.ttf", FontWeight.Normal, FontStyle.Normal),
            assetFont(ctx, "CormorantGaramond-SemiBold.ttf", FontWeight.SemiBold, FontStyle.Normal)
                ?: assetFont(ctx, "CormorantGaramond-Variable.ttf", FontWeight.SemiBold, FontStyle.Normal),
            assetFont(ctx, "CormorantGaramond-Bold.ttf", FontWeight.Bold, FontStyle.Normal)
                ?: assetFont(ctx, "CormorantGaramond-Variable.ttf", FontWeight.Bold, FontStyle.Normal),
            assetFont(ctx, "CormorantGaramond-Italic.ttf", FontWeight.Normal, FontStyle.Italic)
                ?: assetFont(ctx, "CormorantGaramond-Variable.ttf", FontWeight.Normal, FontStyle.Italic),
            assetFont(ctx, "CormorantGaramond-BoldItalic.ttf", FontWeight.Bold, FontStyle.Italic)
                ?: assetFont(ctx, "CormorantGaramond-Variable.ttf", FontWeight.Bold, FontStyle.Italic),
        ),
    )
    val gildaFont = try {
        Font(com.likkho.nativeui.R.font.gilda_display, FontWeight.Normal)
    } catch (_: Throwable) {
        null
    }
    val shortStackFont = try {
        Font(com.likkho.nativeui.R.font.short_stack, FontWeight.Normal)
    } catch (_: Throwable) {
        assetFont(ctx, "ShortStack-Regular.ttf", FontWeight.Normal)
    }
    val prose = family(
        FontFamily.Serif,
        listOfNotNull(gildaFont),
    )
    val shortStack = family(
        FontFamily.SansSerif,
        listOfNotNull(shortStackFont),
    )
    val sans = family(
        FontFamily.SansSerif,
        weights.map { assetFont(ctx, "PlusJakartaSans-Variable.ttf", it) },
    )
    val mono = family(
        FontFamily.Monospace,
        listOf(
            assetFont(ctx, "IBMPlexMono-Regular.ttf", FontWeight.Normal),
            assetFont(ctx, "IBMPlexMono-Medium.ttf", FontWeight.Medium),
            assetFont(ctx, "IBMPlexMono-Medium.ttf", FontWeight.SemiBold),
            assetFont(ctx, "IBMPlexMono-Medium.ttf", FontWeight.Bold),
        ),
    )
    return WikiFonts(serif = serif, prose = prose, sans = sans, mono = mono, shortStack = shortStack)
}

val LocalWikiColors = staticCompositionLocalOf { LightWikiColors }
val LocalWikiFonts = staticCompositionLocalOf<WikiFonts> {
    error("LikkhoTheme is missing")
}

object Wiki {
    val colors: WikiColors
        @Composable @ReadOnlyComposable get() = LocalWikiColors.current
    val fonts: WikiFonts
        @Composable @ReadOnlyComposable get() = LocalWikiFonts.current
}

@Composable
fun LikkhoTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    val ctx = LocalContext.current
    val fonts = remember { buildWikiFonts(ctx) }
    val colors = if (darkTheme) DarkWikiColors else LightWikiColors

    val scheme = if (darkTheme) {
        darkColorScheme(
            primary = colors.primary,
            onPrimary = Color.White,
            background = colors.bg,
            onBackground = colors.text,
            surface = colors.surface,
            onSurface = colors.text,
            outline = colors.border,
        )
    } else {
        lightColorScheme(
            primary = colors.primary,
            onPrimary = Color.White,
            background = colors.bg,
            onBackground = colors.text,
            surface = colors.surface,
            onSurface = colors.text,
            outline = colors.border,
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(colorScheme = scheme) {
        CompositionLocalProvider(
            LocalWikiColors provides colors,
            LocalWikiFonts provides fonts,
            LocalContentColor provides colors.text,
            LocalTextSelectionColors provides TextSelectionColors(
                handleColor = colors.primary,
                backgroundColor = colors.primary.copy(alpha = 0.30f),
            ),
        ) {
            ProvideTextStyle(TextStyle(fontFamily = fonts.sans, color = colors.text)) {
                content()
            }
        }
    }
}
