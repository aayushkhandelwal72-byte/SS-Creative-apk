package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = NavyPrimary,
    onPrimary = CrispWhite,
    primaryContainer = NavyContainer,
    onPrimaryContainer = OnNavyContainer,
    secondary = NavyLight,
    onSecondary = CrispWhite,
    secondaryContainer = Color(0xFFE0E7FF),
    onSecondaryContainer = NavyDark,
    tertiary = PeacockTeal,
    onTertiary = CrispWhite,
    tertiaryContainer = Color(0xFFCCFBF1),
    onTertiaryContainer = Color(0xFF115E59),
    background = OffWhite,
    onBackground = TextPrimary,
    surface = SurfaceLight,
    onSurface = TextPrimary, // Navy Blue text filling
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondary,
    outline = OutlineLight,
    outlineVariant = Color(0xFFD6E2F0)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF93C5FD),
    onPrimary = NavyDark,
    primaryContainer = NavyLight,
    onPrimaryContainer = CrispWhite,
    secondary = Color(0xFFBAE6FD),
    onSecondary = NavyDark,
    tertiary = PeacockGold,
    onTertiary = NavyDark,
    background = DarkBackground,
    onBackground = Color(0xFFF1F5F9),
    surface = DarkSurface,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = DarkOutline
)

/**
 * Common text field colors ensuring text filling and container fill are Navy Blue themed.
 */
@Composable
fun navyTextFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedTextColor = NavyPrimary,
    unfocusedTextColor = NavyPrimary,
    focusedContainerColor = NavyFieldFocusedFill,
    unfocusedContainerColor = NavyFieldFill,
    focusedBorderColor = NavyPrimary,
    unfocusedBorderColor = OutlineLight,
    cursorColor = NavyPrimary,
    focusedLabelColor = NavyPrimary,
    unfocusedLabelColor = NavyLight,
    focusedLeadingIconColor = NavyPrimary,
    unfocusedLeadingIconColor = NavyLight,
    focusedTrailingIconColor = NavyPrimary,
    unfocusedTrailingIconColor = NavyLight,
    focusedPlaceholderColor = NavyLight.copy(alpha = 0.65f),
    unfocusedPlaceholderColor = Color(0xFF64748B)
)

@Composable
fun SSCreativeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val customTextSelectionColors = TextSelectionColors(
        handleColor = NavyPrimary,
        backgroundColor = NavyLight.copy(alpha = 0.35f)
    )

    CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

