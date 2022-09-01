package dk.pressere.kbkompas.compass

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import dk.pressere.kbkompas.R
import dk.pressere.kbkompas.settings.SettingsViewModel
import kotlin.math.roundToInt

private const val DISTANCE_FORMAT_MAX = 30000000f
private const val DISTANCE_FORMAT_MIN = 50f
private const val DISTANCE_SHOW_KILOS = 9999f

fun formatDistance(distance: Float, context: Context): String {
    var s = ""
    if (distance < 0) {
        // Special case.
        s = ""
    } else if (distance < DISTANCE_FORMAT_MIN) {
        s = "< " + DISTANCE_FORMAT_MIN.roundToInt() + " m"
    } else if (distance >= DISTANCE_FORMAT_MIN && distance < DISTANCE_SHOW_KILOS) {
        s = distance.roundToInt().toString() + " m"
    } else if (distance >= DISTANCE_SHOW_KILOS && distance < DISTANCE_FORMAT_MAX) {
        s = (distance / 1000).roundToInt().toString() + " km"
    } else if (distance > DISTANCE_FORMAT_MAX) {
        s = context.getString(R.string.error)
    }
    return s
}

@Composable
fun PickDestinationShowMap(
    navController: NavHostController,
    compassViewModel: CompassViewModel,
    settingsViewModel: SettingsViewModel
) {
    val mapLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {}
    val onDestinationClicked: (Destination) -> Unit = {
        val locationToShow = it
        val mapUri =
            Uri.parse("geo:${locationToShow.location.latitude},${locationToShow.location.longitude}?q=${locationToShow.address}")
        val openMapsIntent = Intent(
            Intent.ACTION_VIEW, mapUri
        )
        // Pop the backStack before launching to exit the view.
        navController.popBackStack()
        mapLauncher.launch(openMapsIntent)
    }
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            Text(
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
                text = "Find på kort:",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }

        // List items
        val destinations = compassViewModel.destinations
        LazyColumn {
            itemsIndexed(
                items = destinations
            ) { _, destination ->
                SimpleDestinationElement(
                    destination = destination,
                    destination.stateOfDistance.value,
                    compassViewModel.getIconResourceByName(destination.iconId),
                    onDestinationClicked,
                )
            }
        }
    }
}

@Composable
fun PickDestinationCompass(
    navController: NavHostController,
    compassViewModel: CompassViewModel,
    settingsViewModel: SettingsViewModel
) {
    val onDestinationClicked: (Destination) -> Unit = {
        compassViewModel.setTargetDestination(it)
        navController.navigate("compass") {
            popUpTo("compass")
            launchSingleTop = true
        }
    }

    // List items.
    val destinations = compassViewModel.destinations
    LazyColumn {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                Text(
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
                    text = "Favoritdestinationer:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }
        for (destination in destinations) {
            // Add an item only if is a favorite.
            if (destination.stateOfIsFavorite.value) {
                item {
                    SimpleDestinationElement(
                        destination = destination,
                        destination.stateOfDistance.value,
                        compassViewModel.getIconResourceByName(destination.iconId),
                        onDestinationClicked,

                    )
                }
            }
        }
        item {
            Divider()
            DestinationElementEmptyBox(
                text = "Rediger Destinationer",
                onClick = {
                    navController.navigate("destinations") {
                        popUpTo("compass")
                        launchSingleTop = true
                    }
                },
                iconId = R.drawable.ic_baseline_edit_location_24
            )
            Divider()
        }
    }
}

@Composable
fun SimpleDestinationElement(
    destination: Destination,
    distanceToDestination: Float,
    iconResourceId: Int,
    onClick: (Destination) -> Unit
) {
    Box(
        modifier = Modifier
            .clickable { onClick(destination) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(all = 0.dp)
        ) {
            Icon(
                painter = painterResource(id = iconResourceId),
                contentDescription = "Destination Icon",
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(all = 16.dp)
                    .size(40.dp)
            )
            Column(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .fillMaxWidth(0.8F)
                    .wrapContentWidth(Alignment.Start)
                    .align(Alignment.CenterVertically)
            ) {
                Text(
                    text = destination.name,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.primary,
                    maxLines = 1,
                    softWrap = false
                )

                Text(
                    text = formatDistance(distanceToDestination, LocalContext.current),
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

@Composable
fun AdvDestinationPicker(
    navController: NavHostController,
    compassViewModel: CompassViewModel,
    settingsViewModel: SettingsViewModel
    // A place to edit destinations.
) {
    val onDestinationClickedFavorite: (Destination) -> Unit = {
        it.stateOfIsFavorite.value = !it.stateOfIsFavorite.value
        compassViewModel.saveAllDestinations()
    }

    val onDestinationClickedDirections: (Destination) -> Unit = {
        compassViewModel.setTargetDestination(it)
        navController.navigate("compass") {
            popUpTo("compass")
            launchSingleTop = true
        }
    }

    val onClicked: (Int) -> Unit = {
        navController.navigate("destination_editor/$it")
    }

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            Text(
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp),
                text = "Vælg en destination at redigere",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        val destinations = compassViewModel.destinations
        LazyColumn {
            itemsIndexed(
                items = destinations
            ) { index, destination ->
                AdvDestinationElement(
                    destination = destination,
                    destination.stateOfDistance.value,
                    index,
                    compassViewModel.getIconResourceByName(destination.iconId),
                    onDestinationClickedFavorite,
                    onDestinationClickedDirections,
                    onClicked
                )
            }

            item {
                Divider()
                DestinationElementEmptyBox(
                    text = "Tilføj ny destination",
                    onClick = {
                        onClicked(-1)
                    }
                )
                Divider()
            }
        }
    }
}

@Composable
fun AdvDestinationElement(
    destination: Destination,
    distanceToDestination: Float,
    index: Int,
    iconResourceId: Int,
    onFavoriteClicked: (Destination) -> Unit,
    onDirectionsClicked: (Destination) -> Unit,
    onClick: (Int) -> Unit
) {
    var starColor = MaterialTheme.colors.onBackground
    if (destination.stateOfIsFavorite.value) {
        starColor = MaterialTheme.colors.primary
    }

    Box(
        modifier = Modifier
            .clickable { onClick(index) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(all = 0.dp)
        ) {
            Icon(
                painter = painterResource(id = iconResourceId),
                contentDescription = "Destination Icon",
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(all = 16.dp)
                    .size(40.dp)
            )
            Column(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .wrapContentWidth(Alignment.Start)
                    .fillMaxWidth(0.5f)
                    .align(Alignment.CenterVertically)
            ) {
                Text(
                    text = destination.name,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.primary,
                    maxLines = 1,
                    softWrap = false
                )

                Text(
                    text = formatDistance(distanceToDestination, LocalContext.current),
                    maxLines = 1,
                    softWrap = false
                )
            }

            // Forces the content to the end.
            Spacer(modifier = Modifier.weight(1f))

            // Use box so the button is easier to click.
            Box(
                Modifier
                    .align(Alignment.CenterVertically)
                    .size(64.dp)
                    .padding(4.dp)
                    .clickable { onFavoriteClicked(destination) }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_baseline_favorite_24),
                    contentDescription = "Favorite",
                    tint = starColor,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(40.dp)
                )
            }

            Box(
                Modifier
                    .align(Alignment.CenterVertically)
                    .size(64.dp)
                    .padding(4.dp)
                    .clickable { onDirectionsClicked(destination) }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_baseline_directions_24),
                    contentDescription = "Directions to ${destination.name}",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(40.dp)
                )
            }


        }
    }
}

@Composable
fun DestinationElementEmptyBox (
    text: String,
    onClick: () -> Unit,
    iconId: Int? = null,
) {
    Box(
        modifier = Modifier
            .clickable { onClick() }
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(all = 0.dp)

        ) {
            Text(
                text = text,
                color = MaterialTheme.colors.primary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }
    }
}