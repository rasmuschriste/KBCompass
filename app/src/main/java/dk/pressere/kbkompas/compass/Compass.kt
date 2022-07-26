package dk.pressere.kbkompas.compass

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dk.pressere.kbkompas.R

@Composable
fun CompassContent(
    compassViewModel: CompassViewModel,
) {
    // Debug Box
    /*Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(Color.Cyan)
    ) {
        Text(text = "")
    }*/



    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        var colorArrow = Color.Black
        var colorDisk = Color.Black

        if (MaterialTheme.colors.isLight) {
            colorArrow = MaterialTheme.colors.primary
            colorDisk = MaterialTheme.colors.secondary
        } else {
            colorArrow = MaterialTheme.colors.onBackground
            colorDisk = MaterialTheme.colors.primaryVariant
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val rotation: Float by animateFloatAsState(
                targetValue = compassViewModel.targetRotation.value,
                animationSpec = spring(
                    stiffness = Spring.StiffnessLow,
                    dampingRatio = Spring.DampingRatioNoBouncy
                )
            )
            Image(
                painter = painterResource(id = R.drawable.compass_disk),
                contentDescription = "Compass",
                colorFilter = ColorFilter.tint(colorDisk),
                modifier = Modifier
                    .rotate(rotation)
            )
            Image(
                painter = painterResource(id = R.drawable.compass_arrow),
                contentDescription = "Compass direction",
                colorFilter = ColorFilter.tint(colorArrow),
                modifier = Modifier
                    .rotate(rotation)
            )
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = compassViewModel.nameDest.value,
                color = colorArrow,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            Text(
                text = formatDistance(compassViewModel.distDest.value, LocalContext.current),
                color = colorArrow,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}