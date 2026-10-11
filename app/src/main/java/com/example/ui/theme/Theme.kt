package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * =====================================================================
 * Futuristic Pharmacy Design System Color Palette (الهوية البصرية والألوان)
 * =====================================================================
 * Dark Teal:         #003B40
 * Deep Teal:         #004B50
 * Dark Background:   #002E33
 * Primary Cyan/Teal: #00B8B0
 * Bright Teal:       #12CFC5
 * Light Cyan:        #B9F0EA
 * Very Light Cyan:   #D8F7F3
 * White:             #FFFFFF
 * Text White:        #EAFDFC
 * Secondary Text:    #9BC9C7
 * Border:            #397B7B
 * Warning:           #F1C75B
 * Danger:            #E65F62
 * Success:           #32C48D
 */

// Direct aliases for colors requested in the prompt
val DarkTeal: Color = PharmacyDarkTeal
val DeepTeal: Color = PharmacyDeepTeal
val DarkBackground: Color = PharmacyDarkBg
val PrimaryCyan: Color = PharmacyPrimaryCyan
val BrightTeal: Color = PharmacyBrightTeal
val LightCyan: Color = PharmacyLightCyan
val VeryLightCyan: Color = PharmacyVeryLightCyan
val White: Color = PharmacyWhite
val TextWhite: Color = PharmacyTextWhite
val SecondaryText: Color = PharmacySecondaryText
val Border: Color = PharmacyBorder
val Warning: Color = PharmacyWarning
val Danger: Color = PharmacyDanger
val Success: Color = PharmacySuccess

/**
 * Material 3 Dark Color Scheme tailored for futuristic pharmacy UI
 */
val DarkColorScheme = darkColorScheme(
    primary = PrimaryCyan,                  // #00B8B0
    onPrimary = White,                      // #FFFFFF
    primaryContainer = DeepTeal,            // #004B50
    onPrimaryContainer = LightCyan,         // #B9F0EA
    secondary = BrightTeal,                 // #12CFC5
    onSecondary = DarkBackground,           // #002E33
    secondaryContainer = DarkTeal,          // #003B40
    onSecondaryContainer = VeryLightCyan,   // #D8F7F3
    tertiary = BrightTeal,                  // #12CFC5
    onTertiary = DarkBackground,            // #002E33
    background = DarkBackground,            // #002E33
    onBackground = TextWhite,               // #EAFDFC
    surface = DarkTeal,                     // #003B40
    onSurface = TextWhite,                  // #EAFDFC
    surfaceVariant = PharmacyCardBg,        // #06484B
    onSurfaceVariant = SecondaryText,       // #9BC9C7
    outline = Border,                       // #397B7B
    outlineVariant = PharmacyInputBorder,   // #3B8583
    error = Danger,                         // #E65F62
    onError = White                         // #FFFFFF
)

/**
 * Material 3 Light Color Scheme
 */
val LightColorScheme = lightColorScheme(
    primary = PrimaryCyan,                  // #00B8B0
    onPrimary = White,
    primaryContainer = LightCyan,           // #B9F0EA
    onPrimaryContainer = DarkTeal,          // #003B40
    secondary = BrightTeal,                 // #12CFC5
    onSecondary = White,
    secondaryContainer = VeryLightCyan,     // #D8F7F3
    onSecondaryContainer = DarkBackground,
    tertiary = DeepTeal,                    // #004B50
    onTertiary = White,
    background = PharmacyLightBg,           // #F0FAF9
    onBackground = PharmacyLightText,       // #003B40
    surface = PharmacyLightSurface,         // #FFFFFF
    onSurface = PharmacyLightText,
    surfaceVariant = PharmacyLightCard,     // #E3F7F5
    onSurfaceVariant = PharmacyLightSecondaryText, // #3B6B6D
    outline = PharmacyLightBorder,          // #B0DBD8
    outlineVariant = Border,
    error = Danger,                         // #E65F62
    onError = White
)

/**
 * Saydali Futuristic Pharmacy Material 3 Theme
 */
@Composable
fun SaydaliPharmacyTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

/**
 * Convenient alias for SaydaliPharmacyTheme
 */
@Composable
fun SaydaliTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    SaydaliPharmacyTheme(darkTheme = darkTheme, content = content)
}
