package dk.pressere.kbkompas.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex

// A window that hovers over the current screen.
@Composable
fun FloatingWindow(
    enabled: Boolean,
    onBackPressed : () -> Unit,
    content: @Composable () -> Unit = {}
) {
    if (enabled) {
        BackHandler {
            onBackPressed()
        }

        // Outer surface to capture click events and fade out background.
        Box(
            Modifier
                .fillMaxSize()
                .zIndex(1f)
                .background(Color(0f, 0f, 0f, 0.7f))
                .clickable {
                    // Clicking outside should also trigger a bag press.
                    onBackPressed()
                },
        ) {
            Box(
                Modifier
                    .padding(24.dp)
                    .background(MaterialTheme.colors.background)
                    .clickable(false) { } // Needed to capture clicks.
            ) {
                content()
            }
        }
    }
}