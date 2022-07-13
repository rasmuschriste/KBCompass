package dk.pressere.kbkompas.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorPalette = darkColors(
    primary = Primary,
    primaryVariant = PrimaryVariant,
    onPrimary = White,

    secondary = Primary,
    secondaryVariant = Primary,
    onSecondary = White,

    background = Dark,
    onBackground = White,
    surface = Dark,
    onSurface = White,

    error = Error,
    onError = Black
)

private val LightColorPalette = lightColors(
    primary = Primary,
    primaryVariant = PrimaryVariant,
    onPrimary = White,

    secondary = CompassBackgroundLight,
    secondaryVariant = CompassBackgroundLight,
    onSecondary = Primary,

    background = White,
    onBackground = Black,
    surface = White,
    onSurface = Black,

    error = Error,
    onError = Black
)

private val ClownColorPalette = lightColors(
    primary = Primary,
    primaryVariant = PrimaryVariant,
    onPrimary = White,

    secondary = Color.Cyan,
    secondaryVariant = Color.Green,
    onSecondary = White,

    background = Color.Blue,
    onBackground = Black,
    surface = Color.Magenta,
    onSurface = Black,

    error = Error,
    onError = Black
)

@Composable
fun BarFinder2Theme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable() () -> Unit) {
    var colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    //colors = ClownColorPalette

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}